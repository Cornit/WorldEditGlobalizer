package me.illgilp.worldeditglobalizersponge.task;


import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;

public abstract class AsyncTask{

    private static Map<UUID,AsyncTask> runningTasks = new HashMap<>();
    private static Map<UUID,Thread> taskFinishThreads = new HashMap<>();

    private UUID uuid = UUID.randomUUID();
    private Thread thread;

    public AsyncTask(UUID uuid) {
        this.uuid = uuid;
        this.thread = new Thread(){
            @Override
            public void run() {
                try {
                    AsyncTask.this.run();
                }catch (Exception e){
                    System.err.println("Exception thrown in Task with id '"+AsyncTask.this.uuid.toString()+"': \n"+ ExceptionUtils.getStackTrace(e));
                    if(AsyncTask.runningTasks.containsKey(AsyncTask.this.uuid)){
                        AsyncTask.this.interrupt();
                    }
                }
            }
        };
        thread.setName("AsyncTask: "+uuid.toString());
    }

    public AsyncTask() {
        this(UUID.randomUUID());
    }

    public abstract void run();

    public void start() {
        if(runningTasks.containsKey(this.uuid)){
            try {
                throw new AsyncTaskException(this,"Task with id '"+uuid.toString()+"' already running");
            } catch (AsyncTaskException e) {
                e.printStackTrace();
                return;
            }
        }
        runningTasks.put(this.uuid,this);
        this.thread.start();
        Thread finishThread = new Thread(){
            @Override
            public void run() {
                try {
                    AsyncTask.this.thread.join();
                } catch (InterruptedException e) {

                }
                AsyncTask.runningTasks.remove(AsyncTask.this.uuid);
                AsyncTask.taskFinishThreads.remove(AsyncTask.this.uuid);
            }
        };
        taskFinishThreads.put(this.uuid,finishThread);
        finishThread.start();
    }

    public void interrupt() {
        if(!runningTasks.containsKey(this.uuid)){
            try {
                throw new AsyncTaskException(this,"Task with id '"+uuid.toString()+"' is not running");
            } catch (AsyncTaskException e) {
                e.printStackTrace();
                return;
            }
        }
        runningTasks.remove(this.uuid);
        if(taskFinishThreads.containsKey(this.uuid)){
            taskFinishThreads.get(this.uuid).interrupt();
        }
        AsyncTask.this.thread.interrupt();
    }

    public boolean isRunning(){
        return runningTasks.containsKey(this.uuid);
    }

    private class AsyncTaskException extends Throwable {

        private AsyncTask task;

        public AsyncTaskException(AsyncTask task, String message) {
            super(message);
            this.task = task;
        }

        public AsyncTask getTask() {
            return task;
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public static List<AsyncTask> getRunningTasks() {
        List<AsyncTask> tasks = new ArrayList<>();
        tasks.addAll(runningTasks.values());
        return tasks;
    }

    public static AsyncTask getRunningTask(UUID uuid) {
        return runningTasks.get(uuid);
    }
}
