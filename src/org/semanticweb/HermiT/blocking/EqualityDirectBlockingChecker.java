package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.*;

public class EqualityDirectBlockingChecker implements DirectBlockingChecker,Serializable {
    private static final long serialVersionUID=9093753046859877016L;

    public static final DirectBlockingChecker INSTANCE=new EqualityDirectBlockingChecker();

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
}
