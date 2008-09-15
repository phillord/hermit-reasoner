package org.semanticweb.HermiT.reasoner;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.HermiT.HermiT;
import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractReasonerTest extends TestCase {
    protected static final Node[][] NO_TUPLES = new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    static {
        Set<DLClause> dlClauses = Collections.emptySet();
        Set<Atom> atoms = Collections.emptySet();
        EMPTY_DL_ONTOLOGY = new DLOntology("opaque:test", dlClauses, atoms,
                atoms, false, false, false, false, false);
    }
    protected OWLOntology m_ontology;
    protected HermiT hermit;

    public AbstractReasonerTest(String name) {
        super(name);
    }

    // protected void setUp() throws Exception {
    // hermit = new HermiT();
    // OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    // m_ontology =
    // manager.createOntology(URI.create("file:/c:/test/ontology.owl"));
    // }
    //
    // protected void tearDown() {
    // m_ontology = null;
    // }

    /**
     * Loads an ontology via the OWL API so that it is available for the custom
     * assert methods.
     * 
     * @param resource
     *            the resource to load
     * @throws Exception
     *             if the resource cannot be found or an error occurred when
     *             loading the ontology
     */
    protected void loadOntologyFromResource(String resourceName)
            throws Exception {
        HermiT.Configuration configuration = new HermiT.Configuration();
        configuration.subsumptionCacheStrategyType = HermiT.SubsumptionCacheStrategyType.ON_REQUEST;
        hermit = new HermiT(getClass().getResource(resourceName).toURI(), configuration);
    }

    /**
     * creates and loads an ontology that contains the given axioms
     * 
     * @param axioms
     *            in functional style syntax
     * @throws InterruptedException
     * @throws OWLException
     */
    protected void loadOntologyWithAxioms(String axioms) throws OWLException,
            InterruptedException {
        StringBuffer buffer = new StringBuffer();
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
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntologyInputSource input = new StringInputSource(buffer.toString());
        m_ontology = manager.loadOntology(input);
        hermit = new HermiT(m_ontology, new HermiT.Configuration());
    }

    /**
     * @param resourceName
     * @return each line from the loaded resource becomes a string in the
     *         returned array
     * @throws Exception
     */
    protected Set<String> getStrings(String resourceName) throws Exception {
        Set<String> strings = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResource(resourceName).openStream()));
        try {
            String line = reader.readLine();
            while (line != null) {
                strings.add(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return strings;
    }

    /**
     * @param resourceName
     * @return the content of the loaded resource as one string
     * @throws Exception
     */
    protected String getResourceText(String resourceName) throws Exception {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter output = new PrintWriter(buffer);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResource(resourceName).openStream()));
        try {
            String line = reader.readLine();
            while (line != null) {
                output.println(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        output.flush();
        return buffer.toString();
    }

    /**
     * 
     * @return the content of the loaded resource as one string
     * @throws Exception
     */
    protected String getSubsumptionHierarchyAsText() throws Exception {
        Map<String, HierarchyPosition<String>> taxonomy = hermit.getClassTaxonomy();
        // Namespaces namespaces = hermit.getNamespaces();
        // namespaces.registerStandardPrefixes();
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter output = new PrintWriter(buffer);
        HermiT.printSortedAncestorLists(output, taxonomy);
        return buffer.toString();
    }

    protected void assertSubsumptionHierarchy(String ontologyResource,
            String controlResource) throws Exception {
        loadOntologyFromResource(ontologyResource);
        String taxonomy = getSubsumptionHierarchyAsText();
        String controlString = getResourceText(controlResource);
        assertEquals(taxonomy, controlString);
    }

    protected void assertABoxSatisfiable(boolean satisfiable) throws Exception {
        assertEquals(satisfiable, hermit.isConsistent());
    }

    protected void assertSubsumedBy(String subAtomicConcept,
            String superAtomicConcept, boolean expectedResult) throws Exception {
        if (!subAtomicConcept.contains("#"))
            subAtomicConcept = "file:/c/test.owl#" + subAtomicConcept;
        if (!superAtomicConcept.contains("#"))
            superAtomicConcept = "file:/c/test.owl#" + superAtomicConcept;
        boolean result = hermit.isClassSubsumedBy(subAtomicConcept,
                superAtomicConcept);
        assertEquals(expectedResult, result);
    }

    protected void assertSatisfiable(String atomicConcept, boolean satisfiable)
            throws Exception {
        assertEquals(satisfiable, hermit.isClassSatisfiable("file:/c/test.owl#"
                + atomicConcept));
    }
}
