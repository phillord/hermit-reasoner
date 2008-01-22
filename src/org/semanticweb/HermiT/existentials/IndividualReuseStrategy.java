package org.semanticweb.HermiT.existentials;

//import java.util.List;
//import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class IndividualReuseStrategy implements ExistentialsExpansionStrategy,Serializable {
    private static final long serialVersionUID=-7373787507623860081L;
    
    protected final BlockingStrategy m_blockingStrategy;
    protected final boolean m_isDeterministic;
    protected final Map<AtomicConcept,Node> m_existentialNodes;
    protected final Set<AtomicConcept> m_dontReueseConceptsThisRun;
    protected final Set<AtomicConcept> m_dontReueseConceptsEver;
//    protected final List<Node> m_newNodes;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    
    public IndividualReuseStrategy(BlockingStrategy blockingStrategy,boolean isDeterministic) {
        m_blockingStrategy=blockingStrategy;
        m_isDeterministic=isDeterministic;
        m_existentialNodes=new HashMap<AtomicConcept,Node>();
        m_dontReueseConceptsThisRun=new HashSet<AtomicConcept>();
        m_dontReueseConceptsEver=new HashSet<AtomicConcept>();
//        m_newNodes=new ArrayList<Node>();
    }
    public void intialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_dontReueseConceptsEver.clear();
        m_blockingStrategy.initialize(tableau);
    }
    public void clear() {
        m_existentialNodes.clear();
        m_dontReueseConceptsThisRun.clear();
        m_blockingStrategy.clear();
    }
    public boolean expandExistentials() {
        m_blockingStrategy.computeBlocking();
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked() && node.hasUnprocessedExistentials()) {
                while (node.hasUnprocessedExistentials()) {
                    ExistentialConcept existentialConcept=node.getSomeUnprocessedExistential();
                    if (existentialConcept instanceof AtLeastAbstractRoleConcept) {
                        AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)existentialConcept;
                        boolean isExistentialSatisfied=m_existentialExpansionManager.isSatisfied(atLeastAbstractRoleConcept,node);
                        // Mark the existential as processed BEFORE any branching takes place
                        m_existentialExpansionManager.markExistentialProcessed(atLeastAbstractRoleConcept,node);
                        if (!isExistentialSatisfied) {
                            if (!m_existentialExpansionManager.tryFunctionalExpansion(atLeastAbstractRoleConcept,node)) {
                                LiteralConcept toConcept=atLeastAbstractRoleConcept.getToConcept();
                                if (toConcept instanceof AtomicConcept && shoudReuse((AtomicConcept)toConcept) && atLeastAbstractRoleConcept.getNumber()==1)
                                    expandWithReuse(atLeastAbstractRoleConcept,node);
                                else
                                    m_existentialExpansionManager.doNormalExpansion(atLeastAbstractRoleConcept,node);
                            }
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
    protected void expandWithReuse(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node node) {
        if (m_tableau.getTableauMonitor()!=null)
            m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractRoleConcept,node);
        DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
        if (!m_isDeterministic) {
            BranchingPoint branchingPoint=new IndividualResueBranchingPoint(m_tableau,atLeastAbstractRoleConcept,node);
            m_tableau.pushBranchingPoint(branchingPoint);
            dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
        }
        AtomicConcept toAtomicConcept=(AtomicConcept)atLeastAbstractRoleConcept.getToConcept();
        Node existentialNode=m_existentialNodes.get(toAtomicConcept);
        if (existentialNode==null) {
            existentialNode=m_tableau.createNewNodeRaw(null,NodeType.ROOT_NODE,Node.GLOBALLY_UNIQUE_NODE);
            m_existentialNodes.put(toAtomicConcept,existentialNode);
        }
        if (!existentialNode.isInTableau()) {
            assert !existentialNode.isMerged() && !existentialNode.isPruned();
            m_tableau.insertIntoTableau(existentialNode,dependencySet);
            m_extensionManager.addConceptAssertion(toAtomicConcept,existentialNode,dependencySet);
        }
        m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,existentialNode,dependencySet);
        if (m_tableau.getTableauMonitor()!=null)
            m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractRoleConcept,node);
    }
/*    protected void expandNoReuse(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node node) {
        DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
        int cardinality=atLeastAbstractRoleConcept.getNumber();
        if (cardinality==1) {
            Node newNode=m_tableau.createNewRootNode(dependencySet,node.getTreeDepth()+1);
            m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,newNode,dependencySet);
            m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),newNode,dependencySet);
        }
        else {
            m_newNodes.clear();
            for (int index=0;index<cardinality;index++) {
                Node newNode=m_tableau.createNewRootNode(dependencySet,node.getTreeDepth()+1);
                m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,newNode,dependencySet);
                m_extensionManager.addConceptAssertion(atLeastAbstractRoleConcept.getToConcept(),newNode,dependencySet);
                m_newNodes.add(newNode);
            }
            for (int outerIndex=0;outerIndex<cardinality;outerIndex++) {
                Node outerNode=m_newNodes.get(outerIndex);
                for (int innerIndex=outerIndex+1;innerIndex<cardinality;innerIndex++)
                    m_extensionManager.addAssertion(Inequality.INSTANCE,outerNode,m_newNodes.get(innerIndex),dependencySet);
            }
            m_newNodes.clear();
        }
    }*/
    protected boolean shoudReuse(AtomicConcept toConcept) {
        return !m_dontReueseConceptsEver.contains(toConcept) && !m_dontReueseConceptsThisRun.contains(toConcept);
    }
    public void nodeWillChange(Node node) {
        m_blockingStrategy.nodeWillChange(node);
    }
    public void branchingPointPushed() {
    }
    public void backtrack() {
    }
    public void modelFound() {
        m_dontReueseConceptsEver.addAll(m_dontReueseConceptsThisRun);
    }
    public boolean isDeterministic() {
        return m_isDeterministic;
    }
    public AtomicConcept getConceptForNode(Node node) {
        for (Map.Entry<AtomicConcept,Node> entry : m_existentialNodes.entrySet())
            if (entry.getValue()==node)
                return entry.getKey();
        return null;
    }
    
    protected class IndividualResueBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastAbstractRoleConcept m_existential;
        protected final Node m_node;

        public IndividualResueBranchingPoint(Tableau tableau,AtLeastAbstractRoleConcept existential,Node node) {
            super(tableau);
            m_existential=existential;
            m_node=node;
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
            m_dontReueseConceptsThisRun.add((AtomicConcept)m_existential.getToConcept());
            DependencySet dependencySet=m_tableau.getDependencySetFactory().removeBranchingPoint(clashDepdendencySet,m_level);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionStarted(m_existential,m_node);
            Node existentialNode=tableau.createNewGraphNode(m_node,dependencySet);
            m_extensionManager.addConceptAssertion(m_existential.getToConcept(),existentialNode,dependencySet);
            m_extensionManager.addRoleAssertion(m_existential.getOnAbstractRole(),m_node,existentialNode,dependencySet);
            if (m_tableau.getTableauMonitor()!=null)
                m_tableau.getTableauMonitor().existentialExpansionFinished(m_existential,m_node);
        }
    }
}