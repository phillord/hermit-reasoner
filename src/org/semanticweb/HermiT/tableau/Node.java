package org.semanticweb.HermiT.tableau;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public final class Node implements Serializable {
    private static final long serialVersionUID=-2549229429321484690L;
    public static final Node CACHE_BLOCKER=new Node(null);

    public static final int GLOBALLY_UNIQUE_NODE=-1;
    public static enum NodeState { ACTIVE,MERGED,PRUNED }
    
    protected final Tableau m_tableau;
    protected int m_nodeID;
    protected NodeState m_nodeState;
    protected Node m_parent;
    protected NodeType m_nodeType;
    protected int m_treeDepth;
    protected Set<Concept> m_positiveLabel;
    protected Set<Concept> m_negativeLabel;
    protected Set<AtomicAbstractRole> m_fromParentLabel;
    protected Set<AtomicAbstractRole> m_toParentLabel;
    protected Set<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_previousTableauNode;
    protected Node m_nextTableauNode;
    protected Node m_previousMergedOrPrunedNode;
    protected Node m_mergedInto;
    protected DependencySet m_mergedIntoDependencySet;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected boolean m_blockingSignatureChanged;
    protected Map<DescriptionGraph,Occurrence> m_occursInDescriptionGraphs;
    protected boolean m_occursInDescriptionGraphsDirty;
    protected int m_numberOfNIAssertionsFromNode;
    protected int m_numberOfNIAssertionsToNode;
    
    public Node(Tableau tableau) {
        m_tableau=tableau;
        m_nodeID=-1;
    }
    protected void initialize(int nodeID,Node parent,NodeType nodeType,int treeDepth) {
        assert m_nodeID==-1;
        assert m_positiveLabel==null;
        assert m_negativeLabel==null;
        assert m_fromParentLabel==null;
        assert m_toParentLabel==null;
        assert m_unprocessedExistentials==null;
        m_nodeID=nodeID;
        m_nodeState=NodeState.ACTIVE;
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
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        m_mergedIntoDependencySet=null;
        m_blocker=null;
        m_directlyBlocked=false;
        m_blockingObject=null;
        m_blockingSignatureChanged=false;
        m_occursInDescriptionGraphs=null;
        m_occursInDescriptionGraphsDirty=false;
        m_numberOfNIAssertionsFromNode=0;
        m_numberOfNIAssertionsToNode=0;
    }
    protected void destroy() {
        m_tableau.m_conceptSetFactory.removeReference(m_positiveLabel);
        m_tableau.m_conceptSetFactory.removeReference(m_negativeLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_fromParentLabel);
        m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_toParentLabel);
        m_tableau.m_existentialConceptSetFactory.removeReference(m_unprocessedExistentials);
        m_nodeID=-1;
        m_nodeState=null;
        m_parent=null;
        m_nodeType=null;
        m_positiveLabel=null;
        m_negativeLabel=null;
        m_fromParentLabel=null;
        m_toParentLabel=null;
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
        m_occursInDescriptionGraphs=null;
    }
    protected void finalize() {
        if (m_positiveLabel!=null)
            m_tableau.m_conceptSetFactory.removeReference(m_positiveLabel);
        if (m_negativeLabel!=null)
            m_tableau.m_conceptSetFactory.removeReference(m_negativeLabel);
        if (m_fromParentLabel!=null)
            m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_fromParentLabel);
        if (m_toParentLabel!=null)
            m_tableau.m_atomicAbstractRoleSetFactory.removeReference(m_toParentLabel);
        if (m_unprocessedExistentials!=null)
            m_tableau.m_existentialConceptSetFactory.removeReference(m_unprocessedExistentials);
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
    public boolean isGloballyUnique() {
        return m_treeDepth==GLOBALLY_UNIQUE_NODE;
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
    public DependencySet addCacnonicalNodeDependencySet(DependencySet dependencySet) {
        Node result=this;
        while (result.m_mergedInto!=null) {
            dependencySet=m_tableau.m_dependencySetFactory.unionWith(dependencySet,result.m_mergedIntoDependencySet);
            result=result.m_mergedInto;
        }
        return dependencySet;
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
