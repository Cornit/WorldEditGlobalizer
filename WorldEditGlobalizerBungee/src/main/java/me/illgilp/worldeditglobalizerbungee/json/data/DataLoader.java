package me.illgilp.worldeditglobalizerbungee.json.data;

import com.google.gson.JsonObject;

public class DataLoader {

    public static <T extends Data> T loadData(JsonObject jsonObject, Class<T> type) {
        try {
            T t = type.newInstance();
            if (t.read(jsonObject)) {
                return t;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
