package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class ContinueCommand extends AbstractCommand {
    
    public ContinueCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "c";
    }
    public String[] getDescription() {
        return new String[] { "","continues with the current reasoning tasks" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: c");
        writer.println("    Continues with the current reasoning tasks.");
    }
    public void execute(String[] args) {
          m_debugger.setInMainLoop(false);
    }
}
