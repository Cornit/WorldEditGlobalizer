package me.illgilp.worldeditglobalizerbungee.json.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public abstract class Data {


    public abstract void load(JsonObject jsonObject);
    public abstract void save(JsonObject jsonObject);


    public abstract String getTypeName();


    public final JsonObject write() {
        JsonObject jsonObject = new JsonObject();

        String type = getTypeName();

        if (type == null) {
            type = this.getClass().getSimpleName();
        }

        jsonObject.addProperty("type", type);
        jsonObject.addProperty("class", this.getClass().getName());

        JsonObject data = new JsonObject();
        save(data);
        jsonObject.add("data", data);

        return jsonObject;
    }

    public final boolean read(JsonObject jsonObject) {
        if (!jsonObject.has("type")) {
            throw new JsonParseException("missing property: type");
        }

        String type = jsonObject.get("type").getAsString();
        if (type == null) {
            throw new JsonParseException("invalid property: type");
        }

        String tt = getTypeName();

        if (tt == null) {
            tt = this.getClass().getSimpleName();
        }

        if (!type.equals(tt)) {
            throw new JsonParseException(String.format("cannot convert data type '%s' to '%s'", type, tt));
        }

        if (!jsonObject.has("data")) {
            throw new JsonParseException("missing property: data");
        }

        if (!jsonObject.get("data").isJsonObject()) {
            throw new JsonParseException("invalid property: data");
        }
        JsonObject data = jsonObject.getAsJsonObject("data");
        load(data);
        return true;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        save(jsonObject);
        return this.getClass().getSimpleName() + new Gson().toJson(jsonObject);
    }
}
