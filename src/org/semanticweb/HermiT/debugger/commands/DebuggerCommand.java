// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

public interface DebuggerCommand {
    String getCommandName();
    String[] getDescription();
    void printHelp(PrintWriter writer);
    void execute(String[] args);
}
