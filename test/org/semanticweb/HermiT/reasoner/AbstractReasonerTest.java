package org.semanticweb.HermiT.reasoner;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLHasKeyDummy;

public abstract class AbstractReasonerTest extends AbstractOntologyTest {
    protected Reasoner m_reasoner;

    public AbstractReasonerTest(String name) {
        super(name);
    }

    protected void tearDown() {
        super.tearDown();
        m_reasoner=null;
    }

    protected void loadReasonerFromResource(String resourceName) throws Exception {
        URI physicalURI=getClass().getResource(resourceName).toURI();
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_ontology=m_ontologyManager.loadOntologyFromPhysicalURI(physicalURI);
        createReasoner();
    }

    protected void loadReasonerWithAxioms(String axioms) throws Exception {
        loadOntologyWithAxioms(axioms);
        createReasoner();
    }

    protected void createReasoner() {
        createReasoner(getConfiguration(),null,null);
    }

    protected void createReasoner(Configuration configuration,Set<DescriptionGraph> descriptionGraphs,Set<OWLHasKeyDummy> keys) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        if (keys==null)
            keys=Collections.emptySet();
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology,descriptionGraphs,keys);
    }

    /**
     * Returns the class and the property hierarchies as text.
     */
    protected String getHierarchiesAsText() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        m_reasoner.printHierarchies(output,true,true,true);
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
     */
    protected void assertHierarchies(String controlResource) throws Exception {
        String taxonomy=getHierarchiesAsText();
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
     *            a string that represents an atomic concept. If no prefix is given, NS is used as prefix
     * @param superAtomicConcept
     *            a string that represents an atomic concept. If no prefix is given, NS is used as prefix
     * @param expectedResult
     */
    protected void assertSubsumedBy(String subAtomicConcept,String superAtomicConcept,boolean expectedResult) {
        if (!subAtomicConcept.contains("#"))
            subAtomicConcept=NS+subAtomicConcept;
        if (!superAtomicConcept.contains("#"))
            superAtomicConcept=NS+superAtomicConcept;
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
            atomicConcept=NS+atomicConcept;
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
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSuperProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some object property.
     */
    protected void assertSubObjectProperties(String objectProperty,Set<String>... control) {
        if (!objectProperty.contains("#"))
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSubProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some object property.
     */
    protected void assertEquivalentObjectProperties(String objectProperty,String... control) {
        if (!objectProperty.contains("#"))
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_ontologyManager.getOWLDataFactory().getOWLObjectProperty(URI.create(objectProperty));
        Set<String> actual=setOfOPEsToStrings(m_reasoner.getEquivalentProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the superproperties of some data property.
     */
    protected void assertSuperDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
        OWLDataProperty dp=m_ontologyManager.getOWLDataFactory().getOWLDataProperty(URI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSuperProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some data property.
     */
    protected void assertSubDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
        OWLDataProperty dp=m_ontologyManager.getOWLDataFactory().getOWLDataProperty(URI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSubProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some data property.
     */
    protected void assertEquivalentDataProperties(String dataProperty,String... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
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

    protected void assertDRSatisfiable(boolean value,String... parts) throws Exception {
        assertDRSatisfiableNEQ(value,1,null,parts);
    }

    protected void assertDRSatisfiable(boolean value,int cardinality,String... parts) throws Exception {
        assertDRSatisfiableNEQ(value,cardinality,null,parts);
    }

    protected void assertDRSatisfiableNEQ(boolean value,String[] forbiddenValues,String... parts) throws Exception {
        assertDRSatisfiableNEQ(value,1,forbiddenValues,parts);
    }
    
    protected void assertDRSatisfiableNEQ(boolean value,int cardinality,String[] forbiddenValues,String... parts) throws Exception {
        StringBuffer buffer=new StringBuffer();
        buffer.append("SubClassOf( test:A DataMinCardinality( ");
        buffer.append(cardinality);
        buffer.append(" test:dp rdfs:Literal ) ) ");
        for (String part : parts) {
            buffer.append("SubClassOf( test:A DataAllValuesFrom( test:dp ");
            buffer.append(part);
            buffer.append(" ) ) ");
        }
        buffer.append("ClassAssertion( test:a test:A ) ");
        if (forbiddenValues!=null) {
            int index=0;
            for (String forbiddenValue : forbiddenValues) {
                String fvName="test:fv"+index;
                buffer.append("DisjointDataProperties( test:dp ");
                buffer.append(fvName);
                buffer.append(" ) ");
                buffer.append("DataPropertyAssertion( ");
                buffer.append(fvName);
                buffer.append(" test:a ");
                buffer.append(forbiddenValue);
                buffer.append(" ) ");
                index++;
            }
        }
        loadReasonerWithAxioms(buffer.toString());
        assertABoxSatisfiable(value);
    }

    protected static Set<String> EQ(String... args) {
        Set<String> result=new HashSet<String>();
        for (String arg : args) {
            if (!arg.contains("#"))
                arg=NS+arg;
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
            arg=NS+arg;
        return arg;
    }

    protected static String INV(String arg) {
        if (!arg.contains("#"))
            arg=NS+arg;
        return "(inv "+arg+")";
    }

    protected static String NOT(String argument) {
        return "DataComplementOf( "+argument+" )";
    }

    protected static String DR(String datatype,String... restrictions) {
        StringBuffer buffer=new StringBuffer();
        if (restrictions.length==0)
            buffer.append(datatype);
        else {
            buffer.append("DatatypeRestriction( ");
            buffer.append(datatype);
            for (String restriction : restrictions) {
                buffer.append(' ');
                if (restriction.startsWith("xsd:"))
                    buffer.append(restriction.substring(4));
                else
                    buffer.append(restriction);
            }
            buffer.append(" )");
        }
        return buffer.toString();
    }

    protected static String OO(String... elements) {
        StringBuffer buffer=new StringBuffer();
        buffer.append("DataOneOf(");
        for (String element : elements) {
            buffer.append(' ');
            buffer.append(element);
        }
        buffer.append(" )");
        return buffer.toString();
    }

    protected static String INT(String value) {
        return '\"'+value+"\"^^xsd:integer";
    }

    protected static String DEC(String value) {
        return '\"'+value+"\"^^xsd:decimal";
    }

    protected static String RAT(String num,String denom) {
        return '\"'+num+"/"+denom+"\"^^owl:rational";
    }

    protected static String FLT(String value) {
        return '\"'+value+"\"^^xsd:float";
    }

    protected static String DBL(String value) {
        return '\"'+value+"\"^^xsd:double";
    }

    protected static String DATE(String value) {
        return '\"'+value+"\"^^xsd:dateTime";
    }

    protected static String DATES(String value) {
        return '\"'+value+"\"^^xsd:dateTimeStamp";
    }

    protected static String XMLL(String value) {
        return '\"'+value+"\"^^rdf:XMLLiteral";
    }

    protected static String[] S(String... args) {
        return args;
    }
}
