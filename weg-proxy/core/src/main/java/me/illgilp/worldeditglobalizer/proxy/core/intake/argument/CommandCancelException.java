package me.illgilp.worldeditglobalizer.proxy.core.intake.argument;

public class CommandCancelException extends RuntimeException {

    public static final CommandCancelException INSTANCE = new CommandCancelException();


    public CommandCancelException() {
    }

}
