/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
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
        return new String[] { "([+|-]gexists|exists|clash|merge|dtcheck|blvalstart|blvalfinish)+","sets (+ default) or removes (-) breakpoint options" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: waitFor ([+|-]gexists|exists|clash|merge)+");
        writer.println("    Sets (+ default) or removes (-) breakpoint options for the debugger.");
        writer.println("    Possible options are:");
        writer.println("        gexists     - stop at the next description graph expansion");
        writer.println("        exists      - stop at the next existential expansion");
        writer.println("        clash       - stop at the next clash");
        writer.println("        merge       - stop at the next merging of nodes");
        writer.println("        dtcheck     - stop before datatype satisfaction checking");
        writer.println("        blvalstart  - stop before blocking validation");
        writer.println("        blvalfinish - stop after blocking validation");
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
            else if ("blvalstart".equals(argument))
                waitOption=Debugger.WaitOption.BLOCKING_VALIDATION_STARTED;
            else if ("blvalfinish".equals(argument))
                waitOption=Debugger.WaitOption.BLOCKING_VALIDATION_FINISHED;
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
