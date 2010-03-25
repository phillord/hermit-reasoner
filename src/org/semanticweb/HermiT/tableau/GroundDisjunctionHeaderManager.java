package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.model.DLPredicate;

public final class GroundDisjunctionHeaderManager {
    protected GroundDisjunctionHeader[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;

    public GroundDisjunctionHeaderManager() {
        clear();
    }
    public void clear() {
        m_buckets=new GroundDisjunctionHeader[1024];
        m_threshold=(int)(m_buckets.length*0.75);
        m_numberOfElements=0;
    }
    public GroundDisjunctionHeader getGroundDisjunctionHeader(DLPredicate[] dlPredicates) {
        int hashCode=0;
        for (int disjunctIndex=0;disjunctIndex<dlPredicates.length;disjunctIndex++)
            hashCode=hashCode*7+dlPredicates[disjunctIndex].hashCode();
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        GroundDisjunctionHeader entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && entry.isEqual(dlPredicates))
                return entry;
            entry=entry.m_nextEntry;
        }
        entry=new GroundDisjunctionHeader(dlPredicates,hashCode,entry);
        m_buckets[bucketIndex]=entry;
        m_numberOfElements++;
        if (m_numberOfElements>=m_threshold)
            resize(m_buckets.length*2);
        return entry;
    }
    protected void resize(int newCapacity) {
        GroundDisjunctionHeader[] newBuckets=new GroundDisjunctionHeader[newCapacity];
        for (int i=0;i<m_buckets.length;i++) {
            GroundDisjunctionHeader entry=m_buckets[i];
            while (entry!=null) {
                GroundDisjunctionHeader nextEntry=entry.m_nextEntry;
                int newIndex=getIndexFor(entry.hashCode(),newCapacity);
                entry.m_nextEntry=newBuckets[newIndex];
                newBuckets[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_threshold=(int)(newCapacity*0.75);
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }
}
