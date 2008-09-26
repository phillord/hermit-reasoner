package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.tableau.Node;


public class ReuseNodeForCommand extends AbstractCommand implements ICommand {
   
    /**
     * Fetches a node ID from the arguments given to the constructor and tries 
     * to find the node for this ID in the tableau. If individual reuse strategy 
     * is used, prints the concepts for which the given node is a reuse node.
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            debugger.getOutput().println("Invalid ID of the node.");
            return;
        }
        Node node=debugger.getTableau().getNode(nodeID);
        if (node==null) {
            debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        ExpansionStrategy strategy=debugger.getTableau().getExistentialsExpansionStrategy();
        if (strategy instanceof IndividualReuseStrategy) {
            IndividualReuseStrategy reuseStrategy=(IndividualReuseStrategy)strategy;
            LiteralConcept conceptForNode=reuseStrategy.getConceptForNode(node);
            debugger.getOutput().print("Node '");
            debugger.getOutput().print(node.getNodeID());
            debugger.getOutput().print("' is ");
            if (conceptForNode==null)
                debugger.getOutput().println("not a reuse node for any concept.");
            else {
                debugger.getOutput().print("a reuse node for the '");
                debugger.getOutput().print(conceptForNode.toString(debugger.getNamespaces()));
                debugger.getOutput().println("' concept.");
            }
        }
        else
            debugger.getOutput().println("Node reuse strategy is not currently in effect.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: reuseNodeFor nodeID");
        writer.println("If individual reuse strategy is used, prints the " +
        		"concepts for which the given node is a reuse node. ");
        writer.flush();
        return buffer.toString();
    }

}
