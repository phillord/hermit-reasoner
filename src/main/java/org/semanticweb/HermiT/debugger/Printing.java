/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.debugger;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;

public class Printing {
    public static void printPadded(PrintWriter writer,int number,int size) {
        printPadded(writer,String.valueOf(number),size);
    }
    public static void printPadded(PrintWriter writer,String string,int size) {
        for (int i=size-string.length();i>=0;--i)
            writer.print(' ');
        writer.print(string);
    }
    public static <T> void printCollection(Collection<T> collection,PrintWriter writer) {
        for (T object : collection) {
            writer.print("    ");
            writer.print(object.toString());
            writer.println();
        }
    }
    public static <T> void diffCollections(String in1NotIn2,String in2NotIn1,PrintWriter writer,Collection<T> c1,Collection<T> c2) {
        boolean window1Message=false;
        for (Object object : c1) {
            if (!c2.contains(object)) {
                if (!window1Message) {
                    writer.println("<<<  "+in1NotIn2+":");
                    window1Message=true;
                }
                writer.print("    ");
                writer.print(object.toString());
                writer.println();
            }
        }
        if (window1Message)
            writer.println("--------------------------------------------");
        boolean window2Message=false;
        for (Object object : c2) {
            if (!c1.contains(object)) {
                if (!window2Message) {
                    writer.println(">>>  "+in2NotIn1+":");
                    window2Message=true;
                }
                writer.print("    ");
                writer.print(object.toString());
                writer.println();
            }
        }
        if (window1Message)
            writer.println("--------------------------------------------");
    }
    public static void printNodeData(Debugger debugger,Node node,PrintWriter writer) {
        writer.print("Node ID:    ");
        writer.println(node.getNodeID());
        writer.print("Node Type:  ");
        writer.println(node.getNodeType());
        writer.print("Parent ID:  ");
        writer.println(node.getParent()==null ? "(root node)" : node.getParent().getNodeID());
        writer.print("Depth:      ");
        writer.println(node.getTreeDepth());
        writer.print("Status:     ");
        if (node.isActive())
            writer.println("active");
        else if (node.isMerged()) {
            Node mergeTarget=node.getMergedInto();
            while (mergeTarget!=null) {
                writer.print(" --> ");
                writer.print(mergeTarget.getNodeID());
                mergeTarget=mergeTarget.getMergedInto();
            }
            writer.println();
        }
        else
            writer.println("pruned");
        writer.print("Blocked:    ");
        writer.println(formatBlockingStatus(node));
        writer.print("Created as: ");
        Debugger.NodeCreationInfo nodeCreationInfo=debugger.getNodeCreationInfo(node);
        ExistentialConcept startExistential=nodeCreationInfo.m_createdByExistential;
        if (!(startExistential instanceof AtLeastConcept))
            writer.println("(root)");
        else
            writer.println(((AtLeastConcept)startExistential).getToConcept().toString(debugger.getPrefixes()));
        printConceptLabel(debugger,node,writer);
        printEdges(debugger,node,writer);
    }
    protected static String formatBlockingStatus(Node node) {
        if (!node.isBlocked())
            return "no";
        else if (node.isDirectlyBlocked())
            return "directly by "+(node.getBlocker()==Node.SIGNATURE_CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
        else
            return "indirectly by "+(node.getBlocker()==Node.SIGNATURE_CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
    }
    protected static void printConceptLabel(Debugger debugger,Node node,PrintWriter writer) {
        TreeSet<AtomicConcept> atomicConceptsCore=new TreeSet<AtomicConcept>(ConceptComparator.INSTANCE);
        TreeSet<AtomicConcept> atomicConceptsNoncore=new TreeSet<AtomicConcept>(ConceptComparator.INSTANCE);
        TreeSet<ExistentialConcept> existentialConcepts=new TreeSet<ExistentialConcept>(ConceptComparator.INSTANCE);
        TreeSet<AtomicNegationConcept> negativeConcepts=new TreeSet<AtomicNegationConcept>(ConceptComparator.INSTANCE);
        TreeSet<DataRange> dataRanges=new TreeSet<DataRange>(DataRangeComparator.INSTANCE);
        ExtensionTable.Retrieval retrieval=debugger.getTableau().getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object potentialConcept=retrieval.getTupleBuffer()[0];
            if (potentialConcept instanceof AtomicNegationConcept)
                negativeConcepts.add((AtomicNegationConcept)potentialConcept);
            else if (potentialConcept instanceof AtomicConcept) {
                if (retrieval.isCore())
                    atomicConceptsCore.add((AtomicConcept)potentialConcept);
                else
                    atomicConceptsNoncore.add((AtomicConcept)potentialConcept);
            }
            else if (potentialConcept instanceof ExistentialConcept)
                existentialConcepts.add((ExistentialConcept)potentialConcept);
            else if (potentialConcept instanceof DataRange)
                dataRanges.add((DataRange)potentialConcept);
            else if (potentialConcept instanceof DescriptionGraph) {
                // ignore description graphs here
            }
            else
                throw new IllegalStateException("Found something in the label that is not a known type!");
            retrieval.next();
        }
        Set<AtomicConcept> noConcepts=Collections.emptySet();
        if (!atomicConceptsCore.isEmpty()) {
            writer.print("-- Positive concept label (core part) -------");
            printConcepts(debugger,atomicConceptsCore,noConcepts,writer,3);
        }
        if (!atomicConceptsNoncore.isEmpty() || !existentialConcepts.isEmpty()) {
            writer.print("-- Positive concept label (noncore part) ----");
            printConcepts(debugger,atomicConceptsNoncore,noConcepts,writer,3);
            printConcepts(debugger,existentialConcepts,node.getUnprocessedExistentials(),writer,1);
        }
        if (!negativeConcepts.isEmpty()) {
            writer.print("-- Negative concept label -------------------");
            printConcepts(debugger,negativeConcepts,noConcepts,writer,3);
        }
        if (!dataRanges.isEmpty()) {
            writer.print("-- Data ranges label ------------------------");
            printDataRanges(debugger,dataRanges,writer,1);
        }
    }
    protected static void printEdges(Debugger debugger,Node node,PrintWriter writer) {
        Map<Node,Set<AtomicRole>> outgoingEdges=new TreeMap<Node,Set<AtomicRole>>(NodeComparator.INSTANCE);
        ExtensionTable.Retrieval retrieval=debugger.getTableau().getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=retrieval.getTupleBuffer()[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)retrieval.getTupleBuffer()[0];
                Node toNode=(Node)retrieval.getTupleBuffer()[2];
                Set<AtomicRole> set=outgoingEdges.get(toNode);
                if (set==null) {
                    set=new TreeSet<AtomicRole>(RoleComparator.INSTANCE);
                    outgoingEdges.put(toNode,set);
                }
                set.add(atomicRole);
            }
            retrieval.next();
        }
        if (!outgoingEdges.isEmpty()) {
            writer.println("-- Outgoing edges --------------------------------");
            printEdgeMap(debugger,outgoingEdges,writer);
        }
        Map<Node,Set<AtomicRole>> incomingEdges=new TreeMap<Node,Set<AtomicRole>>(NodeComparator.INSTANCE);
        retrieval=debugger.getTableau().getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[2]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=retrieval.getTupleBuffer()[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)retrieval.getTupleBuffer()[0];
                Node fromNode=(Node)retrieval.getTupleBuffer()[1];
                Set<AtomicRole> set=incomingEdges.get(fromNode);
                if (set==null) {
                    set=new TreeSet<AtomicRole>(RoleComparator.INSTANCE);
                    incomingEdges.put(fromNode,set);
                }
                set.add(atomicRole);
            }
            retrieval.next();
        }
        if (!incomingEdges.isEmpty()) {
            writer.println("-- Incoming edges --------------------------------");
            printEdgeMap(debugger,incomingEdges,writer);
        }
    }
    protected static void printConcepts(Debugger debugger,Set<? extends Concept> set,Collection<? extends Concept> markedElements,PrintWriter writer,int numberInRow) {
        int number=0;
        for (Concept concept : set) {
            if (number!=0)
                writer.print(", ");
            if ((number%numberInRow)==0) {
                writer.println();
                writer.print("    ");
            }
            writer.print(concept.toString(debugger.getPrefixes()));
            if (markedElements.contains(concept))
                writer.print(" (*)");
            number++;
        }
        writer.println();
    }
    protected static void printDataRanges(Debugger debugger,Set<? extends DataRange> set,PrintWriter writer,int numberInRow) {
        int number=0;
        for (DataRange range : set) {
            if (number!=0)
                writer.print(", ");
            if ((number%numberInRow)==0) {
                writer.println();
                writer.print("    ");
            }
            writer.print(range.toString(debugger.getPrefixes()));
            number++;
        }
        writer.println();
    }
    protected static void printEdgeMap(Debugger debugger,Map<Node,Set<AtomicRole>> map,PrintWriter writer) {
        for (Map.Entry<Node,Set<AtomicRole>> entry : map.entrySet()) {
            writer.print("    ");
            writer.print(entry.getKey().getNodeID());
            writer.print(" -->");
            int number=0;
            for (AtomicRole atomicRole : entry.getValue()) {
                if (number!=0)
                    writer.print(", ");
                if ((number%3)==0) {
                    writer.println();
                    writer.print("        ");
                }
                writer.print(atomicRole.toString(debugger.getPrefixes()));
                number++;
            }
            writer.println();
        }
    }

    public static class ConceptComparator implements Comparator<Concept> {
        public static final ConceptComparator INSTANCE=new ConceptComparator();

        public int compare(Concept c1,Concept c2) {
            ConceptType type1=getConceptType(c1);
            ConceptType type2=getConceptType(c2);
            if (type1!=type2)
                return type1.getTypeIndex()-type2.getTypeIndex();
            switch (type1) {
            case AtomicConcept:
                return ((AtomicConcept)c1).getIRI().compareTo(((AtomicConcept)c2).getIRI());
            case AtLeastConcept:
                {
                    AtLeastConcept l1=(AtLeastConcept)c1;
                    AtLeastConcept l2=(AtLeastConcept)c2;
                    int comparison=RoleComparator.INSTANCE.compare(l1.getOnRole(),l2.getOnRole());
                    if (comparison!=0)
                        return comparison;
                    return compare(l1.getToConcept(),l2.getToConcept());
                }
            case ExistsDescriptionGraph:
                {
                    ExistsDescriptionGraph g1=(ExistsDescriptionGraph)c1;
                    ExistsDescriptionGraph g2=(ExistsDescriptionGraph)c2;
                    return g1.getDescriptionGraph().getName().compareTo(g2.getDescriptionGraph().getName());
                }
            case AtomicNegationConcept:
                return ((AtomicNegationConcept)c1).getNegatedAtomicConcept().getIRI().compareTo(((AtomicNegationConcept)c2).getNegatedAtomicConcept().getIRI());
            default:
                throw new IllegalArgumentException();
            }
        }
        protected static enum ConceptType {
            AtomicConcept(0),
            AtLeastConcept(1),
            ExistsDescriptionGraph(2), 
            AtomicNegationConcept(3);

            private final int m_typeIndex;
            ConceptType(int typeIndex) {
                m_typeIndex=typeIndex;
            }
            final int getTypeIndex() {
                return m_typeIndex;
            }
        }
        protected ConceptType getConceptType(Concept c) {
            if (c instanceof AtomicConcept)
                return ConceptType.AtomicConcept;
            else if (c instanceof AtLeastConcept)
                return ConceptType.AtLeastConcept;
            else if (c instanceof ExistsDescriptionGraph)
                return ConceptType.ExistsDescriptionGraph;
            else if (c instanceof AtomicNegationConcept)
                return ConceptType.AtomicNegationConcept;
            else
                throw new IllegalArgumentException();
        }
    }
    
    public static class DataRangeComparator implements Comparator<DataRange> {
        public static final DataRangeComparator INSTANCE=new DataRangeComparator();

        public int compare(DataRange c1,DataRange c2) {
            DataRangeType type1=getDataRangeType(c1);
            DataRangeType type2=getDataRangeType(c2);
            if (type1!=type2)
                return type1.getTypeIndex()-type2.getTypeIndex();
            switch (type1) {
            case DatatypeRestriction:
                return compareDatatypeRestrictions((DatatypeRestriction)c1,(DatatypeRestriction)c2);
            case ConstantEnumeration:
                return compareConstantEnumerations((ConstantEnumeration)c1,(ConstantEnumeration)c2);
            case AtomicNegationDataRange:
                {
                    AtomicNegationDataRange ndr1=(AtomicNegationDataRange)c1;
                    AtomicNegationDataRange ndr2=(AtomicNegationDataRange)c2;
                    return compare(ndr1.getNegatedDataRange(),ndr2.getNegatedDataRange());
                }
            case InternalDatatype:
                return ((InternalDatatype)c1).getIRI().compareTo(((InternalDatatype)c2).getIRI());
            default:
                throw new IllegalArgumentException();
            }
        }
        protected static enum DataRangeType {
            DatatypeRestriction(0),
            ConstantEnumeration(1),
            AtomicNegationDataRange(2), 
            InternalDatatype(3);

            private final int m_typeIndex;
            DataRangeType(int typeIndex) {
                m_typeIndex=typeIndex;
            }
            final int getTypeIndex() {
                return m_typeIndex;
            }
        }
        protected DataRangeType getDataRangeType(DataRange dr) {
            if (dr instanceof DatatypeRestriction)
                return DataRangeType.DatatypeRestriction;
            else if (dr instanceof InternalDatatype)
                return DataRangeType.InternalDatatype; 
            else if (dr instanceof ConstantEnumeration)
                return DataRangeType.ConstantEnumeration;
            else if (dr instanceof AtomicNegationDataRange)
                return DataRangeType.AtomicNegationDataRange;
            else
                throw new IllegalArgumentException();
        }
        protected int compareDatatypeRestrictions(DatatypeRestriction dr1,DatatypeRestriction dr2) {
            int comparison=dr1.getDatatypeURI().compareTo(dr2.getDatatypeURI());
            if (comparison!=0)
                return comparison;
            comparison=dr1.getNumberOfFacetRestrictions()-dr2.getNumberOfFacetRestrictions();
            if (comparison!=0)
                return comparison;
            for (int index=0;index<dr1.getNumberOfFacetRestrictions();index++) {
                comparison=dr1.getFacetURI(index).compareTo(dr2.getFacetURI(index));
                if (comparison!=0)
                    return comparison;
                comparison=compareConstants(dr1.getFacetValue(index),dr2.getFacetValue(index));
                if (comparison!=0)
                    return comparison;
            }
            return 0;
        }
        protected int compareConstantEnumerations(ConstantEnumeration dve1,ConstantEnumeration dve2) {
            int comparison=dve1.getNumberOfConstants()-dve2.getNumberOfConstants();
            if (comparison!=0)
                return comparison;
            for (int index=0;index<dve1.getNumberOfConstants();index++) {
                comparison=compareConstants(dve1.getConstant(index),dve2.getConstant(index));
                if (comparison!=0)
                    return comparison;
            }
            return 0;

        }
        protected int compareConstants(Constant c1,Constant c2) {
            int comparison=c1.getDatatypeURI().compareTo(c2.getDatatypeURI());
            if (comparison!=0)
                return comparison;
            return c1.getLexicalForm().compareTo(c2.getLexicalForm());
        }
    }
    protected static class RoleComparator implements Comparator<Role> {
        public static final RoleComparator INSTANCE=new RoleComparator();

        public int compare(Role ar1,Role ar2) {
            int type1=getRoleType(ar1);
            int type2=getRoleType(ar2);
            if (type1!=type2)
                return type1-type2;
            if (type1==0)
                return ((AtomicRole)ar1).getIRI().compareTo(((AtomicRole)ar2).getIRI());
            else
                return ((InverseRole)ar1).getInverseOf().getIRI().compareTo(((InverseRole)ar2).getInverseOf().getIRI());
        }
        protected int getRoleType(Role ar) {
            if (ar instanceof AtomicRole)
                return 0;
            else
                return 1;
        }
    }

    protected static class NodeComparator implements Comparator<Node> {
        public static final NodeComparator INSTANCE=new NodeComparator();

        public int compare(Node o1,Node o2) {
            return o1.getNodeID()-o2.getNodeID();
        }
    }

    public static class FactComparator implements Comparator<Object[]> {
        public static final FactComparator INSTANCE=new FactComparator();

        public int compare(Object[] o1,Object[] o2) {
            int compare=o1.length-o2.length;
            if (compare!=0)
                return compare;
            compare=o1[0].toString().compareTo(o2[0].toString());
            if (compare!=0)
                return compare;
            for (int index=1;index<o1.length;index++) {
                compare=((Node)o1[index]).getNodeID()-((Node)o2[index]).getNodeID();
                if (compare!=0)
                    return compare;
            }
            return 0;
        }
    }
}
