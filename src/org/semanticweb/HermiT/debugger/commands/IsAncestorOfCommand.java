package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.tableau.Node;

public class IsAncestorOfCommand extends AbstractCommand {

    public IsAncestorOfCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "isAncOf";
    }
    public String[] getDescription() {
        return new String[] { "nodeID1 nodeID2","tests whether nodeID1 is an ancestor of nodeID2" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: isAncOf nodeID1 nodeID2");
        writer.println("    Prints whether the node for nodeID1 is an ancestor of the node for nodeID2.");
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
        boolean result=node1.isAncestorOf(node2);
        m_debugger.getOutput().print("Node "+node1.getNodeID()+" is "+(result ? "" : "not ")+"an ancestor of node "+node2.getNodeID()+".");
    }
}
