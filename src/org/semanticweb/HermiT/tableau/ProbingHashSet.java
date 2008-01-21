package org.semanticweb.HermiT.tableau;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.NoSuchElementException;
import java.io.Serializable;

/**
 * A hash set implemented using probing.
 */
public class ProbingHashSet<E> implements Set<E>,Serializable {
    private static final long serialVersionUID=-7261811461627330848L;

    protected Object[] m_table;
    protected int m_size;
    protected int m_resizeThreshold;

    public ProbingHashSet() {
        this(16);
    }
    public ProbingHashSet(int expectedSize) {
        m_table=new Object[getClosestBiggerTwoPover(expectedSize)];
        m_resizeThreshold=(int)(0.6*m_table.length);
        m_size=0;
    }
    protected ProbingHashSet(boolean dummy,int exactSize) {
        m_table=new Object[exactSize];
        m_resizeThreshold=(int)(0.6*exactSize);
        m_size=0;
    }
    public static int getClosestBiggerTwoPover(int number) {
        int result=1;
        while (result<number)
            result*=2;
        return result;
    }
    public void clear() {
        for (int i=0;i<m_table.length;i++)
            m_table[i]=null;
        m_size=0;
    }
    public boolean isEmpty() {
        return m_size==0;
    }
    public Iterator<E> iterator() {
        return new ChainingHashSetIterator();
    }
    public int size() {
        return m_size;
    }
    public boolean contains(Object object) {
        // This implementation will terminate only if the table contains at least one null!
        if (object==null)
            return false;
        int tableLength=m_table.length;
        int index=getIndexFor(object.hashCode(),tableLength);
        while (true) {
            Object existing=m_table[index];
            if (existing==null)
                return false;
            if (object.equals(existing))
                return true;
            index=(index+1) % tableLength;
        }
    }
    public boolean containsAll(Collection<?> that) {
        if (that instanceof ProbingHashSet) {
            Object[] thatTable=((ProbingHashSet<?>)that).m_table;
            for (int index=0;index<thatTable.length;index++)
                if (thatTable[index]!=null && !contains(thatTable[index]))
                    return false;
        }
        else {
            for (Object object : that)
                if (!contains(object))
                    return false;
        }
        return true;
    }
    public boolean add(E object) {
        if (object==null)
            throw new IllegalArgumentException("Null values are not supported.");
        // This implementation will terminate only if the table contains at least one null!
        int tableLength=m_table.length;
        int index=getIndexFor(object.hashCode(),tableLength);
        while (true) {
            Object existing=m_table[index];
            if (existing==null) {
                m_table[index]=object;
                m_size++;
                if (m_size>m_resizeThreshold)
                    resize();
                return true;
            }
            if (object.equals(existing))
                return false;
            index=(index+1) % tableLength;
        }
    }
    @SuppressWarnings("unchecked")
    protected void resize() {
        Object[] oldTable=m_table;
        m_table=new Object[m_table.length*2];
        m_resizeThreshold=(int)(0.6*m_table.length);
        m_size=0;
        for (int index=0;index<oldTable.length;index++)
            if (oldTable[index]!=null)
                add((E)oldTable[index]);

    }
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends E> that) {
        boolean changed=false;
        if (that instanceof ProbingHashSet) {
            Object[] thatTable=((ProbingHashSet)that).m_table;
            for (int index=0;index<thatTable.length;index++)
                if (thatTable[index]!=null && add((E)thatTable[index]))
                    changed=true;
        }
        else {
            for (E object : that)
                if (add(object))
                    changed=true;
        }
        return changed;
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
    @SuppressWarnings("unchecked")
    public E getSomeElement() {
        for (int i=0;i<m_table.length;i++)
            if (m_table[i]!=null)
                return (E)m_table[i];
        return null;
    }
    public Object[] toArray() {
        Object[] result=new Object[m_size];
        int index=0;
        for (int i=0;i<m_table.length;i++)
            if (m_table[i]!=null)
                result[index++]=m_table[i];
        return result;
    }
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        int index=0;
        for (int i=0;i<m_table.length;i++)
            if (m_table[i]!=null)
                array[index++]=(T)m_table[i];
        return array;
    }
    public boolean equals(Object that) {
        if (this==that)
            return true;
        if (!(that instanceof Set))
            return false;
        Set<?> thatSet=(Set<?>)that;
        if (m_size!=thatSet.size())
            return false;
        return containsAll(thatSet);
    }
    public int hashCode() {
        int hashCode=0;
        for (int index=0;index<m_table.length;index++)
            if (m_table[index]!=null)
                hashCode+=m_table[index].hashCode();
        return hashCode;
    }
    protected static int getIndexFor(int hashCode,int tableLength) {
        return hashCode & (tableLength-1);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("[");
        boolean first=true;
        for (int index=0;index<m_table.length;index++)
            if (m_table[index]!=null) {
                if (first)
                    first=false;
                else
                    buffer.append(", ");
                buffer.append(m_table[index].toString());
            }
        buffer.append("]");
        return buffer.toString();
    }

    protected class ChainingHashSetIterator implements Iterator<E>,Serializable {
        private static final long serialVersionUID=1899796293471467237L;

        protected int m_index;

        public ChainingHashSetIterator() {
            m_index=0;
            while (m_index<m_table.length && m_table[m_index]==null)
                m_index++;
        }
        public boolean hasNext() {
            return m_index<m_table.length;
        }
        @SuppressWarnings("unchecked")
        public E next() {
            if (m_index>=m_table.length)
                throw new NoSuchElementException();
            E result=(E)m_table[m_index];
            m_index++;
            while (m_index<m_table.length && m_table[m_index]==null)
                m_index++;
            return result;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
