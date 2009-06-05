package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.tableau.Node;

public class ShowNodeCommand extends AbstractCommand {

    public ShowNodeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showNode";
    }
    public String[] getDescription() {
        return new String[] { "nodeID","prints information about the given node" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showNode nodeID");
        writer.println("    Prints information about the node for the given node ID.");
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
            m_debugger.getOutput().println("Invalid ID of the first node.");
            return;
        }
        Node node=m_debugger.getTableau().getNode(nodeID);
        if (node==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        Printing.printNodeData(m_debugger,node,writer);
        writer.flush();
        showTextInWindow(buffer.toString(),"Node '"+node.getNodeID()+"'");
        selectConsoleWindow();
    }
}
