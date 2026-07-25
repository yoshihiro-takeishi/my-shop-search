package com.example.shopsearch.service;

import com.example.shopsearch.model.Category;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final List<Category> categories = List.of(
        new Category("yakiniku", "焼肉", List.of("焼肉", "ホルモン"), "fa-fire"),
        new Category("steak", "ステーキ", List.of("ステーキ", "鉄板焼"), "fa-drumstick-bite"),
        new Category("hamburg", "ハンバーグ", List.of("ハンバーグ"), "fa-circle-dot"),
        new Category("sushi", "寿司・海鮮", List.of("寿司", "海鮮", "魚料理"), "fa-fish"),
        new Category("ramen", "ラーメン", List.of("ラーメン", "つけ麺"), "fa-bowl-food"),
        new Category("chinese", "中華料理", List.of("中華料理", "餃子"), "fa-shrimp"),
        new Category("yoshoku", "オムライス", List.of("オムライス", "洋食"), "fa-egg"),
        new Category("pasta", "パスタ・ピザ", List.of("パスタ", "ピザ", "イタリアン"), "fa-pizza-slice"),
        new Category("shokudo", "定食・食堂", List.of("定食", "食堂", "大衆食堂"), "fa-bowl-rice"),
        new Category("washoku", "和食・鰻", List.of("和食", "鰻", "天ぷら"), "fa-leaf"),
        new Category("soba", "そば・うどん", List.of("そば", "うどん"), "fa-utensils"),
        new Category("tonkatsu", "とんかつ", List.of("とんかつ", "揚げ物"), "fa-bread-slice"),
        new Category("curry", "カレー", List.of("カレー"), "fa-spoon"),
        new Category("bento", "弁当・惣菜", List.of("弁当", "テイクアウト"), "fa-box"),
        new Category("famiresu", "ファミレス", List.of("ファミリーレストラン"), "fa-users"),
        new Category("cafe", "カフェ・甘味", List.of("カフェ", "コーヒー", "スイーツ"), "fa-mug-saucer")
    );

    public List<Category> getAllCategories() { return categories; }
    public Optional<Category> getById(String id) {
        return categories.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}