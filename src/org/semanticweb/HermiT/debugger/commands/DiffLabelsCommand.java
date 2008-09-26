package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;


public class DiffLabelsCommand extends AbstractCommand implements ICommand {
   
    /**
     * Finds the nodes with the given IDs, then prints for both nodes the 
     * (positive) concepts that are in the label of one, but not in the label of 
     * the other node.
     */
    public void execute() {
        if (args.length<3) {
            debugger.getOutput().println("Node IDs are missing.");
            return;
        }
        int nodeID1;
        try {
            nodeID1=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        int nodeID2;
        try {
            nodeID2=Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            debugger.getOutput().println("Invalid ID of the second node.");
            return;
        }
        Node node1=debugger.getTableau().getNode(nodeID1);
        Node node2=debugger.getTableau().getNode(nodeID2);
        if (node1==null) {
            debugger.getOutput().println("Node with ID '" + nodeID1 + 
                    "' not found.");
            return;
        }
        if (node2==null) {
            debugger.getOutput().println("Node with ID '" + nodeID2 + 
                    "' not found.");
            return;
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Differences in labels of node " + node1.getNodeID() + 
                " and " + node2.getNodeID());
        writer.println("===========================================");
        Printing.diffCollections(
            "Concepts in the label of " + node1.getNodeID() + 
            " but not in the label of " + node2.getNodeID(),
            "Concepts in the label of " + node2.getNodeID() + 
            " but not in the label of " + node1.getNodeID(),
            writer, node1.getPositiveLabel(), node2.getPositiveLabel());
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(), 
                "Differences in labels of node " + node1.getNodeID() + " and " + 
                node2.getNodeID());
        selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: diffLabels nodeID1 nodeID2");
        writer.println("Finds the nodes with the given IDs, then prints " +
        	       "for both nodes the (positive) concepts that are in " +
        	       "the label of one, but not in the label of the other " +
        	       "node.");
        writer.flush();
        return buffer.toString();
    }

}
