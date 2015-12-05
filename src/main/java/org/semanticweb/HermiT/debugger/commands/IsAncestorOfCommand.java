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
import org.semanticweb.HermiT.tableau.Node;

public class IsAncestorOfCommand extends AbstractCommand {

    public IsAncestorOfCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "isAncOf";
    }
    public String[] getDescription() {
        return new String[] { "nodeID1 nodeID2","tests whether nodeID1 is an ancestor of nodeID2" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: isAncOf nodeID1 nodeID2");
        writer.println("    Prints whether the node for nodeID1 is an ancestor of the node for nodeID2.");
    }
    public void execute(String[] args) {
        if (args.length<3) {
            m_debugger.getOutput().println("Node IDs are missing.");
            return;
        }
        int nodeID1;
        try {
            nodeID1=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        int nodeID2;
        try {
            nodeID2=Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid ID of the second node.");
            return;
        }
        Node node1=m_debugger.getTableau().getNode(nodeID1);
        Node node2=m_debugger.getTableau().getNode(nodeID2);
        if (node1==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID1+"' not found.");
            return;
        }
        if (node2==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID2+"' not found.");
            return;
        }
        boolean result=node1.isAncestorOf(node2);
        m_debugger.getOutput().print("Node "+node1.getNodeID()+" is "+(result ? "" : "not ")+"an ancestor of node "+node2.getNodeID()+".");
    }
}
