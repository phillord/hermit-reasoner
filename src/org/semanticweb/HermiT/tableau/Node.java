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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.model.ExistentialConcept;

/**
 * Represents a node in the tableau. Nodes are initially active, but can be set
 * to merged or pruned at a later stage, which does not delete, but marks them
 * as inactive.
 */
public final class Node implements Serializable {
    private static final long serialVersionUID=-2549229429321484690L;
    private final static List<ExistentialConcept> NO_EXISTENTIALS=Collections.emptyList();
    /**Signature cache blocker.*/
    public static final Node SIGNATURE_CACHE_BLOCKER=new Node(null);

    static enum NodeState { ACTIVE,MERGED,PRUNED }

    protected final Tableau m_tableau;
    protected int m_nodeID;
    protected NodeState m_nodeState;
    protected Node m_parent;
    protected NodeType m_nodeType;
    protected int m_treeDepth;
    protected int m_numberOfPositiveAtomicConcepts;
    protected int m_numberOfNegatedAtomicConcepts;
    protected int m_numberOfNegatedRoleAssertions;
    protected List<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_previousTableauNode;
    protected Node m_nextTableauNode;
    protected Node m_previousMergedOrPrunedNode;
    protected Node m_mergedInto;
    protected PermanentDependencySet m_mergedIntoDependencySet;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected Object m_blockingCargo;
    protected int m_firstGraphOccurrenceNode;

