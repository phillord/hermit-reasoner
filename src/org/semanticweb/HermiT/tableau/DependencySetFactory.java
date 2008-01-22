package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class DependencySetFactory implements Serializable {
    private static final long serialVersionUID=8632867055646817311L;

    protected final IntegerArray m_mergeArray;
    protected final DependencySet m_emptySet;
    protected DependencySet[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;

    public DependencySetFactory() {
        m_mergeArray=new IntegerArray();
        m_emptySet=new DependencySet(null,-1,null);
        clear();
    }
    public int size() {
        return m_entries.length*4+m_size*20;
    }
    public void clear() {
        m_emptySet.m_nextEntry=null;
        m_entries=new DependencySet[16];
        m_size=0;
        m_resizeThreshold=(int)(m_entries.length*0.75);
    }
    public DependencySet emptySet() {
        return m_emptySet;
    }
    public DependencySet addBranchingPoint(DependencySet dependencySet,int branchingPoint) {
        if (branchingPoint>dependencySet.m_branchingPoint)
            return getNextDepdendencySet(dependencySet,branchingPoint);
        else if (branchingPoint==dependencySet.m_branchingPoint)
            return dependencySet;
        else {
            m_mergeArray.clear();
            DependencySet rest=dependencySet;
            while (branchingPoint<rest.m_branchingPoint) {
                m_mergeArray.add(rest.m_branchingPoint);
                rest=rest.m_rest;
            }
            if (branchingPoint==rest.m_branchingPoint)
                return dependencySet;
            else {
                rest=getNextDepdendencySet(rest,branchingPoint);
                for (int index=m_mergeArray.size()-1;index>=0;--index)
                    rest=getNextDepdendencySet(rest,m_mergeArray.get(index));
                return rest;
            }
        }
    }
    protected DependencySet getNextDepdendencySet(DependencySet dependencySet,int branchingPoint) {
        int index=(dependencySet.hashCode()+branchingPoint) & (m_entries.length-1);
        DependencySet nextSet=m_entries[index];
        while (nextSet!=null) {
            if (nextSet.m_rest==dependencySet && nextSet.m_branchingPoint==branchingPoint)
                return nextSet;
            nextSet=nextSet.m_nextEntry;
        }
        nextSet=new DependencySet(dependencySet,branchingPoint,m_entries[index]);
        m_entries[index]=nextSet;
        m_size++;
        if (m_size>=m_resizeThreshold)
            resizeEntries();
        return nextSet;
    }
    protected void resizeEntries() {
        int newLength=m_entries.length*2;
        int newLengthMinusOne=newLength-1;
        DependencySet[] newEntries=new DependencySet[newLength];
        for (int oldIndex=0;oldIndex<m_entries.length;oldIndex++) {
            DependencySet entry=m_entries[oldIndex];
            while (entry!=null) {
                DependencySet nextEntry=entry.m_nextEntry;
                int newIndex=(entry.m_rest.hashCode()+entry.m_branchingPoint) & newLengthMinusOne;
                entry.m_nextEntry=newEntries[newIndex];
                newEntries[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_entries=newEntries;
        m_resizeThreshold=(int)(m_entries.length*0.75);
    }
    public DependencySet removeBranchingPoint(DependencySet dependencySet,int branchingPoint) {
        if (branchingPoint==dependencySet.m_branchingPoint)
            return dependencySet.m_rest;
        else if (branchingPoint>dependencySet.m_branchingPoint)
            return dependencySet;
        else {
            m_mergeArray.clear();
            DependencySet rest=dependencySet;
            while (branchingPoint<rest.m_branchingPoint) {
                m_mergeArray.add(rest.m_branchingPoint);
                rest=rest.m_rest;
            }
            if (branchingPoint!=rest.m_branchingPoint)
                return dependencySet;
            else {
                rest=rest.m_rest;
                for (int index=m_mergeArray.size()-1;index>=0;--index)
                    rest=getNextDepdendencySet(rest,m_mergeArray.get(index));
                return rest;
            }
        }
    }
    public DependencySet unionWith(DependencySet set1,DependencySet set2) {
        if (set1==set2)
            return set1;
        m_mergeArray.clear();
        while (set1!=set2) {
            if (set1.m_branchingPoint>set2.m_branchingPoint) {
                m_mergeArray.add(set1.m_branchingPoint);
                set1=set1.m_rest;
            }
            else if (set1.m_branchingPoint<set2.m_branchingPoint) {
                m_mergeArray.add(set2.m_branchingPoint);
                set2=set2.m_rest;
            }
            else {
                m_mergeArray.add(set1.m_branchingPoint);
                set1=set1.m_rest;
                set2=set2.m_rest;
            }
        }
        DependencySet result=set1;
        for (int index=m_mergeArray.size()-1;index>=0;--index)
            result=getNextDepdendencySet(result,m_mergeArray.get(index));
        return result;
    }
    
    protected static final class IntegerArray implements Serializable {
        private static final long serialVersionUID=7070190530381846058L;

        protected int[] m_elements;
        protected int m_size;
        
        public IntegerArray() {
            m_elements=new int[64];
            m_size=0;
        }
        public void clear() {
            m_size=0;
        }
        public int size() {
            return m_size;
        }
        public int get(int index) {
            return m_elements[index];
        }
        public void add(int element) {
            if (m_size>=m_elements.length) {
                int[] newElements=new int[m_elements.length*3/2];
                System.arraycopy(m_elements,0,newElements,0,m_elements.length);
                m_elements=newElements;
            }
            m_elements[m_size++]=element;
        }
    }
}
