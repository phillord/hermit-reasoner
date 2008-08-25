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
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialsExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractOWLOntologyTest extends TestCase {
    protected static final Node[][] NO_TUPLES = new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    static {
        Set<DLClause> dlClauses = Collections.emptySet();
        Set<Atom> atoms = Collections.emptySet();
        EMPTY_DL_ONTOLOGY = new DLOntology("opaque:test", dlClauses, atoms,
                atoms, false, false, false, false, false);
    }
    protected OWLOntology m_ontology;

    public AbstractOWLOntologyTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        m_ontology = manager.createOntology(URI.create("file:/c:/test/ontology.owl"));
    }

    protected void tearDown() {
        m_ontology = null;
    }

    /**
     * loads an ontology via the OWL API
     * 
     * @param physicalURI
     *            the physical location of the ontology
     * @return the ontology as an OWLAPI ontology object (not simplified,
     *         normalised or clausified)
     * @throws Exception
     */
    protected OWLOntology getOWLOntology(String physicalURI) throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        URI uri = URI.create(physicalURI);
        OWLOntology ontology = manager.loadOntologyFromPhysicalURI(uri);
        return ontology;
    }

    /**
     * loads an ontology from a relative path via the OWL API
     * 
     * @param resourceName
     *            the relative location of the ontology
     * @return the ontology as an OWLAPI ontology object (not simplified,
     *         normalised or clausified)
     * @throws Exception
     */
    protected OWLOntology getOWLOntologyFromResource(String resourceName)
            throws Exception {
        return getOWLOntology(getClass().getResource(resourceName).toString());
    }

    /**
     * @return a clausified version of the loaded ontology
     * @throws Exception
     */
    protected DLOntology getDLOntology() throws Exception {
        OwlClausification clausifier = new OwlClausification();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        return clausifier.clausify(false, m_ontology, factory,
                noDescriptionGraphs);
    }

    protected Tableau getTableau() throws Exception {
        DLOntology dlOntology = getDLOntology();
        DirectBlockingChecker directBlockingChecker = PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache = new BlockingSignatureCache(
                directBlockingChecker);
        BlockingStrategy blockingStrategy = new AnywhereBlocking(
                directBlockingChecker, blockingSignatureCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy = new CreationOrderStrategy(
                blockingStrategy);
        return new Tableau(null, existentialsExpansionStrategy, dlOntology,
                new HashMap<String, Object>());
    }

    /**
     * loads an ontology from a relative path via the OWL API
     * 
     * @param resourceName
     *            the resouce to load
     * @return the set of axioms from the loaded ontology
     * @throws Exception
     */
    protected Set<OWLAxiom> getOWLAxiomsFromResource(String resourceName)
            throws Exception {
        return getOWLOntologyFromResource(resourceName).getAxioms();
    }

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
    protected void loadResource(String resource) throws Exception {
        m_ontology = getOWLOntologyFromResource(resource);
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
     * converts the axioms to a string via the toString method and compares it
     * with the given string
     */
    protected void assertEquals(Set<OWLAxiom> axioms, String controlResourceName)
            throws Exception {
        String axiomsString = axioms.toString().trim();
        String controlString = getResourceText(controlResourceName).trim();
        assertTrue(axiomsString.equals(controlString));
    }

    /**
     * prints the content of control set and the actual set in case they are
     * different and causes a JUnit test failure
     */
    protected <T> void assertEquals(Set<T> actual, Set<T> control)
            throws Exception {
        if (!actual.equals(control)) {
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
     * tests that the set have equal length and that the actual set contains all
     * objects from the control set, otherwise the test fails and the contents
     * of the control and the actual set are printed
     */
    protected static <T> void assertContainsAll(Collection<T> actual,
            T... control) {
        try {
            assertEquals(control.length, actual.size());
            for (int i = 0; i < control.length; i++)
                assertTrue(actual.contains(control[i]));
        } catch (AssertionFailedError e) {
            System.out.println("Control set (" + control.length + " elements):");
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
