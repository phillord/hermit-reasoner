package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public final class MergingManager implements Serializable {
    private static final long serialVersionUID=-8404748898127176927L;

    protected final Tableau m_tableau;
    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_binaryExtensionTableSearch1Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch1Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch2Bound;
    protected final Object[] m_binaryAuxiliaryTuple;
    protected final Object[] m_ternaryAuxiliaryTuple;
    protected final Map<DescriptionGraph,Object[]> m_descriptionGraphTuples;
    protected final UnionDependencySet m_binaryUnionDependencySet;
    
    public MergingManager(Tableau tableau) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_extensionManager=m_tableau.getExtensionManager();
        m_binaryExtensionTableSearch1Bound=m_extensionManager.m_binaryExtensionTable.createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch1Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch2Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        m_binaryAuxiliaryTuple=new Object[2];
        m_ternaryAuxiliaryTuple=new Object[3];
        m_descriptionGraphTuples=new HashMap<DescriptionGraph,Object[]>();
        for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs())
            m_descriptionGraphTuples.put(descriptionGraph,new Object[descriptionGraph.getNumberOfVertices()+1]);
        m_binaryUnionDependencySet=new UnionDependencySet(2);
    }
    public void clear() {
    }
    public boolean mergeNodes(Node node0,Node node1,DependencySet dependencySet) {
        if (!node0.isActive() || !node1.isActive() || node0==node1)
            return false;
        else if (node0.isGloballyUnique() || node1.isGloballyUnique()) {
            m_extensionManager.setClash(dependencySet);
            if (m_tableauMonitor!=null) {
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeStarted(node0,node1);
                Object[] auxiliaryTuple=new Object[] { Equality.INSTANCE,node0,node1 };
                m_tableauMonitor.clashDetected(auxiliaryTuple);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFinished(node0,node1);
            }
            return true;
        }
        else {
            if (m_tableauMonitor!=null)
                m_tableauMonitor.mergeStarted(node0,node1);
            Node mergeFrom;
            Node mergeInto;
            if (node0.getNodeType()==NodeType.ROOT_NODE && node1.getNodeType()==NodeType.ROOT_NODE) {
                if (node0.getPositiveLabelSize()>node1.getPositiveLabelSize()) {
                    mergeFrom=node1;
                    mergeInto=node0;
                }
                else {
                    mergeFrom=node0;
                    mergeInto=node1;
                }
            }
            else if (node0.getNodeType()==NodeType.ROOT_NODE) {
                mergeFrom=node1;
                mergeInto=node0;
            }
            else if (node1.getNodeType()==NodeType.ROOT_NODE) {
                mergeFrom=node0;
                mergeInto=node1;
            }
            else if (node0.m_parent.m_parent==node1) {
                mergeFrom=node0;
                mergeInto=node1;
            }
            else if (node1.m_parent.m_parent==node0) {
                mergeFrom=node1;
                mergeInto=node0;
            }
            else if (node0.m_parent==node1.m_parent) {
                if (node0.getPositiveLabelSize()>node1.getPositiveLabelSize()) {
                    mergeFrom=node1;
                    mergeInto=node0;
                }
                else {
                    mergeFrom=node0;
                    mergeInto=node1;
                }
            }
            else if (node0.getNodeType()==NodeType.GRAPH_NODE) {
                mergeFrom=node0;
                mergeInto=node1;
            }
            else
                throw new IllegalStateException("Internal error: unsupported merge type.");
            // Now prune the mergeFrom node. We go through all subsequent nodes (all successors of mergeFrom come after mergeFrom)
            // and delete them it their parent is not in tableau or if it is the mergeFrom node.
            Node node=mergeFrom;
            while (node!=null) {
                if (node.isActive() && node.getNodeType()!=NodeType.ROOT_NODE && (!node.getParent().isActive() || node.getParent()==mergeFrom)) {
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.nodePruned(node);
                    m_tableau.pruneNode(node);
                }
                node=node.getNextTableauNode();
            }
            m_binaryUnionDependencySet.m_dependencySets[1]=dependencySet;
            // Copy all unary assertions
            m_binaryAuxiliaryTuple[1]=mergeInto;
            m_binaryExtensionTableSearch1Bound.getBindingsBuffer()[1]=mergeFrom;
            m_binaryExtensionTableSearch1Bound.open();
            Object[] tupleBuffer=m_binaryExtensionTableSearch1Bound.getTupleBuffer();
            while (!m_binaryExtensionTableSearch1Bound.afterLast()) {
                m_binaryAuxiliaryTuple[0]=tupleBuffer[0];
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_binaryAuxiliaryTuple);
                m_binaryUnionDependencySet.m_dependencySets[0]=m_binaryExtensionTableSearch1Bound.getDependencySet();
                m_extensionManager.addTuple(m_binaryAuxiliaryTuple,m_binaryUnionDependencySet);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_binaryAuxiliaryTuple);
                m_binaryExtensionTableSearch1Bound.next();
            }
            // Copy all binary assertions where mergeFrom occurs in the first position
            m_ternaryAuxiliaryTuple[1]=mergeInto;
            m_ternaryExtensionTableSearch1Bound.getBindingsBuffer()[1]=mergeFrom;
            m_ternaryExtensionTableSearch1Bound.open();
            tupleBuffer=m_ternaryExtensionTableSearch1Bound.getTupleBuffer();
            while (!m_ternaryExtensionTableSearch1Bound.afterLast()) {
                m_ternaryAuxiliaryTuple[0]=tupleBuffer[0];
                m_ternaryAuxiliaryTuple[2]=(tupleBuffer[2]==mergeFrom ? mergeInto : tupleBuffer[2]);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                m_binaryUnionDependencySet.m_dependencySets[0]=m_ternaryExtensionTableSearch1Bound.getDependencySet();
                m_extensionManager.addTuple(m_ternaryAuxiliaryTuple,m_binaryUnionDependencySet);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                m_ternaryExtensionTableSearch1Bound.next();
            }
            // Copy all binary assertions where mergeFrom occurs in the second position
            m_ternaryAuxiliaryTuple[2]=mergeInto;
            m_ternaryExtensionTableSearch2Bound.getBindingsBuffer()[2]=mergeFrom;
            m_ternaryExtensionTableSearch2Bound.open();
            tupleBuffer=m_ternaryExtensionTableSearch2Bound.getTupleBuffer();
            while (!m_ternaryExtensionTableSearch2Bound.afterLast()) {
                m_ternaryAuxiliaryTuple[0]=tupleBuffer[0];
                m_ternaryAuxiliaryTuple[1]=(tupleBuffer[1]==mergeFrom ? mergeInto : tupleBuffer[1]);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                m_binaryUnionDependencySet.m_dependencySets[0]=m_ternaryExtensionTableSearch2Bound.getDependencySet();
                m_extensionManager.addTuple(m_ternaryAuxiliaryTuple,m_binaryUnionDependencySet);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                m_ternaryExtensionTableSearch2Bound.next();
            }
            // Now merge the description graphs
            Map<DescriptionGraph,Node.Occurrence> fromOccursInDescriptionGraphs=mergeFrom.m_occursInDescriptionGraphs;
            if (fromOccursInDescriptionGraphs!=null && !fromOccursInDescriptionGraphs.isEmpty()) {
                for (Map.Entry<DescriptionGraph,Node.Occurrence> entry : fromOccursInDescriptionGraphs.entrySet()) {
                    DescriptionGraph descriptionGraph=entry.getKey();
                    ExtensionTable graphExtensionTable=m_extensionManager.getExtensionTable(descriptionGraph.getArity()+1);
                    Object[] auxiliaryTuple=m_descriptionGraphTuples.get(descriptionGraph);
                    Node.Occurrence occurrence=entry.getValue();
                    while (occurrence!=null) {
                        int tupleIndex=occurrence.m_tupleIndex;
                        graphExtensionTable.retrieveTuple(auxiliaryTuple,tupleIndex);
                        if (graphExtensionTable.isTupleActive(auxiliaryTuple)) {
                            if (m_tableauMonitor!=null) {
                                Object[] sourceTuple=new Object[descriptionGraph.getNumberOfVertices()+1];
                                System.arraycopy(auxiliaryTuple,0,sourceTuple,0,auxiliaryTuple.length);
                                m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,sourceTuple,auxiliaryTuple);
                                auxiliaryTuple[occurrence.m_position]=mergeInto;
                                m_binaryUnionDependencySet.m_dependencySets[0]=graphExtensionTable.getDependencySet(tupleIndex);
                                m_extensionManager.addTuple(auxiliaryTuple,m_binaryUnionDependencySet);
                                m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,sourceTuple,auxiliaryTuple);
                            }
                            else {
                                auxiliaryTuple[occurrence.m_position]=mergeInto;
                                m_binaryUnionDependencySet.m_dependencySets[0]=graphExtensionTable.getDependencySet(tupleIndex);
                                m_extensionManager.addTuple(auxiliaryTuple,m_binaryUnionDependencySet);
                            }
                        }
                        occurrence=occurrence.m_next;
                    }
                }
            }
            // Now finally merge the nodes
            m_tableau.mergeNode(mergeFrom,mergeInto,dependencySet);
            if (m_tableauMonitor!=null)
                m_tableauMonitor.mergeFinished(node0,node1);
            return true;
        }
    }
}
