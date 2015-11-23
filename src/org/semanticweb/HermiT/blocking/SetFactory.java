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
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class is used to create sets of various types. It ensures that each distinct set exists only once,
 * thus allowing sets to be compared with ==. Instances of this class are used to create various labels in blocking.
 * @param <E> created type
 */
@SuppressWarnings({ "unchecked", "rawtypes"})
public class SetFactory<E> implements Serializable {
    private static final long serialVersionUID=7071071962187693657L;

    protected Entry[] m_unusedEntries=new Entry[32];
    protected Entry[] m_entries=new Entry[16];
    protected int m_size=0;
    protected int m_resizeThreshold=12;

    /**
     * Clear non permanent.
     */
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
    /**
     * @param set set
     */
    public void addReference(Set<E> set) {
        ((Entry)set).m_referenceCount++;
    }
    /**
     * @param set set
     */
    public void removeReference(Set<E> set) {
        Entry entry=(Entry)set;
        entry.m_referenceCount--;
        if (entry.m_referenceCount==0 && !entry.m_permanent) {
            removeEntry(entry);
            leaveEntry(entry);
        }
    }
    /**
     * @param set set
     */
    public void makePermanent(Set<E> set) {
        ((Entry)set).m_permanent=true;
    }
    /**
     * @param elements elements
     * @return set
     */
    public Set<E> getSet(Set<E> elements) {
        int hashCode=elements.hashCode();
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
            return new Entry<>(size);
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

        protected final T[] m_table;
        protected int m_hashCode;
        protected Entry<T> m_previousEntry;
        protected Entry<T> m_nextEntry;
        protected int m_referenceCount;
        protected boolean m_permanent;

        public Entry(int size) {
            m_hashCode=0;
            m_table=(T[])new Object[size];
        }
        public void initialize(Collection<T> elements,int hashCode) {
            elements.toArray(m_table);
            m_hashCode=hashCode;
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean add(T object) {
            throw new UnsupportedOperationException();
        }
        public boolean equalsTo(Set<T> elements) {
            if (m_table.length!=elements.size())
                return false;
            for (int index=m_table.length-1;index>=0;--index)
                if (!elements.contains(m_table[index]))
                    return false;
            return true;
        }
        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean contains(Object o) {
            for (int index=m_table.length-1;index>=0;--index)
                if (m_table[index].equals(o))
                    return true;
            return false;
        }
        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object object : c)
                if (!contains(object))
                    return false;
            return true;
        }
        @Override
        public boolean isEmpty() {
            return m_table.length==0;
        }
        @Override
        public Iterator<T> iterator() {
            return new EntryIterator();
        }
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        @Override
        public int size() {
            return m_table.length;
        }
        @Override
        public Object[] toArray() {
            return m_table.clone();
        }
        @Override
        public <E> E[] toArray(E[] a) {
            System.arraycopy(m_table,0,a,0,m_table.length);
            return a;
        }
        @Override
        public int hashCode() {
            return m_hashCode;
        }
        @Override
        public boolean equals(Object that) {
            return this==that;
        }

        protected class EntryIterator implements Iterator<T> {
            protected int m_currentIndex;

            public EntryIterator() {
                m_currentIndex=0;
            }
            @Override
            public boolean hasNext() {
                return m_currentIndex<m_table.length;
            }
            @Override
            public T next() {
                if (m_currentIndex>=m_table.length)
                    throw new NoSuchElementException();
                return m_table[m_currentIndex++];
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }
    }
}
