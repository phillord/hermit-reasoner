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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataValueEnumeration;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NegatedAtomicRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.tableau.Node.NodeState;

/**
 * This class coordinates the main tableau expansion for a given DLOntology
 * (a normalized and clausified ontology). It represents the state of a run
 * on a set of clauses and coordinates the extension of the ABox and also the
 * retraction of facts when backtracking. Before starting the expansion,
 * the given clauses are (for better performance) preprocessed via the
 * HyperresolutionManager into a compiled and executable form.
 */
public final class Tableau implements Serializable {
    private static final long serialVersionUID=-28982363158925221L;

    protected final InterruptFlag m_interruptFlag;
    protected final Map<String,Object> m_parameters;
    protected final TableauMonitor m_tableauMonitor;
    protected final ExistentialExpansionStrategy m_existentialExpansionStrategy;
    protected final DLOntology m_dlOntology;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final GroundDisjunctionHeaderManager m_groundDisjunctionHeaderManager;
    protected final ExtensionManager m_extensionManager;
    protected final ClashManager m_clashManager;
    protected final HyperresolutionManager m_hyperresolutionManager;
    protected final MergingManager m_mergingManager;
    protected final ExistentialExpansionManager m_existentialExpasionManager;
    protected final NominalIntroductionManager m_nominalIntroductionManager;
    protected final DescriptionGraphManager m_descriptionGraphManager;
    protected final DatatypeManager m_datatypeManager;
    protected final boolean m_needsThingExtension;
    protected final boolean m_needsNamedExtension;
    protected final List<List<ExistentialConcept>> m_existentialConceptsBuffers;
    protected final boolean m_checkDatatypes;
    protected final boolean m_useDisjunctionLearning;
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
    protected Node m_checkedNode0;
    protected Node m_checkedNode1;

