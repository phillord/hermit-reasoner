// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class SingleStepCommand extends AbstractCommand {

    public SingleStepCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "singleStep";
    }
    public String[] getDescription() {
        return new String[] { "on|off","step-by-step mode on or off" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: singleStep on|off");
        writer.println("    If on, the debugger will return control to the user after each step.");
        writer.println("    If off, the debugger will run until a breakpoint is reached.");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("The status is missing.");
            return;
        }
        String status=args[1].toLowerCase();
        if ("on".equals(status)) {
            m_debugger.setSinglestep(true);
            m_debugger.getOutput().println("Single step mode on.");
        }
        else if ("off".equals(status)) {
            m_debugger.setSinglestep(false);
            m_debugger.getOutput().println("Single step mode off.");
        }
        else
            m_debugger.getOutput().println("Incorrect single step mode '"+status+"'.");
    }
}
