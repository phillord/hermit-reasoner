// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class to work with dependency sets and returns instances of 
 * PermanentDependencySet, which can not directly be created. Dependency sets 
 * are either permanent (in case they are used for a longer time and more 
 * frequently) or temporary. The temporary ones are instances of the class 
 * UnionDependencySet and they can be created directly. If a temporary 
 * dependency sets is used more frequently, it can be turned into a permanent 
 * one by this factory.
 * 
 * WARNING: Java has a really strange bug: the field m_firstUnusedSet
 * sometimes mysteriously changes its value and then points to some random
 * PermanentDependencySet object from some previous tableau run.
 * Just to exclude the possibility of this being a programming error,
 * this class keeps track of the "generation" in which it is used.
 * Each time the factory is cleared, the generation is increased.
 * Then, the generation number is used to check whether the factory
 * is given objects from the current generation. This is then used
 * to detect the error and to try to fix up the internal structure of the factory. 
 */
public final class DependencySetFactory implements Serializable {
    private static final long serialVersionUID=8632867055646817311L;

    protected final IntegerArray m_mergeArray;
    protected final List<PermanentDependencySet> m_mergeSets;
    protected final List<UnionDependencySet> m_unprocessedSets;
    protected PermanentDependencySet m_emptySet;
    protected PermanentDependencySet m_firstUnusedSet;
    protected PermanentDependencySet m_firstDestroyedSet;
    protected PermanentDependencySet[] m_entries;
    protected int m_size;
    protected int m_resizeThreshold;
    protected int m_generation;

