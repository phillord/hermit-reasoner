package org.semanticweb.HermiT;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractOWLOntologyTest extends AbstractHermiTTest {
    protected static final Node[][] NO_TUPLES=new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    protected static final String ONTOLOGY_URI="file:/c/test.owl";
    protected static final String NS=ONTOLOGY_URI+"#";
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

    public AbstractOWLOntologyTest() {
        super();
    }
    public AbstractOWLOntologyTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontology=m_ontologyManager.createOntology(URI.create(ONTOLOGY_URI));
    }

    protected void tearDown() {
        m_ontologyManager=null;
        m_ontology=null;
    }

    /**
     * loads an ontology via the OWL API
     * 
     * @param physicalURI
     *            the physical location of the ontology
     * @throws Exception
     */
    protected void loadOWLOntology(String physicalURI) throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontology=m_ontologyManager.loadOntologyFromPhysicalURI(URI.create(physicalURI));
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
     * @param axioms
     *            in functional style syntax
     * @throws InterruptedException
     * @throws OWLException
     */
    protected void loadOWLOntologyWithAxioms(String axioms) throws OWLException,InterruptedException {
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

    /**
     * converts the axioms to a string via the toString method and compares it with the given string
     */
    protected void assertEquals(Set<OWLAxiom> axioms,String controlResourceName) throws Exception {
        String axiomsString=axioms.toString().trim();
        String controlString=getResourceText(controlResourceName).trim();
        boolean isOK=axiomsString.equals(controlString);
        if (!isOK) {
            System.out.println("Test "+this.getName()+" failed!");
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
     * converts the axioms to a string via the toString method and compares it with the given string
     */
    protected void assertEquals(Set<OWLAxiom> axioms,String controlResourceName1,String controlResourceName2) throws Exception {
        String axiomsString=axioms.toString().trim();
        String controlString1=null;
        String controlString2=null;
        boolean isOK=false;
        if (!isOK && controlResourceName1!=null) {
            controlString1=getResourceText(controlResourceName1).trim();
            isOK=axiomsString.equals(controlString1);
        }
        axiomsString.equals(controlString1);
        if (!isOK && controlResourceName2!=null) {
            controlString2=getResourceText(controlResourceName2).trim();
            isOK=axiomsString.equals(controlString2);
        }
        if (!isOK) {
            System.out.println("Test "+this.getName()+" failed!");
            if (controlString1!=null) {
                System.out.println("Control string: (variant 1)");
                System.out.println("------------------------------------------");
                System.out.println(controlString1);
                System.out.println("------------------------------------------");
            }
            if (controlString2!=null) {
                System.out.println("Control string (variant 2):");
                System.out.println("------------------------------------------");
                System.out.println(controlString2);
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
     * prints the content of control set and the actual set in case they are different and causes a JUnit test failure
     */
    protected <T> void assertEquals(Set<T> actual,Set<T> control) throws Exception {
        if (!actual.equals(control)) {
            System.out.println("Test "+this.getName()+" failed!");
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
}
