/**
 * 
 */
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * @author BGlimm
 * A pairwise blocking checker that also takes into account reflexive edges. 
 */
public class PairwiseDirectBlockingCheckerWithReflexivity implements DirectBlockingChecker,Serializable {
	private static final long serialVersionUID=5592810243469921715L;

	public static final DirectBlockingChecker INSTANCE=new PairwiseDirectBlockingCheckerWithReflexivity();

    public boolean isBlockedBy(Node blocker,Node blocked) {
        return
            !blocker.isBlocked() &&
            blocker.getParent()!=null &&
            blocked.getParent()!=null &&
            blocker.getPositiveLabel()==blocked.getPositiveLabel() &&
            blocker.getParent().getPositiveLabel()==blocked.getParent().getPositiveLabel() &&
            blocker.getFromParentLabel()==blocked.getFromParentLabel() && 
            blocker.getToParentLabel()==blocked.getToParentLabel() && 
            blocker.getToSelfLabel()==blocked.getToSelfLabel(); 
    }
    public int blockingHashCode(Node node) {
        return
            node.getPositiveLabelHashCode()+
            node.getParent().getPositiveLabelHashCode()+
            node.getFromParentLabelHashCode()+
            node.getToParentLabelHashCode()+
            node.getToSelfLabelHashCode();
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public BlockingSignature getBlockingSignatureFor(Node node) {
        return new PairWiseBlockingSignatureWithReflexivity(node);
    }
    
    protected static class PairWiseBlockingSignatureWithReflexivity extends BlockingSignature implements Serializable {
		private static final long serialVersionUID=-6718642106231484602L;
		
        protected final Tableau m_tableau;
        protected final Set<Concept> m_positiveLabel;
        protected final Set<Concept> m_parentPositiveLabel;
        protected final Set<AtomicRole> m_fromParentLabel;
        protected final Set<AtomicRole> m_toParentLabel;
        protected final Set<AtomicRole> m_toSelfLabel;
        protected final int m_hashCode;
        
        public PairWiseBlockingSignatureWithReflexivity(Node node) {
            m_tableau=node.getTableau();
            m_positiveLabel=node.getPositiveLabel();
            m_tableau.getLabelManager().addConceptSetReference(m_positiveLabel);
            m_parentPositiveLabel=node.getParent().getPositiveLabel();
            m_tableau.getLabelManager().addConceptSetReference(m_parentPositiveLabel);
            m_fromParentLabel=node.getFromParentLabel();
            m_tableau.getLabelManager().addAtomicRoleSetReference(m_fromParentLabel);
            m_toParentLabel=node.getToParentLabel();
            m_tableau.getLabelManager().addAtomicRoleSetReference(m_toParentLabel);
            m_toSelfLabel=node.getToSelfLabel();
            m_tableau.getLabelManager().addAtomicRoleSetReference(m_toSelfLabel);
            m_hashCode=
                m_positiveLabel.hashCode()+
                m_parentPositiveLabel.hashCode()+
                m_fromParentLabel.hashCode()+
                m_toParentLabel.hashCode()+
                m_toSelfLabel.hashCode();
        }
        protected void finalize() {
            m_tableau.getLabelManager().removeConceptSetReference(m_positiveLabel);
            m_tableau.getLabelManager().removeConceptSetReference(m_parentPositiveLabel);
            m_tableau.getLabelManager().removeAtomicRoleSetReference(m_fromParentLabel);
            m_tableau.getLabelManager().removeAtomicRoleSetReference(m_toParentLabel);
            m_tableau.getLabelManager().removeAtomicRoleSetReference(m_toSelfLabel);
        }
        public boolean blocksNode(Node node) {
            return
                node.getPositiveLabel()==m_positiveLabel &&
                node.getParent().getPositiveLabel()==m_parentPositiveLabel &&
                node.getFromParentLabel()==m_fromParentLabel &&
                node.getToParentLabel()==m_toParentLabel &&
                node.getToSelfLabel()==m_toSelfLabel;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof PairWiseBlockingSignatureWithReflexivity))
                return false;
    		PairWiseBlockingSignatureWithReflexivity thatSignature=(PairWiseBlockingSignatureWithReflexivity)that;
    		return
    		    m_positiveLabel==thatSignature.m_positiveLabel &&
    		    m_parentPositiveLabel==thatSignature.m_parentPositiveLabel &&
    			m_fromParentLabel==thatSignature.m_fromParentLabel && 
    			m_toParentLabel==thatSignature.m_toParentLabel &&
    			m_toSelfLabel==thatSignature.m_toSelfLabel;
        }
    }
}
