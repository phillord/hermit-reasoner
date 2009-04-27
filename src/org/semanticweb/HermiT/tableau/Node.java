// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.model.ExistentialConcept;

/**
 * Represents a node in the tableau. Nodes are initially active, but can be set 
 * to merged or pruned at a later stage, which does not delete, but marks them 
 * as inaktive. 
 */
public final class Node implements Serializable {
    private static final long serialVersionUID=-2549229429321484690L;
    private static List<ExistentialConcept> NO_EXISTENTIALS=Collections.emptyList();
    public static final Node CACHE_BLOCKER=new Node(null);

    public static enum NodeState { ACTIVE,MERGED,PRUNED }
    
    protected final Tableau m_tableau;
    protected int m_nodeID;
    protected NodeState m_nodeState;
    protected Node m_parent;
    protected NodeType m_nodeType;
    protected int m_treeDepth;
    protected int m_numberOfPositiveAtomicConcepts;
    protected int m_numberOfNegatedAtomicConcepts;
    private List<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_previousTableauNode;
    protected Node m_nextTableauNode;
    protected Node m_previousMergedOrPrunedNode;
    protected Node m_mergedInto;
    protected PermanentDependencySet m_mergedIntoDependencySet;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected int m_numberOfNIAssertionsFromNode;
    protected int m_numberOfNIAssertionsToNode;
    protected int m_firstGraphOccurrenceNode;
    
    public Node(Tableau tableau) {
        m_tableau=tableau;
        m_nodeID=-1;
    }
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
        m_unprocessedExistentials=NO_EXISTENTIALS;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        m_mergedIntoDependencySet=null;
        m_blocker=null;
        m_directlyBlocked=false;
        m_numberOfNIAssertionsFromNode=0;
        m_numberOfNIAssertionsToNode=0;
        m_tableau.m_descriptionGraphManager.intializeNode(this);
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
    public int getNodeID() {
        return m_nodeID;
    }
    public Node getParent() {
        return m_parent;
    }
    public Node getClusterAnchor() {
        if (m_nodeType==NodeType.TREE_NODE)
            return this;
        else
            return m_parent;
    }
    public boolean isRootNode() {
        return m_parent==null;
    }
    public boolean isParentOf(Node potentialChild) {
        return potentialChild.m_parent==this;
    }
    public boolean isAncestorOf(Node potendialDescendant) {
        while (potendialDescendant!=null) {
            potendialDescendant=potendialDescendant.m_parent;
            if (potendialDescendant==this)
                return true;
        }
        return false;
    }
    public NodeType getNodeType() {
        return m_nodeType;
    }
    public int getTreeDepth() {
        return m_treeDepth;
    }
    public boolean isBlocked() {
        return m_blocker!=null;
    }
    public Node getBlocker() {
        return m_blocker;
    }
    public boolean isDirectlyBlocked() {
        return m_directlyBlocked;
    }
    public boolean isIndirectlyBlocked() {
        return m_blocker!=null && !m_directlyBlocked;
    }
    public void setBlocked(Node blocker,boolean directlyBlocked) {
        m_blocker=blocker;
        m_directlyBlocked=directlyBlocked;
    }
    public Object getBlockingObject() {
        return m_blockingObject;
    }
    public void setBlockingObject(Object blockingObject) {
        m_blockingObject=blockingObject;
    }
    public boolean isActive() {
        return m_nodeState==NodeState.ACTIVE;
    }
    public boolean isMerged() {
        return m_nodeState==NodeState.MERGED;
    }
    public Node getMergedInto() {
        return m_mergedInto;
    }
    public boolean isPruned() {
        return m_nodeState==NodeState.PRUNED;
    }
    public Node getPreviousTableauNode() {
        return m_previousTableauNode;
    }
    public Node getNextTableauNode() {
        return m_nextTableauNode;
    }
    public Node getCanonicalNode() {
        Node result=this;
        while (result.m_mergedInto!=null)
            result=result.m_mergedInto;
        return result;
    }
    public PermanentDependencySet addCacnonicalNodeDependencySet(DependencySet dependencySet) {
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
    public boolean hasUnprocessedExistentials() {
        return !m_unprocessedExistentials.isEmpty();
    }
    public ExistentialConcept getSomeUnprocessedExistential() {
        return m_unprocessedExistentials.get(m_unprocessedExistentials.size()-1);
    }
    public Collection<ExistentialConcept> getUnprocessedExistentials() {
        return m_unprocessedExistentials;
    }
    public String toString() {
        return String.valueOf(m_nodeID);
    }
}
