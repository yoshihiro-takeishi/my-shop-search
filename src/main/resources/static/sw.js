const CACHE_NAME = 'lumiere-v1';
const ASSETS = [
  '/index.html',
  '/api/categories',
  'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css'
];

// インストール時に基本ファイルを保存
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS))
  );
});

// ネットワーク優先、ダメならキャッシュから取得
self.addEventListener('fetch', (event) => {
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});