// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
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
import org.semanticweb.HermiT.blocking.ValidatedDirectBlockingChecker;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.ConceptSubsumptionCache;
import org.semanticweb.HermiT.hierarchy.DeterministicHierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyPrinterFSS;
import org.semanticweb.HermiT.hierarchy.RoleSubsumptionCache;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
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
import org.semanticweb.owlapi.inference.MonitorableOWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
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
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.util.ProgressMonitor;

/**
 * Answers queries about the logical implications of a particular knowledge base. A Reasoner is associated with a single knowledge base, which is "loaded" when the reasoner is constructed. By default a full classification of all atomic terms in the knowledge base is also performed at this time (which can take quite a while for large or complex ontologies), but this behavior can be disabled as a part of the Reasoner configuration. Internal details of the loading and reasoning algorithms can be configured in the Reasoner constructor and do not change over the lifetime of the Reasoner object---internal data structures and caches are optimized for a particular configuration. By default, HermiT will use the set of options which provide optimal performance.
 */
public class Reasoner implements MonitorableOWLReasoner,Serializable {
    private static final long serialVersionUID=-3511564272739622311L;

    protected final Configuration m_configuration;
    protected final InterruptFlag m_interruptFlag;
    protected DLOntology m_dlOntology;
    protected Prefixes m_prefixes;
    protected Tableau m_tableau;
    protected ConceptSubsumptionCache m_conceptSubsumptionCache;
    protected Hierarchy<AtomicConcept> m_atomicConceptHierarchy;
    protected RoleSubsumptionCache m_objectRoleSubsumptionCache;
    protected Hierarchy<Role> m_objectRoleHierarchy;
    protected RoleSubsumptionCache m_dataRoleSubsumptionCache;
    protected Hierarchy<Role> m_dataRoleHierarchy;
    protected Map<AtomicConcept,Set<Individual>> m_realization;
    protected ProgressMonitor m_progressMonitor;

    /**
     * Creates a new reasoner instance with standard parameters for blocking, expansion strategy etc. 
     */
    public Reasoner() {
        this(new Configuration());
    }
    
