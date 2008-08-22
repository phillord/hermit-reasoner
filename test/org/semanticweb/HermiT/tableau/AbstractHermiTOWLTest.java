package org.semanticweb.HermiT.tableau;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractHermiTOWLTest extends TestCase {
    protected static final Node[][] NO_TUPLES=new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    static {
        Set<DLClause> dlClauses=Collections.emptySet();
        Set<Atom> atoms=Collections.emptySet();
        EMPTY_DL_ONTOLOGY=new DLOntology("opaque:test",dlClauses,atoms,atoms,false,false,false,false,false);
    }

    public AbstractHermiTOWLTest(String name) {
        super(name);
    }
    /**
     * loads an ontology via the OWL API
     * @param physicalURI the physical location of the ontology
     * @return the ontology as an OWLAPI ontology object (not simplified, normalised or clausified)
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
     * @param resourceName the relative location of the ontology
     * @return the ontology as an OWLAPI ontology object (not simplified, normalised or clausified)
     * @throws Exception
     */
    protected OWLOntology getOWLOntologyFromResource(String resourceName) throws Exception {
        return getOWLOntology(getClass().getResource(resourceName).toString());
    }
    /**
     * loads an ontology from a relative path via the OWL API
     * @param resourceName the resouce to load
     * @return the set of axioms from the OWLAPI ontology
     * @throws Exception
     */
    protected Set<OWLAxiom> getOWLAxiomsFromResource(String resourceName) throws Exception {
        return getOWLOntologyFromResource(resourceName).getAxioms();
    }
    /**
     * @param resourceName
     * @return each line from the loaded resource becomes a string in the returned array
     * @throws Exception
     */
    protected Set<String> getStrings(String resourceName) throws Exception {
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
     * @param resourceName
     * @return the content of the loaded resource as one string
     * @throws Exception
     */
    protected String getResourceText(String resourceName) throws Exception {
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
     * converts the axioms to a string via the toString method and compares it with the given string
     * @param axioms
     * @param controlResourceName
     * @throws Exception
     */
    protected void assertEquals(Set<OWLAxiom> axioms,String controlResourceName) throws Exception {
        String axiomsString = axioms.toString().trim();
        String controlString = getResourceText(controlResourceName).trim();
    	assertTrue(axiomsString.equals(controlString));
    }
    /**
     * prints the content of conrol set and the actual set in case they 
     * are different and causes a JUnit test failure
     * @param <T>
     * @param actual
     * @param control
     * @throws Exception
     */
    protected <T> void assertEquals(Set<T> actual,Set<T> control) throws Exception {
       if (!actual.equals(control)) {
            System.out.println("Control set ("+control.size()+" elements):");
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
            assertTrue(false);
        }
    }
    /**
     * tests that the set have equal length and that the actual set contains all 
     * objects from the control set, otherwise the test fails and the contents 
     * of the control and the actual set are printed
     * @param <T>
     * @param actual
     * @param control
     */
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
