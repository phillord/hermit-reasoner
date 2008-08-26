// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.Node.NodeState;

public final class Tableau implements Serializable {
    private static final long serialVersionUID=-28982363158925221L;

    protected final TableauMonitor m_tableauMonitor;
    protected final ExistentialsExpansionStrategy m_existentialsExpansionStrategy;
    protected final DLOntology m_dlOntology;
    protected final Map<String,Object> m_parameters;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final ExtensionManager m_extensionManager;
    protected final LabelManager m_labelManager;
    protected final HyperresolutionManager m_hyperresolutionManager;
    protected final MergingManager m_mergingManager;
    protected final ExistentialExpansionManager m_existentialExpasionManager;
    protected final NominalIntroductionManager m_nominalIntroductionManager;
    protected final DescriptionGraphManager m_descriptionGraphManager;
    protected final boolean m_needsThingExtension;
    protected final List<List<ExistentialConcept>> m_existentialConceptsBuffers;
    protected BranchingPoint[] m_branchingPoints;
    protected int m_currentBranchingPoint;
    protected int m_nonbacktrackableBranchingPoint;
    protected boolean m_isCurrentModelDeterministic;
    protected int m_allocatedNodes;
    protected int m_numberOfNodesInTableau;
    protected int m_numberOfMergedOrPrunedNodes;
    protected int m_numberOfNodeCreations;
    protected Node m_firstFreeNode;
    protected Node m_firstTableauNode;
    protected Node m_lastTableauNode;
    protected Node m_lastMergedOrPrunedNode;
    protected GroundDisjunction m_firstGroundDisjunction;
    protected GroundDisjunction m_firstUnprocessedGroundDisjunction;
    protected Node m_checkedNode;

