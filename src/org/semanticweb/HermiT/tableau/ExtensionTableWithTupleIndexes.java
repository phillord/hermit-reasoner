// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class ExtensionTableWithTupleIndexes extends ExtensionTable {
    private static final long serialVersionUID=-684536236157965372L;

    protected final TupleIndex[] m_tupleIndexes;
    protected final Object[] m_auxiliaryTuple;
    
    public ExtensionTableWithTupleIndexes(Tableau tableau,ExtensionManager extensionManager,int tupleArity,boolean needsDependencySets,TupleIndex[] tupleIndexes) {
        super(tableau,extensionManager,tupleArity,needsDependencySets);
        m_tupleIndexes=tupleIndexes;
        m_auxiliaryTuple=new Object[m_tupleArity];
    }
    public int sizeInMemory() {
        int size=m_tupleTable.sizeInMemory();
        for (int i=m_tupleIndexes.length-1;i>=0;--i)
            size+=m_tupleIndexes[i].sizeInMemoy();
        return size;
    }
    public boolean addTuple(Object[] tuple,DependencySet dependencySet) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactStarted(tuple);
        if (isTupleActive(tuple) && (m_tableau.m_needsThingExtension || !AtomicConcept.THING.equals(tuple[0]))) {
            int firstFreeTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
            if (m_tupleIndexes[0].addTuple(tuple,firstFreeTupleIndex)) {
                for (int index=1;index<m_tupleIndexes.length;index++)
                    m_tupleIndexes[index].addTuple(tuple,firstFreeTupleIndex);
                m_tupleTable.addTuple(tuple);
                m_dependencySetManager.setDependencySet(firstFreeTupleIndex,dependencySet);
                m_afterDeltaNewTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.addFactFinished(tuple,true);
                postAdd(tuple,dependencySet,firstFreeTupleIndex);
                return true;
            }
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactFinished(tuple,false);
        return false;
    }
    public boolean containsTuple(Object[] tuple) {
        int tupleIndex=m_tupleIndexes[0].getTupleIndex(tuple);
        return tupleIndex!=-1 && isTupleActive(tupleIndex);
    }
    public DependencySet getDependencySet(Object[] tuple) {
        int tupleIndex=m_tupleIndexes[0].getTupleIndex(tuple);
        if (tupleIndex==-1)
            return null;
        else
            return m_dependencySetManager.getDependencySet(tupleIndex);
    }
    public Retrieval createRetrieval(int[] bindingPositions,Object[] bindingsBuffer,View extensionView) {
        TupleIndex selectedTupleIndex=null;
        int boundPrefixSizeInSelected=0;
        for (int index=m_tupleIndexes.length-1;index>=0;--index) {
            int[] indexingSequence=m_tupleIndexes[index].getIndexingSequence();
            int boundPrefixSize=0;
            for (int position=0;position<indexingSequence.length;position++)
                if (bindingPositions[indexingSequence[position]]!=-1)
                    boundPrefixSize++;
                else
                    break;
            if (boundPrefixSize>boundPrefixSizeInSelected) {
                selectedTupleIndex=m_tupleIndexes[index];
                boundPrefixSizeInSelected=boundPrefixSize;
            }
        }
        if (selectedTupleIndex==null)
            return new UnindexedRetrieval(bindingPositions,bindingsBuffer,extensionView);
        else
            return new IndexedRetrieval(selectedTupleIndex,bindingPositions,bindingsBuffer,extensionView);
    }
    protected void removeTuple(int tupleIndex) {
        m_tupleTable.retrieveTuple(m_auxiliaryTuple,tupleIndex);
        for (int index=m_tupleIndexes.length-1;index>=0;--index)
            m_tupleIndexes[index].removeTuple(m_auxiliaryTuple);
        postRemove(m_auxiliaryTuple,tupleIndex);
    }
    public void clear() {
        super.clear();
        for (int index=m_tupleIndexes.length-1;index>=0;--index)
            m_tupleIndexes[index].clear();
    }

    protected class IndexedRetrieval extends TupleIndex.TupleIndexRetrieval implements Retrieval,Serializable {
        private static final long serialVersionUID=2180748099314801734L;

        protected final int[] m_bindingPositions;
        protected final ExtensionTable.View m_extensionView;
        protected final boolean m_checkTupleSelection;
        protected final Object[] m_tupleBuffer;
        protected DependencySet m_dependencySet;
        protected int m_firstTupleIndex;
        protected int m_afterLastTupleIndex;

        public IndexedRetrieval(TupleIndex tupleIndex,int[] bindingPositions,Object[] bindingsBuffer,View extensionView) {
            super(tupleIndex,bindingsBuffer,createSelectionArray(bindingPositions,tupleIndex.m_indexingSequence));
            m_bindingPositions=bindingPositions;
            m_extensionView=extensionView;
            m_tupleBuffer=new Object[m_tupleArity];
            int numberOfBoundPositions=0;
            for (int index=m_bindingPositions.length-1;index>=0;--index)
                if (m_bindingPositions[index]!=-1)
                    numberOfBoundPositions++;
            m_checkTupleSelection=(numberOfBoundPositions>m_selectionIndices.length);
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTableWithTupleIndexes.this;
        }
        public ExtensionTable.View getExtensionView() {
            return m_extensionView;
        }
        public int[] getBindingPositions() {
            return m_bindingPositions;
        }
        public Object[] getBindingsBuffer() {
            return m_bindingsBuffer;
        }
        public Object[] getTupleBuffer() {
            return m_tupleBuffer;
        }
        public DependencySet getDependencySet() {
            return m_dependencySet;
        }
        public void open() {
            switch (m_extensionView) {
            case EXTENSION_THIS:
                m_firstTupleIndex=0;
                m_afterLastTupleIndex=m_afterExtensionThisTupleIndex;
                break;
            case EXTENSION_OLD:
                m_firstTupleIndex=0;
                m_afterLastTupleIndex=m_afterExtensionOldTupleIndex;
                break;
            case DELTA_OLD:
                m_firstTupleIndex=m_afterExtensionOldTupleIndex;
                m_afterLastTupleIndex=m_afterExtensionThisTupleIndex;
                break;
            case TOTAL:
                m_firstTupleIndex=0;
                m_afterLastTupleIndex=m_afterDeltaNewTupleIndex;
                break;
            }
            super.open();
            while (!afterLast()) {
                int tupleIndex=getCurrentTupleIndex();
                if (m_firstTupleIndex<=tupleIndex && tupleIndex<m_afterLastTupleIndex) {
                    m_tupleTable.retrieveTuple(m_tupleBuffer,tupleIndex);
                    if (isTupleValid()) {
                        m_dependencySet=m_dependencySetManager.getDependencySet(tupleIndex);
                        return;
                    }
                }
                super.next();
            }
        }
        public void next() {
            super.next();
            while (!afterLast()) {
                int tupleIndex=getCurrentTupleIndex();
                if (m_firstTupleIndex<=tupleIndex && tupleIndex<m_afterLastTupleIndex) {
                    m_tupleTable.retrieveTuple(m_tupleBuffer,tupleIndex);
                    if (isTupleValid()) {
                        m_dependencySet=m_dependencySetManager.getDependencySet(tupleIndex);
                        return;
                    }
                }
                super.next();
            }
        }
        protected boolean isTupleValid() {
            if (!ExtensionTableWithTupleIndexes.this.isTupleActive(m_tupleBuffer))
                return false;
            if (m_checkTupleSelection)
                for (int index=m_bindingPositions.length-1;index>=0;--index)
                    if (m_bindingPositions[index]!=-1 && !m_tupleBuffer[index].equals(m_bindingsBuffer[m_bindingPositions[index]]))
                        return false;
            return true;
        }
    }
    protected static int[] createSelectionArray(int[] bindingPositions,int[] indexingSequence) {
        int boundPrefixLength=0;
        for (int index=0;index<indexingSequence.length;index++)
            if (bindingPositions[indexingSequence[index]]!=-1)
                boundPrefixLength++;
            else
                break;
        int[] selection=new int[boundPrefixLength];
        for (int index=0;index<boundPrefixLength;index++)
            selection[index]=bindingPositions[indexingSequence[index]];
        return selection;
    }
}
