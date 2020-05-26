package me.illgilp.mavenrepolib.listener;

import java.io.File;

public interface ProgressListener {

    void onDone(File file);

    void onProgressChange(File file, long curr, long max);

    /**
     * @return true for retry
     */
    boolean onFailed(File file);

    void onStart(File file);
}
