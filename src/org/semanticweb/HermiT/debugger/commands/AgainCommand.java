package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class AgainCommand extends AbstractCommand {
    
    public AgainCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "a";
    }
    public String[] getDescription() {
        return new String[] { "","executes the last command again" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: a");
        writer.println("    Executes the last command again.");
    }
    public void execute(String[] args) {
        String commandLine=m_debugger.getLastCommand();
        if (commandLine!=null) {
            m_debugger.getOutput().println("# "+commandLine);
            m_debugger.processCommandLine(commandLine);
        }
    }
}
