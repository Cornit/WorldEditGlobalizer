package me.illgilp.worldeditglobalizerbungee.storage;

import com.j256.ormlite.stmt.Where;

public interface WhereBuilder<T,ID> {

    void build(Where<T, ID> where) throws Exception;
}
