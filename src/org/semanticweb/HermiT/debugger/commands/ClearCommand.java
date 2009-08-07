// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class ClearCommand extends AbstractCommand {

    public ClearCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "clear";
    }
    public String[] getDescription() {
        return new String[] { "","clear the screen" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: clear");
        writer.println("    Clear the command line screen. ");
    }
    public void execute(String[] args) {
        m_debugger.getConsoleTextArea().clear();
    }
}
