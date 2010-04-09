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

package org.semanticweb.HermiT;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.protege.editor.owl.model.inference.ReasonerPreferences;
import org.semanticweb.HermiT.Configuration.BlockingStrategyType;
import org.semanticweb.HermiT.Configuration.PrepareReasonerInferences;
import org.semanticweb.HermiT.blocking.AncestorBlocking;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.AnywhereValidatedBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.ValidatedPairwiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.ValidatedSingleDirectBlockingChecker;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.datatypes.rdfplainliteral.RDFPlainLiteralDataValue;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.AtomicConceptSubsumptionCache;
import org.semanticweb.HermiT.hierarchy.ClassificationManager;
import org.semanticweb.HermiT.hierarchy.DataRoleSubsumptionCache;
import org.semanticweb.HermiT.hierarchy.DeterministicClassificationManager;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyPrinterFSS;
import org.semanticweb.HermiT.hierarchy.ObjectRoleSubsumptionCache;
import org.semanticweb.HermiT.hierarchy.QuasiOrderClassificationManager;
import org.semanticweb.HermiT.hierarchy.StandardClassificationManager;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.monitor.TableauMonitorFork;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.HermiT.monitor.TimerWithPause;
import org.semanticweb.HermiT.structural.BuiltInPropertyManager;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxiomsExpressivity;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.HermiT.structural.OWLNormalization;
import org.semanticweb.HermiT.structural.ObjectPropertyInclusionManager;
import org.semanticweb.HermiT.tableau.InterruptFlag;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.util.Version;

/**
 * Answers queries about the logical implications of a particular knowledge base. A Reasoner is associated with a single knowledge base, which is "loaded" when the reasoner is constructed. By default a full classification of all atomic terms in the knowledge base is also performed at this time (which can take quite a while for large or complex ontologies), but this behavior can be disabled as a part of the Reasoner configuration. Internal details of the loading and reasoning algorithms can be configured in the Reasoner constructor and do not change over the lifetime of the Reasoner object---internal data structures and caches are optimized for a particular configuration. By default, HermiT will use the set of options which provide optimal performance.
 */
public class Reasoner implements OWLReasoner {
    protected final OntologyChangeListener m_ontologyChangeListener;
    protected final Configuration m_configuration;
    protected final OWLOntology m_rootOntology;
    protected final Collection<DescriptionGraph> m_descriptionGraphs;
    protected final InterruptFlag m_interruptFlag;
    protected boolean m_ontologyChanged;
    protected ObjectPropertyInclusionManager m_objectPropertyInclusionManager;
    protected DLOntology m_dlOntology;
    protected Prefixes m_prefixes;
    protected Tableau m_tableau;
    protected ClassificationManager<AtomicConcept> m_atomicConceptClassificationManager;
    protected Hierarchy<AtomicConcept> m_atomicConceptHierarchy;
    protected ClassificationManager<Role> m_objectRoleClassificationManager;
    protected Hierarchy<Role> m_objectRoleHierarchy;
    protected ClassificationManager<Role> m_dataRoleClassificationManager;
    protected Hierarchy<Role> m_dataRoleHierarchy;
    protected Map<AtomicConcept,Set<Individual>> m_realization;
    protected Boolean m_isConsistent;

    /**
     * Creates a new reasoner object with standard parameters for blocking, expansion strategy etc. Then the given manager is used to find all required imports for the given ontology and the ontology with the imports is loaded into the reasoner and the data factory of the manager is used to create fresh concepts during the preprocessing phase if necessary.
     *
     * @param rootOntology
     *            - the ontology that should be loaded by the reasoner
     */
    public Reasoner(OWLOntology rootOntology) {
        this(new Configuration(),rootOntology,(Set<DescriptionGraph>)null);
    }

    /**
     * Creates a new reasoner object with the parameters for blocking, expansion strategy etc as specified in the given configuration object. A default configuration can be obtained by just passing new Configuration(). Then the given manager is used to find all required imports for the given ontology and the ontology with the imports is loaded into the reasoner and the data factory of the manager is used to create fresh concepts during the preprocessing phase if necessary.
     *
     * @param configuration
     *            - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param rootOntology
     *            - the ontology that should be loaded by the reasoner
     */
    public Reasoner(Configuration configuration,OWLOntology rootOntology) {
        this(configuration,rootOntology,(Set<DescriptionGraph>)null);
    }

    /**
     * Creates a new reasoner object loaded with the given ontology and the given description graphs. When creating the reasoner, the given configuration determines the parameters for blocking, expansion strategy etc. A default configuration can be obtained by just passing new Configuration(). Then the given manager is used to find all required imports for the given ontology and the ontology with the imports and the description graphs are loaded into the reasoner. The data factory of the manager is used to create fresh concepts during the preprocessing phase if necessary.
     *
     * @param configuration
     *            - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param rootOntology
     *            - the ontology that should be loaded by the reasoner
     * @param descriptionGraphs
     *            - a set of description graphs
     */
    public Reasoner(Configuration configuration,OWLOntology rootOntology,Collection<DescriptionGraph> descriptionGraphs) {
        m_ontologyChangeListener=new OntologyChangeListener();
        m_configuration=configuration;
        m_rootOntology=rootOntology;
        m_rootOntology.getOWLOntologyManager().addOntologyChangeListener(m_ontologyChangeListener);
        if (descriptionGraphs==null)
            m_descriptionGraphs=Collections.emptySet();
        else
            m_descriptionGraphs=descriptionGraphs;
        m_interruptFlag=new InterruptFlag(configuration.individualTaskTimeout);
        m_ontologyChanged=false;
        loadOntology();
    }

    // Life-cycle management methods

    protected void loadOntology() {
        clearState();
        // Convert OWLOntology into DLOntology
        OWLClausification clausifier=new OWLClausification(m_configuration);
        Object[] result=clausifier.preprocessAndClausify(m_rootOntology,m_descriptionGraphs);
        m_objectPropertyInclusionManager=(ObjectPropertyInclusionManager)result[0];
        m_dlOntology=(DLOntology)result[1];
        // Load the DLOntology
        m_prefixes=createPrefixes(m_dlOntology);
        m_tableau=createTableau(m_interruptFlag,m_configuration,m_dlOntology,null,m_prefixes);
        m_atomicConceptClassificationManager=createAtomicConceptClassificationManager(this);
        m_objectRoleClassificationManager=createObjectRoleClassificationManager(this);
        m_dataRoleClassificationManager=createDataRoleClassificationManager(this);
    }
    protected void finalize() {
        dispose();
    }
    public void dispose() {
        m_rootOntology.getOWLOntologyManager().removeOntologyChangeListener(m_ontologyChangeListener);
        clearState();
        m_interruptFlag.dispose();
    }
    protected void clearState() {
        m_dlOntology=null;
        m_prefixes=null;
        m_tableau=null;
        m_atomicConceptClassificationManager=null;
        m_atomicConceptHierarchy=null;
        m_objectRoleClassificationManager=null;
        m_objectRoleHierarchy=null;
        m_dataRoleClassificationManager=null;
        m_dataRoleHierarchy=null;
        m_realization=null;
        m_isConsistent=null;
    }
    public void interrupt() {
        m_interruptFlag.interrupt();
    }
    public OWLDataFactory getDataFactory() {
        return m_rootOntology.getOWLOntologyManager().getOWLDataFactory();
    }

    // Accessor methods of the OWL API

