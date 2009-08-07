// Copyright 2009 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;

public class NodesForCommand extends AbstractCommand {

    public NodesForCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "nodesFor";
    }
    public String[] getDescription() {
        return new String[] { "conceptName","prints nodes that have been created by (atleast n r.conceptName)" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: nodesFor conceptName");
        writer.println("    Prints all nodes that have been created by a concept (atleast n r.conceptName)");
        writer.println("    together with the information whether the nodes are active or not.");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("Concept name is missing.");
            return;
        }
        String conceptName=args[1];
        AtomicConcept atomicConcept=AtomicConcept.create(m_debugger.getPrefixes().expandAbbreviatedIRI(conceptName));
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Nodes for '"+conceptName+"'");
        writer.println("====================================================================");
        int index=0;
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            Debugger.NodeCreationInfo nodeCreationInfo=m_debugger.getNodeCreationInfo(node);
            ExistentialConcept existentialConcept=nodeCreationInfo.m_createdByExistential;
            if (existentialConcept instanceof AtLeastConcept) {
                if (((AtLeastConcept)existentialConcept).getToConcept().equals(atomicConcept)) {
                    if (index!=0) {
                        writer.print(",");
                        if (index%5==0)
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
}
