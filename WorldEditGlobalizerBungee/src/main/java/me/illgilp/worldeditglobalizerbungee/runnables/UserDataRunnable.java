package me.illgilp.worldeditglobalizerbungee.runnables;

public class UserDataRunnable implements Runnable {

    private Object userData;

    public UserDataRunnable(Object userData) {
        this.userData = userData;
    }

    @Override
    public void run() {

    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public boolean hasUserData(){
        return userData!=null;
    }
}
