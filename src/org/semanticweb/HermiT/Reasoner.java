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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.semanticweb.HermiT.hierarchy.Classifier;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.hierarchy.NaiveHierarchyPosition;
import org.semanticweb.HermiT.hierarchy.PositionTranslator;
import org.semanticweb.HermiT.hierarchy.TableauFunc;
import org.semanticweb.HermiT.hierarchy.TableauSubsumptionChecker;
import org.semanticweb.HermiT.hierarchy.TranslatedHierarchyPosition;
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
import org.semanticweb.HermiT.owlapi.structural.BuiltInPropertyManager;
import org.semanticweb.HermiT.owlapi.structural.OWLAxioms;
import org.semanticweb.HermiT.owlapi.structural.OWLAxiomsExpressivity;
import org.semanticweb.HermiT.owlapi.structural.OWLClausification;
import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.owlapi.structural.OWLNormalization;
import org.semanticweb.HermiT.owlapi.structural.TransitivityManager;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.HermiT.util.GraphUtils;
import org.semanticweb.HermiT.util.TranslatedMap;
import org.semanticweb.HermiT.util.Translator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

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
    protected Map<AtomicConcept,HierarchyPosition<AtomicConcept>> m_atomicConceptHierarchy;
    protected Map<AtomicRole,HierarchyPosition<AtomicRole>> m_atomicRoleHierarchy;
    protected Map<AtomicConcept,Set<Individual>> m_realization;

    public Reasoner(String ontologyURI) throws IllegalArgumentException,OWLException {
        this(new Configuration(),URI.create(ontologyURI));
    }

    public Reasoner(java.net.URI ontologyURI) throws IllegalArgumentException,OWLException {
        this(new Configuration(),ontologyURI);
    }

    public Reasoner(Configuration config,java.net.URI ontologyURI) throws IllegalArgumentException,OWLException {
        this(config,ontologyURI,(Set<DescriptionGraph>)null,(Set<OWLHasKeyDummy>)null);
    }

    public Reasoner(Configuration config,java.net.URI ontologyURI,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) throws IllegalArgumentException,OWLException {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        if (keys==null)
            keys=Collections.emptySet();
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLOntology ontology=ontologyManager.loadOntologyFromPhysicalURI(ontologyURI);
        OWLClausification clausifier=new OWLClausification(config);
        m_dlOntology=clausifier.clausifyWithKeys(ontologyManager,ontology,descriptionGraphs,keys);
        m_config=config;
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology,m_namespaces);
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
        m_tableau=createTableau(m_config,m_dlOntology,m_namespaces);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }
    
    /**
     * Creates a reasoner that contains all axioms from the ontologies in the 'ontologies'' parameter.
     * If any ontology in this collection contains imports, these are *NOT* traversed -- that is,
     * the resulting ontology contains *EXACTLY* the axioms explciitly present in the supplied ontologies.
     * The resulting DL ontology has the URI ontologyURI.
     */
    public Reasoner(Configuration config,OWLOntologyManager ontologyManger,Collection<OWLOntology> importClosure,String ontologyURI) {
        OWLClausification clausifier=new OWLClausification(config);
        Set<OWLHasKeyDummy> keys=Collections.emptySet();
        Set<DescriptionGraph> dgs = Collections.emptySet();
        m_config=config;
        m_dlOntology=clausifier.clausifyImportClosure(ontologyManger.getOWLDataFactory(),ontologyURI,importClosure,dgs,keys);
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology,m_namespaces);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }

    public Reasoner(Configuration config,DLOntology dlOntology) {
        m_config=config;
        m_dlOntology=dlOntology;
        m_namespaces=createNamespaces(m_dlOntology.getOntologyURI());
        m_tableau=createTableau(m_config,m_dlOntology,m_namespaces);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
        m_classifier=new Classifier<AtomicConcept>(new TableauFunc(m_subsumptionChecker));
    }

    // General accessor methods
    
    public Namespaces getNamespaces() {
        return m_namespaces;
    }

    public DLOntology getDLOntology() {
        return m_dlOntology;
    }

    /**
     * Return `true` iff `classUri` occurred in the loaded knowledge base.
     */
    public boolean isClassNameDefined(String classUri) {
        return m_dlOntology.getAllAtomicConcepts().contains(AtomicConcept.create(classUri)) || classUri.equals(AtomicConcept.THING.getURI()) || classUri.equals(AtomicConcept.NOTHING.getURI());
    }

    // General inferences
    
    public boolean isConsistent() {
        return m_tableau.isABoxSatisfiable();
    }

    // Concept inferences
    
    public boolean isClassSatisfiable(String classURI) {
        return m_subsumptionChecker.isSatisfiable(AtomicConcept.create(classURI));
    }

    public boolean isClassSatisfiable(OWLDescription description) {
        if (description instanceof OWLClass)
            return isClassSatisfiable(((OWLClass)description).getURI().toString());
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassAxiom(newClass,description);
            DLOntology newDLOntology=extendDLOntology(m_config,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_config,newDLOntology,m_namespaces);
            return tableau.isSatisfiable(AtomicConcept.create("internal:query-concept"));
        }
    }

    public boolean isClassSubsumedBy(String childName,String parentName) {
        return m_subsumptionChecker.isSubsumedBy(AtomicConcept.create(childName),AtomicConcept.create(parentName));
    }

    public boolean isClassSubsumedBy(OWLDescription subDescription,OWLDescription superDescription) {
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass)
            return isClassSubsumedBy(((OWLClass)subDescription).getURI().toString(),((OWLClass)superDescription).getURI().toString());
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newSubConcept=factory.getOWLClass(URI.create("internal:query-subconcept"));
            OWLAxiom subClassDefinitionAxiom=factory.getOWLSubClassAxiom(newSubConcept,subDescription);
            OWLClass newSuperConcept=factory.getOWLClass(URI.create("internal:query-superconcept"));
            OWLAxiom superClassDefinitionAxiom=factory.getOWLSubClassAxiom(superDescription,newSuperConcept);
            DLOntology newDLOntology=extendDLOntology(m_config,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,subClassDefinitionAxiom,superClassDefinitionAxiom);
            Tableau tableau=createTableau(m_config,newDLOntology,m_namespaces);
            return tableau.isSubsumedBy(AtomicConcept.create("internal:query-subconcept"),AtomicConcept.create("internal:query-superconcept"));
        }
    }

    // Concept hierarchy

    public void computeClassHierarchy() {
        getClassHierarchy();
    }

    public boolean isClassHierarchyComputed() {
        return m_atomicConceptHierarchy!=null;
    }

    public Map<String,HierarchyPosition<String>> getClassHierarchy() {
        return new TranslatedMap<AtomicConcept,String,HierarchyPosition<AtomicConcept>,HierarchyPosition<String>>(getAtomicConceptHierarchy(),new ConceptToString(),new StringToConcept(),new PositionTranslator<AtomicConcept,String>(new ConceptToString()));
    }

    public HierarchyPosition<String> getClassHierarchyPosition(String className) throws IllegalArgumentException{
        if (!isClassNameDefined(className))
            throw new IllegalArgumentException("Class '"+className+"' does not occur in the loaded ontology.");
        return getClassHierarchy().get(className);
    }

    public HierarchyPosition<OWLClass> getClassHierarchyPosition(OWLDescription description) {
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        HierarchyPosition<AtomicConcept> hierarchyPosition;
        if (description instanceof OWLClass) {
            AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getURI().toString());
            hierarchyPosition=getConceptHierarchyPosition(atomicConcept);
        }
        else {
            Map<AtomicConcept,HierarchyPosition<AtomicConcept>> atomicConceptHierarchy=getAtomicConceptHierarchy();
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLEquivalentClassesAxiom(newClass,description);
            DLOntology newDLOntology=extendDLOntology(m_config,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_config,newDLOntology,m_namespaces);
            Classifier<AtomicConcept> classifier=new Classifier<AtomicConcept>(new TableauFunc(new TableauSubsumptionChecker(tableau)));
            hierarchyPosition=classifier.findPosition(AtomicConcept.create("internal:query-concept"),atomicConceptHierarchy.get(AtomicConcept.THING),atomicConceptHierarchy.get(AtomicConcept.NOTHING));
        }
        return new TranslatedHierarchyPosition<AtomicConcept,OWLClass>(hierarchyPosition,new ConceptToOWLClass(factory));
    }

    protected Map<AtomicConcept,HierarchyPosition<AtomicConcept>> getAtomicConceptHierarchy() {
        if (m_atomicConceptHierarchy==null) {
            Collection<AtomicConcept> concepts=new ArrayList<AtomicConcept>();
            concepts.add(AtomicConcept.THING);
            concepts.add(AtomicConcept.NOTHING);
            for (AtomicConcept c : m_dlOntology.getAllAtomicConcepts())
                if (!Namespaces.isInternalURI(c.getURI()))
                    concepts.add(c);
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
                for (AtomicConcept c : concepts) {
                    AtomicConcept canonicalName=acyc.canonical.get(c);
                    NaiveHierarchyPosition<AtomicConcept> pos=(NaiveHierarchyPosition<AtomicConcept>)m_atomicConceptHierarchy.get(canonicalName);
                    if (pos==null) {
                        pos=new NaiveHierarchyPosition<AtomicConcept>(canonicalName);
                        m_atomicConceptHierarchy.put(canonicalName,pos);
                    }
                    pos.labels.add(c);
                    m_atomicConceptHierarchy.put(c,pos);
                }
                if (!m_atomicConceptHierarchy.containsKey(AtomicConcept.THING))
                    m_atomicConceptHierarchy.put(AtomicConcept.THING,new NaiveHierarchyPosition<AtomicConcept>(AtomicConcept.THING));
                for (Map.Entry<AtomicConcept,Set<AtomicConcept>> e : trans.reduced.entrySet()) {
                    AtomicConcept child=e.getKey();
                    for (AtomicConcept parent : e.getValue()) {
                        ((NaiveHierarchyPosition<AtomicConcept>)m_atomicConceptHierarchy.get(child)).parents.add(m_atomicConceptHierarchy.get(parent));
                        ((NaiveHierarchyPosition<AtomicConcept>)m_atomicConceptHierarchy.get(parent)).children.add(m_atomicConceptHierarchy.get(child));
                    }
                }
            }
            else
                m_atomicConceptHierarchy=m_classifier.buildHierarchy(AtomicConcept.THING,AtomicConcept.NOTHING,concepts);
        }
        return m_atomicConceptHierarchy;
    }

    protected HierarchyPosition<AtomicConcept> getConceptHierarchyPosition(AtomicConcept atomicConcept) {
        HierarchyPosition<AtomicConcept> result=getAtomicConceptHierarchy().get(atomicConcept);
        if (result==null)
            result=m_classifier.findPosition(atomicConcept,getAtomicConceptHierarchy().get(AtomicConcept.THING),getAtomicConceptHierarchy().get(AtomicConcept.NOTHING));
        return result;
    }

    public void printSortedAncestorLists(PrintWriter output) {
        printSortedAncestorLists(output,getClassHierarchy());
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

    // Object property inferences
    
    public boolean isAsymmetric(OWLObjectProperty p) {
        return m_tableau.isAsymmetric(AtomicRole.createAtomicRole(p.getURI().toString()));
    }

    // Property hierarchy
    
    public void computePropertyHierarchy() {
        getAtomicRoleHierarchy();
    }

    public boolean isPropertyHierarchyComputed() {
        return m_atomicRoleHierarchy!=null;
    }

    public HierarchyPosition<String> getPropertyHierarchyPosition(String propertyURI) {
        AtomicRole atomicRole=AtomicRole.createAtomicRole(propertyURI);
        if (m_dlOntology.getAllAtomicDataRoles().contains(atomicRole))
            return new TranslatedHierarchyPosition<AtomicRole,String>(getAtomicRoleHierarchyPosition(AtomicRole.createAtomicRole(propertyURI),AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE),new RoleToString());
        else
            return new TranslatedHierarchyPosition<AtomicRole,String>(getAtomicRoleHierarchyPosition(AtomicRole.createAtomicRole(propertyURI),AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE),new RoleToString());
    }
    
    public HierarchyPosition<OWLObjectProperty> getPropertyHierarchyPosition(OWLObjectProperty p) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicRole,OWLObjectProperty>(getAtomicRoleHierarchyPosition(AtomicRole.createAtomicRole(p.getURI().toString()),AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE),new RoleToOWLObjectProperty(factory));
    }

    public HierarchyPosition<OWLDataProperty> getPropertyHierarchyPosition(OWLDataProperty p) {
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        return new TranslatedHierarchyPosition<AtomicRole,OWLDataProperty>(getAtomicRoleHierarchyPosition(AtomicRole.createAtomicRole(p.getURI().toString()),AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE),new RoleToOWLDataProperty(factory));
    }

    protected Map<AtomicRole,HierarchyPosition<AtomicRole>> getAtomicRoleHierarchy() {
        if (m_atomicRoleHierarchy==null) {
            final Map<Role,Set<Role>> subRoles=new HashMap<Role,Set<Role>>();
            for (DLClause dlClause : m_dlOntology.getDLClauses()) {
                if (dlClause.isRoleInclusion()) {
                    Role sub=(Role)dlClause.getBodyAtom(0).getDLPredicate();
                    Role sup=(Role)dlClause.getHeadAtom(0).getDLPredicate();
                    addInclusion(subRoles,sub,sup);
                    addInclusion(subRoles,sub.getInverse(),sup.getInverse());
                }
                else if (dlClause.isRoleInverseInclusion()) {
                    Role sub=(Role)dlClause.getBodyAtom(0).getDLPredicate();
                    Role sup=((Role)dlClause.getHeadAtom(0).getDLPredicate()).getInverse();
                    addInclusion(subRoles,sub,sup);
                    addInclusion(subRoles,sub.getInverse(),sup.getInverse());
                }
            }
            GraphUtils.transitivelyClose(subRoles);
            NaiveHierarchyPosition.Ordering<AtomicRole> ordering=new NaiveHierarchyPosition.Ordering<AtomicRole>() {
                public boolean less(AtomicRole sub,AtomicRole sup) {
                    if (AtomicRole.TOP_DATA_ROLE.equals(sup) || AtomicRole.TOP_OBJECT_ROLE.equals(sup) || AtomicRole.BOTTOM_DATA_ROLE.equals(sub) || AtomicRole.BOTTOM_OBJECT_ROLE.equals(sub))
                        return true;
                    Set<Role> subs=subRoles.get(sup);
                    if (subs==null)
                        return false;
                    return subs.contains(sub);
                }
            };
            m_atomicRoleHierarchy=NaiveHierarchyPosition.buildHierarchy(AtomicRole.TOP_OBJECT_ROLE,AtomicRole.BOTTOM_OBJECT_ROLE,m_dlOntology.getAllAtomicObjectRoles(),ordering);
            m_atomicRoleHierarchy.putAll(NaiveHierarchyPosition.buildHierarchy(AtomicRole.TOP_DATA_ROLE,AtomicRole.BOTTOM_DATA_ROLE,m_dlOntology.getAllAtomicDataRoles(),ordering));
        }
        return m_atomicRoleHierarchy;
    }

    protected static void addInclusion(Map<Role,Set<Role>> subRoles,Role sub,Role sup) {
        Set<Role> subs=subRoles.get(sup);
        if (subs==null) {
            subs=new HashSet<Role>();
            subRoles.put(sup,subs);
        }
        subs.add(sub);
    }

    protected HierarchyPosition<AtomicRole> getAtomicRoleHierarchyPosition(AtomicRole r,AtomicRole topRole,AtomicRole bottomRole) {
        HierarchyPosition<AtomicRole> out=getAtomicRoleHierarchy().get(r);
        if (out==null) {
            NaiveHierarchyPosition<AtomicRole> newPos=new NaiveHierarchyPosition<AtomicRole>(r);
            newPos.parents.add(getAtomicRoleHierarchy().get(topRole));
            newPos.children.add(getAtomicRoleHierarchy().get(bottomRole));
            out=newPos;
        }
        return out;
    }

    // Individual inferences
    
    public boolean isInstanceOf(String classURI,String individualURI) {
        return m_tableau.isInstanceOf(AtomicConcept.create(classURI),Individual.create(individualURI));
    }
    
    public boolean isInstanceOf(OWLDescription description,OWLIndividual individual) {
        if (description instanceof OWLClass)
            return isInstanceOf(((OWLClass)description).getURI().toString(),individual.getURI().toString());
        else {
            OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
            OWLDataFactory factory=ontologyManager.getOWLDataFactory();
            OWLClass newClass=factory.getOWLClass(URI.create("internal:query-concept"));
            OWLAxiom classDefinitionAxiom=factory.getOWLSubClassAxiom(description,newClass);
            DLOntology newDLOntology=extendDLOntology(m_config,m_namespaces,"uri:urn:internal-kb",m_dlOntology,ontologyManager,classDefinitionAxiom);
            Tableau tableau=createTableau(m_config,newDLOntology,m_namespaces);
            return tableau.isInstanceOf(AtomicConcept.create("internal:query-concept"),Individual.create(individual.getURI().toString()));
        }
    }
    
    public void computeRealization() {
        getRealization();
    }

    public boolean isRealizationComputed() {
        return m_realization!=null;
    }

    public Set<HierarchyPosition<String>> getIndividualTypes(String individual) {
        Set<HierarchyPosition<AtomicConcept>> directSuperConceptPositions=getDirectSuperConceptPositions(Individual.create(individual));
        Set<HierarchyPosition<String>> result=new HashSet<HierarchyPosition<String>>();
        for (HierarchyPosition<AtomicConcept> hierarchyPosition : directSuperConceptPositions)
            result.add(new TranslatedHierarchyPosition<AtomicConcept,String>(hierarchyPosition,new ConceptToString()));
        return result;
    }

    public Set<HierarchyPosition<OWLClass>> getIndividualTypes(OWLIndividual individual) {
        Set<HierarchyPosition<AtomicConcept>> directSuperConceptPositions=getDirectSuperConceptPositions(Individual.create(individual.getURI().toString()));
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<HierarchyPosition<OWLClass>> result=new HashSet<HierarchyPosition<OWLClass>>();
        for (HierarchyPosition<AtomicConcept> hierarchyPosition : directSuperConceptPositions)
            result.add(new TranslatedHierarchyPosition<AtomicConcept,OWLClass>(hierarchyPosition,new ConceptToOWLClass(factory)));
        return result;
    }

    public Set<String> getClassInstances(String className) {
        Set<String> result=new HashSet<String>();
        for (AtomicConcept atomicConcept : getConceptHierarchyPosition(AtomicConcept.create(className)).getDescendants()) {
            Set<Individual> realizationForConcept=getRealization().get(atomicConcept);
            // realizationForConcept could be null because of the way realization is constructed;
            // for example, concepts that don't have direct instances are not entered into the realization at all.
            if (realizationForConcept!=null)
                for (Individual individual : realizationForConcept)
                    result.add(individual.getURI());
        }
        return result;
    }

    public Set<OWLIndividual> getClassInstances(OWLDescription description) {
        HierarchyPosition<OWLClass> hierarchyPosition=getClassHierarchyPosition(description);
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLIndividual> result=new HashSet<OWLIndividual>();
        loadIndividualsOfPosition(hierarchyPosition,result,factory);
        for (HierarchyPosition<OWLClass> descendantHierarchyPosition : hierarchyPosition.getDescendantPositions())
            loadIndividualsOfPosition(descendantHierarchyPosition,result,factory);
        return result;
    }
    
    protected void loadIndividualsOfPosition(HierarchyPosition<OWLClass> position,Set<OWLIndividual> result,OWLDataFactory factory) {
        AtomicConcept atomicConcept=AtomicConcept.create(position.getEquivalents().iterator().next().getURI().toString());
        Set<Individual> realizationForConcept=getRealization().get(atomicConcept);
        // realizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null)
            for (Individual individual : realizationForConcept)
                result.add(factory.getOWLIndividual(URI.create(individual.getURI())));
    }
    
    public Set<String> getClassDirectInstances(String className) {
        Set<String> result=new HashSet<String>();
        Set<Individual> realizationForConcept=getRealization().get(AtomicConcept.create(className));
        // realizationForConcept could be null because of the way realization is constructed;
        // for example, concepts that don't have direct instances are not entered into the realization at all.
        if (realizationForConcept!=null)
            for (Individual individual : realizationForConcept)
                result.add(individual.getURI());
        return result;
    }

    public Set<OWLIndividual> getClassDirectInstances(OWLDescription description) {
        HierarchyPosition<OWLClass> hierarchyPosition=getClassHierarchyPosition(description);
        OWLDataFactory factory=OWLManager.createOWLOntologyManager().getOWLDataFactory();
        Set<OWLIndividual> result=new HashSet<OWLIndividual>();
        Map<AtomicConcept,Set<Individual>> realization=getRealization();
        Set<OWLClass> children=hierarchyPosition.getEquivalents();
        if (children.isEmpty())
            for (HierarchyPosition<OWLClass> childPosition : hierarchyPosition.getChildPositions())
                children.addAll(childPosition.getEquivalents());
        for (OWLClass child : children) {
            Set<Individual> realizationForConcept=realization.get(AtomicConcept.create(child.getURI().toString()));
            // realizationForConcept could be null because of the way realization is constructed;
            // for example, concepts that don't have direct instances are not entered into the realization at all.
            if (realizationForConcept!=null)
                for (Individual individual : realizationForConcept)
                    result.add(factory.getOWLIndividual(URI.create(individual.getURI())));
        }
        return result;
    }

    protected Map<AtomicConcept,Set<Individual>> getRealization() {
        if (m_realization==null) {
            m_realization=new HashMap<AtomicConcept,Set<Individual>>();
            for (Individual individual : m_dlOntology.getAllIndividuals()) {
                Set<HierarchyPosition<AtomicConcept>> directSuperConceptPositions=getDirectSuperConceptPositions(individual);
                for (HierarchyPosition<AtomicConcept> directSuperConceptPosition : directSuperConceptPositions) {
                    for (AtomicConcept directSuperConcept : directSuperConceptPosition.getEquivalents()) {
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
        return m_realization;
    }
    
    protected Set<HierarchyPosition<AtomicConcept>> getDirectSuperConceptPositions(final Individual individual) {
        Classifier.Util<HierarchyPosition<AtomicConcept>> util=new Classifier.Util<HierarchyPosition<AtomicConcept>>() {
            public Set<HierarchyPosition<AtomicConcept>> nexts(HierarchyPosition<AtomicConcept> u) {
                return u.getChildPositions();
            }
            public Set<HierarchyPosition<AtomicConcept>> prevs(HierarchyPosition<AtomicConcept> u) {
                return u.getParentPositions();
            }
            public boolean trueOf(HierarchyPosition<AtomicConcept> u) {
                AtomicConcept atomicConcept=u.getEquivalents().iterator().next();
                if (AtomicConcept.THING.equals(atomicConcept))
                    return true;
                else
                    return m_tableau.isInstanceOf(atomicConcept,individual);
            }
        };
        Set<HierarchyPosition<AtomicConcept>> topPositions=Collections.singleton(getAtomicConceptHierarchy().get(AtomicConcept.THING));
        return Classifier.search(util,topPositions,null);
    }

    protected boolean isInstanceOf(AtomicConcept atomicConcept,Individual individual) {
        if (AtomicConcept.THING.equals(atomicConcept))
            return true;
        else
            return m_tableau.isInstanceOf(atomicConcept,individual);
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
    
    protected static Namespaces createNamespaces(String ontologyURI) {
        Map<String,String> namespaceDecl=new HashMap<String,String>();
        namespaceDecl.put("",ontologyURI+"#");
        return Namespaces.withInternalNamespaces(new Namespaces(namespaceDecl,Namespaces.semanticWebNamespaces));

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
}
