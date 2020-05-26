package me.illgilp.mavenrepolib.repo;

import me.illgilp.mavenrepolib.lib.Library;
import me.illgilp.mavenrepolib.listener.ProgressListener;

public interface Repository {

    public abstract Library getLibrary(String group, String artifact, String version, ProgressListener progressListener);

    public abstract boolean exists(String group, String artifact, String version);




}
