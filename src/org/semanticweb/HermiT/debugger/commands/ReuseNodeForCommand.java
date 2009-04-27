package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class ReuseNodeForCommand extends AbstractCommand {

    public ReuseNodeForCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "reuseNodeFor";
    }
    public String[] getDescription() {
        return new String[] { "nodeID","prints concepts for which the given node is a reuse node under individual reuse strategy" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: reuseNodeFor nodeID");
        writer.println("    If individual reuse strategy is used, prints the concepts for which the given node is a reuse node.");
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
            m_debugger.getOutput().println("Invalid ID of the node.");
            return;
        }
        Node node=m_debugger.getTableau().getNode(nodeID);
        if (node==null) {
            m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        ExistentialExpansionStrategy strategy=m_debugger.getTableau().getExistentialsExpansionStrategy();
        if (strategy instanceof IndividualReuseStrategy) {
            IndividualReuseStrategy reuseStrategy=(IndividualReuseStrategy)strategy;
            AtomicConcept conceptForNode=reuseStrategy.getConceptForNode(node);
            m_debugger.getOutput().print("Node '");
            m_debugger.getOutput().print(node.getNodeID());
            m_debugger.getOutput().print("' is ");
            if (conceptForNode==null)
                m_debugger.getOutput().println("not a reuse node for any concept.");
            else {
                m_debugger.getOutput().print("a reuse node for the '");
                m_debugger.getOutput().print(conceptForNode.toString(m_debugger.getPrefixes()));
                m_debugger.getOutput().println("' concept.");
            }
        }
        else
            m_debugger.getOutput().println("Node reuse strategy is not currently in effect.");
    }
}