    /**
     * @param tableau tableau
     */
    public Node(Tableau tableau) {
        m_tableau=tableau;
        m_nodeID=-1;
    }
    /**
     * @return tableau
     */
    public Tableau getTableau() {
        return m_tableau;
    }
    protected void initialize(int nodeID,Node parent,NodeType nodeType,int treeDepth) {
        assert m_nodeID==-1;
        assert m_unprocessedExistentials==null;
        m_nodeID=nodeID;
        m_nodeState=NodeState.ACTIVE;
        m_parent=parent;
        m_nodeType=nodeType;
        m_treeDepth=treeDepth;
        m_numberOfPositiveAtomicConcepts=0;
        m_numberOfNegatedAtomicConcepts=0;
        m_numberOfNegatedRoleAssertions=0;
        m_unprocessedExistentials=NO_EXISTENTIALS;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        m_mergedIntoDependencySet=null;
        m_blocker=null;
        m_directlyBlocked=false;
        DescriptionGraphManager.intializeNode(this);
    }
    protected void destroy() {
        m_nodeID=-1;
        m_nodeState=null;
        m_parent=null;
        m_nodeType=null;
        if (m_unprocessedExistentials!=NO_EXISTENTIALS) {
            m_unprocessedExistentials.clear();
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
        }
        m_unprocessedExistentials=null;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        if (m_mergedIntoDependencySet!=null) {
            m_tableau.m_dependencySetFactory.removeUsage(m_mergedIntoDependencySet);
            m_mergedIntoDependencySet=null;
        }
        m_blocker=null;
        m_tableau.m_descriptionGraphManager.destroyNode(this);
    }
    /**
     * @return node id
     */
    public int getNodeID() {
        return m_nodeID;
    }
    /**
     * @return parent
     */
    public Node getParent() {
        return m_parent;
    }
    /**
     * @return cluster anchor
     */
    public Node getClusterAnchor() {
        if (m_nodeType==NodeType.TREE_NODE)
            return this;
        else
            return m_parent;
    }
    /**
     * @return true if root node
     */
    public boolean isRootNode() {
        return m_parent==null;
    }
    /**
     * @param potentialChild potentialChild
     * @return true if parent
     */
    public boolean isParentOf(Node potentialChild) {
        return potentialChild.m_parent==this;
    }
    /**
     * @param potendialDescendant potendialDescendant
     * @return true if ancestor
     */
    public boolean isAncestorOf(Node potendialDescendant) {
        while (potendialDescendant!=null) {
            potendialDescendant=potendialDescendant.m_parent;
            if (potendialDescendant==this)
                return true;
        }
        return false;
    }
    /**
     * @return node type
     */
    public NodeType getNodeType() {
        return m_nodeType;
    }
    /**
     * @return tree depth
     */
    public int getTreeDepth() {
        return m_treeDepth;
    }
    /**
     * @return true if blocked
     */
    public boolean isBlocked() {
        return m_blocker!=null;
    }
    /**
     * @return true if directly blocked
     */
    public boolean isDirectlyBlocked() {
        return m_directlyBlocked;
    }
    /**
     * @return true if indirectly blocked
     */
    public boolean isIndirectlyBlocked() {
        return m_blocker!=null && !m_directlyBlocked;
    }
    /**
     * @return blocker
     */
    public Node getBlocker() {
        return m_blocker;
    }
    /**
     * @param blocker blocker
     * @param directlyBlocked directlyBlocked
     */
    public void setBlocked(Node blocker,boolean directlyBlocked) {
        m_blocker=blocker;
        m_directlyBlocked=directlyBlocked;
    }
    /**
     * @return a blocking object (PairwiseBlockingObject or SingleBlockingObject) that stores
     * blocking relevant information of a node such as is label.
     */
    public Object getBlockingObject() {
        return m_blockingObject;
    }
    /**
     * Stores a blocking object (PairwiseBlockingObject or SingleBlockingObject) for this node
     * that stores blocking relevant information of a node such as is label.
     * @param blockingObject blockingObject
     */
    public void setBlockingObject(Object blockingObject) {
        m_blockingObject=blockingObject;
    }
    /**
     * @return an object that should be a BlockersCache.CacheEntry and is used to
     * remove or add the object to the blockers cache even after the hash code has
     * changed due to label modifications
     */
    public Object getBlockingCargo() {
        return m_blockingCargo;
    }
    /**
     * @param blockingCargo should be an object of type BlockersCache.CacheEntry
     */
    public void setBlockingCargo(Object blockingCargo) {
        m_blockingCargo=blockingCargo;
    }
    /**
     * @return number of positive atomic concepts
     */
    public int getNumberOfPositiveAtomicConcepts() {
        return m_numberOfPositiveAtomicConcepts;
    }
    /**
     * @return true if active
     */
    public boolean isActive() {
        return m_nodeState==NodeState.ACTIVE;
    }
    /**
     * @return true if merged
     */
    public boolean isMerged() {
        return m_nodeState==NodeState.MERGED;
    }
    /**
     * @return merged into
     */
    public Node getMergedInto() {
        return m_mergedInto;
    }
    /**
     * @return merged into dep set
     */
    public PermanentDependencySet getMergedIntoDependencySet() {
        return m_mergedIntoDependencySet;
    }
    /**
     * @return true if pruned
     */
    public boolean isPruned() {
        return m_nodeState==NodeState.PRUNED;
    }
    /**
     * @return previous tableau node
     */
    public Node getPreviousTableauNode() {
        return m_previousTableauNode;
    }
    /**
     * @return next tableau node
     */
    public Node getNextTableauNode() {
        return m_nextTableauNode;
    }
    /**
     * @return canonical node
     */
    public Node getCanonicalNode() {
        Node result=this;
        while (result.m_mergedInto!=null)
            result=result.m_mergedInto;
        return result;
    }
    /**
     * @return dep set
     */
    public PermanentDependencySet getCanonicalNodeDependencySet() {
        return addCanonicalNodeDependencySet(m_tableau.m_dependencySetFactory.m_emptySet);
    }
    /**
     * @param dependencySet dependencySet
     * @return dep set
     */
    public PermanentDependencySet addCanonicalNodeDependencySet(DependencySet dependencySet) {
        PermanentDependencySet result=m_tableau.m_dependencySetFactory.getPermanent(dependencySet);
        Node node=this;
        while (node.m_mergedInto!=null) {
            result=m_tableau.m_dependencySetFactory.unionWith(result,node.m_mergedIntoDependencySet);
            node=node.m_mergedInto;
        }
        return result;
    }
    protected void addToUnprocessedExistentials(ExistentialConcept existentialConcept) {
        assert NO_EXISTENTIALS.isEmpty();
        if (m_unprocessedExistentials==NO_EXISTENTIALS) {
            m_unprocessedExistentials=m_tableau.getExistentialConceptsBuffer();
            assert m_unprocessedExistentials.isEmpty();
        }
        m_unprocessedExistentials.add(existentialConcept);
    }
    protected void removeFromUnprocessedExistentials(ExistentialConcept existentialConcept) {
        assert !m_unprocessedExistentials.isEmpty();
        if (existentialConcept==m_unprocessedExistentials.get(m_unprocessedExistentials.size()-1))
            m_unprocessedExistentials.remove(m_unprocessedExistentials.size()-1);
        else {
            boolean result=m_unprocessedExistentials.remove(existentialConcept);
            assert result;
        }
        if (m_unprocessedExistentials.isEmpty()) {
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
            m_unprocessedExistentials=NO_EXISTENTIALS;
        }
    }
    /**
     * @return true if has unprocessed existentials
     */
    public boolean hasUnprocessedExistentials() {
        return !m_unprocessedExistentials.isEmpty();
    }
    /**
     * @return some unprocessed existentials
     */
    public ExistentialConcept getSomeUnprocessedExistential() {
        return m_unprocessedExistentials.get(m_unprocessedExistentials.size()-1);
    }
    /**
     * @return unprocessed existential
     */
    public Collection<ExistentialConcept> getUnprocessedExistentials() {
        return m_unprocessedExistentials;
    }
    @Override
    public String toString() {
        return String.valueOf(m_nodeID);
    }
}
