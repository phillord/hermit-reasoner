package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.*;

public class BlockingCache implements Serializable {
    private static final long serialVersionUID=-7692825443489644667L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected CacheEntry[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;

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
    }
    public void removeCacheEntry(CacheEntry removeEntry) {
        int bucketIndex=getIndexFor(removeEntry.m_hashCode,m_buckets.length);
        CacheEntry lastEntry=null;
        CacheEntry entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (entry==removeEntry) {
                if (lastEntry==null)
                    m_buckets[bucketIndex]=entry.m_nextEntry;
                else
                    lastEntry.m_nextEntry=entry.m_nextEntry;
                entry.m_nextEntry=null;
                m_numberOfElements--;
                return;
            }
            lastEntry=entry;
            entry=entry.m_nextEntry;
        }
        throw new IllegalStateException("Internal error: node not found in the blocking cache.");
    }
    public CacheEntry addNode(Node node) {
        if (m_directBlockingChecker.canBeBlocker(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            CacheEntry entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && m_directBlockingChecker.isBlockedBy(entry.m_node,node))
                    return entry;
                entry=entry.m_nextEntry;
            }
            entry=new CacheEntry(node,hashCode,m_buckets[bucketIndex]);
            m_buckets[bucketIndex]=entry;
            m_numberOfElements++;
            if (m_numberOfElements>=m_threshold)
                resize(m_buckets.length*2);
            return entry;
        }
        else
            return null;
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

        protected final Node m_node;
        protected final int m_hashCode;
        protected CacheEntry m_nextEntry;

        public CacheEntry(Node node,int hashCode,CacheEntry nextEntry) {
            m_node=node;
            m_hashCode=hashCode;
            m_nextEntry=nextEntry;
        }
    }
}
