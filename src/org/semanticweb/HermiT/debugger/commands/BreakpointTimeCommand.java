package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class BreakpointTimeCommand extends AbstractCommand {

    public BreakpointTimeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "bpTime";
    }
    public String[] getDescription() {
        return new String[] { "timeInSeconds","set break point after timeInSeconds" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: bpTime timeInSeconds");
        writer.println("Sets the next breakpoint time to the given time in seconds.");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("Time is missing.");
            return;
        }
        int breakpointTimeSeconds;
        try {
            breakpointTimeSeconds=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid time.");
            return;
        }
        m_debugger.getOutput().println("Breakpoint time is "+breakpointTimeSeconds+" seconds.");
        m_debugger.setBreakpointTime(breakpointTimeSeconds*1000);
    }
}
