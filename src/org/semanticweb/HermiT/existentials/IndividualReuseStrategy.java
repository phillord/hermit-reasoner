package org.semanticweb.HermiT.existentials;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class IndividualReuseStrategy implements ExistentialsExpansionStrategy,Serializable {
    private static final long serialVersionUID=-7373787507623860081L;
    protected static final int MINIMAL_TREE_DEPTH_BEFORE_REUSE=-5;
    
    protected final boolean m_isDeterministic;
    protected final Map<AtomicConcept,Node> m_existentialNodes;
    protected final Set<AtomicConcept> m_dontReueseConceptsThisRun;
    protected final Set<AtomicConcept> m_dontReueseConceptsEver;
    protected final TupleTable m_tupleTable;
    protected final List<Node> m_newNodes;
    protected final Object[] m_auxiliatyTuple;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    protected int[] m_indicesByBranchingPoint;
    
    public IndividualReuseStrategy(boolean isDeterministic) {
        m_isDeterministic=isDeterministic;
        m_existentialNodes=new HashMap<AtomicConcept,Node>();
        m_dontReueseConceptsThisRun=new HashSet<AtomicConcept>();
        m_dontReueseConceptsEver=new HashSet<AtomicConcept>();
        m_tupleTable=new TupleTable(1);
        m_newNodes=new ArrayList<Node>();
        m_auxiliatyTuple=new Object[1];
        m_indicesByBranchingPoint=new int[2];
    }
    public void intialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_dontReueseConceptsEver.clear();
    }
    public void clear() {
        m_auxiliatyTuple[0]=null;
        m_existentialNodes.clear();
        m_tupleTable.clear();
        m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().getLevel()]=m_tupleTable.getFirstFreeTupleIndex();
        m_dontReueseConceptsThisRun.clear();
    }
    public boolean expandExistentials() {
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (node.hasUnprocessedExistentials()) {
                while (node.hasUnprocessedExistentials()) {
                    ExistentialConcept existentialConcept=node.getSomeUnprocessedExistential();
                    if (existentialConcept instanceof AtLeastAbstractRoleConcept) {
                        AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)existentialConcept;
                        boolean isExistentialSatisfied=m_existentialExpansionManager.isSatisfied(atLeastAbstractRoleConcept,node);
                        m_existentialExpansionManager.markExistentialProcessed(atLeastAbstractRoleConcept,node); // Mark the existential as processed BEFORE any branching takes place
                        if (!isExistentialSatisfied) {
                            if (m_tableau.getTableauMonitor()!=null)
                                m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractRoleConcept,node);
                            DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
                            LiteralConcept toConcept=atLeastAbstractRoleConcept.getToConcept();
                            int cardinality=atLeastAbstractRoleConcept.getNumber();
                            if (toConcept instanceof AtomicConcept && shoudReuse((AtomicConcept)toConcept) && cardinality==1 && node.getTreeDepth()>=MINIMAL_TREE_DEPTH_BEFORE_REUSE) {
                                if (!m_isDeterministic) {
                                    BranchingPoint branchingPoint=new IndividualResueBranchingPoint(m_tableau,atLeastAbstractRoleConcept,node);
                                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                                    m_tableau.pushBranchingPoint(branchingPoint);
                                }
                                AtomicConcept toAtomicConcept=(AtomicConcept)toConcept;
                                Node existentialNode=m_existentialNodes.get((AtomicConcept)toConcept);
                                if (existentialNode==null) {
                                    existentialNode=m_tableau.createNewRootNode(dependencySet,Node.GLOBALLY_UNIQUE_NODE);
                                    m_existentialNodes.put(toAtomicConcept,existentialNode);
                                    m_extensionManager.addConceptAssertion(toAtomicConcept,existentialNode,dependencySet);
                                    m_auxiliatyTuple[0]=toAtomicConcept;
                                    m_tupleTable.addTuple(m_auxiliatyTuple);
                                }
                                m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,existentialNode,dependencySet);
                            }
                            else if (cardinality==1) {
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
                            if (m_tableau.getTableauMonitor()!=null)
                                m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractRoleConcept,node);
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
    protected final boolean shoudReuse(AtomicConcept toConcept) {
        return !m_dontReueseConceptsEver.contains(toConcept) && !m_dontReueseConceptsThisRun.contains(toConcept);
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
        m_indicesByBranchingPoint[start]=m_tupleTable.getFirstFreeTupleIndex();
    }
    public void nodeWillChange(Node node) {
    }
    public void backtrack() {
        int newFirstFreeTupleIndex=m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().getLevel()];
        for (int tupleIndex=m_tupleTable.getFirstFreeTupleIndex()-1;tupleIndex>=newFirstFreeTupleIndex;--tupleIndex) {
            AtomicConcept toAtomicConcept=(AtomicConcept)m_tupleTable.getTupleObject(tupleIndex,0);
            m_existentialNodes.remove(toAtomicConcept);
        }
        m_tupleTable.truncate(newFirstFreeTupleIndex);
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
            Node existentialNode=tableau.createNewRootNode(dependencySet,m_node.getTreeDepth()+1);
            m_extensionManager.addConceptAssertion(m_existential.getToConcept(),existentialNode,dependencySet);
            m_extensionManager.addRoleAssertion(m_existential.getOnAbstractRole(),m_node,existentialNode,dependencySet);
        }
    }
}
