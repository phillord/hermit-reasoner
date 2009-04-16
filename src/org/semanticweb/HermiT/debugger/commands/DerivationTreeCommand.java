package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.DerivationHistory;
import org.semanticweb.HermiT.debugger.DerivationViewer;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class DerivationTreeCommand extends AbstractCommand {

    public DerivationTreeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "dertree";
    }
    public String[] getDescription() {
        return new String[] {
            "clash","shows the derivation tree for the clash",
            "predicateURI nodeID","shows the derivation tree for the given unary atom",
            "predicateURI nodeID nodeID","shows the derivation tree for the given binary atom"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: dertree clash");
        writer.println("or");
        writer.println("usage: dertree unaryPredicateURI nodeID");
        writer.println("or");
        writer.println("usage: dertree binaryPredicateURI nodeID nodeID");
        writer.println("Shows the derivation tree for the given atom or the clash.");
    }
    public void execute(String[] args) {
        Object[] tuple;
        // args should look as follows:
        // derivationTree clash or
        // derivationTree unaryPredicateURI nodeID or
        // derivationTree binaryPredicateURI nodeID nodeID
        if (args.length<2) {
            m_debugger.getOutput().println("The specification of the predicate is missing.");
            return;
        }
        String predicate=args[1];
        if ("clash".equals(predicate.toLowerCase()))
            tuple=new Object[0];
        else {
            tuple=new Object[args.length-1];
            tuple[0]=m_debugger.getDLPredicate(predicate);
            if (tuple[0]==null) {
                m_debugger.getOutput().println("Invalid predicate '"+predicate+"'.");
                return;
            }
        }
        for (int index=1;index<tuple.length;index++) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(args[index+1]);
            }
            catch (NumberFormatException e) {
                m_debugger.getOutput().println("Invalid ID of the node at argument "+index+".");
                return;
            }
            Node node=m_debugger.getTableau().getNode(nodeID);
            if (node==null) {
                m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                return;
            }
            tuple[index]=node;
        }
        DerivationHistory.Atom atom=m_debugger.getDerivationHistory().getAtom(tuple);
        if (atom!=null) {
            new DerivationViewer(m_debugger.getPrefixes(),atom);
            selectConsoleWindow();
        }
        else
            m_debugger.getOutput().println("Atom not found.");
    }
}
