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
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.NegatedAtomicRole;
import org.semanticweb.HermiT.monitor.TableauMonitor;

/**
 * An extension table keeps track of the assertions in the ABox during a run of
 * the tableau. For this purpose, it holds a binary (concept, node) and a
 * ternary (role, node, node) tuple table, which represent concept and role
 * assertions respectively. Since this is one of the most crucial parts
 * regarding memory usage, reusing already allocated space is the main design
 * goal. In case of backtracking during the expansion, we just set the pointer
 * to a previous entry in the table that then becomes the current one. When
 * merging or pruning, we leave the entries for the merged/pruned nodes in the
 * table so that we do not have holes in there. The tuple tables are indexed
 * (tries/prefix trees) to speed-up the search for matching atoms during rule
 * applications.
 */
public abstract class ExtensionTable implements Serializable {
    private static final long serialVersionUID=-5029938218056017193L;

    public static enum View { EXTENSION_THIS,EXTENSION_OLD,DELTA_OLD,TOTAL };

    protected final Tableau m_tableau;
    protected final TableauMonitor m_tableauMonitor;
    protected final int m_tupleArity;
    protected final TupleTable m_tupleTable;
    protected final DependencySetManager m_dependencySetManager;
    protected final CoreManager m_coreManager;
    protected int m_afterExtensionOldTupleIndex;
    protected int m_afterExtensionThisTupleIndex;
    protected int m_afterDeltaNewTupleIndex;
    protected int[] m_indicesByBranchingPoint;

