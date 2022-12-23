package me.illgilp.worldeditglobalizer.proxy.core.intake.parametric;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import me.illgilp.worldeditglobalizer.proxy.core.intake.argument.CommandArgs;

public class DirectCommandExecutor implements CommandExecutor {

    @Override
    public <T> Future<T> submit(Callable<T> task, CommandArgs args) {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            T returnValue = task.call();
            future.complete(returnValue);
        } catch (Throwable e) {
            future.completeExceptionally(e);
        }
        return future;
    }

}
