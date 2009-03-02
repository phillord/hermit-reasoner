// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.AtLeastAbstractRoleConcept;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DependencySet;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.tableau.TupleTable;

public class IndividualReuseStrategy extends StrategyBase implements Serializable {
    private static final long serialVersionUID=-7373787507623860081L;

    protected final boolean m_isDeterministic;
    protected final Map<LiteralConcept,NodeBranchingPointPair> m_reusedNodes;
    protected final Set<LiteralConcept> m_doReuseConceptsAlways;
    protected final Set<LiteralConcept> m_dontReuseConceptsThisRun;
    protected final Set<LiteralConcept> m_dontReuseConceptsEver;
    protected final TupleTable m_reuseBacktrackingTable;
    protected final Object[] m_auxiliaryBuffer;
    protected int[] m_indicesByBranchingPoint;

    public IndividualReuseStrategy(BlockingStrategy strategy,boolean isDeterministic) {
        super(strategy,true);
        m_isDeterministic=isDeterministic;
        m_reusedNodes=new HashMap<LiteralConcept,NodeBranchingPointPair>();
        m_doReuseConceptsAlways=new HashSet<LiteralConcept>();
        m_dontReuseConceptsThisRun=new HashSet<LiteralConcept>();
        m_dontReuseConceptsEver=new HashSet<LiteralConcept>();
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
            m_doReuseConceptsAlways.addAll((Set<? extends LiteralConcept>)object);
        object=tableau.getParameters().get("IndividualReuseStrategy.reuseNever");
        if (object instanceof Set)
            m_dontReuseConceptsEver.addAll((Set<? extends LiteralConcept>)object);
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
            LiteralConcept reuseConcept=(LiteralConcept)m_reuseBacktrackingTable.getTupleObject(index,0);
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
    public LiteralConcept getConceptForNode(Node node) {
        for (Map.Entry<LiteralConcept,NodeBranchingPointPair> entry : m_reusedNodes.entrySet())
            if (entry.getValue().node==node)
                return entry.getKey();
        return null;
    }
    public Set<LiteralConcept> getDontReuseConceptsEver() {
        return m_dontReuseConceptsEver;
    }
    protected void expandExistential(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node forNode) {
        // Mark existential as processed BEFORE branching takes place!
        m_existentialExpansionManager.markExistentialProcessed(atLeastAbstractRoleConcept,forNode);
        if (!m_existentialExpansionManager.tryFunctionalExpansion(atLeastAbstractRoleConcept,forNode))
            if (!tryParentReuse(atLeastAbstractRoleConcept,forNode))
                if (!expandWithModelReuse(atLeastAbstractRoleConcept,forNode))
                    m_existentialExpansionManager.doNormalExpansion(atLeastAbstractRoleConcept,forNode);
    }
    protected boolean tryParentReuse(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node node) {
        if (atLeastAbstractConcept.getNumber()==1) {
            Node parent=node.getParent();
            if (parent!=null && m_extensionManager.containsConceptAssertion(atLeastAbstractConcept.getToConcept(),parent)) {
                DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,node);
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(m_tableau,atLeastAbstractConcept,node,true);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                m_extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),node,parent,dependencySet);
                return true;
            }
        }
        return false;
    }
    protected boolean expandWithModelReuse(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node node) {
        LiteralConcept toConcept=atLeastAbstractConcept.getToConcept();
        if ((toConcept instanceof AtomicConcept) && Namespaces.isInternalURI(((AtomicConcept)toConcept).getURI()))
            return false;
        if (atLeastAbstractConcept.getNumber()==1 && (m_doReuseConceptsAlways.contains(toConcept) || !m_dontReuseConceptsThisRun.contains(toConcept))) {
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractConcept,node);
            DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,node);
            Node existentialNode;
            NodeBranchingPointPair reuseInfo=m_reusedNodes.get(toConcept);
            if (reuseInfo==null) {
                // No existential with the target concept toConcept has been expanded.
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(m_tableau,atLeastAbstractConcept,node,false);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                // create a root node so that keys are not applicable
                existentialNode=m_tableau.createNewRootNode(dependencySet,0);
                reuseInfo=new NodeBranchingPointPair(existentialNode,m_tableau.getCurrentBranchingPoint().getLevel());
                m_reusedNodes.put(toConcept,reuseInfo);
                m_extensionManager.addConceptAssertion(toConcept,existentialNode,dependencySet);
                m_auxiliaryBuffer[0]=toConcept;
                m_reuseBacktrackingTable.addTuple(m_auxiliaryBuffer);
            }
            else {
                dependencySet=reuseInfo.node.addCacnonicalNodeDependencySet(dependencySet);
                existentialNode=reuseInfo.node.getCanonicalNode();
                dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,reuseInfo.branchingPoint);
            }
            m_extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),node,existentialNode,dependencySet);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractConcept,node);
            return true;
        }
        return false;
    }

    protected class IndividualReuseBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastAbstractRoleConcept m_existential;
        protected final Node m_node;
        protected final boolean m_wasParentReuse;

        public IndividualReuseBranchingPoint(Tableau tableau,AtLeastAbstractRoleConcept existential,Node node,boolean wasParentReuse) {
            super(tableau);
            m_existential=existential;
            m_node=node;
            m_wasParentReuse=wasParentReuse;
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDependencySet) {
            if (!m_wasParentReuse)
                m_dontReuseConceptsThisRun.add(m_existential.getToConcept());
            DependencySet dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(clashDependencySet,m_level);
            if (tableau.getTableauMonitor()!=null)
                tableau.getTableauMonitor().existentialExpansionStarted(m_existential,m_node);
            Node existentialNode=tableau.createNewTreeNode(dependencySet,m_node);
            m_extensionManager.addConceptAssertion(m_existential.getToConcept(),existentialNode,dependencySet);
            m_extensionManager.addRoleAssertion(m_existential.getOnRole(),m_node,existentialNode,dependencySet);
            if (tableau.getTableauMonitor()!=null)
                tableau.getTableauMonitor().existentialExpansionFinished(m_existential,m_node);
        }
    }

    protected static class NodeBranchingPointPair implements Serializable {
        private static final long serialVersionUID=427963701900451471L;

        protected final Node node;
        protected final int branchingPoint;

        public NodeBranchingPointPair(Node node,int branchingPoint) {
            this.node=node;
            this.branchingPoint=branchingPoint;
        }
    }
}
