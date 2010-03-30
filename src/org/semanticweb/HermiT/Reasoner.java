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
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.semanticweb.HermiT.Configuration.BlockingStrategyType;
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
import org.semanticweb.HermiT.model.Individual;
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
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
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
public class Reasoner implements OWLReasoner,Serializable {
    private static final long serialVersionUID=-3511564272739622311L;

    protected final Configuration m_configuration;
    protected final InterruptFlag m_interruptFlag;
    protected final ReasonerProgressMonitor m_progressMonitor;
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
    public Reasoner(Configuration configuration,OWLOntology rootOntology,Set<DescriptionGraph> descriptionGraphs) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag(configuration.individualTaskTimeout);
        m_progressMonitor=configuration.reasonerProgressMonitor;
        loadOntology(rootOntology,descriptionGraphs);
    }

    /**
     * This is mainly an internal method. Once an ontology is loaded, normalised and clausified, the resulting DLOntology object can be obtained by calling getDLOntology(), saved and reloaded later with this method so that normalisation and clausification is not done again. A default configuration can be obtained by just passing new Configuration().
     *
     * @param configuration
     *            - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param dlOntology
     *            - an ontology in HermiT's internal ontology format
     */
    public Reasoner(Configuration configuration,DLOntology dlOntology) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag(configuration.individualTaskTimeout);
        m_progressMonitor=configuration.reasonerProgressMonitor;
        loadDLOntology(dlOntology);
    }

    // Life-cycle management methods

    protected void loadOntology(OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLClausification clausifier=new OWLClausification(m_configuration);
        DLOntology dlOntology=clausifier.clausify(ontology,descriptionGraphs);
        loadDLOntology(dlOntology);
    }
    protected void loadDLOntology(DLOntology dlOntology) {
        clearState();
        m_dlOntology=dlOntology;
        m_prefixes=createPrefixes(m_dlOntology);
        m_tableau=createTableau(m_interruptFlag,m_configuration,m_dlOntology,m_prefixes);
        m_atomicConceptClassificationManager=createAtomicConceptClassificationManager(this);
        m_objectRoleClassificationManager=createObjectRoleClassificationManager(this);
        m_dataRoleClassificationManager=createDataRoleClassificationManager(this);
    }
    public void interrupt() {
        m_interruptFlag.interrupt();
    }
    public void dispose() {
        clearState();
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
        return null;
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
    }

    // General inferences

    /**
     * @param owlClass
     *            - a named OWL Class
     * @return true if the class occurs in the import closure of the loaded ontology
     */
    public boolean isDefined(OWLClass owlClass) {
        AtomicConcept atomicConcept=AtomicConcept.create(owlClass.getIRI().toString());
        return m_dlOntology.containsAtomicConcept(atomicConcept) || AtomicConcept.THING.equals(atomicConcept) || AtomicConcept.NOTHING.equals(atomicConcept);
    }
    /**
     * @param owlIndividual
     *            - a named or anonymous individual
     * @return true if the given individual occurs in the import closure of the loaded ontology
     */
    public boolean isDefined(OWLIndividual owlIndividual) {
        Individual individual;
        if (owlIndividual.isAnonymous()) {
            individual=Individual.create(owlIndividual.asOWLAnonymousIndividual().getID().toString(),false);
        }
        else {
            individual=Individual.create(owlIndividual.asOWLNamedIndividual().getIRI().toString(),true);
        }
        return m_dlOntology.containsIndividual(individual);
    }
    /**
     * @param owlObjectProperty
     *            - a named object property
     * @return true if the given object property occurs in the import closure of the loaded ontology
     */
    public boolean isDefined(OWLObjectProperty owlObjectProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlObjectProperty.getIRI().toString());
        return m_dlOntology.containsObjectRole(atomicRole) || AtomicRole.TOP_OBJECT_ROLE.equals(owlObjectProperty) || AtomicRole.BOTTOM_OBJECT_ROLE.equals(owlObjectProperty);
    }
    /**
     * @param owlDataProperty
     *            - a named data property
     * @return true if the given data property occurs in the import closure of the loaded ontology
     */
    public boolean isDefined(OWLDataProperty owlDataProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlDataProperty.getIRI().toString());
        return m_dlOntology.containsDataRole(atomicRole) || AtomicRole.TOP_DATA_ROLE.equals(atomicRole) || AtomicRole.BOTTOM_DATA_ROLE.equals(atomicRole);
    }
    public void prepareReasoner() {
        throwInconsistentOntologyExceptionIfNecessary();
        classify();
        classifyObjectProperties();
        classifyDataProperties();
        realise();
    }
    public boolean isConsistent() {
        if (m_isConsistent==null)
            m_isConsistent=getTableau().isABoxSatisfiable();
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
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        EntailmentChecker checker=new EntailmentChecker(this,factory);
        return checker.entails(axiom);
    }
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        if (m_configuration.freshEntityPolicy==FreshEntityPolicy.DISALLOW)
            for (OWLAxiom axiom : axioms)
                throwFreshEntityExceptionIfNecessary(axiom);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        EntailmentChecker checker=new EntailmentChecker(this,factory);
        return checker.entails(axioms);
    }
    protected Boolean entailsDatatypeDefinition(OWLDatatypeDefinitionAxiom axiom) {
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (m_dlOntology.hasDatatypes()) {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
            OWLDataProperty newDP=factory.getOWLDataProperty(IRI.create("internal:internalDP"));
            OWLDataRange dr=axiom.getDataRange();
            OWLDatatype dt=axiom.getDatatype();
            OWLDataIntersectionOf dr1=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dr),dt);
            OWLDataIntersectionOf dr2=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dt),dr);
            OWLDataUnionOf union=factory.getOWLDataUnionOf(dr1,dr2);
            OWLClassExpression c=factory.getOWLDataSomeValuesFrom(newDP,union);
            OWLClassAssertionAxiom ax=factory.getOWLClassAssertionAxiom(c,individualA);
            Tableau tableau=getTableau(ontologyManager,ax);
            return !tableau.isABoxSatisfiable();
        }
        else
            return false;
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
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStarted("Building the class hierarchy...");
                    ClassificationManager.ProgressMonitor<AtomicConcept> progressMonitor=new ClassificationManager.ProgressMonitor<AtomicConcept>() {
                        protected int m_processedConcepts=0;
                        public void elementClassified(AtomicConcept element) {
                            m_processedConcepts++;
                            if (m_progressMonitor!=null)
                                m_progressMonitor.reasonerTaskProgressChanged(m_processedConcepts,numRelevantConcepts);
                        }
                    };
                    m_atomicConceptHierarchy=m_atomicConceptClassificationManager.classify(progressMonitor,AtomicConcept.THING,AtomicConcept.NOTHING,relevantAtomicConcepts);
                }
                finally {
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLClass> getTopClassNode() {
        return atomicConceptsToOWLAPI(getHierarchyNode(AtomicConcept.THING).getEquivalentElements());
    }
    public Node<OWLClass> getBottomClassNode() {
        return atomicConceptsToOWLAPI(getHierarchyNode(AtomicConcept.NOTHING).getEquivalentElements());
    }
    public boolean isSatisfiable(OWLClassExpression description) {
        throwFreshEntityExceptionIfNecessary(description);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return false;
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            if (m_atomicConceptHierarchy==null)
                return m_atomicConceptClassificationManager.isSatisfiable(concept);
            else {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                return node!=m_atomicConceptHierarchy.getBottomNode();
            }
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(newClass,description);
            Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            return tableau.isSatisfiable(AtomicConcept.create("internal:query-concept"));
        }
    }

    public boolean isSubClassOf(OWLClassExpression subDescription,OWLClassExpression superDescription) {
        throwFreshEntityExceptionIfNecessary(subDescription,superDescription);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass) {
            AtomicConcept subconcept=AtomicConcept.create(((OWLClass)subDescription).getIRI().toString());
            AtomicConcept superconcept=AtomicConcept.create(((OWLClass)superDescription).getIRI().toString());
            return m_atomicConceptClassificationManager.isSubsumedBy(subconcept,superconcept);
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newSubConcept=factory.getOWLClass(IRI.create("internal:query-subconcept"));
            OWLAxiom subClassDefinitionAxiom=factory.getOWLSubClassOfAxiom(newSubConcept,subDescription);
            OWLClass newSuperConcept=factory.getOWLClass(IRI.create("internal:query-superconcept"));
            OWLAxiom superClassDefinitionAxiom=factory.getOWLSubClassOfAxiom(superDescription,newSuperConcept);
            Tableau tableau=getTableau(ontologyManager,subClassDefinitionAxiom,superClassDefinitionAxiom);
            return tableau.isSubsumedBy(AtomicConcept.create("internal:query-subconcept"),AtomicConcept.create("internal:query-superconcept"));
        }
    }

    /**
     * @param classExpression1
     *            - an OWL class expression
     * @param classExpression2
     *            - an OWL class expression
     * @return true if classExpression1 and classExpression2 are equivalent
     */
    public boolean isEquivalentClass(OWLClassExpression classExpression1,OWLClassExpression classExpression2) {
        return isSubClassOf(classExpression1,classExpression2) && isSubClassOf(classExpression2,classExpression1);
    }
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptsToOWLAPI(node.getEquivalentElements());
    }
    protected Set<HierarchyNode<AtomicConcept>> getSubClassNodes(OWLClassExpression classExpression,boolean direct,boolean inclusive) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        if (direct)
            return node.getChildNodes();
        else {
            if (inclusive)
                return node.getDescendantNodes();
            Set<HierarchyNode<AtomicConcept>> nodes=new HashSet<HierarchyNode<AtomicConcept>>(node.getDescendantNodes());
            nodes.remove(node);
            return nodes;
        }
    }
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression classExpression,boolean direct) {
        return atomicConceptNodesToOWLAPI(getSubClassNodes(classExpression,direct,false));
    }
    /**
     * @param classExpression
     *            - an OWL class expression
     * @return a set of Nodes {N1, ..., Nn} such that classes in each node Ni are equivalent to each other and (not necessarily strict) subclasses of classExpression, i.e., if classExpression is a class name, then classExpression is part of the answer
     */
    public NodeSet<OWLClass> getDescendantClasses(OWLClassExpression classExpression) {
        return atomicConceptNodesToOWLAPI(getSubClassNodes(classExpression,false,true));
    }
    protected Set<HierarchyNode<AtomicConcept>> getSuperClassNodes(OWLClassExpression classExpression,boolean direct,boolean inclusive) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        if (direct)
            return node.getParentNodes();
        else {
            if (inclusive)
                return node.getAncestorNodes();
            Set<HierarchyNode<AtomicConcept>> nodes=new HashSet<HierarchyNode<AtomicConcept>>(node.getAncestorNodes());
            nodes.remove(node);
            return nodes;
        }
    }
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression classExpression,boolean direct) {
        return atomicConceptNodesToOWLAPI(getSuperClassNodes(classExpression,direct,false));
    }
    /**
     * @param classExpression
     *            - an OWL class expression
     * @return a set of Nodes {N1, ..., Nn} such that classes in each node Ni are equivalent to each other and (not necessarily strict) superclasses of classExpression, i.e., if classExpression is a class name, then classExpression is part of the answer
     */
    public NodeSet<OWLClass> getAncestorClasses(OWLClassExpression classExpression) {
        return atomicConceptNodesToOWLAPI(getSuperClassNodes(classExpression,false,true));
    }
    public Node<OWLClass> getUnsatisfiableClasses() {
        classify();
        HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getBottomNode();
        return atomicConceptsToOWLAPI(node.getEquivalentElements());
    }
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression classExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(classExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLClassNodeSet();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Node<OWLClass> equivToDisjoint=getEquivalentClasses(factory.getOWLObjectComplementOf(classExpression));
        if (direct && equivToDisjoint.getSize()>0)
            return new OWLClassNodeSet(equivToDisjoint);
        NodeSet<OWLClass> subsDisjoint=getSubClasses(factory.getOWLObjectComplementOf(classExpression),direct);
        if (direct)
            return subsDisjoint;
        Set<Node<OWLClass>> result=new HashSet<Node<OWLClass>>();
        result.add(equivToDisjoint);
        result.addAll(subsDisjoint.getNodes());
        return new OWLClassNodeSet(result);
    }
    protected HierarchyNode<AtomicConcept> getHierarchyNode(AtomicConcept atomicConcept) {
        classify();
        HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(atomicConcept);
        if (node==null)
            node=new HierarchyNode<AtomicConcept>(atomicConcept,Collections.singleton(atomicConcept),Collections.singleton(m_atomicConceptHierarchy.getTopNode()),Collections.singleton(m_atomicConceptHierarchy.getBottomNode()));
        return node;
    }
    protected HierarchyNode<AtomicConcept> getHierarchyNode(OWLClassExpression description) {
        throwFreshEntityExceptionIfNecessary(description);
        if (!isConsistent())
            return getHierarchyNode(AtomicConcept.NOTHING);
        classify();
        if (description instanceof OWLClass) {
            AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(atomicConcept);
            if (node==null)
                node=new HierarchyNode<AtomicConcept>(atomicConcept,Collections.singleton(atomicConcept),Collections.singleton(m_atomicConceptHierarchy.getTopNode()),Collections.singleton(m_atomicConceptHierarchy.getBottomNode()));
            return node;
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(newClass,description);
            final Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            StandardClassificationManager.Relation<AtomicConcept> hierarchyRelation=new StandardClassificationManager.Relation<AtomicConcept>() {
                public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                    return tableau.isSubsumedBy(child,parent);
                }
            };
            return StandardClassificationManager.findPosition(hierarchyRelation,AtomicConcept.create("internal:query-concept"),m_atomicConceptHierarchy.getTopNode(),m_atomicConceptHierarchy.getBottomNode());
        }
    }

    // Object property inferences

    /**
     * @return true if the object property hierarchy has been computed and false otherwise
     */
    public boolean areObjectPropertiesClassified() {
        return m_objectRoleHierarchy!=null;
    }
    /**
     * Builds the object property hierarchy. With nominals and cardinality restrictions this cannot always been dome by simply building the transitive closure of the asserted object property hierarchy.
     */
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
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStarted("Classifying object properties...");
                    ClassificationManager.ProgressMonitor<Role> progressMonitor=new ClassificationManager.ProgressMonitor<Role>() {
                        protected int m_processedRoles=0;
                        public void elementClassified(Role element) {
                            m_processedRoles++;
                            if (m_progressMonitor!=null)
                                m_progressMonitor.reasonerTaskProgressChanged(m_processedRoles,numRoles);
                        }
                    };
                    m_objectRoleHierarchy=m_objectRoleClassificationManager.classify(progressMonitor,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE,allObjectRoles);
                }
                finally {
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLObjectProperty> getTopObjectPropertyNode() {
        return objectPropertiesToOWLAPI(getHierarchyNodeObjectRole(AtomicRole.TOP_OBJECT_ROLE).getEquivalentElements());
    }
    public Node<OWLObjectProperty> getBottomObjectPropertyNode() {
        return objectPropertiesToOWLAPI(getHierarchyNodeObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE).getEquivalentElements());
    }
    /**
     * Determines if subObjectPropertyExpression is a sub-property of superObjectPropertyExpression. HermiT answers true if subObjectPropertyExpression=superObjectPropertyExpression.
     *
     * @param subObjectPropertyExpression
     *            - the sub object property expression
     * @param superObjectPropertyExpression
     *            - the super object property expression
     * @return true if whenever a pair related with the property subObjectPropertyExpression is necessarily related with the property superObjectPropertyExpression and false otherwise
     */
    public boolean isSubObjectPropertyExpressionOf(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        throwFreshEntityExceptionIfNecessary(subObjectPropertyExpression,superObjectPropertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        Role subRole;
        if (subObjectPropertyExpression.getSimplified().isAnonymous())
            subRole=InverseRole.create(AtomicRole.create(subObjectPropertyExpression.getNamedProperty().getIRI().toString()));
        else
            subRole=AtomicRole.create(subObjectPropertyExpression.getNamedProperty().getIRI().toString());
        Role superRole;
        if (superObjectPropertyExpression.getSimplified().isAnonymous())
            superRole=InverseRole.create(AtomicRole.create(superObjectPropertyExpression.getNamedProperty().getIRI().toString()));
        else
            superRole=AtomicRole.create(superObjectPropertyExpression.getNamedProperty().getIRI().toString());
        return m_objectRoleClassificationManager.isSubsumedBy(subRole,superRole);
    }

    /**
     * Determines if the property chain represented by subPropertyChain is a sub-property of superObjectPropertyExpression.
     *
     * @param subPropertyChain
     *            - a list that represents a property chain
     * @param superObjectPropertyExpression
     *            - an object property expression
     * @return if r1, ..., rn is the given chain and r the given super property, then the answer is true if whenever r1(d0, d1), ..., rn(dn-1, dn) holds in a model for some elements d0, ..., dn, then necessarily r(d0, dn) holds and it is false otherwise
     */
    public boolean isSubObjectPropertyExpressionOf(List<OWLObjectPropertyExpression> subPropertyChain,OWLObjectPropertyExpression superObjectPropertyExpression) {
        for (OWLObjectPropertyExpression subProperty : subPropertyChain)
            throwFreshEntityExceptionIfNecessary(subProperty);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent() || superObjectPropertyExpression.getNamedProperty().isOWLTopObjectProperty())
            return true;
        else {
            OWLObjectPropertyExpression[] subObjectProperties=new OWLObjectPropertyExpression[subPropertyChain.size()];
            subPropertyChain.toArray(subObjectProperties);
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            OWLIndividual randomIndividual=factory.getOWLNamedIndividual(IRI.create("internal:randomIndividual"));
            OWLClassExpression owlSuperClassExpression=factory.getOWLObjectHasValue(superObjectPropertyExpression,randomIndividual);
            OWLClassExpression owlSubClassExpression=factory.getOWLObjectHasValue(subObjectProperties[subObjectProperties.length-1],randomIndividual);
            for (int i=subObjectProperties.length-2;i>=0;i--)
                owlSubClassExpression=factory.getOWLObjectSomeValuesFrom(subObjectProperties[i],owlSubClassExpression);
            return isSubClassOf(owlSubClassExpression,owlSuperClassExpression);
        }
    }
    /**
     * @param objectPropertyExpression1
     *            - an object property expression
     * @param objectPropertyExpression2
     *            - an object property expression
     * @return true if the extension of objectPropertyExpression1 is the same as the extension of objectPropertyExpression2 in each model of the ontology and false otherwise
     */
    public boolean isEquivalentObjectPropertyExpression(OWLObjectPropertyExpression objectPropertyExpression1,OWLObjectPropertyExpression objectPropertyExpression2) {
        return isSubObjectPropertyExpressionOf(objectPropertyExpression1,objectPropertyExpression2) && isSubObjectPropertyExpressionOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    protected Set<HierarchyNode<Role>> getSuperObjectPropertyNodes(OWLObjectPropertyExpression propertyExpression,boolean direct,boolean inclusive) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        if (direct)
            return node.getParentNodes();
        else {
            if (inclusive)
                return node.getAncestorNodes();
            else {
                Set<HierarchyNode<Role>> relevantNodes=node.getAncestorNodes();
                relevantNodes.remove(node);
                return relevantNodes;
            }
        }
    }
    public NodeSet<OWLObjectProperty> getSuperObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        return objectPropertyNodesToOWLAPI(getSuperObjectPropertyNodes(propertyExpression,direct,false));
    }
    /**
     * @param propertyExpression
     *            - an OWL object property expression (named or inverse)
     * @return a set of Nodes {N1, ..., Nn} such that object property NAMES in each node Ni are equivalent to each other and (not necessarily strict) super object properties of propertyExpression, i.e., if propertyExpression is a named object property, then propertyExpression is part of the answer
     */
    public NodeSet<OWLObjectProperty> getAncestorObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return objectPropertyNodesToOWLAPI(getSuperObjectPropertyNodes(propertyExpression,false,true));
    }
    /**
     * @param propertyExpression
     *            - an OWL object property expression (named or inverse)
     * @param direct
     *            - if true, then only the strict and direct super object properties EXPRESSIONs are returned otherwise strict but possibly indirect super object property EXPRESSIONs are returned
     * @return a set of sets {S1, ..., Sn} such that object property EXPRESSIONS in each set Si are equivalent to each other and strict super object properties expressions of propertyExpression, i.e., if propertyExpression is not part of the answer
     */
    public Set<Set<OWLObjectPropertyExpression>> getSuperObjectPropertyExpressions(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        return objectRoleNodesToSetOfSetsOfObjectPropertyExpressions(getSuperObjectPropertyNodes(propertyExpression,direct,false));
    }
    /**
     * @param propertyExpression
     *            - an OWL object property expression (named or inverse)
     * @return a set of sets {S1, ..., Sn} such that object property EXPRESSIONs in each set Si are equivalent to each other and (not necessarily strict) super object properties EXPRESSIONS of propertyExpression, i.e., propertyExpression is part of the answer
     */
    public Set<Set<OWLObjectPropertyExpression>> getAncestorObjectPropertyExpressions(OWLObjectPropertyExpression propertyExpression) {
        return objectRoleNodesToSetOfSetsOfObjectPropertyExpressions(getSuperObjectPropertyNodes(propertyExpression,false,true));
    }
    protected Set<HierarchyNode<Role>> getSubObjectPropertyNodes(OWLObjectPropertyExpression propertyExpression,boolean direct,boolean inclusive) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        if (direct)
            return node.getChildNodes();
        else {
            if (inclusive)
                return node.getDescendantNodes();
            else {
                Set<HierarchyNode<Role>> relevantNodes=node.getDescendantNodes();
                relevantNodes.remove(node);
                return relevantNodes;
            }
        }
    }
    public NodeSet<OWLObjectProperty> getSubObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        return objectPropertyNodesToOWLAPI(getSubObjectPropertyNodes(propertyExpression,direct,false));
    }
    /**
     * @param propertyExpression
     *            - an OWL object property expression (named or inverse)
     * @return a set of Nodes {N1, ..., Nn} such that object property NAMES in each node Ni are equivalent to each other and (not necessarily strict) sub object properties of propertyExpression, i.e., if propertyExpression is a named object property, then propertyExpression is part of the answer
     */
    public NodeSet<OWLObjectProperty> getDescendantObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return objectPropertyNodesToOWLAPI(getSubObjectPropertyNodes(propertyExpression,false,true));
    }
    public Set<Set<OWLObjectPropertyExpression>> getSubObjectPropertyExpressions(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        return objectRoleNodesToSetOfSetsOfObjectPropertyExpressions(getSubObjectPropertyNodes(propertyExpression,direct,false));
    }
    /**
     * @param propertyExpression
     *            - an OWL object property expression (named or inverse)
     * @return a set of sets {S1, ..., Sn} such that object property EXPRESSIONs in each set Si are equivalent to each other and (not necessarily strict) sub object properties EXPRESSIONS of propertyExpression, i.e., propertyExpression is part of the answer
     */
    public Set<Set<OWLObjectPropertyExpression>> getDescendantObjectPropertyExpressions(OWLObjectPropertyExpression propertyExpression) {
        return objectRoleNodesToSetOfSetsOfObjectPropertyExpressions(getSubObjectPropertyNodes(propertyExpression,false,true));
    }
    public Node<OWLObjectProperty> getEquivalentObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return objectPropertiesToOWLAPI(getHierarchyNode(propertyExpression).getEquivalentElements());
    }
    /**
     * @param propertyExpression
     *            - an object property expression (named or inverse)
     * @return a set of object property expressions such that each object property expression ope in the set is equivalent to propertyExpression
     */
    public Set<OWLObjectPropertyExpression> getEquivalentObjectPropertyExpressions(OWLObjectPropertyExpression propertyExpression) {
        return objectRolesToObjectPropertyExpressions(getHierarchyNode(propertyExpression).getEquivalentElements());
    }
    protected HierarchyNode<Role> getHierarchyNodeObjectRole(Role role) {
        classifyObjectProperties();
        HierarchyNode<Role> node=m_objectRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_objectRoleHierarchy.getTopNode()),Collections.singleton(m_objectRoleHierarchy.getBottomNode()));
        return node;
    }
    protected HierarchyNode<Role> getHierarchyNode(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return getHierarchyNodeObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE);
        Role role;
        if (propertyExpression.getSimplified().isAnonymous())
            role=InverseRole.create(AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString()));
        else
            role=AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString());
        return getHierarchyNodeObjectRole(role);
    }
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        HierarchyNode<AtomicConcept> node=getHierarchyNode(factory.getOWLObjectSomeValuesFrom(propertyExpression,factory.getOWLThing()));
        Set<HierarchyNode<AtomicConcept>> nodes;
        if (direct)
            nodes=node.getParentNodes();
        else
            nodes=node.getAncestorNodes();
        return atomicConceptNodesToOWLAPI(nodes);
    }
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        HierarchyNode<AtomicConcept> node=getHierarchyNode(factory.getOWLObjectSomeValuesFrom(propertyExpression.getInverseProperty(),factory.getOWLThing()));
        Set<HierarchyNode<AtomicConcept>> nodes;
        if (direct)
            nodes=node.getParentNodes();
        else
            nodes=node.getAncestorNodes();
        return atomicConceptNodesToOWLAPI(nodes);
    }
    public Node<OWLObjectProperty> getInverseObjectProperties(OWLObjectPropertyExpression propertyExpression) {
        return getEquivalentObjectProperties(propertyExpression.getInverseProperty());
    }
    public NodeSet<OWLObjectProperty> getDisjointObjectProperties(OWLObjectPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLObjectPropertyNodeSet();
        Set<HierarchyNode<Role>> result=new HashSet<HierarchyNode<Role>>();
        if (propertyExpression.getNamedProperty().isOWLTopObjectProperty()) {
            result.add(getHierarchyNodeObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE));
            return objectPropertyNodesToOWLAPI(result);
        }
        else if (propertyExpression.isOWLBottomObjectProperty()) {
            HierarchyNode<Role> node=getHierarchyNodeObjectRole(AtomicRole.TOP_OBJECT_ROLE);
            result.add(node);
            if (!direct)
                result.addAll(node.getDescendantNodes());
            return objectPropertyNodesToOWLAPI(result);
        }
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom assertion=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,individualA,individualB);
        OWLAxiom assertion2;
        OWLObjectProperty testProperty;
        Set<HierarchyNode<Role>> nodesToTest=new HashSet<HierarchyNode<Role>>();
        nodesToTest.addAll(getHierarchyNodeObjectRole(AtomicRole.TOP_OBJECT_ROLE).getChildNodes());
        while (!nodesToTest.isEmpty()) {
            HierarchyNode<Role> nodeToTest=nodesToTest.iterator().next();
            Role roleToTest=nodeToTest.getRepresentative();
            testProperty=factory.getOWLObjectProperty(IRI.create(roleToTest.toString()));
            assertion2=factory.getOWLObjectPropertyAssertionAxiom(testProperty,individualA,individualB);
            Tableau tableau=getTableau(ontologyManager,assertion,assertion2);
            if (!tableau.isABoxSatisfiable()) {
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
            result.add(getHierarchyNodeObjectRole(AtomicRole.BOTTOM_OBJECT_ROLE));
        return objectPropertyNodesToOWLAPI(result);
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if each individual can have at most one outgoing connection of the specified object property expression
     */
    public boolean isFunctional(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,propertyExpression));
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if each individual can have at most one incoming connection of the specified object property expression
     */
    public boolean isInverseFunctional(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,propertyExpression.getInverseProperty()));
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if the extension of propertyExpression is an irreflexive relation in each model of the loaded ontology
     */
    public boolean isIrreflexive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectHasSelf(propertyExpression));
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if the extension of propertyExpression is a reflexive relation in each model of the loaded ontology
     */
    public boolean isReflexive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectComplementOf(factory.getOWLObjectHasSelf(propertyExpression)));
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if the extension of propertyExpression is an asymmetric relation in each model of the loaded ontology
     */
    public boolean isAsymmetric(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom assertion1=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,individualA,individualB);
        OWLAxiom assertion2=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression.getInverseProperty(),individualA,individualB);
        Tableau tableau=getTableau(ontologyManager,assertion1,assertion2);
        return !tableau.isABoxSatisfiable();
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if the extension of propertyExpression is a symmetric relation in each model of the loaded ontology
     */
    public boolean isSymmetric(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent() || propertyExpression.getNamedProperty().isOWLTopObjectProperty())
            return true;
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom assertion1=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression.getNamedProperty(),individualA,individualB);
        OWLObjectAllValuesFrom all=factory.getOWLObjectAllValuesFrom(propertyExpression.getNamedProperty(),factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(individualA)));
        OWLAxiom assertion2=factory.getOWLClassAssertionAxiom(all,individualB);
        Tableau tableau=getTableau(ontologyManager,assertion1,assertion2);
        return !tableau.isABoxSatisfiable();
    }
    /**
     * @param propertyExpression
     *            - an object property expression
     * @return true if the extension of propertyExpression is a transitive relation in each model of the loaded ontology
     */
    public boolean isTransitive(OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        List<OWLObjectPropertyExpression> chain=new ArrayList<OWLObjectPropertyExpression>();
        chain.add(propertyExpression);
        chain.add(propertyExpression);
        return isSubObjectPropertyExpressionOf(chain,propertyExpression);
    }

    // Data property inferences

    /**
     * @return true if the data property hierarchy has been computed and false otherwise
     */
    public boolean areDataPropertiesClassified() {
        return m_dataRoleHierarchy!=null;
    }

    /**
     * Builds the data property hierarchy.
     */
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
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStarted("Classifying data properties...");
                    ClassificationManager.ProgressMonitor<Role> progressMonitor=new ClassificationManager.ProgressMonitor<Role>() {
                        protected int m_processedRoles=0;
                        public void elementClassified(Role element) {
                            m_processedRoles++;
                            if (m_progressMonitor!=null)
                                m_progressMonitor.reasonerTaskProgressChanged(m_processedRoles,numRoles);
                        }
                    };
                    m_dataRoleHierarchy=m_dataRoleClassificationManager.classify(progressMonitor,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE,allDataRoles);
                }
                finally {
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskStopped();
                }
            }
        }
    }
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return dataPropertiesToOWLAPI(getHierarchyNodeDataRole(AtomicRole.TOP_DATA_ROLE).getEquivalentElements());
    }
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return dataPropertiesToOWLAPI(getHierarchyNodeDataRole(AtomicRole.BOTTOM_DATA_ROLE).getEquivalentElements());
    }
    /**
     * Determines if the property chain represented by subPropertyChain is a sub-property of superObjectPropertyExpression.
     *
     * @param subDataProperty
     *            - a data property
     * @param superDataProperty
     *            - a data property
     * @return true if whenever a pair related with the property subDataProperty is necessarily related with the property superDataProperty and false otherwise
     */
    public boolean isSubDataPropertyOf(OWLDataProperty subDataProperty,OWLDataProperty superDataProperty) {
        throwFreshEntityExceptionIfNecessary(subDataProperty,superDataProperty);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (m_dlOntology.hasDatatypes()) {
            // classifyDataProperties();
            return m_dataRoleClassificationManager.isSubsumedBy(AtomicRole.create(subDataProperty.getIRI().toString()),AtomicRole.create(superDataProperty.getIRI().toString()));
        }
        else
            return subDataProperty.isOWLBottomDataProperty() || superDataProperty.isOWLTopDataProperty();
    }
    /**
     * @param dataProperty1
     *            - a data property
     * @param dataProperty2
     *            - a data property
     * @return true if the extension of dataProperty1 is the same as the extension of dataProperty2 in each model of the ontology and false otherwise
     */
    public boolean isEquivalentDataProperty(OWLDataProperty dataProperty1,OWLDataProperty dataProperty2) {
        return isSubDataPropertyOf(dataProperty1,dataProperty2) && isSubDataPropertyOf(dataProperty2,dataProperty1);
    }
    protected Set<HierarchyNode<Role>> getSuperDataPropertyNodes(OWLDataProperty property,boolean direct,boolean inclusive) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        if (direct)
            return node.getParentNodes();
        else {
            if (inclusive)
                return node.getAncestorNodes();
            else {
                Set<HierarchyNode<Role>> nodes=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
                nodes.remove(node);
                return nodes;
            }
        }
    }
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty property,boolean direct) {
        return dataPropertyNodesToOWLAPI(getSuperDataPropertyNodes(property,direct,false));
    }
    /**
     * @param property
     *            - an OWL data property
     * @return a set of Nodes {N1, ..., Nn} such that data properties in each node Ni are equivalent to each other and (not necessarily strict) super data properties of property, i.e., property is part of the answer
     */
    public NodeSet<OWLDataProperty> getAncestorDataProperties(OWLDataProperty property) {
        return dataPropertyNodesToOWLAPI(getSuperDataPropertyNodes(property,false,true));
    }
    protected Set<HierarchyNode<Role>> getSubDataPropertyNodes(OWLDataProperty property,boolean direct,boolean inclusive) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        if (direct)
            return node.getChildNodes();
        else {
            if (inclusive)
                return node.getDescendantNodes();
            else {
                Set<HierarchyNode<Role>> nodes=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
                nodes.remove(node);
                return nodes;
            }
        }
    }
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty property,boolean direct) {
        return dataPropertyNodesToOWLAPI(getSubDataPropertyNodes(property,direct,false));
    }
    /**
     * @param property
     *            - an OWL data property
     * @return a set of Nodes {N1, ..., Nn} such that data properties in each node Ni are equivalent to each other and (not necessarily strict) sub data properties of property, i.e., property is part of the answer
     */
    public NodeSet<OWLDataProperty> getDescendantDataProperties(OWLDataProperty property) {
        return dataPropertyNodesToOWLAPI(getSubDataPropertyNodes(property,false,true));
    }
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty property) {
        return dataPropertiesToOWLAPI(getHierarchyNode(property).getEquivalentElements());
    }
    protected HierarchyNode<Role> getHierarchyNodeDataRole(Role role) {
        classifyDataProperties();
        HierarchyNode<Role> node=m_dataRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_dataRoleHierarchy.getTopNode()),Collections.singleton(m_dataRoleHierarchy.getBottomNode()));
        return node;
    }
    protected HierarchyNode<Role> getHierarchyNode(OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(property);
        return getHierarchyNodeDataRole(AtomicRole.create(property.getIRI().toString()));
    }
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty property,boolean direct) {
        throwFreshEntityExceptionIfNecessary(property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        if (m_dlOntology.hasDatatypes()) {
            HierarchyNode<AtomicConcept> node=getHierarchyNode(factory.getOWLDataSomeValuesFrom(property,factory.getTopDatatype()));
            Set<HierarchyNode<AtomicConcept>> nodes;
            if (direct)
                nodes=node.getParentNodes();
            else
                nodes=node.getAncestorNodes();
            return atomicConceptNodesToOWLAPI(nodes);
        }
        else
            return new OWLClassNodeSet(new OWLClassNode(factory.getOWLThing()));
    }
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual namedIndividual,OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(namedIndividual,property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            throw new InconsistentOntologyException();
        if (m_dlOntology.hasDatatypes()) {
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            Set<OWLLiteral> result=new HashSet<OWLLiteral>();
            Individual ind=Individual.create(namedIndividual.getIRI().toString(),true);
            for (OWLDataProperty dp : getDescendantDataProperties(property).getFlattened()) {
                AtomicRole role=AtomicRole.create(dp.getIRI().toString());
                Map<Individual,Set<Constant>> dataPropertyAssertions=m_dlOntology.getDataPropertyAssertions().get(role);
                if (dataPropertyAssertions!=null) {
                    for (Constant constant : dataPropertyAssertions.get(ind)) {
                        URI datatypeURI=constant.getDatatypeURI();
                        if (datatypeURI!=null)
                            result.add(factory.getOWLTypedLiteral(constant.getDataValue().toString(),factory.getOWLDatatype(IRI.create(datatypeURI))));
                        else
                            result.add(factory.getOWLStringLiteral(constant.getDataValue().toString()));
                    }
                }
            }
            return result;
        }
        else
            return new HashSet<OWLLiteral>();
    }
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression propertyExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(propertyExpression);
        if (m_dlOntology.hasDatatypes()) {
            classifyDataProperties();
            if (!isConsistent())
                return new OWLDataPropertyNodeSet();
            Set<HierarchyNode<Role>> result=new HashSet<HierarchyNode<Role>>();
            if (propertyExpression.isOWLTopDataProperty()) {
                result.add(getHierarchyNodeDataRole(AtomicRole.BOTTOM_DATA_ROLE));
                return dataPropertyNodesToOWLAPI(result);
            }
            else if (propertyExpression.isOWLBottomDataProperty()) {
                HierarchyNode<Role> node=getHierarchyNodeDataRole(AtomicRole.TOP_DATA_ROLE);
                result.add(node);
                if (!direct)
                    result.addAll(node.getDescendantNodes());
                return dataPropertyNodesToOWLAPI(result);
            }
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLIndividual individual=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant",anonymousConstantsDatatype);
            OWLDataProperty property=propertyExpression.asOWLDataProperty();
            OWLAxiom assertion=factory.getOWLDataPropertyAssertionAxiom(property,individual,constant);
            OWLAxiom assertion2;
            OWLDataProperty testProperty;
            Set<HierarchyNode<Role>> nodesToTest=new HashSet<HierarchyNode<Role>>();
            nodesToTest.addAll(getHierarchyNodeDataRole(AtomicRole.TOP_DATA_ROLE).getChildNodes());
            while (!nodesToTest.isEmpty()) {
                HierarchyNode<Role> nodeToTest=nodesToTest.iterator().next();
                Role roleToTest=nodeToTest.getRepresentative();
                testProperty=factory.getOWLDataProperty(IRI.create(roleToTest.toString()));
                assertion2=factory.getOWLDataPropertyAssertionAxiom(testProperty,individual,constant);
                Tableau tableau=getTableau(ontologyManager,assertion,assertion2);
                if (!tableau.isABoxSatisfiable()) {
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
                result.add(getHierarchyNodeDataRole(AtomicRole.BOTTOM_DATA_ROLE));
            return dataPropertyNodesToOWLAPI(result);
        }
        else {
            throwInconsistentOntologyExceptionIfNecessary();
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            if (propertyExpression.isOWLTopDataProperty() && isConsistent())
                return new OWLDataPropertyNodeSet(new OWLDataPropertyNode(factory.getOWLBottomDataProperty()));
            else if (propertyExpression.isOWLBottomDataProperty() && isConsistent())
                return new OWLDataPropertyNodeSet(new OWLDataPropertyNode(factory.getOWLTopDataProperty()));
            else
                return new OWLDataPropertyNodeSet();
        }
    }
    /**
     * @param property
     *            - a data property
     * @return true if each individual can have at most one outgoing connection of the specified data property
     */
    public boolean isFunctional(OWLDataProperty property) {
        throwFreshEntityExceptionIfNecessary(property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLDataMinCardinality(2,property));
    }

    // Individual inferences

    /**
     * @return true if instances of the named classes in the loaded ontology have been computed
     */
    public boolean isRealised() {
        return m_realization!=null;
    }
    /**
     * Compute, for each named class C in the loaded ontology, the instances of C.
     */
    public void realise() {
        if (m_realization==null) {
            throwInconsistentOntologyExceptionIfNecessary();
            if (!isConsistent()) {
                if (m_progressMonitor!=null)
                    m_progressMonitor.reasonerTaskStarted("Computing instances for all classes...");
                m_realization=new HashMap<AtomicConcept,Set<Individual>>();
                int numConcepts=m_dlOntology.getAllAtomicConcepts().size();
                int currentConcept=0;
                Set<Individual> individuals=m_dlOntology.getAllIndividuals();
                for (AtomicConcept directSuperConcept : m_dlOntology.getAllAtomicConcepts()) {
                    currentConcept++;
                    if (!Prefixes.isInternalIRI(directSuperConcept.getIRI()))
                        m_realization.put(directSuperConcept,individuals);
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskProgressChanged(currentConcept,numConcepts);
                }
                if (m_progressMonitor!=null)
                    m_progressMonitor.reasonerTaskStopped();
            }
            else {
                m_realization=new HashMap<AtomicConcept,Set<Individual>>();
                int numIndividuals=m_dlOntology.getAllIndividuals().size();
                if (m_atomicConceptHierarchy==null && numIndividuals>0)
                    classify();
                int currentIndividual=0;
                if (m_progressMonitor!=null)
                    m_progressMonitor.reasonerTaskStarted("Computing instances for all classes...");
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
                    if (m_progressMonitor!=null)
                        m_progressMonitor.reasonerTaskProgressChanged(currentIndividual,numIndividuals);
                }
                if (m_progressMonitor!=null)
                    m_progressMonitor.reasonerTaskStopped();
            }
        }
    }
    protected Set<HierarchyNode<AtomicConcept>> getDirectSuperConceptNodes(final Individual individual) {
        classify();
        if (!isConsistent())
            return Collections.singleton(getHierarchyNode(AtomicConcept.NOTHING));
        StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>> predicate=new StandardClassificationManager.SearchPredicate<HierarchyNode<AtomicConcept>>() {
            public Set<HierarchyNode<AtomicConcept>> getSuccessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getChildNodes();
            }
            public Set<HierarchyNode<AtomicConcept>> getPredecessorElements(HierarchyNode<AtomicConcept> u) {
                return u.getParentNodes();
            }
            public boolean trueOf(HierarchyNode<AtomicConcept> u) {
                AtomicConcept atomicConcept=u.getEquivalentElements().iterator().next();
                if (AtomicConcept.THING.equals(atomicConcept))
                    return true;
                else
                    return getTableau().isInstanceOf(atomicConcept,individual);
            }
        };
        Set<HierarchyNode<AtomicConcept>> topPositions=Collections.singleton(m_atomicConceptHierarchy.getTopNode());
        return StandardClassificationManager.search(predicate,topPositions,null);
    }
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual namedIndividual) {
        throwFreshEntityExceptionIfNecessary(namedIndividual);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLNamedIndividualNode(individualsToNamedIndividuals(m_dlOntology.getAllIndividuals()));
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        NodeSet<OWLNamedIndividual> result=getInstances(factory.getOWLObjectOneOf(namedIndividual),false);
        assert result.isSingleton();
        return result.iterator().next();
    }
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual namedIndividual) {
        throwFreshEntityExceptionIfNecessary(namedIndividual);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLNamedIndividualNodeSet();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getInstances(factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(namedIndividual)),false);
    }
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual owlIndividual,boolean direct) {
        throwFreshEntityExceptionIfNecessary(owlIndividual);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLClassNodeSet(getBottomClassNode());
        Individual individual=Individual.create(owlIndividual.getIRI().toString(),true);
        Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(individual);
        Set<HierarchyNode<AtomicConcept>> result=new HashSet<HierarchyNode<AtomicConcept>>(directSuperConceptNodes);
        if (!direct)
            for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes)
                result.addAll(directSuperConceptNode.getAncestorNodes());
        return atomicConceptNodesToOWLAPI(result);
    }
    /**
     * @param owlIndividual
     *            - an OWL named individual
     * @param type
     *            - an OWL class expression
     * @param direct
     *            - true if only most specific classes should be considered and false if also indirect types should be considered
     * @return true if owlIndividual is an instance of the class type and either direct is false or there is no class C such that owlIndividual is an instance of C and C is a subclass of type
     */
    public boolean hasType(OWLNamedIndividual owlIndividual,OWLClassExpression type,boolean direct) {
        throwFreshEntityExceptionIfNecessary(owlIndividual,type);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        if (direct || isRealised())
            return getInstances(type,direct).containsEntity(owlIndividual);
        else {
            Individual individual=Individual.create(owlIndividual.getIRI().toString(),false);
            if (type instanceof OWLClass) {
                AtomicConcept concept=AtomicConcept.create(((OWLClass)type).getIRI().toString());
                return getTableau().isInstanceOf(concept,individual);
            }
            else {
                OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
                OWLDataFactory factory=ontologyManager.getOWLDataFactory();
                OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
                OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(type,newClass);
                Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
                return tableau.isInstanceOf(AtomicConcept.create("internal:query-concept"),individual);
            }
        }
    }
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression classExpression,boolean direct) {
        throwFreshEntityExceptionIfNecessary(classExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLNamedIndividualNodeSet();
        realise();
        Set<Node<OWLNamedIndividual>> result=new HashSet<Node<OWLNamedIndividual>>();
        if (classExpression instanceof OWLClass) {
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            AtomicConcept concept=AtomicConcept.create(((OWLClass)classExpression).getIRI().toString());
            Set<Individual> instances=m_realization.get(concept);
            if (instances!=null)
                for (Individual instance : instances)
                    if (instance.isNamed() && !Prefixes.isInternalIRI(instance.getIRI()))
                        result.add(new OWLNamedIndividualNode(factory.getOWLNamedIndividual(IRI.create(instance.getIRI()))));
            if (!direct) {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                if (node!=null)
                    for (HierarchyNode<AtomicConcept> descendantNode : node.getDescendantNodes())
                        loadIndividualsOfNode(descendantNode,result);
            }
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(classExpression,newClass);
            Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            AtomicConcept queryConcept=AtomicConcept.create("internal:query-concept");
            HierarchyNode<AtomicConcept> hierarchyNode=getHierarchyNode(classExpression);
            loadIndividualsOfNode(hierarchyNode,result);
            if (!direct)
                for (HierarchyNode<AtomicConcept> descendantNode : hierarchyNode.getDescendantNodes())
                    loadIndividualsOfNode(descendantNode,result);
            Set<HierarchyNode<AtomicConcept>> visitedNodes=new HashSet<HierarchyNode<AtomicConcept>>(hierarchyNode.getChildNodes());
            List<HierarchyNode<AtomicConcept>> toVisit=new ArrayList<HierarchyNode<AtomicConcept>>(hierarchyNode.getParentNodes());
            while (!toVisit.isEmpty()) {
                HierarchyNode<AtomicConcept> node=toVisit.remove(toVisit.size()-1);
                if (visitedNodes.add(node)) {
                    AtomicConcept nodeAtomicConcept=node.getEquivalentElements().iterator().next();
                    Set<Individual> realizationForNodeConcept=m_realization.get(nodeAtomicConcept);
                    if (realizationForNodeConcept!=null)
                        for (Individual individual : realizationForNodeConcept)
                            if (tableau.isInstanceOf(queryConcept,individual) && individual.isNamed() && !Prefixes.isInternalIRI(individual.getIRI()))
                                result.add(new OWLNamedIndividualNode(factory.getOWLNamedIndividual(IRI.create(individual.getIRI()))));
                    toVisit.addAll(node.getChildNodes());
                }
            }
        }
        if (m_configuration.individualNodeSetPolicy==IndividualNodeSetPolicy.BY_SAME_AS) {
            // group the individuals by same as equivalence classes
            Set<Node<OWLNamedIndividual>> groupedResult=new HashSet<Node<OWLNamedIndividual>>();
            while (!result.isEmpty()) {
                Node<OWLNamedIndividual> individualNode=result.iterator().next();
                assert individualNode.getSize()==1;
                Set<OWLNamedIndividual> sameIndividuals=getSameAsInstances(individualNode.getRepresentativeElement()).getFlattened();
                for (OWLNamedIndividual namedIndividual : sameIndividuals)
                    result.remove(new OWLNamedIndividualNode(namedIndividual));
                groupedResult.add(new OWLNamedIndividualNode(sameIndividuals));
            }
            return new OWLNamedIndividualNodeSet(groupedResult);
        }
        else
            return new OWLNamedIndividualNodeSet(result);
    }
    protected NodeSet<OWLNamedIndividual> getSameAsInstances(OWLNamedIndividual namedIndividual) {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
        OWLClassExpression description=factory.getOWLObjectOneOf(namedIndividual);
        OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(description,newClass);
        Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
        AtomicConcept queryConcept=AtomicConcept.create("internal:query-concept");
        HierarchyNode<AtomicConcept> hierarchyNode=getHierarchyNode(description);
        Set<Node<OWLNamedIndividual>> result=new HashSet<Node<OWLNamedIndividual>>();
        loadIndividualsOfNode(hierarchyNode,result);
        for (HierarchyNode<AtomicConcept> descendantNode : hierarchyNode.getDescendantNodes())
            loadIndividualsOfNode(descendantNode,result);
        Set<HierarchyNode<AtomicConcept>> visitedNodes=new HashSet<HierarchyNode<AtomicConcept>>(hierarchyNode.getChildNodes());
        List<HierarchyNode<AtomicConcept>> toVisit=new ArrayList<HierarchyNode<AtomicConcept>>(hierarchyNode.getParentNodes());
        while (!toVisit.isEmpty()) {
            HierarchyNode<AtomicConcept> node=toVisit.remove(toVisit.size()-1);
            if (visitedNodes.add(node)) {
                AtomicConcept nodeAtomicConcept=node.getEquivalentElements().iterator().next();
                Set<Individual> realizationForNodeConcept=m_realization.get(nodeAtomicConcept);
                if (realizationForNodeConcept!=null) {
                    for (Individual individual : realizationForNodeConcept) {
                        if (individual.isNamed() && tableau.isInstanceOf(queryConcept,individual) && !Prefixes.isInternalIRI(individual.getIRI())) {
                            OWLNamedIndividual owlIndividual=factory.getOWLNamedIndividual(IRI.create(individual.getIRI()));
                            result.add(new OWLNamedIndividualNode(owlIndividual));
                        }
                    }
                }
                toVisit.addAll(node.getChildNodes());
            }
        }
        return new OWLNamedIndividualNodeSet(result);
    }
    protected void loadIndividualsOfNode(HierarchyNode<AtomicConcept> node,Set<Node<OWLNamedIndividual>> result) {
        AtomicConcept atomicConcept=node.getEquivalentElements().iterator().next();
        Set<Individual> realizationForConcept=m_realization.get(atomicConcept);
        // RealizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null) {
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            for (Individual individual : realizationForConcept)
                if (individual.isNamed() && !Prefixes.isInternalIRI(individual.getIRI()))
                    result.add(new OWLNamedIndividualNode(factory.getOWLNamedIndividual(IRI.create(individual.getIRI()))));
        }
    }
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual namedIndividual,OWLObjectPropertyExpression propertyExpression) {
        throwFreshEntityExceptionIfNecessary(namedIndividual,propertyExpression);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return new OWLNamedIndividualNodeSet();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getInstances(factory.getOWLObjectSomeValuesFrom(propertyExpression.getInverseProperty(),factory.getOWLObjectOneOf(namedIndividual)),false);
    }
    /**
     * @param subject
     *            - a named OWL individual
     * @param property
     *            - an OWL object property expression
     * @param object
     *            - a named OWL individual
     * @return true if the pair (subject, object) is an instance of property in each model of the loaded ontology
     */
    public boolean hasObjectPropertyRelationship(OWLNamedIndividual subject,OWLObjectPropertyExpression property,OWLNamedIndividual object) {
        throwFreshEntityExceptionIfNecessary(subject,property,object);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLObjectSomeValuesFrom(property,factory.getOWLObjectOneOf(object)),false);
    }
    /**
     * @param subject
     *            - a named OWL individual
     * @param property
     *            - an OWL object property expression
     * @param object
     *            - an OWL data literal
     * @return true if the pair (subject, object) is an instance of property in each model of the loaded ontology
     */
    public boolean hasDataPropertyRelationship(OWLNamedIndividual subject,OWLDataProperty property,OWLLiteral object) {
        throwFreshEntityExceptionIfNecessary(subject,property);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLDataHasValue(property,object),false);
    }

    // other inferences

    protected Boolean hasKey(OWLHasKeyAxiom key) {
        throwFreshEntityExceptionIfNecessary(key);
        throwInconsistentOntologyExceptionIfNecessary();
        if (!isConsistent())
            return true;
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLClassAssertionAxiom(key.getClassExpression(),individualA));
        axioms.add(factory.getOWLClassAssertionAxiom(key.getClassExpression(),individualB));
        int i=0;
        for (OWLObjectPropertyExpression p : key.getObjectPropertyExpressions()) {
            OWLIndividual tmp=factory.getOWLNamedIndividual(IRI.create("internal:individual"+i));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p,individualA,tmp));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p,individualB,tmp));
            i++;
        }
        for (OWLDataPropertyExpression p : key.getDataPropertyExpressions()) {
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant"+i,anonymousConstantsDatatype);
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p,individualA,constant));
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p,individualB,constant));
            i++;
        }
        axioms.add(factory.getOWLDifferentIndividualsAxiom(individualA,individualB));
        Tableau tableau=getTableau(ontologyManager,axioms.toArray(new OWLAxiom[axioms.size()]));
        return !tableau.isABoxSatisfiable();
    }

    // Various creation methods

    public Tableau getTableau() {
        m_tableau.clearAdditionalAxioms();
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
    public Tableau getTableau(OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        if (additionalAxioms==null || additionalAxioms.length==0)
            return getTableau();
        else {
            DLOntology deltaDLOntology=createDeltaDLOntology(m_configuration,m_dlOntology,ontologyManager,additionalAxioms);
            if (isSecondStrictlyMoreExpressive(m_dlOntology,deltaDLOntology)) {
                DLOntology mergedDLOntology=mergeDLOntologies(m_configuration,m_prefixes,"uri:urn:internal-kb",m_dlOntology,deltaDLOntology);
                return createTableau(m_interruptFlag,m_configuration,mergedDLOntology,m_prefixes);
            }
            else {
                m_tableau.setAdditionalAxioms(deltaDLOntology.getDLClauses(),deltaDLOntology.getPositiveFacts(),deltaDLOntology.getNegativeFacts());
                return m_tableau;
            }
        }
    }
    protected boolean isSecondStrictlyMoreExpressive(DLOntology first,DLOntology second) {
        return
            (!first.hasInverseRoles() && second.hasInverseRoles()) ||
            (!first.hasAtMostRestrictions() && second.hasAtMostRestrictions()) ||
            (!first.hasNominals() && second.hasNominals()) ||
            (!first.hasDatatypes() && second.hasDatatypes()) ||
            (first.isHorn() && !second.isHorn());
    }
    protected static Tableau createTableau(InterruptFlag interruptFlag,Configuration config,DLOntology dlOntology,Prefixes prefixes) throws IllegalArgumentException {
        if (config.checkClauses) {
            Collection<DLClause> nonAdmissibleDLClauses=dlOntology.getNonadmissibleDLClauses();
            if (!nonAdmissibleDLClauses.isEmpty()) {
                String CRLF=System.getProperty("line.separator");
                StringBuffer buffer=new StringBuffer();
                buffer.append("The following DL-clauses in the DL-ontology are not admissible:");
                buffer.append(CRLF);
                for (DLClause dlClause : nonAdmissibleDLClauses) {
                    buffer.append(dlClause.toString(prefixes));
                    buffer.append(CRLF);
                }
                throw new IllegalArgumentException(buffer.toString());
            }
        }

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
            if ((config.blockingStrategyType==BlockingStrategyType.OPTIMAL && dlOntology.hasNominals()) || config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedSingleDirectBlockingChecker(dlOntology.hasInverseRoles());
            else {
                if (dlOntology.hasInverseRoles())
                    directBlockingChecker=new PairWiseDirectBlockingChecker();
                else
                    directBlockingChecker=new SingleDirectBlockingChecker();
            }
            break;
        case SINGLE:
            if (config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedSingleDirectBlockingChecker(dlOntology.hasInverseRoles());
            else
                directBlockingChecker=new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
            if (config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)
                directBlockingChecker=new ValidatedPairwiseDirectBlockingChecker(dlOntology.hasInverseRoles());
            else
                directBlockingChecker=new PairWiseDirectBlockingChecker();
            break;
        default:
            throw new IllegalArgumentException("Unknown direct blocking type.");
        }

        BlockingSignatureCache blockingSignatureCache=null;
        if (!dlOntology.hasNominals() && !(config.blockingStrategyType==BlockingStrategyType.SIMPLE_CORE || config.blockingStrategyType==BlockingStrategyType.COMPLEX_CORE)) {
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
            blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,dlOntology.hasInverseRoles(),true);
            break;
        case COMPLEX_CORE:
            blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,dlOntology.hasInverseRoles(),false);
            break;
        case OPTIMAL:
            if (dlOntology.hasNominals())
                blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,dlOntology.hasInverseRoles(),true);
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
        return new Tableau(interruptFlag,tableauMonitor,existentialsExpansionStrategy,config.useDisjunctionLearning,dlOntology,config.parameters);
    }

    protected ClassificationManager<AtomicConcept> createAtomicConceptClassificationManager(Reasoner reasoner) {
        if (reasoner.getTableau().isDeterministic())
            return new DeterministicClassificationManager<AtomicConcept>(new AtomicConceptSubsumptionCache(reasoner));
        else
            return new QuasiOrderClassificationManager(reasoner,m_dlOntology);
    }

    protected static ClassificationManager<Role> createObjectRoleClassificationManager(Reasoner reasoner) {
        if (reasoner.getTableau().isDeterministic())
            return new DeterministicClassificationManager<Role>(new ObjectRoleSubsumptionCache(reasoner));
        else
            return new StandardClassificationManager<Role>(new ObjectRoleSubsumptionCache(reasoner));
    }

    protected static ClassificationManager<Role> createDataRoleClassificationManager(Reasoner reasoner) {
        if (reasoner.getTableau().isDeterministic())
            return new DeterministicClassificationManager<Role>(new DataRoleSubsumptionCache(reasoner));
        else
            return new StandardClassificationManager<Role>(new DataRoleSubsumptionCache(reasoner));
    }

    protected static DLOntology createDeltaDLOntology(Configuration configuration,DLOntology originalDLOntology,OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        try {
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLOntology newOntology=ontologyManager.createOntology(IRI.create("uri:urn:internal-kb"));
            for (OWLAxiom axiom : additionalAxioms) {
                if (axiom instanceof OWLSubObjectPropertyOfAxiom)
                    throw new IllegalArgumentException("It is not possible to extend a DL-ontology with object property inclusions.");
                ontologyManager.addAxiom(newOntology,axiom);
            }
            OWLAxioms axioms=new OWLAxioms();
            axioms.m_definedDatatypesIRIs.addAll(originalDLOntology.getDefinedDatatypeIRIs());
            OWLNormalization normalization=new OWLNormalization(factory,axioms);
            normalization.processOntology(configuration,newOntology);
            BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);
            builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms,originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_OBJECT_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_OBJECT_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_DATA_ROLE),originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_DATA_ROLE));
            if (!originalDLOntology.getAllComplexObjectRoleInclusions().isEmpty()) {
                for (DLClause dlClause : originalDLOntology.getDLClauses()) {
                    if (dlClause.getClauseType()==DLClause.ClauseType.OBJECT_PROPERTY_INCLUSION) {
                        OWLObjectProperty subObjectProperty=getObjectProperty(factory,(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate());
                        OWLObjectProperty superObjectProperty=getObjectProperty(factory,(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate());
                        axioms.m_simpleObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectProperty,superObjectProperty });
                    }
                    else if (dlClause.getClauseType()==DLClause.ClauseType.INVERSE_OBJECT_PROPERTY_INCLUSION) {
                        OWLObjectProperty subObjectProperty=getObjectProperty(factory,(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate());
                        OWLObjectProperty superObjectProperty=getObjectProperty(factory,(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate());
                        axioms.m_simpleObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectProperty,superObjectProperty.getInverseProperty() });
                    }
                }
                for (DLOntology.ComplexObjectRoleInclusion complexInclusion : originalDLOntology.getAllComplexObjectRoleInclusions()) {
                    OWLObjectPropertyExpression[] subObjectPropertyExpressions=new OWLObjectPropertyExpression[complexInclusion.getNumberOfSubRoles()];
                    for (int subRoleIndex=0;subRoleIndex<complexInclusion.getNumberOfSubRoles();subRoleIndex++)
                        subObjectPropertyExpressions[subRoleIndex]=getObjectPropertyExpression(factory,complexInclusion.getSubRole(subRoleIndex));
                    OWLObjectPropertyExpression superObjectPropertyExpression=getObjectPropertyExpression(factory,complexInclusion.getSuperRole());
                    axioms.m_complexObjectPropertyInclusions.add(new OWLAxioms.ComplexObjectPropertyInclusion(subObjectPropertyExpressions,superObjectPropertyExpression));
                }
                ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(factory);
                objectPropertyInclusionManager.rewriteAxioms(axioms,originalDLOntology.getAllAtomicConcepts().size());
                // The object property axioms must be cleared or they will be clausified below twice.
                // Since the subproperty axioms for object properties are not allowed as extending axioms,
                // this does not cause problems.
                axioms.m_simpleObjectPropertyInclusions.clear();
                axioms.m_complexObjectPropertyInclusions.clear();
            }
            OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
            axiomsExpressivity.m_hasAtMostRestrictions|=originalDLOntology.hasAtMostRestrictions();
            axiomsExpressivity.m_hasInverseRoles|=originalDLOntology.hasInverseRoles();
            axiomsExpressivity.m_hasNominals|=originalDLOntology.hasNominals();
            axiomsExpressivity.m_hasDatatypes|=originalDLOntology.hasDatatypes();
            OWLClausification clausifier=new OWLClausification(configuration);
            Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
            return clausifier.clausify(factory,"uri:urn:internal-kb",axioms,axiomsExpressivity,descriptionGraphs);
        }
        catch (OWLException e) {
            throw new IllegalStateException("Internal error: unexpected OWLException",e);
        }
    }

    protected static DLOntology mergeDLOntologies(Configuration configuration,Prefixes prefixes,String resultingOntologyIRI,DLOntology dlOntology1,DLOntology dlOntology2) throws IllegalArgumentException {
        Set<DLClause> dlClauses=createUnion(dlOntology1.getDLClauses(),dlOntology2.getDLClauses());
        Set<Atom> positiveFacts=createUnion(dlOntology1.getPositiveFacts(),dlOntology2.getPositiveFacts());
        Set<Atom> negativeFacts=createUnion(dlOntology1.getNegativeFacts(),dlOntology2.getNegativeFacts());
        Set<AtomicConcept> atomicConcepts=createUnion(dlOntology1.getAllAtomicConcepts(),dlOntology2.getAllAtomicConcepts());
        Set<DLOntology.ComplexObjectRoleInclusion> complexObjectRoleInclusions=createUnion(dlOntology1.getAllComplexObjectRoleInclusions(),dlOntology2.getAllComplexObjectRoleInclusions());
        Set<AtomicRole> atomicObjectRoles=createUnion(dlOntology1.getAllAtomicObjectRoles(),dlOntology2.getAllAtomicObjectRoles());
        Set<AtomicRole> atomicDataRoles=createUnion(dlOntology1.getAllAtomicDataRoles(),dlOntology2.getAllAtomicDataRoles());
        Set<String> definedDatatypeIRIs=createUnion(dlOntology1.getDefinedDatatypeIRIs(),dlOntology2.getDefinedDatatypeIRIs());
        Set<Individual> individuals=createUnion(dlOntology1.getAllIndividuals(),dlOntology2.getAllIndividuals());
        boolean hasInverseRoles=dlOntology1.hasInverseRoles() || dlOntology2.hasInverseRoles();
        boolean hasAtMostRestrictions=dlOntology1.hasAtMostRestrictions() || dlOntology2.hasAtMostRestrictions();
        boolean hasNominals=dlOntology1.hasNominals() || dlOntology2.hasNominals();
        boolean hasDatatypes=dlOntology1.hasDatatypes() || dlOntology2.hasDatatypes();
        return new DLOntology(resultingOntologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,atomicObjectRoles,complexObjectRoleInclusions,atomicDataRoles,definedDatatypeIRIs,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,hasDatatypes);
    }

    protected static <T> Set<T> createUnion(Set<T> set1,Set<T> set2) {
        Set<T> result=new HashSet<T>();
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }
    protected static <K, V> Map<K,V> createUnion(Map<K,V> map1,Map<K,V> map2) {
        Map<K,V> result=new HashMap<K,V>();
        if (map1!=null)
            result.putAll(map1);
        if (map2!=null)
            result.putAll(map2);
        return result;
    }
    protected static OWLObjectProperty getObjectProperty(OWLDataFactory factory,AtomicRole atomicRole) {
        return factory.getOWLObjectProperty(IRI.create(atomicRole.getIRI()));
    }
    protected static OWLObjectPropertyExpression getObjectPropertyExpression(OWLDataFactory factory,Role role) {
        if (role instanceof AtomicRole)
            return factory.getOWLObjectProperty(IRI.create(((AtomicRole)role).getIRI()));
        else {
            AtomicRole inverseOf=((InverseRole)role).getInverseOf();
            return factory.getOWLObjectProperty(IRI.create(inverseOf.getIRI())).getInverseProperty();
        }
    }
    protected static OWLDataProperty getDataProperty(OWLDataFactory factory,AtomicRole atomicRole) {
        return factory.getOWLDataProperty(IRI.create(atomicRole.getIRI()));
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
    protected Node<OWLClass> atomicConceptsToOWLAPI(Collection<AtomicConcept> concepts) {
        Set<OWLClass> result=new HashSet<OWLClass>();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        for (AtomicConcept concept : concepts)
            if (!Prefixes.isInternalIRI(concept.getIRI()))
                result.add(factory.getOWLClass(IRI.create(concept.getIRI())));
        return new OWLClassNode(result);
    }
    protected NodeSet<OWLClass> atomicConceptNodesToOWLAPI(Collection<HierarchyNode<AtomicConcept>> nodes) {
        Set<Node<OWLClass>> result=new HashSet<Node<OWLClass>>();
        for (HierarchyNode<AtomicConcept> node : nodes) {
            Node<OWLClass> owlapinode=atomicConceptsToOWLAPI(node.getEquivalentElements());
            if (owlapinode.getSize()!=0)
                result.add(owlapinode);
        }
        return new OWLClassNodeSet(result);
    }
    protected Node<OWLDataProperty> dataPropertiesToOWLAPI(Collection<Role> dataProperties) {
        Set<OWLDataProperty> result=new HashSet<OWLDataProperty>();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        for (Role role : dataProperties)
            result.add(factory.getOWLDataProperty(IRI.create(((AtomicRole)role).getIRI())));
        return new OWLDataPropertyNode(result);
    }
    protected NodeSet<OWLDataProperty> dataPropertyNodesToOWLAPI(Collection<HierarchyNode<Role>> nodes) {
        Set<Node<OWLDataProperty>> result=new HashSet<Node<OWLDataProperty>>();
        for (HierarchyNode<Role> node : nodes)
            result.add(dataPropertiesToOWLAPI(node.getEquivalentElements()));
        return new OWLDataPropertyNodeSet(result);
    }
    protected Node<OWLObjectProperty> objectPropertiesToOWLAPI(Collection<Role> objectProperties) {
        Set<OWLObjectProperty> result=new HashSet<OWLObjectProperty>();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        for (Role role : objectProperties)
            if (role instanceof AtomicRole)
                result.add(factory.getOWLObjectProperty(IRI.create(((AtomicRole)role).getIRI())));
        return new OWLObjectPropertyNode(result);
    }
    protected NodeSet<OWLObjectProperty> objectPropertyNodesToOWLAPI(Collection<HierarchyNode<Role>> nodes) {
        Set<Node<OWLObjectProperty>> result=new HashSet<Node<OWLObjectProperty>>();
        for (HierarchyNode<Role> node : nodes)
            result.add(objectPropertiesToOWLAPI(node.getEquivalentElements()));
        return new OWLObjectPropertyNodeSet(result);
    }
    protected Set<OWLObjectPropertyExpression> objectRolesToObjectPropertyExpressions(Collection<Role> roles) {
        Set<OWLObjectPropertyExpression> result=new HashSet<OWLObjectPropertyExpression>();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        for (Role role : roles)
            if (role instanceof AtomicRole)
                result.add(factory.getOWLObjectProperty(IRI.create(((AtomicRole)role).getIRI())));
            else
                result.add(factory.getOWLObjectProperty(IRI.create(((InverseRole)role).getInverseOf().getIRI())).getInverseProperty());
        return result;
    }
    protected Set<Set<OWLObjectPropertyExpression>> objectRoleNodesToSetOfSetsOfObjectPropertyExpressions(Collection<HierarchyNode<Role>> nodes) {
        Set<Set<OWLObjectPropertyExpression>> result=new HashSet<Set<OWLObjectPropertyExpression>>();
        for (HierarchyNode<Role> node : nodes)
            result.add(objectRolesToObjectPropertyExpressions(node.getEquivalentElements()));
        return result;
    }
    protected Set<OWLNamedIndividual> individualsToNamedIndividuals(Collection<Individual> individuals) {
        Set<OWLNamedIndividual> result=new HashSet<OWLNamedIndividual>();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        for (Individual ind : individuals)
            if (ind.isNamed() && !Prefixes.isInternalIRI(ind.getIRI()))
                result.add(factory.getOWLNamedIndividual(IRI.create(ind.getIRI())));
        return result;
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
            return new ChangeTrackingReasoner(configuration,ontology);
        }
    }

    // The reasoner that tracks changes

    public static class ProtegeReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
        public OWLReasoner createReasoner(OWLOntology ontology,ReasonerProgressMonitor monitor) {
            ReasonerFactory factory=new ReasonerFactory();
            Configuration configuration=factory.getProtegeConfiguration(null);
            configuration.reasonerProgressMonitor=monitor;
            return factory.createHermiTOWLReasoner(configuration,ontology);
        }
        public void initialise() throws Exception {
        }
        public void dispose() throws Exception {
        }
    }
}
