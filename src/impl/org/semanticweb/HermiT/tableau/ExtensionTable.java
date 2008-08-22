// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public abstract class ExtensionTable implements Serializable {
    private static final long serialVersionUID=-5029938218056017193L;

    public static enum View { EXTENSION_THIS,EXTENSION_OLD,DELTA_OLD,TOTAL };

    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final TableauMonitor m_tableauMonitor;
    protected final int m_tupleArity;
    protected final TupleTable m_tupleTable;
    protected final DependencySetManager m_dependencySetManager;
    protected final Object[] m_binaryAuxiliaryTuple;
    protected final UnionDependencySet m_binaryUnionDependencySet;
    protected int m_afterExtensionOldTupleIndex;
    protected int m_afterExtensionThisTupleIndex;
    protected int m_afterDeltaNewTupleIndex;
    protected int[] m_indicesByBranchingPoint;
    
    public ExtensionTable(Tableau tableau,ExtensionManager extensionManager,int tupleArity,boolean needsDependencySets) {
        m_tableau=tableau;
        m_tableauMonitor=m_tableau.m_tableauMonitor;
        m_extensionManager=extensionManager;
        m_tupleArity=tupleArity;
        m_tupleTable=new TupleTable(m_tupleArity+(needsDependencySets ? 1 : 0));
        m_dependencySetManager=needsDependencySets ? new LastObjectDependencySetManager(this) : new DeterministicDependencySetManager(this);
        m_binaryAuxiliaryTuple=new Object[2];
        m_binaryUnionDependencySet=new UnionDependencySet(2);
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
    public abstract boolean addTuple(Object[] tuple,DependencySet dependencySet);
    protected void postAdd(Object[] tuple,DependencySet dependencySet,int tupleIndex) {
        Object dlPredicateObject=tuple[0];
        if (dlPredicateObject instanceof Concept) {
            Node node=(Node)tuple[1];
            m_tableau.m_existentialsExpansionStrategy.assertionAdded((Concept)dlPredicateObject,node);
            if (dlPredicateObject instanceof AtomicNegationConcept) {
                node.addToNegativeLabel();
                if (node.getPositiveLabelSize()>0) {
                    m_binaryAuxiliaryTuple[0]=((AtomicNegationConcept)dlPredicateObject).getNegatedAtomicConcept();
                    m_binaryAuxiliaryTuple[1]=node;
                    if (containsTuple(m_binaryAuxiliaryTuple)) {
                        m_binaryUnionDependencySet.m_dependencySets[0]=dependencySet;
                        m_binaryUnionDependencySet.m_dependencySets[1]=getDependencySet(m_binaryAuxiliaryTuple);
                        m_extensionManager.setClash(m_binaryUnionDependencySet);
                        if (m_tableauMonitor!=null)
                            m_tableauMonitor.clashDetected(tuple,m_binaryAuxiliaryTuple);
                    }
                }
            }
            else if (AtomicConcept.NOTHING.equals(dlPredicateObject)) {
                m_extensionManager.setClash(dependencySet);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.clashDetected(tuple);
            }
            else if (dlPredicateObject instanceof Concept) {
                Concept concept=(Concept)dlPredicateObject;
                node.addToPositiveLabel(concept);
                if (concept instanceof AtomicConcept && node.getNegativeLabelSize()>0) {
                    m_binaryAuxiliaryTuple[0]=AtomicNegationConcept.create((AtomicConcept)concept);
                    m_binaryAuxiliaryTuple[1]=node;
                    if (containsTuple(m_binaryAuxiliaryTuple)) {
                        m_binaryUnionDependencySet.m_dependencySets[0]=dependencySet;
                        m_binaryUnionDependencySet.m_dependencySets[1]=getDependencySet(m_binaryAuxiliaryTuple);
                        m_extensionManager.setClash(m_binaryUnionDependencySet);
                        if (m_tableauMonitor!=null)
                            m_tableauMonitor.clashDetected(tuple,m_binaryAuxiliaryTuple);
                    }
                }
                if (dlPredicateObject instanceof ExistentialConcept)
                    node.addToUnprocessedExistentials((ExistentialConcept)dlPredicateObject);
                m_tableau.m_nominalIntroductionManager.addNonnegativeConceptAssertion(concept,node);
            }
        }
        else if (dlPredicateObject instanceof AtomicAbstractRole) {
            AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)dlPredicateObject;
            Node node0=(Node)tuple[1];
            Node node1=(Node)tuple[2];
            m_tableau.m_existentialsExpansionStrategy.assertionAdded(atomicAbstractRole,node0,node1);
            if (node0.isParentOf(node1))
                node1.addToFromParentLabel(atomicAbstractRole);
            else if (node1.isParentOf(node0))
                node0.addToToParentLabel(atomicAbstractRole);
            m_tableau.m_nominalIntroductionManager.addAtomicAbstractRoleAssertion(atomicAbstractRole,node0,node1);
        }
        else if (Inequality.INSTANCE.equals(dlPredicateObject)) {
            if (tuple[1]==tuple[2]) {
                m_extensionManager.setClash(dependencySet);
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.clashDetected(tuple);
            }
        }
        else if (dlPredicateObject instanceof DescriptionGraph)
            m_tableau.m_descriptionGraphManager.descriptionGraphTupleAdded(tupleIndex,tuple);
    }
    public abstract boolean containsTuple(Object[] tuple);
    public Retrieval createRetrieval(boolean[] bindingPattern,View extensionView) {
        int[] bindingPositions=new int[bindingPattern.length];
        for (int index=0;index<bindingPattern.length;index++)
            if (bindingPattern[index])
                bindingPositions[index]=index;
            else
                bindingPositions[index]=-1;
        return createRetrieval(bindingPositions,new Object[bindingPattern.length],extensionView);
    }
    public abstract Retrieval createRetrieval(int[] bindingPositions,Object[] bindingsBuffer,View extensionView);
    public abstract DependencySet getDependencySet(Object[] tuple);
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
            m_tableau.m_existentialsExpansionStrategy.assertionRemoved((Concept)dlPredicateObject,node);
            if (dlPredicateObject instanceof AtomicNegationConcept)
                node.removeFromNegativeLabel();
            else if (dlPredicateObject instanceof Concept) {
                Concept concept=(Concept)dlPredicateObject;
                node.removeFromPositiveLabel(concept);
                if (dlPredicateObject instanceof ExistentialConcept)
                    node.removeFromUnprocessedExistentials((ExistentialConcept)dlPredicateObject);
            }
        }
        else if (dlPredicateObject instanceof AtomicAbstractRole) {
            AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)dlPredicateObject;
            Node node0=(Node)tuple[1];
            Node node1=(Node)tuple[2];
            m_tableau.m_existentialsExpansionStrategy.assertionRemoved(atomicAbstractRole,node0,node1);
            if (node0.isParentOf(node1))
                node1.removeFromFromParentLabel(atomicAbstractRole);
            else if (node1.isParentOf(node0))
                node0.removeFromToParentLabel(atomicAbstractRole);
            m_tableau.m_nominalIntroductionManager.removeAtomicAbstractRoleAssertion(atomicAbstractRole,node0,node1);
        }
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
        int[] getBindingPositions();
        Object[] getBindingsBuffer();
        Object[] getTupleBuffer();
        DependencySet getDependencySet();
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
        protected final boolean m_checkTupleSelection;
        protected final Object[] m_tupleBuffer;
        protected int m_currentTupleIndex;
        protected int m_afterLastTupleIndex;

        public UnindexedRetrieval(int[] bindingPositions,Object[] bindingsBuffer,ExtensionTable.View extensionView) {
            m_bindingPositions=bindingPositions;
            m_extensionView=extensionView;
            m_bindingsBuffer=bindingsBuffer;
            int numberOfBoundPositions=0;
            for (int index=m_bindingPositions.length-1;index>=0;--index)
                if (m_bindingPositions[index]!=-1)
                    numberOfBoundPositions++;
            m_checkTupleSelection=(numberOfBoundPositions>0);
            m_tupleBuffer=new Object[m_tupleArity];
        }
        public ExtensionTable getExtensionTable() {
            return ExtensionTable.this;
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
            return m_dependencySetManager.getDependencySet(m_currentTupleIndex);
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

        protected final PermanentDependencySet m_emptySet;

        public DeterministicDependencySetManager(ExtensionTable extensionTable) {
            m_emptySet=extensionTable.m_tableau.getDependencySetFactory().emptySet();
        }
        public DependencySet getDependencySet(int tupleIndex) {
            return m_emptySet;
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
}
