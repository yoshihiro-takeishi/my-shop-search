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

    // 引数に boolean openNow を確実に含めています（合計9個）
    public List<PlaceResponse> search(Double lat, Double lng, String locationName, 
                                     List<String> categoryIds, String storeName, 
                                     String sortBy, boolean independentOnly, Double radius, boolean openNow) {
        
        StringBuilder queryBuilder = new StringBuilder();
        boolean isStoreSearch = (storeName != null && !storeName.isEmpty());

        if (isStoreSearch) queryBuilder.append(storeName).append(" ");
        if (locationName != null && !locationName.isEmpty()) queryBuilder.append(locationName).append(" ");
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            String catKeywords = categoryIds.stream()
                .map(id -> categoryService.getById(id)).filter(Optional::isPresent)
                .flatMap(opt -> opt.get().keywords().stream()).collect(Collectors.joining(" "));
            queryBuilder.append(catKeywords);
        }

        if (independentOnly && !isStoreSearch) {
            queryBuilder.append(" 個人店 隠れ家 -チェーン店");
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) return Collections.emptyList();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", query);
        requestBody.put("maxResultCount", 10);
        requestBody.put("languageCode", "ja");
        requestBody.put("openNow", openNow); // これで「openNow cannot be resolved」が消えます

        if (lat != null && lng != null) {
            Map<String, Object> circle = new HashMap<>();
            circle.put("center", Map.of("latitude", lat, "longitude", lng));
            circle.put("radius", (radius != null) ? radius : 5000.0);
            requestBody.put("locationBias", Map.of("circle", circle));
        }

        try {
            var response = restClient.post().uri("/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.rating,places.userRatingCount,places.formattedAddress,places.googleMapsUri,places.location,places.photos,places.priceLevel,places.currentOpeningHours,places.editorialSummary,places.websiteUri,places.reviews,places.regularOpeningHours")
                .body(requestBody).retrieve().body(Map.class);

            if (response == null || !response.containsKey("places")) return Collections.emptyList();
            
            // 型安全性の警告を抑制
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");

            return places.stream().map(p -> {
                String id = (String) p.get("id");
                String name = (p.get("displayName") instanceof Map<?, ?> m) ? (String) m.get("text") : "名称不明";

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
                    if (reviews.get(0) instanceof Map<?, ?> r && r.get("text") instanceof Map<?, ?> t) {
                        reviewSnippet = (String) t.get("text");
                    }
                }

                List<String> weekdayText = null;
                if (p.get("regularOpeningHours") instanceof Map<?, ?> reg) {
                    // capture#17 警告を回避するためのキャスト
                    Object desc = reg.get("weekdayDescriptions");
                    if (desc instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> casted = (List<String>) desc;
                        weekdayText = casted;
                    }
                }

                Boolean isOpen = (p.get("currentOpeningHours") instanceof Map<?, ?> cur) ? (Boolean) cur.get("openNow") : null;

                String photoRef = null;
                if (p.get("photos") instanceof List<?> ph && !ph.isEmpty()) {
                    if (ph.get(0) instanceof Map<?, ?> firstPhoto) {
                        photoRef = (String) firstPhoto.get("name");
                    }
                }

                return new PlaceResponse(
                    id, name, convertToDouble(p.get("rating")), convertToInteger(p.get("userRatingCount")),
                    (String) p.get("formattedAddress"), (String) p.get("googleMapsUri"),
                    dist, photoRef, pLat, pLng, formatPriceLevel(p.get("priceLevel")), isOpen, summary, (String) p.get("websiteUri"), reviewSnippet, weekdayText
                );
            }).sorted(getComparator(sortBy, lat != null)).toList();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Double convertToDouble(Object obj) {
        if (obj instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private Integer convertToInteger(Object obj) {
        if (obj instanceof Number n) return n.intValue();
        return 0;
    }

    private String formatPriceLevel(Object obj) {
        if (obj == null) return null;
        String s = obj.toString();
        if (s.contains("INEXPENSIVE")) return "￥";
        if (s.contains("MODERATE")) return "￥￥";
        if (s.contains("VERY_EXPENSIVE")) return "￥￥￥￥";
        if (s.contains("EXPENSIVE")) return "￥￥￥";
        return null;
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