    public ExtensionTable(Tableau tableau,int tupleArity,boolean needsDependencySets) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_tupleArity=tupleArity;
        m_tupleTable=new TupleTable(m_tupleArity+(needsDependencySets ? 1 : 0));
        m_dependencySetManager=needsDependencySets ? new LastObjectDependencySetManager(this) : new DeterministicDependencySetManager(this);
        if (m_tupleArity==2)
            m_coreManager=new RealCoreManager();
        else
            m_coreManager=new NoCoreManager();
        m_indicesByBranchingPoint=new int[2*3];
    }
    public abstract int sizeInMemory();
    public int getArity() {
        return m_tupleArity;
    }
    public void retrieveTuple(Object[] tupleBuffer,int tupleIndex) {
        m_tupleTable.retrieveTuple(tupleBuffer,tupleIndex);
    }
    public Object getTupleObject(int tupleIndex,int objectIndex) {
        return m_tupleTable.getTupleObject(tupleIndex,objectIndex);
    }
    public DependencySet getDependencySet(int tupleIndex) {
        return m_dependencySetManager.getDependencySet(tupleIndex);
    }
    public boolean isCore(int tupleIndex) {
        return m_coreManager.isCore(tupleIndex);
    }
    public abstract boolean addTuple(Object[] tuple,DependencySet dependencySet,boolean isCore);
    /**
     * This method is called each time a fresh tuple is added. The method is not called if the tuple
     * was already contained in the extension table. The method updates a couple of relevant data structures
     * and notifies all relevant parties of the tuple's addition.
     */
    protected void postAdd(Object[] tuple,DependencySet dependencySet,int tupleIndex,boolean isCore) {
        Object dlPredicateObject=tuple[0];
        if (dlPredicateObject instanceof Concept) {
            Node node=(Node)tuple[1];
            if (dlPredicateObject instanceof AtomicConcept)
                node.m_numberOfPositiveAtomicConcepts++;
            else if (dlPredicateObject instanceof ExistentialConcept)
                node.addToUnprocessedExistentials((ExistentialConcept)dlPredicateObject);
            else if (dlPredicateObject instanceof AtomicNegationConcept)
                node.m_numberOfNegatedAtomicConcepts++;
            m_tableau.m_existentialExpansionStrategy.assertionAdded((Concept)dlPredicateObject,node,isCore);
        }
        else if (dlPredicateObject instanceof AtomicRole)
            m_tableau.m_existentialExpansionStrategy.assertionAdded((AtomicRole)dlPredicateObject,(Node)tuple[1],(Node)tuple[2],isCore);
        else if (dlPredicateObject instanceof NegatedAtomicRole)
            ((Node)tuple[1]).m_numberOfNegatedRoleAssertions++;
        else if (dlPredicateObject instanceof DescriptionGraph)
            m_tableau.m_descriptionGraphManager.descriptionGraphTupleAdded(tupleIndex,tuple);
        m_tableau.m_clashManager.tupleAdded(this,tuple,dependencySet,isCore);
    }
    public abstract boolean containsTuple(Object[] tuple);
    public Retrieval createRetrieval(boolean[] bindingPattern,View extensionView) {
        int[] bindingPositions=new int[bindingPattern.length];
        for (int index=0;index<bindingPattern.length;index++)
            if (bindingPattern[index])
                bindingPositions[index]=index;
            else
                bindingPositions[index]=-1;
        return createRetrieval(bindingPositions,new Object[bindingPattern.length],new Object[bindingPattern.length],true,extensionView);
    }
    public abstract Retrieval createRetrieval(int[] bindingPositions,Object[] bindingsBuffer,Object[] tupleBuffer,boolean ownsBuffers,View extensionView);
    public abstract DependencySet getDependencySet(Object[] tuple);
    public abstract boolean isCore(Object[] tuple);
    public boolean propagateDeltaNew() {
        boolean deltaNewNotEmpty=(m_afterExtensionThisTupleIndex!=m_afterDeltaNewTupleIndex);
        m_afterExtensionOldTupleIndex=m_afterExtensionThisTupleIndex;
        m_afterExtensionThisTupleIndex=m_afterDeltaNewTupleIndex;
        m_afterDeltaNewTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
        return deltaNewNotEmpty;
    }
    public void branchingPointPushed() {
        int start=m_tableau.getCurrentBranchingPoint().m_level*3;
        int requiredSize=start+3;
        if (requiredSize>m_indicesByBranchingPoint.length) {
            int newSize=m_indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize)
                newSize=newSize*3/2;
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(m_indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,m_indicesByBranchingPoint.length);
            m_indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        m_indicesByBranchingPoint[start]=m_afterExtensionOldTupleIndex;
        m_indicesByBranchingPoint[start+1]=m_afterExtensionThisTupleIndex;
        m_indicesByBranchingPoint[start+2]=m_afterDeltaNewTupleIndex;
    }
    public void backtrack() {
        int start=m_tableau.getCurrentBranchingPoint().m_level*3;
        int newAfterDeltaNewTupleIndex=m_indicesByBranchingPoint[start+2];
        for (int tupleIndex=m_afterDeltaNewTupleIndex-1;tupleIndex>=newAfterDeltaNewTupleIndex;--tupleIndex) {
            removeTuple(tupleIndex);
            m_dependencySetManager.forgetDependencySet(tupleIndex);
            m_tupleTable.nullifyTuple(tupleIndex);
        }
        m_tupleTable.truncate(newAfterDeltaNewTupleIndex);
        m_afterExtensionOldTupleIndex=m_indicesByBranchingPoint[start];
        m_afterExtensionThisTupleIndex=m_indicesByBranchingPoint[start+1];
        m_afterDeltaNewTupleIndex=newAfterDeltaNewTupleIndex;
    }
    protected abstract void removeTuple(int tupleIndex);
    protected void postRemove(Object[] tuple,int tupleIndex) {
        Object dlPredicateObject=tuple[0];
        if (dlPredicateObject instanceof Concept) {
            Node node=(Node)tuple[1];
            m_tableau.m_existentialExpansionStrategy.assertionRemoved((Concept)dlPredicateObject,node,m_coreManager.isCore(tupleIndex));
            if (dlPredicateObject instanceof AtomicConcept)
                node.m_numberOfPositiveAtomicConcepts--;
            else if (dlPredicateObject instanceof ExistentialConcept)
                node.removeFromUnprocessedExistentials((ExistentialConcept)dlPredicateObject);
            else if (dlPredicateObject instanceof AtomicNegationConcept)
                node.m_numberOfNegatedAtomicConcepts--;
        }
        else if (dlPredicateObject instanceof AtomicRole)
            m_tableau.m_existentialExpansionStrategy.assertionRemoved((AtomicRole)dlPredicateObject,(Node)tuple[1],(Node)tuple[2],m_coreManager.isCore(tupleIndex));
        else if (dlPredicateObject instanceof NegatedAtomicRole)
            ((Node)tuple[1]).m_numberOfNegatedRoleAssertions--;
        else if (dlPredicateObject instanceof DescriptionGraph)
            m_tableau.m_descriptionGraphManager.descriptionGraphTupleRemoved(tupleIndex,tuple);
        if (m_tableauMonitor!=null)
            m_tableauMonitor.tupleRemoved(tuple);
    }
    public void clear() {
        m_tupleTable.clear();
        m_afterExtensionOldTupleIndex=0;
        m_afterExtensionThisTupleIndex=0;
        m_afterDeltaNewTupleIndex=0;
    }
    public boolean isTupleActive(Object[] tuple) {
        for (int objectIndex=m_tupleArity-1;objectIndex>0;--objectIndex)
            if (!((Node)tuple[objectIndex]).isActive())
                return false;
        return true;
    }
    public boolean isTupleActive(int tupleIndex) {
        for (int objectIndex=m_tupleArity-1;objectIndex>0;--objectIndex)
            if (!((Node)m_tupleTable.getTupleObject(tupleIndex,objectIndex)).isActive())
                return false;
        return true;
    }

    public static interface Retrieval {
        ExtensionTable getExtensionTable();
        View getExtensionView();
        void clear();
        int[] getBindingPositions();
        Object[] getBindingsBuffer();
        Object[] getTupleBuffer();
        DependencySet getDependencySet();
        boolean isCore();
        void open();
        boolean afterLast();
        int getCurrentTupleIndex();
        void next();
    }

    protected class UnindexedRetrieval implements Retrieval,Serializable {
        private static final long serialVersionUID=6395072458663267969L;

        protected final ExtensionTable.View m_extensionView;
        protected final int[] m_bindingPositions;
        protected final Object[] m_bindingsBuffer;
        protected final Object[] m_tupleBuffer;
        protected final boolean m_ownsBuffers;
        protected final boolean m_checkTupleSelection;
        protected int m_currentTupleIndex;
        protected int m_afterLastTupleIndex;

        public UnindexedRetrieval(int[] bindingPositions,Object[] bindingsBuffer,Object[] tupleBuffer,boolean ownsBuffers,ExtensionTable.View extensionView) {
            m_bindingPositions=bindingPositions;
            m_extensionView=extensionView;
            m_bindingsBuffer=bindingsBuffer;
            m_tupleBuffer=tupleBuffer;
            m_ownsBuffers=ownsBuffers;
            int numberOfBoundPositions=0;
            for (int index=m_bindingPositions.length-1;index>=0;--index)
                if (m_bindingPositions[index]!=-1)
                    numberOfBoundPositions++;
            m_checkTupleSelection=(numberOfBoundPositions>0);
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTable.this;
        }
        public ExtensionTable.View getExtensionView() {
            return m_extensionView;
        }
        public void clear() {
            if (m_ownsBuffers) {
                for (int index=m_bindingsBuffer.length-1;index>=0;--index)
                    m_bindingsBuffer[index]=null;
                for (int index=m_tupleBuffer.length-1;index>=0;--index)
                    m_tupleBuffer[index]=null;
            }
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
            return m_dependencySetManager.getDependencySet(m_currentTupleIndex);
        }
        public boolean isCore() {
            return m_coreManager.isCore(m_currentTupleIndex);
        }
        public void open() {
            switch (m_extensionView) {
            case EXTENSION_THIS:
                m_currentTupleIndex=0;
                m_afterLastTupleIndex=m_afterExtensionThisTupleIndex;
                break;
            case EXTENSION_OLD:
                m_currentTupleIndex=0;
                m_afterLastTupleIndex=m_afterExtensionOldTupleIndex;
                break;
            case DELTA_OLD:
                m_currentTupleIndex=m_afterExtensionOldTupleIndex;
                m_afterLastTupleIndex=m_afterExtensionThisTupleIndex;
                break;
            case TOTAL:
                m_currentTupleIndex=0;
                m_afterLastTupleIndex=m_afterDeltaNewTupleIndex;
                break;
            }
            while (m_currentTupleIndex<m_afterLastTupleIndex) {
                m_tupleTable.retrieveTuple(m_tupleBuffer,m_currentTupleIndex);
                if (isTupleActive())
                    return;
                m_currentTupleIndex++;
            }
        }
        public boolean afterLast() {
            return m_currentTupleIndex>=m_afterLastTupleIndex;
        }
        public int getCurrentTupleIndex() {
            return m_currentTupleIndex;
        }
        public void next() {
            if (m_currentTupleIndex<m_afterLastTupleIndex) {
                m_currentTupleIndex++;
                while (m_currentTupleIndex<m_afterLastTupleIndex) {
                    m_tupleTable.retrieveTuple(m_tupleBuffer,m_currentTupleIndex);
                    if (isTupleActive())
                        return;
                    m_currentTupleIndex++;
                }
            }
        }
        protected boolean isTupleActive() {
            if (!ExtensionTable.this.isTupleActive(m_tupleBuffer))
                return false;
            if (m_checkTupleSelection)
                for (int index=m_bindingPositions.length-1;index>=0;--index)
                    if (m_bindingPositions[index]!=-1 && !m_tupleBuffer[index].equals(m_bindingsBuffer[m_bindingPositions[index]]))
                        return false;
            return true;
        }
    }

    protected static interface DependencySetManager {
        DependencySet getDependencySet(int tupleIndex);
        void setDependencySet(int tupleIndex,DependencySet dependencySet);
        void forgetDependencySet(int tupleIndex);
    }

    protected static class DeterministicDependencySetManager implements DependencySetManager,Serializable {
        private static final long serialVersionUID=7982627098607954806L;

        protected final DependencySetFactory m_dependencySetFactory;

        public DeterministicDependencySetManager(ExtensionTable extensionTable) {
            m_dependencySetFactory=extensionTable.m_tableau.getDependencySetFactory();
        }
        public DependencySet getDependencySet(int tupleIndex) {
            return m_dependencySetFactory.emptySet();
        }
        public void setDependencySet(int tupleIndex,DependencySet dependencySet) {
        }
        public void forgetDependencySet(int tupleIndex) {
        }
    }

    protected class LastObjectDependencySetManager implements DependencySetManager,Serializable {
        private static final long serialVersionUID=-8097612469749016470L;

        protected final DependencySetFactory m_dependencySetFactory;

        public LastObjectDependencySetManager(ExtensionTable extensionTable) {
            m_dependencySetFactory=extensionTable.m_tableau.getDependencySetFactory();
        }
        public DependencySet getDependencySet(int tupleIndex) {
            return (DependencySet)m_tupleTable.getTupleObject(tupleIndex,m_tupleArity);
        }
        public void setDependencySet(int tupleIndex,DependencySet dependencySet) {
            PermanentDependencySet permanentDependencySet=m_dependencySetFactory.getPermanent(dependencySet);
            m_tupleTable.setTupleObject(tupleIndex,m_tupleArity,permanentDependencySet);
            m_dependencySetFactory.addUsage(permanentDependencySet);
        }
        public void forgetDependencySet(int tupleIndex) {
            PermanentDependencySet permanentDependencySet=(PermanentDependencySet)m_tupleTable.getTupleObject(tupleIndex,m_tupleArity);
            m_dependencySetFactory.removeUsage(permanentDependencySet);
        }
    }

    protected static interface CoreManager {
        boolean isCore(int tupleIndex);
        void addCore(int tupleIndex);
        void setCore(int tupleIndex,boolean isCore);
    }

    protected static class NoCoreManager implements CoreManager,Serializable {
        private static final long serialVersionUID=3252994135060928432L;

        public boolean isCore(int tupleIndex) {
            return true;
        }
        public void addCore(int tupleIndex) {
        }
        public void setCore(int tupleIndex,boolean isCore) {
        }
    }

    protected static class RealCoreManager implements CoreManager,Serializable {
        private static final long serialVersionUID=3276377301185845284L;

        protected int[] m_bits;

        public RealCoreManager() {
            m_bits=new int[TupleTable.PAGE_SIZE/32];
        }
        public boolean isCore(int tupleIndex) {
            int frameIndex=tupleIndex/32;
            int mask=1 << (tupleIndex % 32);
            return (m_bits[frameIndex] & mask)!=0;
        }
        public void addCore(int tupleIndex) {
            int frameIndex=tupleIndex/32;
            int mask=1 << (tupleIndex % 32);
            m_bits[frameIndex]|=mask;
        }
        public void setCore(int tupleIndex,boolean isCore) {
            int frameIndex=tupleIndex/32;
            int mask=1 << (tupleIndex % 32);
            if (frameIndex>=m_bits.length) {
                int newSize=3*m_bits.length/2;
                while (frameIndex>=newSize)
                    newSize=3*newSize/2;
                int[] newBits=new int[newSize];
                System.arraycopy(m_bits,0,newBits,0,m_bits.length);
                m_bits=newBits;
            }
            if (isCore)
                m_bits[frameIndex]|=mask;
            else
                m_bits[frameIndex]&=~mask;
        }
    }
}
