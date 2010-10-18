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
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.tableau.TupleTable;

public class IndividualReuseStrategy extends AbstractExpansionStrategy implements Serializable {
    private static final long serialVersionUID=-7373787507623860081L;

    protected final boolean m_isDeterministic;
    protected final Map<AtomicConcept,NodeBranchingPointPair> m_reusedNodes;
    protected final Set<AtomicConcept> m_doReuseConceptsAlways;
    protected final Set<AtomicConcept> m_dontReuseConceptsThisRun;
    protected final Set<AtomicConcept> m_dontReuseConceptsEver;
    protected final TupleTable m_reuseBacktrackingTable;
    protected final Object[] m_auxiliaryBuffer;
    protected int[] m_indicesByBranchingPoint;

    public IndividualReuseStrategy(BlockingStrategy strategy,boolean isDeterministic) {
        super(strategy,true);
        m_isDeterministic=isDeterministic;
        m_reusedNodes=new HashMap<AtomicConcept,NodeBranchingPointPair>();
        m_doReuseConceptsAlways=new HashSet<AtomicConcept>();
        m_dontReuseConceptsThisRun=new HashSet<AtomicConcept>();
        m_dontReuseConceptsEver=new HashSet<AtomicConcept>();
        m_reuseBacktrackingTable=new TupleTable(1);
        m_auxiliaryBuffer=new Object[1];
        m_indicesByBranchingPoint=new int[10];
    }
    @SuppressWarnings("unchecked")
    public void initialize(Tableau tableau) {
        super.initialize(tableau);
        m_doReuseConceptsAlways.clear();
        m_dontReuseConceptsEver.clear();
        Object object=tableau.getParameters().get("IndividualReuseStrategy.reuseAlways");
        if (object instanceof Set)
            m_doReuseConceptsAlways.addAll((Set<? extends AtomicConcept>)object);
        object=tableau.getParameters().get("IndividualReuseStrategy.reuseNever");
        if (object instanceof Set)
            m_dontReuseConceptsEver.addAll((Set<? extends AtomicConcept>)object);
    }
    public void clear() {
        super.clear();
        m_reusedNodes.clear();
        m_reuseBacktrackingTable.clear();
        m_dontReuseConceptsThisRun.clear();
        m_dontReuseConceptsThisRun.addAll(m_dontReuseConceptsEver);
    }
    public void branchingPointPushed() {
        int start=m_tableau.getCurrentBranchingPoint().getLevel();
        int requiredSize=start+1;
        if (requiredSize>m_indicesByBranchingPoint.length) {
            int newSize=m_indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize)
                newSize=newSize*3/2;
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(m_indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,m_indicesByBranchingPoint.length);
            m_indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        m_indicesByBranchingPoint[start]=m_reuseBacktrackingTable.getFirstFreeTupleIndex();
    }
    public void backtrack() {
        int requiredFirstFreeTupleIndex=m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().getLevel()];
        for (int index=m_reuseBacktrackingTable.getFirstFreeTupleIndex()-1;index>=requiredFirstFreeTupleIndex;--index) {
            AtomicConcept reuseConcept=(AtomicConcept)m_reuseBacktrackingTable.getTupleObject(index,0);
            Object result=m_reusedNodes.remove(reuseConcept);
            assert result!=null;
        }
        m_reuseBacktrackingTable.truncate(requiredFirstFreeTupleIndex);
    }
    public void modelFound() {
        m_dontReuseConceptsEver.addAll(m_dontReuseConceptsThisRun);
    }
    public boolean isDeterministic() {
        return m_isDeterministic;
    }
    public AtomicConcept getConceptForNode(Node node) {
        for (Map.Entry<AtomicConcept,NodeBranchingPointPair> entry : m_reusedNodes.entrySet())
            if (entry.getValue().m_node==node)
                return entry.getKey();
        return null;
    }
    public Set<AtomicConcept> getDontReuseConceptsEver() {
        return m_dontReuseConceptsEver;
    }
    protected void expandExistential(AtLeast atLeast,Node forNode) {
        // Mark existential as processed BEFORE branching takes place!
        m_existentialExpansionManager.markExistentialProcessed(atLeast,forNode);
        if (!m_existentialExpansionManager.tryFunctionalExpansion(atLeast,forNode)) 
            if (atLeast instanceof AtLeastDataRange)
                m_existentialExpansionManager.doNormalExpansion((AtLeastDataRange)atLeast,forNode);
            else {
                AtLeastConcept atLeastConcept=(AtLeastConcept)atLeast;
                if (!tryParentReuse(atLeastConcept,forNode))
                    if (!expandWithModelReuse(atLeastConcept,forNode))
                        m_existentialExpansionManager.doNormalExpansion(atLeastConcept,forNode);
            }
    }
    protected boolean tryParentReuse(AtLeastConcept atLeastConcept,Node node) {
        if (atLeastConcept.getNumber()==1) {
            Node parent=node.getParent();
            if (parent!=null && m_extensionManager.containsConceptAssertion(atLeastConcept.getToConcept(),parent)) {
                DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastConcept,node);
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(m_tableau,atLeastConcept,node,true);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                m_extensionManager.addRoleAssertion(atLeastConcept.getOnRole(),node,parent,dependencySet,true);
                return true;
            }
        }
        return false;
    }
    protected boolean expandWithModelReuse(AtLeastConcept atLeastConcept,Node node) {
        if (!(atLeastConcept.getToConcept() instanceof AtomicConcept))
            return false;
        AtomicConcept toConcept=(AtomicConcept)atLeastConcept.getToConcept();
        if (Prefixes.isInternalIRI(toConcept.getIRI()))
            return false;
        if (atLeastConcept.getNumber()==1 && (m_doReuseConceptsAlways.contains(toConcept) || !m_dontReuseConceptsThisRun.contains(toConcept))) {
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastConcept,node);
            DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastConcept,node);
            Node existentialNode;
            NodeBranchingPointPair reuseInfo=m_reusedNodes.get(toConcept);
            if (reuseInfo==null) {
                // No existential with the target concept toConcept has been expanded.
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(m_tableau,atLeastConcept,node,false);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                // create a root node so that keys are not applicable
                existentialNode=m_tableau.createNewNINode(dependencySet);
                reuseInfo=new NodeBranchingPointPair(existentialNode,m_tableau.getCurrentBranchingPointLevel());
                m_reusedNodes.put(toConcept,reuseInfo);
                m_extensionManager.addConceptAssertion(toConcept,existentialNode,dependencySet,true);
                m_auxiliaryBuffer[0]=toConcept;
                m_reuseBacktrackingTable.addTuple(m_auxiliaryBuffer);
            }
            else {
                dependencySet=reuseInfo.m_node.addCanonicalNodeDependencySet(dependencySet);
                existentialNode=reuseInfo.m_node.getCanonicalNode();
                if (!m_isDeterministic)
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,reuseInfo.m_branchingPoint);
            }
            m_extensionManager.addRoleAssertion(atLeastConcept.getOnRole(),node,existentialNode,dependencySet,true);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastConcept,node);
            return true;
        }
        return false;
    }

    protected class IndividualReuseBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastConcept m_existential;
        protected final Node m_node;
        protected final boolean m_wasParentReuse;

        public IndividualReuseBranchingPoint(Tableau tableau,AtLeastConcept existential,Node node,boolean wasParentReuse) {
            super(tableau);
            m_existential=existential;
            m_node=node;
            m_wasParentReuse=wasParentReuse;
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDependencySet) {
            if (!m_wasParentReuse)
                m_dontReuseConceptsThisRun.add((AtomicConcept)m_existential.getToConcept());
            DependencySet dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(clashDependencySet,m_level);
            if (tableau.getTableauMonitor()!=null)
                tableau.getTableauMonitor().existentialExpansionStarted(m_existential,m_node);
            Node existentialNode=tableau.createNewTreeNode(dependencySet,m_node);
            m_extensionManager.addConceptAssertion(m_existential.getToConcept(),existentialNode,dependencySet,true);
            m_extensionManager.addRoleAssertion(m_existential.getOnRole(),m_node,existentialNode,dependencySet,true);
            if (tableau.getTableauMonitor()!=null)
                tableau.getTableauMonitor().existentialExpansionFinished(m_existential,m_node);
        }
    }

    protected static class NodeBranchingPointPair implements Serializable {
        private static final long serialVersionUID=427963701900451471L;

        protected final Node m_node;
        protected final int m_branchingPoint;

        public NodeBranchingPointPair(Node node,int branchingPoint) {
            m_node=node;
            m_branchingPoint=branchingPoint;
        }
    }
}
