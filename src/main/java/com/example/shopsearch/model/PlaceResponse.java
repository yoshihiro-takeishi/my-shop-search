package com.example.shopsearch.model;

/**
 * 店舗検索結果レスポンス
 */
public record PlaceResponse(
    String name,
    Double rating,
    Integer userRatingCount,
    String address,
    String googleMapsUrl,
    Integer distanceMeters,
    String photoReference, // 写真取得用のID
    Double lat,            // ルート案内用（緯度）
    Double lng             // ルート案内用（経度）
) {}