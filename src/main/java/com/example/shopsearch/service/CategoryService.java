package com.example.shopsearch.service;

import com.example.shopsearch.model.Category;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final List<Category> categories = List.of(
        // --- 1. がっつり肉・中華・韓国 ---
        new Category("yakiniku", "焼肉・ホルモン", List.of("焼肉", "ホルモン"), "fa-fire", "肉・中華・韓国"),
        new Category("steak", "ステーキ・鉄板焼", List.of("ステーキ"), "fa-drumstick-bite", "肉・中華・韓国"),
        new Category("hamburg", "ハンバーグ", List.of("ハンバーグ"), "fa-circle-dot", "肉・中華・韓国"),
        new Category("tonkatsu", "とんかつ・揚げ物", List.of("とんかつ"), "fa-bread-slice", "肉・中華・韓国"),
        new Category("yakitori", "焼き鳥・串焼き", List.of("焼き鳥"), "fa-vihara", "肉・中華・韓国"),
        new Category("burger", "ハンバーガー", List.of("ハンバーガー"), "fa-burger", "肉・中華・韓国"),
        new Category("chinese_machichuka", "町中華・餃子", List.of("町中華", "餃子"), "fa-pepper-hot", "肉・中華・韓国"),
        new Category("spicy_korean", "韓国料理", List.of("韓国料理"), "fa-fire-flame-curved", "肉・中華・韓国"),

        // --- 2. 魚・和食・麺類 ---
        new Category("sushi", "寿司", List.of("寿司"), "fa-fish", "和食・麺類"),
        new Category("kaisen", "海鮮丼・魚料理", List.of("海鮮丼"), "fa-fish-fins", "和食・麺類"),
        new Category("unagi", "鰻（うなぎ）", List.of("うなぎ"), "fa-water", "和食・麺類"),
        new Category("tempura", "天ぷら・天丼", List.of("天ぷら"), "fa-mountain", "和食・麺類"),
        new Category("shokudo", "定食・大衆食堂", List.of("定食"), "fa-bowl-rice", "和食・麺類"),
        new Category("ramen_kotteri", "こってりラーメン", List.of("家系", "二郎系"), "fa-bowl-food", "和食・麺類"),
        new Category("ramen_assari", "あっさりラーメン", List.of("中華そば"), "fa-bowl-rice", "和食・麺類"),
        new Category("tsukemen", "つけ麺・油そば", List.of("つけ麺"), "fa-layer-group", "和食・麺類"),
        new Category("sanuki_udon", "讃岐うどん", List.of("讃岐うどん"), "fa-bowl-food", "和食・麺類"),
        new Category("nikujiru_udon", "肉汁うどん", List.of("肉汁うどん"), "fa-whiskey-glass", "和食・麺類"),
        new Category("soba", "蕎麦", List.of("蕎麦"), "fa-wheat-awn", "和食・麺類"),
        new Category("tachigui", "立ち食いそば", List.of("立ち食いそば"), "fa-bolt", "和食・麺類"),

        // --- 3. 洋食・イタリアン・カレー ---
        new Category("pasta", "イタリアン", List.of("イタリアン", "パスタ"), "fa-stroopwafel", "洋食・カレー"),
        new Category("pizza", "ピザ", List.of("ピザ"), "fa-pizza-slice", "洋食・カレー"),
        new Category("omurice", "オムライス・洋食", List.of("オムライス"), "fa-egg", "洋食・カレー"),
        new Category("bistro", "ビストロ・ワイン", List.of("ビストロ"), "fa-wine-glass", "洋食・カレー"),
        new Category("curry_japanese", "欧風カレー", List.of("欧風カレー"), "fa-spoon", "洋食・カレー"),
        new Category("curry_spice", "スパイスカレー", List.of("インドカレー"), "fa-pepper-hot", "洋食・カレー"),

        // --- 4. カフェ・パン・軽食 ---
        new Category("cafe_coffee", "カフェ・喫茶店", List.of("カフェ"), "fa-mug-saucer", "カフェ・軽食"),
        new Category("sweets", "スイーツ・甘味", List.of("パフェ", "ケーキ"), "fa-cake-candles", "カフェ・軽食"),
        new Category("bakery", "パン・サンドイッチ", List.of("パン"), "fa-cookie", "カフェ・軽食"),
        new Category("healthy", "サラダ・健康食", List.of("サラダ"), "fa-seedling", "カフェ・軽食"),
        new Category("ethnic", "タイ・ベトナム料理", List.of("タイ料理"), "fa-leaf", "カフェ・軽食"),

        // --- 5. その他・利便性 ---
        new Category("gyudon", "牛丼・丼もの", List.of("牛丼"), "fa-bowl-rice", "その他"),
        new Category("bento", "弁当・テイクアウト", List.of("弁当"), "fa-box", "その他"),
        new Category("famiresu", "ファミレス", List.of("ファミリーレストラン"), "fa-users", "その他")
    );

    public List<Category> getAllCategories() { return categories; }
    public Optional<Category> getById(String id) { return categories.stream().filter(c -> c.id().equals(id)).findFirst(); }
}