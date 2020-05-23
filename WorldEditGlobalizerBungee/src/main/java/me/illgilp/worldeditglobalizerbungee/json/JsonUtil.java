package me.illgilp.worldeditglobalizerbungee.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static JsonBuilder createJsonBuilder() {
        return new JsonBuilder();
    }

    public static Gson getGson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting().create();
        return gson;
    }

    public static class JsonBuilder {
        private Map<String, Object> map = new HashMap<>();

        private JsonBuilder() {

        }

        public JsonBuilder put(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return map;
        }

        public String buildAsJsonString() {
            return new Gson().toJson(map);
        }

        public JsonObject buildAsJsonObject(){
            return (JsonObject) new Gson().toJsonTree(map);
        }

        public Object get(String key) {
            return map.get(key);
        }
    }

}

