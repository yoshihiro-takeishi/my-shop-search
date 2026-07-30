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
        if (independentOnly && !isStoreSearch) queryBuilder.append(" 個人店 隠れ家 -チェーン店");

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) return Collections.emptyList();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("textQuery", query);
        requestBody.put("maxResultCount", 20);
        requestBody.put("languageCode", "ja");
        requestBody.put("openNow", openNow);

        if (lat != null && lng != null) {
            Map<String, Object> circle = Map.of("center", Map.of("latitude", lat, "longitude", lng), "radius", (radius != null ? radius : 5000.0));
            requestBody.put("locationBias", Map.of("circle", circle));
        }

        try {
            var response = restClient.post().uri("/places:searchText")
                .header("X-Goog-Api-Key", apiKey)
                // places.menuUri を削除
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.rating,places.userRatingCount,places.formattedAddress,places.googleMapsUri,places.location,places.photos,places.priceLevel,places.currentOpeningHours,places.editorialSummary,places.websiteUri,places.reviews,places.regularOpeningHours")
                .body(requestBody).retrieve().body(Map.class);

            if (response == null || !response.containsKey("places")) return Collections.emptyList();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> places = (List<Map<String, Object>>) response.get("places");

            return places.stream().map(p -> {
                Map<String, Object> loc = (Map<String, Object>) p.get("location");
                double pLat = ((Number) loc.get("latitude")).doubleValue();
                double pLng = ((Number) loc.get("longitude")).doubleValue();
                
                String review = null;
                if (p.get("reviews") instanceof List<?> rs && !rs.isEmpty()) {
                    if (rs.get(0) instanceof Map<?, ?> r && r.get("text") instanceof Map<?, ?> t) review = (String) t.get("text");
                }

                List<String> weekdayText = null;
                if (p.get("regularOpeningHours") instanceof Map<?, ?> reg) weekdayText = (List<String>) reg.get("weekdayDescriptions");

                return new PlaceResponse(
                    (String) p.get("id"),
                    (String) ((Map) p.get("displayName")).get("text"),
                    p.get("rating") != null ? ((Number) p.get("rating")).doubleValue() : 0.0,
                    p.get("userRatingCount") != null ? ((Number) p.get("userRatingCount")).intValue() : 0,
                    (String) p.get("formattedAddress"), (String) p.get("googleMapsUri"),
                    (lat != null && lng != null) ? (int)(calculateDistance(lat, lng, pLat, pLng) * 1000) : null,
                    (p.get("photos") instanceof List<?> phs && !phs.isEmpty()) ? (String)((Map)phs.get(0)).get("name") : null,
                    pLat, pLng, formatPriceLevel(p.get("priceLevel")),
                    (p.get("currentOpeningHours") instanceof Map<?, ?> cur) ? (Boolean) cur.get("openNow") : null,
                    (p.get("editorialSummary") instanceof Map<?, ?> sm) ? (String) sm.get("text") : null,
                    (String) p.get("websiteUri"), review, weekdayText
                );
            })
            .filter(dto -> (lat == null || radius == null || dto.distanceMeters() == null) ? true : dto.distanceMeters() <= (radius * 1.2))
            .sorted(getComparator(sortBy, lat != null)).limit(10).toList();
        } catch (Exception e) { e.printStackTrace(); throw e; }
    }

    private String formatPriceLevel(Object o) {
        if (o == null) return null;
        String s = o.toString();
        if (s.contains("INEXPENSIVE")) return "￥";
        if (s.contains("MODERATE")) return "￥￥";
        if (s.contains("VERY_EXPENSIVE")) return "￥￥￥￥";
        if (s.contains("EXPENSIVE")) return "￥￥￥";
        return null;
    }

    private double calculateDistance(double la1, double lo1, double la2, double lo2) {
        double dLat = Math.toRadians(la2 - la1); double dLon = Math.toRadians(lo2 - lo1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(la1)) * Math.cos(Math.toRadians(la2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private Comparator<PlaceResponse> getComparator(String s, boolean h) {
        return switch (s) {
            case "distance" -> h ? Comparator.comparing(PlaceResponse::distanceMeters, Comparator.nullsLast(Comparator.naturalOrder())) : Comparator.comparing(PlaceResponse::rating).reversed();
            case "userRatingsTotal" -> Comparator.comparing(PlaceResponse::userRatingCount).reversed();
            default -> Comparator.comparing(PlaceResponse::rating).reversed();
        };
    }
}