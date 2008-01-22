package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.*;

public class AncestorBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=1075850000309773283L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingCache m_blockingCache;
    protected Tableau m_tableau;

    public AncestorBlocking(DirectBlockingChecker directBlockingChecker,BlockingCache blockingCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockingCache=blockingCache;
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
    }
    public void clear() {
    }
    public void computeBlocking() {
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            Node parent=node.getParent();
            if (parent==null)
                node.setBlocked(null,false);
            else if (parent.isBlocked())
                node.setBlocked(parent.getBlocker(),false);
            else if (m_blockingCache!=null) {
                Node blocker=m_blockingCache.getBlocker(node);
                if (blocker==null)
                    checkParentBlocking(node);
                else
                    node.setBlocked(blocker,true);
            }
            else
                checkParentBlocking(node);
            node=node.getNextTableauNode();
        }
    }
    protected final void checkParentBlocking(Node node) {
        Node blocker=node.getParent();
        while (blocker!=null) {
            if (m_directBlockingChecker.isBlockedBy(blocker,node)) {
                node.setBlocked(blocker,true);
                break;
            }
            blocker=blocker.getParent();
        }
    }
    public void nodeWillChange(Node node) {
    }
    public void modelFound() {
        if (m_blockingCache!=null) {
            computeBlocking();
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (!node.isBlocked())
                    m_blockingCache.addNode(node);
                node=node.getNextTableauNode();
            }
        }
    }
}
