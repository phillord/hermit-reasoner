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
import java.net.URI;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.semanticweb.HermiT.blocking.AncestorBlocking;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.blocking.SingleDirectBlockingChecker;
import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.HierarchyBuilder;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.hierarchy.DeterministicHierarchyBuilder;
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
import org.semanticweb.HermiT.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.structural.OWLNormalization;
import org.semanticweb.HermiT.structural.TransitivityManager;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.inference.OWLReasoner;

/**
 * Answers queries about the logical implications of a particular knowledge base. A Reasoner is associated with a single knowledge base, which is "loaded" when the reasoner is constructed. By default a full classification of all atomic terms in the knowledge base is also performed at this time (which can take quite a while for large or complex ontologies), but this behavior can be disabled as a part of the Reasoner configuration. Internal details of the loading and reasoning algorithms can be configured in the Reasoner constructor and do not change over the lifetime of the Reasoner object---internal data structures and caches are optimized for a particular configuration. By default, HermiT will use the set of options which provide optimal performance.
 */
public class Reasoner implements OWLReasoner,Serializable {
    private static final long serialVersionUID=-3511564272739622311L;

    protected final Configuration m_configuration;
    protected DLOntology m_dlOntology;
    protected Namespaces m_namespaces;
    protected Tableau m_tableau;
    protected TableauSubsumptionChecker m_subsumptionChecker;
    protected Hierarchy<AtomicConcept> m_atomicConceptHierarchy;
    protected Hierarchy<Role> m_atomicObjectRoleHierarchy;
    protected Hierarchy<AtomicRole> m_atomicDataRoleHierarchy;
    protected Map<AtomicConcept,Set<Individual>> m_realization;

