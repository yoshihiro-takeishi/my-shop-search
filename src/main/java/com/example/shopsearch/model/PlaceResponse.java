package com.example.shopsearch.model;
import java.util.List;

public record PlaceResponse(
    String id, String name, Double rating, Integer userRatingCount,
    String address, String googleMapsUrl, Integer distanceMeters,
    String photoReference, Double lat, Double lng,
    String priceLevel, Boolean openNow, String summary,
    String websiteUri, String reviewSnippet, List<String> businessHours,
    // --- 設備情報 ---
    Boolean reservable,      // 予約可否
    Boolean hasParking,     // 駐車場の有無
    Boolean outdoorSeating, // テラス席
    Boolean goodForGroups   // 大人数・グループ向き
) {}