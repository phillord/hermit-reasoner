// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public final class DescriptionGraphManager implements Serializable {
    private static final long serialVersionUID=4536271856850424712L;

    protected final Tableau m_tableau;
    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final MergingManager m_mergingManager;
    protected final boolean m_hasDescriptionGraphs;
    protected final OccurrenceManager m_occurrenceManager;
    protected final Map<DescriptionGraph,Integer> m_descriptionGraphIndices;
    protected final DescriptionGraph[] m_descriptionGraphsByIndex;
    protected final ExtensionTable[] m_extensionTablesByIndex;
    protected final Object[][] m_auxiliaryTuples1;
    protected final Object[][] m_auxiliaryTuples2;
    protected final List<Node> m_newNodes;
    protected final UnionDependencySet m_binaryUnionDependencySet;
    protected final ExtensionTable.Retrieval[] m_deltaOldRetrievals;
    
    public DescriptionGraphManager(Tableau tableau) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_extensionManager=m_tableau.getExtensionManager();
        m_mergingManager=m_tableau.getMergingManager();
        m_hasDescriptionGraphs=!m_tableau.m_dlOntology.getAllDescriptionGraphs().isEmpty();
        m_occurrenceManager=new OccurrenceManager();
        m_descriptionGraphIndices=new HashMap<DescriptionGraph,Integer>();
        Set<ExtensionTable> extensionTables=new HashSet<ExtensionTable>();
        List<DescriptionGraph> descriptionGraphsByIndex=new ArrayList<DescriptionGraph>();
        List<ExtensionTable> extensionTablesByIndex=new ArrayList<ExtensionTable>();
        for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs()) {
            m_descriptionGraphIndices.put(descriptionGraph,Integer.valueOf(descriptionGraphsByIndex.size()));
            descriptionGraphsByIndex.add(descriptionGraph);
            ExtensionTable extensionTable=m_extensionManager.getExtensionTable(descriptionGraph.getNumberOfVertices()+1);
            extensionTablesByIndex.add(extensionTable);
            extensionTables.add(extensionTable);
        }
        m_descriptionGraphsByIndex=new DescriptionGraph[descriptionGraphsByIndex.size()];
        descriptionGraphsByIndex.toArray(m_descriptionGraphsByIndex);
        m_extensionTablesByIndex=new ExtensionTable[extensionTablesByIndex.size()];
        extensionTablesByIndex.toArray(m_extensionTablesByIndex);
        m_auxiliaryTuples1=new Object[m_descriptionGraphsByIndex.length][];
        m_auxiliaryTuples2=new Object[m_descriptionGraphsByIndex.length][];
        for (int index=0;index<m_descriptionGraphsByIndex.length;index++) {
            DescriptionGraph descriptionGraph=m_descriptionGraphsByIndex[index];
            m_auxiliaryTuples1[index]=new Object[descriptionGraph.getNumberOfVertices()+1];
            m_auxiliaryTuples2[index]=new Object[descriptionGraph.getNumberOfVertices()+1];
        }
        m_newNodes=new ArrayList<Node>();
        m_binaryUnionDependencySet=new UnionDependencySet(2);
        m_deltaOldRetrievals=new ExtensionTable.Retrieval[extensionTables.size()];
        int index=0;
        for (ExtensionTable extensionTable : extensionTables)
            m_deltaOldRetrievals[index++]=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.DELTA_OLD);
    }
    public void clear() {
        for (int index=0;index<m_auxiliaryTuples1.length;index++) {
            Arrays.fill(m_auxiliaryTuples1[index],null);
            Arrays.fill(m_auxiliaryTuples2[index],null);
        }
        m_occurrenceManager.clear();
    }
    public boolean checkGraphConstraints() {
        if (m_hasDescriptionGraphs) {
            boolean hasChange=false;
            for (ExtensionTable.Retrieval retrieval : m_deltaOldRetrievals) {
                ExtensionTable extensionTable=retrieval.getExtensionTable();
                retrieval.open();
                Object[] tupleBuffer=retrieval.getTupleBuffer();
                int arity=tupleBuffer.length;
                while (!retrieval.afterLast()) {
                    if (tupleBuffer[0] instanceof DescriptionGraph) {
                        int thisGraphIndex=m_descriptionGraphIndices.get(tupleBuffer[0]).intValue();
                        int thisTupleIndex=retrieval.getCurrentTupleIndex();
                        for (int thisPositionInTuple=1;thisPositionInTuple<arity;thisPositionInTuple++) {
                            Node node=(Node)tupleBuffer[thisPositionInTuple];
                            int listNode=node.m_firstGraphOccurrenceNode;
                            while (listNode!=-1) {
                                int graphIndex=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.GRAPH_INDEX);
                                int tupleIndex=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.TUPLE_INDEX);
                                int positionInTuple=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.POSITION_IN_TUPLE);
                                if (thisGraphIndex==graphIndex && (thisTupleIndex!=tupleIndex || thisPositionInTuple!=positionInTuple) && extensionTable.isTupleActive(tupleIndex)) {
                                    m_binaryUnionDependencySet.m_dependencySets[0]=retrieval.getDependencySet();
                                    m_binaryUnionDependencySet.m_dependencySets[1]=extensionTable.getDependencySet(tupleIndex);
                                    if (thisPositionInTuple==positionInTuple) {
                                        for (int mergePosition=arity-1;mergePosition>=1;--mergePosition) {
                                            Node nodeFirst=(Node)extensionTable.getTupleObject(thisTupleIndex,mergePosition);
                                            Node nodeSecond=(Node)extensionTable.getTupleObject(tupleIndex,mergePosition);
                                            if (nodeFirst!=nodeSecond) {
                                                if (m_tableauMonitor==null)
                                                    m_mergingManager.mergeNodes(nodeFirst,nodeSecond,m_binaryUnionDependencySet);
                                                else {
                                                    Object[] graph1=m_auxiliaryTuples1[thisGraphIndex];
                                                    extensionTable.retrieveTuple(graph1,thisTupleIndex);
                                                    Object[] graph2=m_auxiliaryTuples2[graphIndex];
                                                    extensionTable.retrieveTuple(graph2,tupleIndex);
                                                    m_tableauMonitor.mergeGraphsStarted(graph1,graph2,mergePosition);
                                                    m_mergingManager.mergeNodes(nodeFirst,nodeSecond,m_binaryUnionDependencySet);
                                                    m_tableauMonitor.mergeGraphsFinished(graph1,graph2,mergePosition);
                                                }
                                                hasChange=true;
                                            }
                                        }
                                    }
                                    else {
                                        m_extensionManager.setClash(m_binaryUnionDependencySet);
                                        if (m_tableauMonitor!=null) {
                                            Object[] graph1=m_auxiliaryTuples1[thisGraphIndex];
                                            extensionTable.retrieveTuple(graph1,thisTupleIndex);
                                            Object[] graph2=m_auxiliaryTuples2[graphIndex];
                                            extensionTable.retrieveTuple(graph2,tupleIndex);
                                            m_tableauMonitor.clashDetected(graph1,graph2);
                                        }
                                        return true;
                                    }
                                }
                                listNode=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.NEXT_NODE);
                            }
                        }
                    }
                    retrieval.next();
                }
            }
            return hasChange;
        }
        else
            return false;
    }   
    public boolean isSatisfied(ExistsDescriptionGraph existsDescriptionGraph,Node node) {
        int graphIndex=m_descriptionGraphIndices.get(existsDescriptionGraph.getDescriptionGraph()).intValue();
        int positionInTuple=existsDescriptionGraph.getVertex()+1;
        int listNode=node.m_firstGraphOccurrenceNode;
        while (listNode!=-1) {
            if (graphIndex==m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.GRAPH_INDEX) && positionInTuple==m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.POSITION_IN_TUPLE))
                return true;
            listNode=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.NEXT_NODE);
        }
        return false;
    }
    public void mergeGraphs(Node mergeFrom,Node mergeInto,UnionDependencySet binaryUnionDependencySet) {
        int listNode=mergeFrom.m_firstGraphOccurrenceNode;
        while (listNode!=-1) {
            int graphIndex=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.GRAPH_INDEX);
            int tupleIndex=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.TUPLE_INDEX);
            int positionInTuple=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.POSITION_IN_TUPLE);
            ExtensionTable extensionTable=m_extensionTablesByIndex[graphIndex];
            Object[] auxiliaryTuple=m_auxiliaryTuples1[graphIndex];
            extensionTable.retrieveTuple(auxiliaryTuple,tupleIndex);
            if (extensionTable.isTupleActive(auxiliaryTuple)) {
                if (m_tableauMonitor!=null) {
                    Object[] sourceTuple=m_auxiliaryTuples2[graphIndex];
                    System.arraycopy(auxiliaryTuple,0,sourceTuple,0,auxiliaryTuple.length);
                    auxiliaryTuple[positionInTuple]=mergeInto;
                    m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,auxiliaryTuple);
                    m_binaryUnionDependencySet.m_dependencySets[0]=extensionTable.getDependencySet(tupleIndex);
                    m_extensionManager.addTuple(auxiliaryTuple,m_binaryUnionDependencySet);
                    m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,auxiliaryTuple);
                }
                else {
                    auxiliaryTuple[positionInTuple]=mergeInto;
                    m_binaryUnionDependencySet.m_dependencySets[0]=extensionTable.getDependencySet(tupleIndex);
                    m_extensionManager.addTuple(auxiliaryTuple,m_binaryUnionDependencySet);
                }
            }
            listNode=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.NEXT_NODE);
        }
    }
    public void descriptionGraphTupleAdded(int tupleIndex,Object[] tuple) {
        int graphIndex=m_descriptionGraphIndices.get(tuple[0]).intValue();
        for (int positionInTuple=tuple.length-1;positionInTuple>=1;--positionInTuple) {
            Node node=(Node)tuple[positionInTuple];
            int listNode=m_occurrenceManager.newListNode();
            m_occurrenceManager.initializeListNode(listNode,graphIndex,tupleIndex,positionInTuple,node.m_firstGraphOccurrenceNode);
            node.m_firstGraphOccurrenceNode=listNode;
        }
    }
    public void descriptionGraphTupleRemoved(int tupleIndex,Object[] tuple) {
        for (int positionInTuple=tuple.length-1;positionInTuple>=1;--positionInTuple) {
            Node node=(Node)tuple[positionInTuple];
            int listNode=node.m_firstGraphOccurrenceNode;
            assert m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.GRAPH_INDEX)==m_descriptionGraphIndices.get(tuple[0]).intValue();
            assert m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.TUPLE_INDEX)==tupleIndex;
            assert m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.POSITION_IN_TUPLE)==positionInTuple;
            node.m_firstGraphOccurrenceNode=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.NEXT_NODE);
            m_occurrenceManager.deleteListNode(listNode);
        }
    }
    public void expand(ExistsDescriptionGraph existsDescriptionGraph,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(existsDescriptionGraph,forNode);
        m_newNodes.clear();
        DescriptionGraph descriptionGraph=existsDescriptionGraph.getDescriptionGraph();
        DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(existsDescriptionGraph,forNode);
        Object[] auxiliaryTuple=m_auxiliaryTuples1[m_descriptionGraphIndices.get(descriptionGraph).intValue()];
        auxiliaryTuple[0]=descriptionGraph;
        for (int vertex=0;vertex<descriptionGraph.getNumberOfVertices();vertex++) {
            Node newNode;
            if (vertex==existsDescriptionGraph.getVertex())
                newNode=forNode;
            else
                newNode=m_tableau.createNewGraphNode(forNode,dependencySet);
            m_newNodes.add(newNode);
            auxiliaryTuple[vertex+1]=newNode;
        }
        m_extensionManager.addTuple(auxiliaryTuple,dependencySet);
        // Replace all nodes with the canonical node because the nodes might have been merged
        for (int vertex=0;vertex<descriptionGraph.getNumberOfVertices();vertex++) {
            Node newNode=m_newNodes.get(vertex);
            dependencySet=newNode.addCacnonicalNodeDependencySet(dependencySet);
            m_newNodes.set(vertex,newNode.getCanonicalNode());
        }
        // Now add the graph layout
        for (int vertex=0;vertex<descriptionGraph.getNumberOfVertices();vertex++)
            m_extensionManager.addConceptAssertion(descriptionGraph.getAtomicConceptForVertex(vertex),m_newNodes.get(vertex),dependencySet);
        for (int edgeIndex=0;edgeIndex<descriptionGraph.getNumberOfEdges();edgeIndex++) {
            DescriptionGraph.Edge edge=descriptionGraph.getEdge(edgeIndex);
            m_extensionManager.addRoleAssertion(edge.getAtomicAbstractRole(),m_newNodes.get(edge.getFromVertex()),m_newNodes.get(edge.getToVertex()),dependencySet);
        }
        m_newNodes.clear();
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionFinished(existsDescriptionGraph,forNode);
    }
    public void intializeNode(Node node) {
        node.m_firstGraphOccurrenceNode=-1;
    }
    public void destroyNode(Node node) {
        int listNode=node.m_firstGraphOccurrenceNode;
        while (listNode!=-1) {
            int nextListNode=m_occurrenceManager.getListNodeComponent(listNode,OccurrenceManager.NEXT_NODE);
            m_occurrenceManager.deleteListNode(listNode);
            listNode=nextListNode;
        }
        node.m_firstGraphOccurrenceNode=-1;
    }
    
    protected static class OccurrenceManager {
        public static final int GRAPH_INDEX=0; 
        public static final int TUPLE_INDEX=1; 
        public static final int POSITION_IN_TUPLE=2;
        public static final int NEXT_NODE=3;
        public static final int LIST_NODE_SIZE=4;
        public static final int LIST_NODE_PAGE_SIZE=LIST_NODE_SIZE*512;
        
        protected int[][] m_nodePages;
        protected int m_firstFreeListNode;
        protected int m_numberOfPages;
        
        public OccurrenceManager() {
            m_nodePages=new int[10][];
            m_nodePages[0]=new int[LIST_NODE_PAGE_SIZE];
            m_numberOfPages=1;
            m_firstFreeListNode=0;
            setListNodeComponent(m_firstFreeListNode,NEXT_NODE,-1);
        }
        public void clear() {
            m_firstFreeListNode=0;
            setListNodeComponent(m_firstFreeListNode,NEXT_NODE,-1);
        }
        public int getListNodeComponent(int listNode,int component) {
            return m_nodePages[listNode / LIST_NODE_PAGE_SIZE][(listNode % LIST_NODE_PAGE_SIZE)+component];
        }
        public void setListNodeComponent(int listNode,int component,int value) {
            m_nodePages[listNode / LIST_NODE_PAGE_SIZE][(listNode % LIST_NODE_PAGE_SIZE)+component]=value;
        }
        public void initializeListNode(int listNode,int graphIndex,int tupleIndex,int positionInTuple,int nextListNode) {
            int pageIndex=listNode / LIST_NODE_PAGE_SIZE;
            int indexInPage=listNode % LIST_NODE_PAGE_SIZE;
            int[] nodePage=m_nodePages[pageIndex];
            nodePage[indexInPage+GRAPH_INDEX]=graphIndex;
            nodePage[indexInPage+TUPLE_INDEX]=tupleIndex;
            nodePage[indexInPage+POSITION_IN_TUPLE]=positionInTuple;
            nodePage[indexInPage+NEXT_NODE]=nextListNode;
        }
        public int newListNode() {
            int newListNode=m_firstFreeListNode;
            int nextFreeListNode=getListNodeComponent(m_firstFreeListNode,NEXT_NODE);
            if (nextFreeListNode!=-1)
                m_firstFreeListNode=nextFreeListNode;
            else {
                m_firstFreeListNode+=LIST_NODE_SIZE;
                int pageIndex=m_firstFreeListNode / LIST_NODE_PAGE_SIZE;
                if (pageIndex>=m_numberOfPages) {
                    if (pageIndex>=m_nodePages.length) {
                        int[][] newNodePages=new int[m_nodePages.length*3/2][];
                        System.arraycopy(m_nodePages,0,newNodePages,0,m_nodePages.length);
                        m_nodePages=newNodePages;
                    }
                    m_nodePages[pageIndex]=new int[LIST_NODE_PAGE_SIZE];
                    m_numberOfPages++;
                }
                setListNodeComponent(m_firstFreeListNode,NEXT_NODE,-1);
            }
            return newListNode;
        }
        public void deleteListNode(int listNode) {
            setListNodeComponent(listNode,NEXT_NODE,m_firstFreeListNode);
            m_firstFreeListNode=listNode;
        }
    }
}
