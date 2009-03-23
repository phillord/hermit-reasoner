// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;

/**
 * This class is not used in the normal expansion of the tableau. Only at the 
 * end of a run, it is used to persist blocking signatures to facilitate another 
 * run on the same DLOntology. Uses a custom implementation of Hash sets. 
 */
public final class LabelManager implements Serializable {
    private static final long serialVersionUID=2628318450352626514L;

    protected final SetFactory<AtomicConcept> m_atomicConceptSetFactory;
    protected final SetFactory<AtomicRole> m_atomicRoleSetFactory;
    protected final ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected final ExtensionTable.Retrieval m_ternaryTableSearch12Bound;
    protected final List<AtomicConcept> m_atomicConceptBuffer;
    protected final List<AtomicRole> m_atomicRoleBuffer;
    
    public LabelManager(Tableau tableau) {
        m_atomicConceptSetFactory=new SetFactory<AtomicConcept>();
        m_atomicRoleSetFactory=new SetFactory<AtomicRole>();
        m_binaryTableSearch1Bound=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearch12Bound=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
        m_atomicConceptBuffer=new ArrayList<AtomicConcept>();
        m_atomicRoleBuffer=new ArrayList<AtomicRole>(); 
    }
    public int sizeInMemoryConceptSetFactory() {
        return m_atomicConceptSetFactory.sizeInMemory();
    }
    public int sizeInMemoryAtomicRoleSetFactory() {
        return m_atomicRoleSetFactory.sizeInMemory();
    }
    public Set<AtomicConcept> getPositiveLabel(Node node) {
        m_atomicConceptBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (concept instanceof AtomicConcept)
                m_atomicConceptBuffer.add((AtomicConcept)concept);
            m_binaryTableSearch1Bound.next();
        }
        Set<AtomicConcept> result=m_atomicConceptSetFactory.getSet(m_atomicConceptBuffer);
        m_atomicConceptBuffer.clear();
        return result;
    }
    public Set<AtomicRole> getEdgeLabel(Node nodeFrom,Node nodeTo) {
        m_atomicRoleBuffer.clear();
        m_ternaryTableSearch12Bound.getBindingsBuffer()[1]=nodeFrom;
        m_ternaryTableSearch12Bound.getBindingsBuffer()[2]=nodeTo;
        m_ternaryTableSearch12Bound.open();
        Object[] tupleBuffer=m_ternaryTableSearch12Bound.getTupleBuffer();
        while (!m_ternaryTableSearch12Bound.afterLast()) {
            Object atomicRole=tupleBuffer[0];
            if (atomicRole instanceof AtomicRole)
                m_atomicRoleBuffer.add((AtomicRole)atomicRole);
            m_ternaryTableSearch12Bound.next();
        }
        Set<AtomicRole> result=m_atomicRoleSetFactory.getSet(m_atomicRoleBuffer);
        m_atomicRoleBuffer.clear();
        return result;
    }
    public void addAtomicConceptSetReference(Set<AtomicConcept> set) {
        m_atomicConceptSetFactory.addReference(set);
    }
    public void removeAtomicConceptSetReference(Set<AtomicConcept> set) {
        m_atomicConceptSetFactory.removeReference(set);
    }
    public void addAtomicRoleSetReference(Set<AtomicRole> set) {
        m_atomicRoleSetFactory.addReference(set);
    }
    public void removeAtomicRoleSetReference(Set<AtomicRole> set) {
        m_atomicRoleSetFactory.removeReference(set);
    }
    public void makePermanentAtomicConceptSet(Set<AtomicConcept> set) {
        m_atomicConceptSetFactory.makePermanent(set);
    }
    public void makePermanentAtomicRoleSet(Set<AtomicRole> set) {
        m_atomicRoleSetFactory.makePermanent(set);
    }
    public void clearNonpermanent() {
        m_atomicConceptSetFactory.clearNonpermanent();
        m_atomicRoleSetFactory.clearNonpermanent();
    }
}
@SuppressWarnings("unchecked")
class SetFactory<E> implements Serializable {
    private static final long serialVersionUID=7071071962187693657L;

