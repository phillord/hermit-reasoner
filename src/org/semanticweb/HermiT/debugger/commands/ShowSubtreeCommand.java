package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class ShowSubtreeCommand extends AbstractCommand {

    public ShowSubtreeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showSubtree";
    }
    public String[] getDescription() {
        return new String[] { "[nodeID]","prints the subtree for the last checked node or nodeID if given" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showSubtree [nodeID]");
        writer.println("Prints the subtree for the node with ID nodeID or for the last checked node if no nodeID is given.");
    }
    public void execute(String[] args) {
        Node subtreeRoot=m_debugger.getTableau().getCheckedNode();
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
