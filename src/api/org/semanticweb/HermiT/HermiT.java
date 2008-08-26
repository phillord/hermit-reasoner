/*
 * HermiT
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

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
import org.semanticweb.HermiT.existentials.ExistentialsExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.hierarchy.NaiveHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchy;
import org.semanticweb.HermiT.hierarchy.SubsumptionHierarchyNode;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.kaon2.structural.Clausification;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.monitor.TableauMonitorFork;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.HermiT.monitor.TimerWithPause;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.HermiT.util.Translator;
import org.semanticweb.HermiT.util.TranslatedMap;
import org.semanticweb.HermiT.hierarchy.TranslatedHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.PositionTranslator;

public class HermiT implements Serializable {
	private static final long serialVersionUID=-8277117863937974032L;

    public static enum TableauMonitorType {
        NONE, TIMING, TIMING_WITH_PAUSE,
        DEBUGGER_NO_HISTORY, DEBUGGER_HISTORY_ON
    };
    public static enum DirectBlockingType {
        SINGLE, PAIR_WISE, PAIR_WISE_REFLEXIVE, OPTIMAL
    };
    public static enum BlockingStrategyType { ANYWHERE, ANCESTOR };
    public static enum BlockingSignatureCacheType { CACHED, NOT_CACHED };
    public static enum ExistentialStrategyType {
        CREATION_ORDER, EL, INDIVIDUAL_REUSE
    };
    public static enum ParserType { KAON2, OWLAPI };
    public static enum SubsumptionCacheStrategyType {
        IMMEDIATE, JUST_IN_TIME, ON_REQUEST
    };
    
    public static class Configuration {
        public TableauMonitorType tableauMonitorType;
        public DirectBlockingType directBlockingType;
        public BlockingStrategyType blockingStrategyType;
        public BlockingSignatureCacheType blockingSignatureCacheType;
        public ExistentialStrategyType existentialStrategyType;
        public ParserType parserType;
        public SubsumptionCacheStrategyType subsumptionCacheStrategyType;
        public final Map<String,Object> parameters;
    
        public Configuration() {
            tableauMonitorType = TableauMonitorType.NONE;
            directBlockingType = DirectBlockingType.OPTIMAL;
            blockingStrategyType = BlockingStrategyType.ANYWHERE;
            blockingSignatureCacheType = BlockingSignatureCacheType.CACHED;
            existentialStrategyType = ExistentialStrategyType.CREATION_ORDER;
            parserType = ParserType.OWLAPI;
            subsumptionCacheStrategyType =
                SubsumptionCacheStrategyType.IMMEDIATE;
            parameters = new HashMap<String,Object>();
        }

        protected void setIndividualReuseStrategyReuseAlways
            (Set<? extends LiteralConcept> concepts) {
            parameters.put("IndividualReuseStrategy.reuseAlways", concepts);
        }

        public void loadIndividualReuseStrategyReuseAlways(File file)
            throws IOException {
            Set<AtomicConcept> concepts = loadConceptsFromFile(file);
            setIndividualReuseStrategyReuseAlways(concepts);
        }

        protected void setIndividualReuseStrategyReuseNever
            (Set<? extends LiteralConcept> concepts) {
            parameters.put("IndividualReuseStrategy.reuseNever", concepts);
        }

        public void loadIndividualReuseStrategyReuseNever(File file)
            throws IOException {
            Set<AtomicConcept> concepts = loadConceptsFromFile(file);
            setIndividualReuseStrategyReuseNever(concepts);
        }

        protected Set<AtomicConcept> loadConceptsFromFile(File file)
            throws IOException {
            Set<AtomicConcept> result = new HashSet<AtomicConcept>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = reader.readLine();
                while (line != null) {
                    result.add(AtomicConcept.create(line));
                    line = reader.readLine();
                }
                return result;
            } finally {
                reader.close();
            }
        }
        
    } // end Configuration class

    private final Configuration m_config; // never null
    private DLOntology m_dlOntology; // never null
    private Namespaces m_namespaces; // never null
    private TableauMonitor m_userTableauMonitor;
    private Tableau m_tableau; // never null
    private TableauSubsumptionChecker m_subsumptionChecker; // never null
    private Map<AtomicConcept, HierarchyPosition<AtomicConcept>>
        atomicConceptHierarchy; // may be null; use getAtomicConceptHierarchy
    
    public HermiT(String ontologyURI)
        throws KAON2Exception, OWLException, InterruptedException {
        m_config = new Configuration();
        loadOntology(URI.create(ontologyURI));
    }
    
    public HermiT(java.net.URI ontologyURI)
        throws KAON2Exception, OWLException, InterruptedException {
        m_config = new Configuration();
        loadOntology(ontologyURI);
    }
    
    public HermiT(java.net.URI ontologyURI, Configuration config)
        throws KAON2Exception, OWLException, InterruptedException {
        m_config = config;
        loadOntology(ontologyURI);
    }
    
    public HermiT(OWLOntology ontology, Configuration config)
        throws OWLException, InterruptedException {
        m_config = config;
        // FIXME: do the identities of the manager and factory matter?
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        loadOwlOntology(ontology, manager.getOWLDataFactory(),
                        (Set<DescriptionGraph>) null);
    }

    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }

    public boolean isClassSatisfiable(String className) {
        return m_subsumptionChecker.isSatisfiable(
            AtomicConcept.create(className)
        );
    }

    public void seedSubsumptionCache() {
        getClassTaxonomy();
    }
    
    public boolean isSubsumptionCacheSeeded() {
        return atomicConceptHierarchy != null;
    }

    public boolean isClassSubsumedBy(String childName,
                                     String parentName) {
        return m_subsumptionChecker.isSubsumedBy(
            AtomicConcept.create(childName), AtomicConcept.create(parentName)
        );
    }
    
    public SubsumptionHierarchy getSubsumptionHierarchy() {
        try {
            return new SubsumptionHierarchy(m_subsumptionChecker);
        } catch (SubsumptionHierarchy.SubusmptionCheckerException e) {
            throw new RuntimeException(
                "Unable to compute subsumption hierarchy.");
        }
    }
    
    protected Map<AtomicConcept, HierarchyPosition<AtomicConcept>>
        getAtomicConceptHierarchy() {
        if (atomicConceptHierarchy == null) {
            SubsumptionHierarchy oldHierarchy;
            try {
                 oldHierarchy = new SubsumptionHierarchy(m_subsumptionChecker);
            } catch (SubsumptionHierarchy.SubusmptionCheckerException e) {
                throw new RuntimeException(
                    "Unable to compute subsumption hierarchy.");
            }
        
            Map<AtomicConcept, HierarchyPosition<AtomicConcept>> newHierarchy =
                new HashMap<AtomicConcept, HierarchyPosition<AtomicConcept>>();
        
            Map<AtomicConcept, NaiveHierarchyPosition<AtomicConcept>> newNodes
                = new HashMap<AtomicConcept,
                                NaiveHierarchyPosition<AtomicConcept>>();
            // First just create all the new hierarchy nodes:
            for (SubsumptionHierarchyNode oldNode : oldHierarchy) {
                NaiveHierarchyPosition<AtomicConcept> newNode =
                    new NaiveHierarchyPosition<AtomicConcept>();
                newNodes.put(oldNode.getRepresentative(), newNode);
                for (AtomicConcept concept : oldNode.getEquivalentConcepts()) {
                    newNode.labels.add(concept);
                    if (newHierarchy.put(concept, newNode) !=
                        null) {
                        throw new RuntimeException("The '" + concept.getURI() +
                            "' concept occurs in two different places" +
                            " in the taxonomy.");
                    }
                }
            }
            // Now connect them together:
            for (SubsumptionHierarchyNode oldNode : oldHierarchy) {
                NaiveHierarchyPosition<AtomicConcept> newNode =
                    newNodes.get(oldNode.getRepresentative());
                for (SubsumptionHierarchyNode parent
                        : oldNode.getParentNodes()) {
                    newNode.parents.add(
                        newNodes.get(parent.getRepresentative())
                    );
                }
                for (SubsumptionHierarchyNode child
                        : oldNode.getChildNodes()) {
                    newNode.children.add(
                        newNodes.get(child.getRepresentative())
                    );
                }
            }
            // Construction finished; set our member cache:
            atomicConceptHierarchy = newHierarchy;
        }
        return atomicConceptHierarchy;
    }
    
    public Map<String, HierarchyPosition<String>> getClassTaxonomy() {
        class StringTranslator implements Translator<AtomicConcept, String> {
            public String translate(AtomicConcept c) {
                return c.getURI();
            }
            public boolean equals(Object o) {
                return o instanceof StringTranslator;
            }
            public int hashCode() {
                return 0;
            }
        }
        class ConceptTranslator implements Translator<Object, AtomicConcept> {
            public AtomicConcept translate(Object o) {
                return AtomicConcept.create(o.toString());
            }
            public boolean equals(Object o) {
                return o instanceof ConceptTranslator;
            }
            public int hashCode() {
                return 0;
            }
        }
        return new TranslatedMap<
                AtomicConcept, String, HierarchyPosition<AtomicConcept>,
                HierarchyPosition<String>
            >(getAtomicConceptHierarchy(), new StringTranslator(),
                new ConceptTranslator(),
                new PositionTranslator<AtomicConcept, String>(new StringTranslator()));
    }
    
    public HierarchyPosition<String>
        getClassTaxonomyPosition(String className) {
        return getClassTaxonomy().get(className);
    }
    
    public void printSortedAncestorLists(PrintWriter output) {
        printSortedAncestorLists(output, getClassTaxonomy());
    }
    
    public static void printSortedAncestorLists(
        PrintWriter output, Map<String, HierarchyPosition<String>> taxonomy) {
        Map<String, Set<String>> flat = new TreeMap<String, Set<String>>();
        for (Map.Entry<String, HierarchyPosition<String>> e :
                taxonomy.entrySet()) {
            flat.put(e.getKey(),
                     new TreeSet<String>(e.getValue().getAncestors()));
        }
        try {
            for (Map.Entry<String, Set<String>> e : flat.entrySet()) {
                output.println("'" + e.getKey() + "' ancestors:");
                for (String ancestor : e.getValue()) {
                    output.println("\t" + ancestor);
                }
                output.println("--------------------------------"); // 32
            }
            output.println("! THE END !");
        } finally {
            output.flush();
        }
    }


    // public void setUserTableauMonitor(TableauMonitor userTableauMonitor) {
    //     m_userTableauMonitor=userTableauMonitor;
    // }
    protected void loadOntology(URI physicalURI)
        throws KAON2Exception, OWLException, InterruptedException {
        loadOntology(physicalURI, null);
    }
    
    protected void loadOntology(URI physicalURI,
                               Set<DescriptionGraph> descriptionGraphs)
        throws KAON2Exception, OWLException, InterruptedException {
        switch (m_config.parserType) {
            case KAON2: {
                DefaultOntologyResolver resolver =
                    new DefaultOntologyResolver();
                String ontologyURI =
                    resolver.registerOntology(physicalURI.toString());
                OntologyManager ontologyManager =
                    KAON2Manager.newOntologyManager();
                ontologyManager.setOntologyResolver(resolver);
                Ontology ontology =
                    ontologyManager.openOntology
                        (ontologyURI, new HashMap<String,Object>());
                loadKAON2Ontology(ontology, descriptionGraphs);
            } break;
            case OWLAPI: {
                OWLOntologyManager manager =
                    OWLManager.createOWLOntologyManager();
                OWLOntology o =
                    manager.loadOntologyFromPhysicalURI(physicalURI);
                loadOwlOntology(o, manager.getOWLDataFactory(),
                                descriptionGraphs);
            } break;
            default:
                throw new IllegalArgumentException(
                    "unknown parser library requested");
        }
    }
    
    protected void loadOwlOntology(OWLOntology ontology,
                                   OWLDataFactory factory,
                                   Set<DescriptionGraph> descriptionGraphs)
        throws OWLException {
        if (descriptionGraphs == null) {
            descriptionGraphs = Collections.emptySet();
        }
        OwlClausification c = new OwlClausification();
        DLOntology d = c.clausify(
            m_config.existentialStrategyType ==
                ExistentialStrategyType.INDIVIDUAL_REUSE,
            ontology, factory, descriptionGraphs
        );
        loadDLOntology(d);
    }
    
    protected void loadKAON2Ontology(Ontology ontology,
                                     Set<DescriptionGraph> descriptionGraphs)
        throws KAON2Exception {
        if (descriptionGraphs == null) {
            descriptionGraphs = Collections.emptySet();
        }
        Clausification clausification=new Clausification();
        DLOntology dlOntology = clausification.clausify(
            m_config.existentialStrategyType ==
                ExistentialStrategyType.INDIVIDUAL_REUSE,
            ontology,descriptionGraphs
        );
        loadDLOntology(dlOntology);
    }
    
    protected void loadDLOntology(File file) throws Exception {
        BufferedInputStream input =
            new BufferedInputStream(new FileInputStream(file));
        try {
            loadDLOntology(DLOntology.load(input));
        } finally {
            input.close();
        }
    }
    
    protected void loadDLOntology(DLOntology dlOntology)
        throws IllegalArgumentException {
        if (!dlOntology.canUseNIRule() &&
            dlOntology.hasAtMostRestrictions() &&
            dlOntology.hasInverseRoles() &&
            (m_config.existentialStrategyType ==
                ExistentialStrategyType.INDIVIDUAL_REUSE)) {
            throw new IllegalArgumentException(
                "The supplied DL-onyology is not compatible" +
                " with the individual reuse strategy.");
        }
        Namespaces namespaces = new Namespaces();
        namespaces.registerStandardPrefixes();
        namespaces.registerPrefix("a", dlOntology.getOntologyURI() + "#");
        namespaces.registerInternalPrefixes(dlOntology.getOntologyURI());
        Collection<DLClause> nonAdmissibleDLClauses =
            dlOntology.getNonadmissibleDLClauses();
        if (!nonAdmissibleDLClauses.isEmpty()) {
            String CRLF = System.getProperty("line.separator");
            StringBuffer buffer = new StringBuffer();
            buffer.append("The following DL-clauses in the DL-ontology" +
                          " are not admissible:");
            buffer.append(CRLF);
            for (DLClause dlClause : nonAdmissibleDLClauses) {
                buffer.append(dlClause.toString(namespaces));
                buffer.append(CRLF);
            }
            throw new IllegalArgumentException(buffer.toString());
        }
        m_dlOntology = dlOntology;
        m_namespaces = namespaces;
        
        TableauMonitor wellKnownTableauMonitor = null;
        switch (m_config.tableauMonitorType) {
        case NONE:
            wellKnownTableauMonitor = null;
            break;
        case TIMING:
            wellKnownTableauMonitor = new Timer();
            break;
        case TIMING_WITH_PAUSE:
            wellKnownTableauMonitor = new TimerWithPause();
            break;
        case DEBUGGER_HISTORY_ON:
            wellKnownTableauMonitor = new Debugger(m_namespaces, true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor = new Debugger(m_namespaces, false);
            break;
        default:
            throw new IllegalArgumentException("Unknown monitor type");
        }
        
        TableauMonitor tableauMonitor = null;
        if (m_userTableauMonitor == null) {
            tableauMonitor = wellKnownTableauMonitor;
        } else if (wellKnownTableauMonitor == null) {
            tableauMonitor = m_userTableauMonitor;
        } else {
            tableauMonitor = new TableauMonitorFork(wellKnownTableauMonitor,
                                                    m_userTableauMonitor);
        }
        
        DirectBlockingChecker directBlockingChecker = null;
        switch (m_config.directBlockingType) {
        case OPTIMAL:
            if (m_dlOntology.hasAtMostRestrictions() &&
                m_dlOntology.hasInverseRoles()) {
                if (m_dlOntology.hasReflexifity()) {
        			directBlockingChecker =
        			    new PairwiseDirectBlockingCheckerWithReflexivity();
        		} else {
        			directBlockingChecker =
        			    new PairWiseDirectBlockingChecker();
    			}
        	} else {
        		directBlockingChecker = new SingleDirectBlockingChecker();
        	}
        	break;
        case SINGLE:
            directBlockingChecker = new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
        	directBlockingChecker = new PairWiseDirectBlockingChecker();
            break;
        case PAIR_WISE_REFLEXIVE:
    		directBlockingChecker =
        		    new PairwiseDirectBlockingCheckerWithReflexivity();
        	break;
        default:
            throw new IllegalArgumentException(
                "Unknown direct blocking type.");
        }
        
        BlockingSignatureCache blockingSignatureCache = null;
        if (!dlOntology.hasNominals()) {
            switch (m_config.blockingSignatureCacheType) {
            case CACHED:
                blockingSignatureCache =
                    new BlockingSignatureCache(directBlockingChecker);
                break;
            case NOT_CACHED:
                blockingSignatureCache = null;
                break;
            default:
                throw new IllegalArgumentException(
                    "Unknown blocking cache type.");
            }
        }
        
        BlockingStrategy blockingStrategy = null;
        switch (m_config.blockingStrategyType) {
        case ANCESTOR:
            blockingStrategy = new AncestorBlocking(directBlockingChecker,
                                                    blockingSignatureCache);
            break;
        case ANYWHERE:
            blockingStrategy = new AnywhereBlocking(directBlockingChecker,
                                                    blockingSignatureCache);
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown blocking strategy type.");
        }
        
        ExistentialsExpansionStrategy existentialsExpansionStrategy = null;
        switch (m_config.existentialStrategyType) {
        case CREATION_ORDER:
            existentialsExpansionStrategy =
                new CreationOrderStrategy(blockingStrategy);
            break;
        case EL:
            existentialsExpansionStrategy =
                new IndividualReuseStrategy(blockingStrategy,true);
            break;
        case INDIVIDUAL_REUSE:
            existentialsExpansionStrategy =
                new IndividualReuseStrategy(blockingStrategy,false);
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown expansion strategy type.");
        }
        
        m_tableau = new Tableau(tableauMonitor,
                                existentialsExpansionStrategy,
                                m_dlOntology,
                                m_config.parameters);
        m_subsumptionChecker = new TableauSubsumptionChecker(m_tableau);
        if (m_config.subsumptionCacheStrategyType ==
            SubsumptionCacheStrategyType.IMMEDIATE) {
            getClassTaxonomy();
        }
    }
    
    
    public Namespaces getNamespaces() {
        return m_namespaces;
    }

    public void save(File file) throws IOException {
        OutputStream outputStream =
            new BufferedOutputStream(new FileOutputStream(file));
        try {
            save(outputStream);
        } finally {
            outputStream.close();
        }
    }
    
    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream =
            new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }
    
    public static HermiT load(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream =
                new ObjectInputStream(inputStream);
            return (HermiT) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            IOException error=new IOException();
            error.initCause(e);
            throw error;
        }
    }
    public static HermiT load(File file) throws IOException {
        InputStream inputStream =
            new BufferedInputStream(new FileInputStream(file));
        try {
            return load(inputStream);
        } finally {
            inputStream.close();
        }
    }
    
    public static void main(String[] args) {
        for (String f : args) {
            try {
                HermiT h = new HermiT(f);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

}
