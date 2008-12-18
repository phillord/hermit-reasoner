// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.InternalNames;
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

    protected final StrategyBase.Expander expander;
    protected final Map<LiteralConcept,NodeBranchingPointPair> reusedNodes;
    protected final Set<LiteralConcept> doReuseConceptsAlways;
    protected final Set<LiteralConcept> dontReuseConceptsThisRun;
    protected final Set<LiteralConcept> dontReuseConceptsEver;
    protected final TupleTable reuseBacktrackingTable;
    protected final Object[] auxiliaryBuffer;
    protected int[] indicesByBranchingPoint;
    protected final boolean isDeterministic;

    public IndividualReuseStrategy(BlockingStrategy strategy,boolean isDeterministic) {
        super(strategy);
        this.isDeterministic=isDeterministic;
        reusedNodes=new HashMap<LiteralConcept,NodeBranchingPointPair>();
        doReuseConceptsAlways=new HashSet<LiteralConcept>();
        dontReuseConceptsThisRun=new HashSet<LiteralConcept>();
        dontReuseConceptsEver=new HashSet<LiteralConcept>();
        reuseBacktrackingTable=new TupleTable(1);
        auxiliaryBuffer=new Object[1];
        indicesByBranchingPoint=new int[10];
        expander=new StrategyBase.Expander() {
            protected Node expanded;
            public boolean expand(AtLeastAbstractRoleConcept c,Node n) {
                if (expanded==null)
                    expanded=n;
                else if (expanded!=n)
                    return true;
                // Mark existential as processed BEFORE branching takes place:
                existentialExpansionManager.markExistentialProcessed(c,n);
                if (!existentialExpansionManager.tryFunctionalExpansion(c,n)&&!tryParentReuse(c,n)&&!expandWithModelReuse(c,n)) {
                    existentialExpansionManager.doNormalExpansion(c,n);
                }
                return false;
            }
            public boolean completeExpansion() {
                if (expanded!=null) {
                    expanded=null;
                    return true;
                }
                else {
                    return false;
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public void initialize(Tableau tableau) {
        super.initialize(tableau);
        doReuseConceptsAlways.clear();
        dontReuseConceptsEver.clear();
        Object object=tableau.getParameters().get("IndividualReuseStrategy.reuseAlways");
        if (object instanceof Set) {
            doReuseConceptsAlways.addAll((Set<? extends LiteralConcept>)object);
        }
        object=tableau.getParameters().get("IndividualReuseStrategy.reuseNever");
        if (object instanceof Set) {
            dontReuseConceptsEver.addAll((Set<? extends LiteralConcept>)object);
        }
    }

    public void clear() {
        super.clear();
        reusedNodes.clear();
        reuseBacktrackingTable.clear();
        dontReuseConceptsThisRun.clear();
        dontReuseConceptsThisRun.addAll(dontReuseConceptsEver);
    }

    public boolean expandExistentials() {
        return expandExistentials(expander);
    }

    protected boolean tryParentReuse(AtLeastAbstractRoleConcept atLeastAbstractConcept,Node node) {
        if (atLeastAbstractConcept.getNumber()==1) {
            Node parent=node.getParent();
            if (parent!=null&&extensionManager.containsConceptAssertion(atLeastAbstractConcept.getToConcept(),parent)) {
                DependencySet dependencySet=extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,node);
                if (!isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(tableau,atLeastAbstractConcept,node,true);
                    tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),node,parent,dependencySet);
                return true;
            }
        }
        return false;
    }

    protected boolean expandWithModelReuse
        (AtLeastAbstractRoleConcept atLeastAbstractConcept, Node node) {
        LiteralConcept toConcept = atLeastAbstractConcept.getToConcept();
        if ((toConcept instanceof AtomicConcept) &&
            InternalNames.isInternalUri(((AtomicConcept) toConcept).getURI())) {
            return false;
        }
        if (atLeastAbstractConcept.getNumber()==1 
                && (doReuseConceptsAlways.contains(toConcept) 
                        || !dontReuseConceptsThisRun.contains(toConcept))) {
            // try reuse
            if (tableau.getTableauMonitor()!=null) {
                tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractConcept,node);
            }
            DependencySet dependencySet=extensionManager.getConceptAssertionDependencySet(atLeastAbstractConcept,node);
            Node existentialNode;
            NodeBranchingPointPair reuseInfo=reusedNodes.get(toConcept);
            if (reuseInfo==null) {
                // no reuse possible
                if (!isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualReuseBranchingPoint(tableau,atLeastAbstractConcept,node,false);
                    tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                // no idea, why we create a root node here, check if the 
                // introduction of named nodes in addition to root nodes makes a 
                // difference
                existentialNode=tableau.createNewRootNode(dependencySet,0);
                reuseInfo=new NodeBranchingPointPair(existentialNode,tableau.getCurrentBranchingPoint().getLevel());
                reusedNodes.put(toConcept,reuseInfo);
                extensionManager.addConceptAssertion(toConcept,existentialNode,dependencySet);
                auxiliaryBuffer[0]=toConcept;
                reuseBacktrackingTable.addTuple(auxiliaryBuffer);
            }
            else {
                dependencySet=reuseInfo.node.addCacnonicalNodeDependencySet(dependencySet);
                existentialNode=reuseInfo.node.getCanonicalNode();
                dependencySet=tableau.getDependencySetFactory().addBranchingPoint(dependencySet,reuseInfo.branchingPoint);
            }
            extensionManager.addRoleAssertion(atLeastAbstractConcept.getOnRole(),node,existentialNode,dependencySet);
            if (tableau.getTableauMonitor()!=null) {
                tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractConcept,node);
            }
            return true;
        }
        return false;
    }

    public void branchingPointPushed() {
        int start=tableau.getCurrentBranchingPoint().getLevel();
        int requiredSize=start+1;
        if (requiredSize>indicesByBranchingPoint.length) {
            int newSize=indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize) {
                newSize=newSize*3/2;
            }
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,indicesByBranchingPoint.length);
            indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        indicesByBranchingPoint[start]=reuseBacktrackingTable.getFirstFreeTupleIndex();
    }

    public void backtrack() {
        int requiredFirstFreeTupleIndex=indicesByBranchingPoint[tableau.getCurrentBranchingPoint().getLevel()];
        for (int index=reuseBacktrackingTable.getFirstFreeTupleIndex()-1;index>=requiredFirstFreeTupleIndex;--index) {
            LiteralConcept reuseConcept=(LiteralConcept)reuseBacktrackingTable.getTupleObject(index,0);
            Object result=reusedNodes.remove(reuseConcept);
            assert result!=null;
        }
        reuseBacktrackingTable.truncate(requiredFirstFreeTupleIndex);
    }

    public void modelFound() {
        dontReuseConceptsEver.addAll(dontReuseConceptsThisRun);
    }

    public boolean isDeterministic() {
        return isDeterministic;
    }

    public LiteralConcept getConceptForNode(Node node) {
        for (Map.Entry<LiteralConcept,NodeBranchingPointPair> entry : reusedNodes.entrySet()) {
            if (entry.getValue().node==node) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Set<LiteralConcept> getDontReuseConceptsEver() {
        return dontReuseConceptsEver;
    }

    protected class IndividualReuseBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastAbstractRoleConcept existential;
        protected final Node node;
        protected final boolean wasParentReuse;

        public IndividualReuseBranchingPoint(Tableau tableau,AtLeastAbstractRoleConcept existential,Node node,boolean wasParentReuse) {
            super(tableau);
            this.existential=existential;
            this.node=node;
            this.wasParentReuse=wasParentReuse;
        }

        public void startNextChoice(Tableau tableau,DependencySet clashDependencySet) {
            if (!wasParentReuse) {
                dontReuseConceptsThisRun.add(existential.getToConcept());
            }
            DependencySet dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(clashDependencySet,m_level);
            if (tableau.getTableauMonitor()!=null) {
                tableau.getTableauMonitor().existentialExpansionStarted(existential,node);
            }
            Node existentialNode=tableau.createNewTreeNode(dependencySet,node);
            extensionManager.addConceptAssertion(existential.getToConcept(),existentialNode,dependencySet);
            extensionManager.addRoleAssertion(existential.getOnRole(),node,existentialNode,dependencySet);
            if (tableau.getTableauMonitor()!=null) {
                tableau.getTableauMonitor().existentialExpansionFinished(existential,node);
            }
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
