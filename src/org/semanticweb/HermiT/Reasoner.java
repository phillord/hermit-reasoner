/*
 * 
 * Version 0.5.0
 *
 * 2008-08-19
 * 
 * Copyright 2008 by Oxford University; see license.txt for details
 */

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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Clausifier.LoadingException;
import org.semanticweb.HermiT.blocking.AncestorBlocking;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairwiseDirectBlockingCheckerWithReflexivity;
import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.DepthFirstStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.Classifier;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.hierarchy.NaiveHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.PositionTranslator;
import org.semanticweb.HermiT.hierarchy.TableauFunc;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.hierarchy.TranslatedHierarchyPosition;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.monitor.TableauMonitorFork;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.HermiT.monitor.TimerWithPause;
import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.owlapi.structural.OWLClausification;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.util.TranslatedMap;
import org.semanticweb.HermiT.util.TranslatedSet;
import org.semanticweb.HermiT.util.Translator;
import org.semanticweb.HermiT.util.GraphUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Answers queries about the logical implications of a particular knowledge base. A Reasoner is associated with a single knowledge base, which is "loaded" when the reasoner is constructed. By default a full classification of all atomic terms in the knowledge base is also performed at this time (which can take quite a while for large or complex ontologies), but this behavior can be disabled as a part of the Reasoner configuration. Internal details of the loading and reasoning algorithms can be configured in the Reasoner constructor and do not change over the lifetime of the Reasoner object---internal data structures and caches are optimized for a particular configuration. By default, HermiT will use the set of options which provide optimal performance.
 */
public class Reasoner implements Serializable {
    private static final long serialVersionUID=-8277117863937974032L;

    protected final Configuration m_config;
    protected final DLOntology m_dlOntology;
    protected final Namespaces m_namespaces;
    protected final Tableau m_tableau;
    protected final TableauSubsumptionChecker m_subsumptionChecker;
    protected final Classifier<AtomicConcept> m_classifier;
    protected Map<AtomicConcept,HierarchyPosition<AtomicConcept>> m_atomicConceptHierarchy; // may be null; use getAtomicConceptHierarchy
    protected Map<AtomicConcept,Set<Individual>> m_realization;

    public Reasoner(String ontologyURI) throws IllegalArgumentException,LoadingException,OWLException {
        this(new Configuration(),URI.create(ontologyURI));
    }

    public Reasoner(java.net.URI ontologyURI) throws IllegalArgumentException,LoadingException,OWLException {
        this(new Configuration(),ontologyURI);
    }

    public Reasoner(Configuration config,java.net.URI ontologyURI) throws IllegalArgumentException,LoadingException,OWLException {
        this(config,ontologyURI,(Set<DescriptionGraph>)null,(Set<OWLHasKeyDummy>)null);
    }

    public Reasoner(Configuration config,java.net.URI ontologyURI,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) throws IllegalArgumentException,LoadingException,OWLException {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        if (keys==null)
            keys=Collections.emptySet();
        switch (config.parserType) {
        case KAON2:
            {
                Clausifier clausifier=null;
                try {
                    clausifier=(Clausifier)Class.forName("org.semanticweb.HermiT.kaon2.Clausifier").newInstance();
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to load KAON2 library",e);
                }
                catch (NoClassDefFoundError e) {
                    // This seems to be the one that comes up with no KAON2 available
                    throw new RuntimeException("Unable to load KAON2 library",e);
                }
                catch (InstantiationException e) {
                    throw new RuntimeException("Unable to load KAON2 library",e);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to load KAON2 library",e);
                }
                m_dlOntology=clausifier.loadFromURI(ontologyURI,null);
            }
            break;
        case OWLAPI:
            {
                OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
                OWLOntology ontology=ontologyManager.loadOntologyFromPhysicalURI(ontologyURI);
                OWLClausification clausifier=new OWLClausification(config);
                m_dlOntology=clausifier.clausifyWithKeys(ontologyManager,ontology,descriptionGraphs,keys);
            }
            break;
        default:
            throw new IllegalArgumentException("unknown parser library requested");
        }
        m_config=config;
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }

    public Reasoner(Configuration config,OWLOntologyManager ontologyManger,OWLOntology ontology) {
        this(config,ontologyManger,ontology,(Set<DescriptionGraph>)null,(Set<OWLHasKeyDummy>)null);
    }

