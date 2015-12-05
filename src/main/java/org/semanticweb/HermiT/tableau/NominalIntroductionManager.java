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

import org.semanticweb.HermiT.model.AnnotatedEquality;

/**
 * Implements the nominal introduction rule.
 */
public final class NominalIntroductionManager implements Serializable {
    private static final long serialVersionUID=5863617010809297861L;

    protected final Tableau m_tableau;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final InterruptFlag m_interruptFlag;
    protected final MergingManager m_mergingManager;
    protected final TupleTable m_annotatedEqualities;
    protected final Object[] m_bufferForAnnotatedEquality;
    protected final TupleTable m_newRootNodesTable;
    protected final TupleTableFullIndex m_newRootNodesIndex;
    protected final Object[] m_bufferForRootNodes;
    protected int[] m_indicesByBranchingPoint;
    protected int m_firstUnprocessedAnnotatedEquality;

    public NominalIntroductionManager(Tableau tableau) {
        m_tableau=tableau;
        m_dependencySetFactory=m_tableau.m_dependencySetFactory;
        m_interruptFlag=m_tableau.m_interruptFlag;
        m_mergingManager=m_tableau.m_mergingManager;
        m_annotatedEqualities=new TupleTable(5);
        m_bufferForAnnotatedEquality=new Object[5];
        m_newRootNodesTable=new TupleTable(4);
        m_newRootNodesIndex=new TupleTableFullIndex(m_newRootNodesTable,3);
        m_bufferForRootNodes=new Object[4];
        m_indicesByBranchingPoint=new int[10*2];
        m_firstUnprocessedAnnotatedEquality=0;
    }
    public void clear() {
        m_annotatedEqualities.clear();
        for (int index=m_bufferForAnnotatedEquality.length-1;index>=0;--index)
            m_bufferForAnnotatedEquality[index]=null;
        m_newRootNodesTable.clear();
        m_newRootNodesIndex.clear();
        for (int index=m_bufferForRootNodes.length-1;index>=0;--index)
            m_bufferForRootNodes[index]=null;
        m_firstUnprocessedAnnotatedEquality=0;
    }
    public void branchingPointPushed() {
        int start=m_tableau.getCurrentBranchingPoint().getLevel()*3;
        int requiredSize=start+3;
        if (requiredSize>m_indicesByBranchingPoint.length) {
            int newSize=m_indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize)
                newSize=newSize*3/2;
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(m_indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,m_indicesByBranchingPoint.length);
            m_indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        m_indicesByBranchingPoint[start]=m_firstUnprocessedAnnotatedEquality;
        m_indicesByBranchingPoint[start+1]=m_annotatedEqualities.getFirstFreeTupleIndex();
        m_indicesByBranchingPoint[start+2]=m_newRootNodesTable.getFirstFreeTupleIndex();
    }
    public void backtrack() {
        int start=m_tableau.getCurrentBranchingPoint().getLevel()*3;
        m_firstUnprocessedAnnotatedEquality=m_indicesByBranchingPoint[start];
        int firstFreeAnnotatedEqualityShouldBe=m_indicesByBranchingPoint[start+1];
        for (int tupleIndex=m_annotatedEqualities.getFirstFreeTupleIndex()-1;tupleIndex>=firstFreeAnnotatedEqualityShouldBe;--tupleIndex)
            m_dependencySetFactory.removeUsage((PermanentDependencySet)m_annotatedEqualities.getTupleObject(tupleIndex,4));
        m_annotatedEqualities.truncate(firstFreeAnnotatedEqualityShouldBe);
        int firstFreeNewRootNodeShouldBe=m_indicesByBranchingPoint[start+2];
        for (int tupleIndex=m_newRootNodesTable.getFirstFreeTupleIndex()-1;tupleIndex>=firstFreeNewRootNodeShouldBe;--tupleIndex)
            m_newRootNodesIndex.removeTuple(tupleIndex);
        m_newRootNodesTable.truncate(firstFreeNewRootNodeShouldBe);
    }
    public boolean processAnnotatedEqualities() {
        boolean result=false;
        while (m_firstUnprocessedAnnotatedEquality<m_annotatedEqualities.getFirstFreeTupleIndex()) {
            m_annotatedEqualities.retrieveTuple(m_bufferForAnnotatedEquality,m_firstUnprocessedAnnotatedEquality);
            m_firstUnprocessedAnnotatedEquality++;
            AnnotatedEquality annotatedEquality=(AnnotatedEquality)m_bufferForAnnotatedEquality[0];
            Node node0=(Node)m_bufferForAnnotatedEquality[1];
            Node node1=(Node)m_bufferForAnnotatedEquality[2];
            Node node2=(Node)m_bufferForAnnotatedEquality[3];
            DependencySet dependencySet=(DependencySet)m_bufferForAnnotatedEquality[4];
            if (applyNIRule(annotatedEquality,node0,node1,node2,dependencySet))
                result=true;
            m_interruptFlag.checkInterrupt();
        }
        return result;
    }
    public boolean canForgetAnnotation(AnnotatedEquality annotatedEquality,Node node0,Node node1,Node node2) {
        return node0.isRootNode() || node1.isRootNode() || !node2.isRootNode() || (node2.isParentOf(node0) && node2.isParentOf(node1));
    }
    public boolean addAnnotatedEquality(AnnotatedEquality annotatedEquality,Node node0,Node node1,Node node2,DependencySet dependencySet) {
        if (!node0.isActive() || !node1.isActive() || !node2.isActive())
            return false;
        else if (canForgetAnnotation(annotatedEquality,node0,node1,node2))
            return m_mergingManager.mergeNodes(node0,node1,dependencySet);
        else if (annotatedEquality.getCaridnality()==1)
            return applyNIRule(annotatedEquality,node0,node1,node2,dependencySet);
        else {
            PermanentDependencySet permanentDependencySet=m_dependencySetFactory.getPermanent(dependencySet);
            m_bufferForAnnotatedEquality[0]=annotatedEquality;
            m_bufferForAnnotatedEquality[1]=node0;
            m_bufferForAnnotatedEquality[2]=node1;
            m_bufferForAnnotatedEquality[3]=node2;
            m_bufferForAnnotatedEquality[4]=permanentDependencySet;
            m_dependencySetFactory.addUsage(permanentDependencySet);
            m_annotatedEqualities.addTuple(m_bufferForAnnotatedEquality);
            return true;
        }
    }
    protected boolean applyNIRule(AnnotatedEquality annotatedEquality,Node node0,Node node1,Node node2,DependencySet dependencySet) {
        if (node0.isPruned() || node1.isPruned() || node2.isPruned())
            return false;
        dependencySet=node0.addCanonicalNodeDependencySet(dependencySet);
        dependencySet=node1.addCanonicalNodeDependencySet(dependencySet);
        dependencySet=node2.addCanonicalNodeDependencySet(dependencySet);
        node0=node0.getCanonicalNode();
        node1=node1.getCanonicalNode();
        node2=node2.getCanonicalNode();
        if (canForgetAnnotation(annotatedEquality,node0,node1,node2))
            return m_mergingManager.mergeNodes(node0,node1,dependencySet);
        else {
            Node niTargetNode;
            Node otherNode;
            if (!node0.isRootNode() && !node2.isParentOf(node0)) {
                niTargetNode=node0;
                otherNode=node1;
            }
            else {
                niTargetNode=node1;
                otherNode=node0;
            }
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.nominalIntorductionStarted(node2,niTargetNode,annotatedEquality,node0,node1);
            if (annotatedEquality.getCaridnality()>1) {
                BranchingPoint branchingPoint=new NominalIntroductionBranchingPoint(m_tableau,node2,niTargetNode,otherNode,annotatedEquality);
                m_tableau.pushBranchingPoint(branchingPoint);
                dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
            }
            Node newRootNode=getNIRootFor(dependencySet,node2,annotatedEquality,1);
            if (!newRootNode.isActive()) {
                assert newRootNode.isMerged();
                dependencySet=newRootNode.addCanonicalNodeDependencySet(dependencySet);
                newRootNode=newRootNode.getCanonicalNode();
            }
            m_mergingManager.mergeNodes(niTargetNode,newRootNode,dependencySet);
            if (!otherNode.isPruned()) {
                dependencySet=otherNode.addCanonicalNodeDependencySet(dependencySet);
                m_mergingManager.mergeNodes(otherNode.getCanonicalNode(),newRootNode,dependencySet);
            }
            if (m_tableau.m_tableauMonitor!=null)
                m_tableau.m_tableauMonitor.nominalIntorductionFinished(node2,niTargetNode,annotatedEquality,node0,node1);
            return true;
        }
    }
    protected Node getNIRootFor(DependencySet dependencySet,Node rootNode,AnnotatedEquality annotatedEquality,int number) {
        m_bufferForRootNodes[0]=rootNode;
        m_bufferForRootNodes[1]=annotatedEquality;
        m_bufferForRootNodes[2]=number;
        int tupleIndex=m_newRootNodesIndex.getTupleIndex(m_bufferForRootNodes);
        if (tupleIndex==-1) {
            Node newRootNode=m_tableau.createNewNINode(dependencySet);
            m_bufferForRootNodes[3]=newRootNode;
            m_newRootNodesIndex.addTuple(m_bufferForRootNodes,m_newRootNodesTable.getFirstFreeTupleIndex());
            m_newRootNodesTable.addTuple(m_bufferForRootNodes);
            return newRootNode;
        }
        else
            return (Node)m_newRootNodesTable.getTupleObject(tupleIndex,3);
    }

