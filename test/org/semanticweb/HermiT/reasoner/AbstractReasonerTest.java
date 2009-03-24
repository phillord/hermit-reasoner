package org.semanticweb.HermiT.reasoner;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.AbstractHermiTTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractReasonerTest extends AbstractHermiTTest {
    protected static final Node[][] NO_TUPLES=new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    static {
        Set<DLClause> dlClauses=Collections.emptySet();
        Set<Atom> atoms=Collections.emptySet();
        EMPTY_DL_ONTOLOGY=new DLOntology("opaque:test", // ontology_URI
                dlClauses, // clauses
                atoms, // positive facts
                atoms, // negative facts
                null, // atomic concepts
                null, // transitive roles
                null, // object roles
                null, // data roles
                null, // individuals
                false, // hasInverseRoles
                false, // hasAtMostRestrictions
                false, // hasNominals
                false, // canUseNIRule
                false); // hasDatatypes
    }
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_ontology;
    protected Reasoner m_reasoner;

    public AbstractReasonerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontology=m_ontologyManager.createOntology(URI.create("file:/c:/test/ontology.owl"));
    }

    protected void tearDown() {
        m_ontologyManager=null;
        m_ontology=null;
        m_reasoner=null;
    }

    /**
     * Loads an ontology via the OWL API with the standard configuration so that
     * it is available for the custom assert methods.
     * 
     * @param resource
     *            the resource to load
     * @throws URISyntaxException
     *             if the resourceName cannot be convertd into a URI
     * @throws OWLException
     *             if there are problems during the parsing of the ontology in resourceName
     * @throws LoadingException
     *             if the ontology cannot be loaded
     * @throws IllegalArgumentException
     *             inappropriate argument
     */
    protected void loadOntologyFromResource(String resourceName) throws Exception {
        loadOntologyFromResource(getConfiguration(),resourceName);
    }

    /**
     * Loads an ontology via the OWL API so that it is available for the custom assert methods.
     * 
     * @param configuration
     *            a configuration for the reasoner instance
     * @param resource
     *            the resource to load
     * @throws URISyntaxException
     *             if the resourceName cannot be convertd into a URI
     * @throws OWLException
     *             if there are problems during the parsing of the ontology in resourceName
     * @throws LoadingException
     *             if the ontology cannot be loaded
     * @throws IllegalArgumentException
     *             inappropriate argument
     */
    protected void loadOntologyFromResource(Configuration configuration,String resourceName) throws Exception {
        URI physicalURI=getClass().getResource(resourceName).toURI();
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontology=m_ontologyManager.loadOntologyFromPhysicalURI(physicalURI);
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology);
    }

    /**
     * Creates and loads an ontology that contains the given axioms. Uses the standard configuration
     * for the reasoner instance. Standard namespace is "file:/c/test.owl" and that is also the URI of the ontology.
     * 
     * @param axioms
     *            in functional style syntax
     * @throws OWLOntologyCreationException
     *             if the ontology could not be created
     */
    protected void loadOntologyWithAxioms(String axioms) throws OWLOntologyCreationException {
        loadOntologyWithAxiomsAndKeys(getConfiguration(),axioms,null);
    }

    /**
     * creates and loads an ontology that contains the given axioms
     * 
     * @param axioms
     *            in functional style syntax
     * @param keys
     *            a set of HasKey axioms (till the OWL API supports them)
     * @throws OWLOntologyCreationException
     *             if the ontology could not be created
     */
    protected void loadOntologyWithAxiomsAndKeys(String axioms,Set<OWLHasKeyDummy> keys) throws OWLOntologyCreationException {
        loadOntologyWithAxiomsAndKeys(getConfiguration(),axioms,keys);
    }

    /**
     * creates and loads an ontology that contains the given axioms
     * 
     * @param configuration
     *            a configuration for the reasoner instance
     * @param axioms
     *            in functional style syntax
     * @param keys
     *            a set of key axioms (till the OWL API supports them)
     * @throws OWLOntologyCreationException
     *             if the ontology could not be created
     */
    protected void loadOntologyWithAxiomsAndKeys(Configuration configuration,String axioms,Set<OWLHasKeyDummy> keys) throws OWLOntologyCreationException {
        StringBuffer buffer=new StringBuffer();
        buffer.append("Namespace(=<file:/c/test.owl#>)");
        buffer.append("Namespace(rdfs=<http://www.w3.org/2000/01/rdf-schema#>)");
        buffer.append("Namespace(owl2xml=<http://www.w3.org/2006/12/owl2-xml#>)");
        buffer.append("Namespace(test=<file:/c/test.owl#>)");
        buffer.append("Namespace(owl=<http://www.w3.org/2002/07/owl#>)");
        buffer.append("Namespace(xsd=<http://www.w3.org/2001/XMLSchema#>)");
        buffer.append("Namespace(rdf=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
        buffer.append("Ontology(<file:/c/test.owl>");
        buffer.append(axioms);
        buffer.append(")");
        OWLOntologyInputSource input=new StringInputSource(buffer.toString());
        m_ontology=m_ontologyManager.loadOntology(input);
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology,null,keys);
    }

    protected void loadOntologyWithAxiomsOnly(String axioms) throws OWLException,InterruptedException {
        StringBuffer buffer=new StringBuffer();
        buffer.append("Namespace(=<file:/c/test.owl#>)");
        buffer.append("Namespace(rdfs=<http://www.w3.org/2000/01/rdf-schema#>)");
        buffer.append("Namespace(owl2xml=<http://www.w3.org/2006/12/owl2-xml#>)");
        buffer.append("Namespace(test=<file:/c/test.owl#>)");
        buffer.append("Namespace(owl=<http://www.w3.org/2002/07/owl#>)");
        buffer.append("Namespace(xsd=<http://www.w3.org/2001/XMLSchema#>)");
        buffer.append("Namespace(rdf=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
        buffer.append("Ontology(<file:/c/test.owl>");
        buffer.append(axioms);
        buffer.append(")");
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        OWLOntologyInputSource input=new StringInputSource(buffer.toString());
        m_ontology=m_ontologyManager.loadOntology(input);
    }

    protected void createReasoner() {
        createReasoner(getConfiguration());
    }

    protected void createReasoner(Configuration configuration) {
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology);
    }

    protected void loadOntologyWithAxioms(String axioms,Configuration configuration) throws OWLException,InterruptedException {
        loadOntologyWithAxiomsOnly(axioms);
        createReasoner(configuration);
    }

    protected void loadOWLOntology(Configuration configuration,OWLOntology ontology,Set<DescriptionGraph> descriptionGraphs) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        m_reasoner=new Reasoner(configuration,m_ontologyManager,ontology,descriptionGraphs,null);
    }

    /**
     * Loads the resource resourceName and returns a set of strings such that each line in the input resource becomes an entry of the set.
     * 
     * @param resourceName
     * @return each line from the loaded resource becomes a string in the returned array
     * @throws IOException
     *             if the resource cannot be found
     */
    protected Set<String> getStrings(String resourceName) throws IOException {
        Set<String> strings=new HashSet<String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResource(resourceName).openStream()));
        try {
            String line=reader.readLine();
            while (line!=null) {
                strings.add(line);
                line=reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        return strings;
    }

    /**
     * Loads the resource resourceName and returns its content as a string.
     * 
     * @param resourceName
     * @return the content of the loaded resource as one string
     * @throws IOException
     *             if the resource cannot be found
     */
    protected String getResourceText(String resourceName) throws IOException {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResource(resourceName).openStream()));
        try {
            String line=reader.readLine();
            while (line!=null) {
                output.println(line);
                line=reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        output.flush();
        return buffer.toString();
    }

    /**
     * Returns a string with a sorted ancestor list that represents the taxonomy of the ontology that is currently loaded in the reasoner.
     * 
     * @return the taxonomy
     */
    protected String getSubsumptionHierarchyAsText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        m_reasoner.printClassHierarchy(output);
        output.flush();
        return buffer.toString();
    }

    /**
     * Loads the ontology from ontologyResource and the string in controlResource and compares the computed taxonomy for the ontology with the one in the controlResource by comparing the sorted ancestor list.
     * 
     * @param ontologyResource
     *            the ontology
     * @param controlResource
     *            the expected taxonomy (sorted ancestor list)
     * @throws URISyntaxException
     *             if the resources cannot be converted to URIs
     * @throws OWLException
     *             if the ontology is invalid
     * @throws LoadingException
     *             if an error occurs during loading
     * @throws IllegalArgumentException
     *             in case of illegal arguments
     * @throws IOException
     *             if the controlString cannot be loaded
     */
    protected void assertSubsumptionHierarchy(String controlResource) throws Exception {
        String taxonomy=getSubsumptionHierarchyAsText();
        String controlString=getResourceText(controlResource);
        assertEquals(controlString,taxonomy);
    }

    /**
     * Tests whether the loaded ontology is consistent or not and asserts that this coincides with the given parameter satisfiable.
     * 
     * @param satisfiable
     *            if the currently loaded ontology is expected to be satisfiable
     */
    protected void assertABoxSatisfiable(boolean satisfiable) {
        assertEquals(satisfiable,m_reasoner.isConsistent());
    }

    /**
     * Tests whether the atomic concept subAtomicConcept is subsumed by the atomic concept superAtomicConcept and asserts that this coincides with the expected result.
     * 
     * @param subAtomicConcept
     *            a string that represents an atomic concept. If no namespace is given, file:/c/test.owl# is used as prefix
     * @param superAtomicConcept
     *            a string that represents an atomic concept. If no namespace is given, file:/c/test.owl# is used as prefix
     * @param expectedResult
     */
    protected void assertSubsumedBy(String subAtomicConcept,String superAtomicConcept,boolean expectedResult) {
        if (!subAtomicConcept.contains("#"))
            subAtomicConcept="file:/c/test.owl#"+subAtomicConcept;
        if (!superAtomicConcept.contains("#"))
            superAtomicConcept="file:/c/test.owl#"+superAtomicConcept;
        OWLClass subClass=m_ontologyManager.getOWLDataFactory().getOWLClass(URI.create(subAtomicConcept));
        OWLClass superClass=m_ontologyManager.getOWLDataFactory().getOWLClass(URI.create(superAtomicConcept));
        boolean result=m_reasoner.isSubClassOf(subClass,superClass);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the possibly complex concept subConcept is subsumed by the possibly complex concept superConcept and asserts that this coincides with the expected result.
     */
    protected void assertSubsumedBy(OWLDescription subConcept,OWLDescription superConcept,boolean expectedResult) {
        boolean result=m_reasoner.isSubClassOf(subConcept,superConcept);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the atomic concept atomicConcept is satisfiable and asserts that this coincides with the expected result (satisfiable).
     */
    protected void assertSatisfiable(String atomicConcept,boolean expectedResult) {
        if (!atomicConcept.contains("#"))
            atomicConcept="file:/c/test.owl#"+atomicConcept;
        OWLClass clazz=m_ontologyManager.getOWLDataFactory().getOWLClass(URI.create(atomicConcept));
        boolean result=m_reasoner.isSatisfiable(clazz);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the given possibly complex concept is satisfiable and asserts that this coincides with the expected result (satisfiable).
     */
    protected void assertSatisfiable(OWLDescription concept,boolean satisfiable) throws Exception {
        assertEquals(satisfiable,m_reasoner.isSatisfiable(concept));
    }

    /**
     * Tests whether the given individual is an instance of the given concept and asserts that this coincides with the expected result.
     */
    protected void assertInstanceOf(OWLDescription concept,OWLIndividual individual,boolean expectedResult) {
        boolean result=m_reasoner.hasType(individual,concept,false);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the given concept has the specified individuals.
     */
    protected void assertInstancesOf(OWLDescription concept,boolean direct,String... expectedIndividuals) {
        Set<OWLIndividual> actual=m_reasoner.getIndividuals(concept,direct);
        Set<String> actualIndividualURIs=new HashSet<String>();
        for (OWLIndividual individual : actual)
            actualIndividualURIs.add(individual.getURI().toString());
        String[] expectedModified=expectedIndividuals.clone();
        assertContainsAll(actualIndividualURIs,expectedModified);
    }
    
    /**
     * Checks the superproperties of some object property.
     */
    protected void assertSuperObjectProperties(String objectProperty,Set<String>... control) {
        if (!objectProperty.contains("#"))
            objectProperty="file:/c/test.owl#"+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSuperProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some object property.
     */
    protected void assertSubObjectProperties(String objectProperty,Set<String>... control) {
        if (!objectProperty.contains("#"))
            objectProperty="file:/c/test.owl#"+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSubProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some object property.
     */
    protected void assertEquivalentObjectProperties(String objectProperty,String... control) {
        if (!objectProperty.contains("#"))
            objectProperty="file:/c/test.owl#"+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<String> actual=setOfOPEsToStrings(m_reasoner.getEquivalentProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the superproperties of some data property.
     */
    protected void assertSuperDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty="file:/c/test.owl#"+dataProperty;
        OWLDataProperty dp=m_ontologyManager.getOWLDataFactory().getOWLDataProperty(URI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSuperProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some data property.
     */
    protected void assertSubDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty="file:/c/test.owl#"+dataProperty;
        OWLDataProperty dp=m_ontologyManager.getOWLDataFactory().getOWLDataProperty(URI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSubProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some data property.
     */
    protected void assertEquivalentDataProperties(String dataProperty,String... control) {
        if (!dataProperty.contains("#"))
            dataProperty="file:/c/test.owl#"+dataProperty;
        OWLDataProperty dp=m_ontologyManager.getOWLDataFactory().getOWLDataProperty(URI.create(dataProperty));
        Set<String> actual=setOfDPsToStrings(m_reasoner.getEquivalentProperties(dp));
        assertContainsAll(actual,control);
    }
    
    protected static Set<Set<String>> setOfSetsOfOPEsToStrings(Set<Set<OWLObjectPropertyExpression>> setOfSets) {
        Set<Set<String>> result=new HashSet<Set<String>>();
        for (Set<OWLObjectPropertyExpression> set : setOfSets) {
            Set<String> translatedSet=setOfOPEsToStrings(set);
            result.add(translatedSet);
        }
        return result;
    }
    
    protected static Set<String> setOfOPEsToStrings(Set<OWLObjectPropertyExpression> set) {
        Set<String> translatedSet=new HashSet<String>();
        for (OWLObjectPropertyExpression ope : set)
            if (ope instanceof OWLObjectProperty)
                translatedSet.add(((OWLObjectProperty)ope).getURI().toString());
            else {
                OWLObjectProperty innerOp=ope.getNamedProperty();
                translatedSet.add("(inv "+innerOp.getURI().toString()+")");
            }
        return translatedSet;
    }    
    
    protected static Set<Set<String>> setOfSetsOfDPsToStrings(Set<Set<OWLDataProperty>> setOfSets) {
        Set<Set<String>> result=new HashSet<Set<String>>();
        for (Set<OWLDataProperty> set : setOfSets) {
            Set<String> translatedSet=setOfDPsToStrings(set);
            result.add(translatedSet);
        }
        return result;
    }
    
    protected static Set<String> setOfDPsToStrings(Set<OWLDataProperty> set) {
        Set<String> translatedSet=new HashSet<String>();
        for (OWLDataProperty dp : set)
            translatedSet.add(dp.getURI().toString());
        return translatedSet;
    }    
    
    /**
     * Can be overridden by the subclass to provide a different configuration for the tests.
     */
    protected Configuration getConfiguration() {
        return new Configuration();
    }

    protected static Set<String> EQ(String... args) {
        Set<String> result=new HashSet<String>();
        for (String arg : args) {
            if (!arg.contains("#"))
                arg="file:/c/test.owl#"+arg;
            result.add(arg);
        }
        return result;
    }

    protected static String[] URIs(String... args) {
        for (int index=0;index<args.length;index++)
            args[index]=URI(args[index]);
        return args;
    }

    protected static String URI(String arg) {
        if (!arg.contains("#"))
            arg="file:/c/test.owl#"+arg;
        return arg;
    }

    protected static String INV(String arg) {
        if (!arg.contains("#"))
            arg="file:/c/test.owl#"+arg;
        return "(inv "+arg+")";
    }
}
