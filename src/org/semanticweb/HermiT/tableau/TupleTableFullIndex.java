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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class TupleTableFullIndex implements Serializable {
    private static final long serialVersionUID=5006873858554891684L;

    protected static final int BUCKET_OFFSET=1;
    protected static final float LOAD_FACTOR=0.75f;

    protected final TupleTable m_tupleTable;
    protected final int m_indexedArity;
    protected final EntryManager m_entryManager;
    protected int[] m_buckets;
    protected int m_resizeThreshold;
    protected int m_numberOfTuples;

    public TupleTableFullIndex(TupleTable tupleTable,int indexedArity) {
        m_tupleTable=tupleTable;
        m_indexedArity=indexedArity;
        m_entryManager=new EntryManager();
        clear();
    }
    public int sizeInMemory() {
        return m_buckets.length*4+m_entryManager.size();
    }
    public void clear() {
        m_buckets=new int[16];
        m_resizeThreshold=(int)(m_buckets.length*LOAD_FACTOR);
        m_entryManager.clear();
    }
    public int addTuple(Object[] tuple,int tentativeTupleIndex) {
        int hashCode=getTupleHashCode(tuple);
        int entryIndex=getBucketIndex(hashCode,m_buckets.length);
        int entry=m_buckets[entryIndex]-BUCKET_OFFSET;
        while (entry!=-1) {
            if (hashCode==m_entryManager.getEntryComponent(entry,ENTRY_HASH_CODE)) {
                int tupleIndex=m_entryManager.getEntryComponent(entry,ENTRY_TUPLE_INDEX);
                if (m_tupleTable.tupleEquals(tuple,tupleIndex,m_indexedArity))
                    return tupleIndex;
            }
            entry=m_entryManager.getEntryComponent(entry,ENTRY_NEXT);
        }
        entry=m_entryManager.newEntry();
        m_entryManager.setEntryComponent(entry,ENTRY_NEXT,m_buckets[entryIndex]-BUCKET_OFFSET);
        m_entryManager.setEntryComponent(entry,ENTRY_HASH_CODE,hashCode);
        m_entryManager.setEntryComponent(entry,ENTRY_TUPLE_INDEX,tentativeTupleIndex);
        m_buckets[entryIndex]=entry+BUCKET_OFFSET;
        m_numberOfTuples++;
        if (m_numberOfTuples>=m_resizeThreshold)
            resizeBuckets();
        return tentativeTupleIndex;
    }
    protected void resizeBuckets() {
        int[] newBuckets=new int[m_buckets.length*2];
        for (int bucketIndex=m_buckets.length-1;bucketIndex>=0;--bucketIndex) {
            int entry=m_buckets[bucketIndex]-BUCKET_OFFSET;
            while (entry!=-1) {
                int nextEntry=m_entryManager.getEntryComponent(entry,ENTRY_NEXT);
                int newBucketIndex=getBucketIndex(m_entryManager.getEntryComponent(entry,ENTRY_HASH_CODE),newBuckets.length);
                m_entryManager.setEntryComponent(entry,ENTRY_NEXT,newBuckets[newBucketIndex]-BUCKET_OFFSET);
                newBuckets[newBucketIndex]=entry+BUCKET_OFFSET;
                entry=nextEntry;
            }
        }
        m_buckets=newBuckets;
        m_resizeThreshold=(int)(newBuckets.length*LOAD_FACTOR);
    }
    public int getTupleIndex(Object[] tuple) {
        int hashCode=getTupleHashCode(tuple);
        int entryIndex=getBucketIndex(hashCode,m_buckets.length);
        int entry=m_buckets[entryIndex]-BUCKET_OFFSET;
        while (entry!=-1) {
            if (hashCode==m_entryManager.getEntryComponent(entry,ENTRY_HASH_CODE)) {
                int tupleIndex=m_entryManager.getEntryComponent(entry,ENTRY_TUPLE_INDEX);
                if (m_tupleTable.tupleEquals(tuple,tupleIndex,m_indexedArity))
                    return tupleIndex;
            }
            entry=m_entryManager.getEntryComponent(entry,ENTRY_NEXT);
        }
        return -1;
    }
    public int getTupleIndex(Object[] tupleBuffer,int[] positionIndexes) {
        int hashCode=getTupleHashCode(tupleBuffer,positionIndexes);
        int entryIndex=getBucketIndex(hashCode,m_buckets.length);
        int entry=m_buckets[entryIndex]-BUCKET_OFFSET;
        while (entry!=-1) {
            if (hashCode==m_entryManager.getEntryComponent(entry,ENTRY_HASH_CODE)) {
                int tupleIndex=m_entryManager.getEntryComponent(entry,ENTRY_TUPLE_INDEX);
                if (m_tupleTable.tupleEquals(tupleBuffer,positionIndexes,tupleIndex,m_indexedArity))
                    return tupleIndex;
            }
            entry=m_entryManager.getEntryComponent(entry,ENTRY_NEXT);
        }
        return -1;
    }
    public boolean removeTuple(int tupleIndex) {
        int hashCode=0;
        for (int i = 0; i < m_indexedArity; ++i) {
            hashCode += m_tupleTable.getTupleObject(tupleIndex, i).hashCode();
        }
        int lastEntry=-1;
        int entryIndex=getBucketIndex(hashCode,m_buckets.length);
        int entry=m_buckets[entryIndex]-BUCKET_OFFSET;
        while (entry!=-1) {
            int nextEntry=m_entryManager.getEntryComponent(entry,ENTRY_NEXT);
            if (hashCode==m_entryManager.getEntryComponent(entry,ENTRY_HASH_CODE) && tupleIndex==m_entryManager.getEntryComponent(entry,ENTRY_TUPLE_INDEX)) {
                if (lastEntry==-1)
                    m_buckets[entryIndex]=nextEntry+BUCKET_OFFSET;
                else
                    m_entryManager.setEntryComponent(lastEntry,ENTRY_NEXT,nextEntry);
                return true;
            }
            lastEntry=entry;
            entry=nextEntry;
        }
        return false;
    }
    protected int getTupleHashCode(Object[] tuple) {
        int hashCode=0;
        for (int index=0;index<m_indexedArity;index++)
            hashCode+=tuple[index].hashCode();
        return hashCode;
    }
    protected int getTupleHashCode(Object[] tupleBuffer,int[] positionIndexes) {
        int hashCode=0;
        for (int index=0;index<m_indexedArity;index++)
            hashCode+=tupleBuffer[positionIndexes[index]].hashCode();
        return hashCode;
    }
    protected static int getBucketIndex(int hashCode,int bucketsLength) {
        return hashCode & (bucketsLength-1);
    }

    protected static final int ENTRY_SIZE=3;
    protected static final int ENTRY_NEXT=0;
    protected static final int ENTRY_HASH_CODE=1;
    protected static final int ENTRY_TUPLE_INDEX=2;
    protected static final int ENTRY_PAGE_SIZE=512;

    protected static final class EntryManager implements Serializable {
        private static final long serialVersionUID=-7562640774004213308L;

        protected int[] m_entries;
        protected int m_firstFreeEntry;

        public EntryManager() {
            clear();
        }
        public int size() {
            return m_entries.length*4;
        }
        public void clear() {
            m_entries=new int[ENTRY_SIZE*ENTRY_PAGE_SIZE];
            m_firstFreeEntry=0;
            m_entries[m_firstFreeEntry+ENTRY_NEXT]=-1;
        }
        public int getEntryComponent(int entry,int component) {
            return m_entries[entry+component];
        }
        public void setEntryComponent(int entry,int component,int value) {
            m_entries[entry+component]=value;
        }
        public int newEntry() {
            int result=m_firstFreeEntry;
            int nextFreeEntry=m_entries[m_firstFreeEntry+ENTRY_NEXT];
            if (nextFreeEntry==-1) {
                m_firstFreeEntry+=ENTRY_SIZE;
                if (m_firstFreeEntry>=m_entries.length) {
                    int[] newEntries=new int[m_entries.length+ENTRY_SIZE*ENTRY_PAGE_SIZE];
                    System.arraycopy(m_entries,0,newEntries,0,m_entries.length);
                    m_entries=newEntries;
                }
                m_entries[m_firstFreeEntry+ENTRY_NEXT]=-1;
            }
            else
                m_firstFreeEntry=nextFreeEntry;
            return result;
        }
        public void deleteEntry(int entry) {
            m_entries[entry+ENTRY_NEXT]=m_firstFreeEntry;
            m_firstFreeEntry=entry;
        }
    }
}
