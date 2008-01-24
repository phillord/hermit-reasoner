package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public final class DependencySetFactory implements Serializable {
    private static final long serialVersionUID=8632867055646817311L;

    protected final IntegerArray m_mergeArray;
    protected final List<DependencySet> m_mergeSets;
    protected final DependencySet m_emptySet;
    protected DependencySet m_firstUnusedSet;
    protected DependencySet m_firstDestroyedSet;
    protected DependencySet[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;

    public DependencySetFactory() {
        m_mergeArray=new IntegerArray();
        m_mergeSets=new ArrayList<DependencySet>();
        m_emptySet=new DependencySet();
        clear();
    }
    public int size() {
        return m_entries.length*4+m_size*20;
    }
    public void clear() {
        m_mergeArray.clear();
        m_mergeSets.clear();
        m_emptySet.m_nextEntry=null;
        m_emptySet.m_usageCounter=1;
        m_emptySet.m_previousUnusedSet=null;
        m_emptySet.m_nextUnusedSet=null;
        m_firstUnusedSet=null;
        m_firstDestroyedSet=null;
        m_entries=new DependencySet[16];
        m_size=0;
        m_resizeThreshold=(int)(m_entries.length*0.75);
    }
    public void cleanUp() {
        while (m_firstUnusedSet!=null)
            destroyDependencySet(m_firstUnusedSet);
    }
    public DependencySet emptySet() {
        return m_emptySet;
    }
    public void addUsage(DependencySet dependencySet) {
        incrementUsageCounter(dependencySet);
    }
    public void removeUsage(DependencySet dependencySet) {
        decrementUsageCounter(dependencySet);
    }
    public DependencySet addBranchingPoint(DependencySet dependencySet,int branchingPoint) {
        if (branchingPoint>dependencySet.m_branchingPoint)
            return getDepdendencySet(dependencySet,branchingPoint);
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
                rest=getDepdendencySet(rest,branchingPoint);
                for (int index=m_mergeArray.size()-1;index>=0;--index)
                    rest=getDepdendencySet(rest,m_mergeArray.get(index));
                return rest;
            }
        }
    }
    protected DependencySet getDepdendencySet(DependencySet rest,int branchingPoint) {
        int index=(rest.hashCode()+branchingPoint) & (m_entries.length-1);
        DependencySet dependencySet=m_entries[index];
        while (dependencySet!=null) {
            if (dependencySet.m_rest==rest && dependencySet.m_branchingPoint==branchingPoint)
                return dependencySet;
            dependencySet=dependencySet.m_nextEntry;
        }
        dependencySet=createDependencySet(rest,branchingPoint);
        dependencySet.m_nextEntry=m_entries[index];
        m_entries[index]=dependencySet;
        if (m_size>=m_resizeThreshold)
            resizeEntries();
        return dependencySet;
    }
    protected DependencySet createDependencySet(DependencySet rest,int branchingPoint) {
        DependencySet newSet;
        if (m_firstDestroyedSet==null)
            newSet=new DependencySet();
        else {
            newSet=m_firstDestroyedSet;
            m_firstDestroyedSet=m_firstDestroyedSet.m_nextEntry;
        }
        newSet.m_rest=rest;
        newSet.m_branchingPoint=branchingPoint;
        newSet.m_usageCounter=0;
        incrementUsageCounter(newSet.m_rest);
        addToUnusedList(newSet);
        m_size++;
        return newSet;
    }
    protected void destroyDependencySet(DependencySet dependencySet) {
        assert dependencySet.m_branchingPoint>=0;
        assert dependencySet.m_usageCounter==0;
        assert dependencySet.m_rest.m_usageCounter>0;
        removeFromUnusedList(dependencySet);
        decrementUsageCounter(dependencySet.m_rest);
        removeFromEntries(dependencySet);
        dependencySet.m_rest=null;
        dependencySet.m_branchingPoint=-2;
        dependencySet.m_nextEntry=m_firstDestroyedSet;
        m_firstDestroyedSet=dependencySet;
        m_size--;
    }
    protected void removeFromEntries(DependencySet dependencySet) {
        int index=(dependencySet.m_rest.hashCode()+dependencySet.m_branchingPoint) & (m_entries.length-1);
        DependencySet lastEntry=null;
        DependencySet entry=m_entries[index];
        while (entry!=null) {
            if (entry==dependencySet) {
                if (lastEntry==null)
                    m_entries[index]=dependencySet.m_nextEntry;
                else
                    lastEntry.m_nextEntry=dependencySet.m_nextEntry;
                return;
            }
            lastEntry=entry;
            entry=entry.m_nextEntry;
        }
        throw new IllegalStateException("Internal error: dependency set not found in the entry table.");
    }
    protected void incrementUsageCounter(DependencySet dependencySet) {
        assert dependencySet.m_branchingPoint>=-1;
        if (dependencySet.m_usageCounter==0)
            removeFromUnusedList(dependencySet);
        dependencySet.m_usageCounter++;
    }
    protected void decrementUsageCounter(DependencySet dependencySet) {
        assert dependencySet.m_usageCounter>0;
        assert dependencySet.m_previousUnusedSet==null;
        assert dependencySet.m_nextUnusedSet==null;
        dependencySet.m_usageCounter--;
        if (dependencySet.m_usageCounter==0)
            addToUnusedList(dependencySet);
    }
    protected void removeFromUnusedList(DependencySet dependencySet) {
        if (dependencySet.m_previousUnusedSet!=null)
            dependencySet.m_previousUnusedSet.m_nextUnusedSet=dependencySet.m_nextUnusedSet;
        else
            m_firstUnusedSet=dependencySet.m_nextUnusedSet;
        if (dependencySet.m_nextUnusedSet!=null)
            dependencySet.m_nextUnusedSet.m_previousUnusedSet=dependencySet.m_previousUnusedSet;
        dependencySet.m_previousUnusedSet=null;
        dependencySet.m_nextUnusedSet=null;
        assert m_firstUnusedSet==null || m_firstUnusedSet.m_usageCounter==0;
    }
    protected void addToUnusedList(DependencySet dependencySet) {
        dependencySet.m_previousUnusedSet=null;
        dependencySet.m_nextUnusedSet=m_firstUnusedSet;
        if (m_firstUnusedSet!=null)
            m_firstUnusedSet.m_previousUnusedSet=dependencySet;
        m_firstUnusedSet=dependencySet;
        assert m_firstUnusedSet==null || m_firstUnusedSet.m_usageCounter==0;
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
                    rest=getDepdendencySet(rest,m_mergeArray.get(index));
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
            result=getDepdendencySet(result,m_mergeArray.get(index));
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
            result=getDepdendencySet(result,m_mergeArray.get(index));
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
        java.util.Set<DependencySet> usedSets=new java.util.HashSet<DependencySet>();
        loadDS(usedSets,extensionManager.getBinaryExtensionTable());
        loadDS(usedSets,extensionManager.getTernaryExtensionTable());
        System.out.println("  Factory contains "+m_size+" dependency sets. Of that, "+usedSets.size()+" sets are used in the extensions.");
    }
    protected void loadDS(java.util.Set<DependencySet> set,ExtensionTable extensionTable) {
        int max=extensionTable.m_afterDeltaNewTupleIndex;
        for (int index=0;index<max;index++) {
            DependencySet ds=extensionTable.m_dependencySetManager.getDependencySet(index);
            while (ds!=null) {
                set.add(ds);
                ds=ds.m_rest;
            }
        }
    }
}
