package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

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
        return node.getPositiveLabel().hashCode()+node.getParent().getPositiveLabel().hashCode()+node.getFromParentLabel().hashCode()+node.getToParentLabel().hashCode();
    }
    public boolean canBeBlocker(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
    public boolean canBeBlocked(Node node) {
        return node.getNodeType()==NodeType.TREE_NODE;
    }
}
