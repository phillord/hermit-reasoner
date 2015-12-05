/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.blocking;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.Node;

public class BlockingSignatureCache implements Serializable {
    private static final long serialVersionUID=-7692825443489644667L;

    protected final DirectBlockingChecker m_directBlockingChecker;
    protected BlockingSignature[] m_buckets;
    protected int m_numberOfElements;
    protected int m_threshold;

    public BlockingSignatureCache(DirectBlockingChecker directBlockingChecker) {
        m_directBlockingChecker=directBlockingChecker;
        m_buckets=new BlockingSignature[1024];
        m_threshold=(int)(m_buckets.length*0.75);
        m_numberOfElements=0;
    }
    public boolean isEmpty() {
        return m_numberOfElements==0;
    }
    public boolean addNode(Node node) {
        int hashCode=m_directBlockingChecker.blockingHashCode(node);
        int bucketIndex=getIndexFor(hashCode,m_buckets.length);
        BlockingSignature entry=m_buckets[bucketIndex];
        while (entry!=null) {
            if (hashCode==entry.hashCode() && entry.blocksNode(node))
                return false;
            entry=entry.m_nextEntry;
        }
        entry=m_directBlockingChecker.getBlockingSignatureFor(node);
        entry.m_nextEntry=m_buckets[bucketIndex];
        m_buckets[bucketIndex]=entry;
        m_numberOfElements++;
        if (m_numberOfElements>=m_threshold)
            resize(m_buckets.length*2);
        return true;
    }
    protected void resize(int newCapacity) {
        BlockingSignature[] newBuckets=new BlockingSignature[newCapacity];
        for (int i=0;i<m_buckets.length;i++) {
            BlockingSignature entry=m_buckets[i];
            while (entry!=null) {
                BlockingSignature nextEntry=entry.m_nextEntry;
                int newIndex=getIndexFor(entry.hashCode(),newCapacity);
                entry.m_nextEntry=newBuckets[newIndex];
                newBuckets[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_threshold=(int)(newCapacity*0.75);
    }
    public boolean containsSignature(Node node) {
        if (m_directBlockingChecker.canBeBlocked(node)) {
            int hashCode=m_directBlockingChecker.blockingHashCode(node);
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            BlockingSignature entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.hashCode() && entry.blocksNode(node))
                    return true;
                entry=entry.m_nextEntry;
            }
        }
        return false;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & (tableLength-1);
    }
}
