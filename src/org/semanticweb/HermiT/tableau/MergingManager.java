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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.monitor.TableauMonitor;

/**
 * Implements the merge rule and is used whenever the merge rule needs to be applied
 * during the expansion of the tableau object used in the constructor of the class.
 */
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
    protected final UnionDependencySet m_binaryUnionDependencySet;

    public MergingManager(Tableau tableau) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_extensionManager=m_tableau.m_extensionManager;
        m_binaryExtensionTableSearch1Bound=m_extensionManager.m_binaryExtensionTable.createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch1Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch2Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        m_binaryAuxiliaryTuple=new Object[2];
        m_ternaryAuxiliaryTuple=new Object[3];
        m_binaryUnionDependencySet=new UnionDependencySet(2);
    }
    public void clear() {
        m_binaryExtensionTableSearch1Bound.clear();
        m_ternaryExtensionTableSearch1Bound.clear();
        m_ternaryExtensionTableSearch2Bound.clear();
        m_binaryAuxiliaryTuple[0]=null;
        m_binaryAuxiliaryTuple[1]=null;
        m_ternaryAuxiliaryTuple[0]=null;
        m_ternaryAuxiliaryTuple[1]=null;
        m_ternaryAuxiliaryTuple[2]=null;
    }
    /**
     * Merges the two given nodes and adjusts the dependency set as required. It is
     * automatically figured out which node has to be merged into which -- that is,
     * the order between node0 and node1 is not important.
     */
    public boolean mergeNodes(Node node0,Node node1,DependencySet dependencySet) {
        assert node0.getNodeType().isAbstract()==node1.getNodeType().isAbstract();
        if (!node0.isActive() || !node1.isActive() || node0==node1)
            return false;
        else {
            Node mergeFrom;
            Node mergeInto;
            int node0Precedence=node0.getNodeType().getMergePrecedence();
            int node1Precedence=node1.getNodeType().getMergePrecedence();
            if (node0Precedence<node1Precedence) {
                mergeFrom=node1;
                mergeInto=node0;
            }
            else if (node0Precedence>node1Precedence) {
                mergeFrom=node0;
                mergeInto=node1;
            }
            else {
                // Cluster anchors correspond to the [s] notation in the graphs paper.
                Node node0ClusterAnchor=node0.getClusterAnchor();
                Node node1ClusterAnchor=node1.getClusterAnchor();
                // Watch out: node0ClusterAnchor and/or node1ClusterAnchor can be 'null' -- that is,
                // 'null' plays the role of the \triangleright symbol from the graphs paper.
                boolean canMerge0Into1=node0.m_parent==node1.m_parent || isDescendantOfAtMostThreeLevels(node0,node1ClusterAnchor);
                boolean canMerge1Into0=node0.m_parent==node1.m_parent || isDescendantOfAtMostThreeLevels(node1,node0ClusterAnchor);
                if (canMerge0Into1 && canMerge1Into0) {
                    if (node0.m_numberOfPositiveAtomicConcepts>node1.m_numberOfPositiveAtomicConcepts) {
                        mergeFrom=node1;
                        mergeInto=node0;
                    }
                    else {
                        mergeFrom=node0;
                        mergeInto=node1;
                    }
                }
                else if (canMerge0Into1) {
                    mergeFrom=node0;
                    mergeInto=node1;
                }
                else if (canMerge1Into0) {
                    mergeFrom=node1;
                    mergeInto=node0;
                }
                else
                    throw new IllegalStateException("Internal error: unsupported merge type.");
            }
            if (m_tableauMonitor!=null)
                m_tableauMonitor.mergeStarted(mergeFrom,mergeInto);
            // Now prune the mergeFrom node. We go through all subsequent nodes
            // (all successors of mergeFrom come after mergeFrom)
            // and delete them it their parent is not in tableau or if it is the
            // mergeFrom node.
            Node node=mergeFrom;
            while (node!=null) {
                if (node.isActive() && node.m_parent!=null && (!node.m_parent.isActive() || node.m_parent==mergeFrom)) {
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
                Object predicate=tupleBuffer[0];
                if (!(predicate instanceof DescriptionGraph)) {
                    m_binaryAuxiliaryTuple[0]=predicate;
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_binaryAuxiliaryTuple);
                    m_binaryUnionDependencySet.m_dependencySets[0]=m_binaryExtensionTableSearch1Bound.getDependencySet();
                    m_extensionManager.addTuple(m_binaryAuxiliaryTuple,m_binaryUnionDependencySet,m_binaryExtensionTableSearch1Bound.isCore());
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_binaryAuxiliaryTuple);
                }
                m_binaryExtensionTableSearch1Bound.next();
            }
            // Copy all binary assertions where mergeFrom occurs in the first position
            m_ternaryAuxiliaryTuple[1]=mergeInto;
            m_ternaryExtensionTableSearch1Bound.getBindingsBuffer()[1]=mergeFrom;
            m_ternaryExtensionTableSearch1Bound.open();
            tupleBuffer=m_ternaryExtensionTableSearch1Bound.getTupleBuffer();
            while (!m_ternaryExtensionTableSearch1Bound.afterLast()) {
                Object predicate=tupleBuffer[0];
                if (!(predicate instanceof DescriptionGraph)) {
                    m_ternaryAuxiliaryTuple[0]=predicate;
                    m_ternaryAuxiliaryTuple[2]=(tupleBuffer[2]==mergeFrom ? mergeInto : tupleBuffer[2]);
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                    m_binaryUnionDependencySet.m_dependencySets[0]=m_ternaryExtensionTableSearch1Bound.getDependencySet();
                    m_extensionManager.addTuple(m_ternaryAuxiliaryTuple,m_binaryUnionDependencySet,m_ternaryExtensionTableSearch1Bound.isCore());
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                }
                m_ternaryExtensionTableSearch1Bound.next();
            }
            // Copy all binary assertions where mergeFrom occurs in the second position
            m_ternaryAuxiliaryTuple[2]=mergeInto;
            m_ternaryExtensionTableSearch2Bound.getBindingsBuffer()[2]=mergeFrom;
            m_ternaryExtensionTableSearch2Bound.open();
            tupleBuffer=m_ternaryExtensionTableSearch2Bound.getTupleBuffer();
            while (!m_ternaryExtensionTableSearch2Bound.afterLast()) {
                Object predicate=tupleBuffer[0];
                if (!(predicate instanceof DescriptionGraph)) {
                    m_ternaryAuxiliaryTuple[0]=predicate;
                    m_ternaryAuxiliaryTuple[1]=(tupleBuffer[1]==mergeFrom ? mergeInto : tupleBuffer[1]);
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactStarted(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                    m_binaryUnionDependencySet.m_dependencySets[0]=m_ternaryExtensionTableSearch2Bound.getDependencySet();
                    m_extensionManager.addTuple(m_ternaryAuxiliaryTuple,m_binaryUnionDependencySet,m_ternaryExtensionTableSearch2Bound.isCore());
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.mergeFactFinished(mergeFrom,mergeInto,tupleBuffer,m_ternaryAuxiliaryTuple);
                }
                m_ternaryExtensionTableSearch2Bound.next();
            }
            // Now merge the description graphs
            m_tableau.m_descriptionGraphManager.mergeGraphs(mergeFrom,mergeInto,m_binaryUnionDependencySet);
            // Now finally merge the nodes
            m_tableau.mergeNode(mergeFrom,mergeInto,dependencySet);
            // Inform the monitor
            if (m_tableauMonitor!=null)
                m_tableauMonitor.mergeFinished(mergeFrom,mergeInto);
            return true;
        }
    }
    protected boolean isDescendantOfAtMostThreeLevels(Node descendant,Node ancestor) {
        // The method tests ancestry, but only up to three levels.
        // Merges over more levels should not happen.
        if (descendant!=null) {
            Node descendantParent=descendant.m_parent;
            if (descendantParent==ancestor)
                return true;
            if (descendantParent!=null) {
                Node descendantParentParent=descendantParent.m_parent;
                if (descendantParentParent==ancestor)
                    return true;
                if (descendantParentParent!=null) {
                    Node descendantParentParentParent=descendantParentParent.m_parent;
                    if (descendantParentParentParent==ancestor)
                        return true;
                }
            }
        }
        return false;
    }
}
