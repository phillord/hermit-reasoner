package org.semanticweb.HermiT;

import java.util.Collection;
import java.util.Set;

import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractOntologyTest extends AbstractHermiTTest {
    protected static final IRI ONTOLOGY_IRI=IRI.create("file:/c/test.owl");
    protected static final String NS=ONTOLOGY_IRI+"#";

    protected OWLOntologyManager m_ontologyManager;
    protected OWLDataFactory m_dataFactory;
    protected OWLOntology m_ontology;

    public AbstractOntologyTest() {
        super();
    }
    public AbstractOntologyTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_dataFactory=m_ontologyManager.getOWLDataFactory();
    }

    protected void tearDown() {
        m_ontologyManager=null;
        m_dataFactory=null;
        m_ontology=null;
    }

    protected DLOntology getDLOntology() {
        return getDLOntology(null);
    }

    protected DLOntology getDLOntology(Collection<DescriptionGraph> descriptionGraphs) {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        return (DLOntology)clausifier.preprocessAndClausify(m_ontology,descriptionGraphs)[1];
    }

    protected Tableau getTableau(Collection<DescriptionGraph> descriptionGraphs) throws Exception {
        DLOntology dlOntology = getDLOntology(descriptionGraphs);
        Configuration c=new Configuration();
        c.blockingSignatureCacheType=Configuration.BlockingSignatureCacheType.CACHED;
        c.blockingStrategyType=Configuration.BlockingStrategyType.ANYWHERE;
        c.directBlockingType=Configuration.DirectBlockingType.PAIR_WISE;
        c.existentialStrategyType=Configuration.ExistentialStrategyType.CREATION_ORDER;
        Reasoner reasoner=new Reasoner(c,dlOntology);
        return reasoner.m_tableau;
    }

    /**
     * loads an ontology via the OWL API
     *
     * @param physicalURI
     *            the physical location of the ontology
     * @throws Exception
     */
    protected void loadOntology(String physicalURI) throws Exception {
        m_ontology=m_ontologyManager.loadOntologyFromOntologyDocument(IRI.create(physicalURI));
    }

    /**
     * Loads an ontology from a relative path.
     */
    protected void loadOntologyFromResource(String resourceName) throws Exception {
        loadOntology(getClass().getResource(resourceName).toString());
    }

    /**
     * loads an OWL ontology that contains the given axioms
     */
    protected void loadOntologyWithAxioms(String axioms) throws Exception {
        StringBuffer buffer=new StringBuffer();
        buffer.append("Prefix(:=<"+NS+">)");
        buffer.append("Prefix(a:=<"+NS+">)");
        buffer.append("Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)");
        buffer.append("Prefix(owl2xml:=<http://www.w3.org/2006/12/owl2-xml#>)");
        buffer.append("Prefix(test:=<"+NS+">)");
        buffer.append("Prefix(owl:=<http://www.w3.org/2002/07/owl#>)");
        buffer.append("Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)");
        buffer.append("Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
        buffer.append("Ontology(<"+ONTOLOGY_IRI+">");
        buffer.append(axioms);
        buffer.append(")");
        OWLOntologyDocumentSource input=new StringDocumentSource(buffer.toString());
        m_ontology=m_ontologyManager.loadOntologyFromOntologyDocument(input);
    }

    /**
     * Converts the axioms to a string via the toString method and compares it with the given string.
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
