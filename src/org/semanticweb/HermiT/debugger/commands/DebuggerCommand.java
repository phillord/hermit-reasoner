package org.semanticweb.HermiT.debugger.commands;

import org.semanticweb.HermiT.debugger.Debugger;

public interface DebuggerCommand {
    public String getHelpText();
    public void execute();
    public void setDebugger(Debugger debugger);
    public void setArgs(String[] args);
}
