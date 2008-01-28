package org.semanticweb.HermiT.tableau;

import java.util.Set;
import java.util.List;
import java.io.Serializable;

/**
 * This object creates sets of a certain type and ensures that only one set with certain elements is created.
 */
@SuppressWarnings("unchecked")
public class SetFactory<E> implements Serializable {
    private static final long serialVersionUID=7071071962187693657L;

    protected final Entry[] m_unusedEntries;
    protected final Entry<E> m_emptySet;
    protected Entry[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;

    public SetFactory() {
        m_unusedEntries=new Entry[32];
        m_emptySet=new Entry<E>(0,1);
        m_entries=new Entry[16];
        m_size=0;
        m_resizeThreshold=(int)(0.75*m_entries.length);
    }
    public int sizeInMemory() {
        int size=m_unusedEntries.length*4+m_entries.length*4;
        for (int i=m_unusedEntries.length-1;i>=0;--i)
            if (m_unusedEntries[i]!=null)
                size+=m_unusedEntries[i].m_table.length*4+7*4;
        for (int i=m_entries.length-1;i>=0;--i)
            if (m_entries[i]!=null)
                size+=m_entries[i].m_table.length*4+7*4;
        return size;
    }
    public void addReference(Set<E> set) {
        ((Entry)set).m_referenceCount++;
    }
    public void removeReference(Set<E> set) {
        Entry entry=(Entry)set;
        entry.m_referenceCount--;
        if (entry.m_referenceCount==0) {
            removeEntry(entry);
            leaveEntry(entry);
        }
    }
    public Set<E> emptySet() {
        return m_emptySet;
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
    public Set<E> addElement(Set<E> set,E element) {
        Entry<E> setEntry=(Entry<E>)set;
        if (setEntry.contains(element))
            return setEntry;
        int hashCode=setEntry.m_hashCode+element.hashCode();
        int index=getIndexFor(hashCode,m_entries.length);
        Entry<E> entry=m_entries[index];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && entry.equalsToEntryPlusNonmemberObject(setEntry,element))
                return entry;
            entry=entry.m_nextEntry;
        }
        entry=getEntry(setEntry.size()+1);
        entry.initializeAndAdd(setEntry,element);
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
    public Set<E> removeElement(Set<E> set,E element) {
        Entry<E> setEntry=(Entry<E>)set;
        if (!setEntry.contains(element))
            return setEntry;
        if (setEntry.size()==1)
            return m_emptySet;
        int hashCode=setEntry.m_hashCode-element.hashCode();
        int index=getIndexFor(hashCode,m_entries.length);
        Entry<E> entry=m_entries[index];
        while (entry!=null) {
            if (hashCode==entry.m_hashCode && entry.equalsToEntryMinusNonmemberObject(setEntry,element))
                return entry;
            entry=entry.m_nextEntry;
        }
        entry=getEntry(setEntry.size()-1);
        entry.initializeAndRemove(setEntry,element);
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
        int requiredSize=(int)Math.ceil(size/0.6+1);
        while (Math.ceil(requiredSize*0.6)<=size)
            requiredSize+=2;
        int sizePowerTwo=1;
        int power=0;
        while (sizePowerTwo<requiredSize) {
            sizePowerTwo*=2;
            power++;
        }
        Entry<E> entry=m_unusedEntries[power];
        if (entry==null)
            return new Entry<E>(power,sizePowerTwo);
        else {
            m_unusedEntries[power]=entry.m_nextEntry;
            entry.m_nextEntry=null;
            return entry;
        }
    }
    protected void leaveEntry(Entry<E> entry) {
        entry.m_nextEntry=m_unusedEntries[entry.m_power];
        entry.m_previousEntry=null;
        m_unusedEntries[entry.m_power]=entry;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        return hashCode & (tableLength-1);
    }
    
    @SuppressWarnings("unchecked")
    protected static class Entry<T> extends ProbingHashSet<T> {
        private static final long serialVersionUID=-3850593656120645350L;

        protected final int m_power;
        protected int m_hashCode;
        protected Entry<T> m_previousEntry;
        protected Entry<T> m_nextEntry;
        protected int m_referenceCount;

        public Entry(int power,int exactSize) {
            super(false,exactSize);
            m_power=power;
            m_hashCode=0;
        }
        public void initialize(List<T> elements,int hashCode) {
            for (int index=0;index<m_table.length;index++)
                m_table[index]=null;
            m_size=0;
            for (int index=elements.size()-1;index>=0;--index)
                super.add(elements.get(index));
            m_hashCode=hashCode;
        }
        public void initializeAndAdd(Entry<T> that,T object) {
            if (m_table.length==that.m_table.length) {
                System.arraycopy(that.m_table,0,m_table,0,m_table.length);
                m_size=that.m_size;
            }
            else {
                for (int index=0;index<m_table.length;index++)
                    m_table[index]=null;
                m_size=0;
                for (int index=0;index<that.m_table.length;index++) {
                    Object element=that.m_table[index];
                    if (element!=null)
                        super.add((T)element);
                }
            }
            super.add(object);
            m_hashCode=that.m_hashCode+object.hashCode();
        }
        public void initializeAndRemove(Entry<T> that,T object) {
            for (int index=0;index<m_table.length;index++)
                m_table[index]=null;
            m_size=0;
            for (int index=0;index<that.m_table.length;index++) {
                Object element=that.m_table[index];
                if (element!=null && !element.equals(object))
                    super.add((T)element);
            }
            m_hashCode=that.m_hashCode-object.hashCode();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
        protected void resize() {
            throw new IllegalStateException();
        }
        public boolean add(T object) {
            throw new UnsupportedOperationException();
        }
        public boolean equals(Object that) {
            return this==that;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equalsToEntryPlusNonmemberObject(Entry<T> that,T object) {
            if (m_size!=that.size()+1)
                return false;
            if (!contains(object))
                return false;
            if (!containsAll(that))
                return false;
            return true;
        }
        public boolean equalsTo(List<T> elements) {
            if (m_size!=elements.size())
                return false;
            for (int index=elements.size()-1;index>=0;--index)
                if (!contains(elements.get(index)))
                    return false;
            return true;
        }
        public boolean equalsToEntryMinusNonmemberObject(Entry<T> that,T object) {
            if (m_size!=that.size()-1)
                return false;
            if (contains(object))
                return false;
            Object[] thatTable=((ProbingHashSet<?>)that).m_table;
            for (int index=0;index<thatTable.length;index++) {
                Object thatElement=thatTable[index];
                if (thatElement!=null && !thatElement.equals(object) && !contains(thatElement))
                    return false;
            }
            return true;
        }
    }
}
