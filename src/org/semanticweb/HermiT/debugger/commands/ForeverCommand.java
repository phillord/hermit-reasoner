// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class ForeverCommand extends AbstractCommand {
    
    public ForeverCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "forever";
    }
    public String[] getDescription() {
        return new String[] { "","run and do not wait for further input" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: forever");
        writer.println("    Continues with the current reasoning task without");
        writer.println("    waiting for further input by the user.");
    }
    public void execute(String[] args) {
        m_debugger.setInMainLoop(false);
        m_debugger.setForever(true);
        m_debugger.setSinglestep(false);
    }
}
