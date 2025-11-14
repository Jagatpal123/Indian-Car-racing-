// sw.js - Service Worker for Offline Capability
const CACHE_NAME = 'car-racing-game-v5'; // Version increased for forced update

// Ensure all assets, including manifest, are listed here
const urlsToCache = [
  './',
  './game.html', // Note: Renamed from index.html to game.html in the main activity logic
  './manifest.json', 
  'road.jpg',
  'player_car.png',
  'obstacle1.png',
  'obstacle2.png', 
  'obstacle3.png',
  'obstacle4.png',
  'coin.gif',
  'Life.png',
  'Dimond.png',
  'Dimond1.png',
  'carsound.mp3',
  'carhit.mp3',
  'collect.wav',
  'opening.wav',
  'Life.mp3',
  'Dimond.mp3',
  'Dimond1.mp3'
];

// Install event - cache all assets
self.addEventListener('install', function(event) {
  console.log('[Service Worker] Install: Caching new assets.');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        // We use 'game.html' here to match the android asset name
        return cache.addAll(urlsToCache.map(url => url === './index.html' ? './game.html' : url));
      })
      .catch(function(error) {
        console.error('[Service Worker] Cache addAll failed:', error);
      })
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', function(event) {
  console.log('[Service Worker] Activate: Cleaning up old caches.');
  event.waitUntil(
    caches.keys().then(function(cacheNames) {
      return Promise.all(
        cacheNames.filter(function(cacheName) {
          // Keep only the current cache
          return cacheName !== CACHE_NAME;
        }).map(function(cacheName) {
          console.log('[Service Worker] Deleting old cache:', cacheName);
          return caches.delete(cacheName);
        })
      );
    })
  );
  // Ensure the new service worker takes control immediately
  return self.clients.claim();
});

// Fetch event - serve from cache if available
self.addEventListener('fetch', function(event) {
  event.respondWith(
    caches.match(event.request)
      .then(function(response) {
        // Cache hit - return response
        if (response) {
          return response;
        }
        // Fallback to network
        return fetch(event.request);
      }
    )
  );
});
