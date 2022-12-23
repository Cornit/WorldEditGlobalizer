package me.illgilp.worldeditglobalizer.common.util;

public interface CatchingFunction<T, R, E extends Throwable> {

    R accept(T t) throws E;

}
