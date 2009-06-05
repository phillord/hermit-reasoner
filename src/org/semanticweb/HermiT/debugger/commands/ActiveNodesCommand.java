package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.tableau.Node;

public class ActiveNodesCommand extends AbstractCommand {
    public ActiveNodesCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "activeNodes";
    }
    public String[] getDescription() {
        return new String[] { "","shows all active nodes" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: activeNodes");
        writer.println("    Prints list of all active (non-blocked) nodes in the current model.");
    }
    public void execute(String[] args) {
        int numberOfNodes=0;
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("===========================================");
        writer.println("      ID");
        writer.println("===========================================");
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked()) {
                numberOfNodes++;
                writer.print("  ");
                writer.println(node.getNodeID());
            }
            node=node.getNextTableauNode();
        }
        writer.flush();
        showTextInWindow("Active nodes ("+numberOfNodes+"):"+buffer.toString(),"Active nodes");
        selectConsoleWindow();
    }
}