    protected Entry[] m_unusedEntries;
    protected Entry[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;

    public SetFactory() {
        m_unusedEntries=new Entry[32];
        m_entries=new Entry[16];
        m_size=0;
        m_resizeThreshold=(int)(0.75*m_entries.length);
    }
    public void clearNonpermanent() {
        for (int i=m_entries.length-1;i>=0;--i) {
            Entry entry=m_entries[i];
            while (entry!=null) {
                Entry nextEntry=entry.m_nextEntry;
                if (!entry.m_permanent) {
                    removeEntry(entry);
                    leaveEntry(entry);
                }
                entry=nextEntry;
            }
        }
    }
    public int sizeInMemory() {
        int size=m_unusedEntries.length*4+m_entries.length*4;
        for (int i=m_unusedEntries.length-1;i>=0;--i) {
            Entry entry=m_unusedEntries[i];
            while (entry!=null) {
                size+=entry.m_table.length*4+6*4;
                entry=entry.m_nextEntry;
            }
        }
        for (int i=m_entries.length-1;i>=0;--i) {
            Entry entry=m_entries[i];
            while (entry!=null) {
                size+=entry.m_table.length*4+6*4;
                entry=entry.m_nextEntry;
            }
        }
        return size;
    }
    public void addReference(Set<E> set) {
        ((Entry)set).m_referenceCount++;
    }
    public void removeReference(Set<E> set) {
        Entry entry=(Entry)set;
        entry.m_referenceCount--;
        if (entry.m_referenceCount==0 && !entry.m_permanent) {
            removeEntry(entry);
            leaveEntry(entry);
        }
    }
    public void makePermanent(Set<E> set) {
        ((Entry)set).m_permanent=true;
    }
    public Set<E> getSet(List<E> elements) {
        int hashCode=0;
        for (int index=elements.size()-1;index>=0;--index)
            hashCode+=elements.get(index).hashCode();
        int index=getIndexFor(hashCode,m_entries.length);
        Entry<E> entry=m_entries[index];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && entry.equalsTo(elements))
                return entry;
            entry=entry.m_nextEntry;
        }
        entry=getEntry(elements.size());
        entry.initialize(elements,hashCode);
        entry.m_previousEntry=null;
        entry.m_nextEntry=m_entries[index];
        if (entry.m_nextEntry!=null)
            entry.m_nextEntry.m_previousEntry=entry;
        m_entries[index]=entry;
        m_size++;
        if (m_size>m_resizeThreshold)
            resize();
        return entry;
    }
    protected void resize() {
        Entry[] newEntries=new Entry[m_entries.length*2];
        for (int index=0;index<m_entries.length;index++) {
            Entry entry=m_entries[index];
            while (entry!=null) {
                Entry nextEntry=entry.m_nextEntry;
                int newIndex=getIndexFor(entry.m_hashCode,newEntries.length);
                entry.m_nextEntry=newEntries[newIndex];
                entry.m_previousEntry=null;
                if (entry.m_nextEntry!=null)
                    entry.m_nextEntry.m_previousEntry=entry;
                newEntries[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_entries=newEntries;
        m_resizeThreshold=(int)(0.75*m_entries.length);
    }
    protected void removeEntry(Entry<E> entry) {
        if (entry.m_nextEntry!=null)
            entry.m_nextEntry.m_previousEntry=entry.m_previousEntry;
        if (entry.m_previousEntry!=null)
            entry.m_previousEntry.m_nextEntry=entry.m_nextEntry;
        int index=getIndexFor(entry.m_hashCode,m_entries.length);
        if (m_entries[index]==entry)
            m_entries[index]=entry.m_nextEntry;
        entry.m_nextEntry=null;
        entry.m_previousEntry=null;
    }
    protected Entry<E> getEntry(int size) {
        if (size>=m_unusedEntries.length) {
            int newSize=m_unusedEntries.length;
            while (newSize<=size)
                newSize=newSize*3/2;
            Entry[] newUnusedEntries=new Entry[newSize];
            System.arraycopy(m_unusedEntries,0,newUnusedEntries,0,m_unusedEntries.length);
            m_unusedEntries=newUnusedEntries;
        }
        Entry<E> entry=m_unusedEntries[size];
        if (entry==null)
            return new Entry<E>(size);
        else {
            m_unusedEntries[size]=entry.m_nextEntry;
            entry.m_nextEntry=null;
            return entry;
        }
    }
    protected void leaveEntry(Entry<E> entry) {
        entry.m_nextEntry=m_unusedEntries[entry.size()];
        entry.m_previousEntry=null;
        m_unusedEntries[entry.size()]=entry;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        return hashCode & (tableLength-1);
    }
    
    protected static class Entry<T> implements Serializable,Set<T> {
        private static final long serialVersionUID=-3850593656120645350L;

        protected T[] m_table;
        protected int m_hashCode;
        protected Entry<T> m_previousEntry;
        protected Entry<T> m_nextEntry;
        protected int m_referenceCount;
        protected boolean m_permanent;

        public Entry(int size) {
            m_hashCode=0;
            m_table=(T[])new Object[size];
        }
        public void initialize(List<T> elements,int hashCode) {
            elements.toArray(m_table);
            m_hashCode=hashCode;
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
        public boolean add(T object) {
            throw new UnsupportedOperationException();
        }
        public boolean equalsTo(List<T> elements) {
            if (m_table.length!=elements.size())
                return false;
            for (int index=m_table.length-1;index>=0;--index)
                if (!elements.contains(m_table[index]))
                    return false;
            return true;
        }
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }
        public boolean contains(Object o) {
            for (int index=0;index<m_table.length;index++)
                if (!m_table[index].equals(o))
                    return false;
            return true;
        }
        public boolean containsAll(Collection<?> c) {
            for (Object object : c)
                if (!contains(object))
                    return false;
            return true;
        }
        public boolean isEmpty() {
            return m_table.length==0;
        }
        public Iterator<T> iterator() {
            return new EntryIterator();
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        public int size() {
            return m_table.length;
        }
        public Object[] toArray() {
            return m_table.clone();
        }
        public <E> E[] toArray(E[] a) {
            System.arraycopy(m_table,0,a,0,m_table.length);
            return a;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            return this==that;
        }
        
        protected class EntryIterator implements Iterator<T> {
            protected int m_currentIndex;

            public EntryIterator() {
                m_currentIndex=0;
            }
            public boolean hasNext() {
                return m_currentIndex<m_table.length;
            }
            public T next() {
                if (m_currentIndex>=m_table.length)
                    throw new NoSuchElementException();
                return m_table[m_currentIndex++];
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        }
    }
}