    public Reasoner(Configuration config,OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) {
        OWLClausification clausifier=new OWLClausification(config);
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        if (keys==null)
            keys=Collections.emptySet();
        m_config=config;
        m_dlOntology=clausifier.clausifyWithKeys(ontologyManager,ontology,descriptionGraphs,keys);
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }
    
    /**
     * Creates a reasoner that contains all axioms from the ontologies in the 'ontologies'' parameter.
     * If any ontology in this collection contains imports, these are *NOT* traversed -- that is,
     * the resulting ontology contains *EXACTLY* the axioms explciitly present in the supplied ontologies.
     * The resulting DL ontology has the URI resultingDLOntologyURI.
     */
    public Reasoner(Configuration config,OWLOntologyManager ontologyManger,Collection<OWLOntology> ontologies,String resultingDLOntologyURI) {
        OWLClausification clausifier=new OWLClausification(config);
        m_config=config;
        m_dlOntology=clausifier.clausifyOntologiesDisregardImports(ontologyManger,ontologies,resultingDLOntologyURI);
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }

    public Reasoner(Configuration config,DLOntology dlOntology) {
        m_config=config;
        m_dlOntology=dlOntology;
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }

    protected Tableau createTableau(Configuration config,DLOntology dlOntology) throws IllegalArgumentException {
        if (!dlOntology.canUseNIRule() && dlOntology.hasAtMostRestrictions() && dlOntology.hasInverseRoles() && config.existentialStrategyType==Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE)
            throw new IllegalArgumentException("The supplied DL-ontology is not compatible with the individual reuse strategy.");

        if (config.checkClauses) {
            Collection<DLClause> nonAdmissibleDLClauses=dlOntology.getNonadmissibleDLClauses();
            if (!nonAdmissibleDLClauses.isEmpty()) {
                String CRLF=System.getProperty("line.separator");
                StringBuffer buffer=new StringBuffer();
                buffer.append("The following DL-clauses in the DL-ontology"+" are not admissible:");
                buffer.append(CRLF);
                for (DLClause dlClause : nonAdmissibleDLClauses) {
                    buffer.append(dlClause.toString(m_namespaces));
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
            wellKnownTableauMonitor=new Debugger(m_namespaces,true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor=new Debugger(m_namespaces,false);
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
            if (config.prepareForExpressiveQueries) {
                directBlockingChecker=new PairwiseDirectBlockingCheckerWithReflexivity();
            }
            else if (m_dlOntology.hasAtMostRestrictions() && m_dlOntology.hasInverseRoles()) {
                if (m_dlOntology.hasReflexifity()) {
                    directBlockingChecker=new PairwiseDirectBlockingCheckerWithReflexivity();
                }
                else {
                    directBlockingChecker=new PairWiseDirectBlockingChecker();
                }
            }
            else {
                directBlockingChecker=new SingleDirectBlockingChecker();
            }
            break;
        case SINGLE:
            directBlockingChecker=new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
            directBlockingChecker=new PairWiseDirectBlockingChecker();
            break;
        case PAIR_WISE_REFLEXIVE:
            directBlockingChecker=new PairwiseDirectBlockingCheckerWithReflexivity();
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
        default:
            throw new IllegalArgumentException("Unknown blocking strategy type.");
        }

        ExpansionStrategy existentialsExpansionStrategy=null;
        switch (config.existentialStrategyType) {
        case CREATION_ORDER:
            existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
            break;
        case DEPTH_FIRST:
            existentialsExpansionStrategy=new DepthFirstStrategy(blockingStrategy);
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

        return new Tableau(tableauMonitor,existentialsExpansionStrategy,m_dlOntology,config.makeTopRoleUniversal,config.parameters);
    }

    protected Namespaces createNamespaces(String ontologyURI) {
        Map<String,String> namespaceDecl=new HashMap<String,String>();
        namespaceDecl.put("",ontologyURI+"#");
        return InternalNames.withInternalNamespaces(new Namespaces(namespaceDecl,Namespaces.semanticWebNamespaces));

    }
    
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }

    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }

    /**
     * Return `true` iff `classUri` occurred in the loaded knowledge base.
     */
    public boolean isClassNameDefined(String classUri) {
        return m_dlOntology.getAllAtomicConcepts().contains(AtomicConcept.create(classUri)) || classUri.equals(AtomicConcept.THING.getURI()) || classUri.equals(AtomicConcept.NOTHING.getURI());
    }

    /**
     * Check whether `classURI` is satisfiable. Note that classes which were not defined in the input ontology are satisfiable if and only if the ontology as a whole is consistent.
     */
    public boolean isClassSatisfiable(String classURI) {
        // In an inconsistent ontology, HermiT considers all classes as unsatisfiable.
        if (!m_tableau.isABoxSatisfiable())
            return false;
        return m_subsumptionChecker.isSatisfiable(AtomicConcept.create(classURI));
    }

    public boolean isClassSatisfiable(OWLDescription desc) {
        // In an inconsistent ontology, HermiT considers all classes as unsatisfiable.
        if (!m_tableau.isABoxSatisfiable())
            return false;
        return m_subsumptionChecker.isSatisfiable(define(desc));
    }

    protected boolean isAsymmetric(OWLObjectProperty p) {
        return m_tableau.isAsymmetric(AtomicRole.createObjectRole(p.getURI().toString()));
    }

    public void seedSubsumptionCache() {
        getClassTaxonomy();
    }

    public boolean isSubsumptionCacheSeeded() {
        return m_atomicConceptHierarchy!=null;
    }

    public void cacheRealization() {
        getRealization();
    }

    public boolean isRealizationCached() {
        return m_realization!=null;
    }

    public boolean isClassSubsumedBy(String childName,String parentName) {
        // For an inconsistent ontology, HermiT answers true for all subsumptions.
        if (!m_tableau.isABoxSatisfiable())
            return true;
        return m_subsumptionChecker.isSubsumedBy(AtomicConcept.create(childName),AtomicConcept.create(parentName));
    }

    public boolean isSubsumedBy(OWLDescription child,OWLDescription parent) {
        // For an inconsistent ontology, HermiT answers true for all subsumptions.
        if (!m_tableau.isABoxSatisfiable())
            return true;
        return m_subsumptionChecker.isSubsumedBy(define(child),define(parent));
    }

    protected Map<AtomicRole,HierarchyPosition<AtomicRole>> getAtomicRoleHierarchy() {
        return m_dlOntology.getExplicitRoleHierarchy();
    }

    protected HierarchyPosition<AtomicRole> getPosition(AtomicRole r) {
        HierarchyPosition<AtomicRole> out=getAtomicRoleHierarchy().get(r);
        if (out==null) {
            NaiveHierarchyPosition<AtomicRole> newPos=new NaiveHierarchyPosition<AtomicRole>(r);
            if (r.isRestrictedToDatatypes()) {
                newPos.parents.add(getAtomicRoleHierarchy().get(AtomicRole.TOP_DATA_ROLE));
                newPos.children.add(getAtomicRoleHierarchy().get(AtomicRole.BOTTOM_DATA_ROLE));
            }
            else {
                newPos.parents.add(getAtomicRoleHierarchy().get(AtomicRole.TOP_OBJECT_ROLE));
                newPos.children.add(getAtomicRoleHierarchy().get(AtomicRole.BOTTOM_OBJECT_ROLE));
            }
            out=newPos;
        }
        return out;
    }

    public HierarchyPosition<String> getPropertyHierarchyPosition(String propertyName) {
        AtomicRole role=AtomicRole.createDataRole(propertyName);
        if (!getAtomicRoleHierarchy().containsKey(role)) {
            role=AtomicRole.createObjectRole(propertyName);
        }
        return new TranslatedHierarchyPosition<AtomicRole,String>(getPosition(role),new RoleToString());
    }

    public HierarchyPosition<OWLObjectProperty> getPosition(OWLObjectProperty p) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicRole,OWLObjectProperty>(getPosition(AtomicRole.createObjectRole(p.getURI().toString())),new RoleToOWLObjectProperty(factory));
    }

    public HierarchyPosition<OWLDataProperty> getPosition(OWLDataProperty p) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicRole,OWLDataProperty>(getPosition(AtomicRole.createDataRole(p.getURI().toString())),new RoleToOWLDataProperty(factory));
    }

