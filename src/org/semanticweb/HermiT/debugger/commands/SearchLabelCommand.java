package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class SearchLabelCommand extends AbstractCommand {

    public SearchLabelCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "searchLabel";
    }
    public String[] getDescription() {
        return new String[] { "nodeID","prints all nodes with label equal to nodeID" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: searchLabel nodeID");
        writer.println("Finds the node with the given ID, then searches for all nodes with an equal label and prints their IDs and blocking status.");
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
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("Nodes with label equal to the one of "+node.getNodeID());
            writer.println("===========================================");
            writer.println("      ID    Blocked");
            writer.println("===========================================");
            Node searchNode=m_debugger.getTableau().getFirstTableauNode();
            while (searchNode!=null) {
                if (node.getPositiveLabel().equals(searchNode.getPositiveLabel())) {
                    writer.print("  ");
                    Printing.printPadded(writer,searchNode.getNodeID(),6);
                    writer.print("    ");
                    writer.print(formatBlockingStatus(searchNode));
                    writer.println();
                }
                searchNode=searchNode.getNextTableauNode();
            }
            writer.flush();
            showTextInWindow(buffer.toString(),"Nodes with label equal to the one of "+node.getNodeID());
            selectConsoleWindow();
        }
        else
            m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
    }
}
