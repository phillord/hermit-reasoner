package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class ExtensionTableWithFullIndex extends ExtensionTable {
    private static final long serialVersionUID=2856811178050960058L;

    protected final TupleTableFullIndex m_tupleTableFullIndex;
    protected final Object[] m_auxiliaryTuple;
    
    public ExtensionTableWithFullIndex(Tableau tableau,ExtensionManager extensionManager,int tupleArity,boolean needsDependencySets) {
        super(tableau,extensionManager,tupleArity,needsDependencySets);
        m_tupleTableFullIndex=new TupleTableFullIndex(m_tupleTable,m_tupleArity);
        m_auxiliaryTuple=new Object[m_tupleArity];
    }
    public int sizeInMemory() {
        return m_tupleTable.sizeInMemory()+m_tupleTableFullIndex.sizeInMemory();
    }
    public boolean addTuple(Object[] tuple,DependencySet[] dependencySets) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactStarted(tuple);
        if (isTupleActive(tuple) && (m_tableau.m_needsThingExtension || !AtomicConcept.THING.equals(tuple[0]))) {
            int firstFreeTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
            if (m_tupleTableFullIndex.addTuple(tuple,firstFreeTupleIndex)==firstFreeTupleIndex) {
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
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        return tupleIndex!=-1 && isTupleActive(tupleIndex);
    }
    public DependencySet getDependencySet(Object[] tuple) {
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        if (tupleIndex==-1)
            return null;
        else
            return m_dependencySetManager.getDependencySet(tupleIndex);
    }
    public Retrieval createRetrieval(boolean[] bindingPattern,View extensionView) {
        int numberOfBindings=0;
        for (int index=m_tupleArity-1;index>=0;--index)
            if (bindingPattern[index])
                numberOfBindings++;
        if (numberOfBindings==m_tupleArity)
            return new IndexedRetrieval(extensionView,bindingPattern);
        else
            return new UnindexedRetrieval(extensionView,bindingPattern);
    }
    protected void removeTuple(int tupleIndex) {
        m_tupleTableFullIndex.removeTuple(tupleIndex);
        m_tupleTable.retrieveTuple(m_auxiliaryTuple,tupleIndex);
        postRemove(m_auxiliaryTuple,tupleIndex);
    }
    public void clear() {
        super.clear();
        m_tupleTableFullIndex.clear();
    }

    protected class IndexedRetrieval implements Retrieval,Serializable {
        private static final long serialVersionUID=5984560476970027366L;

        protected final ExtensionTable.View m_extensionView;
        protected final boolean[] m_bindingPattern;
        protected final Object[] m_bindingsBuffer;
        protected final Object[] m_tupleBuffer;
        protected int m_currentTupleIndex;

        public IndexedRetrieval(ExtensionTable.View extensionView,boolean[] bindingPattern) {
            m_extensionView=extensionView;
            m_bindingPattern=bindingPattern;
            m_bindingsBuffer=new Object[m_tupleArity];
            m_tupleBuffer=new Object[m_tupleArity];
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTableWithFullIndex.this;
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
            if (m_currentTupleIndex==-1)
                return null;
            else
                return m_dependencySetManager.getDependencySet(m_currentTupleIndex);
        }
        public void open() {
            m_currentTupleIndex=m_tupleTableFullIndex.getTupleIndex(m_bindingsBuffer);
            switch (m_extensionView) {
            case EXTENSION_THIS:
                if (!(0<=m_currentTupleIndex && m_currentTupleIndex<m_afterExtensionThisTupleIndex))
                    m_currentTupleIndex=-1;
                break;
            case EXTENSION_OLD:
                if (!(0<=m_currentTupleIndex && m_currentTupleIndex<m_afterExtensionOldTupleIndex))
                    m_currentTupleIndex=-1;
                break;
            case DELTA_OLD:
                if (!(m_afterExtensionOldTupleIndex<=m_currentTupleIndex && m_currentTupleIndex<m_afterExtensionThisTupleIndex))
                    m_currentTupleIndex=-1;
                break;
            case TOTAL:
                if (!(0<=m_currentTupleIndex && m_currentTupleIndex<m_afterDeltaNewTupleIndex))
                    m_currentTupleIndex=-1;
                break;
            }
            if (m_currentTupleIndex!=-1) {
                m_tupleTable.retrieveTuple(m_tupleBuffer,m_currentTupleIndex);
                if (!isTupleActive(m_tupleBuffer))
                    m_currentTupleIndex=-1;
            }
        }
        public boolean afterLast() {
            return m_currentTupleIndex==-1;
        }
        public void next() {
            m_currentTupleIndex++;
        }
    }
}