    public Reasoner(Configuration configuration) {
        m_configuration=configuration;
        clearOntologies();
    }
    
    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManger,OWLOntology ontology) {
        this(configuration,ontologyManger,ontology,(Set<DescriptionGraph>)null,(Set<OWLHasKeyDummy>)null);
    }

    public Reasoner(Configuration configuration,OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) {
        m_configuration=configuration;
        loadOntology(ontologyManager,ontology,descriptionGraphs,keys);
    }
    
    public Reasoner(Configuration configuration,Set<OWLOntology> importClosure) {
        m_configuration=configuration;
        loadOntologies(importClosure);
    }

    public Reasoner(Configuration configuration,DLOntology dlOntology) {
        m_configuration=configuration;
        loadDLOntology(dlOntology);
    }

    // General accessor methods
    
    public Namespaces getNamespaces() {
        return m_namespaces;
    }

    public DLOntology getDLOntology() {
        return m_dlOntology;
    }

    public Configuration getConfiguration() {
        return m_configuration.clone();
    }

    // Loading and managing ontologies

    public void loadDLOntology(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_namespaces=createNamespaces(m_dlOntology);
        m_tableau=createTableau(m_configuration,m_dlOntology,m_namespaces);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
    }
    
    public void loadOntology(OWLOntologyManager ontologyManager,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        if (keys==null)
            keys=Collections.emptySet();
        OWLClausification clausifier=new OWLClausification(m_configuration);
        loadDLOntology(clausifier.clausifyWithKeys(ontologyManager,ontology,descriptionGraphs,keys));
    }

    public void loadOntologies(Set<OWLOntology> ontologies) {
        OWLClausification clausifier=new OWLClausification(m_configuration);
        Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
        Set<OWLHasKeyDummy> keys=Collections.emptySet();
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        loadDLOntology(clausifier.clausifyImportClosure(factory,"urn:hermit:kb",ontologies,descriptionGraphs,keys));
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
        Set<Role> noRoles=Collections.emptySet();
        Set<Individual> noIndividuals=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("urn:hermit:kb",noDLClauses,noAtoms,noAtoms,noAtomicConcepts,noRoles,noAtomicRoles,noAtomicRoles,noIndividuals,false,false,false,true,false);
        loadDLOntology(emptyDLOntology);
    }
    
    public void dispose() {
        clearOntologies();
    }
    
    // Checking the signature of the ontology
    
    public boolean isDefined(OWLClass owlClass) {
        AtomicConcept atomicConcept=AtomicConcept.create(owlClass.getURI().toString());
        return m_dlOntology.getAllAtomicConcepts().contains(atomicConcept) || AtomicConcept.THING.equals(atomicConcept) || AtomicConcept.NOTHING.equals(atomicConcept);
    }

    public boolean isDefined(OWLIndividual owlIndividual) {
        Individual individual=Individual.create(owlIndividual.getURI().toString());
        return m_dlOntology.getAllIndividuals().contains(individual);
    }

    public boolean isDefined(OWLObjectProperty owlObjectProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlObjectProperty.getURI().toString());
        return m_dlOntology.getAllAtomicObjectRoles().contains(atomicRole);
    }

    public boolean isDefined(OWLDataProperty owlDataProperty) {
        AtomicRole atomicRole=AtomicRole.create(owlDataProperty.getURI().toString());
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
            Set<AtomicConcept> relevantAtomicConcepts=new HashSet<AtomicConcept>();
            relevantAtomicConcepts.add(AtomicConcept.THING);
            relevantAtomicConcepts.add(AtomicConcept.NOTHING);
            for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                if (!Namespaces.isInternalURI(atomicConcept.getURI()))
                    relevantAtomicConcepts.add(atomicConcept);
            if (!m_subsumptionChecker.isSatisfiable(AtomicConcept.THING))
                m_atomicConceptHierarchy=Hierarchy.emptyHierarchy(relevantAtomicConcepts,AtomicConcept.THING,AtomicConcept.NOTHING);
            else if (m_subsumptionChecker.canGetAllSubsumersEasily()) {
                Map<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,DeterministicHierarchyBuilder.GraphNode<AtomicConcept>>();
                for (AtomicConcept atomicConcept : relevantAtomicConcepts) {
                    Set<AtomicConcept> subsumers=m_subsumptionChecker.getAllKnownSubsumers(atomicConcept);
                    if (subsumers==null)
                        subsumers=relevantAtomicConcepts;
                    allSubsumers.put(atomicConcept,new DeterministicHierarchyBuilder.GraphNode<AtomicConcept>(atomicConcept,subsumers));
                }
                DeterministicHierarchyBuilder<AtomicConcept> hierarchyBuilder=new DeterministicHierarchyBuilder<AtomicConcept>(allSubsumers,AtomicConcept.THING,AtomicConcept.NOTHING);
                m_atomicConceptHierarchy=hierarchyBuilder.buildHierarchyNew();
            }
            if (m_atomicConceptHierarchy==null) {
                HierarchyBuilder<AtomicConcept> hierarchyBuilder=new HierarchyBuilder<AtomicConcept>(
                    new HierarchyBuilder.Relation<AtomicConcept>() {
                        public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                            return m_subsumptionChecker.isSubsumedBy(child,parent);
                        }
                    }
                );
                m_atomicConceptHierarchy=hierarchyBuilder.buildHierarchy(AtomicConcept.THING,AtomicConcept.NOTHING,relevantAtomicConcepts);
            }
        }
    }
    
    public boolean isSatisfiable(OWLDescription description) {
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getURI().toString());
            if (m_atomicConceptHierarchy==null)
                return m_subsumptionChecker.isSatisfiable(concept);
            else {
                HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(concept);
                return node!=m_atomicConceptHierarchy.getBottomNode();
            }
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassAxiom(newClass,description);
            DLOntology newDLOntology=extendDLOntology(m_configuration,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_configuration,newDLOntology,m_namespaces);
            return tableau.isSatisfiable(AtomicConcept.create("internal:query-concept"));
        }
    }

    public boolean isSubClassOf(OWLDescription subDescription,OWLDescription superDescription) {
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass) {
            AtomicConcept subconcept=AtomicConcept.create(((OWLClass)subDescription).getURI().toString());
            AtomicConcept superconcept=AtomicConcept.create(((OWLClass)superDescription).getURI().toString());
            return m_subsumptionChecker.isSubsumedBy(subconcept,superconcept);
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newSubConcept=factory.getOWLClass(URI.create("internal:query-subconcept"));
            OWLAxiom subClassDefinitionAxiom=factory.getOWLSubClassAxiom(newSubConcept,subDescription);
            OWLClass newSuperConcept=factory.getOWLClass(URI.create("internal:query-superconcept"));
            OWLAxiom superClassDefinitionAxiom=factory.getOWLSubClassAxiom(superDescription,newSuperConcept);
            DLOntology newDLOntology=extendDLOntology(m_configuration,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,subClassDefinitionAxiom,superClassDefinitionAxiom);
            Tableau tableau=createTableau(m_configuration,newDLOntology,m_namespaces);
            return tableau.isSubsumedBy(AtomicConcept.create("internal:query-subconcept"),AtomicConcept.create("internal:query-superconcept"));
        }
    }

    public boolean isEquivalentClass(OWLDescription description1,OWLDescription description2) {
        return isSubClassOf(description1,description2) && isSubClassOf(description2,description1); 
    }
    
    public Set<OWLClass> getEquivalentClasses(OWLDescription description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptsToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLClass>> getSubClasses(OWLDescription description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getChildNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getSuperClasses(OWLDescription description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getParentNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getAncestorClasses(OWLDescription description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getAncestorNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<Set<OWLClass>> getDescendantClasses(OWLDescription description) {
        HierarchyNode<AtomicConcept> node=getHierarchyNode(description);
        return atomicConceptNodesToOWLAPI(node.getDescendantNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<OWLClass> getInconsistentClasses() {
        classify();
        HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getBottomNode();
        return atomicConceptsToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    protected HierarchyNode<AtomicConcept> getHierarchyNode(OWLDescription description) {
        classify();
        if (description instanceof OWLClass) {
            AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getURI().toString());
            HierarchyNode<AtomicConcept> node=m_atomicConceptHierarchy.getNodeForElement(atomicConcept);
            if (node==null)
                node=new HierarchyNode<AtomicConcept>(atomicConcept,Collections.singleton(m_atomicConceptHierarchy.getTopNode()),Collections.singleton(m_atomicConceptHierarchy.getBottomNode()));
            return node;
        }
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(newClass,description);
            DLOntology newDLOntology=extendDLOntology(m_configuration,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_configuration,newDLOntology,m_namespaces);
            final TableauSubsumptionChecker subsumptionChecker=new TableauSubsumptionChecker(tableau);
            HierarchyBuilder<AtomicConcept> hierarchyBuilder=new HierarchyBuilder<AtomicConcept>(new HierarchyBuilder.Relation<AtomicConcept>() {
                public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                    return subsumptionChecker.isSubsumedBy(child,parent);
                }
            });
            return hierarchyBuilder.findPosition(AtomicConcept.create("internal:query-concept"),m_atomicConceptHierarchy.getTopNode(),m_atomicConceptHierarchy.getBottomNode());
        }
    }

    public void printClassHierarchy(PrintWriter output) {
        classify();
        Map<String,Set<String>> flat=new TreeMap<String,Set<String>>();
        for (HierarchyNode<AtomicConcept> node : m_atomicConceptHierarchy.getAllNodes()) {
            Set<String> ancestors=new TreeSet<String>();
            for (AtomicConcept atomicConcept : node.getEquivalentElements())
                if (!AtomicConcept.THING.equals(atomicConcept))
                    ancestors.add(atomicConcept.getURI());
            for (HierarchyNode<AtomicConcept> ancestorNode : node.getAncestorNodes())
                for (AtomicConcept atomicConcept : ancestorNode.getEquivalentElements())
                    if (!AtomicConcept.THING.equals(atomicConcept))
                        ancestors.add(atomicConcept.getURI());
            for (AtomicConcept atomicConcept : node.getEquivalentElements())
                if (!AtomicConcept.NOTHING.equals(atomicConcept))
                    flat.put(atomicConcept.getURI(),ancestors);
        }
        try {
            for (Map.Entry<String,Set<String>> entry : flat.entrySet()) {
                output.println(entry.getKey());
                for (String ancestor : entry.getValue()) {
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

    // Object property inferences
    
    public boolean areObjectPropertiesClassified() {
        return m_atomicObjectRoleHierarchy!=null;
    }

    public void classifyObjectProperties() {
        if (m_atomicObjectRoleHierarchy==null) {
            Map<Role,DeterministicHierarchyBuilder.GraphNode<Role>> allSubsumers=new HashMap<Role,DeterministicHierarchyBuilder.GraphNode<Role>>();
            addInclusion(allSubsumers,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.TOP_OBJECT_ROLE);
            addInclusion(allSubsumers,AtomicRole.BOTTOM_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE);
            for (DLClause dlClause : m_dlOntology.getDLClauses()) {
                if (dlClause.isRoleInclusion()) {
                    AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                    AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                    if (m_dlOntology.getAllAtomicObjectRoles().contains(sub) && m_dlOntology.getAllAtomicObjectRoles().contains(sup)) {
                        addInclusion(allSubsumers,sub,sup);
                        addInclusion(allSubsumers,sub.getInverse(),sup.getInverse());
                    }
                }
                else if (dlClause.isRoleInverseInclusion()) {
                    AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                    AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                    if (m_dlOntology.getAllAtomicObjectRoles().contains(sub) && m_dlOntology.getAllAtomicObjectRoles().contains(sup)) {
                        addInclusion(allSubsumers,sub.getInverse(),sup);
                        addInclusion(allSubsumers,sub,sup.getInverse());
                    }
                }
            }
            Set<Role> allRoles=new HashSet<Role>();
            for (AtomicRole atomicRole : m_dlOntology.getAllAtomicObjectRoles()) {
                allRoles.add(atomicRole);
                allRoles.add(atomicRole.getInverse());
                addInclusion(allSubsumers,atomicRole,AtomicRole.TOP_OBJECT_ROLE);
                addInclusion(allSubsumers,atomicRole.getInverse(),AtomicRole.TOP_OBJECT_ROLE);
                addInclusion(allSubsumers,AtomicRole.BOTTOM_OBJECT_ROLE,atomicRole);
                addInclusion(allSubsumers,AtomicRole.BOTTOM_OBJECT_ROLE,atomicRole.getInverse());
            }
            DeterministicHierarchyBuilder<Role> hierarchyBuilder=new DeterministicHierarchyBuilder<Role>(allSubsumers,AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE);
            m_atomicObjectRoleHierarchy=hierarchyBuilder.buildHierarchyNew();
        }
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
        return objectPropertyNodesToOWLAPI(node.getAncestorNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLObjectPropertyExpression>> getDescendantProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertyNodesToOWLAPI(node.getDescendantNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<OWLObjectPropertyExpression> getEquivalentProperties(OWLObjectPropertyExpression propertyExpression) {
        HierarchyNode<Role> node=getHierarchyNode(propertyExpression);
        return objectPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    protected HierarchyNode<Role> getHierarchyNode(OWLObjectPropertyExpression propertyExpression) {
        propertyExpression=propertyExpression.getSimplified();
        Role role;
        if (propertyExpression instanceof OWLObjectProperty)
            role=AtomicRole.create(((OWLObjectProperty)propertyExpression).getURI().toString());
        else {
            OWLObjectPropertyInverse inverse=(OWLObjectPropertyInverse)propertyExpression;
            role=AtomicRole.create(((OWLObjectProperty)inverse).getURI().toString());
        }
        classifyObjectProperties();
        HierarchyNode<Role> node=m_atomicObjectRoleHierarchy.getNodeForElement(role);
        if (node==null)
            node=new HierarchyNode<Role>(role,Collections.singleton(m_atomicObjectRoleHierarchy.getTopNode()),Collections.singleton(m_atomicObjectRoleHierarchy.getBottomNode()));
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
    public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLObjectSomeRestriction(property,factory.getOWLThing()));
        return (Set<Set<OWLDescription>>)object;
    }

    public Set<OWLDescription> getRanges(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLDescription> result=new HashSet<OWLDescription>();
        Set<Set<OWLClass>> ranges=getAncestorClasses(factory.getOWLObjectSomeRestriction(property.getInverseProperty(),factory.getOWLThing()));
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
        return !isSatisfiable(factory.getOWLObjectMinCardinalityRestriction(property,2));
    }

    public boolean isInverseFunctional(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectMinCardinalityRestriction(property.getInverseProperty(),2));
    }

    public boolean isIrreflexive(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectSelfRestriction(property));
    }

    public boolean isReflexive(OWLObjectProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLObjectComplementOf(factory.getOWLObjectSelfRestriction(property)));
    }

    public boolean isAsymmetric(OWLObjectProperty property) {
        AtomicRole atomicRole=AtomicRole.create(property.getURI().toString());
        return m_tableau.isAsymmetric(atomicRole);
    }

    @Deprecated
    public boolean isAntiSymmetric(OWLObjectProperty property) {
        return isAsymmetric(property);
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
            Map<AtomicRole,DeterministicHierarchyBuilder.GraphNode<AtomicRole>> allSubsumers=new HashMap<AtomicRole,DeterministicHierarchyBuilder.GraphNode<AtomicRole>>();
            addInclusion(allSubsumers,AtomicRole.TOP_DATA_ROLE,AtomicRole.TOP_DATA_ROLE);
            addInclusion(allSubsumers,AtomicRole.BOTTOM_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE);
            for (DLClause dlClause : m_dlOntology.getDLClauses()) {
                if (dlClause.isRoleInclusion()) {
                    AtomicRole sub=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                    AtomicRole sup=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                    if (m_dlOntology.getAllAtomicDataRoles().contains(sub) && m_dlOntology.getAllAtomicDataRoles().contains(sup))
                        addInclusion(allSubsumers,sub,sup);
                }
            }
            for (AtomicRole atomicRole : m_dlOntology.getAllAtomicDataRoles()) {
                addInclusion(allSubsumers,atomicRole,AtomicRole.TOP_DATA_ROLE);
                addInclusion(allSubsumers,AtomicRole.BOTTOM_DATA_ROLE,atomicRole);
            }
            DeterministicHierarchyBuilder<AtomicRole> hierarchyBuilder=new DeterministicHierarchyBuilder<AtomicRole>(allSubsumers,AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE);
            m_atomicDataRoleHierarchy=hierarchyBuilder.buildHierarchyNew();
        }
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
        return dataPropertyNodesToOWLAPI(node.getAncestorNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }

    public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        return dataPropertyNodesToOWLAPI(node.getDescendantNodes(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) {
        HierarchyNode<AtomicRole> node=getHierarchyNode(property);
        return dataPropertiesToOWLAPI(node.getEquivalentElements(),OWLManager.createOWLOntologyManager().getOWLDataFactory());
    }
    
    protected HierarchyNode<AtomicRole> getHierarchyNode(OWLDataProperty property) {
        AtomicRole atomicRole=AtomicRole.create(property.getURI().toString());
        classifyDataProperties();
        HierarchyNode<AtomicRole> node=m_atomicDataRoleHierarchy.getNodeForElement(atomicRole);
        if (node==null)
            node=new HierarchyNode<AtomicRole>(atomicRole,Collections.singleton(m_atomicDataRoleHierarchy.getTopNode()),Collections.singleton(m_atomicDataRoleHierarchy.getBottomNode()));
        return node;
    }
    
    @SuppressWarnings("unchecked")
    public Set<Set<OWLDescription>> getDomains(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Object object=getAncestorClasses(factory.getOWLDataSomeRestriction(property,factory.getTopDataType()));
        return (Set<Set<OWLDescription>>)object;
    }

    public Set<OWLDataRange> getRanges(OWLDataProperty property) {
        throw new UnsupportedOperationException();
    }

    public boolean isFunctional(OWLDataProperty property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return !isSatisfiable(factory.getOWLDataMinCardinalityRestriction(property,2));
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
            public Set<HierarchyNode<AtomicConcept>> getAncestorElements(HierarchyNode<AtomicConcept> u) {
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
        Set<HierarchyNode<AtomicConcept>> directSuperConceptNodes=getDirectSuperConceptNodes(Individual.create(individual.getURI().toString()));
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<Set<OWLClass>> result=atomicConceptNodesToOWLAPI(directSuperConceptNodes,factory);
        if (!direct)
            for (HierarchyNode<AtomicConcept> directSuperConceptNode : directSuperConceptNodes)
                result.addAll(atomicConceptNodesToOWLAPI(directSuperConceptNode.getAncestorNodes(),factory));
        return result;
    }

    public boolean hasType(OWLIndividual owlIndividual,OWLDescription type,boolean direct) {
        if (direct || isRealised())
            return getIndividuals(type,direct).contains(owlIndividual);
        else {
            Individual individual=Individual.create(owlIndividual.getURI().toString());
            if (type instanceof OWLClass) {
                AtomicConcept concept=AtomicConcept.create(((OWLClass)type).getURI().toString());
                return m_tableau.isInstanceOf(concept,individual);
            }
            else {
                OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
                OWLDataFactory factory=ontologyManager.getOWLDataFactory();
                OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
                OWLAxiom classDefinitionAxiom=factory.getOWLSubClassAxiom(type,newClass);
                DLOntology newDLOntology=extendDLOntology(m_configuration,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
                Tableau tableau=createTableau(m_configuration,newDLOntology,m_namespaces);
                return tableau.isInstanceOf(AtomicConcept.create("internal:query-concept"),individual);
            }
        }
    }

    public Set<OWLIndividual> getIndividuals(OWLDescription description,boolean direct) {
        realise();
        if (description instanceof OWLClass) {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getURI().toString());
            OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
            Set<OWLIndividual> result=new HashSet<OWLIndividual>();
            Set<Individual> instances=m_realization.get(concept);
            if (instances!=null)
                for (Individual instance : instances)
                    result.add(factory.getOWLIndividual(URI.create(instance.getURI())));
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
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassAxiom(description,newClass);
            DLOntology newDLOntology=extendDLOntology(m_configuration,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_configuration,newDLOntology,m_namespaces);
            AtomicConcept queryConcept=AtomicConcept.create("internal:query-concept");
            HierarchyNode<AtomicConcept> hierarchyNode=getHierarchyNode(description);
            Set<OWLIndividual> result=new HashSet<OWLIndividual>();
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
                            result.add(factory.getOWLIndividual(URI.create(individual.getURI())));
            }
            return result;
        }
    }

    protected void loadIndividualsOfNode(HierarchyNode<AtomicConcept> node,Set<OWLIndividual> result,OWLDataFactory factory) {
        AtomicConcept atomicConcept=node.getEquivalentElements().iterator().next();
        Set<Individual> realizationForConcept=m_realization.get(atomicConcept);
        // RealizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null)
            for (Individual individual : realizationForConcept)
                result.add(factory.getOWLIndividual(URI.create(individual.getURI())));
    }
    
    public Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
        throw new UnsupportedOperationException();
    }
    
    public Map<OWLDataProperty,Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
        throw new UnsupportedOperationException();
    }

    public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,OWLObjectPropertyExpression property) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return getIndividuals(factory.getOWLObjectSomeRestriction(property.getInverseProperty(),factory.getOWLObjectOneOf(subject)),false);
    }

    public Set<OWLConstant> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
        throw new UnsupportedOperationException();
    }

    public boolean hasObjectPropertyRelationship(OWLIndividual subject,OWLObjectPropertyExpression property,OWLIndividual object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLObjectSomeRestriction(property,factory.getOWLObjectOneOf(object)),false);
    }

    public boolean hasDataPropertyRelationship(OWLIndividual subject,OWLDataPropertyExpression property,OWLConstant object) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return hasType(subject,factory.getOWLDataValueRestriction(property,object),false);
    }

    // Various creation methods
    
    protected static Tableau createTableau(Configuration config,DLOntology dlOntology,Namespaces namespaces) throws IllegalArgumentException {
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
                    buffer.append(dlClause.toString(namespaces));
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
            wellKnownTableauMonitor=new Debugger(namespaces,true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor=new Debugger(namespaces,false);
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
        case EL:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,true);
            break;
        case INDIVIDUAL_REUSE:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
            break;
        default:
            throw new IllegalArgumentException("Unknown expansion strategy type.");
        }

        return new Tableau(tableauMonitor,existentialsExpansionStrategy,dlOntology,config.parameters);
    }

    protected static DLOntology extendDLOntology(Configuration config,Namespaces namespaces,String resultingOntologyURI,DLOntology originalDLOntology,OWLOntologyManager ontologyManager,OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        try {
            Set<DescriptionGraph> descriptionGraphs=Collections.emptySet();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLOntology newOntology=ontologyManager.createOntology(URI.create("uri:urn:internal-kb"));
            for (OWLAxiom axiom : additionalAxioms)
                ontologyManager.addAxiom(newOntology,axiom);
            OWLAxioms axioms=new OWLAxioms();
            OWLNormalization normalization=new OWLNormalization(factory,axioms);
            normalization.processOntology(config,newOntology);
            if (!originalDLOntology.getAllAtomicObjectRoles().contains(AtomicRole.TOP_OBJECT_ROLE)) {
                BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);   
                builtInPropertyManager.axiomatizeTopObjectPropertyIfNeeded(axioms);
            }
            if (!originalDLOntology.getAllTransitiveObjectRoles().isEmpty() || !axioms.m_transitiveObjectProperties.isEmpty()) {
                TransitivityManager transitivityManager=new TransitivityManager(factory);
                transitivityManager.prepareTransformation(axioms);
                for (Role transitiveRole : originalDLOntology.getAllTransitiveObjectRoles()) {
                    OWLObjectPropertyExpression objectPropertyExpression=getObjectPropertyExpression(factory,transitiveRole);
                    transitivityManager.makeTransitive(objectPropertyExpression);
                }
                for (DLClause dlClause : originalDLOntology.getDLClauses()) {
                    if (dlClause.isRoleInclusion()) {
                        AtomicRole subAtomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                        AtomicRole superAtomicRole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                        if (originalDLOntology.getAllAtomicObjectRoles().contains(subAtomicRole) && originalDLOntology.getAllAtomicObjectRoles().contains(superAtomicRole)) {
                            OWLObjectProperty subObjectProperty=getObjectProperty(factory,subAtomicRole);
                            OWLObjectProperty superObjectProperty=getObjectProperty(factory,superAtomicRole);
                            transitivityManager.addInclusion(subObjectProperty,superObjectProperty);
                        }
                    }
                    else if (dlClause.isRoleInverseInclusion()) {
                        AtomicRole subAtomicRole=(AtomicRole)dlClause.getBodyAtom(0).getDLPredicate();
                        AtomicRole superAtomicRole=(AtomicRole)dlClause.getHeadAtom(0).getDLPredicate();
                        if (originalDLOntology.getAllAtomicObjectRoles().contains(subAtomicRole) && originalDLOntology.getAllAtomicObjectRoles().contains(superAtomicRole)) {
                            OWLObjectProperty subObjectProperty=getObjectProperty(factory,subAtomicRole);
                            OWLObjectPropertyExpression superObjectPropertyExpression=getObjectProperty(factory,superAtomicRole).getInverseProperty();
                            transitivityManager.addInclusion(subObjectProperty,superObjectPropertyExpression);
                        }
                    }
                }
                transitivityManager.rewriteConceptInclusions(axioms);
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
            Set<Role> transitiveObjectRoles=createUnion(originalDLOntology.getAllTransitiveObjectRoles(),newDLOntology.getAllTransitiveObjectRoles());
            Set<AtomicRole> atomicObjectRoles=createUnion(originalDLOntology.getAllAtomicObjectRoles(),newDLOntology.getAllAtomicObjectRoles());
            Set<AtomicRole> atomicDataRoles=createUnion(originalDLOntology.getAllAtomicDataRoles(),newDLOntology.getAllAtomicDataRoles());
            Set<Individual> individuals=createUnion(originalDLOntology.getAllIndividuals(),newDLOntology.getAllIndividuals());
            boolean hasInverseRoles=originalDLOntology.hasInverseRoles() || newDLOntology.hasInverseRoles();
            boolean hasAtMostRestrictions=originalDLOntology.hasAtMostRestrictions() || newDLOntology.hasAtMostRestrictions();
            boolean hasNominals=originalDLOntology.hasNominals() || newDLOntology.hasNominals();
            boolean canUseNIRule=originalDLOntology.canUseNIRule() || newDLOntology.canUseNIRule();
            boolean hasDatatypes=originalDLOntology.hasDatatypes() || newDLOntology.hasDatatypes();
            return new DLOntology(resultingOntologyURI,dlClauses,positiveFacts,negativeFacts,atomicConcepts,transitiveObjectRoles,atomicObjectRoles,atomicDataRoles,individuals,hasInverseRoles,hasAtMostRestrictions,hasNominals,canUseNIRule,hasDatatypes);
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
        return factory.getOWLObjectProperty(URI.create(atomicRole.getURI()));
    }
    
    protected static OWLObjectPropertyExpression getObjectPropertyExpression(OWLDataFactory factory,Role role) {
        if (role instanceof AtomicRole)
            return factory.getOWLObjectProperty(URI.create(((AtomicRole)role).getURI()));
        else {
            AtomicRole inverseOf=((InverseRole)role).getInverseOf();
            return factory.getOWLObjectProperty(URI.create(inverseOf.getURI())).getInverseProperty();
        }
    }
    
    protected static Namespaces createNamespaces(DLOntology dlOntology) {
        Set<String> namespaceURIs=new HashSet<String>();
        for (AtomicConcept concept : dlOntology.getAllAtomicConcepts())
            addURI(concept.getURI(),namespaceURIs);
        for (AtomicRole atomicRole : dlOntology.getAllAtomicDataRoles())
            addURI(atomicRole.getURI(),namespaceURIs);
        for (AtomicRole atomicRole : dlOntology.getAllAtomicObjectRoles())
            addURI(atomicRole.getURI(),namespaceURIs);
        for (Individual individual : dlOntology.getAllIndividuals())
            addURI(individual.getURI(),namespaceURIs);
        Namespaces namespaces=new Namespaces();
        namespaces.reegisterSemanticWebPrefixes();
        namespaces.registerInternalNamespaces(namespaceURIs);
        namespaces.registerDefaultNamespace(dlOntology.getOntologyURI()+"#");
        int prefixIndex=0;
        for (String namespace : namespaceURIs)
            if (!namespaces.isNamespaceRegistered(namespace)) {
                String prefix=getPrefixForIndex(prefixIndex);
                while (namespaces.isPrefixRegistered(prefix))
                    prefix=getPrefixForIndex(++prefixIndex);
                namespaces.registerNamespace(prefix,namespace);
                ++prefixIndex;
            }
        return namespaces;
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
    protected static void addURI(String uri,Set<String> namespaceURIs) {
        if (!Namespaces.isInternalURI(uri)) {
            int lastHash=uri.lastIndexOf('#');
            if (lastHash!=-1) {
                String namespaceURI=uri.substring(0,lastHash+1);
                namespaceURIs.add(namespaceURI);
            }
        }
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

    public static Reasoner loadRasoner(File file) throws IOException {
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
            result.add(factory.getOWLClass(URI.create(concept.getURI())));
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
            result.add(factory.getOWLDataProperty(URI.create(atomicRole.getURI())));
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
    
    // The factory for the reasoner from a plugin

    public static class ReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
        
        @SuppressWarnings("serial")
        public OWLReasoner createReasoner(OWLOntologyManager ontologyManager) {
            Configuration configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
            return new Reasoner(configuration) {
                protected Set<OWLOntology> m_loadedOntologies;
                
                public void loadOntologies(Set<OWLOntology> ontologies) {
                    super.loadOntologies(ontologies);
                    m_loadedOntologies=ontologies;
                }
                public Set<OWLOntology> getLoadedOntologies() {
                    return m_loadedOntologies;
                }
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
                public Map<OWLDataProperty,Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) {
                    return new HashMap<OWLDataProperty,Set<OWLConstant>>();
                }
                public Set<OWLConstant> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
                    return new HashSet<OWLConstant>();
                }
            };
        }
        public void initialise() {
        }
        public void dispose() {
        }
        public boolean requiresExplicitClassification() {
            return false;
        }
    }
}
