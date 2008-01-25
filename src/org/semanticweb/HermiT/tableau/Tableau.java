package org.semanticweb.HermiT.tableau;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.monitor.*;

public final class Tableau implements Serializable {
    private static final long serialVersionUID=-28982363158925221L;

    protected final TableauMonitor m_tableauMonitor;
    protected final ExistentialsExpansionStrategy m_existentialsExpansionStrategy;
    protected final DLOntology m_dlOntology;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final SetFactory<Concept> m_conceptSetFactory;
    protected final SetFactory<ExistentialConcept> m_existentialConceptSetFactory;
    protected final SetFactory<AtomicAbstractRole> m_atomicAbstractRoleSetFactory;
    protected final ExtensionManager m_extensionManager;
    protected final HyperresolutionManager m_hyperresolutionManager;
    protected final MergingManager m_mergingManager;
    protected final ExistentialExpansionManager m_existentialExpasionManager;
    protected final NominalIntroductionManager m_nominalIntroductionManager;
    protected final DescriptionGraphManager m_descriptionGraphManager;
    protected final boolean m_needsThingExtension;
    protected BranchingPoint[] m_branchingPoints;
    protected int m_currentBranchingPoint;
    protected boolean m_isCurrentModelDeterministic;
    protected Node m_firstTableauNode;
    protected Node m_lastTableauNode;
    protected Node m_firstFreeNode;
    protected int m_numberOfNodesInTableau;
    protected int m_numberOfMergedOrPrunedNodes;
    protected Node m_lastChangedNode;
    protected Node.NodeEvent m_lastChangedNodeEvent;
    protected GroundDisjunction m_firstGroundDisjunction;
    protected GroundDisjunction m_firstUnprocessedGroundDisjunction;
    protected int m_lastNodeID;
    protected int m_allocatedNodes;
    protected int m_lastOrderPosition;
    protected Node m_checkedNode;

