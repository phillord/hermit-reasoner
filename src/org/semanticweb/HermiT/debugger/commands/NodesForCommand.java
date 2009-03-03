package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.debugger.Debugger.NodeCreationInfo;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;

public class NodesForCommand extends AbstractCommand implements ICommand {

    /**
     * Tries to fetch a concept name \"C\" from the arguments given to the constructor and
     * prints a list of nodes and their blocking status for
     * nodes that have been created by a concept of the form (atleast n r.C)
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("Concept name is missing.");
            return;
        }
        String conceptName=args[1];
        AtomicConcept atomicConcept=AtomicConcept.create(debugger.getNamespaces().uriFromId(conceptName));
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Nodes for '"+conceptName+"'");
        writer.println("====================================================================");
        int index=0;
        Node node=debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            NodeCreationInfo nodeCreationInfo=debugger.getNodeCreationInfo(node);
            ExistentialConcept existentialConcept=nodeCreationInfo.m_createdByExistential;
            if (existentialConcept instanceof AtLeastConcept) {
                if (((AtLeastConcept)existentialConcept).getToConcept().equals(atomicConcept)) {
                    if (index!=0) {
                        writer.print(",");
                        if (index % 5==0)
                            writer.println();
                        else
                            writer.print("  ");
                    }
                    Printing.printPadded(writer,node.getNodeID()+(node.isActive() ? "" : "*"),8);
                    index++;
                }
            }
            node=node.getNextTableauNode();
        }
        writer.println();
        writer.println("====================================================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Nodes for '"+conceptName+"'");
        selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("usage: nodesFor conceptName");
        writer.println("Creates a concept \"C\" from conceptName, finds all nodes that have been created by a concept (atleast n r.C) and prints them and whether they are active or not.");
        writer.flush();
        return buffer.toString();
    }

}
