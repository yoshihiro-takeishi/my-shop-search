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
                                     List<String> categoryIds, String storeName, String sortBy, boolean independentOnly) {
        
        StringBuilder queryBuilder = new StringBuilder();
        if (storeName != null && !storeName.isEmpty()) queryBuilder.append(storeName).append(" ");
        if (locationName != null && !locationName.isEmpty()) queryBuilder.append(locationName).append(" ");
        if (categoryIds != null && !categoryIds.isEmpty()) {
            String catKeywords = categoryIds.stream()
                .map(id -> categoryService.getById(id)).filter(Optional::isPresent)
                .flatMap(opt -> opt.get().keywords().stream()).collect(Collectors.joining(" "));
            queryBuilder.append(catKeywords);
        }
        if (independentOnly && queryBuilder.length() > 0) {
            queryBuilder.append(" 個人店 隠れ家 -チェーン店");
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) return Collections.emptyList();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", query);
        requestBody.put("maxResultCount", 10);
        requestBody.put("languageCode", "ja");
        if (lat != null && lng != null) {
            requestBody.put("locationBias", Map.of("circle", Map.of("center", Map.of("latitude", lat, "longitude", lng), "radius", 5000.0)));
        }

        try {
            var response = restClient.post().uri("/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.rating,places.userRatingCount,places.formattedAddress,places.googleMapsUri,places.location,places.photos,places.priceLevel,places.currentOpeningHours,places.editorialSummary,places.websiteUri,places.reviews,places.regularOpeningHours")
                .body(requestBody).retrieve().body(Map.class);

            if (response == null || !response.containsKey("places")) return Collections.emptyList();
            List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");

            return places.stream().map(p -> {
                try {
                    // 1. 基本情報の安全取得
                    String id = (String) p.get("id");
                    String name = "名称不明";
                    if (p.get("displayName") instanceof Map<?, ?> m && m.get("text") != null) {
                        name = (String) m.get("text");
                    }

                    // 2. 座標と距離の安全取得
                    Double pLat = null, pLng = null;
                    Integer dist = null;
                    if (p.get("location") instanceof Map<?, ?> loc) {
                        Object latObj = loc.get("latitude");
                        Object lngObj = loc.get("longitude");
                        if (latObj instanceof Number && lngObj instanceof Number) {
                            pLat = ((Number) latObj).doubleValue();
                            pLng = ((Number) lngObj).doubleValue();
                            if (lat != null && lng != null) {
                                dist = (int)(calculateDistance(lat, lng, pLat, pLng) * 1000);
                            }
                        }
                    }

                    // 3. 概要
                    String summary = null;
                    if (p.get("editorialSummary") instanceof Map<?, ?> m) summary = (String) m.get("text");

                    // 4. クチコミ
                    String reviewSnippet = null;
                    if (p.get("reviews") instanceof List<?> reviews && !reviews.isEmpty()) {
                        if (reviews.get(0) instanceof Map<?, ?> first && first.get("text") instanceof Map<?, ?> t) {
                            reviewSnippet = (String) t.get("text");
                        }
                    }

                    // 5. 営業時間
                    List<String> weekdayText = null;
                    if (p.get("regularOpeningHours") instanceof Map<?, ?> reg) {
                        weekdayText = (List<String>) reg.get("weekdayDescriptions");
                    }
                    Boolean openNow = null;
                    if (p.get("currentOpeningHours") instanceof Map<?, ?> cur) {
                        openNow = (Boolean) cur.get("openNow");
                    }

                    // 6. 写真・予算・サイト
                    String photoRef = null;
                    if (p.get("photos") instanceof List<?> photos && !photos.isEmpty()) {
                        if (photos.get(0) instanceof Map<?, ?> firstPhoto) photoRef = (String) firstPhoto.get("name");
                    }

                    String priceStr = null;
                    if (p.get("priceLevel") != null && p.get("priceLevel") instanceof Number num) {
                        priceStr = "￥".repeat(Math.max(1, num.intValue()));
                    }

                    return new PlaceResponse(
                        id, name,
                        p.get("rating") != null ? ((Number) p.get("rating")).doubleValue() : 0.0,
                        p.get("userRatingCount") != null ? ((Number) p.get("userRatingCount")).intValue() : 0,
                        (String) p.get("formattedAddress"), (String) p.get("googleMapsUri"),
                        dist, photoRef, pLat, pLng,
                        priceStr, openNow, summary, (String) p.get("websiteUri"), reviewSnippet, weekdayText
                    );
                } catch (Exception e) {
                    return null; // 個別の店舗データに不備がある場合はスキップ
                }
            }).filter(Objects::nonNull).sorted(getComparator(sortBy, lat != null)).toList();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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