package com.example.shopsearch.controller;

import com.example.shopsearch.model.*;
import com.example.shopsearch.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaceController {
    private final CategoryService categoryService;
    private final PlaceService placeService;
    
    @Value("${google.places.api-key}")
    private String apiKey;

    @GetMapping("/ping")
    public ResponseEntity<Void> ping() { return ResponseEntity.noContent().build(); }

    @GetMapping("/categories")
    public List<Category> getCategories() { return categoryService.getAllCategories(); }

    @GetMapping("/places/search")
    public List<PlaceResponse> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> categoryIds,
            @RequestParam(required = false) String storeName,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "false") boolean independentOnly) {
        
        // categoryIds が null の場合に空のリストを作成
        List<String> cats = (categoryIds != null) ? categoryIds : new ArrayList<>();
        return placeService.search(lat, lng, location, cats, storeName, sortBy, independentOnly);
    }

    @GetMapping("/places/photo")
    public ResponseEntity<Void> getPhoto(@RequestParam String photoName) {
        String url = "https://places.googleapis.com/v1/" + photoName + "/media?maxHeightPx=400&maxWidthPx=400&key=" + apiKey;
        return ResponseEntity.status(302).header("Location", url).build();
    }
}