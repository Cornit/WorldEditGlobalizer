package me.illgilp.worldeditglobalizerbungee.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.illgilp.worldeditglobalizerbungee.WorldEditGlobalizerBungee;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public abstract class Callback<U,R> {

    private static Map<UUID, Callback> callbacks = new HashMap<>();

    private long timeOut = 1000;
    private UUID identifier;

    private U userData;
    private long startTime;

    private ScheduledTask task;

    private Callback<U,R> instance;
    private boolean called = false;


    public Callback(long timeOut, UUID identifier) {
        this.timeOut = timeOut;
        this.identifier = identifier;
        callbacks.put(identifier, this);
        instance = this;
    }

    public static Callback callback(UUID identifier, Object response) {
        if (callbacks.containsKey(identifier)) {
            Callback callback = callbacks.get(identifier);
            callback.onCallback(callback, response);
            callbacks.remove(identifier);
            callback.called = true;
            synchronized (callback) {
                callback.notify();
            }
            return callback;
        } else {
            return null;
        }
    }

    public abstract void onTimeOut(Callback<U,R> callback);

    public abstract void onCallback(Callback<U,R> callback, R response);

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public U getUserData() {
        return userData;
    }

    public void setUserData(U userData) {
        this.userData = userData;
    }

    public boolean hasUserData() {
        return userData != null;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        task = BungeeCord.getInstance().getScheduler().schedule(WorldEditGlobalizerBungee.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime >= timeOut) {
                    if (!called) {
                        onTimeOut(instance);
                        task.cancel();
                        callbacks.remove(identifier);
                    } else {
                        task.cancel();
                        callbacks.remove(identifier);
                    }
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }
}