    public int getNumberOfConcepts() {
        return m_dlOntology.getNumberOfExternalConcepts();
    }

    protected Map<AtomicConcept,HierarchyPosition<AtomicConcept>> getAtomicConceptHierarchy() {
        if (m_atomicConceptHierarchy==null) {
            Collection<AtomicConcept> concepts=new ArrayList<AtomicConcept>();
            concepts.add(AtomicConcept.THING);
            concepts.add(AtomicConcept.NOTHING);
            for (AtomicConcept c : m_dlOntology.getAllAtomicConcepts()) {
                if (!InternalNames.isInternalURI(c.getURI())) {
                    concepts.add(c);
                }
            }
            ReasoningCache cache=new ReasoningCache();
            cache.seed(concepts,m_tableau);
            if (cache.allSubsumptionsKnown(concepts)) {
                GraphUtils.Acyclic<AtomicConcept> acyc=new GraphUtils.Acyclic<AtomicConcept>(cache.knownSubsumers);
                GraphUtils.TransAnalyzed<AtomicConcept> trans=new GraphUtils.TransAnalyzed<AtomicConcept>(acyc.graph);
                m_atomicConceptHierarchy=new HashMap<AtomicConcept,HierarchyPosition<AtomicConcept>>();
                for (AtomicConcept c : trans.reduced.keySet()) {
                    NaiveHierarchyPosition<AtomicConcept> pos=new NaiveHierarchyPosition<AtomicConcept>(acyc.equivs.get(c));
                    for (AtomicConcept equiv : acyc.equivs.get(c)) {
                        assert acyc.canonical.get(equiv)==c;
                        assert pos.labels.contains(equiv);
                        m_atomicConceptHierarchy.put(equiv,pos);
                    }
                }
                if (!m_atomicConceptHierarchy.containsKey(AtomicConcept.THING)) {
                    m_atomicConceptHierarchy.put(AtomicConcept.THING,new NaiveHierarchyPosition<AtomicConcept>(AtomicConcept.THING));
                }
                for (Map.Entry<AtomicConcept,Set<AtomicConcept>> e : trans.reduced.entrySet()) {
                    AtomicConcept child=e.getKey();
                    for (AtomicConcept parent : e.getValue()) {
                        ((NaiveHierarchyPosition<AtomicConcept>)m_atomicConceptHierarchy.get(child)).parents.add(m_atomicConceptHierarchy.get(parent));
                        ((NaiveHierarchyPosition<AtomicConcept>)m_atomicConceptHierarchy.get(parent)).children.add(m_atomicConceptHierarchy.get(child));
                    }
                }
            }
            else {
                m_atomicConceptHierarchy=m_classifier.buildHierarchy(AtomicConcept.THING,AtomicConcept.NOTHING,concepts);
            }
        }
        return m_atomicConceptHierarchy;
    }

