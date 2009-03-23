package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;

public class SearchPairwiseBlockingCommand extends AbstractCommand implements ICommand {

    /**
     * Tries to fetch a node ID from the arguments given to the constructor and prints nodes and their blocking status that have the same pair-wise blocking signature of the node for the given node ID.
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
        Node node=debugger.getTableau().getNode(nodeID);
        if (node!=null) {
            if (node.getNodeType()!=NodeType.TREE_NODE)
                debugger.getOutput().println("Node "+node.getNodeID()+" is not a tree node and does not have a pair-wise "+"blocking signature.");
            else {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("Nodes with the pair-wise blocking signature of "+node.getNodeID());
                writer.println("===========================================");
                writer.println("      ID    Blocked");
                writer.println("===========================================");
                Node searchNode=debugger.getTableau().getFirstTableauNode();
                while (searchNode!=null) {
                    if (searchNode.getNodeType()==NodeType.TREE_NODE && node.getPositiveLabel().equals(searchNode.getPositiveLabel()) && node.getParent().getPositiveLabel().equals(searchNode.getParent().getPositiveLabel()) && node.getFromParentLabel().equals(searchNode.getFromParentLabel()) && node.getToParentLabel().equals(searchNode.getToParentLabel())) {
                        writer.print("  ");
                        Printing.printPadded(writer,searchNode.getNodeID(),6);
                        writer.print("    ");
                        writer.print(formatBlockingStatus(searchNode));
                        writer.println();
                    }
                    searchNode=searchNode.getNextTableauNode();
                }
                writer.flush();
                showTextInWindow(buffer.toString(),"Nodes with the pair-wise blocking signature of "+node.getNodeID());
                selectConsoleWindow();
            }
        }
        else {
            debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
        }
    }
    public String getHelpText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("usage: SearchPWBlock NodeID");
        writer.println("Prints nodes and their blocking status that have the "+"same pair-wise blocking signature of the node for "+"the given node ID. ");
        writer.flush();
        return buffer.toString();
    }

}
