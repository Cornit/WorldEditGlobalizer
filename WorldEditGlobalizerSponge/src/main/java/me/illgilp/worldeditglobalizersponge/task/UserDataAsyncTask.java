package me.illgilp.worldeditglobalizersponge.task;

import java.util.UUID;

public abstract class UserDataAsyncTask<T> extends AsyncTask {

    private T userData;

    public UserDataAsyncTask(UUID uuid, T userData) {
        super(uuid);
        this.userData = userData;
    }

    public UserDataAsyncTask(T userData) {
        this.userData = userData;
    }

    public UserDataAsyncTask(UUID uuid) {
        super(uuid);
    }

    public UserDataAsyncTask() {
    }

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }

    public boolean hasUserData(){
        return userData != null;
    }

}