    protected HierarchyPosition<AtomicConcept> getPosition(AtomicConcept c) {
        HierarchyPosition<AtomicConcept> out=getAtomicConceptHierarchy().get(c);
        if (out==null) {
            out=m_classifier.findPosition(c,getAtomicConceptHierarchy().get(AtomicConcept.THING),getAtomicConceptHierarchy().get(AtomicConcept.NOTHING));
        }
        return out;
    }

    private AtomicConcept define(OWLDescription desc) {
        if (desc.isAnonymous()) {
            throw new IllegalArgumentException("Complex descriptions are not supported yet.");
        }
        else
            return AtomicConcept.create(desc.asOWLClass().getURI().toString());
    }

    static class ConceptToString implements Translator<AtomicConcept,String> {
        public String translate(AtomicConcept c) {
            return c.getURI();
        }
        public boolean equals(Object o) {
            return o instanceof ConceptToString;
        }
        public int hashCode() {
            return 0;
        }
    }

    public Map<String,HierarchyPosition<String>> getClassTaxonomy() {
        return new TranslatedMap<AtomicConcept,String,HierarchyPosition<AtomicConcept>,HierarchyPosition<String>>(getAtomicConceptHierarchy(),new ConceptToString(),new StringToConcept(),new PositionTranslator<AtomicConcept,String>(new ConceptToString()));
    }