    public Tableau(TableauMonitor tableauMonitor,ExistentialsExpansionStrategy existentialsExpansionStrategy,DLOntology dlOntology) {
        m_tableauMonitor=tableauMonitor;
        m_existentialsExpansionStrategy=existentialsExpansionStrategy;
        m_dlOntology=dlOntology;
        m_dependencySetFactory=new DependencySetFactory();
        m_conceptSetFactory=new SetFactory<Concept>();
        m_existentialConceptSetFactory=new SetFactory<ExistentialConcept>();
        m_atomicAbstractRoleSetFactory=new SetFactory<AtomicAbstractRole>();
        m_extensionManager=new ExtensionManager(this);
        m_hyperresolutionManager=new HyperresolutionManager(this);
        m_mergingManager=new MergingManager(this);
        m_existentialExpasionManager=new ExistentialExpansionManager(this);
        m_nominalIntroductionManager=new NominalIntroductionManager(this);
        m_descriptionGraphManager=new DescriptionGraphManager(this);
        m_existentialsExpansionStrategy.intialize(this);
        m_needsThingExtension=m_hyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.THING);
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.setTableau(this);
    }
    public DLOntology getDLOntology() {
        return m_dlOntology;
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
    public SetFactory<Concept> getConceptSetFactory() {
        return m_conceptSetFactory;
    }
    public SetFactory<ExistentialConcept> getExistentialConceptSetFactory() {
        return m_existentialConceptSetFactory;
    }
    public SetFactory<AtomicAbstractRole> getAtomicAbstractRoleSetFactory() {
        return m_atomicAbstractRoleSetFactory;
    }
    public ExtensionManager getExtensionManager() {
        return m_extensionManager;
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
        m_firstFreeNode=null;
        m_firstTableauNode=null;
        m_lastTableauNode=null;
        m_numberOfNodesInTableau=0;
        m_numberOfMergedOrPrunedNodes=0;
        m_lastChangedNode=null;
        m_lastChangedNodeEvent=null;
        m_firstGroundDisjunction=null;
        m_firstUnprocessedGroundDisjunction=null;
        m_lastNodeID=0;
        m_allocatedNodes=0;
        m_lastOrderPosition=0;
        m_checkedNode=null;
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1; // The constructor of BranchingPoint depends on this value  
        m_branchingPoints[0]=new BranchingPoint(this);
        m_currentBranchingPoint++;
        m_dependencySetFactory.clear();
        m_extensionManager.clear();
        m_mergingManager.clear();
        m_nominalIntroductionManager.clear();
        m_descriptionGraphManager.clear();
        m_isCurrentModelDeterministic=true;
        m_existentialsExpansionStrategy.clear();
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
        m_existentialsExpansionStrategy.modelFound();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.saturateFinished();
        return !m_extensionManager.containsClash();
    }
    protected boolean doIteration() {
        if (!m_extensionManager.containsClash())
            if (m_descriptionGraphManager.checkGraphConstraints())
                return true;
        if (!m_extensionManager.containsClash())
            if (m_hyperresolutionManager.evaluate())
                return true;
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
            if (newCurrentBranchingPoint==-1)
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
        m_extensionManager.addConceptAssertion(AtomicNegationConcept.create(superconcept),m_checkedNode,m_dependencySetFactory.emptySet());
        boolean result=!isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByFinished(subconcept,superconcept,result);
        return result;
    }
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
    public BranchingPoint getCurrentBranchingPoint() {
        return m_branchingPoints[m_currentBranchingPoint];
    }
    public Node getFirstTableauNode() {
        return m_firstTableauNode;
    }
    public Node getLastTableauNode() {
        return m_lastTableauNode;
    }
    public int getNumberOfNodesInTableau() {
        return m_numberOfNodesInTableau;
    }
    public int getNumberOfMergedOrPrunedNodes() {
        return m_numberOfMergedOrPrunedNodes;
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
        // backtrack node changes
        Node lastChangedNodeShouldBe=branchingPoint.m_lastChangedNode;
        Node.NodeEvent lastChangedNodeEventShouldBe=branchingPoint.m_lastChangedNodeEvent;
        while (lastChangedNodeShouldBe!=m_lastChangedNode || lastChangedNodeEventShouldBe!=m_lastChangedNodeEvent)
            m_lastChangedNode.backtrackNodeChange();
        // finish 
        m_extensionManager.clearClash();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.backtrackToFinished(branchingPoint);
    }
    public Node createNewRootNode(DependencySet dependencySet,int treeDepth) {
        Node node=createNewNodeRaw(null,NodeType.ROOT_NODE,treeDepth);
        insertIntoTableau(node,dependencySet);
        return node;
    }
    public Node createNewTreeNode(Node parent,DependencySet dependencySet) {
        Node node=createNewNodeRaw(parent,NodeType.TREE_NODE,parent.getTreeDepth()+1);
        insertIntoTableau(node,dependencySet);
        return node;
    }
    public Node createNewGraphNode(Node parent,DependencySet dependencySet) {
        Node node=createNewNodeRaw(parent,NodeType.GRAPH_NODE,parent.getTreeDepth());
        insertIntoTableau(node,dependencySet);
        return node;
    }
    public Node createNewNodeRaw(Node parent,NodeType nodeType,int treeDepth) {
        Node node;
        if (m_firstFreeNode==null) {
            node=new Node(this);
            m_allocatedNodes++;
        }
        else {
            node=m_firstFreeNode;
            m_firstFreeNode=m_firstFreeNode.m_nextTableauNode;
        }
        node.initialize(++m_lastNodeID,parent,nodeType,treeDepth);
        return node;
    }
    public void insertIntoTableau(Node node,DependencySet dependencySet) {
        node.insertIntoTableau();
        m_extensionManager.addConceptAssertion(AtomicConcept.THING,node,dependencySet);
    }
    public int getNumberOfCreatedNodes() {
        return m_lastNodeID;
    }
    public int getNumberOfAllocatedNodes() {
        return m_allocatedNodes;
    }
    public Node getNode(int nodeID) {
        Node node=m_lastChangedNode;
        Node.NodeEvent nodeEvent=m_lastChangedNodeEvent;
        while (node!=null) {
            if (node.getNodeID()==nodeID)
                return node;
            Node previousNode;
            Node.NodeEvent previousNodeEvent;
            if (nodeEvent==Node.NodeEvent.INSERTED_INTO_TALBEAU) {
                previousNode=node.m_previousChangedNodeForInsert;
                previousNodeEvent=node.m_previousChangedNodeEventForInsert;
            }
            else {
                previousNode=node.m_previousChangedNodeForRemove;
                previousNodeEvent=node.m_previousChangedNodeEventForRemove;
            }
            node=previousNode;
            nodeEvent=previousNodeEvent;
        }
        return null;
    }
    protected void checkTableauList() {
        Node node=m_firstTableauNode;
        int numberOfNodesInTableau=0;
        while (node!=null) {
            if (!node.isInTableau())
                throw new IllegalStateException("A node is encountered in the node list which is not in the tableau.");
            if (node.m_previousTableauNode==null) {
                if (m_firstTableauNode!=node)
                    throw new IllegalStateException("First tableau node is point wrongly.");
            }
            else {
                if (node.m_previousTableauNode.m_nextTableauNode!=node)
                    throw new IllegalStateException("Previous tableau node is pointing wrongly.");
            }
            if (node.m_nextTableauNode==null) {
                if (m_lastTableauNode!=node)
                    throw new IllegalStateException("Last tableau node is point wrongly.");
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
