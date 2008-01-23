package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    protected final List<Node> m_newNodes;
    protected final Map<DescriptionGraph,Object[]> m_descriptionGraphTuples;
    protected final Map<ExistsDescriptionGraph,ExtensionTable.Retrieval> m_cachedRetrievals;
    
    public DescriptionGraphManager(Tableau tableau) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_extensionManager=m_tableau.getExtensionManager();
        m_mergingManager=m_tableau.getMergingManager();
        m_hasDescriptionGraphs=!m_tableau.m_dlOntology.getAllDescriptionGraphs().isEmpty();
        m_newNodes=new ArrayList<Node>();
        m_descriptionGraphTuples=new HashMap<DescriptionGraph,Object[]>();
        for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs())
            m_descriptionGraphTuples.put(descriptionGraph,new Object[descriptionGraph.getNumberOfVertices()+1]);
        m_cachedRetrievals=new HashMap<ExistsDescriptionGraph,ExtensionTable.Retrieval>();
    }
    public void clear() {
        m_cachedRetrievals.clear();
        for (Map.Entry<DescriptionGraph,Object[]> entry : m_descriptionGraphTuples.entrySet())
            Arrays.fill(entry.getValue(),null);
    }
    public boolean checkGraphConstraints() {
        if (!m_hasDescriptionGraphs)
            return false;
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isInTableau() && node.m_occursInDescriptionGraphs!=null && node.m_occursInDescriptionGraphsDirty) {
                for (Map.Entry<DescriptionGraph,Node.Occurrence> entry : node.m_occursInDescriptionGraphs.entrySet()) {
                    Node.Occurrence occurrence=entry.getValue();
                    if (occurrence!=null && occurrence.m_next!=null) {
                        DescriptionGraph descriptionGraph=entry.getKey();
                        Node.Occurrence firstValidOccurrence=null;
                        ExtensionTable graphExtensionTable=m_extensionManager.getExtensionTable(descriptionGraph.getArity()+1);
                        while (occurrence!=null) {
                            int tupleIndex=occurrence.m_tupleIndex;
                            if (graphExtensionTable.isTupleValid(tupleIndex)) {
                                if (firstValidOccurrence==null)
                                    firstValidOccurrence=occurrence;
                                else {
                                    DependencySet firstSet=graphExtensionTable.getDependencySet(firstValidOccurrence.m_tupleIndex);
                                    DependencySet secondSet=graphExtensionTable.getDependencySet(occurrence.m_tupleIndex);
                                    if (firstValidOccurrence.m_position!=occurrence.m_position) {
                                        m_extensionManager.setClash(firstSet,secondSet);
                                        if (m_tableauMonitor!=null) {
                                            Object[] graph1=new Object[descriptionGraph.getArity()+1];
                                            graphExtensionTable.retrieveTuple(graph1,firstValidOccurrence.m_tupleIndex);
                                            Object[] graph2=new Object[descriptionGraph.getArity()+1];
                                            graphExtensionTable.retrieveTuple(graph2,tupleIndex);
                                            m_tableauMonitor.clashDetected(graph1,graph2);
                                        }
                                        return true;
                                    }
                                    else {
                                        for (int index=descriptionGraph.getArity();index>0;--index) {
                                            Node nodeFirst=(Node)graphExtensionTable.getTupleObject(firstValidOccurrence.m_tupleIndex,index);
                                            Node nodeSecond=(Node)graphExtensionTable.getTupleObject(tupleIndex,index);
                                            if (nodeFirst!=nodeSecond) {
                                                if (m_tableauMonitor==null)
                                                    m_mergingManager.mergeNodes(nodeFirst,nodeSecond,firstSet,secondSet);
                                                else {
                                                    Object[] graph1=new Object[descriptionGraph.getArity()+1];
                                                    graphExtensionTable.retrieveTuple(graph1,firstValidOccurrence.m_tupleIndex);
                                                    Object[] graph2=new Object[descriptionGraph.getArity()+1];
                                                    graphExtensionTable.retrieveTuple(graph2,tupleIndex);
                                                    m_tableauMonitor.mergeGraphsStarted(graph1,graph2,index);
                                                    m_mergingManager.mergeNodes(nodeFirst,nodeSecond,firstSet,secondSet);
                                                    m_tableauMonitor.mergeGraphsFinished(graph1,graph2,index);
                                                }
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                            occurrence=occurrence.m_next;
                        }
                    }
                }
                node.m_occursInDescriptionGraphsDirty=false;
            }
            node=node.m_nextTableauNode;
        }
        return false;
    }   
    public boolean isSatisfied(ExistsDescriptionGraph existsDescriptionGraph,Node node) {
        ExtensionTable.Retrieval retrieval=m_cachedRetrievals.get(existsDescriptionGraph);
        if (retrieval==null) {
            DescriptionGraph descriptionGraph=existsDescriptionGraph.getDescriptionGraph();
            ExtensionTable extensionTable=m_extensionManager.getExtensionTable(descriptionGraph.getNumberOfVertices()+1);
            boolean[] bindingPattern=new boolean[descriptionGraph.getNumberOfVertices()+1];
            bindingPattern[0]=true;
            bindingPattern[existsDescriptionGraph.getVertex()+1]=true;
            retrieval=extensionTable.createRetrieval(bindingPattern,ExtensionTable.View.TOTAL);
            m_cachedRetrievals.put(existsDescriptionGraph,retrieval);
            retrieval.getBindingsBuffer()[0]=descriptionGraph;
        }
        retrieval.getBindingsBuffer()[existsDescriptionGraph.getVertex()+1]=node;
        retrieval.open();
        return !retrieval.afterLast();
    }
    public void expand(ExistsDescriptionGraph existsDescriptionGraph,Node forNode) {
        if (m_tableau.m_tableauMonitor!=null)
            m_tableau.m_tableauMonitor.existentialExpansionStarted(existsDescriptionGraph,forNode);
        m_newNodes.clear();
        DescriptionGraph descriptionGraph=existsDescriptionGraph.getDescriptionGraph();
        DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(existsDescriptionGraph,forNode);
        Object[] auxiliaryTuple=m_descriptionGraphTuples.get(descriptionGraph);
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
}
