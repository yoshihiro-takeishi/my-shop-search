# --- ステージ1: ビルド ---
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
# 必要なファイルをコピー
COPY . .
# 実行権限を付与してビルド
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# --- ステージ2: 実行 ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# ビルドしたJARファイルだけをコピー
COPY --from=build /app/target/*.jar app.jar
# ポート番号の設定（RenderのPORT環境変数に対応）
ENV PORT=8080
EXPOSE 8080
# アプリ起動
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]