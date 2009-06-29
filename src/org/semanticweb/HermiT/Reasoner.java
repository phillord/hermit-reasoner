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
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.existentials.LazyStrategy;
import org.semanticweb.HermiT.hierarchy.DeterministicHierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyPrinterFSS;
import org.semanticweb.HermiT.hierarchy.SubsumptionCache;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
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
        Set<String> noDefinedDatatypeIRIs=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("urn:hermit:kb",noDLClauses,noAtoms,noAtoms,noAtomicConcepts,noComplexObjectRoleInclusions,noAtomicRoles,noAtomicRoles,noDefinedDatatypeIRIs,noIndividuals,false,false,false,false);
        loadDLOntology(emptyDLOntology);
    }
    
    public void dispose() {
        clearOntologies();
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
        Individual individual;
        if (owlIndividual.isAnonymous()) {
            individual=Individual.create(owlIndividual.asAnonymousIndividual().getID().toString(),false);
        } else {
            individual=Individual.create(owlIndividual.asNamedIndividual().getIRI().toString(),true);
        }
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
    @Deprecated
    public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
        return getUnsatisfiableClasses();
    }
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
            OWLAxiom subAssertion=factory.getOWLObjectPropertyAssertionAxiom(subObjectPropertyExpression,individualA,individualB);
            OWLAxiom superAssertion=factory.getOWLObjectPropertyAssertionAxiom(negatedSuperProperty,individualA,individualB);
            OWLAxiom superDisjoint=factory.getOWLDisjointObjectPropertiesAxiom(superObjectPropertyExpression,negatedSuperProperty);
            Tableau tableau=getTableau(ontologyManager,subAssertion,superAssertion,superDisjoint);
            return !tableau.isABoxSatisfiable();
        }
    }
    
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
    
    public boolean isEquivalentProperty(OWLObjectPropertyExpression objectPropertyExpression1,OWLObjectPropertyExpression objectPropertyExpression2) {
        return isSubPropertyOf(objectPropertyExpression1,objectPropertyExpression2) && isSubPropertyOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSuperProperties((OWLObjectPropertyExpression)property));
    }
    public Set<Set<OWLObjectPropertyExpression>> getSuperProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    @Deprecated
    public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getSubProperties((OWLObjectPropertyExpression)property));
    }
    public Set<Set<OWLObjectPropertyExpression>> getSubProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    @Deprecated
    public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getAncestorProperties((OWLObjectPropertyExpression)property));
    }
    public Set<Set<OWLObjectPropertyExpression>> getAncestorProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> ancestorsPlusNode=new HashSet<HierarchyNode<Role>>(node.getAncestorNodes());
        ancestorsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(ancestorsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    @Deprecated
    public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) {
        return filterObjectPropertySets(getDescendantProperties((OWLObjectPropertyExpression)property));
    }
    public Set<Set<OWLObjectPropertyExpression>> getDescendantProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        Set<HierarchyNode<Role>> descendantsPlusNode=new HashSet<HierarchyNode<Role>>(node.getDescendantNodes());
        descendantsPlusNode.add(node);
        return objectPropertyNodesToOWLAPI(descendantsPlusNode,OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    @Deprecated
    public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) {
        return filterObjectProperties(getEquivalentProperties((OWLObjectPropertyExpression)property));
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
    public Set<OWLObjectPropertyExpression> getInverseProperties(OWLObjectPropertyExpression property) {
        return getEquivalentProperties(property.getInverseProperty());
    }
    public boolean isFunctional(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,property));
    }

    public boolean isInverseFunctional(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinality(2,property.getInverseProperty()));
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
        if (property.isOWLTopDataProperty()) return true;        
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:individualA"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:individualB"));
        OWLAxiom assertion=factory.getOWLObjectPropertyAssertionAxiom(property,individualA,individualB);
        OWLObjectAllValuesFrom all=factory.getOWLObjectAllValuesFrom(property, factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(individualA)));
        OWLAxiom assertion2=factory.getOWLClassAssertionAxiom(all, individualB);
        Tableau tableau=getTableau(ontologyManager,assertion,assertion2);
        return !tableau.isABoxSatisfiable();
    }

    public boolean isTransitive(OWLObjectProperty property) {
        List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();
        chain.add(property);
        chain.add(property);
        return isSubPropertyOf(chain, property);
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
        else if (subDataProperty.getIRI().toString().equals(AtomicRole.BOTTOM_DATA_ROLE.getIRI()))
            return true;
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLIndividual individual=factory.getOWLNamedIndividual(IRI.create("internal:individual"));
            OWLDataProperty negatedSuperProperty=factory.getOWLDataProperty(IRI.create("internal:negated-superproperty"));
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLTypedLiteral constant=factory.getOWLTypedLiteral("internal:constant",anonymousConstantsDatatype);
            OWLAxiom subAssertion=factory.getOWLDataPropertyAssertionAxiom(subDataProperty,individual,constant);
            OWLAxiom superAssertion=factory.getOWLDataPropertyAssertionAxiom(negatedSuperProperty,individual,constant);
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
            for (Individual individual : realizationForConcept) {
                if (individual.isNamed()) 
                    result.add(factory.getOWLNamedIndividual(IRI.create(individual.getIRI())));
            }
    }
    
    public Map<OWLObjectProperty,Set<OWLNamedIndividual>> getObjectPropertyRelationships(OWLNamedIndividual individual) {
        throw new UnsupportedOperationException();
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
    
    public Boolean entailsDatatypeDefinition(OWLDatatypeDefinitionAxiom axiom) {
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
        case CORE:
            blockingStrategy=new AnywhereCoreBlocking(new CorePreDirectBlockingChecker(),dlOntology.getUnaryValidBlockConditions(),dlOntology.getNAryValidBlockConditions(),dlOntology.hasInverseRoles());
            break;
        case TWOPHASE:
            blockingStrategy=new AnywhereTwoPhaseBlocking(new TwoPhaseDirectBlockingChecker(),dlOntology.getUnaryValidBlockConditions(),dlOntology.getNAryValidBlockConditions(),dlOntology.hasInverseRoles());
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
        case LAZY: 
            if (config.blockingStrategyType!=BlockingStrategyType.CORE) {
                throw new IllegalArgumentException("Lazy expansion can only be used with core blocking. ");
            }
            existentialsExpansionStrategy=new LazyStrategy(blockingStrategy);
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
                /**
                 * gstoil this is obsolete
                 */
//                for (DLOntology.ComplexObjectRoleInclusion inclusion : originalDLOntology.getAllComplexObjectRoleInclusions()) {
//                    OWLObjectPropertyExpression[] subObjectPropertyExpressions=new OWLObjectPropertyExpression[inclusion.getNumberOfSubRoles()];
//                    for (int index=inclusion.getNumberOfSubRoles()-1;index>=0;--index)
//                        subObjectPropertyExpressions[index]=getObjectPropertyExpression(factory,inclusion.getSubRole(index));
//                    OWLObjectPropertyExpression superObjectPropertyExpression=getObjectPropertyExpression(factory,inclusion.getSuperRole());
//                    objectPropertyInclusionManager.addInclusion(subObjectPropertyExpressions,superObjectPropertyExpression);
//                }
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
            if (config.blockingStrategyType==Configuration.BlockingStrategyType.CORE || config.blockingStrategyType==Configuration.BlockingStrategyType.TWOPHASE) {
                Map<AtomicConcept,Set<Set<Concept>>> unaryValidBlockConditions=createUnion(originalDLOntology.getUnaryValidBlockConditions(),newDLOntology.getUnaryValidBlockConditions());
                Map<Set<AtomicConcept>,Set<Set<Concept>>> nAryValidBlockConditions=createUnion(originalDLOntology.getNAryValidBlockConditions(),newDLOntology.getNAryValidBlockConditions());
                return new DLOntology(resultingOntologyIRI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,complexObjectRoleInclusions,atomicObjectRoles,atomicDataRoles,definedDatatypeIRIs,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,hasDatatypes,unaryValidBlockConditions,nAryValidBlockConditions);
            }
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
    
    // The factory for OWL API OWL reasoners
    
    public static class ReasonerFactory implements OWLReasonerFactory {

        public OWLReasoner createReasoner(OWLOntologyManager manager) {
            return this.createReasoner(manager, new HashSet<OWLOntology>());
        }
        public String getReasonerName() {
            return getClass().getPackage().getImplementationTitle();
        }
        public OWLReasoner createReasoner(OWLOntologyManager manager,Set<OWLOntology> ontologies) {
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            Reasoner hermit=new Reasoner(configuration,manager,ontologies);
            return hermit;
        }
    }
   
    // The factory for the reasoner from the Protege plug-in
    // outsourced into a separate file since it cannot be compiled until Protege 4.1 is updated
}
