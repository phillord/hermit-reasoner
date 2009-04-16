package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class ExitCommand extends AbstractCommand {

    public ExitCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "exit";
    }
    public String[] getDescription() {
        return new String[] { "","exit the debugger" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: exit");
        writer.println("Exits the current program. ");
    }
    public void execute(String[] args) {
        System.exit(0);
    }
}
