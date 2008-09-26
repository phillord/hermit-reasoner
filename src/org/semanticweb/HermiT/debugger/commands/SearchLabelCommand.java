package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;


public class SearchLabelCommand extends AbstractCommand implements ICommand {
   
    /**
     * Tries to fetch a node ID from the arguments given to the constructor and 
     * prints their IDs and blocking status.
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
            debugger.getOutput().println("Invalid node ID.");
            return;
        }
        Node node = debugger.getTableau().getNode(nodeID);
        if (node!=null) {
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("Nodes with label equal to the one of "+node.getNodeID());
            writer.println("===========================================");
            writer.println("      ID    Blocked");
            writer.println("===========================================");
            Node searchNode = debugger.getTableau().getFirstTableauNode();
            while (searchNode!=null) {
                if (node.getPositiveLabel().equals(searchNode.getPositiveLabel())) {
                    writer.print("  ");
                    Printing.printPadded(writer,searchNode.getNodeID(),6);
                    writer.print("    ");
                    writer.print(formatBlockingStatus(searchNode));
                    writer.println();
                }
                searchNode = searchNode.getNextTableauNode();
            }
            writer.flush();
            super.showTextInWindow(buffer.toString(),"Nodes with label equal to the one of "+node.getNodeID());
            super.selectConsoleWindow();
        }
        else
            debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: searchLabel nodeID");
        writer.println("Finds the node with the given ID, then searches for " +
        	       "all nodes with an equal label and prints their " +
        	       "IDs and blocking status.");
        writer.flush();
        return buffer.toString();
    }

}
