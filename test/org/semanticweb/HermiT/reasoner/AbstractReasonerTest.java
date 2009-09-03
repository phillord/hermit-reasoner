package org.semanticweb.HermiT.reasoner;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.EntailmentChecker;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

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
        m_ontology=m_ontologyManager.loadOntologyFromPhysicalURI(physicalURI);
        createReasoner();
    }

    protected void loadReasonerWithAxioms(String axioms) throws Exception {
        loadOntologyWithAxioms(axioms);
        createReasoner();
    }
    protected void createReasoner() {
        createReasoner(getConfiguration(),null);
    }
    protected void createReasoner(Configuration configuration,Set<DescriptionGraph> descriptionGraphs) {
        if (descriptionGraphs==null)
            descriptionGraphs=Collections.emptySet();
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_ontology,descriptionGraphs);
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
        OWLClass subClass=m_dataFactory.getOWLClass(IRI.create(subAtomicConcept));
        OWLClass superClass=m_dataFactory.getOWLClass(IRI.create(superAtomicConcept));
        boolean result=m_reasoner.isSubClassOf(subClass,superClass);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the possibly complex concept subConcept is subsumed by the possibly complex concept superConcept and asserts that this coincides with the expected result.
     */
    protected void assertSubsumedBy(OWLClassExpression subConcept,OWLClassExpression superConcept,boolean expectedResult) {
        boolean result=m_reasoner.isSubClassOf(subConcept,superConcept);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the atomic concept atomicConcept is satisfiable and asserts that this coincides with the expected result (satisfiable).
     */
    protected void assertSatisfiable(String atomicConcept,boolean expectedResult) {
        if (!atomicConcept.contains("#"))
            atomicConcept=NS+atomicConcept;
        OWLClass clazz=m_dataFactory.getOWLClass(IRI.create(atomicConcept));
        boolean result=m_reasoner.isSatisfiable(clazz);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the given possibly complex concept is satisfiable and asserts that this coincides with the expected result (satisfiable).
     */
    protected void assertSatisfiable(OWLClassExpression concept,boolean satisfiable) throws Exception {
        assertEquals(satisfiable,m_reasoner.isSatisfiable(concept));
    }

    /**
     * Tests whether the given individual is an instance of the given concept and asserts that this coincides with the expected result.
     */
    protected void assertInstanceOf(OWLClassExpression concept,OWLNamedIndividual individual,boolean expectedResult) {
        boolean result=m_reasoner.hasType(individual,concept,false);
        assertEquals(expectedResult,result);
    }

    /**
     * Tests whether the given concept has the specified individuals.
     */
    protected void assertInstancesOf(OWLClassExpression concept,boolean direct,String... expectedIndividuals) {
        Set<OWLNamedIndividual> actual=m_reasoner.getIndividuals(concept,direct);
        Set<String> actualIndividualIRIs=new HashSet<String>();
        for (OWLIndividual individual : actual) {
            if (!individual.isAnonymous()) 
                actualIndividualIRIs.add(individual.asNamedIndividual().getIRI().toString());
        }
        String[] expectedModified=expectedIndividuals.clone();
        assertContainsAll(actualIndividualIRIs,expectedModified);
    }
    
    /**
     * Checks the superproperties of some object property.
     */
    protected void assertSuperObjectProperties(String objectProperty,Set<String>... control) {
        if (!objectProperty.contains("#"))
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_dataFactory.getOWLObjectProperty(IRI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSuperProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some object property.
     */
    protected void assertSubObjectProperties(String objectProperty,Set<String>... control) {
        if (!objectProperty.contains("#"))
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_dataFactory.getOWLObjectProperty(IRI.create(objectProperty));
        Set<Set<String>> actual=setOfSetsOfOPEsToStrings(m_reasoner.getSubProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some object property.
     */
    protected void assertEquivalentObjectProperties(String objectProperty,String... control) {
        if (!objectProperty.contains("#"))
            objectProperty=NS+objectProperty;
        OWLObjectPropertyExpression ope=m_dataFactory.getOWLObjectProperty(IRI.create(objectProperty));
        Set<String> actual=setOfOPEsToStrings(m_reasoner.getEquivalentProperties(ope));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the superproperties of some data property.
     */
    protected void assertSuperDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
        OWLDataProperty dp=m_dataFactory.getOWLDataProperty(IRI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSuperProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the subproperties of some data property.
     */
    protected void assertSubDataProperties(String dataProperty,Set<String>... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
        OWLDataProperty dp=m_dataFactory.getOWLDataProperty(IRI.create(dataProperty));
        Set<Set<String>> actual=setOfSetsOfDPsToStrings(m_reasoner.getSubProperties(dp));
        assertContainsAll(actual,control);
    }
    
    /**
     * Checks the equivalents of some data property.
     */
    protected void assertEquivalentDataProperties(String dataProperty,String... control) {
        if (!dataProperty.contains("#"))
            dataProperty=NS+dataProperty;
        OWLDataProperty dp=m_dataFactory.getOWLDataProperty(IRI.create(dataProperty));
        Set<String> actual=setOfDPsToStrings(m_reasoner.getEquivalentProperties(dp));
        assertContainsAll(actual,control);
    }
    
    protected void assertEntails(OWLAxiom axiom, boolean expectedResult) {
        assertTrue(new EntailmentChecker(m_reasoner, m_dataFactory).entails(axiom)==expectedResult);
    }
    
    protected void assertEntails(Set<OWLLogicalAxiom> axioms, boolean expectedResult) {
        assertTrue(new EntailmentChecker(m_reasoner, m_dataFactory).entails(axioms)==expectedResult);
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
                translatedSet.add(((OWLObjectProperty)ope).getIRI().toString());
            else {
                OWLObjectProperty innerOp=ope.getNamedProperty();
                translatedSet.add("(inv "+innerOp.getIRI().toString()+")");
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
            translatedSet.add(dp.getIRI().toString());
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
        // The dummy forbidden value is used to turn off the "symmetric clique" optimization in the DatatypeManager.
        // This is useful only when value>=1 (otherwise, there is just one individual and the D-conjunction is a clique).
        // This is so that we test the basic algorithm.
        assertDRSatisfiableNEQ(value,cardinality,new String[] { STR("$internal$") },parts);
    }

    protected void assertDRSatisfiableUseCliqueOptimization(boolean value,int cardinality,String... parts) throws Exception {
        // This version of the method does not introduce any forbidden values, which allows the DatatypeManager
        // to use the "symmetric clique" optimization if possible.
        assertDRSatisfiableNEQ(value,cardinality,null,parts);
    }

    protected void assertDRSatisfiableNEQ(boolean value,String[] forbiddenValues,String... parts) throws Exception {
        assertDRSatisfiableNEQ(value,1,forbiddenValues,parts);
    }
    
    protected void assertDRSatisfiableNEQ(boolean value,int cardinality,String[] forbiddenValues,String... parts) throws Exception {
        StringBuffer buffer=new StringBuffer();
        buffer.append("Declaration(NamedIndividual(test:a)) Declaration(Class(test:A)) Declaration(DataProperty(test:dp)) SubClassOf( test:A DataMinCardinality( ");
        buffer.append(cardinality);
        buffer.append(" test:dp rdfs:Literal ) ) ");
        for (String part : parts) {
            buffer.append("SubClassOf( test:A DataAllValuesFrom( test:dp ");
            buffer.append(part);
            buffer.append(" ) ) ");
        }
        buffer.append("ClassAssertion( test:A test:a ) ");
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
    
    protected void assertRegular(String axioms, boolean b) throws Exception {
    	boolean regular = true;
    	try{
    		loadReasonerWithAxioms(axioms);
    	}catch(IllegalArgumentException e){
    		if( e.getMessage().contains( "The given role hierarchy is not regular" ) )
    			regular = false;
    		else{
    			throw new Exception( e.getMessage() );
    		}
    	}
    	assertEquals(regular,b);
	}
    protected void assertSimple(String axioms, boolean b) throws Exception {
    	boolean simple = true;
    	try{
    		loadReasonerWithAxioms(axioms);
    	}catch(IllegalArgumentException e){
    		if( e.getMessage().contains( "Non simple role '" ) )
    			simple = false;
    		else{
    			throw new Exception( e.getMessage() );
    		}
    	}
    	assertEquals(simple,b);
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

    protected static String[] IRIs(String... args) {
        for (int index=0;index<args.length;index++)
            args[index]=IRI(args[index]);
        return args;
    }

    protected static String IRI(String arg) {
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

    protected static String HEXB(String value) {
        return '\"'+value+"\"^^xsd:hexBinary";
    }
    
    protected static String B64B(String value) {
        return '\"'+value+"\"^^xsd:base64Binary";
    }

    protected static String STR(String value) {
        return '\"'+value+"\"^^xsd:string";
    }

    protected static String STR(String value,String languageTag) {
        return '\"'+value+"\"@"+languageTag;
    }

    protected static String AURI(String value) {
        return '\"'+value+"\"^^xsd:anyURI";
    }

    protected static String[] S(String... args) {
        return args;
    }
}
