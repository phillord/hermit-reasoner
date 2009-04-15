package org.semanticweb.HermiT.debugger.commands;



public class DummyCommand extends AbstractCommand implements DebuggerCommand {
    public void execute() {
        // do nothing
    }

    public String getHelpText() {
        return null;
    }

}
