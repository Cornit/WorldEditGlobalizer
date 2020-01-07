package me.illgilp.worldeditglobalizerbungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Callback {

    private static Map<UUID, Callback> callbacks = new HashMap<>();

    private long timeOut = 1000;
    private UUID identifier;

    private Object userData;
    private long startTime;

    private ScheduledTask task;

    private Callback instance;
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

    public abstract void onTimeOut(Callback callback);

    public abstract void onCallback(Callback callback, Object response);

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public boolean hasUserData() {
        return userData != null;
    }

    public void waitFor() {
        synchronized (this) {
            try {
                this.wait(timeOut);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!called) onTimeOut(this);
        }
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