    public DependencySetFactory() {
        m_mergeArray=new IntegerArray();
        m_mergeSets=new ArrayList<PermanentDependencySet>();
        m_unprocessedSets=new ArrayList<UnionDependencySet>();
        clear();
    }
    public int sizeInMemory() {
        return m_entries.length*4+m_size*20;
    }
    public void clear() {
        m_generation++;
        m_mergeArray.clear();
        m_mergeSets.clear();
        m_unprocessedSets.clear();
        m_emptySet=new PermanentDependencySet(m_generation);
        m_emptySet.m_branchingPoint=-1;
        m_emptySet.m_usageCounter=1;
        m_emptySet.m_rest=null;
        m_emptySet.m_previousUnusedSet=null;
        m_emptySet.m_nextUnusedSet=null;
        m_firstUnusedSet=null;
        m_firstDestroyedSet=null;
        m_entries=new PermanentDependencySet[16];
        m_resizeThreshold=(int)(m_entries.length*0.75);
        m_size=0;
    }
    public PermanentDependencySet emptySet() {
        return m_emptySet;
    }
    public void removeUnusedSets() {
        boolean errorDetected=false;
        while (m_firstUnusedSet!=null && !errorDetected)
            errorDetected=destroyDependencySet(m_firstUnusedSet);
        if (errorDetected) {
            m_firstUnusedSet=null;
            m_firstDestroyedSet=null;
            Runtime.getRuntime().gc();
            for (int index=0;index<m_entries.length;index++) {
                PermanentDependencySet entry=m_entries[index];
                while (entry!=null) {
                    if (entry.m_generation!=m_generation)
                        throw new IllegalStateException("Internal error due to a bug in Java VM: the dependency set factory is corrupt!");
                    if (entry.m_usageCounter==0) {
                        checkGeneration(entry);
                        entry.m_previousUnusedSet=null;
                        entry.m_nextUnusedSet=m_firstUnusedSet;
                        if (m_firstUnusedSet!=null)
                            m_firstUnusedSet.m_previousUnusedSet=entry;
                        m_firstUnusedSet=entry;
                    }
                    else {
                        entry.m_previousUnusedSet=null;
                        entry.m_nextUnusedSet=null;
                    }
                    entry=entry.m_nextEntry;
                }
            }
            errorDetected=false;
            while (m_firstUnusedSet!=null && !errorDetected)
                errorDetected=destroyDependencySet(m_firstUnusedSet);
            if (errorDetected)
                throw new IllegalStateException("Internal error due to a bug in Java VM: the corruption was detected for the second time in a row, so we are guving up!");
        }
    }
    protected void checkGeneration(PermanentDependencySet dependencySet) {
        while (dependencySet!=null) {
            if (dependencySet.m_generation!=m_generation || (dependencySet!=m_emptySet && !isInEntries(dependencySet)))
                throw new IllegalStateException("Internal error due to a bug in Java VM: the entries list is corrupt!");
            dependencySet=dependencySet.m_rest;
        }
    }
    protected boolean isInEntries(PermanentDependencySet dependencySet) {
        int index=(dependencySet.m_rest.hashCode()+dependencySet.m_branchingPoint) & (m_entries.length-1);
        PermanentDependencySet entry=m_entries[index];
        while (entry!=null) {
            if (entry==dependencySet)
                return true;
            entry=entry.m_nextEntry;
        }
        return false;
    }
    public void addUsage(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        assert dependencySet.m_branchingPoint>=0 || dependencySet==m_emptySet;
        if (dependencySet.m_usageCounter==0)
            removeFromUnusedList(dependencySet);
        dependencySet.m_usageCounter++;
    }
    public void removeUsage(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        assert dependencySet.m_branchingPoint>=0 || dependencySet==m_emptySet;
        assert dependencySet.m_usageCounter>0;
        assert dependencySet.m_previousUnusedSet==null;
        assert dependencySet.m_nextUnusedSet==null;
        dependencySet.m_usageCounter--;
        if (dependencySet.m_usageCounter==0)
            addToUnusedList(dependencySet);
    }
    public PermanentDependencySet addBranchingPoint(DependencySet dependencySet,int branchingPoint) {
        PermanentDependencySet permanentDependencySet=getPermanent(dependencySet);
        if (branchingPoint>permanentDependencySet.m_branchingPoint)
            return getDepdendencySet(permanentDependencySet,branchingPoint);
        else if (branchingPoint==permanentDependencySet.m_branchingPoint)
            return permanentDependencySet;
        else {
            m_mergeArray.clear();
            PermanentDependencySet rest=permanentDependencySet;
            while (branchingPoint<rest.m_branchingPoint) {
                m_mergeArray.add(rest.m_branchingPoint);
                rest=rest.m_rest;
            }
            if (branchingPoint==rest.m_branchingPoint)
                return permanentDependencySet;
            else {
                rest=getDepdendencySet(rest,branchingPoint);
                for (int index=m_mergeArray.size()-1;index>=0;--index)
                    rest=getDepdendencySet(rest,m_mergeArray.get(index));
                return rest;
            }
        }
    }
    protected PermanentDependencySet getDepdendencySet(PermanentDependencySet rest,int branchingPoint) {
        assert rest.m_generation==m_generation;
        int index=(rest.hashCode()+branchingPoint) & (m_entries.length-1);
        PermanentDependencySet dependencySet=m_entries[index];
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
    protected PermanentDependencySet createDependencySet(PermanentDependencySet rest,int branchingPoint) {
        assert rest.m_generation==m_generation;
        PermanentDependencySet newSet;
        if (m_firstDestroyedSet==null)
            newSet=new PermanentDependencySet(m_generation);
        else {
            newSet=m_firstDestroyedSet;
            m_firstDestroyedSet=m_firstDestroyedSet.m_nextEntry;
        }
        newSet.m_rest=rest;
        newSet.m_branchingPoint=branchingPoint;
        newSet.m_usageCounter=0;
        addUsage(newSet.m_rest);
        addToUnusedList(newSet);
        m_size++;
        return newSet;
    }
    protected boolean destroyDependencySet(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        assert dependencySet.m_branchingPoint>=0;
        assert dependencySet.m_usageCounter==0;
        assert dependencySet.m_rest.m_usageCounter>0;
        removeFromUnusedList(dependencySet);
        removeUsage(dependencySet.m_rest);
        boolean errorDetected=removeFromEntries(dependencySet);
        dependencySet.m_rest=null;
        dependencySet.m_branchingPoint=-2;
        dependencySet.m_nextEntry=m_firstDestroyedSet;
        m_firstDestroyedSet=dependencySet;
        m_size--;
        return errorDetected;
    }
    protected boolean removeFromEntries(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        int index=(dependencySet.m_rest.hashCode()+dependencySet.m_branchingPoint) & (m_entries.length-1);
        PermanentDependencySet lastEntry=null;
        PermanentDependencySet entry=m_entries[index];
        while (entry!=null) {
            if (entry==dependencySet) {
                if (lastEntry==null)
                    m_entries[index]=dependencySet.m_nextEntry;
                else
                    lastEntry.m_nextEntry=dependencySet.m_nextEntry;
                return false;
            }
            lastEntry=entry;
            entry=entry.m_nextEntry;
        }
        // Returning true means that we've detected an error.
        return true;
    }
    protected void removeFromUnusedList(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        if (dependencySet.m_previousUnusedSet!=null)
            dependencySet.m_previousUnusedSet.m_nextUnusedSet=dependencySet.m_nextUnusedSet;
        else
            m_firstUnusedSet=dependencySet.m_nextUnusedSet;
        if (dependencySet.m_nextUnusedSet!=null)
            dependencySet.m_nextUnusedSet.m_previousUnusedSet=dependencySet.m_previousUnusedSet;
        dependencySet.m_previousUnusedSet=null;
        dependencySet.m_nextUnusedSet=null;
        assert m_firstUnusedSet==null || (m_firstUnusedSet.m_usageCounter==0 && m_firstUnusedSet.m_generation==m_generation);
    }
    protected void addToUnusedList(PermanentDependencySet dependencySet) {
        assert dependencySet.m_generation==m_generation;
        dependencySet.m_previousUnusedSet=null;
        dependencySet.m_nextUnusedSet=m_firstUnusedSet;
        if (m_firstUnusedSet!=null)
            m_firstUnusedSet.m_previousUnusedSet=dependencySet;
        m_firstUnusedSet=dependencySet;
        assert m_firstUnusedSet==null || (m_firstUnusedSet.m_usageCounter==0 && m_firstUnusedSet.m_generation==m_generation);
    }
    protected void resizeEntries() {
        int newLength=m_entries.length*2;
        int newLengthMinusOne=newLength-1;
        PermanentDependencySet[] newEntries=new PermanentDependencySet[newLength];
        for (int oldIndex=0;oldIndex<m_entries.length;oldIndex++) {
            PermanentDependencySet entry=m_entries[oldIndex];
            while (entry!=null) {
                PermanentDependencySet nextEntry=entry.m_nextEntry;
                int newIndex=(entry.m_rest.hashCode()+entry.m_branchingPoint) & newLengthMinusOne;
                entry.m_nextEntry=newEntries[newIndex];
                newEntries[newIndex]=entry;
                entry=nextEntry;
            }
        }
        m_entries=newEntries;
        m_resizeThreshold=(int)(m_entries.length*0.75);
    }
    public PermanentDependencySet removeBranchingPoint(DependencySet dependencySet,int branchingPoint) {
        PermanentDependencySet permanentDependencySet=getPermanent(dependencySet);
        assert permanentDependencySet.m_generation==m_generation;
        if (branchingPoint==permanentDependencySet.m_branchingPoint)
            return permanentDependencySet.m_rest;
        else if (branchingPoint>permanentDependencySet.m_branchingPoint)
            return permanentDependencySet;
        else {
            m_mergeArray.clear();
            PermanentDependencySet rest=permanentDependencySet;
            while (branchingPoint<rest.m_branchingPoint) {
                m_mergeArray.add(rest.m_branchingPoint);
                rest=rest.m_rest;
            }
            if (branchingPoint!=rest.m_branchingPoint)
                return permanentDependencySet;
            else {
                rest=rest.m_rest;
                for (int index=m_mergeArray.size()-1;index>=0;--index)
                    rest=getDepdendencySet(rest,m_mergeArray.get(index));
                return rest;
            }
        }
    }
    public PermanentDependencySet unionWith(DependencySet set1,DependencySet set2) {
        PermanentDependencySet permanentSet1=getPermanent(set1);
        assert permanentSet1.m_generation==m_generation;
        PermanentDependencySet permanentSet2=getPermanent(set2);
        assert permanentSet2.m_generation==m_generation;
        if (permanentSet1==permanentSet2)
            return permanentSet1;
        m_mergeArray.clear();
        while (permanentSet1!=permanentSet2) {
            if (permanentSet1.m_branchingPoint>permanentSet2.m_branchingPoint) {
                m_mergeArray.add(permanentSet1.m_branchingPoint);
                permanentSet1=permanentSet1.m_rest;
            }
            else if (permanentSet1.m_branchingPoint<permanentSet2.m_branchingPoint) {
                m_mergeArray.add(permanentSet2.m_branchingPoint);
                permanentSet2=permanentSet2.m_rest;
            }
            else {
                m_mergeArray.add(permanentSet1.m_branchingPoint);
                permanentSet1=permanentSet1.m_rest;
                permanentSet2=permanentSet2.m_rest;
            }
        }
        PermanentDependencySet result=permanentSet1;
        for (int index=m_mergeArray.size()-1;index>=0;--index)
            result=getDepdendencySet(result,m_mergeArray.get(index));
        return result;
    }
    public PermanentDependencySet getPermanent(DependencySet dependencySet) {
        if (dependencySet instanceof PermanentDependencySet)
            return (PermanentDependencySet)dependencySet;
        m_unprocessedSets.clear();
        m_mergeSets.clear();
        m_unprocessedSets.add((UnionDependencySet)dependencySet);
        while (!m_unprocessedSets.isEmpty()) {
            UnionDependencySet unionDependencySet=m_unprocessedSets.remove(m_unprocessedSets.size()-1);
            for (DependencySet constituent : unionDependencySet.m_dependencySets) {
                if (constituent instanceof UnionDependencySet)
                    m_unprocessedSets.add((UnionDependencySet)constituent);
                else {
                    PermanentDependencySet permanentSet=(PermanentDependencySet)constituent;
                    assert permanentSet.m_generation==m_generation;
                    m_mergeSets.add(permanentSet);
                }
            }
        }
        int numberOfSets=m_mergeSets.size();
        m_mergeArray.clear();
        while (true) {
            PermanentDependencySet firstSet=m_mergeSets.get(0);
            int maximal=firstSet.m_branchingPoint;
            int maximalIndex=0;
            boolean hasEquals=false;
            boolean allAreEqual=true;
            for (int index=1;index<numberOfSets;index++) {
                PermanentDependencySet permanentDependencySet=m_mergeSets.get(index);
                int branchingPoint=permanentDependencySet.m_branchingPoint;
                if (branchingPoint>maximal) {
                    maximal=branchingPoint;
                    hasEquals=false;
                    maximalIndex=index;
                }
                else if (branchingPoint==maximal)
                    hasEquals=true;
                if (permanentDependencySet!=firstSet)
                    allAreEqual=false;
            }
            if (allAreEqual)
                break;
            m_mergeArray.add(maximal);
            if (hasEquals) {
                for (int index=0;index<numberOfSets;index++) {
                    PermanentDependencySet permanentDependencySet=m_mergeSets.get(index);
                    if (permanentDependencySet.m_branchingPoint==maximal)
                        m_mergeSets.set(index,permanentDependencySet.m_rest);
                }
            }
            else {
                PermanentDependencySet permanentDependencySet=m_mergeSets.get(maximalIndex);
                m_mergeSets.set(maximalIndex,permanentDependencySet.m_rest);
            }
        }
        PermanentDependencySet result=m_mergeSets.get(0);
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
}
