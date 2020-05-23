package me.illgilp.worldeditglobalizerbungee.storage.table;

import com.j256.ormlite.support.ConnectionSource;
import java.util.List;
import java.util.Map;
import me.illgilp.worldeditglobalizerbungee.storage.WhereBuilder;

public interface Table<T, ID> {

    boolean init(ConnectionSource source);

    boolean isInitialized();

    boolean add(T instance);

    boolean exists(T instance);

    boolean update(T instance);

    boolean createOrUpdate(T instance);

    boolean remove(T instance);

    T getExact(T instance);

    List<T> get(T instance);

    List<T> getAll();

    long sizeOf(Map<String, Object> map);
    long sizeOf(WhereBuilder<T, ID> builder);

    long size();

}
