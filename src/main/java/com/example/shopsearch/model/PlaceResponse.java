package com.example.shopsearch.model;
import java.util.List;

public record PlaceResponse(
    String id,
    String name,
    Double rating,
    Integer userRatingCount,
    String address,
    String googleMapsUrl,
    Integer distanceMeters,
    String photoReference,
    Double lat,
    Double lng,
    String priceLevel,
    Boolean openNow,
    String summary,
    String websiteUri,
    String reviewSnippet,
    List<String> businessHours
) {}