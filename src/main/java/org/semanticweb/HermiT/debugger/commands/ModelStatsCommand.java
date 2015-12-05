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

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.tableau.Node;

public class ModelStatsCommand extends AbstractCommand {

    public ModelStatsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "modelStats";
    }
    public String[] getDescription() {
        return new String[] { "","prints statistics about a model" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: modelStats");
        writer.println("    Prints statistics about the current model.");
    }
    public void execute(String[] args) {
        int noNodes=0;
        int noUnblockedNodes=0;
        int noDirectlyBlockedNodes=0;
        int noIndirectlyBlockedNodes=0;
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            noNodes++;
            if (node.isDirectlyBlocked())
                noDirectlyBlockedNodes++;
            else if (node.isIndirectlyBlocked())
                noIndirectlyBlockedNodes++;
            else
                noUnblockedNodes++;
            node=node.getNextTableauNode();
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("  Model statistics");
        writer.println("================================================");
        writer.println("  Number of nodes:                    "+noNodes);
        writer.println("  Number of unblocked nodes:          "+noUnblockedNodes);
        writer.println("  Number of directly blocked nodes:   "+noDirectlyBlockedNodes);
        writer.println("  Number of indirectly blocked nodes: "+noIndirectlyBlockedNodes);
        writer.println("================================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Model statistics");
        selectConsoleWindow();
    }
}
