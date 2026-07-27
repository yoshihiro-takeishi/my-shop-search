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
                                     List<String> categoryIds, String storeName, 
                                     String sortBy, boolean independentOnly, Double radius) {
        
        StringBuilder queryBuilder = new StringBuilder();
        if (storeName != null && !storeName.isEmpty()) queryBuilder.append(storeName).append(" ");
        if (locationName != null && !locationName.isEmpty()) queryBuilder.append(locationName).append(" ");
        if (categoryIds != null && !categoryIds.isEmpty()) {
            String catKeywords = categoryIds.stream()
                .map(id -> categoryService.getById(id)).filter(Optional::isPresent)
                .flatMap(opt -> opt.get().keywords().stream()).collect(Collectors.joining(" "));
            queryBuilder.append(catKeywords).append(" ");
        }
        if (independentOnly && (storeName == null || storeName.isEmpty())) {
            queryBuilder.append(" 個人店 隠れ家 -チェーン店");
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) return Collections.emptyList();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", query);
        requestBody.put("maxResultCount", 10);
        requestBody.put("languageCode", "ja");

        if (lat != null && lng != null) {
            requestBody.put("locationRestriction", Map.of(
                "circle", Map.of(
                    "center", Map.of("latitude", lat, "longitude", lng),
                    "radius", radius
                )
            ));
        }

        try {
            var response = restClient.post().uri("/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.rating,places.userRatingCount,places.formattedAddress,places.googleMapsUri,places.location,places.photos,places.priceLevel,places.currentOpeningHours,places.editorialSummary,places.websiteUri,places.reviews,places.regularOpeningHours")
                .body(requestBody).retrieve().body(Map.class);

            if (response == null || !response.containsKey("places")) return Collections.emptyList();
            List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");

            return places.stream().map(p -> {
                String id = (String) p.get("id");
                String name = (p.get("displayName") instanceof Map<?, ?> m) ? (String) m.get("text") : "名称不明";

                // 数値データの安全なパース
                Double pLat = null, pLng = null;
                Integer dist = null;
                if (p.get("location") instanceof Map<?, ?> loc) {
                    pLat = convertToDouble(loc.get("latitude"));
                    pLng = convertToDouble(loc.get("longitude"));
                    if (lat != null && lng != null && pLat != null && pLng != null) {
                        dist = (int)(calculateDistance(lat, lng, pLat, pLng) * 1000);
                    }
                }

                String summary = (p.get("editorialSummary") instanceof Map<?, ?> m) ? (String) m.get("text") : null;
                String reviewSnippet = null;
                if (p.get("reviews") instanceof List<?> reviews && !reviews.isEmpty()) {
                    if (reviews.get(0) instanceof Map<?, ?> first && first.get("text") instanceof Map<?, ?> t) {
                        reviewSnippet = (String) t.get("text");
                    }
                }

                List<String> weekdayText = (p.get("regularOpeningHours") instanceof Map<?, ?> reg) ? (List<String>) reg.get("weekdayDescriptions") : null;
                Boolean openNow = (p.get("currentOpeningHours") instanceof Map<?, ?> cur) ? (Boolean) cur.get("openNow") : null;

                String photoRef = null;
                if (p.get("photos") instanceof List<?> photos && !photos.isEmpty()) {
                    photoRef = (String) ((Map<?, ?>) photos.get(0)).get("name");
                }

                // 予算レベル（String Enum に対応）
                String priceStr = formatPriceLevel(p.get("priceLevel"));

                return new PlaceResponse(
                    id, name, 
                    convertToDouble(p.get("rating")),
                    convertToInteger(p.get("userRatingCount")),
                    (String) p.get("formattedAddress"), (String) p.get("googleMapsUri"),
                    dist, photoRef, pLat, pLng, priceStr, openNow, summary, (String) p.get("websiteUri"), reviewSnippet, weekdayText
                );
            }).sorted(getComparator(sortBy, lat != null)).toList();
        } catch (Exception e) { 
            e.printStackTrace(); 
            throw e; 
        }
    }

    // 数値変換ヘルパー
    private Double convertToDouble(Object obj) {
        if (obj instanceof Number n) return n.doubleValue();
        if (obj instanceof String s) try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
        return 0.0;
    }

    private Integer convertToInteger(Object obj) {
        if (obj instanceof Number n) return n.intValue();
        if (obj instanceof String s) try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
        return 0;
    }

    // 予算レベルの変換 (Enum文字列を ￥ マークに変換)
    private String formatPriceLevel(Object obj) {
        if (obj == null) return null;
        String level = obj.toString(); // "PRICE_LEVEL_MODERATE" 等
        return switch (level) {
            case "PRICE_LEVEL_INEXPENSIVE" -> "￥";
            case "PRICE_LEVEL_MODERATE" -> "￥￥";
            case "PRICE_LEVEL_EXPENSIVE" -> "￥￥￥";
            case "PRICE_LEVEL_VERY_EXPENSIVE" -> "￥￥￥￥";
            default -> null;
        };
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1); double dLon = Math.toRadians(lon2 - lon1);
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