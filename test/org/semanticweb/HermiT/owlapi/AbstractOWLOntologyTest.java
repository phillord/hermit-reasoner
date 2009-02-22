package org.semanticweb.HermiT.owlapi;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.owlapi.structural.OwlClausificationd;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractOWLOntologyTest extends TestCase {
    protected static final Node[][] NO_TUPLES = new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    protected static final String[] IGNORED_CLAUSES = {
        " :- owl:BottomDataProperty*(X,Y)",
        " :- owl:BottomObjectProperty(X,Y)"
    };
    static {
        Set<DLClause> dlClauses = Collections.emptySet();
        Set<Atom> atoms = Collections.emptySet();
        EMPTY_DL_ONTOLOGY = new DLOntology(
                "opaque:test", // ontology_URI
                dlClauses, // clauses
                atoms, // positive facts
                atoms, // negative facts 
                null, // atomic concepts
                null, // individuals
                null, // role hierarchy
                false, // hasInverseRoles
                false, // hasAtMostRestrictions
                false, // hasNominals
                false, // canUseNIRule
                false, // hasReflexivity
                false); // hasDatatypes
    }
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_ontology;

    public AbstractOWLOntologyTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        m_ontology = m_ontologyManager.createOntology(URI.create("file:/c:/test/ontology.owl"));
    }

    protected void tearDown() {
        m_ontologyManager = null;
        m_ontology = null;
    }

    /**
     * loads an ontology via the OWL API
     * 
     * @param physicalURI
     *            the physical location of the ontology
     * @throws Exception
     */
    protected void loadOWLOntology(String physicalURI) throws Exception {
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        URI uri = URI.create(physicalURI);
        m_ontology = m_ontologyManager.loadOntologyFromPhysicalURI(uri);
    }

    /**
     * loads an ontology from a relative path via the OWL API
     * 
     * @param resourceName
     *            the relative location of the ontology
     * @throws Exception
     */
    protected void loadOWLOntologyFromResource(String resourceName) throws Exception {
        loadOWLOntology(getClass().getResource(resourceName).toString());
    }

    /**
     * loads an OWL ontology that contains the given axioms
     * 
     * @param axioms in functional style syntax
     * @throws InterruptedException
     * @throws OWLException
     */
    protected void loadOWLOntologyWithAxioms(String axioms) throws OWLException, InterruptedException {
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
        m_ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntologyInputSource input = new StringInputSource(buffer.toString());
        m_ontology = m_ontologyManager.loadOntology(input);
    }

    /**
     * @return a clausified version of the loaded ontology
     * @throws Exception
     */
    protected DLOntology getDLOntology() throws Exception {
        OwlClausificationd clausifier = new OwlClausificationd(new Reasoner.Configuration());
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        return clausifier.clausify(m_ontologyManager, m_ontology, noDescriptionGraphs);
    }

    protected Tableau getTableau() throws Exception {
        DLOntology dlOntology = getDLOntology();
        DirectBlockingChecker directBlockingChecker = PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache = new BlockingSignatureCache(
                directBlockingChecker);
        BlockingStrategy blockingStrategy = new AnywhereBlocking(
                directBlockingChecker, blockingSignatureCache);
        ExpansionStrategy ExpansionStrategy = new CreationOrderStrategy(
                blockingStrategy);
        return new Tableau(null, ExpansionStrategy, dlOntology, false,
                new HashMap<String, Object>());
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
        if (resourceName == null) return null;
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
     * converts the axioms to a string via the toString method and compares it
     * with the given string
     */
    protected void assertEquals(Set<OWLAxiom> axioms, String controlResourceName)
            throws Exception {
        String axiomsString = axioms.toString().trim();
        String controlString = getResourceText(controlResourceName).trim();
        boolean isOK = axiomsString.equals(controlString);
        if (!isOK) {
            System.out.println("Test " + this.getName() + " failed!");
            System.out.println("Control string:");
            System.out.println("------------------------------------------");
            System.out.println(controlString);
            System.out.println("------------------------------------------");
            System.out.println("Actual string:");
            System.out.println("------------------------------------------");
            System.out.println(axiomsString);
            System.out.println("------------------------------------------");
            System.out.flush();
        }
        assertTrue(isOK);
    }

    /**
     * converts the axioms to a string via the toString method and compares it
     * with the given string
     */
    protected void assertEquals(Set<OWLAxiom> axioms, 
            String controlResourceName, 
            String controlResourceNameVariant)
            throws Exception {
        String axiomsString = axioms.toString().trim();
        String controlString = getResourceText(controlResourceName).trim();
        boolean isOK = axiomsString.equals(controlString);
        String controlStringVariant = "";
        if (!isOK && controlResourceNameVariant != null) {
            controlStringVariant = getResourceText(controlResourceNameVariant).trim();
            isOK = axiomsString.equals(controlStringVariant);
        }
        if (!isOK) {
            System.out.println("Test " + this.getName() + " failed!");
            System.out.println("Control string:");
            System.out.println("------------------------------------------");
            System.out.println(controlString);
            System.out.println("------------------------------------------");
            if (controlResourceNameVariant != null) {
                System.out.println("Control string (allowed variant):");
                System.out.println("------------------------------------------");
                System.out.println(controlStringVariant);
                System.out.println("------------------------------------------");
            }
            System.out.println("Actual string:");
            System.out.println("------------------------------------------");
            System.out.println(axiomsString);
            System.out.println("------------------------------------------");
            System.out.flush();
        }
        assertTrue(isOK);
    }
    
    /**
     * prints the content of control set and the actual set in case they are
     * different and causes a JUnit test failure
     */
    protected <T> void assertEquals(Set<T> actual, Set<T> control)
            throws Exception {
        if (!actual.equals(control)) {
            System.out.println("Test " + this.getName() + " failed!");
            System.out.println("Control set (" + control.size() + " elements):");
            System.out.println("------------------------------------------");
            for (T object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set (" + actual.size() + " elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            assertTrue(false);
        }
    }

    /**
     * tests that the sets have equal length and that the actual set contains all
     * objects from the control set, otherwise the test fails and the contents
     * of the control and the actual set are printed
     */
    protected static void assertContainsAll(String testName, 
            Collection<String> actual,
            String[] control, String[] controlVariant) {
        for (String s : IGNORED_CLAUSES) {
            actual.remove(s);
        }
        for (Iterator<String> i = actual.iterator(); i.hasNext(); ) {
            String s = i.next();
            if (s.startsWith("owl:TopObjectProperty") ||
                s.startsWith("owl:TopDataProperty")) {
                i.remove();
            }
        }
        try {
            assertEquals(control.length, actual.size());
            boolean isOK = true;
            for (int i = 0; i < control.length; i++) {
                if (isOK) isOK = actual.contains(control[i]);
            }
            boolean isOKVariant = false;
            if (controlVariant != null) {
                isOKVariant = true;
                for (int i = 0; i < controlVariant.length; i++) {
                    if (isOK) isOK = actual.contains(control[i]);
                }
            }
            assertTrue(isOK || isOKVariant);
        } catch (AssertionFailedError e) {
            System.out.println("Test " + testName + " failed!");
            System.out.println("Control set (" + control.length + " elements):");
            System.out.println("------------------------------------------");
            for (String object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set (" + actual.size() + " elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }
    
    /**
     * tests that the set have equal length and that the actual set contains all
     * objects from the control set, otherwise the test fails and the contents
     * of the control and the actual set are printed
     */
    protected static <T> void assertContainsAll(String testName, 
            Collection<T> actual,
            Collection<T> control) {
        for (String s : IGNORED_CLAUSES) {
            actual.remove(s);
        }
        for (Iterator<T> i = actual.iterator(); i.hasNext(); ) {
            T val = i.next();
            if (val instanceof String) {
                String s = (String) val;
                if (s.startsWith("owl:TopObjectProperty") ||
                    s.startsWith("owl:TopDataProperty")) {
                    i.remove();
                }
            }
        }
        
        try {
            assertEquals(control.size(), actual.size());
            for (T contr : control)
                assertTrue(actual.contains(contr));
        } catch (AssertionFailedError e) {
            System.out.println("Test " + testName + " failed!");
            System.out.println("Control set (" + control.size() + " elements):");
            System.out.println("------------------------------------------");
            for (T object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set (" + actual.size() + " elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }
}
