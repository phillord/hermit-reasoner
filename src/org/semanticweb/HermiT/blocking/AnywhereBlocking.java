package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class AnywhereBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingCache m_blockingCache;
    protected final BlockingCache m_currentModelCache;
    protected Tableau m_tableau;
    protected Node m_firstChangedNode;

    public AnywhereBlocking(DirectBlockingChecker directBlockingChecker,BlockingCache blockingCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_blockingCache=blockingCache;
        m_currentModelCache=new BlockingCache(directBlockingChecker);
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
    }
    public void clear() {
        m_currentModelCache.clear();
        m_firstChangedNode=null;
    }
    public void computeBlocking() {
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                BlockingCache.CacheEntry cacheEntry=(BlockingCache.CacheEntry)node.getBlockingObject();
                if (cacheEntry!=null) {
                    m_currentModelCache.removeCacheEntry(cacheEntry);
                    node.setBlockingObject(null);
                }
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            boolean checkBlockingCache=(m_blockingCache!=null && !m_blockingCache.isEmpty());
            while (node!=null) {
                if (node.isActive() && (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node))) {
                    if (node.getBlockingSignatureChanged() || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false);
                        else if (parent.isBlocked())
                            node.setBlocked(parent,false);
                        else if (checkBlockingCache) {
                            Node blocker=m_blockingCache.getBlocker(node);
                            if (blocker==null)
                                blocker=m_currentModelCache.getBlocker(node);
                            else
                                blocker=Node.CACHE_BLOCKER;
                            node.setBlocked(blocker,blocker!=null);
                        }
                        else {
                            Node blocker=m_currentModelCache.getBlocker(node);
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node)) {
                            assert node.getBlockingObject()==null;
                            BlockingCache.CacheEntry updateNodeCacheEntry=m_currentModelCache.addNode(node);
                            node.setBlockingObject(updateNodeCacheEntry);
                        }
                    }
                }
                node.setBlockingSignatureChanged(false);
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    public void assertionAdded(Concept concept,Node node) {
        if (node.getNodeType()==NodeType.TREE_NODE && (concept instanceof AtomicConcept || concept instanceof ExistentialConcept))
            updateNodeChange(node);
    }
    public void assertionRemoved(Concept concept,Node node) {
        if (node.getNodeType()==NodeType.TREE_NODE && (concept instanceof AtomicConcept || concept instanceof ExistentialConcept))
            updateNodeChange(node);
    }
    public void assertionAdded(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        if (nodeTo.getNodeType()==NodeType.TREE_NODE && nodeFrom.isParentOf(nodeTo))
            updateNodeChange(nodeTo);
        else if (nodeFrom.getNodeType()==NodeType.TREE_NODE && nodeTo.isParentOf(nodeFrom))
            updateNodeChange(nodeFrom);
    }
    public void assertionRemoved(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        if (nodeTo.getNodeType()==NodeType.TREE_NODE && nodeFrom.isParentOf(nodeTo))
            updateNodeChange(nodeTo);
        else if (nodeFrom.getNodeType()==NodeType.TREE_NODE && nodeTo.isParentOf(nodeFrom))
            updateNodeChange(nodeFrom);
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        node.setBlockingSignatureChanged(true);
        if (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID())
            m_firstChangedNode=node;
    }
    public void nodeDestroyed(Node node) {
        BlockingCache.CacheEntry cacheEntry=(BlockingCache.CacheEntry)node.getBlockingObject();
        if (cacheEntry!=null)
            m_currentModelCache.removeCacheEntry(cacheEntry);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
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
