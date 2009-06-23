// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

public class TwoPhaseDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID = -5003606007272321326L;
    
    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    
    public TwoPhaseDirectBlockingChecker() {
        m_atomicConceptsSetFactory=new SetFactory<AtomicConcept>();
        m_atomicConceptsBuffer=new ArrayList<AtomicConcept>();
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_binaryTableSearch1Bound=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
    }
    public void clear() {
        m_atomicConceptsSetFactory.clearNonpermanent();
    }
    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            ((TwoPhaseBlockingObject)blocker.getBlockingObject()).getAtomicConceptLabel()==((TwoPhaseBlockingObject)blocked.getBlockingObject()).getAtomicConceptLabel();
    }
    public int blockingHashCode(Node node) {
        return ((TwoPhaseBlockingObject)node.getBlockingObject()).m_atomicConceptsHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((TwoPhaseBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((TwoPhaseBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new TwoPhaseBlockingObject(node));
        ((TwoPhaseBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((TwoPhaseBlockingObject)node.getBlockingObject()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((TwoPhaseBlockingObject)node.getBlockingObject()).addConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionRemoved(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((TwoPhaseBlockingObject)node.getBlockingObject()).removeConcept((AtomicConcept)concept);
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
        return null; 
    }
    protected Set<AtomicConcept> getAtomicConcepts(Node node) {
        m_atomicConceptsBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept) {
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            }
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }
    protected final class TwoPhaseBlockingObject implements Serializable {
        private static final long serialVersionUID = -8515134126252287921L;
        
        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Node m_greatestInvalidBlocker;
        protected Set<AtomicConcept> m_atomicConcepts;
        protected int m_atomicConceptsHashCode;
        
        public TwoPhaseBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_atomicConcepts=null;
            m_atomicConceptsHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_atomicConcepts!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConcepts);
                m_atomicConcepts=null;
            }
        }        
        public Set<AtomicConcept> getAtomicConceptLabel() {
            if (m_atomicConcepts==null) {
                m_atomicConcepts=TwoPhaseDirectBlockingChecker.this.getAtomicConcepts(m_node);
                m_atomicConceptsSetFactory.addReference(m_atomicConcepts);
            }
            return m_atomicConcepts;
        }
        public void addConcept(AtomicConcept atomicConcept) {
            if (m_atomicConcepts!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConcepts);
                m_atomicConcepts=null;
            }
            m_atomicConceptsHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeConcept(AtomicConcept atomicConcept) {
            if (m_atomicConcepts!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConcepts);
                m_atomicConcepts=null;
            }
            m_atomicConceptsHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void setGreatestInvalidBlocker(Node node) {
            m_greatestInvalidBlocker = node;
        }
    }
}
