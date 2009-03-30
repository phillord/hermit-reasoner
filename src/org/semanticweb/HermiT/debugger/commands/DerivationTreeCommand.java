package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.DerivationHistory;
import org.semanticweb.HermiT.debugger.DerivationViewer;
import org.semanticweb.HermiT.tableau.Node;


public class DerivationTreeCommand extends AbstractCommand implements ICommand {
   
    /**
     * Tries to extract \"clash\" or \"unaryPredicateURI\" and \"nodeID\" or 
     * \"binaryPredicateURI\" and \"nodeID\" and \"nodeID\" from the arguments 
     * given to the constructor and transforms the predicateURI into an 
     * DLPredicate and shows the derivation tree for the given predicate and 
     * nodes or the clash
     */
    public void execute() {
        Object[] tuple;
        // we expect args to look as follows
        // derivationTree clash or
        // derivationTree unaryPredicateURI nodeID or
        // derivationTree binaryPredicateURI nodeID nodeID
        if (args.length<2) {
            debugger.getOutput().println("The specification of the predicate is missing.");
            return;
        }
        String predicate=args[1];
        if ("clash".equals(predicate.toLowerCase()))
            tuple=new Object[0];
        else {
            tuple=new Object[args.length-1];
            tuple[0] = debugger.getDLPredicate(predicate);
            if (tuple[0]==null){
                debugger.getOutput().println("Invalid predicate '"+predicate+"'.");
                return;
            }
        }
        for (int index=1;index<tuple.length;index++) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(args[index+1]);
            }
            catch (NumberFormatException e) {
                debugger.getOutput().println("Invalid ID of the node at argument "+index+".");
                return;
            }
            Node node=debugger.getTableau().getNode(nodeID);
            if (node==null) {
                debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                return;
            }
            tuple[index]=node;
        }
        DerivationHistory.Atom atom=debugger.getDerivationHistory().getAtom(tuple);
        if (atom!=null) {
            new DerivationViewer(debugger.getPrefixes(),atom);
            selectConsoleWindow();
        }
        else
            debugger.getOutput().println("Atom not found.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: dertree clash");
        writer.println("or");
        writer.println("usage: dertree unaryPredicateURI nodeID");
        writer.println("or");
        writer.println("usage: dertree binaryPredicateURI nodeID nodeID");
        writer.println("Shows the derivation tree for the given atom or the clash.");
        writer.flush();
        return buffer.toString();
    }

}
