package com.example.shopsearch.model;

/**
 * エラーレスポンス
 * @param message エラーメッセージ
 * @param status HTTPステータスコード
 */
public record ErrorResponse(
    String message, 
    int status
) {}