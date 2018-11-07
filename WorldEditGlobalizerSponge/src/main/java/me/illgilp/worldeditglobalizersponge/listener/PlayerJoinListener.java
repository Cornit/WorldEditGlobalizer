package me.illgilp.worldeditglobalizersponge.listener;

import me.illgilp.worldeditglobalizersponge.WorldEditGlobalizerSponge;
import me.illgilp.worldeditglobalizersponge.runnables.ClipboardRunnable;
import me.illgilp.worldeditglobalizersponge.task.AsyncTask;
import me.illgilp.worldeditglobalizersponge.task.QueuedAsyncTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;

public class PlayerJoinListener{

    @Listener
    public void onJoin(ClientConnectionEvent.Join e){
        AsyncTask rn = new ClipboardRunnable(e.getTargetEntity());
        rn.start();
    }

}
