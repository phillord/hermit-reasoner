// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * The manager for the internable objects.
 */
public abstract class InterningManager<E> {
    protected static final double LOAD_FACTOR=0.75;

    protected final ReferenceQueue<E> m_referenceQueue;
    protected Entry<E>[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;
    
    public InterningManager() {
        m_referenceQueue=new ReferenceQueue<E>();
        m_entries=createEntries(16);
        m_size=0;
        m_resizeThreshold=(int)(m_entries.length*LOAD_FACTOR);
    }
    public synchronized E intern(E object) {
        processQueue();
        int hashCode=getHashCode(object);
        int objectEntryIndex=getIndexFor(hashCode,m_entries.length);
        Entry<E> previousEntry=null;
        Entry<E> entry=m_entries[objectEntryIndex];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode) {
                E entryObject=entry.get();
                if (entryObject==null) {
                    if (previousEntry==null)
                        m_entries[objectEntryIndex]=entry.m_next;
                    else
                        previousEntry.m_next=entry.m_next;
                    m_size--;
                }
                else if (equal(object,entryObject))
                    return entryObject;
            }
            previousEntry=entry;
            entry=entry.m_next;
        }
        if (m_size>=m_resizeThreshold) {
            int newEntriesLength=m_entries.length*2;
            Entry<E>[] newEntries=createEntries(newEntriesLength);
            for (int entryIndex=0;entryIndex<m_entries.length;entryIndex++) {
                Entry<E> currentEntry=m_entries[entryIndex];
                while (currentEntry!=null) {
                    Entry<E> nextEntry=currentEntry.m_next;
                    if (currentEntry.get()==null)
                        m_size--;
                    else {
                        int newIndex=getIndexFor(currentEntry.m_hashCode,newEntriesLength);
                        currentEntry.m_next=newEntries[newIndex];
                        newEntries[newIndex]=currentEntry;
                    }
                    currentEntry=nextEntry;
                }
            }
            m_entries=newEntries;
            m_resizeThreshold=(int)(newEntriesLength*LOAD_FACTOR);
            objectEntryIndex=getIndexFor(hashCode,m_entries.length);
        }
        Entry<E> newEntry=new Entry<E>(object,hashCode,m_entries[objectEntryIndex]);
        m_entries[objectEntryIndex]=newEntry;
        m_size++;
        return object;
    }
    protected final int getIndexFor(int hashCode,int entriesLength) {
        return hashCode & (entriesLength-1);
    }
    protected void removeEntry(Entry<E> entry) {
        int index=getIndexFor(entry.m_hashCode,m_entries.length);
        Entry<E> previousEntry=null;
        for (Entry<E> current=m_entries[index];current!=null;current=current.m_next) {
            if (current==entry) {
                m_size--;
                if (previousEntry==null)
                    m_entries[index]=current.m_next;
                else
                    previousEntry.m_next=current.m_next;
                return;
            }
            previousEntry=current;
        }
    }
    @SuppressWarnings("unchecked")
    protected void processQueue() {
        Entry<E> entry=(Entry<E>)m_referenceQueue.poll();
        while (entry!=null) {
            removeEntry(entry);
            entry=(Entry<E>)m_referenceQueue.poll();
        }
    }
    @SuppressWarnings("unchecked")
    protected final Entry<E>[] createEntries(int size) {
        return (Entry<E>[])new Entry[size];
    }
    protected abstract int getHashCode(E object);
    protected abstract boolean equal(E object1,E object2);

    /**
     * The entry for the hash map.
     */
    protected static class Entry<E> extends WeakReference<E> {
        public final int m_hashCode;
        public Entry<E> m_next;
        
        public Entry(E object,int hashCode,Entry<E> next) {
            super(object);
            m_hashCode=hashCode;
            m_next=next;
        }
    }
}
