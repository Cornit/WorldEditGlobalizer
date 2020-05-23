/*
 * This file is part of VanillaSkyblock.
 * Copyright (C) 2020 MyFTB <http://myftb.de>
 * All rights reserved
 */

package me.illgilp.worldeditglobalizerbukkit.util;

import java.util.Map;

public class MapBuilder<K, V> {

    private Map<K, V> map;

    private MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    public static <T, S> MapBuilder<T, S> of(Map<T, S> map) {
        return new MapBuilder<>(map);
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> create() {
        return map;
    }
}
