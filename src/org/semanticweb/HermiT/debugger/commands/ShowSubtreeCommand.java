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

public class ShowSubtreeCommand extends AbstractCommand {

    public ShowSubtreeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showSubtree";
    }
    public String[] getDescription() {
        return new String[] {
            "nodeID","shows the subtree rooted at nodeID"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showSubtree nodeID");
        writer.println("    Shows the subtree of the model rooted at the given node.");
        writer.println("    black: root node");
        writer.println("    darkgrey: named node");
        writer.println("    green: blockable node (not blocked)");
        writer.println("    light gray: inactive node");
        writer.println("    cyan: blocked node");
        writer.println("    red: node with unprocessed existentials");
        writer.println("    magenta: description graph node");
        writer.println("    blue: concrete/data value node");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        Node subtreeRoot=m_debugger.getTableau().getNode(nodeID);
        if (subtreeRoot==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        new SubtreeViewer(m_debugger,subtreeRoot);
    }
}
