package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public final class DependencySetFactory implements Serializable {
    private static final long serialVersionUID=8632867055646817311L;

    protected final IntegerArray m_mergeArray;
    protected final List<DependencySet> m_mergeSets;
    protected final DependencySet m_emptySet;
    protected DependencySet[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;

    public DependencySetFactory() {
        m_mergeArray=new IntegerArray();
        m_mergeSets=new ArrayList<DependencySet>();
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
    public DependencySet unionSetsPlusOne(DependencySet set,DependencySet[] sets) {
        if (sets.length==0)
            return set;
        m_mergeSets.clear();
        m_mergeSets.add(set);
        for (DependencySet set1 : sets)
            m_mergeSets.add(set1);
        return unionSetsInternal();
    }
    public DependencySet unionSets(DependencySet[] sets) {
        if (sets.length==1)
            return sets[0];
        m_mergeSets.clear();
        for (DependencySet set : sets)
            m_mergeSets.add(set);
        return unionSetsInternal();
    }
    protected DependencySet unionSetsInternal() {
        int numberOfSets=m_mergeSets.size();
        m_mergeArray.clear();
        while (true) {
            DependencySet firstSet=m_mergeSets.get(0);
            int maximal=firstSet.m_branchingPoint;
            int maximalIndex=0;
            boolean hasEquals=false;
            boolean allAreEqual=true;
            for (int index=1;index<numberOfSets;index++) {
                DependencySet dependencySet=m_mergeSets.get(index);
                int branchingPoint=dependencySet.m_branchingPoint;
                if (branchingPoint>maximal) {
                    maximal=branchingPoint;
                    hasEquals=false;
                    maximalIndex=index;
                }
                else if (branchingPoint==maximal)
                    hasEquals=true;
                if (dependencySet!=firstSet)
                    allAreEqual=false;
            }
            if (allAreEqual)
                break;
            m_mergeArray.add(maximal);
            if (hasEquals) {
                for (int index=0;index<numberOfSets;index++) {
                    DependencySet dependencySet=m_mergeSets.get(index);
                    if (dependencySet.m_branchingPoint==maximal)
                        m_mergeSets.set(index,dependencySet.m_rest);
                }
            }
            else {
                DependencySet dependencySet=m_mergeSets.get(maximalIndex);
                m_mergeSets.set(maximalIndex,dependencySet.m_rest);
            }
        }
        DependencySet result=m_mergeSets.get(0);
        for (int index=m_mergeArray.size()-1;index>=0;--index)
            result=getNextDepdendencySet(result,m_mergeArray.get(index));
        m_mergeSets.clear();
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
    
    public void doStats(ExtensionManager extensionManager) {
        java.util.Set<DependencySet> unusedSets=new java.util.HashSet<DependencySet>();
        for (int index=m_entries.length-1;index>=0;--index) {
            DependencySet ds=m_entries[index];
            while (ds!=null) {
                unusedSets.add(ds);
                ds=ds.m_rest;
            }
        }
        ExtensionTable.Retrieval retrieval=extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        while (!retrieval.afterLast()) {
            DependencySet ds=retrieval.getDependencySet();
            while (unusedSets.remove(ds))
                ds=ds.m_rest;
            retrieval.next();
        }
        retrieval=extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        while (!retrieval.afterLast()) {
            DependencySet ds=retrieval.getDependencySet();
            while (unusedSets.remove(ds))
                ds=ds.m_rest;
            retrieval.next();
        }
        System.out.println("  Factory contains "+m_size+" dependency sets. Of that, "+unusedSets.size()+" sets are not used in the extensions.");
    }
}
