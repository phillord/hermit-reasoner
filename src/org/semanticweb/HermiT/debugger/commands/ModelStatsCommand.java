package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;

public class ModelStatsCommand extends AbstractCommand implements DebuggerCommand {

    /**
     * Prints statistics about the current model.
     */
    public void execute() {
        int noNodes=0;
        int noUnblockedNodes=0;
        int noDirectlyBlockedNodes=0;
        int noIndirectlyBlockedNodes=0;
        Node node=debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            noNodes++;
            if (node.isDirectlyBlocked())
                noDirectlyBlockedNodes++;
            else if (node.isIndirectlyBlocked())
                noIndirectlyBlockedNodes++;
            else
                noUnblockedNodes++;
            node=node.getNextTableauNode();
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("  Model statistics");
        writer.println("================================================");
        writer.println("  Number of nodes:                    "+noNodes);
        writer.println("  Number of unblocked nodes:          "+noUnblockedNodes);
        writer.println("  Number of directly blocked nodes:   "+noDirectlyBlockedNodes);
        writer.println("  Number of indirectly blocked nodes: "+noIndirectlyBlockedNodes);
        writer.println("================================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Model statistics");
        selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("usage: modelStats");
        writer.println("Prints the statistics about the current model. ");
        writer.flush();
        return buffer.toString();
    }
}
