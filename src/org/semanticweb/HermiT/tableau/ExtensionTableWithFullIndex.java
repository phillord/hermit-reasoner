// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * This extension table is for use with Description Graphs and it supports tuple 
 * tables with arity greater than three, but are, as a result, less efficient.
 * @see ExtensionTableWithTupleIndexes
 */
public class ExtensionTableWithFullIndex extends ExtensionTable {
    private static final long serialVersionUID=2856811178050960058L;

    protected final TupleTableFullIndex m_tupleTableFullIndex;
    protected final Object[] m_auxiliaryTuple;
    
    public ExtensionTableWithFullIndex(Tableau tableau,int tupleArity,boolean needsDependencySets) {
        super(tableau,tupleArity,needsDependencySets);
        m_tupleTableFullIndex=new TupleTableFullIndex(m_tupleTable,m_tupleArity);
        m_auxiliaryTuple=new Object[m_tupleArity];
    }
    public int sizeInMemory() {
        return m_tupleTable.sizeInMemory()+m_tupleTableFullIndex.sizeInMemory();
    }
    public boolean addTuple(Object[] tuple,DependencySet dependencySet,boolean isCore) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactStarted(tuple,isCore);
        if (isTupleActive(tuple) && (m_tableau.m_needsThingExtension || !AtomicConcept.THING.equals(tuple[0])) && !DatatypeRestriction.RDFS_LITERAL.equals(tuple[0])) {
            int firstFreeTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
            int addTupleIndex=m_tupleTableFullIndex.addTuple(tuple,firstFreeTupleIndex);
            if (addTupleIndex==firstFreeTupleIndex) {
                m_tupleTable.addTuple(tuple);
                m_dependencySetManager.setDependencySet(addTupleIndex,dependencySet);
                m_coreManager.setCore(addTupleIndex,isCore);
                m_afterDeltaNewTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.addFactFinished(tuple,isCore,true);
                postAdd(tuple,dependencySet,addTupleIndex,isCore);
                return true;
            }
            if (isCore && !m_coreManager.isCore(addTupleIndex)) {
                m_coreManager.addCore(addTupleIndex);
                Object dlPredicateObject=tuple[0];
                if (dlPredicateObject instanceof Concept)
                    m_tableau.m_existentialExpansionStrategy.assertionCoreSet((Concept)dlPredicateObject,(Node)tuple[1]);
                else if (dlPredicateObject instanceof AtomicRole)
                    m_tableau.m_existentialExpansionStrategy.assertionCoreSet((AtomicRole)dlPredicateObject,(Node)tuple[1],(Node)tuple[2]);
            }
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactFinished(tuple,isCore,false);
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
    public boolean isCore(Object[] tuple) {
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        if (tupleIndex==-1)
            return false;
        else
            return m_coreManager.isCore(tupleIndex);
    }
    public Retrieval createRetrieval(int[] bindingPositions,Object[] bindingsBuffer,View extensionView) {
        int numberOfBindings=0;
        for (int index=m_tupleArity-1;index>=0;--index)
            if (bindingPositions[index]!=-1)
                numberOfBindings++;
        if (numberOfBindings==m_tupleArity)
            return new IndexedRetrieval(bindingPositions,bindingsBuffer,extensionView);
        else
            return new UnindexedRetrieval(bindingPositions,bindingsBuffer,extensionView);
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

        protected final int[] m_bindingPositions;
        protected final Object[] m_bindingsBuffer;
        protected final ExtensionTable.View m_extensionView;
        protected final Object[] m_reorderedBindingsBuffer;
        protected final Object[] m_tupleBuffer;
        protected int m_currentTupleIndex;

        public IndexedRetrieval(int[] bindingPositions,Object[] bindingsBuffer,View extensionView) {
            m_bindingPositions=bindingPositions;
            m_bindingsBuffer=new Object[m_tupleArity];
            m_extensionView=extensionView;
            m_reorderedBindingsBuffer=new Object[m_tupleArity];
            m_tupleBuffer=new Object[m_tupleArity];
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTableWithFullIndex.this;
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
            if (m_currentTupleIndex==-1)
                return null;
            else
                return m_dependencySetManager.getDependencySet(m_currentTupleIndex);
        }
        public boolean isCore() {
            if (m_currentTupleIndex==-1)
                return false;
            else
                return m_coreManager.isCore(m_currentTupleIndex);
        }
        public void open() {
            for (int index=m_bindingPositions.length-1;index>=0;--index)
                m_reorderedBindingsBuffer[index]=m_bindingsBuffer[m_bindingPositions[index]];
            m_currentTupleIndex=m_tupleTableFullIndex.getTupleIndex(m_reorderedBindingsBuffer);
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
        public int getCurrentTupleIndex() {
            return m_currentTupleIndex;
        }
        public void next() {
            m_currentTupleIndex++;
        }
    }
}
