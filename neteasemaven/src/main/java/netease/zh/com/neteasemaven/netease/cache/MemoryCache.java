package netease.zh.com.neteasemaven.netease.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MemoryCache<K, V> implements ICache<K, V> {
    private LruCache<K, V> cache;
    private ISizer<K, V> sizer;

    public MemoryCache(int maxSize) {
        this(maxSize, new ConstantSizer<K, V>());
    }

    public MemoryCache(int maxSize, ISizer<K, V> sizer) {
        this.sizer = sizer;
        cache = new LruCache<K, V>(maxSize) {
            protected int sizeOf(K key, V value) {
                return MemoryCache.this.sizer.sizeOf(key, value);
            }
        };
    }

    public void clear() {
        cache.evictAll();
    }

    @Override
    public void set(K k, V v) {
        cache.put(k, v);
    }

    @Override
    public V get(K k) {
        return cache.get(k);
    }

    public int getMaxSize() {
        return cache.maxSize();
    }

    public static interface ISizer<K, V> {
        int sizeOf(K key, V value);
    }

    public static class ConstantSizer<K, V> implements ISizer<K, V> {
        @Override
        public int sizeOf(K key, V value) {
            return 1;
        }
    }

    public static class BitmapSizer<K> implements ISizer<K, Bitmap> {
        @Override
        public int sizeOf(K key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }

    public static class ByteArraySizer<K> implements ISizer<K, byte[]> {
        @Override
        public int sizeOf(K key, byte[] value) {
            return value.length;
        }
    }
}
