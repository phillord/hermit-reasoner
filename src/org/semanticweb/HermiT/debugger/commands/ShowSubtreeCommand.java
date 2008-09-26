package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;


public class ShowSubtreeCommand extends AbstractCommand implements ICommand {
    
    /**
     * Prints the subtree for the node ID given in args or for the last checked 
     * node if no nodeID is in args[1].  
     */
    public void execute() {
        Node subtreeRoot=debugger.getTableau().getCheckedNode();
        if (args.length>=2) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                debugger.getOutput().println("Invalid ID of the first node.");
                return;
            }
            subtreeRoot=debugger.getTableau().getNode(nodeID);
            if (subtreeRoot==null) {
                debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                return;
            }
        }
        new SubtreeViewer(debugger, subtreeRoot);
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: showSubtree [nodeID]");
        writer.println("Prints the subtree for the node with ID nodeID or for " +
        		"the last checked node if no nodeID is given.");
        writer.flush();
        return buffer.toString();
    }

}
