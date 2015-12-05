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
import java.util.Set;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.NegatedAtomicRole;
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
    protected final DLOntology m_permanentDLOntology;
    protected DLOntology m_additionalDLOntology;
    protected final DependencySetFactory m_dependencySetFactory;
    protected final ExtensionManager m_extensionManager;
    protected final ClashManager m_clashManager;
    protected final HyperresolutionManager m_permanentHyperresolutionManager;
    protected HyperresolutionManager m_additionalHyperresolutionManager;
    protected final MergingManager m_mergingManager;
    protected final ExistentialExpansionManager m_existentialExpasionManager;
    protected final NominalIntroductionManager m_nominalIntroductionManager;
    protected final DescriptionGraphManager m_descriptionGraphManager;
    protected final DatatypeManager m_datatypeManager;
    protected final List<List<ExistentialConcept>> m_existentialConceptsBuffers;
    protected final boolean m_useDisjunctionLearning;
    protected final boolean m_hasDescriptionGraphs;
    protected BranchingPoint[] m_branchingPoints;
    protected int m_currentBranchingPoint;
    protected int m_nonbacktrackableBranchingPoint;
    protected boolean m_isCurrentModelDeterministic;
    protected boolean m_needsThingExtension;
    protected boolean m_needsNamedExtension;
    protected boolean m_needsRDFSLiteralExtension;
    protected boolean m_checkDatatypes;
    protected boolean m_checkUnknownDatatypeRestrictions;
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

    public Tableau(InterruptFlag interruptFlag,TableauMonitor tableauMonitor,ExistentialExpansionStrategy existentialsExpansionStrategy,boolean useDisjunctionLearning,DLOntology permanentDLOntology,DLOntology additionalDLOntology,Map<String,Object> parameters) {
        if (additionalDLOntology!=null && !additionalDLOntology.getAllDescriptionGraphs().isEmpty())
            throw new IllegalArgumentException("Additional ontology cannot contain description graphs.");
        m_interruptFlag=interruptFlag;
        m_interruptFlag.startTask();
        try {
            m_parameters=parameters;
            m_tableauMonitor=tableauMonitor;
            m_existentialExpansionStrategy=existentialsExpansionStrategy;
            m_permanentDLOntology=permanentDLOntology;
            m_additionalDLOntology=additionalDLOntology;
            m_dependencySetFactory=new DependencySetFactory();
            m_extensionManager=new ExtensionManager(this);
            m_clashManager=new ClashManager(this);
            m_permanentHyperresolutionManager=new HyperresolutionManager(this,m_permanentDLOntology.getDLClauses());
            if (m_additionalDLOntology!=null)
                m_additionalHyperresolutionManager=new HyperresolutionManager(this,m_additionalDLOntology.getDLClauses());
            else
                m_additionalHyperresolutionManager=null;
            m_mergingManager=new MergingManager(this);
            m_existentialExpasionManager=new ExistentialExpansionManager(this);
            m_nominalIntroductionManager=new NominalIntroductionManager(this);
            m_descriptionGraphManager=new DescriptionGraphManager(this);
            m_datatypeManager=new DatatypeManager(this);
            m_existentialExpansionStrategy.initialize(this);
            m_existentialConceptsBuffers=new ArrayList<List<ExistentialConcept>>();
            m_useDisjunctionLearning=useDisjunctionLearning;
            m_hasDescriptionGraphs=!m_permanentDLOntology.getAllDescriptionGraphs().isEmpty();
            m_branchingPoints=new BranchingPoint[2];
            m_currentBranchingPoint=-1;
            m_nonbacktrackableBranchingPoint=-1;
            updateFlagsDependentOnAdditionalOntology();
            if (m_tableauMonitor!=null)
                m_tableauMonitor.setTableau(this);
        }
        finally {
            m_interruptFlag.endTask();
        }
    }
    public InterruptFlag getInterruptFlag() {
        return m_interruptFlag;
    }
    public DLOntology getPermanentDLOntology() {
        return m_permanentDLOntology;
    }
    public DLOntology getAdditionalDLOntology() {
        return m_additionalDLOntology;
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
        return m_permanentDLOntology.isHorn() && (m_additionalDLOntology==null || m_additionalDLOntology.isHorn()) && m_existentialExpansionStrategy.isDeterministic();
    }
    public DependencySetFactory getDependencySetFactory() {
        return m_dependencySetFactory;
    }
    public ExtensionManager getExtensionManager() {
        return m_extensionManager;
    }
    public HyperresolutionManager getPermanentHyperresolutionManager() {
        return m_permanentHyperresolutionManager;
    }
    public HyperresolutionManager getAdditionalHyperresolutionManager() {
        return m_additionalHyperresolutionManager;
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
        m_branchingPoints=new BranchingPoint[2];
        m_currentBranchingPoint=-1;
        m_nonbacktrackableBranchingPoint=-1;
        m_dependencySetFactory.clear();
        m_extensionManager.clear();
        m_clashManager.clear();
        m_permanentHyperresolutionManager.clear();
        if (m_additionalHyperresolutionManager!=null)
            m_additionalHyperresolutionManager.clear();
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
    public boolean supportsAdditionalDLOntology(DLOntology additionalDLOntology) {
        boolean hasInverseRoles=(m_permanentDLOntology.hasInverseRoles() || (m_additionalDLOntology!=null && m_additionalDLOntology.hasInverseRoles()));
        boolean hasNominals=(m_permanentDLOntology.hasNominals() || (m_additionalDLOntology!=null && m_additionalDLOntology.hasNominals()));
        boolean isHorn=(m_permanentDLOntology.isHorn() || (m_additionalDLOntology!=null && m_additionalDLOntology.isHorn()));
        boolean permanentHasBottomObjectProperty=m_permanentDLOntology.containsObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE);
        boolean hasBottomObjectProperty=(permanentHasBottomObjectProperty || (m_additionalDLOntology!=null && m_additionalDLOntology.containsObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE)));
        if (!additionalDLOntology.getAllDescriptionGraphs().isEmpty() || (additionalDLOntology.hasInverseRoles() && !hasInverseRoles) || (additionalDLOntology.hasNominals() && !hasNominals) || (!additionalDLOntology.isHorn() && isHorn) || (hasBottomObjectProperty && !permanentHasBottomObjectProperty))
            return false;
        for (DLClause dlClause : additionalDLOntology.getDLClauses())
            if (dlClause.isAtomicRoleInclusion() || dlClause.isAtomicRoleInverseInclusion() || dlClause.isFunctionalityAxiom() || dlClause.isInverseFunctionalityAxiom())
                return false;
        return true;
    }
    public void setAdditionalDLOntology(DLOntology additionalDLOntology) {
        if (!supportsAdditionalDLOntology(additionalDLOntology))
            throw new IllegalArgumentException("Additional DL-ontology contains features that are incompatible with this tableau.");
        m_additionalDLOntology=additionalDLOntology;
        m_additionalHyperresolutionManager=new HyperresolutionManager(this,m_additionalDLOntology.getDLClauses());
        m_existentialExpansionStrategy.additionalDLOntologySet(m_additionalDLOntology);
        m_datatypeManager.additionalDLOntologySet(m_additionalDLOntology);
        updateFlagsDependentOnAdditionalOntology();
    }
    public void clearAdditionalDLOntology() {
        m_additionalDLOntology=null;
        m_additionalHyperresolutionManager=null;
        m_existentialExpansionStrategy.additionalDLOntologyCleared();
        m_datatypeManager.additionalDLOntologyCleared();
        updateFlagsDependentOnAdditionalOntology();
    }
    protected void updateFlagsDependentOnAdditionalOntology() {
        m_needsThingExtension=m_permanentHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.THING);
        m_needsNamedExtension=m_permanentHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.INTERNAL_NAMED);
        m_needsRDFSLiteralExtension=m_permanentHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(InternalDatatype.RDFS_LITERAL);
        m_checkDatatypes=m_permanentDLOntology.hasDatatypes();
        m_checkUnknownDatatypeRestrictions=m_permanentDLOntology.hasUnknownDatatypeRestrictions();
        if (m_additionalHyperresolutionManager!=null) {
            m_needsThingExtension|=m_additionalHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.THING);
            m_needsNamedExtension|=m_additionalHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(AtomicConcept.INTERNAL_NAMED);
            m_needsRDFSLiteralExtension|=m_additionalHyperresolutionManager.m_tupleConsumersByDeltaPredicate.containsKey(InternalDatatype.RDFS_LITERAL);
        }
        if (m_additionalDLOntology!=null) {
            m_checkDatatypes|=m_additionalDLOntology.hasDatatypes();
            m_checkUnknownDatatypeRestrictions|=m_additionalDLOntology.hasUnknownDatatypeRestrictions();
        }
    }
    public boolean isSatisfiable(boolean loadAdditionalABox,Set<Atom> perTestPositiveFactsNoDependency,Set<Atom> perTestNegativeFactsNoDependency,Set<Atom> perTestPositiveFactsDummyDependency,Set<Atom> perTestNegativeFactsDummyDependency,Map<Individual,Node> nodesForIndividuals,ReasoningTaskDescription reasoningTaskDescription) {
        boolean loadPermanentABox=m_permanentDLOntology.hasNominals() || (m_additionalDLOntology!=null && m_additionalDLOntology.hasNominals());
        return isSatisfiable(loadPermanentABox,loadAdditionalABox,perTestPositiveFactsNoDependency,perTestNegativeFactsNoDependency,perTestPositiveFactsDummyDependency,perTestNegativeFactsDummyDependency,new HashMap<Term,Node>(),nodesForIndividuals,reasoningTaskDescription);
    }
    public boolean isSatisfiable(boolean loadPermanentABox,boolean loadAdditionalABox,Set<Atom> perTestPositiveFactsNoDependency,Set<Atom> perTestNegativeFactsNoDependency,Set<Atom> perTestPositiveFactsDummyDependency,Set<Atom> perTestNegativeFactsDummyDependency,Map<Individual,Node> nodesForIndividuals,ReasoningTaskDescription reasoningTaskDescription) {
        return isSatisfiable(loadPermanentABox,loadAdditionalABox,perTestPositiveFactsNoDependency,perTestNegativeFactsNoDependency,perTestPositiveFactsDummyDependency,perTestNegativeFactsDummyDependency,new HashMap<Term,Node>(),nodesForIndividuals,reasoningTaskDescription);
    }
    public boolean isSatisfiable(boolean loadPermanentABox,boolean loadAdditionalABox,Set<Atom> perTestPositiveFactsNoDependency,Set<Atom> perTestNegativeFactsNoDependency,Set<Atom> perTestPositiveFactsDummyDependency,Set<Atom> perTestNegativeFactsDummyDependency,Map<Term,Node> termsToNodes,Map<Individual,Node> nodesForIndividuals,ReasoningTaskDescription reasoningTaskDescription) {
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableStarted(reasoningTaskDescription);
        clear();
        if (loadPermanentABox) {
            for (Atom atom : m_permanentDLOntology.getPositiveFacts())
                loadPositiveFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
            for (Atom atom : m_permanentDLOntology.getNegativeFacts())
                loadNegativeFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
        }
        if (loadAdditionalABox && m_additionalDLOntology!=null) {
            for (Atom atom : m_additionalDLOntology.getPositiveFacts())
                loadPositiveFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
            for (Atom atom : m_additionalDLOntology.getNegativeFacts())
                loadNegativeFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
        }
        if (perTestPositiveFactsNoDependency!=null && !perTestPositiveFactsNoDependency.isEmpty())
            for (Atom atom : perTestPositiveFactsNoDependency)
                loadPositiveFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
        if (perTestNegativeFactsNoDependency!=null && !perTestNegativeFactsNoDependency.isEmpty())
            for (Atom atom : perTestNegativeFactsNoDependency)
                loadNegativeFact(termsToNodes,atom,m_dependencySetFactory.emptySet());
        if ((perTestPositiveFactsDummyDependency!=null && !perTestPositiveFactsDummyDependency.isEmpty()) || (perTestNegativeFactsDummyDependency!=null && !perTestNegativeFactsDummyDependency.isEmpty())) {
            m_branchingPoints[0]=new BranchingPoint(this);
            m_currentBranchingPoint++;
            m_nonbacktrackableBranchingPoint=m_currentBranchingPoint;
            DependencySet dependencySet=m_dependencySetFactory.addBranchingPoint(m_dependencySetFactory.emptySet(),m_currentBranchingPoint);
            if (perTestPositiveFactsDummyDependency!=null && !perTestPositiveFactsDummyDependency.isEmpty())
                for (Atom atom : perTestPositiveFactsDummyDependency)
                    loadPositiveFact(termsToNodes,atom,dependencySet);
            if (perTestNegativeFactsDummyDependency!=null && !perTestNegativeFactsDummyDependency.isEmpty())
                for (Atom atom : perTestNegativeFactsDummyDependency)
                    loadNegativeFact(termsToNodes,atom,dependencySet);
        }
        if (nodesForIndividuals!=null)
            for (Map.Entry<Individual,Node> entry : nodesForIndividuals.entrySet()) {
                if (termsToNodes.get(entry.getValue())==null) {
                    Atom topAssertion=Atom.create(AtomicConcept.THING, entry.getKey());
                    loadPositiveFact(termsToNodes,topAssertion,m_dependencySetFactory.emptySet());
                }
                entry.setValue(termsToNodes.get(entry.getKey()));
            }
        // Ensure that at least one individual exists.
        if (m_firstTableauNode==null)
            createNewNINode(m_dependencySetFactory.emptySet());
        boolean result=runCalculus();
        if (m_tableauMonitor!=null)
            m_tableauMonitor.isSatisfiableFinished(reasoningTaskDescription,result);
        return result;
    }
    protected void loadPositiveFact(Map<Term,Node> termsToNodes,Atom atom,DependencySet dependencySet) {
        DLPredicate dlPredicate=atom.getDLPredicate();
        if (dlPredicate instanceof LiteralConcept)
            m_extensionManager.addConceptAssertion((LiteralConcept)dlPredicate,getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet),dependencySet,true);
        else if (dlPredicate instanceof AtomicRole || Equality.INSTANCE.equals(dlPredicate) || Inequality.INSTANCE.equals(dlPredicate))
            m_extensionManager.addAssertion(dlPredicate,getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet),getNodeForTerm(termsToNodes,atom.getArgument(1),dependencySet),dependencySet,true);
        else if (dlPredicate instanceof DescriptionGraph) {
            DescriptionGraph descriptionGraph=(DescriptionGraph)dlPredicate;
            Object[] tuple=new Object[descriptionGraph.getArity()+1];
            tuple[0]=descriptionGraph;
            for (int argumentIndex=0;argumentIndex<descriptionGraph.getArity();argumentIndex++)
                tuple[argumentIndex+1]=getNodeForTerm(termsToNodes,atom.getArgument(argumentIndex),dependencySet);
            m_extensionManager.addTuple(tuple,dependencySet,true);
        }
        else
            throw new IllegalArgumentException("Unsupported type of positive ground atom.");
    }
    protected void loadNegativeFact(Map<Term,Node> termsToNodes,Atom atom,DependencySet dependencySet) {
        DLPredicate dlPredicate=atom.getDLPredicate();
        if (dlPredicate instanceof LiteralConcept)
            m_extensionManager.addConceptAssertion(((LiteralConcept)dlPredicate).getNegation(),getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet),dependencySet,true);
        else if (dlPredicate instanceof AtomicRole) {
            Object[] ternaryTuple=m_extensionManager.m_ternaryAuxiliaryTupleAdd;
            ternaryTuple[0]=NegatedAtomicRole.create((AtomicRole)dlPredicate);
            ternaryTuple[1]=getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet);
            ternaryTuple[2]=getNodeForTerm(termsToNodes,atom.getArgument(1),dependencySet);
            m_extensionManager.addTuple(ternaryTuple,dependencySet,true);
        }
        else if (Equality.INSTANCE.equals(dlPredicate))
            m_extensionManager.addAssertion(Inequality.INSTANCE,getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet),getNodeForTerm(termsToNodes,atom.getArgument(1),dependencySet),dependencySet,true);
        else if (Inequality.INSTANCE.equals(dlPredicate))
            m_extensionManager.addAssertion(Equality.INSTANCE,getNodeForTerm(termsToNodes,atom.getArgument(0),dependencySet),getNodeForTerm(termsToNodes,atom.getArgument(1),dependencySet),dependencySet,true);
        else
            throw new IllegalArgumentException("Unsupported type of negative ground atom.");
    }
    protected Node getNodeForTerm(Map<Term,Node> termsToNodes,Term term,DependencySet dependencySet) {
        Node node=termsToNodes.get(term);
        if (node==null) {
            if (term instanceof Individual) {
                Individual individual=(Individual)term;
                if (individual.isAnonymous())
                    node=createNewNINode(dependencySet);
                else
                    node=createNewNamedNode(dependencySet);
            }
            else {
                Constant constant=(Constant)term;
                node=createNewRootConstantNode(dependencySet);
                // Anonymous constant values are not assigned a particular value.
                // See the hack in OWLClausification for an explanation.
                if (!constant.isAnonymous())
                    m_extensionManager.addAssertion(ConstantEnumeration.create(new Constant[] { constant }),node,dependencySet,true);
            }
            termsToNodes.put(term,node);
        }
        return node.getCanonicalNode();
    }
    protected boolean runCalculus() {
        m_interruptFlag.startTask();
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
        }
        finally {
            m_interruptFlag.endTask();
        }
    }
    protected boolean doIteration() {
        if (!m_extensionManager.containsClash()) {
            m_nominalIntroductionManager.processAnnotatedEqualities();
            boolean hasChange=false;
            while (m_extensionManager.propagateDeltaNew() && !m_extensionManager.containsClash()) {
                if (m_hasDescriptionGraphs && !m_extensionManager.containsClash())
                    m_descriptionGraphManager.checkGraphConstraints();
                if (!m_extensionManager.containsClash())
                    m_permanentHyperresolutionManager.applyDLClauses();
                if (m_additionalHyperresolutionManager!=null && !m_extensionManager.containsClash())
                    m_additionalHyperresolutionManager.applyDLClauses();
                if (m_checkUnknownDatatypeRestrictions && !m_extensionManager.containsClash())
                    m_datatypeManager.applyUnknownDatatypeRestrictionSemantics();
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
        if (nodeType.m_isAbstract) {
            m_extensionManager.addConceptAssertion(AtomicConcept.THING,node,dependencySet,true);
            if (nodeType==NodeType.NAMED_NODE && m_needsNamedExtension)
                m_extensionManager.addConceptAssertion(AtomicConcept.INTERNAL_NAMED,node,dependencySet,true);
        }
        else
            m_extensionManager.addDataRangeAssertion(InternalDatatype.RDFS_LITERAL,node,dependencySet,true);
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
}
