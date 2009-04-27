// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.LabelManager;

public class SingleDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    public static final DirectBlockingChecker INSTANCE=new SingleDirectBlockingChecker();

    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getNodeType()==NodeType.TREE_NODE &&
            blocked.getNodeType()==NodeType.TREE_NODE &&
            ((SingleBlockingObject)blocker.getBlockingObject()).getAtomicConceptsLabel()==((SingleBlockingObject)blocked.getBlockingObject()).getAtomicConceptsLabel();
    }
    public int blockingHashCode(Node node) {
        return ((SingleBlockingObject)node.getBlockingObject()).m_atomicConceptsLabelHashCode;
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean hasBlockingInfoChanged(Node node) {
        return ((SingleBlockingObject)node.getBlockingObject()).m_hasChanged;
    }
    public void clearBlockingInfoChanged(Node node) {
        ((SingleBlockingObject)node.getBlockingObject()).m_hasChanged=false;
    }
    public void nodeInitialized(Node node) {
        if (node.getBlockingObject()==null)
            node.setBlockingObject(new SingleBlockingObject(node));
        ((SingleBlockingObject)node.getBlockingObject()).initialize();
    }
    public void nodeDestroyed(Node node) {
        ((SingleBlockingObject)node.getBlockingObject()).destroy();
    }
    public boolean assertionAdded(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SingleBlockingObject)node.getBlockingObject()).addAtomicConcept((AtomicConcept)concept);
            return true;
        }
        else
            return false;
    }
    public boolean assertionRemoved(Concept concept,Node node) {
        if (concept instanceof AtomicConcept) {
            ((SingleBlockingObject)node.getBlockingObject()).removeAtomicConcept((AtomicConcept)concept);
            return true;
        }
        else
            return false;
    }
    public Node assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        return null;
    }
    public Node assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        return null;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new SingleBlockingSignature(node);
    }
    
    protected static final class SingleBlockingObject implements Serializable {
        private static final long serialVersionUID=-5439737072100509531L;

        protected final Node m_node;
        protected boolean m_hasChanged;
        protected Set<AtomicConcept> m_atomicConceptsLabel;
        protected int m_atomicConceptsLabelHashCode;

        public SingleBlockingObject(Node node) {
            m_node=node;
        }
        public void initialize() {
            m_atomicConceptsLabel=null;
            m_atomicConceptsLabelHashCode=0;
            m_hasChanged=true;
        }
        public void destroy() {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
        }
        public Set<AtomicConcept> getAtomicConceptsLabel() {
            if (m_atomicConceptsLabel==null) {
                LabelManager labelManager=m_node.getTableau().getLabelManager();
                m_atomicConceptsLabel=labelManager.getPositiveLabel(m_node);
                labelManager.addAtomicConceptSetReference(m_atomicConceptsLabel);
            }
            return m_atomicConceptsLabel;
        }
        public void addAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode+=atomicConcept.hashCode();
            m_hasChanged=true;
        }
        public void removeAtomicConcept(AtomicConcept atomicConcept) {
            if (m_atomicConceptsLabel!=null) {
                m_node.getTableau().getLabelManager().removeAtomicConceptSetReference(m_atomicConceptsLabel);
                m_atomicConceptsLabel=null;
            }
            m_atomicConceptsLabelHashCode-=atomicConcept.hashCode();
            m_hasChanged=true;
        }
    }
    
    protected static class SingleBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=-7349489846772132258L;

        protected final Set<AtomicConcept> m_atomicConceptsLabel;

        public SingleBlockingSignature(Node node) {
            m_atomicConceptsLabel=((SingleBlockingObject)node.getBlockingObject()).getAtomicConceptsLabel();
            LabelManager labelManager=node.getTableau().getLabelManager();
            labelManager.addAtomicConceptSetReference(m_atomicConceptsLabel);
            labelManager.makePermanentAtomicConceptSet(m_atomicConceptsLabel);
        }
        public boolean blocksNode(Node node) {
            return ((SingleBlockingObject)node.getBlockingObject()).getAtomicConceptsLabel()==m_atomicConceptsLabel;
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