    public HierarchyPosition<String> getClassTaxonomyPosition(String className) {
        if (!isClassNameDefined(className)) {
            throw new RuntimeException("unrecognized class name '"+className+"'");
        }
        return getClassTaxonomy().get(className);
    }

    public HierarchyPosition<OWLClass> getPosition(OWLDescription description) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicConcept,OWLClass>(getPosition(define(description)),new ConceptToOWLClass(factory));
    }

    protected HierarchyPosition<AtomicConcept> getMemberships(Individual individual) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getPosition(define(factory.getOWLObjectOneOf(factory.getOWLIndividual(URI.create(individual.getURI())))));
    }

    public HierarchyPosition<String> getMemberships(String individual) {
        return new TranslatedHierarchyPosition<AtomicConcept,String>(getMemberships(Individual.create(individual)),new ConceptToString());
    }

    public HierarchyPosition<OWLClass> getMemberships(OWLIndividual i) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicConcept,OWLClass>(getMemberships(Individual.create(i.getURI().toString())),new ConceptToOWLClass(factory));
    }

    private Map<AtomicConcept,Set<Individual>> getRealization() {
        if (m_realization==null) {
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            m_realization=new HashMap<AtomicConcept,Set<Individual>>();
            for (Individual i : m_dlOntology.getAllIndividuals()) {
                HierarchyPosition<AtomicConcept> p=getPosition(define(factory.getOWLObjectOneOf(factory.getOWLIndividual(URI.create(i.getURI())))));
                Set<AtomicConcept> parents=p.getEquivalents();
                if (parents.isEmpty()) {
                    parents=new HashSet<AtomicConcept>();
                    for (HierarchyPosition<AtomicConcept> parentPos : p.getParentPositions()) {
                        parents.addAll(parentPos.getEquivalents());
                    }
                }
                for (AtomicConcept c : parents) {
                    if (!m_realization.containsKey(c)) {
                        m_realization.put(c,new HashSet<Individual>());
                    }
                    m_realization.get(c).add(i);
                }
            }
        }
        return m_realization;
    }

    public Set<String> getDirectMembers(String className) {
        return new TranslatedSet<Individual,String>(getRealization().get(AtomicConcept.create(className)),new IndividualToString());
    }

    public Set<String> getMembers(String className) {
        Set<String> out=new HashSet<String>();
        for (AtomicConcept c : getPosition(AtomicConcept.create(className)).getDescendants()) {
            Set<Individual> realizationForConcept=getRealization().get(c);
            if (realizationForConcept!=null) {
                for (Individual i : realizationForConcept) {
                    out.add(i.getURI());
                }
            }
        }
        return out;
    }

    public Set<OWLIndividual> getMembers(OWLDescription description) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLIndividual> out=new HashSet<OWLIndividual>();
        for (AtomicConcept c : getPosition(define(description)).getDescendants()) {
            Set<Individual> realizationForConcept=getRealization().get(c);
            if (realizationForConcept!=null) {
                for (Individual i : realizationForConcept) {
                    out.add(factory.getOWLIndividual(URI.create(i.getURI())));
                }
            }
        }
        return out;
    }

    public Set<OWLIndividual> getDirectMembers(OWLDescription description) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLIndividual> out=new HashSet<OWLIndividual>();
        HierarchyPosition<AtomicConcept> p=getPosition(define(description));
        Set<AtomicConcept> children=p.getEquivalents();
        if (children.isEmpty())
            for (HierarchyPosition<AtomicConcept> childPos : p.getChildPositions())
                children.addAll(childPos.getEquivalents());
        for (AtomicConcept c : children) {
            Set<Individual> realizationForConcept=getRealization().get(c);
            if (realizationForConcept!=null)
                for (Individual i : realizationForConcept)
                    out.add(factory.getOWLIndividual(URI.create(i.getURI())));
        }
        return out;
    }

    public void printSortedAncestorLists(PrintWriter output) {
        printSortedAncestorLists(output,getClassTaxonomy());
    }

    public static void printSortedAncestorLists(PrintWriter output,Map<String,HierarchyPosition<String>> taxonomy) {
        Map<String,Set<String>> flat=new TreeMap<String,Set<String>>();
        for (Map.Entry<String,HierarchyPosition<String>> e : taxonomy.entrySet()) {
            if (!e.getKey().equals("http://www.w3.org/2002/07/owl#Nothing")) {
                Set<String> ancestors=new TreeSet<String>();
                for (String ancestor : e.getValue().getAncestors())
                    if (!"http://www.w3.org/2002/07/owl#Thing".equals(ancestor))
                        ancestors.add(ancestor);
                flat.put(e.getKey(),ancestors);
            }
        }
        try {
            for (Map.Entry<String,Set<String>> e : flat.entrySet()) {
                output.println(e.getKey());
                for (String ancestor : e.getValue()) {
                    output.print("    ");
                    output.println(ancestor);
                }
                output.println("--------------------------------");
            }
            output.println("! THE END !");
        }
        finally {
            output.flush();
        }
    }

    public void outputClauses(PrintWriter output,Namespaces namespaces) {
        output.println(m_dlOntology.toString(namespaces));
    }

    public Namespaces getNamespaces() {
        return m_namespaces;
    }

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

    /**
     * Creates an ObjectInputStream from the given input stream and tries to read (deserialise) a (serialised) Reasoner object from the stream.
     * 
     * @param inputStream
     *            an input stream that contains a Reasoner object
     * @return the instance of Reasoner as read from the given input stream
     * @throws IOException
     *             if an IOException occurs or if the Reasoner class cannot be found
     */
    public static Reasoner load(InputStream inputStream) throws IOException {
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

    /**
     * Tries to deserialize a Reasoner object from the given file.
     * 
     * @param file
     *            a file that contains the serialisation of a Reasoner object
     * @return the deserialzed Reasoner object
     * @throws IOException
     *             if the file cannot be read or does not contain a searialized Reasoner object
     */
    public static Reasoner load(File file) throws IOException {
        InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
        try {
            return load(inputStream);
        }
        finally {
            inputStream.close();
        }
    }

    static class RoleToString implements Translator<AtomicRole,String> {
        public String translate(AtomicRole r) {
            return r.getURI();
        }
        public boolean equals(Object o) {
            return o instanceof ConceptToString;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class ConceptToOWLClass implements Translator<AtomicConcept,OWLClass> {
        private OWLDataFactory factory;
        ConceptToOWLClass(OWLDataFactory factory) {
            this.factory=factory;
        }
        public OWLClass translate(AtomicConcept c) {
            return factory.getOWLClass(URI.create(c.getURI()));
        }
        public boolean equals(Object o) {
            return o instanceof ConceptToOWLClass;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class RoleToOWLObjectProperty implements Translator<AtomicRole,OWLObjectProperty> {
        private OWLDataFactory factory;
        RoleToOWLObjectProperty(OWLDataFactory factory) {
            this.factory=factory;
        }
        public OWLObjectProperty translate(AtomicRole r) {
            // should really ensure that r is an object, not data, role
            return factory.getOWLObjectProperty(URI.create(r.getURI()));
        }
        public boolean equals(Object o) {
            return o instanceof RoleToOWLObjectProperty;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class RoleToOWLDataProperty implements Translator<AtomicRole,OWLDataProperty> {
        private OWLDataFactory factory;
        RoleToOWLDataProperty(OWLDataFactory factory) {
            this.factory=factory;
        }
        public OWLDataProperty translate(AtomicRole r) {
            // should really ensure that r is a data, not object, role
            return factory.getOWLDataProperty(URI.create(r.getURI()));
        }
        public boolean equals(Object o) {
            return o instanceof RoleToOWLDataProperty;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class IndividualToString implements Translator<Individual,String> {
        public String translate(Individual i) {
            return i.getURI();
        }
        public boolean equals(Object o) {
            return o instanceof IndividualToString;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class StringToConcept implements Translator<Object,AtomicConcept> {
        public AtomicConcept translate(Object o) {
            return AtomicConcept.create(o.toString());
        }
        public boolean equals(Object o) {
            return o instanceof StringToConcept;
        }
        public int hashCode() {
            return 0;
        }
    }

    static class OWLClassToConcept implements Translator<Object,AtomicConcept> {
        public AtomicConcept translate(Object o) {
            return AtomicConcept.create(((OWLClass)o).getURI().toString());
        }
        public boolean equals(Object o) {
            return o instanceof OWLClassToConcept;
        }
        public int hashCode() {
            return 0;
        }
    }
}
