package me.illgilp.worldeditglobalizer.common.util;

public interface CatchingConsumer<T, E extends Throwable> {

    void accept(T t) throws E;

}
