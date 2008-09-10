// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
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
    protected Set<Concept> m_positiveLabel;
    protected int m_positiveLabelSize;
    protected int m_positiveLabelHashCode;
    protected int m_negativeLabelSize;
    protected Set<AtomicRole> m_fromParentLabel;
    protected int m_fromParentLabelHashCode;
    protected Set<AtomicRole> m_toParentLabel;
    protected int m_toParentLabelHashCode;
    protected Set<AtomicRole> m_toSelfLabel;
    protected int m_toSelfLabelHashCode;
    private List<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_previousTableauNode;
    protected Node m_nextTableauNode;
    protected Node m_previousMergedOrPrunedNode;
    protected Node m_mergedInto;
    protected PermanentDependencySet m_mergedIntoDependencySet;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected boolean m_blockingSignatureChanged;
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
        assert m_positiveLabel==null;
        assert m_fromParentLabel==null;
        assert m_toParentLabel==null;
        assert m_unprocessedExistentials==null;
        m_nodeID=nodeID;
        m_nodeState=NodeState.ACTIVE;
        m_parent=parent;
        m_nodeType=nodeType;
        m_treeDepth=treeDepth;
        m_positiveLabel=null;
        m_positiveLabelSize=0;
        m_positiveLabelHashCode=0;
        m_negativeLabelSize=0;
        m_fromParentLabel=null;
        m_fromParentLabelHashCode=0;
        m_toParentLabel=null;
        m_toParentLabelHashCode=0;
        m_toSelfLabel=null;
        m_toSelfLabelHashCode=0;
        m_unprocessedExistentials=NO_EXISTENTIALS;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        m_mergedIntoDependencySet=null;
        m_blocker=null;
        m_directlyBlocked=false;
        m_blockingObject=null;
        m_blockingSignatureChanged=false;
        m_numberOfNIAssertionsFromNode=0;
        m_numberOfNIAssertionsToNode=0;
        m_tableau.m_descriptionGraphManager.intializeNode(this);
    }
    protected void destroy() {
        m_nodeID=-1;
        m_nodeState=null;
        m_parent=null;
        m_nodeType=null;
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        if (m_toSelfLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toSelfLabel);
            m_toSelfLabel=null;
        }
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
        m_blockingObject=null;
        m_tableau.m_descriptionGraphManager.destroyNode(this);
    }
    protected void finalize() {
        if (m_positiveLabel!=null)
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
        if (m_fromParentLabel!=null)
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_fromParentLabel);
        if (m_toParentLabel!=null)
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toParentLabel);
        if (m_toSelfLabel!=null)
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toSelfLabel);
        if (m_unprocessedExistentials!=NO_EXISTENTIALS && m_unprocessedExistentials!=null) {
            m_unprocessedExistentials.clear();
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
        }
        if (m_mergedIntoDependencySet!=null)
            m_tableau.m_dependencySetFactory.removeUsage(m_mergedIntoDependencySet);
    }
    public int getNodeID() {
        return m_nodeID;
    }
    public Node getParent() {
        return m_parent;
    }
    public boolean isParentOf(Node potentialChild) {
        return potentialChild.m_parent==this;
    }
    public boolean isAncestorOf(Node potendialDescendant) {
        while (potendialDescendant!=null) {
            if (potendialDescendant==this)
                return true;
            potendialDescendant=potendialDescendant.m_parent;
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
    public boolean getBlockingSignatureChanged() {
        return m_blockingSignatureChanged;
    }
    public void setBlockingSignatureChanged(boolean blockingSignatureChanged) {
        m_blockingSignatureChanged=blockingSignatureChanged;
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
    public Set<Concept> getPositiveLabel() {
        if (m_positiveLabel==null) {
            m_positiveLabel=m_tableau.m_labelManager.getPositiveLabel(this);
            m_tableau.m_labelManager.addConceptSetReference(m_positiveLabel);
        }
        return m_positiveLabel;
    }
    public int getPositiveLabelSize() {
        return m_positiveLabelSize;
    }
    public int getPositiveLabelHashCode() {
        return m_positiveLabelHashCode;
    }
    protected void addToPositiveLabel(Concept concept) {
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        m_positiveLabelHashCode+=concept.hashCode();
        m_positiveLabelSize++;
    }
    protected void removeFromPositiveLabel(Concept concept) {
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        m_positiveLabelHashCode-=concept.hashCode();
        m_positiveLabelSize--;
    }
    public int getNegativeLabelSize() {
        return m_negativeLabelSize;
    }
    protected void addToNegativeLabel() {
        m_negativeLabelSize++;
    }
    protected void removeFromNegativeLabel() {
        m_negativeLabelSize--;
    }
    public Set<AtomicRole> getFromParentLabel() {
        if (m_fromParentLabel==null) {
            m_fromParentLabel=m_tableau.m_labelManager.getEdgeLabel(m_parent,this);
            m_tableau.m_labelManager.addAtomicRoleSetReference(m_fromParentLabel);
        }
        return m_fromParentLabel;
    }
    public int getFromParentLabelHashCode() {
        return m_fromParentLabelHashCode;
    }
    protected void addToFromParentLabel(AtomicRole atomicRole) {
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        m_fromParentLabelHashCode+=atomicRole.hashCode();
    }
    protected void removeFromFromParentLabel(AtomicRole atomicRole) {
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        m_fromParentLabelHashCode-=atomicRole.hashCode();
    }
    public Set<AtomicRole> getToParentLabel() {
        if (m_toParentLabel==null) {
            m_toParentLabel=m_tableau.m_labelManager.getEdgeLabel(this,m_parent);
            m_tableau.m_labelManager.addAtomicRoleSetReference(m_toParentLabel);
        }
        return m_toParentLabel;
    }
    public int getToParentLabelHashCode() {
        return m_toParentLabelHashCode;
    }
    protected void addToToParentLabel(AtomicRole atomicRole) {
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        m_toParentLabelHashCode+=atomicRole.hashCode();
    }
    protected void removeFromToParentLabel(AtomicRole atomicRole) {
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        m_toParentLabelHashCode-=atomicRole.hashCode();
    }
    public Set<AtomicRole> getToSelfLabel() {
        if (m_toSelfLabel==null) {
            m_toSelfLabel=m_tableau.m_labelManager.getEdgeLabel(this,this);
            m_tableau.m_labelManager.addAtomicRoleSetReference(m_toSelfLabel);
        }
        return m_toParentLabel;
    }
    public int getToSelfLabelHashCode() {
        return m_toSelfLabelHashCode;
    }
    protected void addToToSelfLabel(AtomicRole atomicRole) {
        if (m_toSelfLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toSelfLabel);
            m_toSelfLabel=null;
        }
        m_toSelfLabelHashCode+=atomicRole.hashCode();
    }
    protected void removeFromToSelfLabel(AtomicRole atomicRole) {
        if (m_toSelfLabel!=null) {
            m_tableau.m_labelManager.removeAtomicRoleSetReference(m_toSelfLabel);
            m_toSelfLabel=null;
        }
        m_toSelfLabelHashCode-=atomicRole.hashCode();
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
