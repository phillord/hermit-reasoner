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
import org.semanticweb.HermiT.blocking.AnywhereCoreBlocking;
import org.semanticweb.HermiT.blocking.AnywhereTwoPhaseBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.CorePreDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.TwoPhaseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.core.AtMostConcept;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.DeterministicHierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyPrinterFSS;
import org.semanticweb.HermiT.hierarchy.SubsumptionCache;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
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
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.IRI;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDatatype;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLiteral;
import org.semanticweb.owl.model.OWLNamedIndividual;
import org.semanticweb.owl.model.OWLObjectAllValuesFrom;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinality;
import org.semanticweb.owl.model.OWLObjectMinCardinality;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLTypedLiteral;
import org.semanticweb.owl.util.ProgressMonitor;

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
    protected SubsumptionCache m_subsumptionCache;
    protected Hierarchy<AtomicConcept> m_atomicConceptHierarchy;
    protected Hierarchy<Role> m_objectRoleHierarchy;
    protected Hierarchy<AtomicRole> m_atomicDataRoleHierarchy;
    protected Map<AtomicConcept,Set<Individual>> m_realization;
    protected ProgressMonitor m_progressMonitor;

    public Reasoner(Configuration configuration) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        if (m_dlOntology != null || m_tableau != null) clearOntologies();
    }
    
    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManger,OWLOntology ontology) {
        this(configuration,ontologyManger,ontology,(Set<DescriptionGraph>)null);
    }

    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadOntology(ontologyManager,ontology,descriptionGraphs);
    }
    
    public Reasoner(Configuration configuration,Set<OWLOntology> importClosure) {
        this(configuration, OWLManager.createOWLOntologyManager(), importClosure);
    }
    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManager,Set<OWLOntology> importClosure) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadOntologies(ontologyManager, importClosure);
    }

    public Reasoner(Configuration configuration,DLOntology dlOntology) {
        m_configuration=configuration;
        m_interruptFlag=new InterruptFlag();
        loadDLOntology(dlOntology);
    }

    // General accessor methods
    
    public Prefixes getPrefixes() {
        return m_prefixes;
    }

    public DLOntology getDLOntology() {
        return m_dlOntology;
    }

    public Configuration getConfiguration() {
        return m_configuration.clone();
    }

    public void interrupt() {
        m_interruptFlag.interrupt();
    }
    
    // Loading and managing ontologies

    public void loadDLOntology(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_prefixes=createPrefixes(m_dlOntology);
        m_tableau=createTableau(m_interruptFlag,m_configuration,m_dlOntology,m_prefixes);
        m_subsumptionCache=new SubsumptionCache(m_tableau);
    }
    
    public void loadOntology(OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLClausification clausifier=new OWLClausification(m_configuration);
        loadDLOntology(clausifier.clausify(ontologyManager,ontology,descriptionGraphs));
    }

    public void loadOntologies(Set<OWLOntology> ontologies) {
        loadOntologies(OWLManager.createOWLOntologyManager(), ontologies);
    }
    
    public void loadOntologies(OWLOntologyManager ontologyManager, Set<OWLOntology> ontologies) {
        OWLClausification clausifier=new OWLClausification(m_configuration);
        Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
        loadDLOntology(clausifier.clausifyImportClosure(ontologyManager.getOWLDataFactory(),"urn:hermit:kb",ontologies,descriptionGraphs));
    }
    
    public Set<OWLOntology> getLoadedOntologies() {
        throw new UnsupportedOperationException();
    }

    public void unloadOntologies(Set<OWLOntology> inOntologies) {
        throw new UnsupportedOperationException();
    }
    
    public void clearOntologies() {
        Set<DLClause> noDLClauses=Collections.emptySet();
        Set<Atom> noAtoms=Collections.emptySet();
        Set<AtomicConcept> noAtomicConcepts=Collections.emptySet();
        Set<AtomicRole> noAtomicRoles=Collections.emptySet();
        Set<DLOntology.ComplexObjectRoleInclusion> noComplexObjectRoleInclusions=Collections.emptySet();
        Set<Individual> noIndividuals=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("urn:hermit:kb",noDLClauses,noAtoms,noAtoms,noAtomicConcepts,noComplexObjectRoleInclusions,noAtomicRoles,noAtomicRoles,noIndividuals,false,false,false,false);
        loadDLOntology(emptyDLOntology);
    }
    
    public void dispose() {
        clearOntologies();
    }

    // -- HACKS ------------------------------------------------
    
    public void loadOntologyForCoreBlocking(OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        // Temporary HACK: this is so that we showe the axioms into core blocking
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();

        // This part is copied from OWLClausification
        Set<OWLOntology> importClosure=new HashSet<OWLOntology>();
        List<OWLOntology> toProcess=new ArrayList<OWLOntology>();
        toProcess.add(ontology);
        while (!toProcess.isEmpty()) {
            OWLOntology anOntology=toProcess.remove(toProcess.size()-1);
            if (importClosure.add(anOntology))
                toProcess.addAll(anOntology.getImports(ontologyManager));
        }

        OWLAxioms axioms=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(ontologyManager.getOWLDataFactory(),axioms);
        for (OWLOntology o : importClosure)
            normalization.processOntology(m_configuration,o);
        ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(ontologyManager.getOWLDataFactory());
        objectPropertyInclusionManager.prepareTransformation(axioms);
        objectPropertyInclusionManager.rewriteAxioms(axioms);
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        
        OWLClausification clausifier=new OWLClausification(m_configuration);
        m_dlOntology=clausifier.clausify(ontologyManager.getOWLDataFactory(),ontology.getOntologyID().getDefaultDocumentIRI() == null ? "urn:hermit:kb" : ontology.getOntologyID().getDefaultDocumentIRI().toString(),axioms,axiomsExpressivity,descriptionGraphs);
        m_prefixes=createPrefixes(m_dlOntology);

        // This creates an OWLAxioms instance that is then given to the core blocking strategy
        boolean foundRelevantConcept;
        // the key is a concept and the values are sets of sets of concepts that are implies by the key, 
        // where each set of concepts is to be interpreted as a disjunction of concepts and the sets of sets of concepts are conjunctions
        // so the axioms "A -> B or C" and "A -> D", are represented by an entry with key A mapped to the value {{B, C}, {D}}
        Map<AtomicConcept, Set<Set<Concept>>> blockRelUnary = new HashMap<AtomicConcept, Set<Set<Concept>>>();
        // similarly as above, but with several premises, e.g., to represent A and B -> C
        Map<Set<AtomicConcept>, Set<Set<Concept>>> blockRelNAry= new HashMap<Set<AtomicConcept>, Set<Set<Concept>>>();
        Set<AtomicConcept> premises;
        Set<Concept> conclusions;
        for (OWLClassExpression[] descs : axioms.m_conceptInclusions) {
            foundRelevantConcept = false;
            premises = new HashSet<AtomicConcept>();
            conclusions = new HashSet<Concept>();
            for (int i = 0; i<descs.length&&!foundRelevantConcept; i++) {
                OWLClassExpression desc = descs[i];
                if (desc instanceof OWLObjectAllValuesFrom) {
                    foundRelevantConcept = true;
                    OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) desc;
                    AtomicRole ar = AtomicRole.create(all.getProperty().getNamedProperty().getIRI().toString()); 
                    Role r = all.getProperty().getSimplified().isAnonymous() ? InverseRole.create(ar) : ar;
                    LiteralConcept c;
                    // since the axioms are normalised, the filler is a literal
                    if (all.getFiller() instanceof OWLObjectComplementOf) {
                        OWLClassExpression nonNegated = ((OWLObjectComplementOf) all.getFiller()).getOperand();
                        if (nonNegated.isAnonymous()) {
                            // negated nominal concept
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicConcept.create(nonNegated.asOWLClass().getIRI().toString());
                        }
                    } else {
                        if (all.getFiller().isAnonymous()) {
                            // nominal
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicNegationConcept.create(AtomicConcept.create(all.getFiller().asOWLClass().getIRI().toString()));
                        }
                    }
                    conclusions.add(AtMostConcept.create(0, r, c));
                } else if (desc instanceof OWLObjectSomeValuesFrom) {
                    foundRelevantConcept = true;
                    OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) desc;
                    AtomicRole ar = AtomicRole.create(some.getProperty().getNamedProperty().getIRI().toString()); 
                    Role r = some.getProperty().getSimplified().isAnonymous() ? InverseRole.create(ar) : ar;
                    LiteralConcept c;
                    // since the axioms are normalised, the filler is a literal
                    if (some.getFiller() instanceof OWLObjectComplementOf) {
                        OWLClassExpression nonNegated = ((OWLObjectComplementOf) some.getFiller()).getOperand();
                        if (nonNegated.isAnonymous()) {
                            // negated nominal concept
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicNegationConcept.create(AtomicConcept.create(nonNegated.asOWLClass().getIRI().toString()));
                        }
                    } else {
                        if (some.getFiller().isAnonymous()) {
                            // nominal
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicConcept.create(some.getFiller().asOWLClass().getIRI().toString());
                        }
                    }
                    conclusions.add(AtLeastConcept.create(1, r, c));
                } else if (desc instanceof OWLObjectMinCardinality) {
                    OWLObjectMinCardinality min = (OWLObjectMinCardinality) desc;
                    AtomicRole ar = AtomicRole.create(min.getProperty().getNamedProperty().getIRI().toString()); 
                    Role r = min.getProperty().getSimplified().isAnonymous() ? InverseRole.create(ar) : ar;
                    LiteralConcept c;
                    // since the axioms are normalised, the filler is a literal
                    if (min.getFiller() instanceof OWLObjectComplementOf) {
                        OWLClassExpression nonNegated = ((OWLObjectComplementOf) min.getFiller()).getOperand();
                        if (nonNegated.isAnonymous()) {
                            // negated nominal concept
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicNegationConcept.create(AtomicConcept.create(nonNegated.asOWLClass().getIRI().toString()));
                        }
                    } else {
                        if (min.getFiller().isAnonymous()) {
                            // nominal
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicConcept.create(min.getFiller().asOWLClass().getIRI().toString());
                        }
                    }
                    conclusions.add(AtLeastConcept.create(1, r, c));
                } else if (desc instanceof OWLObjectMaxCardinality) {
                    foundRelevantConcept = true;
                    OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) desc;
                    AtomicRole ar = AtomicRole.create(max.getProperty().getNamedProperty().getIRI().toString()); 
                    Role r = max.getProperty().getSimplified().isAnonymous() ? InverseRole.create(ar) : ar;
                    LiteralConcept c;
                    // since the axioms are normalised, the filler is a literal
                    if (max.getFiller() instanceof OWLObjectComplementOf) {
                        OWLClassExpression nonNegated = ((OWLObjectComplementOf) max.getFiller()).getOperand();
                        if (nonNegated.isAnonymous()) {
                            // negated nominal concept
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicNegationConcept.create(AtomicConcept.create(nonNegated.asOWLClass().getIRI().toString()));
                        }
                    } else {
                        if (max.getFiller().isAnonymous()) {
                            // nominal
                            throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                        } else {
                            c = AtomicConcept.create(max.getFiller().asOWLClass().getIRI().toString());
                        }
                    }
                    conclusions.add(AtMostConcept.create(max.getCardinality(), r, c));
                } else if (desc instanceof OWLClass) {
                    conclusions.add(AtomicConcept.create(desc.asOWLClass().getIRI().toString()));
                } else if (desc instanceof OWLObjectComplementOf) {
                    OWLClassExpression nonNegated = ((OWLObjectComplementOf) desc).getOperand();
                    if (nonNegated.isAnonymous()) {
                        // nominal
                        throw new IllegalArgumentException("Core blocking is not compatible with nominals. ");
                    } else {
                        premises.add(AtomicConcept.create(nonNegated.asOWLClass().getIRI().toString()));
                    }
                }
            }
            if (foundRelevantConcept) {
                if (premises.size() <= 1) {
                    AtomicConcept p = premises.iterator().hasNext() ? premises.iterator().next() : AtomicConcept.THING;
                    if (blockRelUnary.containsKey(p)) {
                        blockRelUnary.get(p).add(conclusions);
                    } else {
                        Set<Set<Concept>> conjuncts = new HashSet<Set<Concept>>();
                        conjuncts.add(conclusions);
                        blockRelUnary.put(p, conjuncts);
                    }
                } else {
                    if (blockRelNAry.containsKey(premises)) {
                        blockRelNAry.get(premises).add(conclusions);
                    } else {
                        Set<Set<Concept>> conjuncts = new HashSet<Set<Concept>>();
                        conjuncts.add(conclusions);
                        blockRelNAry.put(premises, conjuncts);
                    }
                }
            }
        }

        // And now for the Tableau!
        TableauMonitor wellKnownTableauMonitor=null;
        switch (m_configuration.tableauMonitorType) {
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
            wellKnownTableauMonitor=new Debugger(m_prefixes,true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor=new Debugger(m_prefixes,false);
            break;
        default:
            throw new IllegalArgumentException("Unknown monitor type");
        }

        TableauMonitor tableauMonitor=null;
        if (m_configuration.monitor==null)
            tableauMonitor=wellKnownTableauMonitor;
        else if (wellKnownTableauMonitor==null)
            tableauMonitor=m_configuration.monitor;
        else
            tableauMonitor=new TableauMonitorFork(wellKnownTableauMonitor,m_configuration.monitor);
        
        BlockingStrategy blockingStrategy;
        if (m_configuration.blockingStrategyType==Configuration.BlockingStrategyType.TWOPHASE) {
            blockingStrategy=new AnywhereTwoPhaseBlocking(new TwoPhaseDirectBlockingChecker(),blockRelUnary,blockRelNAry,axiomsExpressivity.m_hasInverseRoles);
        } else {
            blockingStrategy=new AnywhereCoreBlocking(new CorePreDirectBlockingChecker(),blockRelUnary,blockRelNAry,axiomsExpressivity.m_hasInverseRoles);
        }
        
        ExistentialExpansionStrategy existentialsExpansionStrategy=null;
        switch (m_configuration.existentialStrategyType) {
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

        m_tableau=new Tableau(m_interruptFlag,tableauMonitor,existentialsExpansionStrategy,m_dlOntology,m_configuration.parameters);
        m_subsumptionCache=new SubsumptionCache(m_tableau);
    }
    
    // Monitor interface

    public OWLEntity getCurrentEntity() {
        throw new UnsupportedOperationException();
    }

    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        m_progressMonitor=progressMonitor;
    }

    // Checking the signature of the ontology
    
    public boolean isDefined(OWLClass owlClass) {
        AtomicConcept atomicConcept=AtomicConcept.create(owlClass.getIRI().toString());
        return m_dlOntology.getAllAtomicConcepts().contains(atomicConcept) || AtomicConcept.THING.equals(atomicConcept) || AtomicConcept.NOTHING.equals(atomicConcept);
    }

    public boolean isDefined(OWLIndividual owlIndividual) {
        if (owlIndividual.isAnonymous()) return false;
        Individual individual=Individual.create(owlIndividual.asNamedIndividual().getIRI().toString());
        return m_dlOntology.getAllIndividuals().contains(individual);
    }

    public boolean isDefined(OWLObjectProperty owlObjectProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlObjectProperty.getIRI().toString());
        return m_dlOntology.getAllAtomicObjectRoles().contains(atomicRole);
    }

    public boolean isDefined(OWLDataProperty owlDataProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlDataProperty.getIRI().toString());
        return m_dlOntology.getAllAtomicDataRoles().contains(atomicRole);
    }

    // General inferences
    
    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }

    @Deprecated
    public boolean isConsistent(OWLOntology ignored) {
        return isConsistent();
    }
    
    // Concept inferences
    
    public boolean isClassified() {
        return m_atomicConceptHierarchy!=null;
    }

    public void classify() {
        if (m_atomicConceptHierarchy==null) {
            try {
                Set<AtomicConcept> relevantAtomicConcepts=new HashSet<AtomicConcept>();
                relevantAtomicConcepts.add(AtomicConcept.THING);
                relevantAtomicConcepts.add(AtomicConcept.NOTHING);
                for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                    if (!Prefixes.isInternalIRI(atomicConcept.getIRI()))
                        relevantAtomicConcepts.add(atomicConcept);
                if (m_progressMonitor!=null) {
                    m_progressMonitor.setSize(relevantAtomicConcepts.size());
                    m_progressMonitor.setProgress(0);
                    m_progressMonitor.setStarted();
                }
                if (!m_subsumptionCache.isSatisfiable(AtomicConcept.THING))
                    m_atomicConceptHierarchy=Hierarchy.emptyHierarchy(relevantAtomicConcepts,AtomicConcept.THING,AtomicConcept.NOTHING);
                else if (m_subsumptionCache.canGetAllSubsumersEasily()) {
                    Map<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>>();
                    int processedConcepts=0;
                    for (AtomicConcept atomicConcept : relevantAtomicConcepts) {
                        Set<AtomicConcept> subsumers=m_subsumptionCache.getAllKnownSubsumers(atomicConcept);
                        if (subsumers==null)
                            subsumers=relevantAtomicConcepts;
                        allSubsumers.put(atomicConcept,new DeterministicHierarchyBuilder.GraphNode<AtomicConcept>(atomicConcept,subsumers));
                        if (m_progressMonitor!=null) {
                            processedConcepts++;
                            m_progressMonitor.setProgress(processedConcepts);
                        }
                    }
                    DeterministicHierarchyBuilder<AtomicConcept> hierarchyBuilder=new DeterministicHierarchyBuilder<AtomicConcept>(allSubsumers,AtomicConcept.THING,AtomicConcept.NOTHING);
                    m_atomicConceptHierarchy=hierarchyBuilder.buildHierarchy();
                }
                if (m_atomicConceptHierarchy==null) {
                    HierarchyBuilder.Relation<AtomicConcept> relation=
                        new HierarchyBuilder.Relation<AtomicConcept>() {
                            public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                                return m_subsumptionCache.isSubsumedBy(child,parent);
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
                                    m_progressMonitor.setProgress(m_processedConcepts);
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
    
    public boolean isSatisfiable(OWLClassExpression description) {
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
            if (m_atomicConceptHierarchy==null)
                return m_subsumptionCache.isSatisfiable(concept);
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
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass) {
            AtomicConcept subconcept=AtomicConcept.create(((OWLClass)subDescription).getIRI().toString());
            AtomicConcept superconcept=AtomicConcept.create(((OWLClass)superDescription).getIRI().toString());
            return m_subsumptionCache.isSubsumedBy(subconcept,superconcept);
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

    public boolean isEquivalentClass(OWLClassExpression description1,OWLClassExpression description2) {
        return isSubClassOf(description1,description2) && isSubClassOf(description2,description1); 
    }
    
    public Set<OWLClass> getEquivalentClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptsToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLClass>> getSubClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getSuperClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getAncestorClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getAncestorNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getDescendantClasses(OWLClassExpression description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getDescendantNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<OWLClass> getInconsistentClasses() {
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
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(IRI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(newClass,description);
            Tableau tableau=getTableau(ontologyManager,classDefinitionAxiom);
            final SubsumptionCache subsumptionCache=new SubsumptionCache(tableau);
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
    
    public boolean areObjectPropertiesClassified() {
        return m_objectRoleHierarchy!=null;
    }

    public void classifyObjectProperties() {
        if (m_objectRoleHierarchy==null) {
            HierarchyBuilder.Relation<Role> relation=
                new HierarchyBuilder.Relation<Role>() {
                    protected final OWLDataFactory m_factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
                    public boolean doesSubsume(Role parent,Role child) {
                        return isSubPropertyOf(getObjectPropertyExpression(m_factory,child),getObjectPropertyExpression(m_factory,parent));
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
                            m_progressMonitor.setProgress(m_processedRoles);
                        }
                    };
            Set<Role> allObjectRoles=new HashSet<Role>();
            for (AtomicRole atomicRole : m_dlOntology.getAllAtomicObjectRoles()) {
                allObjectRoles.add(atomicRole);
                allObjectRoles.add(atomicRole.getInverse());
            }
            HierarchyBuilder<Role> hierarchyBuilder=new HierarchyBuilder<Role>(relation,progressMonitor);
            m_objectRoleHierarchy=hierarchyBuilder.buildHierarchy(AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE,allObjectRoles);
        }
    }
    
    public boolean isSubPropertyOf(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        if (superObjectPropertyExpression.getNamedProperty().getIRI().toString().equals(AtomicRole.TOP_OBJECT_ROLE.getIRI()))
            return true;
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
            OWLObjectProperty negatedSuperProperty=factory.getOWLObjectProperty(IRI.create("internal:negated-superproperty"));
            OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
            OWLAxiom subAssertion=factory.getOWLObjectPropertyAssertionAxiom(individualA,subObjectPropertyExpression,individualB);
            OWLAxiom superAssertion=factory.getOWLObjectPropertyAssertionAxiom(individualA,negatedSuperProperty,individualB);
            OWLAxiom superDisjoint=factory.getOWLDisjointObjectPropertiesAxiom(superObjectPropertyExpression,negatedSuperProperty);
            Tableau tableau=getTableau(ontologyManager,subAssertion,superAssertion,superDisjoint);
            return !tableau.isABoxSatisfiable();
        }
    }
    
    public boolean isEquivalentProperty(OWLObjectPropertyExpression objectPropertyExpression1,OWLObjectPropertyExpression objectPropertyExpression2) {
        return isSubPropertyOf(objectPropertyExpression1,objectPropertyExpression2) && isSubPropertyOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    
    public Set<Set<OWLObjectPropertyExpression>> getSuperProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLObjectPropertyExpression>> getSubProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLObjectPropertyExpression>> getAncestorProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> ancestorsPlusNode=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
        ancestorsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(ancestorsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLObjectPropertyExpression>> getDescendantProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> descendantsPlusNode=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
        descendantsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(descendantsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<OWLObjectPropertyExpression> getEquivalentProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    protected HierarchyNode<Role> getHierarchyNode(OWLObjectPropertyExpression propertyExpression) {
        propertyExpression=propertyExpression.getSimplified();
        Role role;
        if (propertyExpression instanceof OWLObjectProperty)
            role=AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString());
        else
            role=AtomicRole.create(propertyExpression.getNamedProperty().getIRI().toString()).getInverse();
        classifyObjectProperties();
        HierarchyNode<Role> node=m_objectRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(role),Collections.singleton(m_objectRoleHierarchy.getTopNode()),Collections.singleton(m_objectRoleHierarchy.getBottomNode()));
        return node;
    }
    
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSuperProperties((OWLObjectPropertyExpression)property));
    }
    
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSubProperties((OWLObjectPropertyExpression)property));
    }

    @Deprecated
    public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getAncestorProperties((OWLObjectPropertyExpression)property));
    }

    @Deprecated
    public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getDescendantProperties((OWLObjectPropertyExpression)property));
    }

    @Deprecated
    public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) {
        return filterObjectProperties(getEquivalentProperties((OWLObjectPropertyExpression)property));
    }

    @SuppressWarnings("unchecked")
    public Set<Set<OWLClassExpression>> getDomains(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLObjectSomeValuesFrom(property,factory.getOWLThing()));
        return (Set<Set<OWLClassExpression>>)object;
    }

    public Set<OWLClassExpression> getRanges(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLClassExpression> result=new HashSet<OWLClassExpression>();
        Set<Set<OWLClass>> ranges=getAncestorClasses(factory.getOWLObjectSomeValuesFrom(property.getInverseProperty(),factory.getOWLThing()));
        for (Set<OWLClass> classSet : ranges)
            result.addAll(classSet);
        return result;
    }

    public Set<OWLObjectPropertyExpression> getInverseProperties(OWLObjectPropertyExpression property) {
        return getEquivalentProperties(property.getInverseProperty());
    }

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

    public boolean isFunctional(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(property,2));
    }

    public boolean isInverseFunctional(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(property.getInverseProperty(),2));
    }

    public boolean isIrreflexive(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectHasSelf(property));
    }

    public boolean isReflexive(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectComplementOf(factory.getOWLObjectHasSelf(property)));
    }
    @Deprecated
    public boolean isAntiSymmetric(OWLObjectProperty property) throws OWLReasonerException {
        AtomicRole atomicRole=AtomicRole.create(property.getIRI().toString());
        return m_tableau.isAsymmetric(atomicRole);
    }
    public boolean isAsymmetric(OWLObjectProperty property) {
        AtomicRole atomicRole=AtomicRole.create(property.getIRI().toString());
        return m_tableau.isAsymmetric(atomicRole);
    }

    public boolean isSymmetric(OWLObjectProperty property) {
        throw new UnsupportedOperationException();
    }

    public boolean isTransitive(OWLObjectProperty property) {
        throw new UnsupportedOperationException();
    }

    // Data property inferences

    public boolean areDataPropertiesClassified() {
        return m_atomicDataRoleHierarchy!=null;
    }
    
    public void classifyDataProperties() {
        if (m_atomicDataRoleHierarchy==null) {
            HierarchyBuilder.Relation<AtomicRole> relation=
                new HierarchyBuilder.Relation<AtomicRole>() {
                    protected final OWLDataFactory m_factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
                    public boolean doesSubsume(AtomicRole parent,AtomicRole child) {
                        return isSubPropertyOf(getDataProperty(m_factory,child),getDataProperty(m_factory,parent));
                    }
                };
            HierarchyBuilder.ClassificationProgressMonitor<AtomicRole> progressMonitor;
            if (m_progressMonitor==null)
                progressMonitor=null;
            else
                progressMonitor=
                    new HierarchyBuilder.ClassificationProgressMonitor<AtomicRole>() {
                        protected int m_processedAtomicRoles=0;
                        public void elementClassified(AtomicRole element) {
                            m_processedAtomicRoles++;
                            m_progressMonitor.setProgress(m_processedAtomicRoles);
                        }
                    };
            HierarchyBuilder<AtomicRole> hierarchyBuilder=new HierarchyBuilder<AtomicRole>(relation,progressMonitor);
            m_atomicDataRoleHierarchy=hierarchyBuilder.buildHierarchy(AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE,m_dlOntology.getAllAtomicDataRoles());
        }
    }
    
    public boolean isSubPropertyOf(OWLDataProperty subDataProperty,OWLDataProperty superDataProperty) {
        if (superDataProperty.getIRI().toString().equals(AtomicRole.TOP_DATA_ROLE.getIRI()))
            return true;
        else if (subDataProperty.getIRI().toString().equals(AtomicRole.TOP_DATA_ROLE.getIRI()))
            return !isConsistent();
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLIndividual individual=factory.getOWLNamedIndividual(IRI.create("internal:individual"));
            OWLDataProperty negatedSuperProperty=factory.getOWLDataProperty(IRI.create("internal:negated-superproperty"));
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant",anonymousConstantsDatatype);
            OWLAxiom subAssertion=factory.getOWLDataPropertyAssertionAxiom(individual,subDataProperty,constant);
            OWLAxiom superAssertion=factory.getOWLDataPropertyAssertionAxiom(individual,negatedSuperProperty,constant);
            OWLAxiom superDisjoint=factory.getOWLDisjointDataPropertiesAxiom(superDataProperty,negatedSuperProperty);
            Tableau tableau=getTableau(ontologyManager,subAssertion,superAssertion,superDisjoint);
            return !tableau.isABoxSatisfiable();
        }
    }
    
    public boolean isEquivalentProperty(OWLDataProperty dataProperty1,OWLDataProperty dataProperty2) {
        return isSubPropertyOf(dataProperty1,dataProperty2) && isSubPropertyOf(dataProperty2,dataProperty1);
    }
    
    public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        return dataPropertyNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        return dataPropertyNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        Set<HierarchyNode<AtomicRole>> ancestorsPlusNode=new HashSet<HierarchyNode<AtomicRole>>(node.getAncestorNodes());
        ancestorsPlusNode.add(node);
        return dataPropertyNodesToOWLAPI(ancestorsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        Set<HierarchyNode<AtomicRole>> descendantsPlusNode=new HashSet<HierarchyNode<AtomicRole>>(node.getDescendantNodes());
        descendantsPlusNode.add(node);
        return dataPropertyNodesToOWLAPI(descendantsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        return dataPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    protected HierarchyNode<AtomicRole> getHierarchyNode(OWLDataProperty property) {
        AtomicRole atomicRole=AtomicRole.create(property.getIRI().toString());
        classifyDataProperties();
        HierarchyNode<AtomicRole> node=m_atomicDataRoleHierarchy.getNodeForElement(atomicRole);
        if (node==null)
            node=new HierarchyNode<AtomicRole>(atomicRole,Collections.singleton(atomicRole),Collections.singleton(m_atomicDataRoleHierarchy.getTopNode()),Collections.singleton(m_atomicDataRoleHierarchy.getBottomNode()));
        return node;
    }
    
    @SuppressWarnings("unchecked")
    public Set<Set<OWLClassExpression>> getDomains(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLDataSomeValuesFrom(property,factory.getTopDatatype()));
        return (Set<Set<OWLClassExpression>>)object;
    }

    public Set<OWLDataRange> getRanges(OWLDataProperty property) {
        throw new UnsupportedOperationException();
    }

    public boolean isFunctional(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLDataMinCardinality(property,2));
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

    public Set<Set<OWLClass>> getTypes(OWLIndividual individual,boolean direct) {
        if (individual.isAnonymous()) return null;
        Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(Individual.create(individual.asNamedIndividual().getIRI().toString()));
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<Set<OWLClass>> result=atomicConceptNodesToOWLAPI(directSuperConceptNodes,factory);
        if (!direct)
            for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes)
                result.addAll(atomicConceptNodesToOWLAPI(directSuperConceptNode.getAncestorNodes(),factory));
        return result;
    }

    public boolean hasType(OWLIndividual owlIndividual,OWLClassExpression type,boolean direct) {
        if (owlIndividual.isAnonymous()) return false;
        if (direct || isRealised())
            return getIndividuals(type,direct).contains(owlIndividual.asNamedIndividual());
        else {
            Individual individual=Individual.create(owlIndividual.asNamedIndividual().getIRI().toString());
            if (type instanceof OWLClass) {
                AtomicConcept concept=AtomicConcept.create(((OWLClass)type).getIRI().toString());
                return m_tableau.isInstanceOf(concept,individual);
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

    public Set<OWLIndividual> getIndividuals(OWLClassExpression description,boolean direct) {
        return new HashSet<OWLIndividual>(getNamedIndividuals(description, direct));
    }
    
    public Set<OWLNamedIndividual> getNamedIndividuals(OWLClassExpression description,boolean direct) {
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
            for (HierarchyNode<AtomicConcept> parentNode : hierarchyNode.getParentNodes()) {
                AtomicConcept parentAtomicConcept=parentNode.getEquivalentElements().iterator().next();
                Set<Individual> realizationForParentConcept=m_realization.get(parentAtomicConcept);
                if (realizationForParentConcept!=null)
                    for (Individual individual : realizationForParentConcept)
                        if (tableau.isInstanceOf(queryConcept,individual))
                            result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
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
            for (Individual individual : realizationForConcept)
                result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
    }
    
    public Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        throw new UnsupportedOperationException();
    }
    
    public Map<OWLDataProperty,Set<OWLLiteral>> getDataPropertyRelationships(OWLIndividual individual) {
        throw new UnsupportedOperationException();
    }

    public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,OWLObjectPropertyExpression property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getIndividuals(factory.getOWLObjectSomeValuesFrom(property.getInverseProperty(),factory.getOWLObjectOneOf(subject)),false);
    }

    public Set<OWLLiteral> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
        throw new UnsupportedOperationException();
    }

    public boolean hasObjectPropertyRelationship(OWLIndividual subject,OWLObjectPropertyExpression property,OWLIndividual object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLObjectSomeValuesFrom(property,factory.getOWLObjectOneOf(object)),false);
    }

    public boolean hasDataPropertyRelationship(OWLIndividual subject,OWLDataPropertyExpression property,OWLLiteral object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLDataHasValue(property,object),false);
    }

    // Various creation methods
    
    protected Tableau getTableau(OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
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
        switch (config.directBlockingType) {
        case OPTIMAL:
            if (dlOntology.hasAtMostRestrictions() && dlOntology.hasInverseRoles())
                directBlockingChecker=new PairWiseDirectBlockingChecker(config.blockingStrategyType==BlockingStrategyType.CORE);
            else
                directBlockingChecker=new SingleDirectBlockingChecker(config.blockingStrategyType==BlockingStrategyType.CORE);
            break;
        case SINGLE:
            directBlockingChecker=new SingleDirectBlockingChecker(config.blockingStrategyType==BlockingStrategyType.CORE);
            break;
        case PAIR_WISE:
            directBlockingChecker=new PairWiseDirectBlockingChecker(config.blockingStrategyType==BlockingStrategyType.CORE);
            break;
        default:
            throw new IllegalArgumentException("Unknown direct blocking type.");
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
        case ANCESTOR:
            blockingStrategy=new AncestorBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case ANYWHERE:
            blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case CORE:
            blockingStrategy=new AnywhereCoreBlocking(new CorePreDirectBlockingChecker(), new HashMap<AtomicConcept, Set<Set<Concept>>>(), new HashMap<Set<AtomicConcept>, Set<Set<Concept>>>(), dlOntology.hasInverseRoles());
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
            OWLNormalization normalization=new OWLNormalization(factory,axioms);
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
                for (DLOntology.ComplexObjectRoleInclusion inclusion : originalDLOntology.getAllComplexObjectRoleInclusions()) {
                    OWLObjectPropertyExpression[] subObjectPropertyExpressions=new OWLObjectPropertyExpression[inclusion.getNumberOfSubRoles()];
                    for (int index=inclusion.getNumberOfSubRoles()-1;index>=0;--index)
                        subObjectPropertyExpressions[index]=getObjectPropertyExpression(factory,inclusion.getSubRole(index));
                    OWLObjectPropertyExpression superObjectPropertyExpression=getObjectPropertyExpression(factory,inclusion.getSuperRole());
                    objectPropertyInclusionManager.addInclusion(subObjectPropertyExpressions,superObjectPropertyExpression);
                }
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
                 * @gstoil
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
            Set<Individual> individuals=createUnion(originalDLOntology.getAllIndividuals(),newDLOntology.getAllIndividuals());
            boolean hasInverseRoles=originalDLOntology.hasInverseRoles() || newDLOntology.hasInverseRoles();
            boolean hasAtMostRestrictions=originalDLOntology.hasAtMostRestrictions() || newDLOntology.hasAtMostRestrictions();
            boolean hasNominals=originalDLOntology.hasNominals() || newDLOntology.hasNominals();
            boolean hasDatatypes=originalDLOntology.hasDatatypes() || newDLOntology.hasDatatypes();
            return new DLOntology(resultingOntologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,complexObjectRoleInclusions,atomicObjectRoles,atomicDataRoles,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,hasDatatypes);
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
        if (dataProperties && !m_atomicDataRoleHierarchy.isEmpty()) {
            if (!atLF)
                out.println();
            printer.printRoleHierarchy(m_atomicDataRoleHierarchy,false);
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
        for (AtomicConcept concept : atomicConcepts)
            result.add(factory.getOWLClass(IRI.create(concept.getIRI())));
        return result;
    }

    protected static Set<Set<OWLClass>> atomicConceptNodesToOWLAPI(Collection<HierarchyNode<AtomicConcept>> nodes,OWLDataFactory factory) {
        Set<Set<OWLClass>> result=new HashSet<Set<OWLClass>>();
        for (HierarchyNode<AtomicConcept> node : nodes)
            result.add(atomicConceptsToOWLAPI(node.getEquivalentElements(),factory));
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
    
    protected static Set<OWLDataProperty> dataPropertiesToOWLAPI(Collection<AtomicRole> dataProperties,OWLDataFactory factory) {
        Set<OWLDataProperty> result=new HashSet<OWLDataProperty>();
        for (AtomicRole atomicRole : dataProperties)
            result.add(factory.getOWLDataProperty(IRI.create(atomicRole.getIRI())));
        return result;
    }
    
    protected static Set<Set<OWLDataProperty>> dataPropertyNodesToOWLAPI(Collection<HierarchyNode<AtomicRole>> nodes,OWLDataFactory factory) {
        Set<Set<OWLDataProperty>> result=new HashSet<Set<OWLDataProperty>>();
        for (HierarchyNode<AtomicRole> node : nodes)
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
    
    // The factory for reasoner the OWL API
    
    public static class ReasonerFactory implements OWLReasonerFactory {

        public OWLReasoner createReasoner(OWLOntologyManager manager) {
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            return new Reasoner(configuration);
        }
        public String getReasonerName() {
            return getClass().getPackage().getImplementationTitle();
        }
        public OWLReasoner createReasoner(OWLOntologyManager manager,
                Set<OWLOntology> ontologies) {
            // TODO Auto-generated method stub
            return null;
        }
    }
   
    // The factory for the reasoner from the Protege plug-in

    public static class ProtegeReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
        
        public OWLReasoner createReasoner(OWLOntologyManager ontologyManager) {
            return this.createReasoner(ontologyManager, new HashSet<OWLOntology>());
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
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            return new Reasoner(configuration) {
                protected Set<OWLOntology> m_loadedOntologies;
                
                public void loadOntologies(OWLOntologyManager ontologyManager, Set<OWLOntology> ontologies) {
                    if (!ontologies.isEmpty()) {
                        super.loadOntologies(ontologyManager, ontologies);
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
                public Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
                    return new HashMap<OWLObjectProperty,Set<OWLIndividual>>();
                }
                public Map<OWLDataProperty,Set<OWLLiteral>> getDataPropertyRelationships(OWLIndividual individual) {
                    return new HashMap<OWLDataProperty,Set<OWLLiteral>>();
                }
                public Set<OWLLiteral> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
                    return new HashSet<OWLLiteral>();
                }
            };
        }
    }
}
