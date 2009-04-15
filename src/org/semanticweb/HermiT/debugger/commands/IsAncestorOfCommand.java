package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;

public class IsAncestorOfCommand extends AbstractCommand implements DebuggerCommand {

    /**
     * Finds the nodes with the given IDs, then prints whether the node for nodeID1 is an ancestor of the node for nodeID2.
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
            debugger.getOutput().println("Node with ID '"+nodeID1+"' not found.");
            return;
        }
        if (node2==null) {
            debugger.getOutput().println("Node with ID '"+nodeID2+"' not found.");
            return;
        }
        boolean result=node1.isAncestorOf(node2);
        debugger.getOutput().print("Node "+node1.getNodeID()+" is "+(result ? "" : "not ")+"an ancestor of node "+node2.getNodeID()+".");
    }
    public String getHelpText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("usage: isAncOf nodeID1 nodeID2");
        writer.println("Finds the nodes with the given IDs, then prints "+"whether the node for nodeID1 is an ancestor of the "+"node for nodeID2. ");
        writer.flush();
        return buffer.toString();
    }
}
