package me.illgilp.worldeditglobalizerbungee.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import net.md_5.bungee.BungeeCord;

public class Cache<K,V> {

    private Map<K,V> cached = new ConcurrentHashMap<>();
    private Map<K,Long> lastUse = new ConcurrentHashMap<>();

    private final long HOLD_TIME;

    public Cache() {
        this(1000 * 60 * 30);
    }

    public Cache(long holdTimeInMillis) {
        this.HOLD_TIME = holdTimeInMillis;
        BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(),
            () -> {
                for (Map.Entry<K, Long> entry : new HashMap<>(lastUse).entrySet()) {
                    if (System.currentTimeMillis() - entry.getValue() >= this.HOLD_TIME) {
                        cached.remove(entry.getKey());
                        lastUse.remove(entry.getKey());
                    }
                }
            }, 1000, 1000, TimeUnit.SECONDS
        );
    }

    public V get(K key) {
        V v = cached.get(key);
        if (v != null) {
            lastUse.put(key, System.currentTimeMillis());
        }

        return v;
    }

    public V getOrDefault(K key, V defaultValue) {
        V v = cached.get(key);
        if (v != null) {
            lastUse.put(key, System.currentTimeMillis());
        } else {
            v = defaultValue;
        }

        return v;
    }

    public V remove(K key) {
        V v = cached.remove(key);
        if (v != null) {
            lastUse.remove(key);
        }
        return v;
    }

    public void put(K key, V value) {
        cached.put(key, value);
        lastUse.put(key, System.currentTimeMillis());
    }

    public void clear() {
        cached.clear();
        lastUse.clear();
    }

    public boolean containsKey(K key) {
        return cached.containsKey(key);
    }


}
