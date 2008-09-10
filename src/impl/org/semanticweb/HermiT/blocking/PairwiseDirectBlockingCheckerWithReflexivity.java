/**
 * 
 */
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.tableau.Node;

/**
 * @author BGlimm
 * A pairwise blocking checker that also takes into account reflexive edges. 
 */
public class PairwiseDirectBlockingCheckerWithReflexivity extends
		PairWiseDirectBlockingChecker {
	
	private static final long serialVersionUID = 5592810243469921715L;

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
            blocker.getToSelfLabel()==blocked.getToSelfLabel() && 
            blocker.getParent().getToSelfLabel()==blocked.getParent().getToSelfLabel();
    }
    public int blockingHashCode(Node node) {
        return node.getPositiveLabelHashCode()+node.getParent().getPositiveLabelHashCode()+node.getFromParentLabelHashCode()+node.getToParentLabelHashCode()+node.getToSelfLabelHashCode();
    }
    
    protected static class PairWiseBlockingSignatureWithReflexivity extends PairWiseBlockingSignature implements Serializable {

		private static final long serialVersionUID = -6718642106231484602L;
		
        protected final Set<AtomicRole> m_toSelfLabel;
        protected final int m_hashCode;
        
        public PairWiseBlockingSignatureWithReflexivity(Node node) {
        	super(node);
            m_toSelfLabel=node.getToSelfLabel();
            m_tableau.getLabelManager().addAtomicRoleSetReference(m_toSelfLabel);
            m_hashCode=m_positiveLabel.hashCode()+m_parentPositiveLabel.hashCode()+m_fromParentLabel.hashCode()+m_toParentLabel.hashCode()+m_toSelfLabel.hashCode();
        }
        protected void finalize() {
            super.finalize();
            m_tableau.getLabelManager().removeAtomicRoleSetReference(m_toSelfLabel);
        }
        public boolean blocksNode(Node node) {
            return super.blocksNode(node) && 
                   node.getToSelfLabel()==m_toSelfLabel ;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
        	if (this==that)
        		return true;
        	if (that instanceof PairWiseBlockingSignatureWithReflexivity) {
        		PairWiseBlockingSignatureWithReflexivity thatSignature 
        			= (PairWiseBlockingSignatureWithReflexivity)that;
        		return m_positiveLabel==thatSignature.m_positiveLabel && 
        			m_fromParentLabel==thatSignature.m_fromParentLabel && 
        			m_toParentLabel==thatSignature.m_toParentLabel &&
        			m_toSelfLabel==thatSignature.m_toSelfLabel;
        	} else {
        		return false;
        	}
        }
    }
}
