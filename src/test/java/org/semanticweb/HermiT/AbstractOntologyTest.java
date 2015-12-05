package org.semanticweb.HermiT;

import java.util.Collection;
import java.util.Set;

import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractOntologyTest extends AbstractHermiTTest {
    protected static final IRI ONTOLOGY_IRI=IRI.create("file:/c/test.owl");
    protected static final String NS=ONTOLOGY_IRI+"#";
    public static String LB=System.getProperty("line.separator");

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
    protected Tableau getTableau(Collection<DescriptionGraph> descriptionGraphs) throws Exception {
        Configuration c=new Configuration();
        c.blockingSignatureCacheType=Configuration.BlockingSignatureCacheType.CACHED;
        c.blockingStrategyType=Configuration.BlockingStrategyType.ANYWHERE;
        c.directBlockingType=Configuration.DirectBlockingType.PAIR_WISE;
        c.existentialStrategyType=Configuration.ExistentialStrategyType.CREATION_ORDER;
        Reasoner reasoner=new Reasoner(c,m_ontology,descriptionGraphs);
        return reasoner.getTableau();
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
        buffer.append("Prefix(:=<"+NS+">)"+LB);
        buffer.append("Prefix(a:=<"+NS+">)"+LB);
        buffer.append("Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)"+LB);
        buffer.append("Prefix(owl2xml:=<http://www.w3.org/2006/12/owl2-xml#>)"+LB);
        buffer.append("Prefix(test:=<"+NS+">)"+LB);
        buffer.append("Prefix(owl:=<http://www.w3.org/2002/07/owl#>)"+LB);
        buffer.append("Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)"+LB);
        buffer.append("Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)"+LB);
        buffer.append("Ontology(<"+ONTOLOGY_IRI+">"+LB);
        buffer.append(axioms+LB);
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
    protected OWLClass C(String uri) {
        return m_dataFactory.getOWLClass(IRI.create(uri));
    }
    protected OWLClass NS_C(String suffix) {
        return C(NS+suffix);
    }
    protected OWLDatatype DT(String uri) {
        return m_dataFactory.getOWLDatatype(IRI.create(uri));
    }
    protected OWLDatatype NS_DT(String suffix) {
        return DT(NS+suffix);
    }
    protected OWLObjectProperty OP(String uri) {
        return m_dataFactory.getOWLObjectProperty(IRI.create(uri));
    }
    protected OWLObjectProperty NS_OP(String suffix) {
        return OP(NS+suffix);
    }
    protected OWLDataProperty DP(String uri) {
        return m_dataFactory.getOWLDataProperty(IRI.create(uri));
    }
    protected OWLDataProperty NS_DP(String suffix) {
        return DP(NS+suffix);
    }
    protected OWLNamedIndividual NI(String uri) {
        return m_dataFactory.getOWLNamedIndividual(IRI.create(uri));
    }
    protected OWLNamedIndividual NS_NI(String suffix) {
        return NI(NS+suffix);
    }
    protected OWLLiteral SL(String lexicalForm) {
        return m_dataFactory.getOWLLiteral(lexicalForm);
    }
    protected OWLLiteral PL(String lexicalForm,String languageTag) {
        return m_dataFactory.getOWLLiteral(lexicalForm,languageTag);
    }
    protected OWLLiteral TL(String lexicalForm,String datatypeURI) {
        return m_dataFactory.getOWLLiteral(lexicalForm,m_dataFactory.getOWLDatatype(IRI.create(Prefixes.STANDARD_PREFIXES.expandAbbreviatedIRI(datatypeURI))));
    }
    protected OWLAnonymousIndividual AI(String id) {
        return m_dataFactory.getOWLAnonymousIndividual(id);
    }
    protected OWLObjectSomeValuesFrom SVF(OWLObjectPropertyExpression objectPropertyExpression,OWLClassExpression classExpression) {
        return m_dataFactory.getOWLObjectSomeValuesFrom(objectPropertyExpression,classExpression);
    }
    protected OWLObjectAllValuesFrom AVF(OWLObjectPropertyExpression objectPropertyExpression,OWLClassExpression classExpression) {
        return m_dataFactory.getOWLObjectAllValuesFrom(objectPropertyExpression,classExpression);
    }
    protected OWLDataSomeValuesFrom SVF(OWLDataProperty dataProperty,OWLDataRange dataRange) {
        return m_dataFactory.getOWLDataSomeValuesFrom(dataProperty,dataRange);
    }
    protected OWLDataAllValuesFrom AVF(OWLDataProperty dataProperty,OWLDataRange dataRange) {
        return m_dataFactory.getOWLDataAllValuesFrom(dataProperty,dataRange);
    }
}
