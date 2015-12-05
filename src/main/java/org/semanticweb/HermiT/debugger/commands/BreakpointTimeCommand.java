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

public class BreakpointTimeCommand extends AbstractCommand {

    public BreakpointTimeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "bpTime";
    }
    public String[] getDescription() {
        return new String[] { "timeInSeconds","sets the break point time" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: bpTime timeInSeconds");
        writer.println("    Sets the breakpoint time -- that is, after timeInSeconds,");
        writer.println("    the debugger will return control to the user.");
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
