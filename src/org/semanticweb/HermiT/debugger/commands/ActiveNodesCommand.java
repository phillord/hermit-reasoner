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

public class ActiveNodesCommand extends AbstractCommand {
    public ActiveNodesCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "activeNodes";
    }
    public String[] getDescription() {
        return new String[] { "","shows all active nodes" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: activeNodes");
        writer.println("    Prints list of all active (non-blocked) nodes in the current model.");
    }
    public void execute(String[] args) {
        int numberOfNodes=0;
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("===========================================");
        writer.println("      ID");
        writer.println("===========================================");
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked()) {
                numberOfNodes++;
                writer.print("  ");
                writer.println(node.getNodeID());
            }
            node=node.getNextTableauNode();
        }
        writer.flush();
        showTextInWindow("Active nodes ("+numberOfNodes+"):"+buffer.toString(),"Active nodes");
        selectConsoleWindow();
    }
}
