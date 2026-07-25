package com.example.shopsearch.model;
import java.util.List;

public record Category(
    String id, 
    String label, 
    List<String> keywords,
    String icon // FontAwesomeのクラス名
) {}