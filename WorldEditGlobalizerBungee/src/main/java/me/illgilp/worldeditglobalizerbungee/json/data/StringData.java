package me.illgilp.worldeditglobalizerbungee.json.data;

import com.google.gson.JsonObject;

public class StringData extends Data {

    private String string;


    public StringData() {
    }

    public StringData(String string) {
        this.string = string;
    }

    @Override
    public void load(JsonObject jsonObject) {
        this.string = jsonObject.get("string").getAsString();
    }

    @Override
    public void save(JsonObject jsonObject) {
        jsonObject.addProperty("string", this.string);
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public String getTypeName() {
        return "string";
    }
}