    public String getReasonerName() {
        return getClass().getPackage().getImplementationTitle();
    }
    public Version getReasonerVersion() {
        String versionString=Reasoner.class.getPackage().getImplementationVersion();
        String[] splitted;
        int filled=0;
        int version[]=new int[4];
        if (versionString!=null) {
            splitted=versionString.split("\\.");
            while (filled<splitted.length) {
                version[filled]=Integer.parseInt(splitted[filled]);
                filled++;
            }
        }
        while (filled<version.length) {
            version[filled]=0;
            filled++;
        }
        return new Version(version[0],version[1],version[2],version[3]);
    }
    public OWLOntology getRootOntology() {
        return null;
    }
    public long getTimeOut() {
        return m_configuration.individualTaskTimeout;
    }
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return m_configuration.getIndividualNodeSetPolicy();
    }
    public FreshEntityPolicy getFreshEntityPolicy() {
        return m_configuration.getFreshEntityPolicy();
    }

    // HermiT's accessor methods

    public Prefixes getPrefixes() {
        return m_prefixes;
    }
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    public Configuration getConfiguration() {
        return m_configuration.clone();
    }

    // Ontology change management methods, only implemented in ChangeTrackingReasoner

    public BufferingMode getBufferingMode() {
        return m_configuration.bufferChanges ? BufferingMode.BUFFERING : BufferingMode.NON_BUFFERING;
    }
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return Collections.emptySet();
    }
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return Collections.emptySet();
    }
    public List<OWLOntologyChange> getPendingChanges() {
        return Collections.emptyList();
    }
    public void flush() {
        if (m_ontologyChanged) {
            loadOntology();
            m_ontologyChanged=false;
        }
    }

    // General inferences

    public boolean isDefined(OWLClass owlClass) {
        AtomicConcept atomicConcept=AtomicConcept.create(owlClass.getIRI().toString());
        return m_dlOntology.containsAtomicConcept(atomicConcept) || AtomicConcept.THING.equals(atomicConcept) || AtomicConcept.NOTHING.equals(atomicConcept);
    }
    public boolean isDefined(OWLIndividual owlIndividual) {
        Individual individual;
        if (owlIndividual.isAnonymous())
            individual=Individual.createAnonymous(owlIndividual.asOWLAnonymousIndividual().getID().toString());
        else
            individual=Individual.create(owlIndividual.asOWLNamedIndividual().getIRI().toString());
        return m_dlOntology.containsIndividual(individual);
    }
    public boolean isDefined(OWLObjectProperty owlObjectProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlObjectProperty.getIRI().toString());
        return m_dlOntology.containsObjectRole(atomicRole) || AtomicRole.TOP_OBJECT_ROLE.equals(owlObjectProperty) || AtomicRole.BOTTOM_OBJECT_ROLE.equals(owlObjectProperty);
    }
    public boolean isDefined(OWLDataProperty owlDataProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlDataProperty.getIRI().toString());
        return m_dlOntology.containsDataRole(atomicRole) || AtomicRole.TOP_DATA_ROLE.equals(atomicRole) || AtomicRole.BOTTOM_DATA_ROLE.equals(atomicRole);
    }
    public void prepareReasoner() {
        throwInconsistentOntologyExceptionIfNecessary();
        boolean doAll=m_configuration.prepareReasonerInferences==null;
        if (doAll || m_configuration.prepareReasonerInferences.classClassificationRequired)
            classify();
        if (doAll || m_configuration.prepareReasonerInferences.objectPropertyClassificationRequired)
            classifyObjectProperties();
        if (doAll || m_configuration.prepareReasonerInferences.dataPropertyClassificationRequired)
            classifyDataProperties();
        if (doAll || m_configuration.prepareReasonerInferences.objectPropertyRealisationRequired)
            realise();
    }
    public boolean isConsistent() {
        if (m_isConsistent==null)
            m_isConsistent=getTableau().isSatisfiable(true,true,null,null,null,null,null,ReasoningTaskDescription.isABoxSatisfiable());
        return m_isConsistent;
    }
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return true;
    }
    public boolean isEntailed(OWLAxiom axiom) {
        throwFreshEntityExceptionIfNecessary(axiom);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        EntailmentChecker checker=new EntailmentChecker(this,getDataFactory());
        return checker.entails(axiom);
    }
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        if (m_configuration.freshEntityPolicy==FreshEntityPolicy.DISALLOW)
            for (OWLAxiom axiom : axioms)
                throwFreshEntityExceptionIfNecessary(axiom);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        EntailmentChecker checker=new EntailmentChecker(this,getDataFactory());
        return checker.entails(axioms);
    }

    // Concept inferences

    public boolean isClassified() {
        return m_atomicConceptHierarchy!=null;
    }
    public void classify() {
        if (m_atomicConceptHierarchy==null) {
            throwInconsistentOntologyExceptionIfNecessary();
            Set<AtomicConcept> relevantAtomicConcepts=new HashSet<AtomicConcept>();
            relevantAtomicConcepts.add(AtomicConcept.THING);
            relevantAtomicConcepts.add(AtomicConcept.NOTHING);
            for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                if (!Prefixes.isInternalIRI(atomicConcept.getIRI()))
                    relevantAtomicConcepts.add(atomicConcept);
            if (!isConsistent())
                m_atomicConceptHierarchy=Hierarchy.emptyHierarchy(relevantAtomicConcepts,AtomicConcept.THING,AtomicConcept.NOTHING);
            else {
                try {
                    final int numRelevantConcepts=relevantAtomicConcepts.size();
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStarted("Building the class hierarchy...");
                    ClassificationManager.ProgressMonitor<AtomicConcept> progressMonitor=new ClassificationManager.ProgressMonitor<AtomicConcept>() {
                        protected int m_processedConcepts=0;
                        public void elementClassified(AtomicConcept element) {
                            m_processedConcepts++;
                            if (m_configuration.reasonerProgressMonitor!=null)
                                m_configuration.reasonerProgressMonitor.reasonerTaskProgressChanged(m_processedConcepts,numRelevantConcepts);
                        }
                    };
                    m_atomicConceptHierarchy=m_atomicConceptClassificationManager.classify(progressMonitor,AtomicConcept.THING,AtomicConcept.NOTHING,relevantAtomicConcepts);
                }
                finally {
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLClass> getTopClassNode() {
        classify();
        return atomicConceptHierarchyNodeToNode(m_atomicConceptHierarchy.getTopNode());
    }
    public Node<OWLClass> getBottomClassNode() {
        classify();
        return atomicConceptHierarchyNodeToNode(m_atomicConceptHierarchy.getBottomNode());
    }
    public boolean isSatisfiable(OWLClassExpression classExpression) {
        throwFreshEntityExceptionIfNecessary(classExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return false;
        if (classExpression instanceof OWLClass) {
            AtomicConcept concept=H((OWLClass)classExpression);
            if (m_atomicConceptHierarchy==null)
                return m_atomicConceptClassificationManager.isSatisfiable(concept);
            else {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                return node!=m_atomicConceptHierarchy.getBottomNode();
            }
        }
        else {
            OWLDataFactory factory=getDataFactory();
            OWLIndividual freshIndividual=factory.getOWLAnonymousIndividual("fresh-individual");
            OWLClassAssertionAxiom assertClassExpression=factory.getOWLClassAssertionAxiom(classExpression,freshIndividual);
            Tableau tableau=getTableau(assertClassExpression);
            return tableau.isSatisfiable(true,null,null,null,null,null,ReasoningTaskDescription.isConceptSatisfiable(classExpression));
        }
    }
    public boolean isSubClassOf(OWLClassExpression subClassExpression,OWLClassExpression superClassExpression) {
        throwFreshEntityExceptionIfNecessary(subClassExpression,superClassExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (subClassExpression instanceof OWLClass && superClassExpression instanceof OWLClass) {
            AtomicConcept subconcept=H((OWLClass)subClassExpression);
            AtomicConcept superconcept=H((OWLClass)superClassExpression);
            return m_atomicConceptClassificationManager.isSubsumedBy(subconcept,superconcept);
        }
        else {
            OWLDataFactory factory=getDataFactory();
            OWLIndividual freshIndividual=factory.getOWLAnonymousIndividual("fresh-individual");
            OWLClassAssertionAxiom assertSubClassExpression=factory.getOWLClassAssertionAxiom(subClassExpression,freshIndividual);
            OWLClassAssertionAxiom assertNotSuperClassExpression=factory.getOWLClassAssertionAxiom(superClassExpression.getObjectComplementOf(),freshIndividual);
            Tableau tableau=getTableau(assertSubClassExpression,assertNotSuperClassExpression);
            return !tableau.isSatisfiable(true,null,null,null,null,null,ReasoningTaskDescription.isConceptSubsumedBy(subClassExpression,superClassExpression));
        }
    }
    public boolean isEquivalentClass(OWLClassExpression classExpression1,OWLClassExpression classExpression2) {
        return isSubClassOf(classExpression1,classExpression2) && isSubClassOf(classExpression2,classExpression1);
    }
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptHierarchyNodeToNode(node);
    }
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression classExpression,boolean direct) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        Set<HierarchyNode<AtomicConcept>> result;
        if (direct)
            result=node.getParentNodes();
        else {
            result=new HashSet<HierarchyNode<AtomicConcept>>(node.getAncestorNodes());
            result.remove(node);
        }
        return atomicConceptHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLClass> getAncestorClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptHierarchyNodesToNodeSet(node.getAncestorNodes());
    }
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression classExpression,boolean direct) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        Set<HierarchyNode<AtomicConcept>> result;
        if (direct)
            result=node.getChildNodes();
        else {
            result=new HashSet<HierarchyNode<AtomicConcept>>(node.getDescendantNodes());
            result.remove(node);
        }
        return atomicConceptHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLClass> getDescendantClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptHierarchyNodesToNodeSet(node.getDescendantNodes());
    }
    public Node<OWLClass> getUnsatisfiableClasses() {
        classify();
        HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getBottomNode();
        return atomicConceptHierarchyNodeToNode(node);
    }
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression classExpression,boolean direct) {
        Node<OWLClass> equivalentToComplement=getEquivalentClasses(classExpression.getObjectComplementOf());
        if (direct && equivalentToComplement.getSize()>0)
            return new OWLClassNodeSet(equivalentToComplement);
        NodeSet<OWLClass> subsDisjoint=getSubClasses(classExpression.getObjectComplementOf(),direct);
        if (direct)
            return subsDisjoint;
        Set<Node<OWLClass>> result=new HashSet<Node<OWLClass>>();
        result.add(equivalentToComplement);
        result.addAll(subsDisjoint.getNodes());
        return new OWLClassNodeSet(result);
    }
    protected HierarchyNode<AtomicConcept> getHierarchyNode(OWLClassExpression classExpression) {
        throwFreshEntityExceptionIfNecessary(classExpression);
        classify();
        if (!isConsistent())
            return m_atomicConceptHierarchy.getBottomNode();
        else if (classExpression instanceof OWLClass) {
            AtomicConcept atomicConcept=H((OWLClass)classExpression);
            HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(atomicConcept);
            if (node==null)
                node=new HierarchyNode<AtomicConcept>(atomicConcept,Collections.singleton(atomicConcept),Collections.singleton(m_atomicConceptHierarchy.getTopNode()),Collections.singleton(m_atomicConceptHierarchy.getBottomNode()));
            return node;
        }
        else {
            OWLDataFactory factory=getDataFactory();
            OWLClass queryConcept=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(queryConcept,classExpression);
            final Tableau tableau=getTableau(classDefinitionAxiom);
            StandardClassificationManager.Relation<AtomicConcept> hierarchyRelation=new StandardClassificationManager.Relation<AtomicConcept>() {
                public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                    Individual freshIndividual=Individual.createAnonymous("fresh-individual");
                    return !tableau.isSatisfiable(true,Collections.singleton(Atom.create(child,freshIndividual)),null,null,Collections.singleton(Atom.create(parent,freshIndividual)),null,ReasoningTaskDescription.isConceptSubsumedBy(child,parent));
                }
            };
            return StandardClassificationManager.findPosition(hierarchyRelation,AtomicConcept.create("internal:query-concept"),m_atomicConceptHierarchy.getTopNode(),m_atomicConceptHierarchy.getBottomNode());
        }
    }

    // Object property inferences

    public boolean areObjectPropertiesClassified() {
        return m_objectRoleHierarchy!=null;
    }
    public void classifyObjectProperties() {
        if (m_objectRoleHierarchy==null) {
            throwInconsistentOntologyExceptionIfNecessary();
            Set<Role> allObjectRoles=new HashSet<Role>();
            for (AtomicRole atomicRole : m_dlOntology.getAllAtomicObjectRoles()) {
                allObjectRoles.add(atomicRole);
                if (m_dlOntology.hasInverseRoles())
                    allObjectRoles.add(atomicRole.getInverse());
            }
            allObjectRoles.add(AtomicRole.TOP_OBJECT_ROLE);
            allObjectRoles.add(AtomicRole.BOTTOM_OBJECT_ROLE);
            if (!isConsistent())
                m_objectRoleHierarchy=Hierarchy.emptyHierarchy(allObjectRoles,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE);
            else {
                try {
                    final int numRoles=allObjectRoles.size();
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStarted("Classifying object properties...");
                    ClassificationManager.ProgressMonitor<Role> progressMonitor=new ClassificationManager.ProgressMonitor<Role>() {
                        protected int m_processedRoles=0;
                        public void elementClassified(Role element) {
                            m_processedRoles++;
                            if (m_configuration.reasonerProgressMonitor!=null)
                                m_configuration.reasonerProgressMonitor.reasonerTaskProgressChanged(m_processedRoles,numRoles);
                        }
                    };
                    m_objectRoleHierarchy=m_objectRoleClassificationManager.classify(progressMonitor,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE,allObjectRoles);
                }
                finally {
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLObjectProperty> getTopObjectPropertyNode() {
        classifyObjectProperties();
        return objectPropertyHierarchyNodeToNode(m_objectRoleHierarchy.getTopNode());
    }
    public Node<OWLObjectProperty> getBottomObjectPropertyNode() {
        classifyObjectProperties();
        return objectPropertyHierarchyNodeToNode(m_objectRoleHierarchy.getBottomNode());
    }
    public boolean isSubObjectPropertyExpressionOf(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        throwFreshEntityExceptionIfNecessary(subObjectPropertyExpression,superObjectPropertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent() || superObjectPropertyExpression.getNamedProperty().isOWLTopObjectProperty())
            return true;
        Role subrole=H(subObjectPropertyExpression);
        Role superrole=H(superObjectPropertyExpression);
        return m_objectRoleClassificationManager.isSubsumedBy(subrole,superrole);
    }
    public boolean isSubObjectPropertyExpressionOf(List<OWLObjectPropertyExpression> subPropertyChain,OWLObjectPropertyExpression superObjectPropertyExpression) {
        for (OWLObjectPropertyExpression subProperty : subPropertyChain)
            throwFreshEntityExceptionIfNecessary(subProperty);
        throwFreshEntityExceptionIfNecessary(superObjectPropertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent() || superObjectPropertyExpression.getNamedProperty().isOWLTopObjectProperty())
            return true;
        else {
            OWLDataFactory factory=getDataFactory();
            OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
            OWLClassExpression allSuperNotPseudoNominal=factory.getOWLObjectAllValuesFrom(superObjectPropertyExpression,pseudoNominal.getObjectComplementOf());
            OWLAxiom[] additionalAxioms=new OWLAxiom[subPropertyChain.size()+2];
            int axiomIndex=0;
            for (OWLObjectPropertyExpression subObjectPropertyExpression : subPropertyChain) {
                OWLIndividual first=factory.getOWLAnonymousIndividual("fresh-individual-"+axiomIndex);
                OWLIndividual second=factory.getOWLAnonymousIndividual("fresh-individual-"+(axiomIndex+1));
                additionalAxioms[axiomIndex++]=factory.getOWLObjectPropertyAssertionAxiom(subObjectPropertyExpression,first,second);
            }
            OWLIndividual freshIndividual0=factory.getOWLAnonymousIndividual("fresh-individual-0");
            OWLIndividual freshIndividualN=factory.getOWLAnonymousIndividual("fresh-individual-"+subPropertyChain.size());
            additionalAxioms[axiomIndex++]=factory.getOWLClassAssertionAxiom(pseudoNominal,freshIndividualN);
            additionalAxioms[axiomIndex++]=factory.getOWLClassAssertionAxiom(allSuperNotPseudoNominal,freshIndividual0);
            Tableau tableau=getTableau(additionalAxioms);
            return !tableau.isSatisfiable(true,null,null,null,null,null,new ReasoningTaskDescription(true,"subproperty chain subsumption"));
        }
    }
    public boolean isEquivalentObjectPropertyExpression(OWLObjectPropertyExpression objectPropertyExpression1,OWLObjectPropertyExpression objectPropertyExpression2) {
        return isSubObjectPropertyExpressionOf(objectPropertyExpression1,objectPropertyExpression2) && isSubObjectPropertyExpressionOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    public NodeSet<OWLObjectProperty> getSuperObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> result;
        if (direct)
            result=node.getParentNodes();
        else {
            result=node.getAncestorNodes();
            result.remove(node);
        }
        return objectPropertyHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLObjectProperty> getAncestorObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyHierarchyNodesToNodeSet(node.getAncestorNodes());
    }
    public NodeSet<OWLObjectProperty> getSubObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> result;
        if (direct)
            result=node.getChildNodes();
        else {
            result=node.getDescendantNodes();
            result.remove(node);
        }
        return objectPropertyHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLObjectProperty> getDescendantObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyHierarchyNodesToNodeSet(node.getDescendantNodes());
    }
    public Node<OWLObjectProperty> getEquivalentObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return objectPropertyHierarchyNodeToNode(getHierarchyNode(propertyExpression));
    }
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        classify();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        final Role role=H(propertyExpression);
        final Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
        final Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
        final Set<Atom> roleAssertion=Collections.singleton(role.getRoleAssertion(freshIndividualA,freshIndividualB));
        final Tableau tableau=getTableau();
        StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>> searchPredicate=new StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>>() {
            public Set<HierarchyNode<AtomicConcept>> getSuccessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getChildNodes();
            }
            public Set<HierarchyNode<AtomicConcept>> getPredecessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getParentNodes();
            }
            public boolean trueOf(HierarchyNode<AtomicConcept> u) {
                AtomicConcept potentialDomainConcept=u.getRepresentative();
                return !tableau.isSatisfiable(false,roleAssertion,Collections.singleton(Atom.create(potentialDomainConcept,freshIndividualA)),null,null,null,ReasoningTaskDescription.isDomainOf(potentialDomainConcept,role));
            }
        };
        Set<HierarchyNode<AtomicConcept>> directDomainNodes=StandardClassificationManager.search(searchPredicate,Collections.singleton(m_atomicConceptHierarchy.getTopNode()),null);
        Set<HierarchyNode<AtomicConcept>> resultDomainNodes;
        if (direct)
            resultDomainNodes=directDomainNodes;
        else
            resultDomainNodes=HierarchyNode.getAncestorNodes(directDomainNodes);
        return atomicConceptHierarchyNodesToNodeSet(resultDomainNodes);
    }
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        classify();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        final Role role=H(propertyExpression);
        final Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
        final Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
        final Set<Atom> roleAssertion=Collections.singleton(role.getRoleAssertion(freshIndividualA,freshIndividualB));
        final Tableau tableau=getTableau();
        StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>> searchPredicate=new StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>>() {
            public Set<HierarchyNode<AtomicConcept>> getSuccessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getChildNodes();
            }
            public Set<HierarchyNode<AtomicConcept>> getPredecessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getParentNodes();
            }
            public boolean trueOf(HierarchyNode<AtomicConcept> u) {
                AtomicConcept potentialRangeConcept=u.getRepresentative();
                return !tableau.isSatisfiable(false,roleAssertion,Collections.singleton(Atom.create(potentialRangeConcept,freshIndividualB)),null,null,null,ReasoningTaskDescription.isRangeOf(potentialRangeConcept,role));
            }
        };
        Set<HierarchyNode<AtomicConcept>> directDomainNodes=StandardClassificationManager.search(searchPredicate,Collections.singleton(m_atomicConceptHierarchy.getTopNode()),null);
        Set<HierarchyNode<AtomicConcept>> resultDomainNodes;
        if (direct)
            resultDomainNodes=directDomainNodes;
        else
            resultDomainNodes=HierarchyNode.getAncestorNodes(directDomainNodes);
        return atomicConceptHierarchyNodesToNodeSet(resultDomainNodes);
    }
    public Node<OWLObjectProperty> getInverseObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return getEquivalentObjectProperties(propertyExpression.getInverseProperty());
    }
    public NodeSet<OWLObjectProperty> getDisjointObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        if (!isConsistent())
            return new OWLObjectPropertyNodeSet();
        classifyObjectProperties();
        Set<HierarchyNode<Role>> result=new HashSet<HierarchyNode<Role>>();
        if (propertyExpression.getNamedProperty().isOWLTopObjectProperty()) {
            result.add(m_objectRoleHierarchy.getBottomNode());
            return objectPropertyHierarchyNodesToNodeSet(result);
        }
        else if (propertyExpression.isOWLBottomObjectProperty()) {
            HierarchyNode<Role> node=m_objectRoleHierarchy.getTopNode();
            result.add(node);
            if (!direct)
                result.addAll(node.getDescendantNodes());
            return objectPropertyHierarchyNodesToNodeSet(result);
        }
        OWLDataFactory factory=getDataFactory();
        OWLIndividual freshIndividualA=factory.getOWLAnonymousIndividual("fresh-individual-A");
        OWLIndividual freshIndividualB=factory.getOWLAnonymousIndividual("fresh-individual-B");
        OWLAxiom assertion=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,freshIndividualA,freshIndividualB);
        Set<HierarchyNode<Role>> nodesToTest=new HashSet<HierarchyNode<Role>>();
        nodesToTest.addAll(m_objectRoleHierarchy.getTopNode().getChildNodes());
        while (!nodesToTest.isEmpty()) {
            HierarchyNode<Role> nodeToTest=nodesToTest.iterator().next();
            Role roleToTest=nodeToTest.getRepresentative();
            OWLObjectProperty testProperty;
            OWLAxiom assertion2;
            if (roleToTest instanceof AtomicRole) {
                testProperty=factory.getOWLObjectProperty(IRI.create(((AtomicRole) roleToTest).getIRI()));
                assertion2=factory.getOWLObjectPropertyAssertionAxiom(testProperty,freshIndividualA,freshIndividualB);
            } else {
                testProperty=factory.getOWLObjectProperty(IRI.create(((InverseRole)roleToTest).getInverseOf().getIRI()));
                assertion2=factory.getOWLObjectPropertyAssertionAxiom(testProperty,freshIndividualB,freshIndividualA);
            }
            Tableau tableau=getTableau(assertion,assertion2);
            if (!tableau.isSatisfiable(true,true,null,null,null,null,null,new ReasoningTaskDescription(true,"disjointness of {0} and {1}",propertyExpression,testProperty))) {
                // disjoint
                if (direct)
                    result.add(nodeToTest);
                else
                    result.addAll(nodeToTest.getDescendantNodes());
            }
            else {
                // maybe some children are disjoint
                nodesToTest.addAll(nodeToTest.getChildNodes());
            }
        }
        if (result.isEmpty())
            result.add(m_objectRoleHierarchy.getBottomNode());
        return objectPropertyHierarchyNodesToNodeSet(result);
    }
    public boolean isFunctional(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        Role role=H(propertyExpression);
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
        Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
        Set<Atom> assertions=new HashSet<Atom>();
        assertions.add(role.getRoleAssertion(freshIndividual,freshIndividualA));
        assertions.add(role.getRoleAssertion(freshIndividual,freshIndividualB));
        assertions.add(Atom.create(Inequality.INSTANCE,freshIndividualA,freshIndividualB));
        return !getTableau().isSatisfiable(false,assertions,null,null,null,null,new ReasoningTaskDescription(true,"functionality of {0}",role));
    }
    public boolean isInverseFunctional(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        Role role=H(propertyExpression);
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Individual freshIndividualA=Individual.createAnonymous("fresh-individual-A");
        Individual freshIndividualB=Individual.createAnonymous("fresh-individual-B");
        Set<Atom> assertions=new HashSet<Atom>();
        assertions.add(role.getRoleAssertion(freshIndividualA,freshIndividual));
        assertions.add(role.getRoleAssertion(freshIndividualB,freshIndividual));
        assertions.add(Atom.create(Inequality.INSTANCE,freshIndividualA,freshIndividualB));
        return !getTableau().isSatisfiable(false,assertions,null,null,null,null,new ReasoningTaskDescription(true,"inverse-functionality of {0}",role));
    }
    public boolean isIrreflexive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        Role role=H(propertyExpression);
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        return !getTableau().isSatisfiable(false,Collections.singleton(role.getRoleAssertion(freshIndividual,freshIndividual)),null,null,null,null,new ReasoningTaskDescription(true,"irreflexivity of {0}",role));
    }
    public boolean isReflexive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
        OWLClassExpression allNotPseudoNominal=factory.getOWLObjectAllValuesFrom(propertyExpression,pseudoNominal.getObjectComplementOf());
        OWLIndividual freshIndividual=factory.getOWLAnonymousIndividual("fresh-individual");
        OWLAxiom pseudoNominalAssertion=factory.getOWLClassAssertionAxiom(pseudoNominal,freshIndividual);
        OWLAxiom allNotPseudoNominalAssertion=factory.getOWLClassAssertionAxiom(allNotPseudoNominal,freshIndividual);
        Tableau tableau=getTableau(pseudoNominalAssertion,allNotPseudoNominalAssertion);
        return !tableau.isSatisfiable(true,null,null,null,null,null,new ReasoningTaskDescription(true,"symmetry of {0}",H(propertyExpression)));
    }
    public boolean isAsymmetric(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLIndividual freshIndividualA=factory.getOWLAnonymousIndividual("fresh-individual-A");
        OWLIndividual freshIndividualB=factory.getOWLAnonymousIndividual("fresh-individual-B");
        OWLAxiom assertion1=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,freshIndividualA,freshIndividualB);
        OWLAxiom assertion2=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression.getInverseProperty(),freshIndividualA,freshIndividualB);
        Tableau tableau=getTableau(assertion1,assertion2);
        return !tableau.isSatisfiable(true,null,null,null,null,null,new ReasoningTaskDescription(true,"asymmetry of {0}",H(propertyExpression)));
    }
    public boolean isSymmetric(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent() || propertyExpression.getNamedProperty().isOWLTopObjectProperty())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
        OWLClassExpression allNotPseudoNominal=factory.getOWLObjectAllValuesFrom(propertyExpression,pseudoNominal.getObjectComplementOf());
        OWLIndividual freshIndividualA=factory.getOWLAnonymousIndividual("fresh-individual-A");
        OWLIndividual freshIndividualB=factory.getOWLAnonymousIndividual("fresh-individual-B");
        OWLAxiom assertion1=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,freshIndividualA,freshIndividualB);
        OWLAxiom assertion2=factory.getOWLClassAssertionAxiom(allNotPseudoNominal,freshIndividualB);
        OWLAxiom assertion3=factory.getOWLClassAssertionAxiom(pseudoNominal,freshIndividualA);
        Tableau tableau=getTableau(assertion1,assertion2,assertion3);
        return !tableau.isSatisfiable(true,null,null,null,null,null,new ReasoningTaskDescription(true,"symmetry of {0}",propertyExpression));
    }
    public boolean isTransitive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
        OWLClassExpression allNotPseudoNominal=factory.getOWLObjectAllValuesFrom(propertyExpression,pseudoNominal.getObjectComplementOf());
        OWLIndividual freshIndividualA=factory.getOWLAnonymousIndividual("fresh-individual-A");
        OWLIndividual freshIndividualB=factory.getOWLAnonymousIndividual("fresh-individual-B");
        OWLIndividual freshIndividualC=factory.getOWLAnonymousIndividual("fresh-individual-C");
        OWLAxiom assertion1=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,freshIndividualA,freshIndividualB);
        OWLAxiom assertion2=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,freshIndividualB,freshIndividualC);
        OWLAxiom assertion3=factory.getOWLClassAssertionAxiom(allNotPseudoNominal,freshIndividualA);
        OWLAxiom assertion4=factory.getOWLClassAssertionAxiom(pseudoNominal,freshIndividualC);
        Tableau tableau=getTableau(assertion1,assertion2,assertion3,assertion4);
        return !tableau.isSatisfiable(true,null,null,null,null,null,new ReasoningTaskDescription(true,"transitivity of {0}",H(propertyExpression)));
    }
    protected HierarchyNode<Role> getHierarchyNode(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        classifyObjectProperties();
        if (!isConsistent())
            return m_objectRoleHierarchy.getBottomNode();
        else {
            Role role=H(propertyExpression);
            HierarchyNode<Role> node=m_objectRoleHierarchy.getNodeForElement(role);
            if (node==null)
                node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_objectRoleHierarchy.getTopNode()),Collections.singleton(m_objectRoleHierarchy.getBottomNode()));
            return node;
        }
    }

    // Data property inferences

    public boolean areDataPropertiesClassified() {
        return m_dataRoleHierarchy!=null;
    }
    public void classifyDataProperties() {
        if (m_dataRoleHierarchy==null) {
            throwInconsistentOntologyExceptionIfNecessary();
            Set<Role> allDataRoles=new HashSet<Role>(m_dlOntology.getAllAtomicDataRoles());
            allDataRoles.add(AtomicRole.TOP_DATA_ROLE);
            allDataRoles.add(AtomicRole.BOTTOM_DATA_ROLE);
            if (!isConsistent())
                m_dataRoleHierarchy=Hierarchy.emptyHierarchy(allDataRoles,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE);
            else {
                try {
                    final int numRoles=allDataRoles.size();
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStarted("Classifying data properties...");
                    ClassificationManager.ProgressMonitor<Role> progressMonitor=new ClassificationManager.ProgressMonitor<Role>() {
                        protected int m_processedRoles=0;
                        public void elementClassified(Role element) {
                            m_processedRoles++;
                            if (m_configuration.reasonerProgressMonitor!=null)
                                m_configuration.reasonerProgressMonitor.reasonerTaskProgressChanged(m_processedRoles,numRoles);
                        }
                    };
                    m_dataRoleHierarchy=m_dataRoleClassificationManager.classify(progressMonitor,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE,allDataRoles);
                }
                finally {
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        classifyDataProperties();
        return dataPropertyHierarchyNodeToNode(m_dataRoleHierarchy.getTopNode());
    }
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        classifyDataProperties();
        return dataPropertyHierarchyNodeToNode(m_dataRoleHierarchy.getBottomNode());
    }
    public boolean isSubDataPropertyOf(OWLDataProperty subDataProperty,OWLDataProperty superDataProperty) {
        throwFreshEntityExceptionIfNecessary(subDataProperty,superDataProperty);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (m_dlOntology.hasDatatypes()) {
            AtomicRole subrole=H(subDataProperty);
            AtomicRole superrole=H(superDataProperty);
            return m_dataRoleClassificationManager.isSubsumedBy(subrole,superrole);
        }
        else
            return subDataProperty.isOWLBottomDataProperty() || superDataProperty.isOWLTopDataProperty();
    }
    public boolean isEquivalentDataProperty(OWLDataProperty dataProperty1,OWLDataProperty dataProperty2) {
        return isSubDataPropertyOf(dataProperty1,dataProperty2) && isSubDataPropertyOf(dataProperty2,dataProperty1);
    }
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty property,boolean direct) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        Set<HierarchyNode<Role>> result;
        if (direct)
            result=node.getParentNodes();
        else {
            result=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
            result.remove(node);
        }
        return dataPropertyHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLDataProperty> getAncestorDataProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        return dataPropertyHierarchyNodesToNodeSet(node.getAncestorNodes());
    }
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty property,boolean direct) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        Set<HierarchyNode<Role>> result;
        if (direct)
            result=node.getChildNodes();
        else {
            result=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
            result.remove(node);
        }
        return dataPropertyHierarchyNodesToNodeSet(result);
    }
    public NodeSet<OWLDataProperty> getDescendantDataProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        return dataPropertyHierarchyNodesToNodeSet(node.getDescendantNodes());
    }
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty property) {
        return dataPropertyHierarchyNodeToNode(getHierarchyNode(property));
    }
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty property,boolean direct) {
        throwFreshEntityExceptionIfNecessary(property);
        throwInconsistentOntologyExceptionIfNecessary();
        classify();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        final AtomicRole atomicRole=H(property);
        final Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        final Constant freshConstant=Constant.create(new Constant.AnonymousConstantValue("anonymous-constant"));
        final Set<Atom> roleAssertion=Collections.singleton(atomicRole.getRoleAssertion(freshIndividual,freshConstant));
        final Tableau tableau=getTableau();
        StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>> searchPredicate=new StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>>() {
            public Set<HierarchyNode<AtomicConcept>> getSuccessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getChildNodes();
            }
            public Set<HierarchyNode<AtomicConcept>> getPredecessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getParentNodes();
            }
            public boolean trueOf(HierarchyNode<AtomicConcept> u) {
                AtomicConcept potentialDomainConcept=u.getRepresentative();
                return !tableau.isSatisfiable(false,roleAssertion,Collections.singleton(Atom.create(potentialDomainConcept,freshIndividual)),null,null,null,ReasoningTaskDescription.isDomainOf(potentialDomainConcept,atomicRole));
            }
        };
        Set<HierarchyNode<AtomicConcept>> directDomainNodes=StandardClassificationManager.search(searchPredicate,Collections.singleton(m_atomicConceptHierarchy.getTopNode()),null);
        Set<HierarchyNode<AtomicConcept>> resultDomainNodes;
        if (direct)
            resultDomainNodes=directDomainNodes;
        else
            resultDomainNodes=HierarchyNode.getAncestorNodes(directDomainNodes);
        return atomicConceptHierarchyNodesToNodeSet(resultDomainNodes);
    }
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        if (m_dlOntology.hasDatatypes()) {
            classifyDataProperties();
            if (!isConsistent())
                return new OWLDataPropertyNodeSet();
            Set<HierarchyNode<Role>> result=new HashSet<HierarchyNode<Role>>();
            if (propertyExpression.isOWLTopDataProperty()) {
                result.add(m_dataRoleHierarchy.getBottomNode());
                return dataPropertyHierarchyNodesToNodeSet(result);
            }
            else if (propertyExpression.isOWLBottomDataProperty()) {
                HierarchyNode<Role> node=m_dataRoleHierarchy.getTopNode();
                result.add(node);
                if (!direct)
                    result.addAll(node.getDescendantNodes());
                return dataPropertyHierarchyNodesToNodeSet(result);
            }
            OWLDataFactory factory=getDataFactory();
            OWLIndividual individual=factory.getOWLAnonymousIndividual("fresh-individual-A");
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant",anonymousConstantsDatatype);
            OWLDataProperty property=propertyExpression.asOWLDataProperty();
            OWLAxiom assertion=factory.getOWLDataPropertyAssertionAxiom(property,individual,constant);
            OWLAxiom assertion2;
            OWLDataProperty testProperty;
            Set<HierarchyNode<Role>> nodesToTest=new HashSet<HierarchyNode<Role>>();
            nodesToTest.addAll(m_dataRoleHierarchy.getTopNode().getChildNodes());
            while (!nodesToTest.isEmpty()) {
                HierarchyNode<Role> nodeToTest=nodesToTest.iterator().next();
                Role roleToTest=nodeToTest.getRepresentative();
                testProperty=factory.getOWLDataProperty(IRI.create(roleToTest.toString()));
                assertion2=factory.getOWLDataPropertyAssertionAxiom(testProperty,individual,constant);
                Tableau tableau=getTableau(assertion,assertion2);
                if (!tableau.isSatisfiable(true,true,null,null,null,null,null,new ReasoningTaskDescription(true,"disjointness of {0} and {1}",propertyExpression,testProperty))) {
                    // disjoint
                    if (direct)
                        result.add(nodeToTest);
                    else
                        result.addAll(nodeToTest.getDescendantNodes());
                }
                else {
                    // maybe some children are disjoint
                    nodesToTest.addAll(nodeToTest.getChildNodes());
                }
            }
            if (result.isEmpty())
                result.add(m_dataRoleHierarchy.getBottomNode());
            return dataPropertyHierarchyNodesToNodeSet(result);
        }
        else {
            throwInconsistentOntologyExceptionIfNecessary();
            OWLDataFactory factory=getDataFactory();
            if (propertyExpression.isOWLTopDataProperty() && isConsistent())
                return new OWLDataPropertyNodeSet(new OWLDataPropertyNode(factory.getOWLBottomDataProperty()));
            else if (propertyExpression.isOWLBottomDataProperty() && isConsistent())
                return new OWLDataPropertyNodeSet(new OWLDataPropertyNode(factory.getOWLTopDataProperty()));
            else
                return new OWLDataPropertyNodeSet();
        }
    }
    public boolean isFunctional(OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        AtomicRole atomicRole=H(property);
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Constant freshConstantA=Constant.create(new Constant.AnonymousConstantValue("fresh-constant-A"));
        Constant freshConstantB=Constant.create(new Constant.AnonymousConstantValue("fresh-constant-B"));
        Set<Atom> assertions=new HashSet<Atom>();
        assertions.add(atomicRole.getRoleAssertion(freshIndividual,freshConstantA));
        assertions.add(atomicRole.getRoleAssertion(freshIndividual,freshConstantB));
        assertions.add(Atom.create(Inequality.INSTANCE,freshConstantA,freshConstantB));
        return !getTableau().isSatisfiable(false,assertions,null,null,null,null,new ReasoningTaskDescription(true,"functionality of {0}",atomicRole));
    }
    protected HierarchyNode<Role> getHierarchyNode(OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(property);
        classifyDataProperties();
        if (!isConsistent())
            return m_dataRoleHierarchy.getBottomNode();
        else {
            Role role=H(property);
            HierarchyNode<Role> node=m_dataRoleHierarchy.getNodeForElement(role);
            if (node==null)
                node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_dataRoleHierarchy.getTopNode()),Collections.singleton(m_dataRoleHierarchy.getBottomNode()));
            return node;
        }
    }

    // Individual inferences

    public boolean isRealised() {
        return m_realization!=null;
    }
    public void realise() {
        if (m_realization==null) {
            throwInconsistentOntologyExceptionIfNecessary();
            classify();
            m_realization=new HashMap<AtomicConcept,Set<Individual>>();
            if (!isConsistent()) {
                Set<Individual> individuals=m_dlOntology.getAllIndividuals();
                for (AtomicConcept directSuperConcept : m_dlOntology.getAllAtomicConcepts())
                    if (!Prefixes.isInternalIRI(directSuperConcept.getIRI()))
                        m_realization.put(directSuperConcept,individuals);
            }
            else {
                if (m_configuration.reasonerProgressMonitor!=null)
                    m_configuration.reasonerProgressMonitor.reasonerTaskStarted("Computing instances for all classes...");
                int numIndividuals=m_dlOntology.getAllIndividuals().size();
                int currentIndividual=0;
                for (Individual individual : m_dlOntology.getAllIndividuals()) {
                    currentIndividual++;
                    Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(individual);
                    for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes) {
                        for (AtomicConcept directSuperConcept : directSuperConceptNode.getEquivalentElements()) {
                            Set<Individual> individuals=m_realization.get(directSuperConcept);
                            if (individuals==null) {
                                individuals=new HashSet<Individual>();
                                m_realization.put(directSuperConcept,individuals);
                            }
                            individuals.add(individual);
                        }
                    }
                    if (m_configuration.reasonerProgressMonitor!=null)
                        m_configuration.reasonerProgressMonitor.reasonerTaskProgressChanged(currentIndividual,numIndividuals);
                }
                if (m_configuration.reasonerProgressMonitor!=null)
                    m_configuration.reasonerProgressMonitor.reasonerTaskStopped();
            }
        }
    }
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual namedIndividual,boolean direct) {
        throwFreshEntityExceptionIfNecessary(namedIndividual);
        classify();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        Individual individual=H(namedIndividual);
        Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(individual);
        Set<HierarchyNode<AtomicConcept>> result=new HashSet<HierarchyNode<AtomicConcept>>(directSuperConceptNodes);
        if (!direct)
            for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes)
                result.addAll(directSuperConceptNode.getAncestorNodes());
        return atomicConceptHierarchyNodesToNodeSet(result);
    }
    public boolean hasType(OWLNamedIndividual namedIndividual,OWLClassExpression type,boolean direct) {
        throwFreshEntityExceptionIfNecessary(namedIndividual,type);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        else if (direct && isRealised() && type instanceof OWLClass) {
            Individual individual=H(namedIndividual);
            AtomicConcept atomicConcept=H((OWLClass)type);
            Set<Individual> individuals=m_realization.get(atomicConcept);
            return individuals!=null && individuals.contains(individual);
        }
        else if (type instanceof OWLClass) {
            Individual individual=H(namedIndividual);
            AtomicConcept atomicConcept=H((OWLClass)type);
            return !getTableau().isSatisfiable(true,true,null,Collections.singleton(Atom.create(atomicConcept,individual)),null,null,null,ReasoningTaskDescription.isInstanceOf(individual,atomicConcept));
        }
        else {
            OWLDataFactory factory=getDataFactory();
            OWLAxiom negatedAssertionAxiom=factory.getOWLClassAssertionAxiom(type.getObjectComplementOf(),namedIndividual);
            Tableau tableau=getTableau(negatedAssertionAxiom);
            return !tableau.isSatisfiable(true,true,null,null,null,null,null,ReasoningTaskDescription.isInstanceOf(namedIndividual,type));
        }
    }
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression classExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(classExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent()) {
            Node<OWLNamedIndividual> node=new OWLNamedIndividualNode(getAllNamedIndividuals());
            return new OWLNamedIndividualNodeSet(Collections.singleton(node));
        }
        realise();
        Set<Individual> result=new HashSet<Individual>();
        if (classExpression instanceof OWLClass) {
            AtomicConcept concept=H((OWLClass)classExpression);
            Set<Individual> directInstances=m_realization.get(concept);
            if (directInstances!=null)
                for (Individual instance : directInstances)
                    if (isResultRelevantIndividual(instance))
                        result.add(instance);
            if (!direct) {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                if (node!=null)
                    for (HierarchyNode<AtomicConcept> descendantNode : node.getDescendantNodes())
                        loadIndividualsOfNode(descendantNode,result);
            }
        }
        else {
            HierarchyNode<AtomicConcept> hierarchyNode=getHierarchyNode(classExpression);
            loadIndividualsOfNode(hierarchyNode,result);
            if (!direct)
                for (HierarchyNode<AtomicConcept> descendantNode : hierarchyNode.getDescendantNodes())
                    loadIndividualsOfNode(descendantNode,result);
            OWLDataFactory factory=getDataFactory();
            OWLClass queryClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom queryClassDefinition=factory.getOWLSubClassOfAxiom(queryClass,classExpression.getObjectComplementOf());
            Tableau tableau=getTableau(queryClassDefinition);
            AtomicConcept queryConcept=AtomicConcept.create("internal:query-concept");
            Set<HierarchyNode<AtomicConcept>> visitedNodes=new HashSet<HierarchyNode<AtomicConcept>>(hierarchyNode.getChildNodes());
            List<HierarchyNode<AtomicConcept>> toVisit=new ArrayList<HierarchyNode<AtomicConcept>>(hierarchyNode.getParentNodes());
            while (!toVisit.isEmpty()) {
                HierarchyNode<AtomicConcept> node=toVisit.remove(toVisit.size()-1);
                if (visitedNodes.add(node)) {
                    AtomicConcept nodeAtomicConcept=node.getRepresentative();
                    Set<Individual> realizationForNodeConcept=m_realization.get(nodeAtomicConcept);
                    if (realizationForNodeConcept!=null)
                        for (Individual individual : realizationForNodeConcept)
                            if (isResultRelevantIndividual(individual))
                                if (!tableau.isSatisfiable(true,true,Collections.singleton(Atom.create(queryConcept,individual)),null,null,null,null,ReasoningTaskDescription.isInstanceOf(individual,classExpression)))
                                    result.add(individual);
                    toVisit.addAll(node.getChildNodes());
                }
            }
        }
        return sortBySameAsIfNecessary(result);
    }
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual namedIndividual) {
        throwFreshEntityExceptionIfNecessary(namedIndividual);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLNamedIndividualNode(getAllNamedIndividuals());
        Set<Individual> sameIndividuals=getSameAsIndividuals(H(namedIndividual));
        OWLDataFactory factory=getDataFactory();
        Set<OWLNamedIndividual> result=new HashSet<OWLNamedIndividual>();
        for (Individual individual : sameIndividuals)
            result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
        return new OWLNamedIndividualNode(result);
    }
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual namedIndividual) {
        throwFreshEntityExceptionIfNecessary(namedIndividual);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent()) {
            Node<OWLNamedIndividual> node=new OWLNamedIndividualNode(getAllNamedIndividuals());
            return new OWLNamedIndividualNodeSet(Collections.singleton(node));
        }
        Individual individual=H(namedIndividual);
        Tableau tableau=getTableau();
        Set<Individual> result=new HashSet<Individual>();
        for (Individual potentiallyDifferentIndividual : m_dlOntology.getAllIndividuals())
            if (isResultRelevantIndividual(potentiallyDifferentIndividual) && !individual.equals(potentiallyDifferentIndividual))
                if (!tableau.isSatisfiable(true,true,Collections.singleton(Atom.create(Equality.INSTANCE,individual,potentiallyDifferentIndividual)),null,null,null,null,new ReasoningTaskDescription(true,"is {0} different from {1}",individual,potentiallyDifferentIndividual)))
                    result.add(potentiallyDifferentIndividual);
        return sortBySameAsIfNecessary(result);
    }
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual namedIndividual,OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(namedIndividual,propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent()) {
            Node<OWLNamedIndividual> node=new OWLNamedIndividualNode(getAllNamedIndividuals());
            return new OWLNamedIndividualNodeSet(Collections.singleton(node));
        }
        OWLDataFactory factory=getDataFactory();
        return getInstances(factory.getOWLObjectHasValue(propertyExpression.getInverseProperty(),namedIndividual),false);
    }
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual namedIndividual,OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(namedIndividual,property);
        throwInconsistentOntologyExceptionIfNecessary();
        Set<OWLLiteral> result=new HashSet<OWLLiteral>();
        if (m_dlOntology.hasDatatypes()) {
            OWLDataFactory factory=getDataFactory();
            Prefixes noPrefixes=new Prefixes();
            Individual individual=H(namedIndividual);
            for (OWLDataProperty dataProperty : getDescendantDataProperties(property).getFlattened()) {
                AtomicRole atomicRole=H(dataProperty);
                Map<Individual,Set<Constant>> dataPropertyAssertions=m_dlOntology.getDataPropertyAssertions().get(atomicRole);
                if (dataPropertyAssertions!=null) {
                    for (Constant constant : dataPropertyAssertions.get(individual)) {
                        Object dataValue=constant.getDataValue();
                        OWLLiteral literal;
                        if (dataValue instanceof String)
                            literal=factory.getOWLStringLiteral((String)dataValue);
                        else if (dataValue instanceof RDFPlainLiteralDataValue) {
                            RDFPlainLiteralDataValue rdfPlainLiteralDataValue=(RDFPlainLiteralDataValue)dataValue;
                            literal=factory.getOWLStringLiteral(rdfPlainLiteralDataValue.getString(),rdfPlainLiteralDataValue.getLanguageTag());
                        }
                        else {
                            String stringValue=DatatypeRegistry.toString(noPrefixes,dataValue);
                            int indexOfLastQuote=stringValue.lastIndexOf('"');
                            String lexicalForm=stringValue.substring(1,indexOfLastQuote);
                            String datatypeIRI=stringValue.substring(indexOfLastQuote+3);
                            int datatypeIRILastChar=datatypeIRI.length()-1;
                            if (datatypeIRILastChar>=1 && datatypeIRI.charAt(0)=='<' && datatypeIRI.charAt(datatypeIRILastChar)=='>')
                                datatypeIRI=datatypeIRI.substring(1,datatypeIRILastChar);
                            literal=factory.getOWLTypedLiteral(lexicalForm,factory.getOWLDatatype(IRI.create(datatypeIRI)));
                        }
                        result.add(literal);
                    }
                }
            }
        }
        return result;
    }
    public boolean hasObjectPropertyRelationship(OWLNamedIndividual subject,OWLObjectPropertyExpression propertyExpression,OWLNamedIndividual object) {
        throwFreshEntityExceptionIfNecessary(subject,propertyExpression,object);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLClass pseudoNominal=factory.getOWLClass(IRI.create("internal:pseudo-nominal"));
        OWLClassExpression allNotPseudoNominal=factory.getOWLObjectAllValuesFrom(propertyExpression,pseudoNominal.getObjectComplementOf());
        OWLAxiom allNotPseudoNominalAssertion=factory.getOWLClassAssertionAxiom(allNotPseudoNominal,subject);
        OWLAxiom pseudoNominalAssertion=factory.getOWLClassAssertionAxiom(pseudoNominal,object);
        Tableau tableau=getTableau(allNotPseudoNominalAssertion,pseudoNominalAssertion);
        return !tableau.isSatisfiable(true,true,null,null,null,null,null,new ReasoningTaskDescription(true,"is {0} connected to {1} via {2}",H(subject),H(object),H(propertyExpression)));
    }
    public boolean hasDataPropertyRelationship(OWLNamedIndividual subject,OWLDataProperty property,OWLLiteral object) {
        throwFreshEntityExceptionIfNecessary(subject,property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=getDataFactory();
        OWLAxiom notAssertion=factory.getOWLNegativeDataPropertyAssertionAxiom(property,subject,object);
        Tableau tableau=getTableau(notAssertion);
        return !tableau.isSatisfiable(true,true,null,null,null,null,null,new ReasoningTaskDescription(true,"is {0} connected to {1} via {2}",H(subject),object,H(property)));
    }
    protected void loadIndividualsOfNode(HierarchyNode<AtomicConcept> node,Set<Individual> result) {
        AtomicConcept atomicConcept=node.getRepresentative();
        Set<Individual> realizationForConcept=m_realization.get(atomicConcept);
        // RealizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null)
            for (Individual individual : realizationForConcept)
                if (!individual.isAnonymous() && !Prefixes.isInternalIRI(individual.getIRI()))
                    result.add(individual);
    }
    protected Set<HierarchyNode<AtomicConcept>> getDirectSuperConceptNodes(final Individual individual) {
        StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>> predicate=new StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>>() {
            public Set<HierarchyNode<AtomicConcept>> getSuccessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getChildNodes();
            }
            public Set<HierarchyNode<AtomicConcept>> getPredecessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getParentNodes();
            }
            public boolean trueOf(HierarchyNode<AtomicConcept> u) {
                AtomicConcept atomicConcept=u.getRepresentative();
                if (AtomicConcept.THING.equals(atomicConcept))
                    return true;
                else
                    return !getTableau().isSatisfiable(true,true,null,Collections.singleton(Atom.create(atomicConcept,individual)),null,null,null,ReasoningTaskDescription.isInstanceOf(individual,atomicConcept));
            }
        };
        return StandardClassificationManager.search(predicate,Collections.singleton(m_atomicConceptHierarchy.getTopNode()),null);
    }
    protected NodeSet<OWLNamedIndividual> sortBySameAsIfNecessary(Set<Individual> individuals) {
        OWLDataFactory factory=getDataFactory();
        Set<Node<OWLNamedIndividual>> result=new HashSet<Node<OWLNamedIndividual>>();
        if (m_configuration.individualNodeSetPolicy==IndividualNodeSetPolicy.BY_SAME_AS) {
            // group the individuals by same as equivalence classes
            while (!individuals.isEmpty()) {
                Individual individual=individuals.iterator().next();
                Set<Individual> sameIndividuals=getSameAsIndividuals(individual);
                Set<OWLNamedIndividual> sameNamedIndividuals=new HashSet<OWLNamedIndividual>();
                sameNamedIndividuals.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
                for (Individual sameIndividual : sameIndividuals) {
                    individuals.remove(sameIndividual);
                    sameNamedIndividuals.add(factory.getOWLNamedIndividual(IRI.create(sameIndividual.getIRI())));
                }
                result.add(new OWLNamedIndividualNode(sameNamedIndividuals));
            }
        }
        else {
            for (Individual individual : individuals)
                result.add(new OWLNamedIndividualNode(factory.getOWLNamedIndividual(IRI.create(individual.getIRI()))));
        }
        return new OWLNamedIndividualNodeSet(result);
    }
    protected Set<Individual> getSameAsIndividuals(Individual individual) {
        Tableau tableau=getTableau();
        Set<Individual> result=new HashSet<Individual>();
        result.add(individual);
        for (Individual potentiallySameIndividual : m_dlOntology.getAllIndividuals())
            if (isResultRelevantIndividual(potentiallySameIndividual) && !individual.equals(potentiallySameIndividual))
                if (!tableau.isSatisfiable(true,true,Collections.singleton(Atom.create(Inequality.INSTANCE,individual,potentiallySameIndividual)),null,null,null,null,new ReasoningTaskDescription(true,"is {0} same as {1}",individual,potentiallySameIndividual)))
                    result.add(potentiallySameIndividual);
        return result;
    }
    protected Set<OWLNamedIndividual> getAllNamedIndividuals() {
        Set<OWLNamedIndividual> result=new HashSet<OWLNamedIndividual>();
        OWLDataFactory factory=getDataFactory();
        for (Individual individual : m_dlOntology.getAllIndividuals())
            if (isResultRelevantIndividual(individual))
                result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
        return result;
    }
    protected static boolean isResultRelevantIndividual(Individual individual) {
        return !individual.isAnonymous() && !Prefixes.isInternalIRI(individual.getIRI());
    }

    // Various creation methods

    public Tableau getTableau() {
        m_tableau.clearAdditionalDLOntology();
        return m_tableau;
    }

    /**
     * A mostly internal method. Can be used to retrieve a tableau for axioms in the given ontology manager plus an additional set of axioms.
     *
     * @param ontologyManager
     *            - the manager is assumed to contain the axioms to which the given additional axioms are to be added
     * @param additionalAxioms
     *            - a list of additional axioms that should be included in the tableau
     * @return a tableau containing rules for the normalised axioms, this tableau is not permanent in the reasoner, i.e., it does not overwrite the originally created tableau
     * @throws IllegalArgumentException
     *             - if the axioms lead to non-admissible clauses, some configuration parameters are incompatible or other such errors
     */
    public Tableau getTableau(OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        if (additionalAxioms==null || additionalAxioms.length==0)
            return getTableau();
        else {
            DLOntology deltaDLOntology=createDeltaDLOntology(m_configuration,m_dlOntology,additionalAxioms);
            if (m_tableau.supportsAdditionalDLOntology(deltaDLOntology)) {
                m_tableau.setAdditionalDLOntology(deltaDLOntology);
                return m_tableau;
            }
            else
                return createTableau(m_interruptFlag,m_configuration,m_dlOntology,deltaDLOntology,m_prefixes);
        }
    }
    protected static Tableau createTableau(InterruptFlag interruptFlag,Configuration config,DLOntology permanentDLOntology,DLOntology additionalDLOntology,Prefixes prefixes) throws IllegalArgumentException {
        if (config.checkClauses) {
            Collection<DLClause> nonAdmissiblePermanentDLClauses=permanentDLOntology.getNonadmissibleDLClauses();
            Collection<DLClause> nonAdmissibleAdditionalDLClauses=Collections.emptySet();
            if (additionalDLOntology!=null)
                nonAdmissibleAdditionalDLClauses=additionalDLOntology.getNonadmissibleDLClauses();
            if (!nonAdmissiblePermanentDLClauses.isEmpty() || !nonAdmissibleAdditionalDLClauses.isEmpty()) {
                String CRLF=System.getProperty("line.separator");
                StringBuffer buffer=new StringBuffer();
                buffer.append("The following DL-clauses in the DL-ontology are not admissible:");
                buffer.append(CRLF);
                for (DLClause dlClause : nonAdmissiblePermanentDLClauses) {
                    buffer.append(dlClause.toString(prefixes));
                    buffer.append(CRLF);
                }
                for (DLClause dlClause : nonAdmissibleAdditionalDLClauses) {
                    buffer.append(dlClause.toString(prefixes));
                    buffer.append(CRLF);
                }
                throw new IllegalArgumentException(buffer.toString());
            }
        }

        boolean hasInverseRoles=(permanentDLOntology.hasInverseRoles() || (additionalDLOntology!=null && additionalDLOntology.hasInverseRoles()));
        boolean hasNominals=(permanentDLOntology.hasNominals() || (additionalDLOntology!=null && additionalDLOntology.hasNominals()));

        TableauMonitor wellKnownTableauMonitor=null;
        switch (config.tableauMonitorType) {
        case NONE:
            wellKnownTableauMonitor=null;
            break;
        case TIMING:
            wellKnownTableauMonitor=new Timer();
            break;
        case TIMING_WITH_PAUSE:
            wellKnownTableauMonitor=new TimerWithPause();
            break;
        case DEBUGGER_HISTORY_ON:
            wellKnownTableauMonitor=new Debugger(prefixes,true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor=new Debugger(prefixes,false);
            break;
        default:
            throw new IllegalArgumentException("Unknown monitor type");
        }

        TableauMonitor tableauMonitor=null;
        if (config.monitor==null)
            tableauMonitor=wellKnownTableauMonitor;
        else if (wellKnownTableauMonitor==null)
            tableauMonitor=config.monitor;
        else
            tableauMonitor=new TableauMonitorFork(wellKnownTableauMonitor,config.monitor);

        DirectBlockingChecker directBlockingChecker=null;
        switch (config.directBlockingType) {
        case OPTIMAL:
            if ((config.blockingStrategyType==BlockingStrategyType.OPTIMAL && hasNominals) || config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedSingleDirectBlockingChecker(hasInverseRoles);
            else {
                if (hasInverseRoles)
                    directBlockingChecker=new PairWiseDirectBlockingChecker();
                else
                    directBlockingChecker=new SingleDirectBlockingChecker();
            }
            break;
        case SINGLE:
            if (config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedSingleDirectBlockingChecker(hasInverseRoles);
            else
                directBlockingChecker=new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
            if (config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedPairwiseDirectBlockingChecker(hasInverseRoles);
            else
                directBlockingChecker=new PairWiseDirectBlockingChecker();
            break;
        default:
            throw new IllegalArgumentException("Unknown direct blocking type.");
        }

        BlockingSignatureCache blockingSignatureCache=null;
        if (!hasNominals && !(config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)) {
            switch (config.blockingSignatureCacheType) {
            case CACHED:
                blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
                break;
            case NOT_CACHED:
                blockingSignatureCache=null;
                break;
            default:
                throw new IllegalArgumentException("Unknown blocking cache type.");
            }
        }

        BlockingStrategy blockingStrategy=null;
        switch (config.blockingStrategyType) {
        case ANCESTOR:
            blockingStrategy=new AncestorBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case ANYWHERE:
            blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case SIMPLE_CORE:
            blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,hasInverseRoles,true);
            break;
        case COMPLEX_CORE:
            blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,hasInverseRoles,false);
            break;
        case OPTIMAL:
            if (hasNominals)
                blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,hasInverseRoles,true);
            else
                blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        default:
            throw new IllegalArgumentException("Unknown blocking strategy type.");
        }

        ExistentialExpansionStrategy existentialsExpansionStrategy=null;
        switch (config.existentialStrategyType) {
        case CREATION_ORDER:
            existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
            break;
        case EL:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,true);
            break;
        case INDIVIDUAL_REUSE:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
            break;
        default:
            throw new IllegalArgumentException("Unknown expansion strategy type.");
        }

        return new Tableau(interruptFlag,tableauMonitor,existentialsExpansionStrategy,config.useDisjunctionLearning,permanentDLOntology,additionalDLOntology,config.parameters);
    }

    protected static ClassificationManager<AtomicConcept> createAtomicConceptClassificationManager(Reasoner reasoner) {
        if (reasoner.getTableau().isDeterministic())
            return new DeterministicClassificationManager<AtomicConcept>(new AtomicConceptSubsumptionCache(reasoner));
        else
            return new QuasiOrderClassificationManager(reasoner);
    }

    protected static ClassificationManager<Role> createObjectRoleClassificationManager(Reasoner reasoner) {
        return new StandardClassificationManager<Role>(new ObjectRoleSubsumptionCache(reasoner));
    }

    protected static ClassificationManager<Role> createDataRoleClassificationManager(Reasoner reasoner) {
        return new StandardClassificationManager<Role>(new DataRoleSubsumptionCache(reasoner));
    }

    protected DLOntology createDeltaDLOntology(Configuration configuration,DLOntology originalDLOntology,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        Set<OWLAxiom> additionalAxiomsSet=new HashSet<OWLAxiom>();
        for (OWLAxiom axiom : additionalAxioms) {
                if (isUnsupportedExtensionAxiom(axiom))
                    throw new IllegalArgumentException("Internal error: unsupported extension axiom type.");
            additionalAxiomsSet.add(axiom);
        }
        OWLDataFactory dataFactory=getDataFactory();
        OWLAxioms axioms=new OWLAxioms();
        axioms.m_definedDatatypesIRIs.addAll(originalDLOntology.getDefinedDatatypeIRIs());
        OWLNormalization normalization=new OWLNormalization(dataFactory,axioms,originalDLOntology.getAllAtomicConcepts().size());
        normalization.processAxioms(additionalAxiomsSet);
        BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(dataFactory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms,originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_OBJECT_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_OBJECT_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_DATA_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_DATA_ROLE));
        m_objectPropertyInclusionManager.rewriteAxioms(dataFactory,axioms,originalDLOntology.getAllAtomicConcepts().size());
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        axiomsExpressivity.m_hasAtMostRestrictions|=originalDLOntology.hasAtMostRestrictions();
        axiomsExpressivity.m_hasInverseRoles|=originalDLOntology.hasInverseRoles();
        axiomsExpressivity.m_hasNominals|=originalDLOntology.hasNominals();
        axiomsExpressivity.m_hasDatatypes|=originalDLOntology.hasDatatypes();
        OWLClausification clausifier=new OWLClausification(configuration);
        Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
        return clausifier.clausify(dataFactory,"uri:urn:internal-kb",axioms,axiomsExpressivity,descriptionGraphs);
    }
    protected static boolean isUnsupportedExtensionAxiom(OWLAxiom axiom) {
        return
            axiom instanceof OWLSubObjectPropertyOfAxiom ||
            axiom instanceof OWLTransitiveObjectPropertyAxiom ||
            axiom instanceof OWLSubPropertyChainOfAxiom ||
            axiom instanceof OWLFunctionalObjectPropertyAxiom ||
            axiom instanceof OWLInverseFunctionalObjectPropertyAxiom;
    }
    protected static Prefixes createPrefixes(DLOntology dlOntology) {
        Set<String> prefixIRIs=new HashSet<String>();
        for (AtomicConcept concept : dlOntology.getAllAtomicConcepts())
            addIRI(concept.getIRI(),prefixIRIs);
        for (AtomicRole atomicRole : dlOntology.getAllAtomicDataRoles())
            addIRI(atomicRole.getIRI(),prefixIRIs);
        for (AtomicRole atomicRole : dlOntology.getAllAtomicObjectRoles())
            addIRI(atomicRole.getIRI(),prefixIRIs);
        for (Individual individual : dlOntology.getAllIndividuals())
            addIRI(individual.getIRI(),prefixIRIs);
        Prefixes prefixes=new Prefixes();
        prefixes.declareSemanticWebPrefixes();
        prefixes.declareInternalPrefixes(prefixIRIs);
        prefixes.declareDefaultPrefix(dlOntology.getOntologyIRI()+"#");
        int prefixIndex=0;
        for (String prefixIRI : prefixIRIs)
            if (prefixes.getPrefixName(prefixIRI)==null) {
                String prefix=getPrefixForIndex(prefixIndex);
                while (prefixes.getPrefixIRI(prefix)!=null)
                    prefix=getPrefixForIndex(++prefixIndex);
                prefixes.declarePrefix(prefix,prefixIRI);
                ++prefixIndex;
            }
        return prefixes;
    }
    protected static String getPrefixForIndex(int prefixIndex) {
        StringBuffer buffer=new StringBuffer();
        while (prefixIndex>=26) {
            buffer.insert(0,(char)(('a')+(prefixIndex%26)));
            prefixIndex/=26;
        }
        buffer.insert(0,(char)(('a')+prefixIndex));
        return buffer.toString();
    }
    protected static void addIRI(String uri,Set<String> prefixIRIs) {
        if (!Prefixes.isInternalIRI(uri)) {
            int lastHash=uri.lastIndexOf('#');
            if (lastHash!=-1) {
                String prefixIRI=uri.substring(0,lastHash+1);
                prefixIRIs.add(prefixIRI);
            }
        }
    }

    // Hierarchy printing

    /**
     * Prints the hierarchies into a functional style syntax ontology.
     *
     * @param out
     *            - the printwriter that is used to output the hierarchies
     * @param classes
     *            - if true, the class hierarchy is printed
     * @param objectProperties
     *            - if true, the object property hierarchy is printed
     * @param dataProperties
     *            - if true, the data property hierarchy is printed
     */
    public void printHierarchies(PrintWriter out,boolean classes,boolean objectProperties,boolean dataProperties) {
        HierarchyPrinterFSS printer=new HierarchyPrinterFSS(out,m_dlOntology.getOntologyIRI()+"#");
        if (classes) {
            classify();
            printer.loadAtomicConceptPrefixIRIs(m_atomicConceptHierarchy.getAllElements());
        }
        if (objectProperties) {
            classifyObjectProperties();
            printer.loadAtomicRolePrefixIRIs(m_dlOntology.getAllAtomicObjectRoles());
        }
        if (dataProperties) {
            classifyDataProperties();
            printer.loadAtomicRolePrefixIRIs(m_dlOntology.getAllAtomicDataRoles());
        }
        printer.startPrinting();
        boolean atLF=true;
        if (classes && !m_atomicConceptHierarchy.isEmpty()) {
            printer.printAtomicConceptHierarchy(m_atomicConceptHierarchy);
            atLF=false;
        }
        if (objectProperties && !m_objectRoleHierarchy.isEmpty()) {
            if (!atLF)
                out.println();
            printer.printRoleHierarchy(m_objectRoleHierarchy,true);
            atLF=false;
        }
        if (dataProperties && !m_dataRoleHierarchy.isEmpty()) {
            if (!atLF)
                out.println();
            printer.printRoleHierarchy(m_dataRoleHierarchy,false);
            atLF=false;
        }
        printer.endPrinting();
    }

    // Various utility methods

    protected void throwInconsistentOntologyExceptionIfNecessary() {
        if (m_configuration.throwInconsistentOntologyException && !isConsistent())
            throw new InconsistentOntologyException();
    }
    protected void throwFreshEntityExceptionIfNecessary(OWLObject... objects) {
        if (m_configuration.freshEntityPolicy==FreshEntityPolicy.DISALLOW) {
            Set<OWLEntity> undeclaredEntities=new HashSet<OWLEntity>();
            for (OWLObject object : objects) {
                if (!(object instanceof OWLEntity) || !((OWLEntity)object).isBuiltIn()) {
                    for (OWLDataProperty dp : object.getDataPropertiesInSignature())
                        if (!isDefined(dp) && !Prefixes.isInternalIRI(dp.getIRI().toString()))
                            undeclaredEntities.add(dp);
                    for (OWLObjectProperty op : object.getObjectPropertiesInSignature())
                        if (!isDefined(op) && !Prefixes.isInternalIRI(op.getIRI().toString()))
                            undeclaredEntities.add(op);
                    for (OWLNamedIndividual individual : object.getIndividualsInSignature())
                        if (!isDefined(individual) && !Prefixes.isInternalIRI(individual.getIRI().toString()))
                            undeclaredEntities.add(individual);
                    for (OWLClass owlClass : object.getClassesInSignature())
                        if (!isDefined(owlClass) && !Prefixes.isInternalIRI(owlClass.getIRI().toString()))
                            undeclaredEntities.add(owlClass);
                }
            }
            if (!undeclaredEntities.isEmpty())
                throw new FreshEntitiesException(undeclaredEntities);
        }
    }

    // Methods for conversion from OWL API to HermiT's API

    protected static AtomicConcept H(OWLClass owlClass) {
        return AtomicConcept.create(owlClass.getIRI().toString());
    }
    protected static AtomicRole H(OWLObjectProperty objectProperty) {
        return AtomicRole.create(objectProperty.getIRI().toString());
    }
    protected static Role H(OWLObjectPropertyExpression objectPropertyExpression) {
        objectPropertyExpression=objectPropertyExpression.getSimplified();
        if (objectPropertyExpression instanceof OWLObjectProperty)
            return H((OWLObjectProperty)objectPropertyExpression);
        else
            return H(objectPropertyExpression.getNamedProperty()).getInverse();
    }
    protected static AtomicRole H(OWLDataProperty dataProperty) {
        return AtomicRole.create(dataProperty.getIRI().toString());
    }
    protected static Role H(OWLDataPropertyExpression dataPropertyExpression) {
        return H((OWLDataProperty)dataPropertyExpression);
    }
    protected static Individual H(OWLNamedIndividual namedIndividual) {
        return Individual.create(namedIndividual.getIRI().toString());
    }
    protected static Individual H(OWLAnonymousIndividual anonymousIndividual) {
        return Individual.createAnonymous(anonymousIndividual.getID().toString());
    }
    protected static Individual H(OWLIndividual individual) {
        if (individual.isAnonymous())
            return H((OWLAnonymousIndividual)individual);
        else
            return H((OWLNamedIndividual)individual);
    }

    // Extended methods for conversion from HermiT's API to OWL API

    protected Node<OWLClass> atomicConceptHierarchyNodeToNode(HierarchyNode<AtomicConcept> hierarchyNode) {
        Set<OWLClass> result=new HashSet<OWLClass>();
        OWLDataFactory factory=getDataFactory();
        for (AtomicConcept concept : hierarchyNode.getEquivalentElements())
            if (!Prefixes.isInternalIRI(concept.getIRI()))
                result.add(factory.getOWLClass(IRI.create(concept.getIRI())));
        return new OWLClassNode(result);
    }
    protected NodeSet<OWLClass> atomicConceptHierarchyNodesToNodeSet(Collection<HierarchyNode<AtomicConcept>> hierarchyNodes) {
        Set<Node<OWLClass>> result=new HashSet<Node<OWLClass>>();
        for (HierarchyNode<AtomicConcept> hierarchyNode : hierarchyNodes) {
            Node<OWLClass> node=atomicConceptHierarchyNodeToNode(hierarchyNode);
            if (node.getSize()!=0)
                result.add(node);
        }
        return new OWLClassNodeSet(result);
    }
    protected Node<OWLObjectProperty> objectPropertyHierarchyNodeToNode(HierarchyNode<Role> hierarchyNode) {
        Set<OWLObjectProperty> result=new HashSet<OWLObjectProperty>();
        OWLDataFactory factory=getDataFactory();
        for (Role role : hierarchyNode.getEquivalentElements())
            if (role instanceof AtomicRole)
                result.add(factory.getOWLObjectProperty(IRI.create(((AtomicRole)role).getIRI())));
        return new OWLObjectPropertyNode(result);
    }
    protected NodeSet<OWLObjectProperty> objectPropertyHierarchyNodesToNodeSet(Collection<HierarchyNode<Role>> hierarchyNodes) {
        Set<Node<OWLObjectProperty>> result=new HashSet<Node<OWLObjectProperty>>();
        for (HierarchyNode<Role> hierarchyNode : hierarchyNodes)
            result.add(objectPropertyHierarchyNodeToNode(hierarchyNode));
        return new OWLObjectPropertyNodeSet(result);
    }
    protected Node<OWLDataProperty> dataPropertyHierarchyNodeToNode(HierarchyNode<Role> hierarchyNode) {
        Set<OWLDataProperty> result=new HashSet<OWLDataProperty>();
        OWLDataFactory factory=getDataFactory();
        for (Role role : hierarchyNode.getEquivalentElements())
            result.add(factory.getOWLDataProperty(IRI.create(((AtomicRole)role).getIRI())));
        return new OWLDataPropertyNode(result);
    }
    protected NodeSet<OWLDataProperty> dataPropertyHierarchyNodesToNodeSet(Collection<HierarchyNode<Role>> hierarchyNodes) {
        Set<Node<OWLDataProperty>> result=new HashSet<Node<OWLDataProperty>>();
        for (HierarchyNode<Role> hierarchyNode : hierarchyNodes)
            result.add(dataPropertyHierarchyNodeToNode(hierarchyNode));
        return new OWLDataPropertyNodeSet(result);
    }

    // Change tracking

    protected class OntologyChangeListener implements OWLOntologyChangeListener {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            m_ontologyChanged=true;
            if (!m_configuration.bufferChanges)
                flush();
        }
    }

    // The factory for OWL API reasoners

    public static class ReasonerFactory implements OWLReasonerFactory {
        public String getReasonerName() {
            return getClass().getPackage().getImplementationTitle();
        }
        public OWLReasoner createReasoner(OWLOntology ontology) {
            return createReasoner(ontology,null);
        }
        public OWLReasoner createReasoner(OWLOntology ontology,OWLReasonerConfiguration config) {
            return createHermiTOWLReasoner(getProtegeConfiguration(config),ontology);
        }
        public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
            return createNonBufferingReasoner(ontology,null);
        }
        public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,OWLReasonerConfiguration owlAPIConfiguration) {
            Configuration configuration=getProtegeConfiguration(owlAPIConfiguration);
            configuration.bufferChanges=false;
            return createHermiTOWLReasoner(configuration,ontology);
        }
        protected Configuration getProtegeConfiguration(OWLReasonerConfiguration owlAPIConfiguration) {
            Configuration configuration;
            if (owlAPIConfiguration!=null) {
                if (owlAPIConfiguration instanceof Configuration)
                    configuration=(Configuration)owlAPIConfiguration;
                else {
                    configuration=new Configuration();
                    configuration.freshEntityPolicy=owlAPIConfiguration.getFreshEntityPolicy();
                    configuration.individualNodeSetPolicy=owlAPIConfiguration.getIndividualNodeSetPolicy();
                    configuration.reasonerProgressMonitor=owlAPIConfiguration.getProgressMonitor();
                    configuration.individualTaskTimeout=owlAPIConfiguration.getTimeOut();
                }
            }
            else {
                configuration=new Configuration();
                configuration.ignoreUnsupportedDatatypes=true;
            }
            return configuration;
        }
        protected OWLReasoner createHermiTOWLReasoner(Configuration configuration,OWLOntology ontology) {
            return new Reasoner(configuration,ontology);
        }
    }

    // The reasoner that tracks changes

    public static class ProtegeReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
        public OWLReasoner createReasoner(OWLOntology ontology,ReasonerProgressMonitor monitor) {
            ReasonerFactory factory=new ReasonerFactory();
            Configuration configuration=factory.getProtegeConfiguration(null);
            configuration.reasonerProgressMonitor=monitor;
            ReasonerPreferences preferences=this.getOWLModelManager().getReasonerPreferences();
            PrepareReasonerInferences prepareReasonerInferences=new PrepareReasonerInferences();
            // class classification
            prepareReasonerInferences.classClassificationRequired=
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_CLASS_UNSATISFIABILITY) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_CLASSES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_INHERITED_ANONYMOUS_CLASSES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_CLASSES);

            // realisation
            prepareReasonerInferences.realisationRequired=
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERED_CLASS_MEMBERS) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_TYPES);

            // data type classification
            prepareReasonerInferences.dataPropertyClassificationRequired=
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATATYPE_PROPERTY_DOMAINS) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_DATATYPE_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_DATATYPE_PROPERTIES);

            // object property classification
            prepareReasonerInferences.objectPropertyClassificationRequired=
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_OBJECT_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_INVERSE_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_OBJECT_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_OBJECT_PROPERTY_UNSATISFIABILITY);

            // object property realisation
            prepareReasonerInferences.objectPropertyRealisationRequired=preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_ASSERTIONS);

            // object property domain & range
            prepareReasonerInferences.objectPropertyDomainsRequired=preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_DOMAINS);
            prepareReasonerInferences.objectPropertyRangesRequired=preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_RANGES);

            configuration.prepareReasonerInferences=prepareReasonerInferences;
            return factory.createHermiTOWLReasoner(configuration,ontology);
        }
        public void initialise() throws Exception {
        }
        public void dispose() throws Exception {
        }
    }
}
