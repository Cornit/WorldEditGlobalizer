package me.illgilp.worldeditglobalizerbungee.json.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonData extends Data{

    private JsonElement jsonElement;


    public JsonData() {
    }

    public JsonData(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }

    @Override
    public void load(JsonObject jsonObject) {
        jsonElement = jsonObject.get("json");
    }

    @Override
    public void save(JsonObject jsonObject) {
        jsonObject.add("json", jsonElement);
    }


    public JsonElement getJsonElement() {
        return jsonElement;
    }

    public void setJsonElement(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }

    @Override
    public String getTypeName() {
        return "json";
    }
}
