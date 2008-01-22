package org.semanticweb.HermiT.tableau;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public final class Node implements Serializable {
    private static final long serialVersionUID=-2549229429321484690L;

    public static final int GLOBALLY_UNIQUE_NODE=-1;
    public static enum NodeState { NOWHERE,IN_TABLEAU,MERGED,PRUNED }
    protected static enum NodeEvent { INSERTED_INTO_TALBEAU,MERGED,PRUNED }
    
    protected final Tableau m_tableau;
    protected final int m_nodeID;
    protected final Node m_parent;
    protected final NodeType m_nodeType;
    protected final int m_treeDepth;
    protected int m_orderPosition;
    protected Set<Concept> m_positiveLabel;
    protected Set<Concept> m_negativeLabel;
    protected Set<AtomicAbstractRole> m_fromParentLabel;
    protected Set<AtomicAbstractRole> m_toParentLabel;
    protected Set<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_nextTableauNode;
    protected Node m_previousTableauNode;
    protected NodeState m_nodeState;
    protected Node m_mergedInto;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected Map<DescriptionGraph,Occurrence> m_occursInDescriptionGraphs;
    protected boolean m_occursInDescriptionGraphsDirty;
    protected int m_numberOfNIAssertionsFromNode;
    protected int m_numberOfNIAssertionsToNode;
    protected Node m_previousChangedNodeForInsert;
    protected NodeEvent m_previousChangedNodeEventForInsert;
    protected Node m_previousChangedNodeForRemove;
    protected NodeEvent m_previousChangedNodeEventForRemove;
    
    public Node(Tableau tableau,int nodeID,Node parent,NodeType nodeType,int treeDepth) {
        m_tableau=tableau;
        m_nodeID=nodeID;
        m_parent=parent;
        m_nodeType=nodeType;
        m_treeDepth=treeDepth;
        m_positiveLabel=m_tableau.m_conceptSetFactory.emptySet();
        m_tableau.m_conceptSetFactory.addReference(m_positiveLabel);
        m_negativeLabel=m_tableau.m_conceptSetFactory.emptySet();
        m_tableau.m_conceptSetFactory.addReference(m_negativeLabel);
        m_fromParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.emptySet();
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(m_fromParentLabel);
        m_toParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.emptySet();
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(m_toParentLabel);
        m_unprocessedExistentials=m_tableau.m_existentialConceptSetFactory.emptySet();
        m_tableau.m_existentialConceptSetFactory.addReference(m_unprocessedExistentials);
        m_nodeState=NodeState.NOWHERE;
    }
    protected void finalize() {
        m_tableau.m_conceptSetFactory.removeReference(m_positiveLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_negativeLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_fromParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_toParentLabel);
        m_tableau.m_existentialConceptSetFactory.removeReference(m_unprocessedExistentials);
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
    public boolean isGloballyUnique() {
        return m_treeDepth==GLOBALLY_UNIQUE_NODE;
    }
    public int getTreeDepth() {
        return m_treeDepth;
    }
    public int getOrderPosition() {
        return m_orderPosition;
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
    public boolean isMerged() {
        return m_nodeState==NodeState.MERGED;
    }
    public boolean isPruned() {
        return m_nodeState==NodeState.PRUNED;
    }
    public boolean isInTableau() {
        return m_nodeState==NodeState.IN_TABLEAU;
    }
    public Node getNextTableauNode() {
        return m_nextTableauNode;
    }
    public Node getPreviousTableauNode() {
        return m_previousTableauNode;
    }
    public Set<Concept> getPositiveLabel() {
        return m_positiveLabel;
    }
    public Set<Concept> getNegativeLabel() {
        return m_negativeLabel;
    }
    public Set<AtomicAbstractRole> getFromParentLabel() {
        return m_fromParentLabel;
    }
    public Set<AtomicAbstractRole> getToParentLabel() {
        return m_toParentLabel;
    }
    public boolean hasUnprocessedExistentials() {
        return !m_unprocessedExistentials.isEmpty();
    }
    public ExistentialConcept getSomeUnprocessedExistential() {
        return ((ProbingHashSet<ExistentialConcept>)m_unprocessedExistentials).getSomeElement();
    }
    public Set<ExistentialConcept> getUnprocessedExistentials() {
        return m_unprocessedExistentials;
    }
    protected void addToPositiveLabel(Concept concept) {
        Set<Concept> newPositiveLabel=m_tableau.m_conceptSetFactory.addElement(m_positiveLabel,concept);
        m_tableau.m_conceptSetFactory.addReference(newPositiveLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_positiveLabel);
        m_positiveLabel=newPositiveLabel;
    }
    protected void addToNegativeLabel(AtomicConcept concept) {
        Set<Concept> newNegativeLabel=m_tableau.m_conceptSetFactory.addElement(m_negativeLabel,concept);
        m_tableau.m_conceptSetFactory.addReference(newNegativeLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_negativeLabel);
        m_negativeLabel=newNegativeLabel;
    }
    protected void removeFromPositiveLabel(Concept concept) {
        Set<Concept> newPositiveLabel=m_tableau.m_conceptSetFactory.removeElement(m_positiveLabel,concept);
        m_tableau.m_conceptSetFactory.addReference(newPositiveLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_positiveLabel);
        m_positiveLabel=newPositiveLabel;
    }
    protected void removeFromNegativeLabel(AtomicConcept concept) {
        Set<Concept> newNegativeLabel=m_tableau.m_conceptSetFactory.removeElement(m_negativeLabel,concept);
        m_tableau.m_conceptSetFactory.addReference(newNegativeLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_negativeLabel);
        m_negativeLabel=newNegativeLabel;
    }
    protected void addToFromParentLabel(AtomicAbstractRole atomicAbstractRole) {
        Set<AtomicAbstractRole> newFromParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.addElement(m_fromParentLabel,atomicAbstractRole);
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(newFromParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_fromParentLabel);
        m_fromParentLabel=newFromParentLabel;
    }
    protected void removeFromFromParentLabel(AtomicAbstractRole atomicAbstractRole) {
        Set<AtomicAbstractRole> newFromParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.removeElement(m_fromParentLabel,atomicAbstractRole);
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(newFromParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_fromParentLabel);
        m_fromParentLabel=newFromParentLabel;
    }
    protected void addToToParentLabel(AtomicAbstractRole atomicAbstractRole) {
        Set<AtomicAbstractRole> newToParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.addElement(m_toParentLabel,atomicAbstractRole);
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(newToParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_toParentLabel);
        m_toParentLabel=newToParentLabel;
    }
    protected void removeFromToParentLabel(AtomicAbstractRole atomicAbstractRole) {
        Set<AtomicAbstractRole> newToParentLabel=m_tableau.m_atomicAbstractRoleSetFactory.removeElement(m_toParentLabel,atomicAbstractRole);
        m_tableau.m_atomicAbstractRoleSetFactory.addReference(newToParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_toParentLabel);
        m_toParentLabel=newToParentLabel;
    }
    protected void addToUnprocessedExistentials(ExistentialConcept existentialConcept) {
        Set<ExistentialConcept> newUnprocessedExistentials=m_tableau.m_existentialConceptSetFactory.addElement(m_unprocessedExistentials,existentialConcept);
        m_tableau.m_existentialConceptSetFactory.addReference(newUnprocessedExistentials);
        m_tableau.m_existentialConceptSetFactory.removeReference(m_unprocessedExistentials);
        m_unprocessedExistentials=newUnprocessedExistentials;
    }
    protected void removeFromUnprocessedExistentials(ExistentialConcept existentialConcept) {
        Set<ExistentialConcept> newUnprocessedExistentials=m_tableau.m_existentialConceptSetFactory.removeElement(m_unprocessedExistentials,existentialConcept);
        m_tableau.m_existentialConceptSetFactory.addReference(newUnprocessedExistentials);
        m_tableau.m_existentialConceptSetFactory.removeReference(m_unprocessedExistentials);
        m_unprocessedExistentials=newUnprocessedExistentials;
    }
    public void insertIntoTableau() {
        assert m_nodeState==NodeState.NOWHERE;
        m_tableau.m_existentialsExpansionStrategy.nodeWillChange(this);
        m_nodeState=NodeState.IN_TABLEAU;
        m_orderPosition=(++m_tableau.m_lastOrderPosition);
        // Update the node list
        m_nextTableauNode=null;
        m_previousTableauNode=m_tableau.m_lastTableauNode;
        if (m_tableau.m_firstTableauNode==null)
            m_tableau.m_firstTableauNode=this;
        else
            m_tableau.m_lastTableauNode.m_nextTableauNode=this;
        m_tableau.m_lastTableauNode=this;
        m_tableau.m_numberOfNodesInTableau++;
        // Update the change list
        m_previousChangedNodeForInsert=m_tableau.m_lastChangedNode;
        m_previousChangedNodeEventForInsert=m_tableau.m_lastChangedNodeEvent;
        m_tableau.m_lastChangedNode=this;
        m_tableau.m_lastChangedNodeEvent=NodeEvent.INSERTED_INTO_TALBEAU;
    }
    public void mergeInto(Node mergeInto) {
        assert m_nodeState==NodeState.IN_TABLEAU;
        assert m_mergedInto==null;
        m_tableau.m_existentialsExpansionStrategy.nodeWillChange(this);
        m_mergedInto=mergeInto;
        m_nodeState=NodeState.MERGED;
        // Update the node list
        if (m_previousTableauNode==null)
            m_tableau.m_firstTableauNode=m_nextTableauNode;
        else
            m_previousTableauNode.m_nextTableauNode=m_nextTableauNode;
        if (m_nextTableauNode==null)
            m_tableau.m_lastTableauNode=m_previousTableauNode;
        else
            m_nextTableauNode.m_previousTableauNode=m_previousTableauNode;
        m_tableau.m_numberOfNodesInTableau--;
        m_tableau.m_numberOfMergedOrPrunedNodes++;
        // Update the change list
        m_previousChangedNodeForRemove=m_tableau.m_lastChangedNode;
        m_previousChangedNodeEventForRemove=m_tableau.m_lastChangedNodeEvent;
        m_tableau.m_lastChangedNode=this;
        m_tableau.m_lastChangedNodeEvent=NodeEvent.MERGED;
    }
    public void prune() {
        assert m_nodeState==NodeState.IN_TABLEAU;
        assert m_mergedInto==null;
        m_tableau.m_existentialsExpansionStrategy.nodeWillChange(this);
        m_nodeState=NodeState.PRUNED;
        // Update the node list
        if (m_previousTableauNode==null)
            m_tableau.m_firstTableauNode=m_nextTableauNode;
        else
            m_previousTableauNode.m_nextTableauNode=m_nextTableauNode;
        if (m_nextTableauNode==null)
            m_tableau.m_lastTableauNode=m_previousTableauNode;
        else
            m_nextTableauNode.m_previousTableauNode=m_previousTableauNode;
        m_tableau.m_numberOfNodesInTableau--;
        m_tableau.m_numberOfMergedOrPrunedNodes++;
        // update the change list
        m_previousChangedNodeForRemove=m_tableau.m_lastChangedNode;
        m_previousChangedNodeEventForRemove=m_tableau.m_lastChangedNodeEvent;
        m_tableau.m_lastChangedNode=this;
        m_tableau.m_lastChangedNodeEvent=NodeEvent.PRUNED;
    }
    public void backtrackNodeChange() {
        assert m_tableau.m_lastChangedNode==this;
        m_tableau.m_existentialsExpansionStrategy.nodeWillChange(this);
        NodeEvent nodeEvent=m_tableau.m_lastChangedNodeEvent;
        if (nodeEvent==NodeEvent.INSERTED_INTO_TALBEAU) {
            assert m_nodeState==NodeState.IN_TABLEAU;
            assert m_mergedInto==null;
            m_nodeState=NodeState.NOWHERE;
            m_orderPosition=-1;
            // Update the node list
            if (m_previousTableauNode==null)
                m_tableau.m_firstTableauNode=m_nextTableauNode;
            else
                m_previousTableauNode.m_nextTableauNode=m_nextTableauNode;
            if (m_nextTableauNode==null)
                m_tableau.m_lastTableauNode=m_previousTableauNode;
            else
                m_nextTableauNode.m_previousTableauNode=m_previousTableauNode;
            m_tableau.m_numberOfNodesInTableau--;
            m_previousTableauNode=null;
            m_nextTableauNode=null;
            // Update the change list
            m_tableau.m_lastChangedNode=m_previousChangedNodeForInsert;
            m_tableau.m_lastChangedNodeEvent=m_previousChangedNodeEventForInsert;
            m_previousChangedNodeForInsert=null;
            m_previousChangedNodeEventForInsert=null;
        }
        else {
            assert m_nodeState==NodeState.MERGED || m_nodeState==NodeState.PRUNED;
            assert m_nodeState!=NodeState.MERGED || m_mergedInto!=null;
            assert m_nodeState!=NodeState.PRUNED || m_mergedInto==null;
            m_nodeState=NodeState.IN_TABLEAU;
            m_mergedInto=null;
            // Update the node list
            if (m_previousTableauNode==null)
                m_tableau.m_firstTableauNode=this;
            else
                m_previousTableauNode.m_nextTableauNode=this;
            if (m_nextTableauNode==null)
                m_tableau.m_lastTableauNode=this;
            else
                m_nextTableauNode.m_previousTableauNode=this;
            m_tableau.m_numberOfNodesInTableau++;
            m_tableau.m_numberOfMergedOrPrunedNodes--;
            // Update the change list
            m_tableau.m_lastChangedNode=m_previousChangedNodeForRemove;
            m_tableau.m_lastChangedNodeEvent=m_previousChangedNodeEventForRemove;
            m_previousChangedNodeForRemove=null;
            m_previousChangedNodeEventForRemove=null;
        }
        m_occursInDescriptionGraphsDirty=true;
    }
    public Node getCanonicalNode() {
        Node result=this;
        while (result.m_mergedInto!=null)
            result=result.m_mergedInto;
        return result;
    }
    public Occurrence addOccurenceInGraph(DescriptionGraph descriptionGraph,int position,int tupleIndex) {
        if (m_occursInDescriptionGraphs==null)
            m_occursInDescriptionGraphs=new HashMap<DescriptionGraph,Occurrence>();
        Occurrence lastOccurrence=m_occursInDescriptionGraphs.get(descriptionGraph);
        Occurrence newOccurrence=new Occurrence(position,tupleIndex,lastOccurrence);
        m_occursInDescriptionGraphs.put(descriptionGraph,newOccurrence);
        m_occursInDescriptionGraphsDirty=true;
        return newOccurrence;
    }
    public void removeOccurrenceInTuple(DescriptionGraph descriptionGraph,int tupleIndex,int position) {
        if (m_occursInDescriptionGraphs!=null) {
            Occurrence lastOccurrence=null;
            Occurrence occurrence=m_occursInDescriptionGraphs.get(descriptionGraph);
            while (occurrence!=null) {
                if (occurrence.m_tupleIndex==tupleIndex && occurrence.m_position==position) {
                    if (lastOccurrence==null)
                        m_occursInDescriptionGraphs.put(descriptionGraph,occurrence.m_next);
                    else
                        lastOccurrence.m_next=occurrence.m_next;
                    return;
                }
                occurrence=occurrence.m_next;
            }
        }
    }
    public String toString() {
        return String.valueOf(m_nodeID);
    }
    
    public static class Occurrence implements Serializable {
        private static final long serialVersionUID=-3146602839694560335L;

        public final int m_position;
        public final int m_tupleIndex;
        public Occurrence m_next;
        
        public Occurrence(int position,int tupleIndex,Occurrence next) {
            m_position=position;
            m_tupleIndex=tupleIndex;
            m_next=next;
        }
    }
}
