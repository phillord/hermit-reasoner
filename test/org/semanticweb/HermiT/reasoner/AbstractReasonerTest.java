package org.semanticweb.HermiT.reasoner;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Clausifier.LoadingException;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractReasonerTest extends TestCase {
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
    }

    /**
     * Loads an ontology via the OWL API with the standard configuration so that 
     * it is available for the custom assert methods.
     * 
     * @param resource
     *            the resource to load
     * @throws URISyntaxException if the resourceName cannot be convertd into a URI
     * @throws OWLException if there are problems during the parsing of the ontology in resourceName
     * @throws LoadingException if the ontology cannot be loaded
     * @throws IllegalArgumentException inappropriate argument
     */
    protected void loadOntologyFromResource(String resourceName) throws IllegalArgumentException, LoadingException, OWLException, URISyntaxException {
        loadOntologyFromResource(new Configuration(), resourceName);
    }
    
    /**
     * Loads an ontology via the OWL API so that it is available for the custom assert methods.
     * 
     * @param configuration a configuration for the reasoner instance
     * @param resource
     *            the resource to load
     * @throws URISyntaxException if the resourceName cannot be convertd into a URI
     * @throws OWLException if there are problems during the parsing of the ontology in resourceName
     * @throws LoadingException if the ontology cannot be loaded
     * @throws IllegalArgumentException inappropriate argument
     */
    protected void loadOntologyFromResource(Configuration configuration, String resourceName) throws IllegalArgumentException, LoadingException, OWLException, URISyntaxException {
        if (configuration==null) {
            configuration=new Configuration();
        }
        m_reasoner=new Reasoner(configuration,getClass().getResource(resourceName).toURI());
    }

    /**
     * Creates and loads an ontology that contains the given axioms. Uses the 
     * standard configuration for the reasoner instance. Standard 
     * namespace is "file:/c/test.owl" and that is also the URI of the ontology. 
     * 
     * @param axioms
     *            in functional style syntax
     * @throws OWLOntologyCreationException if the ontology could not be created
     */
    protected void loadOntologyWithAxioms(String axioms) throws OWLOntologyCreationException {
        loadOntologyWithAxiomsAndKeys(new Configuration(), axioms, null);
    }
    
    /**
     * Creates and loads an ontology that contains the given axioms and uses the 
     * given configuration for the reasoner instance. Standard namespace is 
     * "file:/c/test.owl" and that is also the URI of the ontology. 
     * 
     * @param configuration a configuration for the reasoner instance
     * @param axioms
     *            in functional style syntax
     * @throws OWLOntologyCreationException if the ontology could not be created
     */
    protected void loadOntologyWithAxioms(Configuration configuration, String axioms) throws OWLOntologyCreationException {
        loadOntologyWithAxiomsAndKeys(configuration, axioms, null);
    }

    /**
     * creates and loads an ontology that contains the given axioms
     * 
     * @param axioms
     *            in functional style syntax
     * @param keys a set of HasKey axioms (till the OWL API supports them)
     * @throws OWLOntologyCreationException if the ontology could not be created
     */
    protected void loadOntologyWithAxiomsAndKeys(String axioms,Set<OWLHasKeyDummy> keys) throws OWLOntologyCreationException {
         loadOntologyWithAxiomsAndKeys(new Configuration(),axioms,keys);
    }
    
    /**
     * creates and loads an ontology that contains the given axioms
     * 
     * @param configuration a configuration for the reasoner instance
     * @param axioms in functional style syntax
     * @param keys a set of key axioms (till the OWL API supports them)
     * @throws OWLOntologyCreationException if the ontology could not be created
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
        if (configuration==null) {
            configuration=new Configuration();
        }
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

    protected void createReasoner(Configuration configuration) {
        if (configuration==null)
            configuration=new Configuration();
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology);
    }
    
    protected void loadOntologyWithAxioms(String axioms,Configuration configuration) throws OWLException,InterruptedException {
        loadOntologyWithAxiomsOnly(axioms);
        createReasoner(configuration);
    }
    
    protected void loadOWLOntology(Configuration configuration, OWLOntology ontology, Set<DescriptionGraph> dgs) {
        if (configuration==null) {
            configuration=new Configuration();
        }
        if (dgs == null) dgs = Collections.emptySet();
        m_reasoner=new Reasoner(configuration,m_ontologyManager, ontology, dgs, null);
    }

    /**
     * Loads the resource resourceName and returns a set of strings such that 
     * each line in the input resource becomes an entry of the set.  
     * @param resourceName
     * @return each line from the loaded resource becomes a string in the returned array
     * @throws IOException if the resource cannot be found
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
     * @param resourceName
     * @return the content of the loaded resource as one string
     * @throws IOException if the resource cannot be found
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
     * Returns a string with a sorted ancestor list that represents the taxonomy 
     * of the ontology that is currently loaded in the reasoner. 
     * @return the taxonomy
     */
    protected String getSubsumptionHierarchyAsText() {
        Map<String,HierarchyPosition<String>> taxonomy=m_reasoner.getClassHierarchy();
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        Reasoner.printSortedAncestorLists(output,taxonomy);
        return buffer.toString();
    }

    /**
     * Loads the ontology from ontologyResource and the string in 
     * controlResource and compares the computed taxonomy for the ontology with 
     * the one in the controlResource by comparing the sorted ancestor list. 
     * @param ontologyResource the ontology
     * @param controlResource the expected taxonomy (sorted ancestor list)
     * @throws URISyntaxException  if the resources cannot be converted to URIs
     * @throws OWLException if the ontology is invalid
     * @throws LoadingException if an error occurs during loading
     * @throws IllegalArgumentException in case of illegal arguments
     * @throws IOException if the controlString cannot be loaded
     */
    protected void assertSubsumptionHierarchy(String controlResource) throws IllegalArgumentException, LoadingException, OWLException, URISyntaxException, IOException {
        String taxonomy=getSubsumptionHierarchyAsText();
        String controlString=getResourceText(controlResource);
        assertEquals(taxonomy,controlString);
    }

    /**
     * Tests whether the loaded ontology is consistent or not and asserts that 
     * this coincides with the given parameter satisfiable.
     * @param satisfiable if the currently loaded ontology is expected to be satisfiable 
     */
    protected void assertABoxSatisfiable(boolean satisfiable) {
        assertEquals(satisfiable,m_reasoner.isConsistent());
    }

    /**
     * Tests whether the atomic concept subAtomicConcept is subsumed by the 
     * atomic concept superAtomicConcept and asserts that this coincides with 
     * the expected result. 
     * @param subAtomicConcept a string that represents an atomic concept. If no 
     *        namespace is given, file:/c/test.owl# is used as prefix 
     * @param superAtomicConcept a string that represents an atomic concept. If 
     *        no namespace is given, file:/c/test.owl# is used as prefix
     * @param expectedResult
     */
    protected void assertSubsumedBy(String subAtomicConcept,String superAtomicConcept,boolean expectedResult) {
        if (!subAtomicConcept.contains("#"))
            subAtomicConcept="file:/c/test.owl#"+subAtomicConcept;
        if (!superAtomicConcept.contains("#"))
            superAtomicConcept="file:/c/test.owl#"+superAtomicConcept;
        boolean result=m_reasoner.isClassSubsumedBy(subAtomicConcept,superAtomicConcept);
        assertEquals(expectedResult,result);
    }
    
    /**
     * Tests whether the possibly complex concept subConcept is subsumed by the 
     * possibly complex concept superConcept and asserts that this coincides 
     * with the expected result.
     * @param subConcept  
     * @param superConcept 
     * @param expectedResult
     */
    protected void assertSubsumedBy(OWLDescription subConcept,OWLDescription superConcept,boolean expectedResult) {
        boolean result=m_reasoner.isClassSubsumedBy(subConcept,superConcept);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the atomic concept atomicConcept is satisfiable and asserts 
     * that this coincides with the expected result (satisfiable).
     * @param atomicConcept a string that represents an atomic concept. If no 
     *        namespace is given, file:/c/test.owl# is used as prefix 
     * @param satisfiable
     */
    protected void assertSatisfiable(String atomicConcept,boolean satisfiable) {
        if (!atomicConcept.contains("#"))
            atomicConcept="file:/c/test.owl#"+atomicConcept;
        assertEquals(satisfiable,m_reasoner.isClassSatisfiable(atomicConcept));
    }
    
    /**
     * Tests whether the given possibly complex concept is satisfiable and 
     * asserts that this coincides with the expected result (satisfiable).
     * @param concept
     * @param satisfiable
     */
    protected void assertSatisfiable(OWLDescription concept,boolean satisfiable) throws Exception {
        assertEquals(satisfiable,m_reasoner.isClassSatisfiable(concept));
    }
    
    /**
     * Tests whether the given individual is an instance of the given concept 
     * and asserts that this coincides with the expected result. 
     * @param concept
     * @param individual
     * @param expectedResult
     */
    protected void assertInstanceOf(OWLDescription concept, OWLIndividual individual, boolean expectedResult) {
        boolean result=m_reasoner.isInstanceOf(concept,individual);
        assertEquals(expectedResult,result);
    }
    
    protected static <T> void assertContainsAll(Collection<T> actual,T... control) {
        try {
            assertEquals(control.length,actual.size());
            for (int i=0;i<control.length;i++)
                assertTrue(actual.contains(control[i]));
        }
        catch (AssertionFailedError e) {
            System.out.println("Control set ("+control.length+" elements):");
            System.out.println("------------------------------------------");
            for (T object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set ("+actual.size()+" elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }
}
