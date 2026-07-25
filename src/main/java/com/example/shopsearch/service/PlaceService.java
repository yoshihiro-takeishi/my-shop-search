package com.example.shopsearch.service;

import com.example.shopsearch.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    @Value("${google.places.api-key}")
    private String apiKey;

    private final RestClient restClient;
    private final CategoryService categoryService;

    public PlaceService(RestClient.Builder builder, CategoryService categoryService) {
        this.restClient = builder.baseUrl("https://places.googleapis.com/v1").build();
        this.categoryService = categoryService;
    }

    public List<PlaceResponse> search(Double lat, Double lng, String locationName, 
                                     List<String> categoryIds, String sortBy, boolean independentOnly) {
        
        // 1. 選択された全カテゴリの全キーワード（List<String>）を抽出し、1つの文字列に結合
        String combinedKeywords = categoryIds.stream()
                .map(id -> categoryService.getById(id)) // Optional<Category>を取得
                .filter(Optional::isPresent)           // 存在するカテゴリのみ
                .flatMap(opt -> opt.get().keywords().stream()) // カテゴリ内のキーワードリストを平坦化
                .collect(Collectors.joining(" "));     // 半角スペースで結合

        if (combinedKeywords.isEmpty()) return Collections.emptyList();

        // 2. 個人店フィルターの適用
        if (independentOnly) {
            combinedKeywords += " 個人店 隠れ家 -チェーン店";
        }

        String query = (locationName != null && !locationName.isEmpty() ? locationName + " " : "") + combinedKeywords;

        // 3. Google APIリクエスト作成 (コスト削減のため10件制限)
        Map<String, Object> request = new HashMap<>();
        request.put("textQuery", query);
        request.put("maxResultCount", 10);
        request.put("languageCode", "ja");

        if (lat != null && lng != null) {
            request.put("locationBias", Map.of("circle", Map.of(
                "center", Map.of("latitude", lat, "longitude", lng), 
                "radius", 5000.0)));
        }

        // 4. Google API呼び出し
        var response = restClient.post()
                .uri("/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.displayName,places.rating,places.userRatingCount,places.formattedAddress,places.googleMapsUri,places.location,places.photos")
                .body(request).retrieve().body(Map.class);

        List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");
        if (places == null) return Collections.emptyList();

        // 5. 結果をDTOに変換
        return places.stream().map(p -> {
            Map<String, Object> loc = (Map<String, Object>) p.get("location");
            Double pLat = (Double) loc.get("latitude");
            Double pLng = (Double) loc.get("longitude");
            
            List<Map<String, Object>> photos = (List<Map<String, Object>>) p.get("photos");
            String photoRef = (photos != null && !photos.isEmpty()) ? (String) photos.get(0).get("name") : null;
            
            Integer dist = (lat != null && lng != null) ? (int)(calculateDistance(lat, lng, pLat, pLng) * 1000) : null;

            return new PlaceResponse(
                (String) ((Map) p.get("displayName")).get("text"),
                p.get("rating") != null ? ((Number) p.get("rating")).doubleValue() : 0.0,
                p.get("userRatingCount") != null ? ((Number) p.get("userRatingCount")).intValue() : 0,
                (String) p.get("formattedAddress"), 
                (String) p.get("googleMapsUri"),
                dist, photoRef, pLat, pLng
            );
        }).sorted(getComparator(sortBy, lat != null)).toList();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private Comparator<PlaceResponse> getComparator(String sortBy, boolean hasGps) {
        return switch (sortBy) {
            case "distance" -> hasGps ? Comparator.comparing(PlaceResponse::distanceMeters, Comparator.nullsLast(Comparator.naturalOrder())) : Comparator.comparing(PlaceResponse::rating).reversed();
            case "userRatingsTotal" -> Comparator.comparing(PlaceResponse::userRatingCount).reversed();
            default -> Comparator.comparing(PlaceResponse::rating).reversed();
        };
    }
}