package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;


public class ActiveNodesCommand extends AbstractCommand implements DebuggerCommand {
    /**
     * Counts the number of active (non-blocked) nodes in the tableau of the 
     * debugger given to the constructor and prints their number and IDs
     */
    public void execute() {
        int numberOfNodes=0;
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("===========================================");
        writer.println("      ID");
        writer.println("===========================================");
        Node node = debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked()) {
                numberOfNodes++;
                writer.print("  ");
                writer.println(node.getNodeID());
            }
            node=node.getNextTableauNode();
        }
        writer.flush();
        super.showTextInWindow("Active nodes (" + numberOfNodes + "):" + 
                buffer.toString(),"Active nodes");
        super.selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: activeNodes");
        writer.println("Counts the number of active (non-blocked) nodes in the " +
        	       "current tableau and prints their number and IDs. ");
        writer.flush();
        return buffer.toString();
    }

}
