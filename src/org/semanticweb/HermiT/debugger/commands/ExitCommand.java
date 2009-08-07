// Copyright 2009 by Oxford University; see license.txt for details
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
        return new String[] { "","exits the curtrent process" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: exit");
        writer.println("    Exits the current process.");
    }
    public void execute(String[] args) {
        System.exit(0);
    }
}