    public Tableau(InterruptFlag interruptFlag,TableauMonitor tableauMonitor,ExistentialExpansionStrategy existentialsExpansionStrategy,boolean useDisjunctionLearning,DLOntology dlOntology,Map<String,Object> parameters) {
        m_interruptFlag=interruptFlag;
        m_interruptFlag.startTask();
        m_parameters=parameters;
        m_tableauMonitor=tableauMonitor;
        m_existentialExpansionStrategy=existentialsExpansionStrategy;
        m_dlOntology=dlOntology;
        m_dependencySetFactory=new DependencySetFactory();
        m_groundDisjunctionHeaderManager=new GroundDisjunctionHeaderManager();
        m_extensionManager=new ExtensionManager(this);
        m_clashManager=new ClashManager(this);
        m_hyperresolutionManager=new HyperresolutionManager(this);
        m_mergingManager=new MergingManager(this);
        m_existentialExpasionManager=new ExistentialExpansionManager(this);
        m_nominalIntroductionManager=new NominalIntroductionManager(this);
        m_descriptionGraphManager=new DescriptionGraphManager(this);
        m_datatypeManager=new DatatypeManager(this);
        m_existentialExpansionStrategy.initialize(this);
        m_needsThingExtension=m_hyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.THING);
        m_needsNamedExtension=m_hyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.INTERNAL_NAMED);
        m_existentialConceptsBuffers=new ArrayList<List<ExistentialConcept>>();
        m_checkDatatypes=m_dlOntology.hasDatatypes();
        m_useDisjunctionLearning=useDisjunctionLearning;
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        m_nonbacktrackableBranchingPoint=-1;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.setTableau(this);
        m_interruptFlag.endTask();
    }
    public InterruptFlag getInterruptFlag() {
        return m_interruptFlag;
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
    public ExistentialExpansionStrategy getExistentialsExpansionStrategy() {
        return m_existentialExpansionStrategy;
    }
    public boolean isDeterministic() {
        return m_dlOntology.isHorn() && m_existentialExpansionStrategy.isDeterministic();
    }
    public DependencySetFactory getDependencySetFactory() {
        return m_dependencySetFactory;
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
        m_checkedNode0=null;
        m_checkedNode1=null;
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        m_nonbacktrackableBranchingPoint=-1;
        m_dependencySetFactory.clear();
        m_extensionManager.clear();
        m_clashManager.clear();
        m_hyperresolutionManager.clear();
        m_mergingManager.clear();
        m_existentialExpasionManager.clear();
        m_nominalIntroductionManager.clear();
        m_descriptionGraphManager.clear();
        m_isCurrentModelDeterministic=true;
        m_existentialExpansionStrategy.clear();
        m_datatypeManager.clear();
        m_existentialConceptsBuffers.clear();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.tableauCleared();
    }
    public boolean isSatisfiable() {
        m_interruptFlag.startTimedTask();
        try {
            boolean existentialsAreExact=m_existentialExpansionStrategy.isExact();
            if (m_tableauMonitor!=null)
                m_tableauMonitor.saturateStarted();
            boolean hasMoreWork=true;
            while (hasMoreWork) {
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.iterationStarted();
                hasMoreWork=doIteration();
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.iterationFinished();
                if (!existentialsAreExact && !hasMoreWork && !m_extensionManager.containsClash()) {
                    // no more work to do, but since we use a blocking strategy that does not necessarily
                    // establish only valid blocks (existentialsAreExact == false), we tell the blocking
                    // strategy to go through the nodes and check whether all blocks are valid and if not,
                    // continue with the expansion
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.iterationStarted();
                    hasMoreWork=m_existentialExpansionStrategy.expandExistentials(true); // returns true if some blocks were invalid
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.iterationFinished();
                }
            }
            if (m_tableauMonitor!=null)
                m_tableauMonitor.saturateFinished(!m_extensionManager.containsClash());
            if (!m_extensionManager.containsClash()) {
                m_existentialExpansionStrategy.modelFound();
                return true;
            }
            else
                return false;
        } finally {
            m_interruptFlag.endTask();
        }
    }
    protected boolean doIteration() {
        if (!m_extensionManager.containsClash()) {
            m_nominalIntroductionManager.processAnnotatedEqualities();
            boolean hasChange=false;
            while (m_extensionManager.propagateDeltaNew() && !m_extensionManager.containsClash()) {
                m_descriptionGraphManager.checkGraphConstraints();
                m_hyperresolutionManager.applyDLClauses();
                if (m_checkDatatypes && !m_extensionManager.containsClash())
                    m_datatypeManager.checkDatatypeConstraints();
                if (!m_extensionManager.containsClash())
                    m_nominalIntroductionManager.processAnnotatedEqualities();
                hasChange=true;
            }
            if (hasChange)
                return true;
        }
        if (!m_extensionManager.containsClash())
            if (m_existentialExpansionStrategy.expandExistentials(false))
                return true;
        if (!m_extensionManager.containsClash()) {
            while (m_firstUnprocessedGroundDisjunction!=null) {
                GroundDisjunction groundDisjunction=m_firstUnprocessedGroundDisjunction;
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.processGroundDisjunctionStarted(groundDisjunction);
                m_firstUnprocessedGroundDisjunction=groundDisjunction.m_previousGroundDisjunction;
                if (!groundDisjunction.isPruned() && !groundDisjunction.isSatisfied(this)) {
                    int[] sortedDisjunctIndexes=groundDisjunction.getGroundDisjunctionHeader().getSortedDisjunctIndexes();
                    DependencySet dependencySet=groundDisjunction.getDependencySet();
                    if (groundDisjunction.getNumberOfDisjuncts()>1) {
                        BranchingPoint branchingPoint=new DisjunctionBranchingPoint(this,groundDisjunction,sortedDisjunctIndexes);
                        pushBranchingPoint(branchingPoint);
                        dependencySet=m_dependencySetFactory.addBranchingPoint(dependencySet,branchingPoint.getLevel());
                    }
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.disjunctProcessingStarted(groundDisjunction,sortedDisjunctIndexes[0]);
                    groundDisjunction.addDisjunctToTableau(this,sortedDisjunctIndexes[0],dependencySet);
                    if (m_tableauMonitor!=null) {
                        m_tableauMonitor.disjunctProcessingFinished(groundDisjunction,sortedDisjunctIndexes[0]);
                        m_tableauMonitor.processGroundDisjunctionFinished(groundDisjunction);
                    }
                    return true;
                }
                else {
                    if (m_tableauMonitor!=null)
                        m_tableauMonitor.groundDisjunctionSatisfied(groundDisjunction);
                }
                m_interruptFlag.checkInterrupt();
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
            m_dependencySetFactory.removeUnusedSets();
            return true;
        }
        return false;
    }
    public Node getCheckedNode0() {
        return m_checkedNode0;
    }
    public Node getCheckedNode1() {
        return m_checkedNode1;
    }
    private boolean hasNominals() {
        return m_dlOntology.hasNominals();
    }
    public boolean isSatisfiable(AtomicConcept atomicConcept) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableStarted(atomicConcept);
        clear();
        if (hasNominals())
            loadABox();
        m_checkedNode0=createNewNINode(m_dependencySetFactory.emptySet());
        m_extensionManager.addConceptAssertion(atomicConcept,m_checkedNode0,m_dependencySetFactory.emptySet(),true);
        boolean result=isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableFinished(atomicConcept,result);
        return result;
    }
    public boolean isSubsumedByList(AtomicConcept subconcept,ArrayList<AtomicConcept> superconcepts) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByStarted(subconcept,superconcepts.get(0));
        clear();
        if (hasNominals())
            loadABox();
        m_checkedNode0=createNewNINode(m_dependencySetFactory.emptySet());
        m_extensionManager.addConceptAssertion(subconcept,m_checkedNode0,m_dependencySetFactory.emptySet(),true);
        m_branchingPoints[0]=new BranchingPoint(this);
        m_currentBranchingPoint++;
        m_nonbacktrackableBranchingPoint=m_currentBranchingPoint;
        DependencySet dependencySet=m_dependencySetFactory.addBranchingPoint(m_dependencySetFactory.emptySet(),m_currentBranchingPoint);
        for( int i=0 ; i<superconcepts.size() ; i++ )
        	m_extensionManager.addConceptAssertion(superconcepts.get(i).getNegation(),m_checkedNode0,dependencySet,true);
        boolean result=!isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByFinished(subconcept,superconcepts.get(0),result);
        return result;
    }
    public boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByStarted(subconcept,superconcept);
        clear();
        if (hasNominals())
            loadABox();
        m_checkedNode0=createNewNINode(m_dependencySetFactory.emptySet());
        m_extensionManager.addConceptAssertion(subconcept,m_checkedNode0,m_dependencySetFactory.emptySet(),true);
        m_branchingPoints[0]=new BranchingPoint(this);
        m_currentBranchingPoint++;
        m_nonbacktrackableBranchingPoint=m_currentBranchingPoint;
        DependencySet dependencySet=m_dependencySetFactory.addBranchingPoint(m_dependencySetFactory.emptySet(),m_currentBranchingPoint);
        m_extensionManager.addConceptAssertion(superconcept.getNegation(),m_checkedNode0,dependencySet,true);
        boolean result=!isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSubsumedByFinished(subconcept,superconcept,result);
        return result;
    }
    public boolean isSatisfiable(Role role,boolean isDataRole) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableStarted(role);
        clear();
        if (hasNominals())
            loadABox();
        m_checkedNode0=createNewNINode(m_dependencySetFactory.emptySet());
        if (isDataRole)
            m_checkedNode1=createNewConcreteNode(m_dependencySetFactory.emptySet(),m_checkedNode0);
        else
            m_checkedNode1=createNewNINode(m_dependencySetFactory.emptySet());
        if (role instanceof InverseRole)
            m_extensionManager.addRoleAssertion(((InverseRole)role).getInverseOf(),m_checkedNode1,m_checkedNode0,m_dependencySetFactory.emptySet(),true);
        else
            m_extensionManager.addRoleAssertion(role,m_checkedNode0,m_checkedNode1,m_dependencySetFactory.emptySet(),true);
        boolean result=isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableFinished(role,result);
        return result;
    }
    public boolean isABoxSatisfiable() {
        return isABoxSatisfiable(null,null);
    }
    public boolean isABoxSatisfiable(Individual checkedNode0Name,Individual checkedNode1Name) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isABoxSatisfiableStarted();
        clear();
        Map<Term,Node> termsToNodes=loadABox();
        if (checkedNode0Name==null)
            m_checkedNode0=null;
        else
            m_checkedNode0=getNodeForTerm(termsToNodes,checkedNode0Name);
        if (checkedNode1Name==null)
            m_checkedNode1=null;
        else
            m_checkedNode1=getNodeForTerm(termsToNodes,checkedNode1Name);
        // Ensure that at least one individual exists.
        if (m_firstTableauNode==null)
            createNewNINode(m_dependencySetFactory.emptySet());
        boolean result=isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isABoxSatisfiableFinished(result);
        return result;
    }
    public boolean isInstanceOf(AtomicConcept atomicConcept,Individual individual) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isInstanceOfStarted(atomicConcept,individual);
        clear();
        Map<Term,Node> termsToNodes=loadABox();
        m_checkedNode0=getNodeForTerm(termsToNodes,individual);
        if (m_checkedNode0==null)
            m_checkedNode0=createNewNINode(m_dependencySetFactory.emptySet());
        m_extensionManager.addConceptAssertion(atomicConcept.getNegation(),m_checkedNode0,m_dependencySetFactory.emptySet(),true);
        boolean result=!isSatisfiable();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isInstanceOfFinished(atomicConcept,individual,result);
        return result;
    }
    protected Map<Term,Node> loadABox() {
        Map<Term,Node> termsToNodes=new HashMap<Term,Node>();
        for (Atom atom : m_dlOntology.getPositiveFacts()) {
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (dlPredicate instanceof LiteralConcept)
                m_extensionManager.addConceptAssertion((LiteralConcept)dlPredicate,getNodeForTerm(termsToNodes,atom.getArgument(0)),m_dependencySetFactory.emptySet(),true);
            else if (dlPredicate instanceof AtomicRole || Equality.INSTANCE.equals(dlPredicate) || Inequality.INSTANCE.equals(dlPredicate))
                m_extensionManager.addAssertion(dlPredicate,getNodeForTerm(termsToNodes,atom.getArgument(0)),getNodeForTerm(termsToNodes,atom.getArgument(1)),m_dependencySetFactory.emptySet(),true);
            else
                throw new IllegalArgumentException("Unsupported type of positive ground atom.");
        }
        Object[] ternaryTuple=new Object[3];
        for (Atom atom : m_dlOntology.getNegativeFacts()) {
            DLPredicate dlPredicate=atom.getDLPredicate();
            if (dlPredicate instanceof LiteralConcept)
                m_extensionManager.addConceptAssertion(((LiteralConcept)dlPredicate).getNegation(),getNodeForTerm(termsToNodes,atom.getArgument(0)),m_dependencySetFactory.emptySet(),true);
            else if (dlPredicate instanceof AtomicRole) {
                ternaryTuple[0]=NegatedAtomicRole.create((AtomicRole)dlPredicate);
                ternaryTuple[1]=getNodeForTerm(termsToNodes,atom.getArgument(0));
                ternaryTuple[2]=getNodeForTerm(termsToNodes,atom.getArgument(1));
                m_extensionManager.addTuple(ternaryTuple,m_dependencySetFactory.emptySet(),true);
            }
            else if (Equality.INSTANCE.equals(dlPredicate))
                m_extensionManager.addAssertion(Inequality.INSTANCE,getNodeForTerm(termsToNodes,atom.getArgument(0)),getNodeForTerm(termsToNodes,atom.getArgument(1)),m_dependencySetFactory.emptySet(),true);
            else if (Inequality.INSTANCE.equals(dlPredicate))
                m_extensionManager.addAssertion(Equality.INSTANCE,getNodeForTerm(termsToNodes,atom.getArgument(0)),getNodeForTerm(termsToNodes,atom.getArgument(1)),m_dependencySetFactory.emptySet(),true);
            else
                throw new IllegalArgumentException("Unsupported type of negative ground atom.");
        }
        return termsToNodes;
    }
    protected Node getNodeForTerm(Map<Term,Node> termsToNodes,Term term) {
        Node node=termsToNodes.get(term);
        if (node==null) {
            if (term instanceof Individual)
                node=createNewNamedNode(m_dependencySetFactory.emptySet());
            else {
                Constant constant=(Constant)term;
                node=createNewRootConstantNode(m_dependencySetFactory.emptySet());
                Object dataValue=constant.getDataValue();
                // Anonymous constant values are not assigned a particular value.
                // See the hack in OWLClausification for an explanation.
                if (!(dataValue instanceof Constant.AnonymousConstantValue))
                    m_extensionManager.addAssertion(DataValueEnumeration.create(new Object[] { dataValue }),node,m_dependencySetFactory.emptySet(),true);
            }
            termsToNodes.put(term,node);
        }
        return node.getCanonicalNode();
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
    /**
     * Add a branching point in case we need to backtrack to this state.
     *
     * @param branchingPoint
     */
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
        m_existentialExpansionStrategy.branchingPointPushed();
        m_nominalIntroductionManager.branchingPointPushed();
        m_isCurrentModelDeterministic=false;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.pushBranchingPointFinished(branchingPoint);
    }
    /**
     * Backtrack to a certain branching point in the list of branching points that have been set during the run.
     *
     * @param newCurrentBrancingPoint
     */
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
        m_existentialExpansionStrategy.backtrack();
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
    /**
     * Create a new node that represents an individual named in the input ontology (thus, keys have to be applied to it)
     *
     * @param dependencySet
     *            the dependency set for the node
     * @return the created node
     */
    public Node createNewNamedNode(DependencySet dependencySet) {
        return createNewNodeRaw(dependencySet,null,NodeType.NAMED_NODE,0);
    }
    /**
     * Create a new node that represents a nominal, but one that is not named in the input ontology (thus, keys are not applicable)
     *
     * @param dependencySet
     *            the dependency set for the node
     * @return the created node
     */
    public Node createNewNINode(DependencySet dependencySet) {
        return createNewNodeRaw(dependencySet,null,NodeType.NI_NODE,0);
    }
    /**
     * Create a new tree node.
     *
     * @param dependencySet
     *            the dependency set for the node
     * @param parent
     *            the parent of the node that is to be created
     * @return the created node
     */
    public Node createNewTreeNode(DependencySet dependencySet,Node parent) {
        return createNewNodeRaw(dependencySet,parent,NodeType.TREE_NODE,parent.getTreeDepth()+1);
    }
    /**
     * Create a new concrete node for datatypes.
     *
     * @param dependencySet
     *            the dependency set for the node
     * @param parent
     *            the parent of the node that is to be created
     * @return the created node
     */
    public Node createNewConcreteNode(DependencySet dependencySet,Node parent) {
        return createNewNodeRaw(dependencySet,parent,NodeType.CONCRETE_NODE,parent.getTreeDepth()+1);
    }
    /**
     * Create a new root constant node for datatypes.
     *
     * @param dependencySet
     *            the dependency set for the node
     * @return the created node
     */
    public Node createNewRootConstantNode(DependencySet dependencySet) {
        return createNewNodeRaw(dependencySet,null,NodeType.ROOT_CONSTANT_NODE,0);
    }
    /**
     * Create a new node graph node for description graphs
     *
     * @param parent
     *            the parent of the node that is to be created (may be null)
     * @param dependencySet
     *            the dependency set for the node
     * @return the created node
     */
    public Node createNewGraphNode(Node parent,DependencySet dependencySet) {
        return createNewNodeRaw(dependencySet,parent,NodeType.GRAPH_NODE,parent==null ? 0 : parent.getTreeDepth());
    }
    protected Node createNewNodeRaw(DependencySet dependencySet,Node parent,NodeType nodeType,int treeDepth) {
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
        m_existentialExpansionStrategy.nodeInitialized(node);
        node.m_previousTableauNode=m_lastTableauNode;
        if (m_lastTableauNode==null)
            m_firstTableauNode=node;
        else
            m_lastTableauNode.m_nextTableauNode=node;
        m_lastTableauNode=node;
        m_existentialExpansionStrategy.nodeStatusChanged(node);
        m_numberOfNodeCreations++;
        if (m_tableauMonitor!=null)
            m_tableauMonitor.nodeCreated(node);
        if (nodeType!=NodeType.CONCRETE_NODE) {
            m_extensionManager.addConceptAssertion(AtomicConcept.THING,node,dependencySet,true);
            if (nodeType==NodeType.NAMED_NODE && m_needsNamedExtension)
                m_extensionManager.addConceptAssertion(AtomicConcept.INTERNAL_NAMED,node,dependencySet,true);
        }
        return node;
    }
    /**
     * Merges node into mergeInto. We assume that concepts and roles have already been copied from node to mergeInto. After the merge node has state NodeState.MERGED.
     *
     * @param node
     *            the node that is to be merged
     * @param mergeInto
     *            the node we merge into
     * @param dependencySet
     */
    public void mergeNode(Node node,Node mergeInto,DependencySet dependencySet) {
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        node.m_mergedInto=mergeInto;
        node.m_mergedIntoDependencySet=m_dependencySetFactory.getPermanent(dependencySet);
        m_dependencySetFactory.addUsage(node.m_mergedIntoDependencySet);
        node.m_nodeState=NodeState.MERGED;
        node.m_previousMergedOrPrunedNode=m_lastMergedOrPrunedNode;
        m_lastMergedOrPrunedNode=node;
        m_numberOfMergedOrPrunedNodes++;
        m_existentialExpansionStrategy.nodeStatusChanged(node);
        m_existentialExpansionStrategy.nodesMerged(node,mergeInto);
    }
    public void pruneNode(Node node) {
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        node.m_nodeState=NodeState.PRUNED;
        node.m_previousMergedOrPrunedNode=m_lastMergedOrPrunedNode;
        m_lastMergedOrPrunedNode=node;
        m_numberOfMergedOrPrunedNodes++;
        m_existentialExpansionStrategy.nodeStatusChanged(node);
    }
    protected void backtrackLastMergedOrPrunedNode() {
        Node node=m_lastMergedOrPrunedNode;
        assert (node.m_nodeState==Node.NodeState.MERGED && node.m_mergedInto!=null && node.m_mergedInto!=null) || (node.m_nodeState==Node.NodeState.PRUNED && node.m_mergedInto==null && node.m_mergedInto==null);
        Node savedMergedInfo=null;
        if (node.m_nodeState==Node.NodeState.MERGED) {
            m_dependencySetFactory.removeUsage(node.m_mergedIntoDependencySet);
            savedMergedInfo=node.m_mergedInto;
            node.m_mergedInto=null;
            node.m_mergedIntoDependencySet=null;
        }
        node.m_nodeState=Node.NodeState.ACTIVE;
        m_lastMergedOrPrunedNode=node.m_previousMergedOrPrunedNode;
        node.m_previousMergedOrPrunedNode=null;
        m_numberOfMergedOrPrunedNodes--;
        m_existentialExpansionStrategy.nodeStatusChanged(node);
        if (savedMergedInfo!=null)
            m_existentialExpansionStrategy.nodesUnmerged(node,savedMergedInfo);
    }
    protected void destroyLastTableauNode() {
        Node node=m_lastTableauNode;
        assert node.m_nodeState==Node.NodeState.ACTIVE;
        assert node.m_mergedInto==null;
        assert node.m_mergedIntoDependencySet==null;
        assert node.m_previousMergedOrPrunedNode==null;
        m_existentialExpansionStrategy.nodeDestroyed(node);
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
    public void putExistentialConceptsBuffer(List<ExistentialConcept> buffer) {
        assert buffer.isEmpty();
        m_existentialConceptsBuffers.add(buffer);
    }
    public void checkTableauList() {
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

    protected static class InterruptTimer extends Thread {
        protected final int m_timeout;
        protected final Tableau m_tableau;
        protected boolean m_timingStopped;

        public InterruptTimer(int timeout,Tableau tableau) {
            super("HermiT Interrupt Current Task Thread");
            setDaemon(true);
            m_timeout=timeout;
            m_tableau=tableau;
            m_timingStopped=false;
        }
        public synchronized void run() {
            try {
                if (!m_timingStopped) {
                    wait(m_timeout);
                    if (!m_timingStopped)
                        m_tableau.getInterruptFlag().interruptCurrentTask();
                }
            } catch (InterruptedException stopped) {
            }
        }
        public synchronized void stopTiming() {
            m_timingStopped=true;
            notifyAll();
        }
    }
}
