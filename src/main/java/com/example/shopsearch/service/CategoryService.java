package com.example.shopsearch.service;

import com.example.shopsearch.model.Category;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final List<Category> categories = List.of(
        // --- 🥩 肉・ガッツリ系 ---
        new Category("yakiniku", "焼肉・ホルモン", List.of("焼肉", "ホルモン"), "fa-fire"),
        new Category("steak", "ステーキ・鉄板焼", List.of("ステーキ", "鉄板焼"), "fa-drumstick-bite"),
        new Category("hamburg", "ハンバーグ", List.of("ハンバーグ"), "fa-circle-dot"),
        new Category("tonkatsu", "とんかつ・揚げ物", List.of("とんかつ", "かつ丼", "フライ"), "fa-bread-slice"),
        new Category("yakitori", "焼き鳥・串焼き", List.of("焼き鳥", "串焼き"), "fa-vihara"),
        new Category("burger", "ハンバーガー", List.of("ハンバーガー", "グルメバーガー"), "fa-burger"), // +α

        // --- 🍜 麺類系 ---
        new Category("ramen_kotteri", "こってりラーメン", List.of("家系ラーメン", "豚骨ラーメン", "二郎系"), "fa-bowl-food"),
        new Category("ramen_assari", "あっさりラーメン", List.of("醤油ラーメン", "塩ラーメン", "中華そば"), "fa-bowl-rice"),
        new Category("tsukemen", "つけ麺・油そば", List.of("つけ麺", "油そば", "まぜそば"), "fa-layer-group"),
        new Category("sanuki_udon", "讃岐うどん・セルフ", List.of("讃岐うどん", "セルフうどん"), "fa-bowl-food"),
        new Category("nikujiru_udon", "肉汁うどん・武蔵野", List.of("肉汁うどん", "武蔵野うどん"), "fa-whiskey-glass"),
        new Category("soba", "蕎麦（十割・手打ち）", List.of("蕎麦", "十割そば", "手打ちそば"), "fa-wheat-awn"),
        new Category("tachigui", "立ち食い・駅そば", List.of("立ち食いそば", "立ち食いうどん"), "fa-bolt"),

        // --- 🐟 魚介・和食系 ---
        new Category("sushi", "寿司", List.of("寿司", "回転寿司"), "fa-fish"),
        new Category("kaisen", "海鮮丼・魚料理", List.of("海鮮丼", "刺身", "魚料理"), "fa-fish-fins"),
        new Category("unagi", "鰻（うなぎ）", List.of("うなぎ", "鰻重"), "fa-water"),
        new Category("tempura", "天ぷら・天丼", List.of("天ぷら", "天丼"), "fa-mountain"),
        new Category("shokudo", "定食・大衆食堂", List.of("定食", "食堂", "大衆食堂"), "fa-bowl-rice"),

        // --- 🍕 洋食・エスニック系 ---
        new Category("pasta", "パスタ・イタリアン", List.of("パスタ", "生パスタ", "イタリアン"), "fa-stroopwafel"),
        new Category("pizza", "ピザ・窯焼き", List.of("ピザ", "ピッツァ"), "fa-pizza-slice"),
        new Category("omurice", "オムライス・洋食", List.of("オムライス", "洋食屋"), "fa-egg"),
        new Category("bistro", "ビストロ・ワイン", List.of("ビストロ", "バル", "ワイン"), "fa-wine-glass"), // +α
        new Category("curry_japanese", "欧風・金沢カレー", List.of("欧風カレー", "カツカレー"), "fa-spoon"),
        new Category("curry_spice", "スパイス・インドカレー", List.of("スパイスカレー", "インドカレー", "ナン"), "fa-pepper-hot"),
        new Category("ethnic", "タイ・ベトナム料理", List.of("タイ料理", "ベトナム料理", "フォー"), "fa-leaf"), // +α

        // --- 🇨🇳 アジア・中華系 ---
        new Category("chinese_machichuka", "町中華・ギョーザ", List.of("町中華", "餃子", "チャーハン"), "fa-pepper-hot"),
        new Category("spicy_korean", "韓国料理・スンドゥブ", List.of("韓国料理", "スンドゥブ", "サムギョプサル"), "fa-fire-flame-curved"),

        // --- ☕ 軽食・カフェ・その他 ---
        new Category("cafe_coffee", "カフェ・喫茶店", List.of("カフェ", "喫茶店", "コーヒー"), "fa-mug-saucer"),
        new Category("sweets", "スイーツ・甘味", List.of("スイーツ", "パフェ", "和菓子", "ケーキ"), "fa-cake-candles"),
        new Category("bakery", "パン・サンドイッチ", List.of("パン", "ベーカリー", "サンドイッチ"), "fa-cookie"),
        new Category("healthy", "サラダ・健康食", List.of("サラダ専門店", "オーガニック", "ヘルシー"), "fa-seedling"), // +α
        new Category("bento", "弁当・テイクアウト", List.of("弁当", "テイクアウト"), "fa-box"),
        new Category("gyudon", "牛丼・丼もの", List.of("牛丼", "親子丼", "天丼"), "fa-bowl-rice"), // +α

        // 粉もの：お好み焼きやたこ焼き
        new Category("konamono", "お好み焼き・粉もの", List.of("お好み焼き", "もんじゃ焼き", "たこ焼き"), "fa-stroopwafel"),

        // 鍋：冬場の強力な味方
        new Category("shabu_nabe", "しゃぶしゃぶ・鍋", List.of("しゃぶしゃぶ", "すき焼き", "鍋料理"), "fa-bowl-hot"),

        // 軽食：もっとカジュアルに済ませたい時
        new Category("fastfood", "軽食・テイクアウト", List.of("ケバブ", "ホットドッグ", "たこ焼き", "おにぎり専門店"), "fa-cookie-bite"),
        new Category("famiresu", "ファミレス", List.of("ファミリーレストラン"), "fa-users")
    );

    public List<Category> getAllCategories() { return categories; }
    public Optional<Category> getById(String id) {
        return categories.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}