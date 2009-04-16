package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Printing;

public class OriginStatsCommand extends AbstractCommand {

    public OriginStatsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "originStats";
    }
    public String[] getDescription() {
        return new String[] { "","prints origin information for nodes in the tableau" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: originStats");
        writer.println("Prints origin information for the nodes in the current tableau. ");
    }
    public void execute(String[] args) {
        Map<Concept,OriginInfo> originInfos=new HashMap<Concept,OriginInfo>();
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            Debugger.NodeCreationInfo nodeCreationInfo=m_debugger.getNodeCreationInfo(node);
            ExistentialConcept existentialConcept=nodeCreationInfo.m_createdByExistential;
            if (existentialConcept instanceof AtLeastConcept) {
                Concept toConcept=((AtLeastConcept)existentialConcept).getToConcept();
                OriginInfo originInfo=originInfos.get(toConcept);
                if (originInfo==null) {
                    originInfo=new OriginInfo(toConcept);
                    originInfos.put(toConcept,originInfo);
                }
                originInfo.m_nodes.add(node);
                if (!node.isActive())
                    originInfo.m_numberOfNonactiveOccurrences++;
            }
            node=node.getNextTableauNode();
        }
        OriginInfo[] originInfosArray=new OriginInfo[originInfos.size()];
        originInfos.values().toArray(originInfosArray);
        Arrays.sort(originInfosArray,OriginInfoComparator.INSTANCE);
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Statistics of node origins");
        writer.println("====================================");
        writer.println("  Occurrence    Nonactive   Concept");
        writer.println("====================================");
        for (OriginInfo originInfo : originInfosArray) {
            writer.print("  ");
            Printing.printPadded(writer,originInfo.m_nodes.size(),8);
            writer.print("    ");
            Printing.printPadded(writer,originInfo.m_numberOfNonactiveOccurrences,8);
            writer.print("    ");
            writer.print(originInfo.m_concept.toString(m_debugger.getPrefixes()));
            if (originInfo.m_nodes.size()<=5) {
                writer.print("  [ ");
                for (int index=0;index<originInfo.m_nodes.size();index++) {
                    if (index!=0)
                        writer.print(", ");
                    node=originInfo.m_nodes.get(index);
                    writer.print(node.getNodeID());
                    if (!node.isActive())
                        writer.print('*');
                }
                writer.print(" ]");
            }
            writer.println();
        }
        writer.println("====================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Statistics of node origins");
        selectConsoleWindow();
    }

    protected static class OriginInfo {
        public final Concept m_concept;
        public final List<Node> m_nodes;
        public int m_numberOfNonactiveOccurrences;

        public OriginInfo(Concept concept) {
            m_concept=concept;
            m_nodes=new ArrayList<Node>();
        }
    }

    protected static class OriginInfoComparator implements Comparator<OriginInfo> {
        public static final OriginInfoComparator INSTANCE=new OriginInfoComparator();

        public int compare(OriginInfo o1,OriginInfo o2) {
            int comparison=o1.m_nodes.size()-o2.m_nodes.size();
            if (comparison==0) {
                comparison=o1.m_numberOfNonactiveOccurrences-o2.m_numberOfNonactiveOccurrences;
                if (comparison==0)
                    comparison=Debugger.ConceptComparator.INSTANCE.compare(o1.m_concept,o2.m_concept);
            }
            return comparison;
        }
    }
}
