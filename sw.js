// sw.js - Service Worker for Offline Capability
const CACHE_NAME = 'car-racing-game-v1';
const urlsToCache = [
  './',
  './index.html',
  // Add all your asset URLs here that you want to cache
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
  console.log('Service Worker installing.');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
      .catch(function(error) {
        console.log('Cache addAll failed:', error);
      })
  );
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
        return fetch(event.request);
      }
    )
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', function(event) {
  event.waitUntil(
    caches.keys().then(function(cacheNames) {
      return Promise.all(
        cacheNames.map(function(cacheName) {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});
