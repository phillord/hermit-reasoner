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

public class SingleDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    protected final SetFactory<AtomicConcept> m_atomicConceptsSetFactory;
    protected final List<AtomicConcept> m_atomicConceptsBuffer;
    protected Tableau m_tableau;
    protected ExtensionTable.Retrieval m_binaryTableSearch1Bound;

    public SingleDirectBlockingChecker() {
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
            ((SingleBlockingObject)blocker.getBlockingSignature()).getAtomicConceptsLabel()==((SingleBlockingObject)blocked.getBlockingSignature()).getAtomicConceptsLabel();
    }
    public int blockingHashCode(Node node) {
        return ((SingleBlockingObject)node.getBlockingSignature()).m_atomicConceptsLabelHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((SingleBlockingObject)node.getBlockingSignature()).m_hasChanged;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((SingleBlockingObject)node.getBlockingSignature()).m_hasChanged=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingSignature()==null)
            node.setBlockingSignature(new SingleBlockingObject(node));
        ((SingleBlockingObject)node.getBlockingSignature()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((SingleBlockingObject)node.getBlockingSignature()).destroy();
    }
    public Node assertionAdded(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SingleBlockingObject)node.getBlockingSignature()).addAtomicConcept((AtomicConcept)concept);
            return node;
        }
        else
            return null;
    }
    public Node assertionRemoved(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SingleBlockingObject)node.getBlockingSignature()).removeAtomicConcept((AtomicConcept)concept);
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
        return new SingleBlockingSignature(this,node);
    }
    protected Set<AtomicConcept> getAtomicConceptsLabel(Node node, boolean coreOnly) {
        m_atomicConceptsBuffer.clear();
        if (m_binaryTableSearch1Bound == null) {
            System.out.println("m_binaryTableSearch1Bound == null");
        } else if (m_binaryTableSearch1Bound.getBindingsBuffer() == null) {
            System.out.println("m_binaryTableSearch1Bound.getBindingsBuffer() == null");
        }
        if (node == null) {
            System.out.println("node == null");
        }
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept && (!coreOnly || m_binaryTableSearch1Bound.isCore()))
                m_atomicConceptsBuffer.add((AtomicConcept)concept);
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptsSetFactory.getSet(m_atomicConceptsBuffer);
        m_atomicConceptsBuffer.clear();
        return result;
    }

    protected final class SingleBlockingObject implements Serializable {
        private static final long serialVersionUID=-5439737072100509531L;
        
        protected final boolean m_useOnlyCore;
        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Set<AtomicConcept> m_atomicConceptsLabel;
        protected int m_atomicConceptsLabelHashCode;
        
        public SingleBlockingObject(Node node) {
            this(node, false);
        }
        public SingleBlockingObject(Node node, boolean useOnlyCore) {
            m_useOnlyCore=useOnlyCore;
            m_node=node;
        }
        public void initialize() {
            m_atomicConceptsLabel=null;
            m_atomicConceptsLabelHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_atomicConceptsLabel!=null) {
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                m_atomicConceptsLabel=SingleDirectBlockingChecker.this.getAtomicConceptsLabel(m_node, m_useOnlyCore);
                m_atomicConceptsSetFactory.addReference(m_atomicConceptsLabel);
            }
            return m_atomicConceptsLabel;
        }
        public void addAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                // invalidate, recompute real label later if necessary
                m_atomicConceptsSetFactory.removeReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
    }
    
    protected static class SingleBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=-7349489846772132258L;

        protected final Set<AtomicConcept> m_atomicConceptsLabel;

        public SingleBlockingSignature(SingleDirectBlockingChecker checker,Node node) {
            m_atomicConceptsLabel=((SingleBlockingObject)node.getBlockingSignature()).getAtomicConceptsLabel();
            checker.m_atomicConceptsSetFactory.addReference(m_atomicConceptsLabel);
            checker.m_atomicConceptsSetFactory.makePermanent(m_atomicConceptsLabel);
        }
        public boolean blocksNode(Node node) {
            return ((SingleBlockingObject)node.getBlockingSignature()).getAtomicConceptsLabel()==m_atomicConceptsLabel;
        }
        public int hashCode() {
            return m_atomicConceptsLabel.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof SingleBlockingSignature))
                return false;
            return m_atomicConceptsLabel==((SingleBlockingSignature)that).m_atomicConceptsLabel;
        }
    }
}