    /**
     * Creates a new reasoner instance with the parameters for blocking, expansion strategy etc as specified 
     * in the given configuration object. A default configuration can be obtained by just passing new Configuration().
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     */
    public Reasoner(Configuration configuration) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        if (m_dlOntology != null || m_tableau != null) clearOntologies();
    }
    
    /**
     * Creates a new reasoner object with standard parameters for blocking, expansion strategy etc.  
     * Then the given manager is used to find all required imports for the given ontology and the ontology with the 
     * imports is loaded into the reasoner and the data factory of the manager is used to create fresh concepts during 
     * the preprocessing phase if necessary. 
     * @param ontologyManger - the manager that will be used to determine the required imports for the given ontology
     * @param ontology - the ontology that should be loaded by the reasoner
     */
    public Reasoner(OWLOntologyManager ontologyManger,OWLOntology ontology) {
        this(new Configuration(),ontologyManger,ontology,(Set<DescriptionGraph>)null);
    }
    
    /**
     * Creates a new reasoner object with the parameters for blocking, expansion strategy etc as specified 
     * in the given configuration object. A default configuration can be obtained by just passing new Configuration(). 
     * Then the given manager is used to find all required imports for the given ontology and the ontology with the 
     * imports is loaded into the reasoner and the data factory of the manager is used to create fresh concepts during 
     * the preprocessing phase if necessary. 
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param ontologyManger - the manager that will be used to determine the required imports for the given ontology
     * @param ontology - the ontology that should be loaded by the reasoner
     */
    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManger,OWLOntology ontology) {
        this(configuration,ontologyManger,ontology,(Set<DescriptionGraph>)null);
    }

    /**
     * Creates a new reasoner object loaded with the given ontology and the given description graphs. When creating 
     * the reasoner, the given configuration determines the parameters for blocking, expansion strategy etc. 
     * A default configuration can be obtained by just passing new Configuration(). 
     * Then the given manager is used to find all required imports for the given ontology and the ontology with the 
     * imports and the description graphs are loaded into the reasoner. The data factory of the manager is used to 
     * create fresh concepts during the preprocessing phase if necessary. 
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param ontologyManager - the manager that will be used to determine the required imports for the given ontology
     * @param ontology - the ontology that should be loaded by the reasoner
     * @param descriptionGraphs - a set of description graphs
     */
    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadOntology(ontologyManager,ontology,descriptionGraphs);
    }
    
    /**
     * This method is mainly for use of HermiT in Protege. It is assumed that the given set of ontologies contains all the 
     * required imports. HermiT will NOT load any imports and only the axioms that are in the given ontologies. 
     * A new reasoner object is created and the given ontologies are loaded. When creating 
     * the reasoner, the given configuration determines the parameters for blocking, expansion strategy etc. 
     * A default configuration can be obtained by just passing new Configuration(). 
     * An arbitrary ontology manager and data factory is used in the preprocessing phase. 
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param importClosure - a set of ontologies that MUST contain all the imports for the ontologies in the set
     */
    public Reasoner(Configuration configuration,Set<OWLOntology> importClosure) {
        this(configuration, OWLManager.createOWLOntologyManager().getOWLDataFactory(), importClosure);
    }
    
    /**
     * This method is mainly for use of HermiT in Protege. It is assumed that the given set of ontologies contains all the 
     * required imports. HermiT will NOT load any imports and only the axioms that are in the given ontologies. 
     * A new reasoner object is created and the given ontologies are loaded. When creating 
     * the reasoner, the given configuration determines the parameters for blocking, expansion strategy etc. 
     * The data factory of the given manager is used to create fresh concepts during the preprocessing phase if necessary. 
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param dataFactory - the data factory to be used in the preprocessing phase
     * @param importClosure - a set of ontologies that MUST contain all the imports for the ontologies in the set
     */
    public Reasoner(Configuration configuration,OWLDataFactory dataFactory,Set<OWLOntology> importClosure) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadOntologies(dataFactory, importClosure);
    }

    /**
     * This is mainly an internal method. Once an ontology is loaded, normalised and clausified, the resulting DLOntology 
     * object can be obtained by calling getDLOntology(), saved and reloaded later with this method so that normalisation 
     * and clausification is not done again. 
     * A default configuration can be obtained by just passing new Configuration(). 
     * @param configuration - a configuration in which parameters can be defined such as the blocking strategy to be used etc
     * @param dlOntology - an ontology in HermiT's internal ontology format
     */
    public Reasoner(Configuration configuration,DLOntology dlOntology) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadDLOntology(dlOntology);
    }

    // General accessor methods
    
    /** 
     * @return A prefix object that is used to maintain the URI/IRI prefixes of classes, properties etc that are used in the reasoner. 
     */
    public Prefixes getPrefixes() {
        return m_prefixes;
    }

    /**
     * @return An ontology in HermiT's internal format containing all axioms of the ontologies that are loaded in the reasoner.   
     */
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }

    /**
     * @return A copy of the configuration that is used by this Reasoner instance.  
     */
    public Configuration getConfiguration() {
        return m_configuration.clone();
    }

    /**
     * Set a flag to tell the reasoner to stop whatever it is doing. All operations check from time to time whether this flag 
     * is set and if it is, then the current process is aborted.  
     */
    public void interrupt() {
        m_interruptFlag.interrupt();
    }
    
    /**
     * Returns the tableau of this reasoner.
     */
    public Tableau getTableau() {
        return m_tableau;
    }
    
    // Loading and managing ontologies

    /**
     * Load an ontology in HermiT's internal format. A DLOntology can be obtained from a Reasoner instance after loading 
     * an OWLOntology by calling getDLOntology(). The DLOntology contains clauses and facts, obtained after normalisation and clausification on an 
     * OWLOntology. The DLOntology also contains a configuration.     
     * @param dlOntology - an ontology in HermiT's internal format
     */
    public void loadDLOntology(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_prefixes=createPrefixes(m_dlOntology);
        m_tableau=createTableau(m_interruptFlag,m_configuration,m_dlOntology,m_prefixes);
        m_conceptSubsumptionCache=new ConceptSubsumptionCache(m_tableau);
        Set<Role> relevantRoles=new HashSet<Role>();
        for (AtomicRole atomicRole : m_dlOntology.getAllAtomicObjectRoles()) {
            relevantRoles.add(atomicRole);
            relevantRoles.add(atomicRole.getInverse());
        }
        m_objectRoleSubsumptionCache=new RoleSubsumptionCache(this,m_dlOntology.hasInverseRoles(),relevantRoles,AtomicRole.BOTTOM_OBJECT_ROLE,AtomicRole.TOP_OBJECT_ROLE);
        m_dataRoleSubsumptionCache=new RoleSubsumptionCache(this,m_dlOntology.hasInverseRoles(),new HashSet<Role>(m_dlOntology.getAllAtomicDataRoles()),AtomicRole.BOTTOM_DATA_ROLE,AtomicRole.TOP_DATA_ROLE);
    }
    
    /**
     * The given ontology and description graphs are loaded into the Reasoner instance. Any previously loaded ontologies are 
     * overwritten.  
     * Then the given manager is used to find all required imports for the given ontology and the ontology with the 
     * imports and the description graphs are loaded into the reasoner. The data factory of the manager is used to 
     * create fresh concepts during the preprocessing phase if necessary. 
     * @param ontologyManager - the manager that will be used to determine the required imports for the given ontology
     * @param ontology - the ontology that should be loaded by the reasoner
     * @param descriptionGraphs - a set of description graphs or null
     */
    public void loadOntology(OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLClausification clausifier=new OWLClausification(m_configuration);
        loadDLOntology(clausifier.clausify(ontologyManager,ontology,descriptionGraphs));
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#loadOntologies(java.util.Set)
     */
    public void loadOntologies(Set<OWLOntology> ontologies) {
        loadOntologies(OWLManager.createOWLOntologyManager().getOWLDataFactory(), ontologies);
    }
    
    /**
     * Loads the specified ontologies. The reasoner will then take into consideration the logical axioms in each ontology.
     * Note that this methods does <b>not<b> load any ontologies in the imports closure - <i>all</i> imports must be loaded
     * explicitly.
     * @param dataFactory - the data factory that is used in the preprocessing phase to introduce new concepts if necessary
     * @param ontologies - the ontologies to be loaded (no imports will be loaded)
     */
    public void loadOntologies(OWLDataFactory dataFactory, Set<OWLOntology> ontologies) {
        OWLClausification clausifier=new OWLClausification(m_configuration);
        Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
        loadDLOntology(clausifier.clausifyImportClosure(dataFactory,"urn:hermit:kb",ontologies,descriptionGraphs));
    }
    
    /**
     * Required for the OWLReasoner interface, but HermiT does not support this method. All loaded ontologies end up in 
     * one set of clauses and we do not keep track of what came from where, so it will throw an UnsupportedOperation exception. 
     * {@inheritDoc}
     */
    public Set<OWLOntology> getLoadedOntologies() {
        throw new UnsupportedOperationException();
    }

    /**
     * Required for the OWLReasoner interface, but HermiT does not support this method. All loaded ontologies end up in 
     * one set of clauses and we do not keep track of what came from where, so it will throw an UnsupportedOperation exception.
     * {@inheritDoc} 
     */
    public void unloadOntologies(Set<OWLOntology> inOntologies) {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#clearOntologies()
     */
    public void clearOntologies() {
        Set<DLClause> noDLClauses=Collections.emptySet();
        Set<Atom> noAtoms=Collections.emptySet();
        Set<AtomicConcept> noAtomicConcepts=Collections.emptySet();
        Set<AtomicRole> noAtomicRoles=Collections.emptySet();
        Set<DLOntology.ComplexObjectRoleInclusion> noComplexObjectRoleInclusions=Collections.emptySet();
        Set<Individual> noIndividuals=Collections.emptySet();
        Set<String> noDefinedDatatypeIRIs=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("urn:hermit:kb",noDLClauses,noAtoms,noAtoms,noAtomicConcepts,noComplexObjectRoleInclusions,noAtomicRoles,noAtomicRoles,noDefinedDatatypeIRIs,noIndividuals,false,false,false,false);
        loadDLOntology(emptyDLOntology);
    }
    
    /**
     * Same as clearOntologies() in HermiT. 
     * {@inheritDoc}
     */
    public void dispose() {
        clearOntologies();
    }
    
    // Monitor interface

    /**
     * Required for the OWLReasoner interface, but HermiT does not support this method and will throw an 
     * UnsupportedOperation exception.
     * {@inheritDoc} 
     */
    public OWLEntity getCurrentEntity() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.MonitorableOWLReasoner#setProgressMonitor(org.semanticweb.owlapi.util.ProgressMonitor)
     */
    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        m_progressMonitor=progressMonitor;
    }

    // Checking the signature of the ontology
    

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#isDefined(org.semanticweb.owlapi.model.OWLClass)
     */
    public boolean isDefined(OWLClass owlClass) {
        AtomicConcept atomicConcept=AtomicConcept.create(owlClass.getIRI().toString());
        return m_dlOntology.getAllAtomicConcepts().contains(atomicConcept) || AtomicConcept.THING.equals(atomicConcept) || AtomicConcept.NOTHING.equals(atomicConcept);
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#isDefined(org.semanticweb.owlapi.model.OWLIndividual)
     */
    public boolean isDefined(OWLIndividual owlIndividual) {
        Individual individual;
        if (owlIndividual.isAnonymous()) {
            individual=Individual.create(owlIndividual.asAnonymousIndividual().getID().toString(),false);
        } else {
            individual=Individual.create(owlIndividual.asNamedIndividual().getIRI().toString(),true);
        }
        return m_dlOntology.getAllIndividuals().contains(individual);
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#isDefined(org.semanticweb.owlapi.model.OWLObjectProperty)
     */
    public boolean isDefined(OWLObjectProperty owlObjectProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlObjectProperty.getIRI().toString());
        return m_dlOntology.getAllAtomicObjectRoles().contains(atomicRole);
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#isDefined(org.semanticweb.owlapi.model.OWLDataProperty)
     */
    public boolean isDefined(OWLDataProperty owlDataProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlDataProperty.getIRI().toString());
        return m_dlOntology.getAllAtomicDataRoles().contains(atomicRole);
    }

    // General inferences
    
    /**
     * @return true if the loaded ontology (set of axioms) has a model and false otherwise
     */
    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }

    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isConsistent()}
     */
    @Deprecated
    public boolean isConsistent(OWLOntology ignored) {
        return isConsistent();
    }
    
    // Concept inferences

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#isClassified()
     */
    public boolean isClassified() {
        return m_atomicConceptHierarchy!=null;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLReasonerBase#classify()
     */
    public void classify() {
        if (m_atomicConceptHierarchy==null) {
            try {
                Set<AtomicConcept> relevantAtomicConcepts=new HashSet<AtomicConcept>();
                relevantAtomicConcepts.add(AtomicConcept.THING);
                relevantAtomicConcepts.add(AtomicConcept.NOTHING);
                for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                    if (!Prefixes.isInternalIRI(atomicConcept.getIRI()))
                        relevantAtomicConcepts.add(atomicConcept);
                final int numRelevantConcepts=relevantAtomicConcepts.size();
                if (m_progressMonitor!=null) {
                    m_progressMonitor.setMessage("Classifying...");
                    m_progressMonitor.setProgress(0);
                    m_progressMonitor.setStarted();
                }
                if (!m_conceptSubsumptionCache.isSatisfiable(AtomicConcept.THING))
                    m_atomicConceptHierarchy=Hierarchy.emptyHierarchy(relevantAtomicConcepts,AtomicConcept.THING,AtomicConcept.NOTHING);
                else if (m_conceptSubsumptionCache.canGetAllSubsumersEasily()) {
                    Map<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>>();
                    int processedConcepts=0;
                    for (AtomicConcept atomicConcept : relevantAtomicConcepts) {
                        Set<AtomicConcept> subsumers=m_conceptSubsumptionCache.getAllKnownSubsumers(atomicConcept);
                        if (subsumers==null)
                            subsumers=relevantAtomicConcepts;
                        allSubsumers.put(atomicConcept,new DeterministicHierarchyBuilder.GraphNode<AtomicConcept>(atomicConcept,subsumers));
                        if (m_progressMonitor!=null) {
                            processedConcepts++;
                            m_progressMonitor.setMessage("Classifying...");
                            m_progressMonitor.setProgress(processedConcepts/numRelevantConcepts);
                        }
                    }
                    DeterministicHierarchyBuilder<AtomicConcept> hierarchyBuilder=new DeterministicHierarchyBuilder<AtomicConcept>(allSubsumers,AtomicConcept.THING,AtomicConcept.NOTHING);
                    m_atomicConceptHierarchy=hierarchyBuilder.buildHierarchy();
                }
                if (m_atomicConceptHierarchy==null) {
                    HierarchyBuilder.Relation<AtomicConcept> relation=
                        new HierarchyBuilder.Relation<AtomicConcept>() {
                            public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                                return m_conceptSubsumptionCache.isSubsumedBy(child,parent);
                            }
                        };
                    HierarchyBuilder.ClassificationProgressMonitor<AtomicConcept> progressMonitor;
                    if (m_progressMonitor==null)
                        progressMonitor=null;
                    else
                        progressMonitor=
                            new HierarchyBuilder.ClassificationProgressMonitor<AtomicConcept>() {
                                protected int m_processedConcepts=0;
                                public void elementClassified(AtomicConcept element) {
                                    m_processedConcepts++;
                                    m_progressMonitor.setMessage("Building the class hierarchy...");
                                    m_progressMonitor.setProgress(m_processedConcepts/numRelevantConcepts);
                                }
                            };
                    HierarchyBuilder<AtomicConcept> hierarchyBuilder=new HierarchyBuilder<AtomicConcept>(relation,progressMonitor);
                    m_atomicConceptHierarchy=hierarchyBuilder.buildHierarchy(AtomicConcept.THING,AtomicConcept.NOTHING,relevantAtomicConcepts);
                }
            }
            finally {
                if (m_progressMonitor!=null)
                    m_progressMonitor.setFinished();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLSatisfiabilityChecker#isSatisfiable(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public boolean isSatisfiable(OWLClassExpression description) {
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            if (m_atomicConceptHierarchy==null)
                return m_conceptSubsumptionCache.isSatisfiable(concept);
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

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#isSubClassOf(org.semanticweb.owlapi.model.OWLClassExpression, org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public boolean isSubClassOf(OWLClassExpression subDescription,OWLClassExpression superDescription) {
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass) {
            AtomicConcept subconcept=AtomicConcept.create(((OWLClass)subDescription).getIRI().toString());
            AtomicConcept superconcept=AtomicConcept.create(((OWLClass)superDescription).getIRI().toString());
            return m_conceptSubsumptionCache.isSubsumedBy(subconcept,superconcept);
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

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#isEquivalentClass(org.semanticweb.owlapi.model.OWLClassExpression, org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public boolean isEquivalentClass(OWLClassExpression description1,OWLClassExpression description2) {
        return isSubClassOf(description1,description2) && isSubClassOf(description2,description1); 
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getEquivalentClasses(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public Set<OWLClass> getEquivalentClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptsToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getSubClasses(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public Set<Set<OWLClass>> getSubClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getSuperClasses(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public Set<Set<OWLClass>> getSuperClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getAncestorClasses(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public Set<Set<OWLClass>> getAncestorClasses(OWLClassExpression classExpression) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(classExpression);
        return atomicConceptNodesToOWLAPI(node.getAncestorNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getDescendantClasses(org.semanticweb.owlapi.model.OWLClassExpression)
     */
    public Set<Set<OWLClass>> getDescendantClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getDescendantNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getUnsatisfiableClasses()}
     */
    @Deprecated
    public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
        return getUnsatisfiableClasses();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.inference.OWLClassReasoner#getUnsatisfiableClasses()
     */
    public Set<OWLClass> getUnsatisfiableClasses() throws OWLReasonerException {
        classify();
        HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getBottomNode();
        return atomicConceptsToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    protected HierarchyNode<AtomicConcept> getHierarchyNode(OWLClassExpression description) {
        classify();
        if (description instanceof OWLClass) {
            AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(atomicConcept);
            if (node==null)
                node=new HierarchyNode<AtomicConcept>(atomicConcept,Collections.singleton(atomicConcept),Collections.singleton(m_atomicConceptHierarchy.getTopNode()),Collections.singleton(m_atomicConceptHierarchy.getBottomNode()));
            return node;
        } else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(newClass,description);
            Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            final ConceptSubsumptionCache subsumptionCache=new ConceptSubsumptionCache(tableau);
            HierarchyBuilder<AtomicConcept> hierarchyBuilder=new HierarchyBuilder<AtomicConcept>(
                new HierarchyBuilder.Relation<AtomicConcept>() {
                    public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                        return subsumptionCache.isSubsumedBy(child,parent);
                    }
                },
                null
            );
            return hierarchyBuilder.findPosition(AtomicConcept.create("internal:query-concept"),m_atomicConceptHierarchy.getTopNode(),m_atomicConceptHierarchy.getBottomNode());
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
     * Builds the object property hierarchy. With nominals and cardinality restrictions this 
     * cannot always been dome by simply building the transitive closure of the asserted object 
     * property hierarchy.  
     */
    public void classifyObjectProperties() {
        if (m_objectRoleHierarchy==null) {
            try {
                Set<Role> allObjectRoles=new HashSet<Role>();
                for (AtomicRole atomicRole : m_dlOntology.getAllAtomicObjectRoles()) {
                    allObjectRoles.add(atomicRole);
                    if (m_dlOntology.hasInverseRoles()) allObjectRoles.add(atomicRole.getInverse());
                }
                allObjectRoles.add(AtomicRole.TOP_OBJECT_ROLE);
                allObjectRoles.add(AtomicRole.BOTTOM_OBJECT_ROLE);
                final int numRoles=allObjectRoles.size();
                if (m_progressMonitor!=null) {
                    m_progressMonitor.setMessage("Classifying object propoerties...");
                    m_progressMonitor.setProgress(0);
                    m_progressMonitor.setStarted();
                }
                
                if (!m_objectRoleSubsumptionCache.isSatisfiable(AtomicRole.TOP_OBJECT_ROLE))
                    m_objectRoleHierarchy=Hierarchy.emptyHierarchy(allObjectRoles,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE);
                else if (m_objectRoleSubsumptionCache.canGetAllSubsumersEasily()) {
                    Map<Role,DeterministicHierarchyBuilder.GraphNode<Role>> allSubsumers=new HashMap<Role,DeterministicHierarchyBuilder.GraphNode<Role>>();
                    int processedRoles=0;
                    for (Role role : allObjectRoles) {
                        Set<Role> subsumers=m_objectRoleSubsumptionCache.getAllKnownSubsumers(role);
                        if (subsumers==null)
                            subsumers=allObjectRoles;
                        allSubsumers.put(role,new DeterministicHierarchyBuilder.GraphNode<Role>(role,subsumers));
                        if (m_progressMonitor!=null) {
                            processedRoles++;
                            m_progressMonitor.setMessage("Classifying object properties...");
                            m_progressMonitor.setProgress(processedRoles/numRoles);
                        }
                    }
                    DeterministicHierarchyBuilder<Role> hierarchyBuilder=new DeterministicHierarchyBuilder<Role>(allSubsumers,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE);
                    m_objectRoleHierarchy=hierarchyBuilder.buildHierarchy();
                }
                if (m_objectRoleHierarchy==null) {
                    HierarchyBuilder.Relation<Role> relation=
                        new HierarchyBuilder.Relation<Role>() {
                            public boolean doesSubsume(Role sup,Role sub) {
                                return m_objectRoleSubsumptionCache.isSubsumedBy(sub,sup);
                            }
                        };
                    HierarchyBuilder.ClassificationProgressMonitor<Role> progressMonitor;
                    if (m_progressMonitor==null)
                        progressMonitor=null;
                    else
                        progressMonitor=
                            new HierarchyBuilder.ClassificationProgressMonitor<Role>() {
                                protected int m_processedRoles=0;
                                public void elementClassified(Role element) {
                                    m_processedRoles++;
                                    m_progressMonitor.setMessage("Building the object property hierarchy...");
                                    m_progressMonitor.setProgress(m_processedRoles/numRoles);
                                }
                            };
                    HierarchyBuilder<Role> hierarchyBuilder=new HierarchyBuilder<Role>(relation,progressMonitor);
                    m_objectRoleHierarchy=hierarchyBuilder.buildHierarchy(AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE,allObjectRoles);
                }
            }
            finally {
                if (m_progressMonitor!=null)
                    m_progressMonitor.setFinished();
            }
        }
    }
    
    /**
     * Determines if subObjectPropertyExpression is a sub-property of superObjectPropertyExpression. 
     * HermiT answers true if subObjectPropertyExpression=superObjectPropertyExpression. 
     * @param subObjectPropertyExpression - the sub object property expression
     * @param superObjectPropertyExpression - the super object property expression
     * @return true if whenever a pair related with the property subObjectPropertyExpression is necessarily 
     *         related with the property superObjectPropertyExpression and false otherwise
     */
    public boolean isSubPropertyOf(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        Role subRole;
        if (subObjectPropertyExpression.getSimplified().isAnonymous()) {
            subRole=InverseRole.create(AtomicRole.create(subObjectPropertyExpression.getNamedProperty().getIRI().toString()));
        } else {
            subRole=AtomicRole.create(subObjectPropertyExpression.getNamedProperty().getIRI().toString());
        }
        Role superRole;
        if (superObjectPropertyExpression.getSimplified().isAnonymous()) {
            superRole=InverseRole.create(AtomicRole.create(superObjectPropertyExpression.getNamedProperty().getIRI().toString()));
        } else {
            superRole=AtomicRole.create(superObjectPropertyExpression.getNamedProperty().getIRI().toString());
        }
        return m_objectRoleSubsumptionCache.isSubsumedBy(subRole, superRole);
    }
    
    /**
     * Determines if the property chain represented by subPropertyChain is a sub-property of superObjectPropertyExpression. 
     * @param subPropertyChain -  a list that represents a property chain 
     * @param superObjectPropertyExpression - an object property expression
     * @return if r1, ..., rn is the given chain and r the given super property, then the answer is true if whenever 
     *         r1(d0, d1), ..., rn(dn-1, dn) holds in a model for some elements d0, ..., dn, then necessarily r(d0, dn) 
     *         holds and it is false otherwise
     */
    public boolean isSubPropertyOf(List<OWLObjectPropertyExpression> subPropertyChain, OWLObjectPropertyExpression superObjectPropertyExpression) {
        if (superObjectPropertyExpression.getNamedProperty().getIRI().toString().equals(AtomicRole.TOP_OBJECT_ROLE.getIRI()))
            return true;
        else {
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            OWLObjectPropertyExpression[] subObjectProperties=new OWLObjectPropertyExpression[subPropertyChain.size()];
            subPropertyChain.toArray(subObjectProperties);
            OWLIndividual randomIndividual=factory.getOWLNamedIndividual(IRI.create("internal:randomIndividual"));
            OWLClassExpression owlSuperClassExpression = factory.getOWLObjectHasValue( superObjectPropertyExpression, randomIndividual);
            OWLClassExpression owlSubClassExpression = factory.getOWLObjectHasValue( subObjectProperties[subObjectProperties.length-1], randomIndividual);
            for( int i=subObjectProperties.length-2 ; i>=0 ; i-- )
                    owlSubClassExpression = factory.getOWLObjectSomeValuesFrom(subObjectProperties[i], owlSubClassExpression);
            return isSubClassOf( owlSubClassExpression , owlSuperClassExpression);
        }
    }
    
    /**
     * @param objectPropertyExpression1 - an object property expression
     * @param objectPropertyExpression2 - an object property expression
     * @return true if the extension of objectPropertyExpression1 is the same as the extension of objectPropertyExpression2 
     *         in each model of the ontology and false otherwise
     */
    public boolean isEquivalentProperty(OWLObjectPropertyExpression objectPropertyExpression1,OWLObjectPropertyExpression objectPropertyExpression2) {
        return isSubPropertyOf(objectPropertyExpression1,objectPropertyExpression2) && isSubPropertyOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getSubProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSuperProperties((OWLObjectPropertyExpression)property));
    }
    /**
     * Returns the DIRECT super-properties of propertyExpression. For retrieving also indirect super-properties use getAncestorProperties(OWLObjectPropertyExpression). 
     * @see org.semanticweb.HermiT.Reasoner#getAncestorProperties(OWLObjectPropertyExpression)
     * @param propertyExpression - an object property expression
     * @return a set of sets {S1, ..., Sn} such that all properties in a set Si are equivalent and each set Si 
     *         contains DIRECT super-properties of propertyExpression
     */
    public Set<Set<OWLObjectPropertyExpression>> getSuperProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getSubProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSubProperties((OWLObjectPropertyExpression)property));
    }
    /**
     * Returns the DIRECT sub-properties of propertyExpression. For retrieving also indirect sub-properties use getDescendantProperties(OWLObjectPropertyExpression). 
     * @see org.semanticweb.HermiT.Reasoner#getDescendantProperties(OWLObjectPropertyExpression)
     * @param propertyExpression - an object property expression
     * @return a set of sets {S1, ..., Sn} such that all properties in a set Si are equivalent and each set Si 
     *         contains sub-properties of propertyExpression on a different level of the ancestror hierarchy 
     */
    public Set<Set<OWLObjectPropertyExpression>> getSubProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getAncestorProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getAncestorProperties((OWLObjectPropertyExpression)property));
    }
    /**
     * Returns the super-properties of propertyExpression. 
     * @param propertyExpression - an object property expression
     * @return a set of sets {S1, ..., Sn} such that all properties in a set Si are equivalent and each set Si 
     *         contains super-properties of propertyExpression on different levels of the property hierarchy
     */
    public Set<Set<OWLObjectPropertyExpression>> getAncestorProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> ancestorsPlusNode=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
        ancestorsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(ancestorsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getDescendantProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getDescendantProperties((OWLObjectPropertyExpression)property));
    }
    /**
     * Returns the sub-properties of propertyExpression. 
     * @param propertyExpression - an object property expression
     * @return a set of sets {S1, ..., Sn} such that all properties in a set Si are equivalent and each set Si 
     *         contains sub-properties of propertyExpression on different levels of the property hierarchy
     */
    public Set<Set<OWLObjectPropertyExpression>> getDescendantProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> descendantsPlusNode=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
        descendantsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(descendantsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#getEquivalentProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) {
        return filterObjectProperties(getEquivalentProperties((OWLObjectPropertyExpression)property));
    }
    /**
     * Returns properties that are equivalent to propertyExpression. 
     * @param propertyExpression - an object property expression
     * @return a set of object property expressions such that all properties in the set are equivalent to propertyExpression
     */
    public Set<OWLObjectPropertyExpression> getEquivalentProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    protected HierarchyNode<Role> getHierarchyNode(OWLObjectPropertyExpression propertyExpression) {
        classifyObjectProperties();
        Role role;
        if (propertyExpression.getSimplified().isAnonymous()) 
            role=InverseRole.create(AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString()));
        else 
            role=AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString());
        HierarchyNode<Role> node=m_objectRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_objectRoleHierarchy.getTopNode()),Collections.singleton(m_objectRoleHierarchy.getBottomNode()));
        return node;
    }

    /**
     * use {@link org.semanticweb.HermiT.Reasoner#getDomains(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLClassExpression>> getDomains(OWLObjectProperty property) {
        return getDomains((OWLObjectPropertyExpression)property);
    }
    /**
     * Gets the domains of a particular property.  A domain class, A, of a property, P,
     * is a named class such that A is an ancestor of \exists p Top ("p some Thing"). 
     * @param propertyExpression - The property whose domains are to be retrieved.
     * @return The domains of the property.  A set of sets of (named) equivalence classes.
     */
    @SuppressWarnings("unchecked")
    public Set<Set<OWLClassExpression>> getDomains(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLObjectSomeValuesFrom(propertyExpression,factory.getOWLThing()));
        return (Set<Set<OWLClassExpression>>)object;
    }

    /**
     * use {@link org.semanticweb.HermiT.Reasoner#getRanges(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<OWLClassExpression> getRanges(OWLObjectProperty property) {
        return getRanges((OWLObjectPropertyExpression)property);
    }
    /**
     * Gets the ranges of the given property. A class A is a range of a property P,
     * if A is a named class such that each P-successor of an individual is necessarily in the extension of A. 
     * @param propertyExpression - an object property expression
     * @return A set of (named) classes such that each class in the set is a range for propertyExpression 
     */
    public Set<OWLClassExpression> getRanges(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLClassExpression> result=new HashSet<OWLClassExpression>();
        Set<Set<OWLClass>> ranges=getAncestorClasses(factory.getOWLObjectSomeValuesFrom(propertyExpression.getInverseProperty(),factory.getOWLThing()));
        for (Set<OWLClass> classSet : ranges)
            result.addAll(classSet);
        return result;
    }
    
    /**
     * use {@link org.semanticweb.HermiT.Reasoner#getInverseProperties(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property) {
        Set<OWLObjectProperty> result=new HashSet<OWLObjectProperty>();
        Set<OWLObjectPropertyExpression> equivalentToInverse=getInverseProperties((OWLObjectPropertyExpression)property);
        for (OWLObjectPropertyExpression objectPropertyExpression : equivalentToInverse)
            if (objectPropertyExpression instanceof OWLObjectProperty)
                result.add((OWLObjectProperty)objectPropertyExpression);
        Set<Set<OWLObjectProperty>> setOfSets=new HashSet<Set<OWLObjectProperty>>();
        setOfSets.add(result);
        return setOfSets;
    }
    /** 
     * @param property - an object property expression
     * @return a set of object property expressions such that each property in th set is equivalent to the inverse of property
     */
    public Set<OWLObjectPropertyExpression> getInverseProperties(OWLObjectPropertyExpression property) {
        return getEquivalentProperties(property.getInverseProperty());
    }
    
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isFunctional(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isFunctional(OWLObjectProperty property) {
        return isFunctional((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if each individual can have at most one outgoing connection of the specified object property expression  
     */
    public boolean isFunctional(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,propertyExpression));
    }

    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isInverseFunctional(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isInverseFunctional(OWLObjectProperty property) {
        return isInverseFunctional((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if each individual can have at most one incoming connection of the specified object property expression  
     */
    public boolean isInverseFunctional(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,propertyExpression.getInverseProperty()));
    }
    
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isIrreflexive(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isIrreflexive(OWLObjectProperty property) {
        return isIrreflexive((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if the extension of propertyExpression is an irreflexive relation in each model of the loaded ontology  
     */
    public boolean isIrreflexive(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectHasSelf(propertyExpression));
    }

    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isReflexive(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isReflexive(OWLObjectProperty property) {
        return isReflexive((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if the extension of propertyExpression is a reflexive relation in each model of the loaded ontology  
     */
    public boolean isReflexive(OWLObjectPropertyExpression propertyExpression) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectComplementOf(factory.getOWLObjectHasSelf(propertyExpression)));
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isAsymmetric(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isAntiSymmetric(OWLObjectProperty property) throws OWLReasonerException {
        return isAsymmetric((OWLObjectPropertyExpression)property);
    }
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isAsymmetric(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isAsymmetric(OWLObjectProperty property) {
        return isAsymmetric((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if the extension of propertyExpression is an asymmetric relation in each model of the loaded ontology 
     */
    public boolean isAsymmetric(OWLObjectPropertyExpression propertyExpression) {
        Role role;
        if (propertyExpression.getSimplified().isAnonymous()) {
            AtomicRole atomicRole=AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString());
            role=InverseRole.create(atomicRole);
        } else {
            role=AtomicRole.create(propertyExpression.getSimplified().asOWLObjectProperty().getIRI().toString());
        }
        return m_tableau.isAsymmetric(role);
    }
    
    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isSymmetric(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isSymmetric(OWLObjectProperty property) {
        return isSymmetric((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if the extension of propertyExpression is a symmetric relation in each model of the loaded ontology 
     */
    public boolean isSymmetric(OWLObjectPropertyExpression propertyExpression) {
        if (propertyExpression.isOWLTopObjectProperty()) return true;        
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom assertion=factory.getOWLObjectPropertyAssertionAxiom(propertyExpression,individualA,individualB);
        OWLObjectAllValuesFrom all=factory.getOWLObjectAllValuesFrom(propertyExpression, factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(individualA)));
        OWLAxiom assertion2=factory.getOWLClassAssertionAxiom(all, individualB);
        Tableau tableau=getTableau(ontologyManager,assertion,assertion2);
        return !tableau.isABoxSatisfiable();
    }

    /**
     * Deprecated, use {@link org.semanticweb.HermiT.Reasoner#isTransitive(OWLObjectPropertyExpression)}
     */
    @Deprecated
    public boolean isTransitive(OWLObjectProperty property) {
        return isTransitive((OWLObjectPropertyExpression)property);
    }
    /**
     * @param propertyExpression - an object property expression
     * @return true if the extension of propertyExpression is a trnsitive relation in each model of the loaded ontology 
     */
    public boolean isTransitive(OWLObjectPropertyExpression propertyExpression) {
        List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();
        chain.add(propertyExpression);
        chain.add(propertyExpression);
        return isSubPropertyOf(chain, propertyExpression);
    }

    // Data property inferences

    /**
     * @return true if the dta property hierarchy has been computed and false otherwise
     */
    public boolean areDataPropertiesClassified() {
        return m_dataRoleHierarchy!=null;
    }
    
    /**
     * Builds the data property hierarchy. 
     */
    public void classifyDataProperties() {
        if (m_dataRoleHierarchy==null) {
            try {
                Set<Role> allDataRoles=new HashSet<Role>(m_dlOntology.getAllAtomicDataRoles());
                allDataRoles.add(AtomicRole.TOP_DATA_ROLE);
                allDataRoles.add(AtomicRole.BOTTOM_DATA_ROLE);
                final int numRoles=allDataRoles.size();
                if (m_progressMonitor!=null) {
                    m_progressMonitor.setMessage("Classifying data propoerties...");
                    m_progressMonitor.setProgress(0);
                    m_progressMonitor.setStarted();
                }
                
                if (!m_dataRoleSubsumptionCache.isSatisfiable(AtomicRole.TOP_DATA_ROLE))
                    m_dataRoleHierarchy=Hierarchy.emptyHierarchy(allDataRoles,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE);
                else if (m_dataRoleSubsumptionCache.canGetAllSubsumersEasily()) {
                    Map<Role,DeterministicHierarchyBuilder.GraphNode<Role>> allSubsumers=new HashMap<Role,DeterministicHierarchyBuilder.GraphNode<Role>>();
                    int processedRoles=0;
                    for (Role role : allDataRoles) {
                        Set<Role> subsumers=m_dataRoleSubsumptionCache.getAllKnownSubsumers(role);
                        if (subsumers==null)
                            subsumers=allDataRoles;
                        allSubsumers.put(role,new DeterministicHierarchyBuilder.GraphNode<Role>(role,subsumers));
                        if (m_progressMonitor!=null) {
                            processedRoles++;
                            m_progressMonitor.setMessage("Classifying data properties...");
                            m_progressMonitor.setProgress(processedRoles/numRoles);
                        }
                    }
                    DeterministicHierarchyBuilder<Role> hierarchyBuilder=new DeterministicHierarchyBuilder<Role>(allSubsumers,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE);
                    m_dataRoleHierarchy=hierarchyBuilder.buildHierarchy();
                }
                if (m_dataRoleHierarchy==null) {
                    HierarchyBuilder.Relation<Role> relation=
                        new HierarchyBuilder.Relation<Role>() {
                            public boolean doesSubsume(Role sup,Role sub) {
                                return m_dataRoleSubsumptionCache.isSubsumedBy(sub,sup);
                            }
                        };
                    HierarchyBuilder.ClassificationProgressMonitor<Role> progressMonitor;
                    if (m_progressMonitor==null)
                        progressMonitor=null;
                    else
                        progressMonitor=
                            new HierarchyBuilder.ClassificationProgressMonitor<Role>() {
                                protected int m_processedRoles=0;
                                public void elementClassified(Role element) {
                                    m_processedRoles++;
                                    m_progressMonitor.setMessage("Building the data property hierarchy...");
                                    m_progressMonitor.setProgress(m_processedRoles/numRoles);
                                }
                            };
                    HierarchyBuilder<Role> hierarchyBuilder=new HierarchyBuilder<Role>(relation,progressMonitor);
                    m_dataRoleHierarchy=hierarchyBuilder.buildHierarchy(AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE,allDataRoles);
                }
            }
            finally {
                if (m_progressMonitor!=null)
                    m_progressMonitor.setFinished();
            }
        }
    }
    
    /**
     * Determines if the property chain represented by subPropertyChain is a sub-property of superObjectPropertyExpression. 
     * @param subDataProperty - a data property 
     * @param superDataProperty - a data property
     * @return true if whenever a pair related with the property subDataProperty is necessarily 
     *         related with the property superDataProperty and false otherwise
     */
    public boolean isSubPropertyOf(OWLDataProperty subDataProperty,OWLDataProperty superDataProperty) {
        return m_dataRoleSubsumptionCache.isSubsumedBy(AtomicRole.create(subDataProperty.getIRI().toString()), AtomicRole.create(superDataProperty.getIRI().toString()));
    }
    
    /**
     * @param dataProperty1 - a data property 
     * @param dataProperty2 - a data property 
     * @return true if the extension of dataProperty1 is the same as the extension of dataProperty2 
     *         in each model of the ontology and false otherwise
     */
    public boolean isEquivalentProperty(OWLDataProperty dataProperty1,OWLDataProperty dataProperty2) {
        return isSubPropertyOf(dataProperty1,dataProperty2) && isSubPropertyOf(dataProperty2,dataProperty1);
    }
    
    /**
     * Returns the most specific direct super-properties of the given property. 
     * @return a set of sets {S1, ..., Sn} such that the data properties in each set Si are equivalent to each other
     *         and all returned data properties are direct/most specific super-properties of the given one 
     */
    public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        return dataPropertyNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    /**
     * Returns the most specific direct sub-properties of the given property. 
     * @return a set of sets {S1, ..., Sn} such that the data properties in each set Si are equivalent to each other
     *         and all returned data properties are direct/most specific sub-properties of the given one 
     */
    public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        return dataPropertyNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /**
     * Returns the (direct and indirect) super-properties of the given property including the property itself. 
     * @return a set of sets {S1, ..., Sn} such that the data properties in each set Si are equivalent to each other
     *         and all returned data properties are super-properties of the given one or equivalent to the given one
     */
    public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        Set<HierarchyNode<Role>> ancestorsPlusNode=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
        ancestorsPlusNode.add(node);
        return dataPropertyNodesToOWLAPI(ancestorsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    /**
     * Returns the (direct and indirect) sub-properties of the given property including the property itself. 
     * @return a set of sets {S1, ..., Sn} such that the data properties in each set Si are equivalent to each other
     *         and all returned data properties are sub-properties of the given one or equivalent to the given one
     */
    public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        Set<HierarchyNode<Role>> descendantsPlusNode=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
        descendantsPlusNode.add(node);
        return dataPropertyNodesToOWLAPI(descendantsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    /**
     * Returns properties that are equivalent to the given property. 
     * @param property - a data property
     * @return a set of data properties such that all properties in the set are equivalent to the given property
     */
    public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) {
        HierarchyNode<Role> node=getHierarchyNode(property);
        return dataPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    protected HierarchyNode<Role> getHierarchyNode(OWLDataProperty property) {
        classifyDataProperties();
        Role role=AtomicRole.create(property.getIRI().toString());
        HierarchyNode<Role> node=m_dataRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_dataRoleHierarchy.getTopNode()),Collections.singleton(m_dataRoleHierarchy.getBottomNode()));
        return node;
    }
    
    /**
     * Gets the domains of the given data property. A class A is the domain of a property P,
     * if it is a named class such that A is an ancestor of \exists p Top ("p some Thing"). 
     * @param property - The data property whose domains are to be retrieved.
     * @return The domains of the property.  A set of sets of (named) equivalence classes.
     */
    @SuppressWarnings("unchecked")
    public Set<Set<OWLClassExpression>> getDomains(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLDataSomeValuesFrom(property,factory.getTopDatatype()));
        return (Set<Set<OWLClassExpression>>)object;
    }


    /** 
     * This method is not supported by HermiT and executing it will result in an UnsupportedOperation error. 
     * The method is required to implement the OWLReasoner interface.
     *  
     * {@inheritDoc}
     */
    public Set<OWLDataRange> getRanges(OWLDataProperty property) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param property - a data property
     * @return true if each individual can have at most one outgoing connection of the specified data property
     */
    public boolean isFunctional(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLDataMinCardinality(2,property));
    }

    // Individual inferences
    
    public boolean isRealised() {
        return m_realization!=null;
    }
    
    public void realise() {
        if (m_realization==null) {
            m_realization=new HashMap<AtomicConcept,Set<Individual>>();
            for (Individual individual : m_dlOntology.getAllIndividuals()) {
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
            }
        }
    }
   
    protected Set<HierarchyNode<AtomicConcept>> getDirectSuperConceptNodes(final Individual individual) {
        classify();
        HierarchyBuilder.SearchPredicate<HierarchyNode<AtomicConcept>> predicate=new HierarchyBuilder.SearchPredicate<HierarchyNode<AtomicConcept>>() {
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
                    return m_tableau.isInstanceOf(atomicConcept,individual);
            }
        };
        Set<HierarchyNode<AtomicConcept>> topPositions=Collections.singleton(m_atomicConceptHierarchy.getTopNode());
        return HierarchyBuilder.search(predicate,topPositions,null);
    }
    
    public Set<Set<OWLClass>> getTypes(OWLNamedIndividual owlIndividual,boolean direct) {
        Individual individual=Individual.create(owlIndividual.getIRI().toString(),true);
        Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(individual);
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<Set<OWLClass>> result=atomicConceptNodesToOWLAPI(directSuperConceptNodes,factory);
        if (!direct)
            for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes)
                result.addAll(atomicConceptNodesToOWLAPI(directSuperConceptNode.getAncestorNodes(),factory));
        return result;
    }
    public boolean hasType(OWLNamedIndividual owlIndividual,OWLClassExpression type,boolean direct) {
        if (direct || isRealised()) {
            return getIndividuals(type,direct).contains(owlIndividual);
        } else {
            Individual individual=Individual.create(owlIndividual.getIRI().toString(),false);
            if (type instanceof OWLClass) {
                AtomicConcept concept=AtomicConcept.create(((OWLClass)type).getIRI().toString());
                return m_tableau.isInstanceOf(concept,individual);
            } else {
                OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
                OWLDataFactory factory=ontologyManager.getOWLDataFactory();
                OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
                OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(type,newClass);
                Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
                return tableau.isInstanceOf(AtomicConcept.create("internal:query-concept"),individual);
            }
        }
    }
    public Set<OWLNamedIndividual> getIndividuals(OWLClassExpression description,boolean direct) {
        realise();
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            Set<OWLNamedIndividual> result=new HashSet<OWLNamedIndividual>();
            Set<Individual> instances=m_realization.get(concept);
            if (instances!=null)
                for (Individual instance : instances)
                    result.add(factory.getOWLNamedIndividual(IRI.create(instance.getIRI())));
            if (!direct) {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                if (node!=null)
                    for (HierarchyNode<AtomicConcept> descendantNode : node.getDescendantNodes())
                        loadIndividualsOfNode(descendantNode,result,factory);
            }
            return result;
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassOfAxiom(description,newClass);
            Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            AtomicConcept queryConcept=AtomicConcept.create("internal:query-concept");
            HierarchyNode<AtomicConcept> hierarchyNode=getHierarchyNode(description);
            Set<OWLNamedIndividual> result=new HashSet<OWLNamedIndividual>();
            loadIndividualsOfNode(hierarchyNode,result,factory);
            if (!direct)
                for (HierarchyNode<AtomicConcept> descendantNode : hierarchyNode.getDescendantNodes())
                    loadIndividualsOfNode(descendantNode,result,factory);
            Set<HierarchyNode<AtomicConcept>> visitedNodes=new HashSet<HierarchyNode<AtomicConcept>>(hierarchyNode.getChildNodes());
            List<HierarchyNode<AtomicConcept>> toVisit=new ArrayList<HierarchyNode<AtomicConcept>>(hierarchyNode.getParentNodes());
            while (!toVisit.isEmpty()) {
                HierarchyNode<AtomicConcept> node=toVisit.remove(toVisit.size()-1);
                if (visitedNodes.add(node)) {
                    AtomicConcept nodeAtomicConcept=node.getEquivalentElements().iterator().next();
                    Set<Individual> realizationForNodeConcept=m_realization.get(nodeAtomicConcept);
                    if (realizationForNodeConcept!=null)
                        for (Individual individual : realizationForNodeConcept)
                            if (tableau.isInstanceOf(queryConcept,individual))
                                result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
                    toVisit.addAll(node.getChildNodes());
                }
            }
            return result;
        }
    }
    protected void loadIndividualsOfNode(HierarchyNode<AtomicConcept> node,Set<OWLNamedIndividual> result,OWLDataFactory factory) {
        AtomicConcept atomicConcept=node.getEquivalentElements().iterator().next();
        Set<Individual> realizationForConcept=m_realization.get(atomicConcept);
        // RealizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null)
            for (Individual individual : realizationForConcept) {
                if (individual.isNamed()) 
                    result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
            }
    }

    public Map<OWLObjectProperty,Set<OWLNamedIndividual>> getObjectPropertyRelationships(OWLNamedIndividual individual) {
		Map<OWLObjectProperty, Set<OWLNamedIndividual>> objectPropertyRelationships = new HashMap<OWLObjectProperty, Set<OWLNamedIndividual>>();
		Set<Role> allAtomicObjectRoles = new HashSet<Role>();
		allAtomicObjectRoles.addAll( m_dlOntology.getAllAtomicObjectRoles() );
		Set<OWLObjectPropertyExpression> objProperties = objectPropertiesToOWLAPI( allAtomicObjectRoles, OWLManager.createOWLOntologyManager().getOWLDataFactory() );
		for( OWLObjectPropertyExpression objProp : objProperties ) {
			Set<OWLNamedIndividual> relatedIndvs = getRelatedIndividuals( individual, objProp );
			if( !relatedIndvs.isEmpty() )
				objectPropertyRelationships.put( objProp.asOWLObjectProperty(), relatedIndvs );
		}
		return objectPropertyRelationships;
    }

    public Map<OWLDataProperty,Set<OWLLiteral>> getDataPropertyRelationships(OWLNamedIndividual individual) {
        throw new UnsupportedOperationException();
    }

    public Set<OWLNamedIndividual> getRelatedIndividuals(OWLNamedIndividual subject,OWLObjectPropertyExpression property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getIndividuals(factory.getOWLObjectSomeValuesFrom(property.getInverseProperty(),factory.getOWLObjectOneOf(subject)),false);
    }
    
    public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual subject,OWLDataPropertyExpression property) {
        throw new UnsupportedOperationException();
    }

    public boolean hasObjectPropertyRelationship(OWLNamedIndividual subject,OWLObjectPropertyExpression property,OWLNamedIndividual object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLObjectSomeValuesFrom(property,factory.getOWLObjectOneOf(object)),false);
    }
    public boolean hasDataPropertyRelationship(OWLNamedIndividual subject,OWLDataPropertyExpression property,OWLLiteral object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLDataHasValue(property,object),false);
    }

    // other inferences

    public Boolean hasKey(OWLHasKeyAxiom key) {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLClassAssertionAxiom(key.getClassExpression(), individualA));
        axioms.add(factory.getOWLClassAssertionAxiom(key.getClassExpression(), individualB));
        int i=0;
        for (OWLObjectPropertyExpression p : key.getObjectPropertyExpressions()) {
            OWLIndividual tmp=factory.getOWLNamedIndividual(IRI.create("internal:individual"+i));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p, individualA, tmp));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p, individualB, tmp));
            i++;
        }
        for (OWLDataPropertyExpression p : key.getDataPropertyExpressions()) {
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant"+i,anonymousConstantsDatatype);
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p, individualA, constant));
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p, individualB, constant));
            i++;
        }
        axioms.add(factory.getOWLDifferentIndividualsAxiom(individualA,individualB));
        Tableau tableau=getTableau(ontologyManager,axioms.toArray(new OWLAxiom[axioms.size()]));
        return !tableau.isABoxSatisfiable();
        
    }
    
    protected Boolean entailsDatatypeDefinition(OWLDatatypeDefinitionAxiom axiom) {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLDataProperty newDP=factory.getOWLDataProperty(IRI.create("internal:internalDP"));
        OWLDataRange dr=axiom.getDataRange();
        OWLDatatype dt=axiom.getDatatype();
        OWLDataIntersectionOf dr1=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dr), dt);
        OWLDataIntersectionOf dr2=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dt), dr);
        OWLDataUnionOf union=factory.getOWLDataUnionOf(dr1, dr2);
        OWLClassExpression c=factory.getOWLDataSomeValuesFrom(newDP, union);
        OWLClassAssertionAxiom ax=factory.getOWLClassAssertionAxiom(c, individualA);
        Tableau tableau=getTableau(ontologyManager,ax);
        return !tableau.isABoxSatisfiable();
    }
    
    
    
    // Various creation methods
    
    public Tableau getTableau(OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        if (additionalAxioms==null || additionalAxioms.length==0)
            return m_tableau;
        else {
            DLOntology newDLOntology=extendDLOntology(m_configuration,m_prefixes,"uri:urn:internal-kb",m_dlOntology,ontologyManager,additionalAxioms);
            return createTableau(m_interruptFlag,m_configuration,newDLOntology,m_prefixes);
        }
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
        if (config.blockingStrategyType==BlockingStrategyType.VALIDATED) {
            directBlockingChecker=new ValidatedDirectBlockingChecker();
        } else {
            switch (config.directBlockingType) {
            case OPTIMAL:
                if (dlOntology.hasInverseRoles() && dlOntology.hasAtMostRestrictions())
                    directBlockingChecker=new PairWiseDirectBlockingChecker();
                else
                    directBlockingChecker=new SingleDirectBlockingChecker();
                break;
            case SINGLE:
                directBlockingChecker=new SingleDirectBlockingChecker();
                break;
            case PAIR_WISE:
                directBlockingChecker=new PairWiseDirectBlockingChecker();
                break;
            default:
                throw new IllegalArgumentException("Unknown direct blocking type.");
            }
        }
        
        BlockingSignatureCache blockingSignatureCache=null;
        if (!dlOntology.hasNominals()) {
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
        case VALIDATED:
            blockingStrategy=new AnywhereValidatedBlocking(directBlockingChecker,blockingSignatureCache,dlOntology.getUnaryValidBlockConditions(),dlOntology.getNAryValidBlockConditions(),dlOntology.hasInverseRoles());
            //blockingStrategy=new AnywhereValidatedBlocking2(directBlockingChecker,blockingSignatureCache,dlOntology.getUnaryValidBlockConditions(),dlOntology.getNAryValidBlockConditions(),dlOntology.hasInverseRoles());
            break;
        case ANCESTOR:
            blockingStrategy=new AncestorBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case ANYWHERE:
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

        return new Tableau(interruptFlag,tableauMonitor,existentialsExpansionStrategy,dlOntology,config.parameters);
    }

    protected static DLOntology extendDLOntology(Configuration config,Prefixes prefixes,String resultingOntologyIRI,DLOntology originalDLOntology,OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        try {
            Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLOntology newOntology=ontologyManager.createOntology(IRI.create("uri:urn:internal-kb"));
            for (OWLAxiom axiom : additionalAxioms)
                ontologyManager.addAxiom(newOntology,axiom);
            OWLAxioms axioms=new OWLAxioms();
            axioms.m_definedDatatypesIRIs.addAll(originalDLOntology.getDefinedDatatypeIRIs());
            OWLNormalization normalization=new OWLNormalization(factory,axioms,!originalDLOntology.getAllDescriptionGraphs().isEmpty());
            normalization.processOntology(config,newOntology);
            BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);   
            builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms,
                originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_OBJECT_ROLE),
                originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_OBJECT_ROLE),
                originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_DATA_ROLE),
                originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.BOTTOM_DATA_ROLE)
            );
            if (!originalDLOntology.getAllComplexObjectRoleInclusions().isEmpty() || !axioms.m_complexObjectPropertyInclusions.isEmpty()) {
                ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(factory);
                objectPropertyInclusionManager.prepareTransformation(axioms);
                for (DLClause dlClause : originalDLOntology.getDLClauses()) {
                    if (dlClause.isRoleInclusion()) {
                        AtomicRole subAtomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                        AtomicRole superAtomicRole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                        if (originalDLOntology.getAllAtomicObjectRoles().contains(subAtomicRole) && originalDLOntology.getAllAtomicObjectRoles().contains(superAtomicRole)) {
                            OWLObjectProperty subObjectProperty=getObjectProperty(factory,subAtomicRole);
                            OWLObjectProperty superObjectProperty=getObjectProperty(factory,superAtomicRole);
                            objectPropertyInclusionManager.addInclusion(subObjectProperty,superObjectProperty);
                        }
                    }
                    else if (dlClause.isRoleInverseInclusion()) {
                        AtomicRole subAtomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                        AtomicRole superAtomicRole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                        if (originalDLOntology.getAllAtomicObjectRoles().contains(subAtomicRole) && originalDLOntology.getAllAtomicObjectRoles().contains(superAtomicRole)) {
                            OWLObjectProperty subObjectProperty=getObjectProperty(factory,subAtomicRole);
                            OWLObjectPropertyExpression superObjectPropertyExpression=getObjectProperty(factory,superAtomicRole).getInverseProperty();
                            objectPropertyInclusionManager.addInclusion(subObjectProperty,superObjectPropertyExpression);
                        }
                    }
                }
                /**
                 * gstoil
                 */
                objectPropertyInclusionManager.rewriteAxioms(axioms,originalDLOntology.getAutomataOfComplexObjectProperties());
            }
            OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
            axiomsExpressivity.m_hasAtMostRestrictions|=originalDLOntology.hasAtMostRestrictions();
            axiomsExpressivity.m_hasInverseRoles|=originalDLOntology.hasInverseRoles();
            axiomsExpressivity.m_hasNominals|=originalDLOntology.hasNominals();
            axiomsExpressivity.m_hasDatatypes|=originalDLOntology.hasDatatypes();
            OWLClausification clausifier=new OWLClausification(config);
            DLOntology newDLOntology=clausifier.clausify(ontologyManager.getOWLDataFactory(),"uri:urn:internal-kb",axioms,axiomsExpressivity,descriptionGraphs);
            
            Set<DLClause> dlClauses=createUnion(originalDLOntology.getDLClauses(),newDLOntology.getDLClauses());
            Set<Atom> positiveFacts=createUnion(originalDLOntology.getPositiveFacts(),newDLOntology.getPositiveFacts());
            Set<Atom> negativeFacts=createUnion(originalDLOntology.getNegativeFacts(),newDLOntology.getNegativeFacts());
            Set<AtomicConcept> atomicConcepts=createUnion(originalDLOntology.getAllAtomicConcepts(),newDLOntology.getAllAtomicConcepts());
            Set<DLOntology.ComplexObjectRoleInclusion> complexObjectRoleInclusions=createUnion(originalDLOntology.getAllComplexObjectRoleInclusions(),newDLOntology.getAllComplexObjectRoleInclusions());
            Set<AtomicRole> atomicObjectRoles=createUnion(originalDLOntology.getAllAtomicObjectRoles(),newDLOntology.getAllAtomicObjectRoles());
            Set<AtomicRole> atomicDataRoles=createUnion(originalDLOntology.getAllAtomicDataRoles(),newDLOntology.getAllAtomicDataRoles());
            Set<String> definedDatatypeIRIs=createUnion(originalDLOntology.getDefinedDatatypeIRIs(),newDLOntology.getDefinedDatatypeIRIs());
            Set<Individual> individuals=createUnion(originalDLOntology.getAllIndividuals(),newDLOntology.getAllIndividuals());
            boolean hasInverseRoles=originalDLOntology.hasInverseRoles() || newDLOntology.hasInverseRoles();
            boolean hasAtMostRestrictions=originalDLOntology.hasAtMostRestrictions() || newDLOntology.hasAtMostRestrictions();
            boolean hasNominals=originalDLOntology.hasNominals() || newDLOntology.hasNominals();
            boolean hasDatatypes=originalDLOntology.hasDatatypes() || newDLOntology.hasDatatypes();
