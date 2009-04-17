package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class DiffLabelsCommand extends AbstractCommand {

    public DiffLabelsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "diffLabels";
    }
    public String[] getDescription() {
        return new String[] { "nodeID1 nodeID2","compares the labels of the given nodes" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: diffLabels nodeID1 nodeID2");
        writer.println("    Prints all (atomic) concepts that are in the label of one node but not in the other.");
    }
    public void execute(String[] args) {
        if (args.length<3) {
            m_debugger.getOutput().println("Node IDs are missing.");
            return;
        }
        int nodeID1;
        try {
            nodeID1=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        int nodeID2;
        try {
            nodeID2=Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            m_debugger.getOutput().println("Invalid ID of the second node.");
            return;
        }
        Node node1=m_debugger.getTableau().getNode(nodeID1);
        Node node2=m_debugger.getTableau().getNode(nodeID2);
        if (node1==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID1+"' not found.");
            return;
        }
        if (node2==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID2+"' not found.");
            return;
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Differences in labels of node "+node1.getNodeID()+" and "+node2.getNodeID());
        writer.println("===========================================");
        Printing.diffCollections("Concepts in the label of "+node1.getNodeID()+" but not in the label of "+node2.getNodeID(),"Concepts in the label of "+node2.getNodeID()+" but not in the label of "+node1.getNodeID(),writer,node1.getPositiveLabel(),node2.getPositiveLabel());
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Differences in labels of node "+node1.getNodeID()+" and "+node2.getNodeID());
        selectConsoleWindow();
    }
}
