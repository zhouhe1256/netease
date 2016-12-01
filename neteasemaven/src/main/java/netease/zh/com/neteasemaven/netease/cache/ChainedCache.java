package netease.zh.com.neteasemaven.netease.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChainedCache<K, V> implements ICache<K, V> {
    private final List<ICache<K, V>> chain = new ArrayList<ICache<K, V>>();

    public ChainedCache(ICache<K, V>... caches) {
        chain.addAll(Arrays.asList(caches));
    }

    @Override
    public void set(K k, V v) {
        for (ICache<K, V> cache : chain) {
            cache.set(k, v);
        }
    }

    @Override
    public V get(K k) {
        int size = chain.size();
        for (int i = 0; i < size; i++) {
            ICache<K, V> cache = chain.get(i);
            V v = cache.get(k);
            if (v != null) {
                fillPreviousCaches(i, k, v);
                return v;
            }
        }
        return null;
    }

    private void fillPreviousCaches(int current, K k, V v) {
        for (int i = current - 1; i >= 0; i--) {
            ICache<K, V> cache = chain.get(i);
            cache.set(k, v);
        }
    }

    @Override
    public void clear() {
        for (ICache<K, V> cache : chain) {
            cache.clear();
        }
    }

    public static <K, V> ChainedCache<K, V> create(int memorySize, MemoryCache.ISizer<K, V> sizer,
                                                   String type, DiskCache.ISerialization<K, V> serialization) {
        return new ChainedCache<K, V>(
                new MemoryCache<K, V>(memorySize, sizer),
                new DiskCache<K, V>(type, serialization)
        );
    }
}
