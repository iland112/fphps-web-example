/**
 * FastPass PWA Service Worker
 * Handles caching strategies for offline support
 */

const CACHE_NAME = 'fastpass-pwa-v3';
const STATIC_CACHE = 'fastpass-static-v3';
const DYNAMIC_CACHE = 'fastpass-dynamic-v3';

// Static assets to cache on install
const STATIC_ASSETS = [
  '/',
  '/fphps',
  '/css/main.css',
  '/js/passport-tabs.js',
  '/js/pa-verification.js',
  '/image/FASTpass.bmp',
  '/image/fastpass-p1.png',
  '/image/favicon.png',
  '/manifest.json',
  // External CDN resources
  'https://cdn.jsdelivr.net/npm/preline@3.0.1/dist/preline.min.js'
];

// Network-first routes (always try network, fallback to cache)
const NETWORK_FIRST_ROUTES = [
  '/fphps/passport/',
  '/fphps/idcard/',
  '/fphps/barcode/',
  '/fphps/device',
  '/fphps/scan-page',
  '/passport/',  // PA verification API endpoints
  '/idcard/',
  '/barcode/',
  '/fastpass' // WebSocket endpoint
];

// Cache-first routes (use cache, update in background)
const CACHE_FIRST_ROUTES = [
  '/css/',
  '/js/',
  '/image/',
  '/webjars/'
];

/**
 * Install event - cache static assets
 */
self.addEventListener('install', (event) => {
  console.log('[SW] Installing Service Worker...');

  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('[SW] Caching static assets');
        return cache.addAll(STATIC_ASSETS.filter(url => !url.startsWith('http')));
      })
      .then(() => {
        console.log('[SW] Static assets cached successfully');
        return self.skipWaiting();
      })
      .catch((err) => {
        console.error('[SW] Failed to cache static assets:', err);
      })
  );
});

/**
 * Activate event - clean up old caches
 */
self.addEventListener('activate', (event) => {
  console.log('[SW] Activating Service Worker...');

  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames
            .filter((cacheName) => {
              return cacheName !== STATIC_CACHE &&
                     cacheName !== DYNAMIC_CACHE &&
                     cacheName.startsWith('fastpass-');
            })
            .map((cacheName) => {
              console.log('[SW] Deleting old cache:', cacheName);
              return caches.delete(cacheName);
            })
        );
      })
      .then(() => {
        console.log('[SW] Service Worker activated');
        return self.clients.claim();
      })
  );
});

/**
 * Fetch event - handle caching strategies
 */
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }

  // Skip WebSocket connections
  if (url.protocol === 'ws:' || url.protocol === 'wss:') {
    return;
  }

  // Skip chrome-extension and other non-http(s) requests
  if (!url.protocol.startsWith('http')) {
    return;
  }

  // Determine caching strategy based on route
  if (isNetworkFirstRoute(url.pathname)) {
    event.respondWith(networkFirst(request));
  } else if (isCacheFirstRoute(url.pathname)) {
    event.respondWith(cacheFirst(request));
  } else {
    event.respondWith(staleWhileRevalidate(request));
  }
});

/**
 * Check if route should use network-first strategy
 */
function isNetworkFirstRoute(pathname) {
  return NETWORK_FIRST_ROUTES.some(route => pathname.startsWith(route));
}

/**
 * Check if route should use cache-first strategy
 */
function isCacheFirstRoute(pathname) {
  return CACHE_FIRST_ROUTES.some(route => pathname.startsWith(route));
}

/**
 * Network First Strategy
 * Try network, fallback to cache if offline
 */
async function networkFirst(request) {
  try {
    const networkResponse = await fetch(request);

    // Cache successful responses
    if (networkResponse.ok) {
      const cache = await caches.open(DYNAMIC_CACHE);
      cache.put(request, networkResponse.clone());
    }

    return networkResponse;
  } catch (error) {
    console.log('[SW] Network failed, trying cache:', request.url);
    const cachedResponse = await caches.match(request);

    if (cachedResponse) {
      return cachedResponse;
    }

    // Return offline page for navigation requests
    if (request.mode === 'navigate') {
      return caches.match('/offline.html');
    }

    throw error;
  }
}

/**
 * Cache First Strategy
 * Use cache if available, otherwise fetch from network
 */
async function cacheFirst(request) {
  const cachedResponse = await caches.match(request);

  if (cachedResponse) {
    return cachedResponse;
  }

  try {
    const networkResponse = await fetch(request);

    if (networkResponse.ok) {
      const cache = await caches.open(STATIC_CACHE);
      cache.put(request, networkResponse.clone());
    }

    return networkResponse;
  } catch (error) {
    console.error('[SW] Cache first failed:', request.url);
    throw error;
  }
}

/**
 * Stale While Revalidate Strategy
 * Return cache immediately, update cache in background
 */
async function staleWhileRevalidate(request) {
  const cachedResponse = await caches.match(request);

  const fetchPromise = fetch(request)
    .then(async (networkResponse) => {
      if (networkResponse.ok) {
        try {
          const responseToCache = networkResponse.clone();
          const cache = await caches.open(DYNAMIC_CACHE);
          await cache.put(request, responseToCache);
        } catch (err) {
          console.log('[SW] Failed to cache response:', err);
        }
      }
      return networkResponse;
    })
    .catch((error) => {
      console.log('[SW] Network request failed:', error);
      return cachedResponse;
    });

  return cachedResponse || fetchPromise;
}

/**
 * Handle push notifications (future use)
 */
self.addEventListener('push', (event) => {
  console.log('[SW] Push notification received');

  const options = {
    body: event.data ? event.data.text() : 'New notification',
    icon: '/image/icons/icon-192x192.png',
    badge: '/image/icons/icon-72x72.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    },
    actions: [
      { action: 'explore', title: 'Open App' },
      { action: 'close', title: 'Close' }
    ]
  };

  event.waitUntil(
    self.registration.showNotification('FastPass', options)
  );
});

/**
 * Handle notification click
 */
self.addEventListener('notificationclick', (event) => {
  console.log('[SW] Notification clicked');
  event.notification.close();

  if (event.action === 'explore') {
    event.waitUntil(
      clients.openWindow('/fphps')
    );
  }
});

/**
 * Handle background sync (future use)
 */
self.addEventListener('sync', (event) => {
  console.log('[SW] Background sync:', event.tag);

  if (event.tag === 'sync-passport-data') {
    event.waitUntil(syncPassportData());
  }
});

async function syncPassportData() {
  // Future: Sync offline passport readings when back online
  console.log('[SW] Syncing passport data...');
}

console.log('[SW] Service Worker loaded');