    protected class NominalIntroductionBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=6678113479704184263L;

        protected final Node m_rootNode;
        protected final Node m_niTargetNode;
        protected final Node m_otherNode;
        protected final AnnotatedEquality m_annotatedEquality;
        protected int m_currentRootNode;

        public NominalIntroductionBranchingPoint(Tableau tableau,Node rootNode,Node niTargetNode,Node otherNode,AnnotatedEquality annotatedEquality) {
            super(tableau);
            m_rootNode=rootNode;
            m_niTargetNode=niTargetNode;
            m_otherNode=otherNode;
            m_annotatedEquality=annotatedEquality;
            m_currentRootNode=1; // This reflects the assumption that the first merge is performed from the NominalIntroductionManager
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
            m_currentRootNode++;
            assert m_currentRootNode<=m_annotatedEquality.getCaridnality();
            DependencySet dependencySet=clashDepdendencySet;
            if (m_currentRootNode==m_annotatedEquality.getCaridnality())
                dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(dependencySet,m_level);
            Node newRootNode=getNIRootFor(dependencySet,m_rootNode,m_annotatedEquality,m_currentRootNode);
            if (!newRootNode.isActive()) {
                assert newRootNode.isMerged();
                dependencySet=newRootNode.addCanonicalNodeDependencySet(dependencySet);
                newRootNode=newRootNode.getCanonicalNode();
            }
            m_mergingManager.mergeNodes(m_niTargetNode,newRootNode,dependencySet);
            if (!m_otherNode.isPruned()) {
                dependencySet=m_otherNode.addCanonicalNodeDependencySet(dependencySet);
                m_mergingManager.mergeNodes(m_otherNode.getCanonicalNode(),newRootNode,dependencySet);
            }
        }
    }
}
