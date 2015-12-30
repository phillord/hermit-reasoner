/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.InternalDatatype;

/**
 * This extension table is for use with Description Graphs and it supports tuple
 * tables with arity greater than three, but are, as a result, less efficient.
 * @see ExtensionTableWithTupleIndexes
 */
public class ExtensionTableWithFullIndex extends ExtensionTable {
    private static final long serialVersionUID=2856811178050960058L;

    protected final TupleTableFullIndex m_tupleTableFullIndex;
    protected final Object[] m_auxiliaryTuple;

    /**
     * @param tableau tableau
     * @param tupleArity tupleArity
     * @param needsDependencySets needsDependencySets
     */
    public ExtensionTableWithFullIndex(Tableau tableau,int tupleArity,boolean needsDependencySets) {
        super(tableau,tupleArity,needsDependencySets);
        m_tupleTableFullIndex=new TupleTableFullIndex(m_tupleTable,m_tupleArity);
        m_auxiliaryTuple=new Object[m_tupleArity];
    }
    @Override
    public int sizeInMemory() {
        return m_tupleTable.sizeInMemory()+m_tupleTableFullIndex.sizeInMemory();
    }
    @Override
    public boolean addTuple(Object[] tuple,DependencySet dependencySet,boolean isCore) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.addFactStarted(tuple,isCore);
        if (isTupleActive(tuple) && (m_tableau.m_needsThingExtension || !AtomicConcept.THING.equals(tuple[0])) && (m_tableau.m_needsRDFSLiteralExtension || !InternalDatatype.RDFS_LITERAL.equals(tuple[0]))) {
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
    @Override
    public boolean containsTuple(Object[] tuple) {
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        return tupleIndex!=-1 && isTupleActive(tupleIndex);
    }
    @Override
    public DependencySet getDependencySet(Object[] tuple) {
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        if (tupleIndex==-1)
            return null;
        else
            return m_dependencySetManager.getDependencySet(tupleIndex);
    }
    @Override
    public boolean isCore(Object[] tuple) {
        int tupleIndex=m_tupleTableFullIndex.getTupleIndex(tuple);
        if (tupleIndex==-1)
            return false;
        else
            return m_coreManager.isCore(tupleIndex);
    }
    @Override
    public Retrieval createRetrieval(int[] bindingPositions,Object[] bindingsBuffer,Object[] tupleBuffer,boolean ownsBuffers,View extensionView) {
        int numberOfBindings=0;
        for (int index=m_tupleArity-1;index>=0;--index)
            if (bindingPositions[index]!=-1)
                numberOfBindings++;
        if (numberOfBindings==m_tupleArity)
            return new IndexedRetrieval(bindingPositions,bindingsBuffer,tupleBuffer,ownsBuffers,extensionView);
        else
            return new UnindexedRetrieval(bindingPositions,bindingsBuffer,tupleBuffer,ownsBuffers,extensionView);
    }
    @Override
    protected void removeTuple(int tupleIndex) {
        m_tupleTableFullIndex.removeTuple(tupleIndex);
        m_tupleTable.retrieveTuple(m_auxiliaryTuple,tupleIndex);
        postRemove(m_auxiliaryTuple,tupleIndex);
    }
    @Override
    public void clear() {
        super.clear();
        m_tupleTableFullIndex.clear();
    }

    protected class IndexedRetrieval implements Retrieval,Serializable {
        private static final long serialVersionUID=5984560476970027366L;

        protected final int[] m_bindingPositions;
        protected final Object[] m_bindingsBuffer;
        protected final Object[] m_tupleBuffer;
        protected final boolean m_ownsBuffers;
        protected final ExtensionTable.View m_extensionView;
        protected int m_currentTupleIndex;

        public IndexedRetrieval(int[] bindingPositions,Object[] bindingsBuffer,Object[] tupleBuffer,boolean ownsBuffers,View extensionView) {
            m_bindingPositions=bindingPositions;
            m_bindingsBuffer=bindingsBuffer;
            m_tupleBuffer=tupleBuffer;
            m_ownsBuffers=ownsBuffers;
            m_extensionView=extensionView;
        }
        @Override
        public ExtensionTable getExtensionTable() {
            return ExtensionTableWithFullIndex.this;
        }
        @Override
        public void clear() {
            if (m_ownsBuffers) {
                for (int index=m_bindingsBuffer.length-1;index>=0;--index)
                    m_bindingsBuffer[index]=null;
                for (int index=m_tupleBuffer.length-1;index>=0;--index)
                    m_tupleBuffer[index]=null;
            }
        }
        @Override
        public int[] getBindingPositions() {
            return m_bindingPositions;
        }
        @Override
        public Object[] getBindingsBuffer() {
            return m_bindingsBuffer;
        }
        @Override
        public Object[] getTupleBuffer() {
            return m_tupleBuffer;
        }
        @Override
        public DependencySet getDependencySet() {
            if (m_currentTupleIndex==-1)
                return null;
            else
                return m_dependencySetManager.getDependencySet(m_currentTupleIndex);
        }
        @Override
        public boolean isCore() {
            if (m_currentTupleIndex==-1)
                return false;
            else
                return m_coreManager.isCore(m_currentTupleIndex);
        }
        @Override
        public void open() {
            m_currentTupleIndex=m_tupleTableFullIndex.getTupleIndex(m_bindingsBuffer,m_bindingPositions);
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
            default:
                break;
            }
            if (m_currentTupleIndex!=-1) {
                m_tupleTable.retrieveTuple(m_tupleBuffer,m_currentTupleIndex);
                if (!isTupleActive(m_tupleBuffer))
                    m_currentTupleIndex=-1;
            }
        }
        @Override
        public boolean afterLast() {
            return m_currentTupleIndex==-1;
        }
        @Override
        public int getCurrentTupleIndex() {
            return m_currentTupleIndex;
        }
        @Override
        public void next() {
            m_currentTupleIndex++;
        }
    }
}
