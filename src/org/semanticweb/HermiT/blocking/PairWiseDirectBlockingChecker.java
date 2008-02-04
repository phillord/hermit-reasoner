package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker.SingleBlockingSignature;
import org.semanticweb.HermiT.model.AtomicAbstractRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.*;

public class PairWiseDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=-8296420442452625109L;

    public static final DirectBlockingChecker INSTANCE=new PairWiseDirectBlockingChecker();

    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getParent()!=null &&
            blocked.getParent()!=null &&
            blocker.getPositiveLabel()==blocked.getPositiveLabel() &&
            blocker.getParent().getPositiveLabel()==blocked.getParent().getPositiveLabel() &&
            blocker.getFromParentLabel()==blocked.getFromParentLabel() && 
            blocker.getToParentLabel()==blocked.getToParentLabel();
    }
    public int blockingHashCode(Node node) {
        return node.getPositiveLabelHashCode()+node.getParent().getPositiveLabelHashCode()+node.getFromParentLabelHashCode()+node.getToParentLabelHashCode();
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new PairWiseBlockingSignature(node);
    }

    protected static class PairWiseBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=4697990424058632618L;

        protected final Tableau m_tableau;
        protected final Set<Concept> m_positiveLabel;
        protected final Set<Concept> m_parentPositiveLabel;
        protected final Set<AtomicAbstractRole> m_fromParentLabel;
        protected final Set<AtomicAbstractRole> m_toParentLabel;
        protected final int m_hashCode;

        public PairWiseBlockingSignature(Node node) {
            m_tableau=node.getTableau();
            m_positiveLabel=node.getPositiveLabel();
            m_tableau.getLabelManager().addConceptSetReference(m_positiveLabel);
            m_parentPositiveLabel=node.getParent().getPositiveLabel();
            m_tableau.getLabelManager().addConceptSetReference(m_parentPositiveLabel);
            m_fromParentLabel=node.getFromParentLabel();
            m_tableau.getLabelManager().addAtomicAbstractRoleSetReference(m_fromParentLabel);
            m_toParentLabel=node.getToParentLabel();
            m_tableau.getLabelManager().addAtomicAbstractRoleSetReference(m_toParentLabel);
            m_hashCode=m_positiveLabel.hashCode()+m_parentPositiveLabel.hashCode()+m_fromParentLabel.hashCode()+m_toParentLabel.hashCode();
        }
        protected void finalize() {
            m_tableau.getLabelManager().removeConceptSetReference(m_positiveLabel);
            m_tableau.getLabelManager().removeConceptSetReference(m_parentPositiveLabel);
            m_tableau.getLabelManager().removeAtomicAbstractRoleSetReference(m_fromParentLabel);
            m_tableau.getLabelManager().removeAtomicAbstractRoleSetReference(m_toParentLabel);
        }
        public boolean blocksNode(Node node) {
            return node.getPositiveLabel()==m_positiveLabel && node.getParent().getPositiveLabel()==m_parentPositiveLabel && node.getFromParentLabel()==m_fromParentLabel && node.getToParentLabel()==m_toParentLabel;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof SingleBlockingSignature))
                return false;
            PairWiseBlockingSignature thatSignature=(PairWiseBlockingSignature)that;
            return m_positiveLabel==thatSignature.m_positiveLabel && m_fromParentLabel==thatSignature.m_fromParentLabel && m_toParentLabel==thatSignature.m_toParentLabel;
        }
    }
}
