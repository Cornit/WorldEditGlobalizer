package me.illgilp.worldeditglobalizerbungee.json.data;

import com.google.gson.JsonObject;

public class LongData extends Data {

    private long longs;


    public LongData() {
    }

    public LongData(long longs) {
        this.longs = longs;
    }

    @Override
    public void load(JsonObject jsonObject) {
        this.longs = jsonObject.get("long").getAsLong();
    }

    @Override
    public void save(JsonObject jsonObject) {
        jsonObject.addProperty("long", this.longs);
    }

    public long getLong() {
        return longs;
    }

    public void setLong(long longs) {
        this.longs = longs;
    }

    @Override
    public String getTypeName() {
        return "long";
    }
}
