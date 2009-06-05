// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class CorePreDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected final SetFactory<AtomicRole> m_atomicRolesSetFactory;
    protected final List<AtomicRole> m_atomicRolesBuffer;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected ExtensionTable.Retrieval m_ternaryTableSearch12Bound;
    
    public CorePreDirectBlockingChecker() {
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
        m_atomicRolesSetFactory=new SetFactory<AtomicRole>();
        m_atomicRolesBuffer=new ArrayList<AtomicRole>();
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_binaryTableSearch1Bound=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearch12Bound=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_atomicConceptsSetFactory.clearNonpermanent();
    }
    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            ((SinglePreCoreBlockingObject)blocker.getBlockingObject()).getCoreConcepts()==((SinglePreCoreBlockingObject)blocked.getBlockingObject()).getCoreConcepts();
    }
    public int blockingHashCode(Node node) {
        return ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_coreConceptsHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new SinglePreCoreBlockingObject(node));
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((SinglePreCoreBlockingObject)node.getBlockingObject()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SinglePreCoreBlockingObject)node.getBlockingObject()).addCoreConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionRemoved(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SinglePreCoreBlockingObject)node.getBlockingObject()).removeCoreConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        return null;
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        return null;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return null; //new SinglePreCoreBlockingSignature(this,node);
    }
    protected Set<AtomicConcept> getCoreConcepts(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept && m_binaryTableSearch1Bound.isCore()) {
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            }
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
    protected Set<AtomicConcept> getNonCoreConcepts(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept && !m_binaryTableSearch1Bound.isCore()) {
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            }
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
//    protected Set<AtomicRole> getEdgeLabel(Node nodeFrom,Node nodeTo) {
//        m_atomicRolesBuffer.clear();
//        m_ternaryTableSearch12Bound.getBindingsBuffer()[1]=nodeFrom;
//        m_ternaryTableSearch12Bound.getBindingsBuffer()[2]=nodeTo;
//        m_ternaryTableSearch12Bound.open();
//        Object[] tupleBuffer=m_ternaryTableSearch12Bound.getTupleBuffer();
//        while (!m_ternaryTableSearch12Bound.afterLast()) {
//            Object atomicRole=tupleBuffer[0];
//            if (atomicRole instanceof AtomicRole && m_binaryTableSearch1Bound.isCore())
//                m_atomicRolesBuffer.add((AtomicRole)atomicRole);
//            m_ternaryTableSearch12Bound.next();
//        }
//        Set<AtomicRole> result=m_atomicRolesSetFactory.getSet(m_atomicRolesBuffer);
//        m_atomicRolesBuffer.clear();
//        return result;
//    }
    
    protected final class SinglePreCoreBlockingObject implements Serializable {
        private static final long serialVersionUID=-5439737072100509531L;
        
        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Node m_greatestInvalidBlocker;
        protected Set<AtomicConcept> m_coreConcepts;
        protected int m_coreConceptsHashCode;
        
        public SinglePreCoreBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_coreConcepts=null;
            m_coreConceptsHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_coreConcepts!=null) {
                m_atomicConceptsSetFactory.removeReference(m_coreConcepts);
                m_coreConcepts=null;
            }
        }        
        public Set<AtomicConcept> getCoreConcepts() {
            if (m_coreConcepts==null) {
                m_coreConcepts=CorePreDirectBlockingChecker.this.getCoreConcepts(m_node);
                m_atomicConceptsSetFactory.addReference(m_coreConcepts);
            }
            return m_coreConcepts;
        }
        public Set<AtomicConcept> getAtomicConceptLabel() {
            Set<AtomicConcept> label = new HashSet<AtomicConcept>();
            label.addAll(this.getCoreConcepts());
            label.addAll(CorePreDirectBlockingChecker.this.getNonCoreConcepts(m_node));
            return label;
        }
        public void addCoreConcept(AtomicConcept atomicConcept) {
            if (m_coreConcepts!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_coreConcepts);
                m_coreConcepts=null;
            }
            m_coreConceptsHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeCoreConcept(AtomicConcept atomicConcept) {
            if (m_coreConcepts!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_coreConcepts);
                m_coreConcepts=null;
            }
            m_coreConceptsHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void setGreatestInvalidBlocker(Node node) {
            m_greatestInvalidBlocker = node;
        }
    }
//    
//    protected static class SinglePreCoreBlockingSignature extends BlockingSignature implements Serializable {
//        private static final long serialVersionUID=-7349489846772132258L;
//
//        protected final Set<AtomicConcept> m_coreConcepts;
//        protected final Set<AtomicConcept> m_NonCoreConcepts;
//        protected final Set<AtomicConcept> m_parentAtomicConceptLabel;
//        protected final Set<AtomicRole> m_fromParentLabel;
//        protected final Set<AtomicRole> m_toParentLabel;
//
//        public SinglePreCoreBlockingSignature(CorePreDirectBlockingChecker checker,Node node) {
//            m_coreConcepts=((SinglePreCoreBlockingObject)node.getBlockingObject()).getCoreConcepts();
//            m_NonCoreConcepts=((SinglePreCoreBlockingObject)node.getBlockingObject()).getNonCoreConcepts();
//            m_parentAtomicConceptLabel=((SinglePreCoreBlockingObject)node.getParent().getBlockingObject()).getNonCoreConcepts();
//            m_fromParentLabel=((SinglePreCoreBlockingObject)node.getBlockingObject()).getFromParentLabel();
//            m_toParentLabel=((SinglePreCoreBlockingObject)node.getBlockingObject()).getToParentLabel();
//            checker.m_atomicConceptsSetFactory.addReference(m_coreConcepts);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_coreConcepts);
//            checker.m_atomicConceptsSetFactory.addReference(m_NonCoreConcepts);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_NonCoreConcepts);
//            checker.m_atomicConceptsSetFactory.addReference(m_parentAtomicConceptLabel);
//            checker.m_atomicConceptsSetFactory.makePermanent(m_parentAtomicConceptLabel);
//            checker.m_atomicRolesSetFactory.addReference(m_fromParentLabel);
//            checker.m_atomicRolesSetFactory.makePermanent(m_fromParentLabel);
//            checker.m_atomicRolesSetFactory.addReference(m_toParentLabel);
//            checker.m_atomicRolesSetFactory.makePermanent(m_toParentLabel);
//        }
//        public boolean blocksNode(Node node) {
//            return ((SinglePreCoreBlockingObject)node.getBlockingObject()).getCoreConcepts()==m_coreConcepts;
//        }
//        public int hashCode() {
//            return m_coreConcepts.hashCode();
//        }
//        public boolean equals(Object that) {
//            if (this==that)
//                return true;
//            if (!(that instanceof SinglePreCoreBlockingSignature))
//                return false;
//            return m_coreConcepts==((SinglePreCoreBlockingSignature)that).m_coreConcepts;
//        }
//    }
}
