package org.semanticweb.HermiT.tableau;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public final class NominalIntroductionManager implements Serializable {
    private static final long serialVersionUID=5863617010809297861L;

    protected static final int ROOT_NODE=0;
    protected static final int TREE_NODE=1;
    protected static final int AT_MOST_CONCEPT=2;
    
    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final MergingManager m_mergingManager;
    protected final Map<RootAtMostPair,Node[]> m_newRoots;
    protected final TupleTable m_targets;
    protected final Object[] m_buffer;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch1Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch2Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch01Bound;
    protected final ExtensionTable.Retrieval m_ternaryExtensionTableSearch02Bound;
    protected int[] m_indicesByBranchingPoint;
    protected int m_firstUnprocessedTarget;
    
    public NominalIntroductionManager(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_mergingManager=m_tableau.getMergingManager();
        m_newRoots=new HashMap<RootAtMostPair,Node[]>();
        m_targets=new TupleTable(3);
        m_buffer=new Object[3];
        m_ternaryExtensionTableSearch1Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch2Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch01Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
        m_ternaryExtensionTableSearch02Bound=m_extensionManager.m_ternaryExtensionTable.createRetrieval(new boolean[] { true,false,true },ExtensionTable.View.TOTAL);
        m_indicesByBranchingPoint=new int[10*2];
        m_firstUnprocessedTarget=0;
    }
    public void clear() {
        for (int index=m_buffer.length-1;index>=0;--index)
            m_buffer[index]=null;
        m_newRoots.clear();
        m_targets.clear();
        m_firstUnprocessedTarget=0;
        m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().getLevel()]=m_firstUnprocessedTarget;
        m_indicesByBranchingPoint[m_tableau.getCurrentBranchingPoint().getLevel()+1]=m_targets.getFirstFreeTupleIndex();
    }
    public void branchingPointPushed() {
        int start=m_tableau.getCurrentBranchingPoint().getLevel()*2;
        int requiredSize=start+2;
        if (requiredSize>m_indicesByBranchingPoint.length) {
            int newSize=m_indicesByBranchingPoint.length*3/2;
            while (requiredSize>newSize)
                newSize=newSize*3/2;
            int[] newIndicesByBranchingPoint=new int[newSize];
            System.arraycopy(m_indicesByBranchingPoint,0,newIndicesByBranchingPoint,0,m_indicesByBranchingPoint.length);
            m_indicesByBranchingPoint=newIndicesByBranchingPoint;
        }
        m_indicesByBranchingPoint[start]=m_firstUnprocessedTarget;
        m_indicesByBranchingPoint[start+1]=m_targets.getFirstFreeTupleIndex();
    }
    public void backtrack() {
        int start=m_tableau.getCurrentBranchingPoint().getLevel()*2;
        m_firstUnprocessedTarget=m_indicesByBranchingPoint[start];
        m_targets.truncate(m_indicesByBranchingPoint[start+1]);
    }
    public void processTargets() {
        while (m_firstUnprocessedTarget<m_targets.getFirstFreeTupleIndex()) {
            m_targets.retrieveTuple(m_buffer,m_firstUnprocessedTarget);
            Node rootNode=(Node)m_buffer[ROOT_NODE];
            Node treeNode=(Node)m_buffer[TREE_NODE];
            m_firstUnprocessedTarget++;
            if (rootNode.isInTableau() && treeNode.isInTableau()) {
                AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)m_buffer[AT_MOST_CONCEPT];
                Node[] newRoots=getRootsFor(rootNode,atMost);
                Node newRootNode=newRoots[0];
                DependencySet dependencySet=m_extensionManager.getConceptAssertionDependencySet(atMost,rootNode);
                dependencySet=m_tableau.getDependencySetFactory().unionWith(dependencySet,m_extensionManager.getRoleAssertionDependencySet(atMost.getOnAbstractRole(),rootNode,treeNode));
                if (!AtomicConcept.THING.equals(atMost.getToAtomicConcept()))
                    dependencySet=m_tableau.getDependencySetFactory().unionWith(dependencySet,m_extensionManager.getConceptAssertionDependencySet(atMost.getToAtomicConcept(),treeNode));
                if (atMost.getCaridnality()>1) {
                    BranchingPoint branchingPoint=new NominalIntroductionBranchingPoint(m_tableau,treeNode,newRoots);
                    m_tableau.pushBranchingPoint(branchingPoint);
                    dependencySet=m_tableau.getDependencySetFactory().addBranchingPoint(dependencySet,branchingPoint.getLevel());
                }
                if (!newRootNode.isInTableau()) {
                    if (newRootNode.isMerged())
                        newRootNode=newRootNode.getCanonicalNode();
                    else
                        m_tableau.insertIntoTableau(newRootNode,dependencySet);
                    assert newRootNode.isInTableau() : "The target of nominal introduction should be in the tableau.";
                }
                m_mergingManager.mergeNodes(treeNode,newRootNode,dependencySet);
            }
        }
    }
    protected Node[] getRootsFor(Node rootNode,AtMostAbstractRoleGuard atMost) {
        RootAtMostPair key=new RootAtMostPair(rootNode,atMost);
        Node[] result=m_newRoots.get(key);
        if (result==null) {
            result=new Node[atMost.getCaridnality()];
            for (int index=0;index<result.length;index++)
                result[index]=m_tableau.createNewNodeRaw(null,NodeType.ROOT_NODE,0);
            m_newRoots.put(key,result);
        }
        return result;
    }
    public void addNonnegativeConceptAssertion(Concept concept,Node node) {
        if (node.getNodeType()==NodeType.ROOT_NODE && concept instanceof AtMostAbstractRoleGuard) {
            AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)concept;
            AbstractRole onAbstractRole=atMost.getOnAbstractRole();
            AtomicConcept toAtomicConcept=atMost.getToAtomicConcept();
            if (onAbstractRole instanceof AtomicAbstractRole && node.m_numberOfNIAssertionsFromNode>0) {
                m_ternaryExtensionTableSearch01Bound.getBindingsBuffer()[0]=onAbstractRole;
                m_ternaryExtensionTableSearch01Bound.getBindingsBuffer()[1]=node;
                m_ternaryExtensionTableSearch01Bound.open();
                while (!m_ternaryExtensionTableSearch01Bound.afterLast()) {
                    Node treeNode=(Node)m_ternaryExtensionTableSearch01Bound.getTupleBuffer()[2];
                    if (treeNode.getNodeType()==NodeType.TREE_NODE && !node.isParentOf(treeNode) && (AtomicConcept.THING.equals(toAtomicConcept) || treeNode.m_positiveLabel.contains(toAtomicConcept)))
                        addTarget(node,treeNode,atMost);
                    m_ternaryExtensionTableSearch01Bound.next();
                }
            }
            else if (onAbstractRole instanceof InverseAbstractRole && node.m_numberOfNIAssertionsToNode>0) {
                m_ternaryExtensionTableSearch02Bound.getBindingsBuffer()[0]=((InverseAbstractRole)onAbstractRole).getInverseOf();
                m_ternaryExtensionTableSearch02Bound.getBindingsBuffer()[2]=node;
                m_ternaryExtensionTableSearch02Bound.open();
                while (!m_ternaryExtensionTableSearch02Bound.afterLast()) {
                    Node treeNode=(Node)m_ternaryExtensionTableSearch02Bound.getTupleBuffer()[1];
                    if (treeNode.getNodeType()==NodeType.TREE_NODE && !node.isParentOf(treeNode) && (AtomicConcept.THING.equals(toAtomicConcept) || treeNode.m_positiveLabel.contains(toAtomicConcept)))
                        addTarget(node,treeNode,atMost);
                    m_ternaryExtensionTableSearch02Bound.next();
                }
            }
        }
        else if (node.getNodeType()==NodeType.TREE_NODE && concept instanceof AtomicConcept) {
            if (node.m_numberOfNIAssertionsFromNode>0) {
                m_ternaryExtensionTableSearch1Bound.getBindingsBuffer()[1]=node;
                m_ternaryExtensionTableSearch1Bound.open();
                while (!m_ternaryExtensionTableSearch1Bound.afterLast()) {
                    Node rootNode=(Node)m_ternaryExtensionTableSearch1Bound.getTupleBuffer()[2];
                    if (rootNode.getNodeType()==NodeType.ROOT_NODE && !rootNode.isParentOf(node)) {
                        AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)m_ternaryExtensionTableSearch1Bound.getTupleBuffer()[0];
                        for (Concept rootNodeConcept : rootNode.m_positiveLabel)
                            if (rootNodeConcept instanceof AtMostAbstractRoleGuard) {
                                AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)rootNodeConcept;
                                if (atMost.getOnAbstractRole() instanceof InverseAbstractRole && ((InverseAbstractRole)atMost.getOnAbstractRole()).getInverseOf().equals(atomicAbstractRole))
                                    addTarget(rootNode,node,atMost);
                            }
                    }
                    m_ternaryExtensionTableSearch1Bound.next();
                }
            }
            if (node.m_numberOfNIAssertionsToNode>0) {
                m_ternaryExtensionTableSearch2Bound.getBindingsBuffer()[2]=node;
                m_ternaryExtensionTableSearch2Bound.open();
                while (!m_ternaryExtensionTableSearch2Bound.afterLast()) {
                    Node rootNode=(Node)m_ternaryExtensionTableSearch2Bound.getTupleBuffer()[1];
                    if (rootNode.getNodeType()==NodeType.ROOT_NODE && !rootNode.isParentOf(node)) {
                        AtomicAbstractRole atomicAbstractRole=(AtomicAbstractRole)m_ternaryExtensionTableSearch2Bound.getTupleBuffer()[0];
                        for (Concept rootNodeConcept : rootNode.m_positiveLabel)
                            if (rootNodeConcept instanceof AtMostAbstractRoleGuard) {
                                AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)rootNodeConcept;
                                if (atMost.getOnAbstractRole().equals(atomicAbstractRole))
                                    addTarget(rootNode,node,atMost);
                            }
                    }
                    m_ternaryExtensionTableSearch2Bound.next();
                }
            }
        }
    }
    public void addAtomicAbstractRoleAssertion(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        if (nodeFrom.getNodeType()==NodeType.ROOT_NODE && nodeTo.getNodeType()==NodeType.TREE_NODE && !nodeFrom.isParentOf(nodeTo)) {
            nodeFrom.m_numberOfNIAssertionsFromNode++;
            nodeTo.m_numberOfNIAssertionsToNode++;
            for (Concept concept : nodeFrom.m_positiveLabel)
                if (concept instanceof AtMostAbstractRoleGuard) {
                    AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)concept;
                    if (atMost.getOnAbstractRole().equals(atomicAbstractRole)) {
                        AtomicConcept toAtomicConcept=atMost.getToAtomicConcept();
                        if (AtomicConcept.THING.equals(toAtomicConcept) || nodeTo.m_positiveLabel.contains(toAtomicConcept))
                            addTarget(nodeFrom,nodeTo,atMost);
                    }
                }
        }
        else if (nodeFrom.getNodeType()==NodeType.TREE_NODE && nodeTo.getNodeType()==NodeType.ROOT_NODE && !nodeTo.isParentOf(nodeFrom)) {
            nodeFrom.m_numberOfNIAssertionsFromNode++;
            nodeTo.m_numberOfNIAssertionsToNode++;
            for (Concept concept : nodeTo.m_positiveLabel)
                if (concept instanceof AtMostAbstractRoleGuard) {
                    AtMostAbstractRoleGuard atMost=(AtMostAbstractRoleGuard)concept;
                    if (atMost.getOnAbstractRole() instanceof InverseAbstractRole && ((InverseAbstractRole)atMost.getOnAbstractRole()).getInverseOf().equals(atomicAbstractRole)) {
                        AtomicConcept toAtomicConcept=atMost.getToAtomicConcept();
                        if (AtomicConcept.THING.equals(toAtomicConcept) || nodeFrom.m_positiveLabel.contains(toAtomicConcept))
                            addTarget(nodeTo,nodeFrom,atMost);
                    }
                }
        }
    }
    public void removeAtomicAbstractRoleAssertion(AtomicAbstractRole atomicAbstractRole,Node nodeFrom,Node nodeTo) {
        if ((nodeFrom.getNodeType()==NodeType.ROOT_NODE && nodeTo.getNodeType()==NodeType.TREE_NODE && !nodeFrom.isParentOf(nodeTo)) ||
            (nodeFrom.getNodeType()==NodeType.TREE_NODE && nodeTo.getNodeType()==NodeType.ROOT_NODE && !nodeTo.isParentOf(nodeFrom))) {
            nodeFrom.m_numberOfNIAssertionsFromNode--;
            nodeTo.m_numberOfNIAssertionsToNode--;
        }
    }
    protected void addTarget(Node rootNode,Node treeNode,AtMostAbstractRoleGuard atMost) {
        m_buffer[ROOT_NODE]=rootNode;
        m_buffer[TREE_NODE]=treeNode;
        m_buffer[AT_MOST_CONCEPT]=atMost;
        m_targets.addTuple(m_buffer);
    }
    
    protected static final class RootAtMostPair {
        public final Node m_rootNode;
        public final AtMostAbstractRoleGuard m_atMost;
        
        public RootAtMostPair(Node rootNode,AtMostAbstractRoleGuard atMost) {
            m_rootNode=rootNode;
            m_atMost=atMost;
        }
        public int hashCode() {
            return m_rootNode.hashCode()+m_atMost.hashCode();
        }
        public boolean equals(Object that) {
            RootAtMostPair thatPair=(RootAtMostPair)that;
            return m_rootNode==thatPair.m_rootNode && m_atMost.equals(thatPair.m_atMost);
        }
    }
    
    protected static class NominalIntroductionBranchingPoint extends BranchingPoint {
        private static final long serialVersionUID=6678113479704184263L;

        protected final Tableau m_tableau;
        protected final Node m_treeNode;
        protected final Node[] m_newRoots;
        protected int m_currentRootNode;
        
        public NominalIntroductionBranchingPoint(Tableau tableau,Node treeNode,Node[] newRoots) {
            super(tableau);
            m_tableau=tableau;
            m_treeNode=treeNode;
            m_newRoots=newRoots;
            m_currentRootNode=0; // This reflects the assumption that the first merge is performed from the NominalIntroductionManager
        }
        public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
            m_currentRootNode++;
            assert m_currentRootNode<m_newRoots.length : "Unsuspected end of new root nodes.";
            DependencySet dependencySet=clashDepdendencySet;
            if (m_currentRootNode==m_newRoots.length-1)
                dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(dependencySet,m_level);
            Node newRootNode=m_newRoots[m_currentRootNode];
            if (!newRootNode.isInTableau()) {
                if (newRootNode.isMerged())
                    newRootNode=newRootNode.getCanonicalNode();
                else
                    m_tableau.insertIntoTableau(newRootNode,dependencySet);
                assert newRootNode.isInTableau() : "The target of nominal introduction should be in the tableau.";
            }
            m_tableau.m_mergingManager.mergeNodes(m_treeNode,newRootNode,dependencySet);
        }
    }
}
