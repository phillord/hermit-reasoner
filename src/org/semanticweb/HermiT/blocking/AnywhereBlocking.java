// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class AnywhereBlocking implements BlockingStrategy,Serializable {
    private static final long serialVersionUID=-2959900333817197464L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected final BlockingCache m_currentBlockersCache;
    protected final BlockingSignatureCache m_blockingSignatureCache;
    protected Tableau m_tableau;
    protected Node m_firstChangedNode;

    public AnywhereBlocking(DirectBlockingChecker directBlockingChecker,BlockingSignatureCache blockingSignatureCache) {
        m_directBlockingChecker=directBlockingChecker;
        m_currentBlockersCache=new BlockingCache(m_directBlockingChecker);
        m_blockingSignatureCache=blockingSignatureCache;
    }
    public void initialize(Tableau tableau) {
        m_tableau=tableau;
        m_directBlockingChecker.initialize(tableau);
    }
    public void clear() {
        m_currentBlockersCache.clear();
        m_firstChangedNode=null;
        m_directBlockingChecker.clear();
    }
    public void computeBlocking(boolean finalChance) {
        if (m_firstChangedNode!=null) {
            Node node=m_firstChangedNode;
            while (node!=null) {
                if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                    m_currentBlockersCache.removeNode(node);
                node=node.getNextTableauNode();
            }
            node=m_firstChangedNode;
            boolean checkBlockingSignatureCache=(m_blockingSignatureCache!=null && !m_blockingSignatureCache.isEmpty());
            while (node!=null) {
                if (node.isActive() && (m_directBlockingChecker.canBeBlocked(node) || m_directBlockingChecker.canBeBlocker(node))) {
                    if (m_directBlockingChecker.hasBlockingInfoChanged(node) || !node.isDirectlyBlocked() || node.getBlocker().getNodeID()>=m_firstChangedNode.getNodeID()) {
                        Node parent=node.getParent();
                        if (parent==null)
                            node.setBlocked(null,false);
                        else if (parent.isBlocked())
                            node.setBlocked(parent,false);
                        else if (checkBlockingSignatureCache) {
                            if (m_blockingSignatureCache.containsSignature(node))
                                node.setBlocked(Node.CACHE_BLOCKER,true);
                            else {
                                Node blocker=m_currentBlockersCache.getBlocker(node);
                                node.setBlocked(blocker,blocker!=null);
                            }
                        }
                        else {
                            Node blocker=m_currentBlockersCache.getBlocker(node);
                            node.setBlocked(blocker,blocker!=null);
                        }
                        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                            m_currentBlockersCache.addNode(node);
                    }
                    m_directBlockingChecker.clearBlockingInfoChanged(node);
                }
                node=node.getNextTableauNode();
            }
            m_firstChangedNode=null;
        }
    }
    public boolean isPermanentAssertion(Concept concept,Node node) {
        return true;
    }
    public void assertionAdded(Concept concept,Node node,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(concept,node));
    }
    public void assertionCoreSet(Concept concept,Node node) {
    }
    public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(concept,node));
    }
    public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionAdded(atomicRole,nodeFrom,nodeTo));
    }
    public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
    }
    public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        updateNodeChange(m_directBlockingChecker.assertionRemoved(atomicRole,nodeFrom,nodeTo));
    }
    public void nodeStatusChanged(Node node) {
        updateNodeChange(node);
    }
    protected final void updateNodeChange(Node node) {
        if (node!=null && (m_firstChangedNode==null || node.getNodeID()<m_firstChangedNode.getNodeID()))
            m_firstChangedNode=node;
    }
    public void nodeInitialized(Node node) {
        m_directBlockingChecker.nodeInitialized(node);
    }
    public void nodeDestroyed(Node node) {
        if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
            m_currentBlockersCache.removeNode(node);
        m_directBlockingChecker.nodeDestroyed(node);
        if (m_firstChangedNode!=null && m_firstChangedNode.getNodeID()>=node.getNodeID())
            m_firstChangedNode=null;
    }
    public void modelFound() {
        if (m_blockingSignatureCache!=null) {
            computeBlocking(false);
            Node node=m_tableau.getFirstTableauNode();
            while (node!=null) {
                if (!node.isBlocked() && m_directBlockingChecker.canBeBlocker(node))
                    m_blockingSignatureCache.addNode(node);
                node=node.getNextTableauNode();
            }
        }
    }
    public boolean isExact() {
        return true;
    }
    public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,Object[] valuesBuffer,boolean[] coreVariables) {
    }
}
class BlockingCache implements Serializable {
    private static final long serialVersionUID=-7692825443489644667L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;
    protected CacheEntry m_emptyEntries;

    public BlockingCache(DirectBlockingChecker directBlockingChecker) {
        m_directBlockingChecker=directBlockingChecker;
        clear();
    }
    public boolean isEmpty() {
        return m_numberOfElements==0;
    }
    public void clear() {
        m_buckets=new CacheEntry[1024];
        m_threshold=(int)(m_buckets.length*0.75);
        m_numberOfElements=0;
        m_emptyEntries=null;
    }
    public void removeNode(Node node) {
        int hashCode=m_directBlockingChecker.blockingHashCode(node);
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry lastEntry=null;
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (entry.m_node==node) {
                if (lastEntry==null)
                    m_buckets[bucketIndex]=entry.m_nextEntry;
                else
                    lastEntry.m_nextEntry=entry.m_nextEntry;
                entry.m_nextEntry=m_emptyEntries;
                entry.m_node=null;
                entry.m_hashCode=0;
                m_emptyEntries=entry;
                m_numberOfElements--;
                return;
            }
            lastEntry=entry;
            entry=entry.m_nextEntry;
        }
    }
    public void addNode(Node node) {
        int hashCode=m_directBlockingChecker.blockingHashCode(node);
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node))
                return;
            entry=entry.m_nextEntry;
        }
        if (m_emptyEntries==null)
            entry=new CacheEntry();
        else {
            entry=m_emptyEntries;
            m_emptyEntries=m_emptyEntries.m_nextEntry;
        }
        entry.initialize(node,hashCode,m_buckets[bucketIndex]);
        m_buckets[bucketIndex]=entry;
        m_numberOfElements++;
        if (m_numberOfElements>=m_threshold)
            resize(m_buckets.length*2);
    }
    protected void resize(int newCapacity) {
        CacheEntry[] newBuckets=new CacheEntry[newCapacity];
        for (int i=0;i<m_buckets.length;i++) {
            CacheEntry entry=m_buckets[i];
            while (entry!=null) {
                CacheEntry nextEntry=entry.m_nextEntry;
                int newIndex=getIndexFor(entry.m_hashCode,newCapacity);
                entry.m_nextEntry=newBuckets[newIndex];
                newBuckets[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_threshold=(int)(newCapacity*0.75);
    }
    public Node getBlocker(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node))
                    return entry.m_node;
                entry=entry.m_nextEntry;
            }
        }
        return null;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }

    public static class CacheEntry implements Serializable {
        private static final long serialVersionUID=-7047487963170250200L;

        protected Node m_node;
        protected int m_hashCode;
        protected CacheEntry m_nextEntry;

        public void initialize(Node node,int hashCode,CacheEntry nextEntry) {
            m_node=node;
            m_hashCode=hashCode;
            m_nextEntry=nextEntry;
        }
    }
}
