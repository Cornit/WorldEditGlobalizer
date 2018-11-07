package me.illgilp.worldeditglobalizersponge.task;


import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class QueuedAsyncTask<T> extends AsyncTask {

    private T userData;
    private static LinkedBlockingDeque<QueuedAsyncTask> queue = new LinkedBlockingDeque<>();
    private static QueuedAsyncTask currentTask;
    private static AsyncTask queueTask;

    static {
        queueTask = new AsyncTask() {
            @Override
            public void run() {
                while (isRunning()){
                    if(currentTask != null){
                        if(currentTask.isRunning()){
                            continue;
                        }
                    }
                    try {
                        currentTask = queue.take();
                        if(currentTask != null){
                            ((AsyncTask)currentTask).start();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        queueTask.start();
    }

    public QueuedAsyncTask(UUID uuid, T userData) {
        super(uuid);
        this.userData = userData;
    }

    public QueuedAsyncTask(T userData) {
        this.userData = userData;
    }

    public QueuedAsyncTask(UUID uuid) {
        super(uuid);
    }

    public QueuedAsyncTask() {
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


    public void addToQueue() {
        queue.add(this);
    }

    @Override
    public void interrupt() {
        if(queue.contains(this)){
            queue.remove(this);
        }else {
            super.interrupt();
        }
    }


}
