package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.debugger.Debugger;

public class SearchPairwiseBlockingCommand extends AbstractCommand {

    public SearchPairwiseBlockingCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "searchPWBlock";
    }
    public String[] getDescription() {
        return new String[] { "nodeID","prints nodes with the same blocking signature" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: searchPWBlock nodeID");
        writer.println("    Prints all nodes that have the same pair-wise blocking signature as the given node.");
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
            m_debugger.getOutput().println("Invalid node ID.");
            return;
        }
        Node node=m_debugger.getTableau().getNode(nodeID);
        if (node!=null) {
            if (node.getNodeType()!=NodeType.TREE_NODE)
                m_debugger.getOutput().println("Node "+node.getNodeID()+" is not a tree node and does not have a pair-wise blocking signature.");
            else {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("Nodes with the pair-wise blocking signature of "+node.getNodeID());
                writer.println("===========================================");
                writer.println("      ID    Blocked");
                writer.println("===========================================");
                Node searchNode=m_debugger.getTableau().getFirstTableauNode();
                while (searchNode!=null) {
                    if (searchNode.getNodeType()==NodeType.TREE_NODE&&node.getPositiveLabel().equals(searchNode.getPositiveLabel())&&node.getParent().getPositiveLabel().equals(searchNode.getParent().getPositiveLabel())&&node.getFromParentLabel().equals(searchNode.getFromParentLabel())&&node.getToParentLabel().equals(searchNode.getToParentLabel())) {
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
        else
            m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
    }
}
