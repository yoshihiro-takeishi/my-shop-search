// index.htmlのAPP_VERSIONと合わせて更新してください
const VERSION = "v1.3.0"; 
const CACHE_NAME = `shop-nav-cache-${VERSION}`;

const ASSETS = [
  '/index.html',
  '/api/categories',
  'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css'
];

// インストール時に新しいキャッシュを作成
self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS))
  );
});

// 古いバージョンのキャッシュを自動削除する処理（これを追加するとクリーンに保てます）
self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) return caches.delete(key);
        })
      );
    })
  );
});

self.addEventListener('fetch', (e) => {
  e.respondWith(fetch(e.request).catch(() => caches.match(e.request)));
});