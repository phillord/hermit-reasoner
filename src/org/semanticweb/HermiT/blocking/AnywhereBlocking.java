package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.*;

public class AnywhereBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingCache m_blockingCache;
    protected final BlockingCache m_currentModelCache;
    protected Tableau m_tableau;
    protected Node m_firstUnchangedNode;
    protected BlockingCache.CacheEntry m_firstBlocker;

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
        m_firstUnchangedNode=null;
        m_firstBlocker=null;
    }
    public void computeBlocking() {
        while (m_firstUnchangedNode!=null && m_firstUnchangedNode.getBlockingObject()==null)
            m_firstUnchangedNode=m_firstUnchangedNode.getPreviousTableauNode();
        BlockingCache.CacheEntry lastEntry;
        BlockingCache.CacheEntry removeEntry;
        if (m_firstUnchangedNode==null) {
            lastEntry=null;
            removeEntry=m_firstBlocker;
        }
        else {
            lastEntry=(BlockingCache.CacheEntry)m_firstUnchangedNode.getBlockingObject();
            removeEntry=lastEntry.m_nextBlocker;
        }
        while (removeEntry!=null) {
            m_currentModelCache.removeCacheEntry(removeEntry);
            removeEntry.m_node.setBlockingObject(null);
            if (lastEntry!=null)
                lastEntry.m_nextBlocker=null;
            lastEntry=removeEntry;
            removeEntry=removeEntry.m_nextBlocker;
        }
        BlockingCache.CacheEntry lastBlocker;
        Node updateNode;
        if (m_firstUnchangedNode==null) {
            lastBlocker=null;
            updateNode=m_tableau.getFirstTableauNode();
            m_firstBlocker=null;
        }
        else {
            lastBlocker=(BlockingCache.CacheEntry)m_firstUnchangedNode.getBlockingObject();
            updateNode=m_firstUnchangedNode.getNextTableauNode();
        }
        boolean checkBlockingCache=(m_blockingCache!=null && !m_blockingCache.isEmpty());
        while (updateNode!=null) {
            Node parent=updateNode.getParent();
            if (parent==null)
                updateNode.setBlocked(null,false);
            else if (parent.isBlocked())
                updateNode.setBlocked(parent.getBlocker(),false);
            else if (checkBlockingCache) {
                Node blocker=m_blockingCache.getBlocker(updateNode);
                if (blocker==null)
                    blocker=m_currentModelCache.getBlocker(updateNode);
                updateNode.setBlocked(blocker,blocker!=null);
            }
            else {
                Node blocker=m_currentModelCache.getBlocker(updateNode);
                updateNode.setBlocked(blocker,blocker!=null);
            }
            if (!updateNode.isBlocked() && m_directBlockingChecker.canBeBlocker(updateNode)) {
                BlockingCache.CacheEntry updateNodeCacheEntry=m_currentModelCache.addNode(updateNode);
                if (m_firstBlocker==null)
                    m_firstBlocker=updateNodeCacheEntry;
                updateNode.setBlockingObject(updateNodeCacheEntry);
                if (lastBlocker!=null)
                    lastBlocker.m_nextBlocker=updateNodeCacheEntry;
                lastBlocker=updateNodeCacheEntry;
            }
            updateNode=updateNode.getNextTableauNode();
        }
        m_firstUnchangedNode=m_tableau.getLastTableauNode();
    }
    public void nodeWillChange(Node node) {
        if (node.isInTableau() && m_firstUnchangedNode!=null && node.getOrderPosition()<=m_firstUnchangedNode.getOrderPosition())
            m_firstUnchangedNode=node.getPreviousTableauNode();
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
