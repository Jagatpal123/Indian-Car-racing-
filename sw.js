// sw.js - Service Worker for Offline Capability
const CACHE_NAME = 'car-racing-game-v4'; // <<< FIX: Increased version to force an update

// IMPORTANT: Add manifest.json to the list
const urlsToCache = [
  './',
  './index.html',
  './manifest.json', // <<< FIX: manifest.json added
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
        return cache.addAll(urlsToCache);
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

// Fetch event - serve from cache if available (Network-first strategy is safer for ads/updates, but cache-first is fine for offline games)
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
