package com.example.shopsearch.service;

import com.example.shopsearch.model.Category;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final List<Category> categories = List.of(
        new Category("meat", "肉・ガッツリ", List.of("焼肉", "ステーキ", "ハンバーグ", "とんかつ"), 
            "https://images.unsplash.com/photo-1544025162-d76694265947?w=400&q=80"),
        new Category("vegetable", "野菜・ヘルシー", List.of("サラダ", "野菜料理", "オーガニック", "定食"), 
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=80"),
        new Category("noodle", "麺類", List.of("ラーメン", "うどん", "そば", "パスタ"), 
            "https://images.unsplash.com/photo-1552611052-33e04de081de?w=400&q=80"),
        new Category("cafe", "カフェ・スイーツ", List.of("カフェ", "喫茶店", "スイーツ"), 
            "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&q=80"),
        new Category("spicy", "辛い・韓国料理", List.of("韓国料理", "四川料理", "カレー"), 
            "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400&q=80")
    );

    public List<Category> getAllCategories() { return categories; }
    public Optional<Category> getById(String id) {
        return categories.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}