//            if (config.blockingStrategyType==Configuration.BlockingStrategyType.CORE) {
//                Map<AtomicConcept,Set<Set<Concept>>> unaryValidBlockConditions=createUnion(originalDLOntology.getUnaryValidBlockConditions(),newDLOntology.getUnaryValidBlockConditions());
//                Map<Set<AtomicConcept>,Set<Set<Concept>>> nAryValidBlockConditions=createUnion(originalDLOntology.getNAryValidBlockConditions(),newDLOntology.getNAryValidBlockConditions());
//                return new DLOntology(resultingOntologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,complexObjectRoleInclusions,atomicObjectRoles,atomicDataRoles,definedDatatypeIRIs,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,hasDatatypes,unaryValidBlockConditions,nAryValidBlockConditions);
//            }
            return new DLOntology(resultingOntologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,complexObjectRoleInclusions,atomicObjectRoles,atomicDataRoles,definedDatatypeIRIs,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,hasDatatypes);
        }
        catch (OWLException shouldntHappen) {
            throw new IllegalStateException("Internal error: Unexpected OWLException.",shouldntHappen);
        }
    }
    
    protected static <T> Set<T> createUnion(Set<T> set1,Set<T> set2) {
        Set<T> result=new HashSet<T>();
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }
    protected static <K,V> Map<K,V> createUnion(Map<K,V> map1,Map<K,V> map2) {
        Map<K,V> result=new HashMap<K,V>();
        if (map1!=null) result.putAll(map1);
        if (map2!=null)result.putAll(map2);
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
            buffer.insert(0,(char)(((int)'a')+(prefixIndex % 26)));
            prefixIndex/=26;
        }
        buffer.insert(0,(char)(((int)'a')+prefixIndex));
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

    // Loading and saving the Reasoner object

    public void save(File file) throws IOException {
        OutputStream outputStream=new BufferedOutputStream(new FileOutputStream(file));
        try {
            save(outputStream);
        }
        finally {
            outputStream.close();
        }
    }

    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }

    public static Reasoner loadReasoner(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            return (Reasoner)objectInputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            IOException error=new IOException();
            error.initCause(e);
            throw error;
        }
    }

    public static Reasoner loadReasoner(File file) throws IOException {
        InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
        try {
            return loadReasoner(inputStream);
        }
        finally {
            inputStream.close();
        }
    }
    
    // Various utility methods
    
    protected static Set<OWLClass> atomicConceptsToOWLAPI(Collection<AtomicConcept> atomicConcepts,OWLDataFactory factory) {
        Set<OWLClass> result=new HashSet<OWLClass>();
        for (AtomicConcept concept : atomicConcepts) {
            if (!Prefixes.isInternalIRI(concept.getIRI()))
                result.add(factory.getOWLClass(IRI.create(concept.getIRI())));
        }
        return result;
    }

    protected static Set<Set<OWLClass>> atomicConceptNodesToOWLAPI(Collection<HierarchyNode<AtomicConcept>> nodes,OWLDataFactory factory) {
        Set<Set<OWLClass>> result=new HashSet<Set<OWLClass>>();
        for (HierarchyNode<AtomicConcept> node : nodes) {
            result.add(atomicConceptsToOWLAPI(node.getEquivalentElements(),factory));
        }
        return result;
    }

    protected static Set<OWLObjectPropertyExpression> objectPropertiesToOWLAPI(Collection<Role> roles,OWLDataFactory factory) {
        Set<OWLObjectPropertyExpression> result=new HashSet<OWLObjectPropertyExpression>();
        for (Role role : roles)
            result.add(getObjectPropertyExpression(factory,role));
        return result;
    }
    protected static Set<Set<OWLObjectPropertyExpression>> objectPropertyNodesToOWLAPI(Collection<HierarchyNode<Role>> nodes,OWLDataFactory factory) {
        Set<Set<OWLObjectPropertyExpression>> result=new HashSet<Set<OWLObjectPropertyExpression>>();
        for (HierarchyNode<Role> node : nodes)
            result.add(objectPropertiesToOWLAPI(node.getEquivalentElements(),factory));
        return result;
    }

    protected static Set<OWLObjectProperty> filterObjectProperties(Set<OWLObjectPropertyExpression> set) {
        Set<OWLObjectProperty> result=new HashSet<OWLObjectProperty>();
        for (OWLObjectPropertyExpression expression : set)
            if (expression instanceof OWLObjectProperty)
                result.add((OWLObjectProperty)expression);
        return result;
    }
    protected static Set<Set<OWLObjectProperty>> filterObjectPropertySets(Set<Set<OWLObjectPropertyExpression>> setOfSets) {
        Set<Set<OWLObjectProperty>> result=new HashSet<Set<OWLObjectProperty>>();
        for (Set<OWLObjectPropertyExpression> set : setOfSets) {
            Set<OWLObjectProperty> filteredSet=filterObjectProperties(set);
            if (!filteredSet.isEmpty())
                result.add(filteredSet);
        }
        return result;
    }
    
    protected static Set<OWLDataProperty> dataPropertiesToOWLAPI(Collection<Role> dataProperties,OWLDataFactory factory) {
        Set<OWLDataProperty> result=new HashSet<OWLDataProperty>();
        for (Role role : dataProperties)
            result.add(factory.getOWLDataProperty(IRI.create(((AtomicRole)role).getIRI())));
        return result;
    }
    
    protected static Set<Set<OWLDataProperty>> dataPropertyNodesToOWLAPI(Collection<HierarchyNode<Role>> nodes,OWLDataFactory factory) {
        Set<Set<OWLDataProperty>> result=new HashSet<Set<OWLDataProperty>>();
        for (HierarchyNode<Role> node : nodes)
            result.add(dataPropertiesToOWLAPI(node.getEquivalentElements(),factory));
        return result;
    }

    protected static <T> void addInclusion(Map<T,DeterministicHierarchyBuilder.GraphNode<T>> knownSubsumers,T subElement,T supElement) {
        DeterministicHierarchyBuilder.GraphNode<T> subGraphNode=knownSubsumers.get(subElement);
        if (subGraphNode==null) {
            subGraphNode=new DeterministicHierarchyBuilder.GraphNode<T>(subElement,new HashSet<T>());
            knownSubsumers.put(subElement,subGraphNode);
        }
        subGraphNode.m_successors.add(supElement);
    }
    
    // The factory for OWL API OWL reasoners
    
    public static class ReasonerFactory implements OWLReasonerFactory {

        public OWLReasoner createReasoner(OWLOntologyManager manager) {
            return this.createReasoner(manager, new HashSet<OWLOntology>());
        }
        public String getReasonerName() {
            return getClass().getPackage().getImplementationTitle();
        }
        /* (non-Javadoc)
         * @see org.semanticweb.owlapi.inference.OWLReasonerFactory#createReasoner(org.semanticweb.owlapi.model.OWLOntologyManager, java.util.Set)
         */
        public OWLReasoner createReasoner(OWLOntologyManager manager,Set<OWLOntology> ontologies) {
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            Reasoner hermit=new Reasoner(configuration,manager.getOWLDataFactory(),ontologies);
            return hermit;
        }
    }
   
    // The factory for the reasoner from the Protege plug-in
    public static class ProtegeReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
        public OWLReasoner createReasoner(OWLOntologyManager ontologyManager) {
            // ignore the given manager
            return this.createReasoner(OWLManager.createOWLOntologyManager(),new HashSet<OWLOntology>());
        }
        public void initialise() {
        }
        public void dispose() {
        }
        public boolean requiresExplicitClassification() {
            return false;
        }
        @SuppressWarnings("serial")
        public OWLReasoner createReasoner(OWLOntologyManager manager,Set<OWLOntology> ontologies) {
            // ignore the given manager
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            Reasoner hermit=new Reasoner(configuration) {
                protected Set<OWLOntology> m_loadedOntologies;
                
                public void loadOntologies(Set<OWLOntology> ontologies) {
                    if (!ontologies.isEmpty()) {
                        super.loadOntologies(ontologies);
                    }
                    m_loadedOntologies=ontologies;
                }
                public Set<OWLOntology> getLoadedOntologies() {
                    return m_loadedOntologies;
                }
                // overwrite so that the methods don't throw errors
                public boolean isSymmetric(OWLObjectProperty property) {
                    return false;
                }
                public boolean isTransitive(OWLObjectProperty property) {
                    return false;
                }
                public Set<OWLDataRange> getRanges(OWLDataProperty property) {
                    return new HashSet<OWLDataRange>();
                }
                public Map<OWLObjectProperty,Set<OWLNamedIndividual>> getObjectPropertyRelationships(OWLNamedIndividual individual) {
                    return new HashMap<OWLObjectProperty,Set<OWLNamedIndividual>>();
                }
                public Map<OWLDataProperty,Set<OWLLiteral>> getDataPropertyRelationships(OWLNamedIndividual individual) {
                    return new HashMap<OWLDataProperty,Set<OWLLiteral>>();
                }
                public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual subject,OWLDataPropertyExpression property) {
                    return new HashSet<OWLLiteral>();
                }
            };
            if (!ontologies.isEmpty()) hermit.loadOntologies(ontologies);
            return hermit;
        }
    }
}
