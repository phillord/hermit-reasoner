// Copyright 2009 by Oxford University; see license.txt for details
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
            "","shows the subtree for rooted at the main node for the task",
            "nodeID","shows the subtree rooted at nodeID"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showSubtree");
        writer.println("    Shows the subtree pf the model rooted at the main node for the task.");
        writer.println("usage: showSubtree nodeID");
        writer.println("    Shows the subtree of the model rooted at the given node.");
    }
    public void execute(String[] args) {
        Node subtreeRoot=m_debugger.getTableau().getCheckedNode0();
        if (args.length>=2) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                m_debugger.getOutput().println("Invalid ID of the first node.");
                return;
            }
            subtreeRoot=m_debugger.getTableau().getNode(nodeID);
            if (subtreeRoot==null) {
                m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                return;
            }
        }
        new SubtreeViewer(m_debugger,subtreeRoot);
    }
}
