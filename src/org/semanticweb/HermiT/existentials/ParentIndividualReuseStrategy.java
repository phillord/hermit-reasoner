package org.semanticweb.HermiT.existentials;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class ParentIndividualReuseStrategy implements ExistentialsExpansionStrategy,Serializable {
    private static final long serialVersionUID=-7373787507623860081L;
    
    protected final BlockingStrategy m_blockingStrategy;
    protected final Map<Node,NodeInfo> m_rootNodeInfos;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    
    public ParentIndividualReuseStrategy(BlockingStrategy blockingStrategy) {
        m_blockingStrategy=blockingStrategy;
        m_rootNodeInfos=new HashMap<Node,NodeInfo>();
    }
    public void intialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_blockingStrategy.initialize(tableau);
    }
    public void clear() {
        m_blockingStrategy.clear();
        m_rootNodeInfos.clear();
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
                            if (!m_existentialExpansionManager.tryFunctionalExpansion(atLeastAbstractRoleConcept,node)) {
                                if (!expandWithReuse(atLeastAbstractRoleConcept,node))
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
    protected boolean expandWithReuse(AtLeastAbstractRoleConcept atLeastAbstractRoleConcept,Node node) {
        if (atLeastAbstractRoleConcept.getNumber()!=1 || node.getNodeType()!=NodeType.ROOT_NODE)
            return false;
        Concept toConcept=atLeastAbstractRoleConcept.getToConcept();
        Node reuseTarget=node;
        while (reuseTarget!=null) {
            NodeInfo nodeInfo=m_rootNodeInfos.get(reuseTarget);
            if (nodeInfo==null) {
                reuseTarget=null;
                break;
            }
//            if (reuseTarget.isActive() && nodeInfo.m_nodeConcept.equals(toConcept))
//                break;
            if (reuseTarget.isActive() && m_extensionManager.containsConceptAssertion(toConcept,reuseTarget))
                break;
            reuseTarget=nodeInfo.m_createdByNode;
        }
        if (m_tableau.getTableauMonitor()!=null)
            m_tableau.getTableauMonitor().existentialExpansionStarted(atLeastAbstractRoleConcept,node);
        DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atLeastAbstractRoleConcept,node);
        AtomicConcept toAtomicConcept=(AtomicConcept)atLeastAbstractRoleConcept.getToConcept();
        Node existentialNode;
        if (reuseTarget==null) {
            BranchingPoint branchingPoint=new ParentIndividualResueBranchingPoint(m_tableau,atLeastAbstractRoleConcept,node);
            m_tableau.pushBranchingPoint(branchingPoint);
            dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
            existentialNode=m_tableau.createNewRootNode(dependencySet,0);
            m_extensionManager.addConceptAssertion(toAtomicConcept,existentialNode,dependencySet);
            m_rootNodeInfos.put(existentialNode,new NodeInfo(node,toAtomicConcept));
        }
        else {
            dependencySet=reuseTarget.addCacnonicalNodeDependencySet(dependencySet);
            existentialNode=reuseTarget.getCanonicalNode();
        }
        m_extensionManager.addRoleAssertion(atLeastAbstractRoleConcept.getOnAbstractRole(),node,existentialNode,dependencySet);
        if (m_tableau.getTableauMonitor()!=null)
            m_tableau.getTableauMonitor().existentialExpansionFinished(atLeastAbstractRoleConcept,node);
        return true;
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
        m_rootNodeInfos.remove(node);
    }
    public void branchingPointPushed() {
    }
    public void backtrack() {
    }
    public void modelFound() {
    }
    public boolean isDeterministic() {
        return false;
    }
    
    protected class ParentIndividualResueBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=-5715836252258022216L;

        protected final AtLeastAbstractRoleConcept m_existential;
        protected final Node m_node;

        public ParentIndividualResueBranchingPoint(Tableau tableau,AtLeastAbstractRoleConcept existential,Node node) {
            super(tableau);
            m_existential=existential;
            m_node=node;
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
            System.out.println("Backtracking!");
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
    
    protected static final class NodeInfo implements Serializable {
        private static final long serialVersionUID=-7228922712378526374L;

        protected final Node m_createdByNode;
        protected final AtomicConcept m_nodeConcept;
        
        public NodeInfo(Node createdByNode,AtomicConcept nodeConcept) {
            m_createdByNode=createdByNode;
            m_nodeConcept=nodeConcept;
        }
    }
}
