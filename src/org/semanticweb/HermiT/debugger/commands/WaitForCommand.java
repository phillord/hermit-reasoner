// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class WaitForCommand extends AbstractCommand implements DebuggerCommand {

    public WaitForCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "waitFor";
    }
    public String[] getDescription() {
        return new String[] { "([+|-]gexists|exists|clash|merge)+","sets (+ default) or removes (-) breakpoint options" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: waitFor ([+|-]gexists|exists|clash|merge)+");
        writer.println("    Sets (+ default) or removes (-) breakpoint options for the debugger.");
        writer.println("    Possible options are:");
        writer.println("        gexists - stop at the next description graph expansion");
        writer.println("        exists  - stop at the next existential expansion");
        writer.println("        clash   - stop at the next clash");
        writer.println("        merge   - stop at the next merging of nodes");
        writer.println("        dtcheck - stop before datatype satisfaction checking");
        writer.println("        blvalid - stop after blocking validation");
        writer.println("    Example: waitFor -clash +gexists");
    }
    public void execute(String[] args) {
        boolean add;
        for (int index=1;index<args.length;index++) {
            String argument=args[index];
            Debugger.WaitOption waitOption=null;
            add=true;
            if (argument.startsWith("-")) {
                add=false;
                argument=argument.substring(1);
            }
            else if (argument.startsWith("+"))
                argument=argument.substring(1);
            if ("gexists".equals(argument))
                waitOption=Debugger.WaitOption.GRAPH_EXPANSION;
            else if ("exists".equals(argument))
                waitOption=Debugger.WaitOption.EXISTENTIAL_EXPANSION;
            else if ("clash".equals(argument))
                waitOption=Debugger.WaitOption.CLASH;
            else if ("merge".equals(argument))
                waitOption=Debugger.WaitOption.MERGE;
            else if ("dtcheck".equals(argument))
                waitOption=Debugger.WaitOption.DATATYPE_CHECKING;
            else if ("blvalid".equals(argument))
                waitOption=Debugger.WaitOption.BLOCKING_VALIDATION;
            else {
                m_debugger.getOutput().println("Invalid wait option '"+argument+"'.");
                return;
            }
            if (waitOption!=null) {
                modifyWaitOptions(waitOption,add);
                m_debugger.getOutput().println("Will "+(add ? "" : "not ")+"wait for "+waitOption+".");
            }
        }
    }
    protected void modifyWaitOptions(Debugger.WaitOption waitOption,boolean add) {
        if (add)
            m_debugger.addWaitOption(waitOption);
        else
            m_debugger.removeWaitOption(waitOption);
    }
}
