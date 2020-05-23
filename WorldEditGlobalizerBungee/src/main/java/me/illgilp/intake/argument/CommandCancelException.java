package me.illgilp.intake.argument;

public class CommandCancelException extends Throwable {

    public static final CommandCancelException INSTANCE = new CommandCancelException();


    public CommandCancelException() {
    }

}
