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
    public int size() {
        int size=m_tupleTable.size();
        for (int i=m_tupleIndexes.length-1;i>=0;--i)
            size+=m_tupleIndexes[i].size();
        return size;
    }
    public boolean addTuple(Object[] tuple,DependencySet[] dependencySets) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactStarted(tuple);
        if (isTupleValid(tuple) && (m_tableau.m_needsThingExtension || !AtomicConcept.THING.equals(tuple[0]))) {
            int firstFreeTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
            if (m_tupleIndexes[0].addTuple(tuple,firstFreeTupleIndex)) {
                for (int index=1;index<m_tupleIndexes.length;index++)
                    m_tupleIndexes[index].addTuple(tuple,firstFreeTupleIndex);
                m_tupleTable.addTuple(tuple);
                m_dependencySetManager.setDependencySet(firstFreeTupleIndex,dependencySets);
                m_afterDeltaNewTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.addFactFinished(tuple,true);
                postAdd(tuple,dependencySets,firstFreeTupleIndex);
                return true;
            }
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactFinished(tuple,false);
        return false;
    }
    public boolean containsTuple(Object[] tuple) {
        int tupleIndex=m_tupleIndexes[0].getTupleIndex(tuple);
        return tupleIndex!=-1 && isTupleValid(tupleIndex);
    }
    public DependencySet getDependencySet(Object[] tuple) {
        int tupleIndex=m_tupleIndexes[0].getTupleIndex(tuple);
        if (tupleIndex==-1)
            return null;
        else
            return m_dependencySetManager.getDependencySet(tupleIndex);
    }
    public Retrieval createRetrieval(boolean[] bindingPattern,View extensionView) {
        TupleIndex selectedTupleIndex=null;
        int boundPrefixSizeInSelected=0;
        for (int index=m_tupleIndexes.length-1;index>=0;--index) {
            int[] indexingSequence=m_tupleIndexes[index].getIndexingSequence();
            int boundPrefixSize=0;
            for (int position=0;position<indexingSequence.length;position++)
                if (bindingPattern[indexingSequence[position]])
                    boundPrefixSize++;
                else
                    break;
            if (boundPrefixSize>boundPrefixSizeInSelected) {
                selectedTupleIndex=m_tupleIndexes[index];
                boundPrefixSizeInSelected=boundPrefixSize;
            }
        }
        if (selectedTupleIndex==null)
            return new UnindexedRetrieval(extensionView,bindingPattern);
        else
            return new IndexedRetrieval(selectedTupleIndex,extensionView,bindingPattern);
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

    protected class IndexedRetrieval implements Retrieval,Serializable {
        private static final long serialVersionUID=2180748099314801734L;

        protected final TupleIndex.Retrieval m_tupleIndexRetrieval;
        protected final ExtensionTable.View m_extensionView;
        protected final boolean[] m_bindingPattern;
        protected final boolean m_checkTupleSelection;
        protected final int m_boundPrefixLength;
        protected final Object[] m_bindingsBuffer;
        protected final Object[] m_tupleBuffer;
        protected DependencySet m_dependencySet;
        protected int m_firstTupleIndex;
        protected int m_afterLastTupleIndex;
        protected boolean m_afterLast;

        public IndexedRetrieval(TupleIndex tupleIndex,ExtensionTable.View extensionView,boolean[] bindingPattern) {
            m_tupleIndexRetrieval=tupleIndex.createRetrieval();
            m_extensionView=extensionView;
            m_bindingPattern=bindingPattern;
            m_bindingsBuffer=new Object[m_tupleArity];
            m_tupleBuffer=new Object[m_tupleArity];
            int boundPrefixLength=0;
            int[] indexingSequence=tupleIndex.getIndexingSequence();
            for (int index=0;index<indexingSequence.length;index++)
                if (m_bindingPattern[indexingSequence[index]])
                    boundPrefixLength++;
                else
                    break;
            m_boundPrefixLength=boundPrefixLength;
            m_tupleIndexRetrieval.setNumberOfSelectionPositions(m_boundPrefixLength);
            int numberOfBoundPositions=0;
            for (int index=m_bindingPattern.length-1;index>=0;--index)
                if (m_bindingPattern[index])
                    numberOfBoundPositions++;
            m_checkTupleSelection=(numberOfBoundPositions>boundPrefixLength);
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTableWithTupleIndexes.this;
        }
        public ExtensionTable.View getExtensionView() {
            return m_extensionView;
        }
        public boolean[] getBindingPattern() {
            return m_bindingPattern;
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
            Object[] selectionBuffer=m_tupleIndexRetrieval.getSelectionBuffer();
            int[] indexingSequence=m_tupleIndexRetrieval.getIndexingSequence();
            for (int index=0;index<m_boundPrefixLength;index++)
                selectionBuffer[index]=m_bindingsBuffer[indexingSequence[index]];
            m_tupleIndexRetrieval.open();
            m_afterLast=false;
            while (!m_tupleIndexRetrieval.afterLast()) {
                int tupleIndex=m_tupleIndexRetrieval.currentTupleIndex();
                if (m_firstTupleIndex<=tupleIndex && tupleIndex<m_afterLastTupleIndex) {
                    m_tupleTable.retrieveTuple(m_tupleBuffer,tupleIndex);
                    if (isTupleValid()) {
                        m_dependencySet=m_dependencySetManager.getDependencySet(tupleIndex);
                        return;
                    }
                }
                m_tupleIndexRetrieval.next();
            }
            m_afterLast=true;
        }
        public boolean afterLast() {
            return m_afterLast;
        }
        public void next() {
            m_tupleIndexRetrieval.next();
            while (!m_tupleIndexRetrieval.afterLast()) {
                int tupleIndex=m_tupleIndexRetrieval.currentTupleIndex();
                if (m_firstTupleIndex<=tupleIndex && tupleIndex<m_afterLastTupleIndex) {
                    m_tupleTable.retrieveTuple(m_tupleBuffer,tupleIndex);
                    if (isTupleValid()) {
                        m_dependencySet=m_dependencySetManager.getDependencySet(tupleIndex);
                        return;
                    }
                }
                m_tupleIndexRetrieval.next();
            }
            m_afterLast=true;
        }
        protected boolean isTupleValid() {
            if (!ExtensionTableWithTupleIndexes.this.isTupleValid(m_tupleBuffer))
                return false;
            if (m_checkTupleSelection)
                for (int index=m_bindingPattern.length-1;index>=0;--index)
                    if (m_bindingPattern[index] && !m_tupleBuffer[index].equals(m_bindingsBuffer[index]))
                        return false;
            return true;
        }
    }
}
