// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;

public class SingleDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    public static final DirectBlockingChecker INSTANCE=new SingleDirectBlockingChecker();

    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getParent()!=null &&
            blocker.getPositiveLabel()==blocked.getPositiveLabel();
    }
    public int blockingHashCode(Node node) {
        return node.getPositiveLabelHashCode();
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new SingleBlockingSignature(node);
    }
    
    protected static class SingleBlockingSignature extends BlockingSignature implements Serializable {
        private static final long serialVersionUID=-7349489846772132258L;

        protected final Set<AtomicConcept> m_positiveLabel;

        public SingleBlockingSignature(Node node) {
            m_positiveLabel=node.getPositiveLabel();
            node.getTableau().getLabelManager().makePermanentAtomicConceptSet(m_positiveLabel);
        }
        public boolean blocksNode(Node node) {
            return node.getPositiveLabel()==m_positiveLabel;
        }
        public int hashCode() {
            return m_positiveLabel.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof SingleBlockingSignature))
                return false;
            return m_positiveLabel==((SingleBlockingSignature)that).m_positiveLabel;
        }
    }
}
