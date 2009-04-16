package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

public interface DebuggerCommand {
    String getCommandName();
    String[] getDescription();
    void printHelp(PrintWriter writer);
    void execute(String[] args);
}
