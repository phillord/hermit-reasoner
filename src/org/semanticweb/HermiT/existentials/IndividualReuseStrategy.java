package org.semanticweb.HermiT.existentials;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class IndividualReuseStrategy implements ExistentialsExpansionStrategy,Serializable {
    private static final long serialVersionUID=-7373787507623860081L;
    
    protected final BlockingStrategy m_blockingStrategy;
    protected final boolean m_isDeterministic;
    protected final Map<LiteralConcept,NodeBranchingPointPair> m_reusedNodes;
    protected final Set<LiteralConcept> m_doReuseConceptsAlways;
    protected final Set<LiteralConcept> m_dontReueseConceptsThisRun;
    protected final Set<LiteralConcept> m_dontReueseConceptsEver;
    protected final TupleTable m_reuseBacktrackingTable;
    protected final Object[] m_auxiliaryBuffer;
    protected int[] m_indicesByBranchingPoint;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    
    public IndividualReuseStrategy(BlockingStrategy blockingStrategy,boolean isDeterministic) {
        m_blockingStrategy=blockingStrategy;
        m_isDeterministic=isDeterministic;
        m_reusedNodes=new HashMap<LiteralConcept,NodeBranchingPointPair>();
        m_doReuseConceptsAlways=new HashSet<LiteralConcept>();
        m_dontReueseConceptsThisRun=new HashSet<LiteralConcept>();
        m_dontReueseConceptsEver=new HashSet<LiteralConcept>();
        m_reuseBacktrackingTable=new TupleTable(1);
        m_auxiliaryBuffer=new Object[1];
        m_indicesByBranchingPoint=new int[10];
    }
    @SuppressWarnings("unchecked")
    public void intialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_doReuseConceptsAlways.clear();
        m_dontReueseConceptsEver.clear();
        m_blockingStrategy.initialize(tableau);
        Object object=m_tableau.getParameters().get("IndividualReuseStrategy.reuseAlways");
        if (object instanceof Set)
            m_doReuseConceptsAlways.addAll((Set<? extends LiteralConcept>)object);
        object=m_tableau.getParameters().get("IndividualReuseStrategy.reuseNever");
        if (object instanceof Set)
            m_dontReueseConceptsEver.addAll((Set<? extends LiteralConcept>)object);
    }
    public void clear() {
        m_reusedNodes.clear();
        m_reuseBacktrackingTable.clear();
        m_blockingStrategy.clear();
        m_dontReueseConceptsThisRun.clear();
        m_dontReueseConceptsThisRun.addAll(m_dontReueseConceptsEver);
    }
    public boolean expandExistentials() {
        m_blockingStrategy.computeBlocking();
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.isActive() && !node.isBlocked() && node.hasUnprocessedExistentials()) {
                while (node.hasUnprocessedExistentials()) {
                    ExistentialConcept existentialConcept=node.getSomeUnprocessedExistential();
                    if (existentialConcept instanceof AtLeastAbstractRoleConcept) {
                        AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)existentialConcept;
                        boolean isExistentialSatisfied=m_existentialExpansionManager.isSatisfied(atLeastAbstractRoleConcept,node);
                        // Mark the existential as processed BEFORE any branching takes place
                        m_existentialExpansionManager.markExistentialProcessed(atLeastAbstractRoleConcept,node);
                        if (!isExistentialSatisfied) {
                            if (!m_existentialExpansionManager.tryFunctionalExpansion(atLeastAbstractRoleConcept,node)) 
                                if (!tryParentReuse(atLeastAbstractRoleConcept,node))
                                    if (!expandWithModelReuse(atLeastAbstractRoleConcept,node))
                                        m_existentialExpansionManager.doNormalExpansion(atLeastAbstractRoleConcept,node);
                        }
                        else {
                            if (m_tableau.getTableauMonitor()!=null)
                                m_tableau.getTableauMonitor().existentialSatisfied(atLeastAbstractRoleConcept,node);
                        }
                    }
                    else
                        throw new IllegalStateException("Unsupported type of existential concept in IndividualReuseStrategy.");
                }
                return true;
            }
            node=node.getNextTableauNode();
        }
        return false;
    }
    protected boolean tryParentReuse(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node node) {
        if (atLeastAbstractRoleConcept.getNumber()==1) {
            Node parent=node.getParent();
            if (parent!=null && m_extensionManager.containsConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),parent)) {
                DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualResueBranchingPoint(m_tableau,atLeastAbstractRoleConcept,node,true);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,parent,dependencySet);
                return true;
            }
        }
        return false;
    }
    protected boolean expandWithModelReuse(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node node) {
        LiteralConcept toConcept=atLeastAbstractRoleConcept.getToConcept();
        if ((toConcept instanceof AtomicConcept) && ((AtomicConcept)toConcept).getURI().startsWith("internal:"))
            return false;
        if (atLeastAbstractRoleConcept.getNumber()==1 && (m_doReuseConceptsAlways.contains(toConcept) || !m_dontReueseConceptsThisRun.contains(toConcept))) {
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractRoleConcept,node);
            DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
            Node existentialNode;
            NodeBranchingPointPair reuseInfo=m_reusedNodes.get(toConcept);
            if (reuseInfo==null) {
                if (!m_isDeterministic) {
                    BranchingPoint branchingPoint=new IndividualResueBranchingPoint(m_tableau,atLeastAbstractRoleConcept,node,false);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                existentialNode=m_tableau.createNewRootNode(dependencySet,0);
                reuseInfo=new NodeBranchingPointPair(existentialNode,m_tableau.getCurrentBranchingPoint().getLevel());
                m_reusedNodes.put(toConcept,reuseInfo);
                m_extensionManager.addConceptAssertion(toConcept,existentialNode,dependencySet);
                m_auxiliaryBuffer[0]=toConcept;
                m_reuseBacktrackingTable.addTuple(m_auxiliaryBuffer);
            }
            else {
                dependencySet=reuseInfo.m_node.addCacnonicalNodeDependencySet(dependencySet);
                existentialNode=reuseInfo.m_node.getCanonicalNode();
                dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,reuseInfo.m_branchingPoint);
            }
            m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,existentialNode,dependencySet);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractRoleConcept,node);
            return true;
        }
        return false;
    }
    public void assertionAdded(Concept concept,Node node) {
        m_blockingStrategy.assertionAdded(concept,node);
    }
    public void assertionRemoved(Concept concept,Node node) {
        m_blockingStrategy.assertionRemoved(concept,node);
    }
    public void assertionAdded(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        m_blockingStrategy.assertionAdded(atomicAbstractRole,nodeFrom,nodeTo);
    }
    public void assertionRemoved(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        m_blockingStrategy.assertionRemoved(atomicAbstractRole,nodeFrom,nodeTo);
    }
    public void nodeStatusChanged(Node node) {
        m_blockingStrategy.nodeStatusChanged(node);
    }
    public void nodeDestroyed(Node node) {
        m_blockingStrategy.nodeDestroyed(node);
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
        m_dontReueseConceptsEver.addAll(m_dontReueseConceptsThisRun);
    }
    public boolean isDeterministic() {
        return m_isDeterministic;
    }
    public LiteralConcept getConceptForNode(Node node) {
        for (Map.Entry<LiteralConcept,NodeBranchingPointPair> entry : m_reusedNodes.entrySet())
            if (entry.getValue().m_node==node)
                return entry.getKey();
        return null;
    }
    public Set<LiteralConcept> getDontReuseConceptsEver() {
        return m_dontReueseConceptsEver;
    }
    
    protected class IndividualResueBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastAbstractRoleConcept m_existential;
        protected final Node m_node;
        protected final boolean m_wasParentReuse;

        public IndividualResueBranchingPoint(Tableau tableau,AtLeastAbstractRoleConcept existential,Node node,boolean wasParentReuse) {
            super(tableau);
            m_existential=existential;
            m_node=node;
            m_wasParentReuse=wasParentReuse;
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
            if (!m_wasParentReuse)
                m_dontReueseConceptsThisRun.add(m_existential.getToConcept());
            DependencySet dependencySet=m_tableau.getDependencySetFactory().removeBranchingPoint(clashDepdendencySet,m_level);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionStarted(m_existential,m_node);
            Node existentialNode=tableau.createNewTreeNode(dependencySet,m_node);
            m_extensionManager.addConceptAssertion(m_existential.getToConcept(),existentialNode,dependencySet);
            m_extensionManager.addRoleAssertion(m_existential.getOnAbstractRole(),m_node,existentialNode,dependencySet);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionFinished(m_existential,m_node);
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