    public Tableau(TableauMonitor tableauMonitor,ExistentialsExpansionStrategy existentialsExpansionStrategy,DLOntology dlOntology,Map<String,Object> parameters) {
        m_parameters=parameters;
        m_tableauMonitor=tableauMonitor;
        m_existentialsExpansionStrategy=existentialsExpansionStrategy;
        m_dlOntology=dlOntology;
        m_dependencySetFactory=new DependencySetFactory();
        m_extensionManager=new ExtensionManager(this);
        m_labelManager=new LabelManager(this);
        m_hyperresolutionManager=new HyperresolutionManager(this);
        m_mergingManager=new MergingManager(this);
        m_existentialExpasionManager=new ExistentialExpansionManager(this);
        m_nominalIntroductionManager=new NominalIntroductionManager(this);
        m_descriptionGraphManager=new DescriptionGraphManager(this);
        m_existentialsExpansionStrategy.intialize(this);
        m_needsThingExtension=m_hyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.THING);
        m_existentialConceptsBuffers=new ArrayList<List<ExistentialConcept>>();
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        m_nonbacktrackableBranchingPoint=-1;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.setTableau(this);
    }
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    public Map<String,Object> getParameters() {
        return m_parameters;
    }
    public TableauMonitor getTableauMonitor() {
        return m_tableauMonitor;
    }
    public ExistentialsExpansionStrategy getExistentialsExpansionStrategy() {
        return m_existentialsExpansionStrategy;
    }
    public boolean isDeterministic() {
        return m_dlOntology.isHorn() && m_existentialsExpansionStrategy.isDeterministic();
    }
    public DependencySetFactory getDependencySetFactory() {
        return m_dependencySetFactory;
    }
    public ExtensionManager getExtensionManager() {
        return m_extensionManager;
    }
    public LabelManager getLabelManager() {
        return m_labelManager;
    }
    public HyperresolutionManager getHyperresolutionManager() {
        return m_hyperresolutionManager;
    }
    public MergingManager getMergingManager() {
        return m_mergingManager;
    }
    public ExistentialExpansionManager getExistentialExpansionManager() {
        return m_existentialExpasionManager;
    }
    public NominalIntroductionManager getNominalIntroductionManager() {
        return m_nominalIntroductionManager;
    }
    public DescriptionGraphManager getDescriptionGraphManager() {
        return m_descriptionGraphManager;
    }
    public void clear() {
        m_allocatedNodes=0;
        m_numberOfNodesInTableau=0;
        m_numberOfMergedOrPrunedNodes=0;
        m_numberOfNodeCreations=0;
        m_firstFreeNode=null;
        m_firstTableauNode=null;
        m_lastTableauNode=null;
        m_lastMergedOrPrunedNode=null;
        m_firstGroundDisjunction=null;
        m_firstUnprocessedGroundDisjunction=null;
        m_checkedNode=null;
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        m_nonbacktrackableBranchingPoint=-1;
        m_dependencySetFactory.clear();
        m_extensionManager.clear();
        m_nominalIntroductionManager.clear();
        m_descriptionGraphManager.clear();
        m_isCurrentModelDeterministic=true;
        m_existentialsExpansionStrategy.clear();
        m_existentialConceptsBuffers.clear();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.tableauCleared();
    }
    public boolean isSatisfiable() {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.saturateStarted();
        boolean hasMoreWork=true;
        while (hasMoreWork) {
            if (m_tableauMonitor!=null)
                m_tableauMonitor.iterationStarted();
            hasMoreWork=doIteration();
            if (m_tableauMonitor!=null)
                m_tableauMonitor.iterationFinished();
        }
        if (m_tableauMonitor!=null)
            m_tableauMonitor.saturateFinished();
        if (!m_extensionManager.containsClash()) {
            m_existentialsExpansionStrategy.modelFound();
            return true;
        }
        else
            return false;
    }
    protected boolean doIteration() {
        if (!m_extensionManager.containsClash()) {
            m_nominalIntroductionManager.processTargets();
            boolean hasChange=false;
            while (m_extensionManager.propagateDeltaNew() && !m_extensionManager.containsClash()) {
                m_descriptionGraphManager.checkGraphConstraints();
                m_hyperresolutionManager.applyDLClauses();
                m_nominalIntroductionManager.processTargets();
                hasChange=true;
            }
            if (hasChange)
                return true;
        }
        if (!m_extensionManager.containsClash())
            if (m_existentialsExpansionStrategy.expandExistentials())
                return true;
        if (!m_extensionManager.containsClash()) {
            while (m_firstUnprocessedGroundDisjunction!=null) {
                GroundDisjunction groundDisjunction=m_firstUnprocessedGroundDisjunction;
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.processGroundDisjunctionStarted(groundDisjunction);
                m_firstUnprocessedGroundDisjunction=groundDisjunction.m_previousGroundDisjunction;
                if (!groundDisjunction.isSatisfied(this)) {
                    DependencySet dependencySet=groundDisjunction.getDependencySet();
                    if (groundDisjunction.getNumberOfDisjuncts()>1) {
                        BranchingPoint branchingPoint=new DisjunctionBranchingPoint(this,groundDisjunction);
                        pushBranchingPoint(branchingPoint);
                        dependencySet=m_dependencySetFactory.addBranchingPoint(dependencySet,branchingPoint.getLevel());
                    }
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.disjunctProcessingStarted(groundDisjunction,0);
                    groundDisjunction.addDisjunctToTableau(this,0,dependencySet);
                    if (m_tableauMonitor!=null) {
                        m_tableauMonitor.disjunctProcessingFinished(groundDisjunction,0);
                        m_tableauMonitor.processGroundDisjunctionFinished(groundDisjunction);
                    }
                    return true;
                }
                else {
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.groundDisjunctionSatisfied(groundDisjunction);
                }
            }
        }
        if (m_extensionManager.containsClash()) {
            DependencySet clashDependencySet=m_extensionManager.getClashDependencySet();
            int newCurrentBranchingPoint=clashDependencySet.getMaximumBranchingPoint();
            if (newCurrentBranchingPoint<=m_nonbacktrackableBranchingPoint)
                return false;
            backtrackTo(newCurrentBranchingPoint);
            BranchingPoint branchingPoint=getCurrentBranchingPoint();
            if (m_tableauMonitor!=null)
                m_tableauMonitor.startNextBranchingPointStarted(branchingPoint);
            branchingPoint.startNextChoice(this,clashDependencySet);
            if (m_tableauMonitor!=null)
                m_tableauMonitor.startNextBranchingPointFinished(branchingPoint);
            m_dependencySetFactory.cleanUp();
            return true;
        }
        return false;
    }
    public Node getCheckedNode() {
        return m_checkedNode;
    }
    public boolean isSatisfiable(AtomicConcept atomicConcept) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableStarted(atomicConcept);
        clear();
        if (m_dlOntology.hasNominals())
            loadDLOntologyABox();
        m_checkedNode=createNewRootNode(m_dependencySetFactory.emptySet(),0);
        m_extensionManager.addConceptAssertion(atomicConcept,m_checkedNode,m_dependencySetFactory.emptySet());
        boolean result=isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableFinished(atomicConcept,result);
        return result;
    }
    public boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByStarted(subconcept,superconcept);
        clear();
        if (m_dlOntology.hasNominals())
            loadDLOntologyABox();
        m_checkedNode=createNewRootNode(m_dependencySetFactory.emptySet(),0);
        m_extensionManager.addConceptAssertion(subconcept,m_checkedNode,m_dependencySetFactory.emptySet());
        m_branchingPoints[0]=new BranchingPoint(this);
        m_currentBranchingPoint++;
        m_nonbacktrackableBranchingPoint=m_currentBranchingPoint;
        DependencySet dependencySet=m_dependencySetFactory.addBranchingPoint(m_dependencySetFactory.emptySet(),m_currentBranchingPoint);
        m_extensionManager.addConceptAssertion(AtomicNegationConcept.create(superconcept),m_checkedNode,dependencySet);
        boolean result=!isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByFinished(subconcept,superconcept,result);
        return result;
    }
    // public boolean isSubsumedBy(AbstractRole subRole, AbstractRole superRole) {
    //     if (m_tableauMonitor!=null) {
    //         m_tableauMonitor.isSubsumedByStarted(subRole,superRole);
    //     }
    //     clear();
    //     if (m_dlOntology.hasNominals()) {
    //         loadDLOntologyABox();
    //     }
    //     m_checkedNode =
    //         createNewRootNode(m_dependencySetFactory.emptySet(), 0);
    //     Node otherNode = 
    //         createNewRootNode(m_dependencySetFactory.emptySet(), 0);
    //     m_extensionManager.addRoleAssertion(subRole,
    //         m_checkedNode, otherNode, m_dependencySetFactory.emptySet());
    //     m_branchingPoints[0] = new BranchingPoint(this);
    //     m_currentBranchingPoint++;
    //     m_nonbacktrackableBranchingPoint = m_currentBranchingPoint;
    //     DependencySet dependencySet =
    //         m_dependencySetFactory.addBranchingPoint
    //             (m_dependencySetFactory.emptySet(), m_currentBranchingPoint);
    //     m_extensionManager.addConceptAssertion(AtomicNegationConcept.create(superconcept),m_checkedNode,dependencySet);
    //     boolean result=!isSatisfiable();
    //     if (m_tableauMonitor!=null)
    //         m_tableauMonitor.isSubsumedByFinished(subconcept,superconcept,result);
    //     return result;
    // }
    public boolean isABoxSatisfiable() {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isABoxSatisfiableStarted();
        clear();
        loadDLOntologyABox();
        if (m_firstTableauNode==null)
            createNewRootNode(m_dependencySetFactory.emptySet(),0); // Ensures that there is at least one individual
        boolean result=isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isABoxSatisfiableFinished(result);
        return result;
    }
    protected void loadDLOntologyABox() {
        Map<Individual,Node> individualsToNodes=new HashMap<Individual,Node>();
        for (Atom atom : m_dlOntology.getPositiveFacts()) {
            DLPredicate dlPredicate=atom.getDLPredicate();
            switch (dlPredicate.getArity()) {
            case 1:
                m_extensionManager.addAssertion(dlPredicate,getNodeForIndividual(individualsToNodes,(Individual)atom.getArgument(0)),m_dependencySetFactory.emptySet());
                break;
            case 2:
                m_extensionManager.addAssertion(dlPredicate,getNodeForIndividual(individualsToNodes,(Individual)atom.getArgument(0)),getNodeForIndividual(individualsToNodes,(Individual)atom.getArgument(1)),m_dependencySetFactory.emptySet());
                break;
            default:
                throw new IllegalArgumentException("Unsupported arity of positive ground atoms.");
            }
        }
        for (Atom atom : m_dlOntology.getNegativeFacts()) {
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (!(dlPredicate instanceof AtomicConcept))
                throw new IllegalArgumentException("Unsupported type of negative fact.");
            switch (dlPredicate.getArity()) {
            case 1:
                m_extensionManager.addConceptAssertion(AtomicNegationConcept.create((AtomicConcept)dlPredicate),getNodeForIndividual(individualsToNodes,(Individual)atom.getArgument(0)),m_dependencySetFactory.emptySet());
                break;
            default:
                throw new IllegalArgumentException("Unsupported arity of negative ground atoms.");
            }
        }
    }
    protected Node getNodeForIndividual(Map<Individual,Node> individualsToNodes,Individual individual) {
        Node node=individualsToNodes.get(individual);
        if (node==null) {
            node=createNewRootNode(m_dependencySetFactory.emptySet(),0);
            individualsToNodes.put(individual,node);
        }
        return node;
    }
    public boolean isCurrentModelDeterministic() {
        return m_isCurrentModelDeterministic;
    }
    public int getCurrentBranchingPointLevel() {
        return m_currentBranchingPoint;
    }
    public BranchingPoint getCurrentBranchingPoint() {
        return m_branchingPoints[m_currentBranchingPoint];
    }
    public void addGroundDisjunction(GroundDisjunction groundDisjunction) {
        groundDisjunction.m_nextGroundDisjunction=m_firstGroundDisjunction;
        groundDisjunction.m_previousGroundDisjunction=null;
        if (m_firstGroundDisjunction!=null)
            m_firstGroundDisjunction.m_previousGroundDisjunction=groundDisjunction;
        m_firstGroundDisjunction=groundDisjunction;
        if (m_firstUnprocessedGroundDisjunction==null)
            m_firstUnprocessedGroundDisjunction=groundDisjunction;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.groundDisjunctionDerived(groundDisjunction);
    }
    public GroundDisjunction getFirstUnprocessedGroundDisjunction() {
        return m_firstUnprocessedGroundDisjunction;
    }
    public void pushBranchingPoint(BranchingPoint branchingPoint) {
        assert m_currentBranchingPoint+1==branchingPoint.m_level;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.pushBranchingPointStarted(branchingPoint);
        m_currentBranchingPoint++;
        if (m_currentBranchingPoint>=m_branchingPoints.length) {
            BranchingPoint[] newBranchingPoints=new BranchingPoint[m_currentBranchingPoint*3/2];
            System.arraycopy(m_branchingPoints,0,newBranchingPoints,0,m_branchingPoints.length);
            m_branchingPoints=newBranchingPoints;
        }
        m_branchingPoints[m_currentBranchingPoint]=branchingPoint;
        m_extensionManager.branchingPointPushed();
        m_existentialExpasionManager.branchingPointPushed();
        m_existentialsExpansionStrategy.branchingPointPushed();
        m_nominalIntroductionManager.branchingPointPushed();
        m_isCurrentModelDeterministic=false;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.pushBranchingPointFinished(branchingPoint);
    }
    protected void backtrackTo(int newCurrentBrancingPoint) {
        BranchingPoint branchingPoint=m_branchingPoints[newCurrentBrancingPoint];
        if (m_tableauMonitor!=null)
            m_tableauMonitor.backtrackToStarted(branchingPoint);
        // backtrack the list of branching points
        for (int index=newCurrentBrancingPoint+1;index<=m_currentBranchingPoint;index++)
            m_branchingPoints[index]=null;
        m_currentBranchingPoint=newCurrentBrancingPoint;
        // backtrack processed ground disjunctions
        m_firstUnprocessedGroundDisjunction=branchingPoint.m_firstUnprocessedGroundDisjunction;
        // backtrack added ground disjunctions
        GroundDisjunction firstGroundDisjunctionShouldBe=branchingPoint.m_firstGroundDisjunction;
        while (m_firstGroundDisjunction!=firstGroundDisjunctionShouldBe) {
            m_firstGroundDisjunction.destroy(this);
            m_firstGroundDisjunction=m_firstGroundDisjunction.m_nextGroundDisjunction;
        }
        if (m_firstGroundDisjunction!=null)
            m_firstGroundDisjunction.m_previousGroundDisjunction=null;
        // backtrack existentials
        m_existentialsExpansionStrategy.backtrack();
        m_existentialExpasionManager.backtrack();
        // backtrack nominal introduction
        m_nominalIntroductionManager.backtrack();
        // backtrack extensions
        m_extensionManager.backtrack();
        // backtrack node merges/prunes
        Node lastMergedOrPrunedNodeShouldBe=branchingPoint.m_lastMergedOrPrunedNode;
        while (m_lastMergedOrPrunedNode!=lastMergedOrPrunedNodeShouldBe)
            backtrackLastMergedOrPrunedNode();
        // backtrack node change list
        Node lastTableauNodeShouldBe=branchingPoint.m_lastTableauNode;
        while (lastTableauNodeShouldBe!=m_lastTableauNode)
            destroyLastTableauNode();
        // finish 
        m_extensionManager.clearClash();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.backtrackToFinished(branchingPoint);
    }
    public Node createNewRootNode(DependencySet dependencySet,int treeDepth) {
        return createNewNodeRaw(dependencySet,null,NodeType.ROOT_NODE,treeDepth);
    }
    public Node createNewTreeNode(DependencySet dependencySet,Node parent) {
        return createNewNodeRaw(dependencySet,parent,NodeType.TREE_NODE,parent.getTreeDepth()+1);
    }
    public Node createNewGraphNode(Node parent,DependencySet dependencySet) {
        return createNewNodeRaw(dependencySet,parent,NodeType.GRAPH_NODE,parent.getTreeDepth());
    }
    public Node createNewNodeRaw(DependencySet dependencySet,Node parent,NodeType nodeType,int treeDepth) {
        Node node;
        if (m_firstFreeNode==null) {
            node=new Node(this);
            m_allocatedNodes++;
        }
        else {
            node=m_firstFreeNode;
            m_firstFreeNode=m_firstFreeNode.m_nextTableauNode;
        }
        assert node.m_nodeID==-1;
        assert node.m_nodeState==null;
        node.initialize(++m_numberOfNodesInTableau,parent,nodeType,treeDepth);
        node.m_previousTableauNode=m_lastTableauNode;
        if (m_lastTableauNode==null)
            m_firstTableauNode=node;
        else
            m_lastTableauNode.m_nextTableauNode=node;
        m_lastTableauNode=node;
        m_existentialsExpansionStrategy.nodeStatusChanged(node);
        m_numberOfNodeCreations++;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.nodeCreated(node);
        m_extensionManager.addConceptAssertion(AtomicConcept.THING,node,dependencySet);
        return node;
    }
    public void mergeNode(Node node,Node mergeInto,DependencySet dependencySet) {
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        m_existentialsExpansionStrategy.nodeStatusChanged(node);
        node.m_mergedInto=mergeInto;
        node.m_mergedIntoDependencySet=m_dependencySetFactory.getPermanent(dependencySet);
        m_dependencySetFactory.addUsage(node.m_mergedIntoDependencySet);
        node.m_nodeState=NodeState.MERGED;
        node.m_previousMergedOrPrunedNode=m_lastMergedOrPrunedNode;
        m_lastMergedOrPrunedNode=node;
        m_numberOfMergedOrPrunedNodes++;
    }
    public void pruneNode(Node node) {
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        m_existentialsExpansionStrategy.nodeStatusChanged(node);
        node.m_nodeState=NodeState.PRUNED;
        node.m_previousMergedOrPrunedNode=m_lastMergedOrPrunedNode;
        m_lastMergedOrPrunedNode=node;
        m_numberOfMergedOrPrunedNodes++;
    }
    protected void backtrackLastMergedOrPrunedNode() {
        Node node=m_lastMergedOrPrunedNode;
        assert (node.m_nodeState==Node.NodeState.MERGED && node.m_mergedInto!=null && node.m_mergedInto!=null) || (node.m_nodeState==Node.NodeState.PRUNED && node.m_mergedInto==null && node.m_mergedInto==null);
        m_existentialsExpansionStrategy.nodeStatusChanged(node);
        if (node.m_nodeState==Node.NodeState.MERGED) {
            m_dependencySetFactory.removeUsage(node.m_mergedIntoDependencySet);
            node.m_mergedInto=null;
            node.m_mergedIntoDependencySet=null;
        }
        node.m_nodeState=Node.NodeState.ACTIVE;
        m_lastMergedOrPrunedNode=node.m_previousMergedOrPrunedNode;
        node.m_previousMergedOrPrunedNode=null;
        m_numberOfMergedOrPrunedNodes--;
    }
    protected void destroyLastTableauNode() {
        Node node=m_lastTableauNode;
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        m_existentialsExpansionStrategy.nodeDestroyed(node);
        if (node.m_previousTableauNode==null)
            m_firstTableauNode=null;
        else
            node.m_previousTableauNode.m_nextTableauNode=null;
        m_lastTableauNode=node.m_previousTableauNode;
        node.destroy();
        node.m_nextTableauNode=m_firstFreeNode;
        m_firstFreeNode=node;
        m_numberOfNodesInTableau--;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.nodeDestroyed(node);
    }
    public int getNumberOfNodeCreations() {
        return m_numberOfNodeCreations;
    }
    public Node getFirstTableauNode() {
        return m_firstTableauNode;
    }
    public Node getLastTableauNode() {
        return m_lastTableauNode;
    }
    public int getNumberOfAllocatedNodes() {
        return m_allocatedNodes;
    }
    public int getNumberOfNodesInTableau() {
        return m_numberOfNodesInTableau;
    }
    public int getNumberOfMergedOrPrunedNodes() {
        return m_numberOfMergedOrPrunedNodes;
    }
    public Node getNode(int nodeID) {
        Node node=m_firstTableauNode;
        while (node!=null) {
            if (node.getNodeID()==nodeID)
                return node;
            node=node.getNextTableauNode();
        }
        return null;
    }
    protected List<ExistentialConcept> getExistentialConceptsBuffer() {
        if (m_existentialConceptsBuffers.isEmpty())
            return new ArrayList<ExistentialConcept>();
        else
            return m_existentialConceptsBuffers.remove(m_existentialConceptsBuffers.size()-1);
    }
    protected void putExistentialConceptsBuffer(List<ExistentialConcept> buffer) {
        assert buffer.isEmpty();
        m_existentialConceptsBuffers.add(buffer);
    }
    protected void checkTableauList() {
        Node node=m_firstTableauNode;
        int numberOfNodesInTableau=0;
        while (node!=null) {
            if (node.m_previousTableauNode==null) {
                if (m_firstTableauNode!=node)
                    throw new IllegalStateException("First tableau node is pointing wrongly.");
            }
            else {
                if (node.m_previousTableauNode.m_nextTableauNode!=node)
                    throw new IllegalStateException("Previous tableau node is pointing wrongly.");
            }
            if (node.m_nextTableauNode==null) {
                if (m_lastTableauNode!=node)
                    throw new IllegalStateException("Last tableau node is pointing wrongly.");
            }
            else {
                if (node.m_nextTableauNode.m_previousTableauNode!=node)
                    throw new IllegalStateException("Next tableau node is pointing wrongly.");
            }
            numberOfNodesInTableau++;
            node=node.m_nextTableauNode;
        }
        if (numberOfNodesInTableau!=m_numberOfNodesInTableau)
            throw new IllegalStateException("Invalid number of nodes in the tableau.");
    }
}
