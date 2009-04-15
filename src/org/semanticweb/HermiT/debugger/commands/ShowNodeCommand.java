package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;


public class ShowNodeCommand extends AbstractCommand implements DebuggerCommand {
    
    /**
     * Prints information about the node for the node ID given in args to the 
     * constructor. 
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
            debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        Node node=debugger.getTableau().getNode(nodeID);
        if (node==null) {
            debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        debugger.printNodeData(node,writer);
        writer.flush();
        showTextInWindow(buffer.toString(),"Node '"+node.getNodeID()+"'");
        selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: showNode nodeID");
        writer.println("Prints information about the node for the given node " +
        		"ID. ");
        writer.flush();
        return buffer.toString();
    }

}
