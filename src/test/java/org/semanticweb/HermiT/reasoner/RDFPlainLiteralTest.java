package org.semanticweb.HermiT.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.datatypes.rdfplainliteral.RDFPlainLiteralDataValue;
import org.semanticweb.HermiT.datatypes.rdfplainliteral.RDFPlainLiteralLengthInterval;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class RDFPlainLiteralTest extends AbstractReasonerTest {
    protected static final String RDF_PLAIN_LITERAL=Prefixes.s_semanticWebPrefixes.get("rdf:")+"PlainLiteral";
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");

    public RDFPlainLiteralTest(String name) {
        super(name);
    }
    public void testInvalidStringLiterals() throws Exception {
        assertEquals("blah",DatatypeRegistry.parseLiteral("blah@",RDF_PLAIN_LITERAL));
        assertEquals(new RDFPlainLiteralDataValue("blah","en"),DatatypeRegistry.parseLiteral("blah@en",RDF_PLAIN_LITERAL));
        assertEquals("blah blah",DatatypeRegistry.parseLiteral("blah blah",XSD_NS+"token"));
        try {
            DatatypeRegistry.parseLiteral("abc@123",RDF_PLAIN_LITERAL);
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral("\u0002blah@en",RDF_PLAIN_LITERAL);
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral(" blah@en",XSD_NS+"token");
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral("blah  blah@en",XSD_NS+"token");
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
    }
    public void testLength_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:length",INT("2")),
            OO(STR("ab"))
        );
    }
    public void testLength_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:length",INT("3")),
            OO(STR("ab"))
        );
    }
    public void testLength_3() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("ab"))
        );
    }
    public void testLength_4() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("abcdef"))
        );
    }
    public void testLength_5() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("abcde"))
        );
    }
    public void testSize_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:length",INT("0"))
        );
    }
    public void testSize_2() throws Exception {
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:length",INT("0"))
        );
    }
    public void testSize_3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:length",INT("0")),
            NOT(OO(STR("")))
        );
    }
    public void testIntersection_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("0")),
            NOT(DR("xsd:string","xsd:minLength",INT("1")))
        );
    }
    public void testIntersection_2() throws Exception {
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:minLength",INT("0")),
            NOT(DR("xsd:string","xsd:minLength",INT("1")))
        );
    }
    public void testExplicitSize() throws Exception {
        RDFPlainLiteralLengthInterval imax2=interval(0,1);
        assertEquals(10000000,imax2.subtractSizeFrom(10000000+1+RDFPlainLiteralLengthInterval.CHARACTER_COUNT));
        RDFPlainLiteralLengthInterval imin1max2=interval(1,1);
        assertEquals(10000000,imin1max2.subtractSizeFrom(10000000+RDFPlainLiteralLengthInterval.CHARACTER_COUNT));
    }
    public void testEnumerate() throws Exception {
        RDFPlainLiteralLengthInterval imax1=interval(0,0);
        List<Object> values=new ArrayList<Object>();
        imax1.enumerateValues(values);
        List<Object> control=new ArrayList<Object>();
        control.add("");
        assertContainsAll(values,control.toArray());
    }
    protected static RDFPlainLiteralLengthInterval interval(int minLength,int maxLength) {
        return new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode .ABSENT,minLength,maxLength);
    }
    public void testPattern1_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)")),
            OO(STR("abc"),STR("abbb"))
        );
    }
    public void testPattern1_2() throws Exception {
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:pattern",STR("ab(c+)")),
            OO(STR("abc"),STR("abbb"))
        );
    }
    public void testPattern2_1() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)"))
        );
    }
    public void testPattern2_2() throws Exception {
        assertDRSatisfiable(false,4,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)"))
        );
    }
    public void testPattern3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)")),
            NOT(OO(STR("abc"),STR("abd"),STR("abe")))
        );
    }
    public void testPatternAndLength1_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:length",INT("5"))
        );
    }
    public void testPatternAndLength1_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4"),"xsd:maxLength",INT("5")),
            NOT(OO(STR("abcc"),STR("abccc")))
        );
    }
    public void testPatternAndLength1_3() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4")),
            NOT(OO(STR("abcc"),STR("abccc")))
        );
    }
    public void testPatternAndLength2() throws Exception {
        ValueSpaceSubset subset=subset("xsd:string","xsd:pattern",STR_C("ab(c+)"),"xsd:minLength",INT_C("5"));
        assertTrue(subset.hasCardinalityAtLeast(5000));
        try {
            subset.enumerateDataValues(new ArrayList<Object>());
            fail();
        }
        catch (Exception expected) {
        }
        assertFalse(subset.containsDataValue("ab"));
        assertTrue(subset.containsDataValue("abccccccccccc"));
    }
    public void testPatternAndLength3() throws Exception {
        ValueSpaceSubset subset=subset("xsd:string","xsd:pattern",STR_C("ab(c+)"),"xsd:minLength",INT_C("5"),"xsd:maxLength",INT_C("10"));
        assertTrue(subset.hasCardinalityAtLeast(6));
        assertFalse(subset.hasCardinalityAtLeast(7));
        Set<Object> values=new HashSet<Object>();
        subset.enumerateDataValues(values);
        assertContainsAll(values,"abccc","abcccc","abccccc","abcccccc","abccccccc","abcccccccc");
    }
    public void testPatternComplement1_1() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5")))
        );
    }
    public void testPatternComplement1_2() throws Exception {
        assertDRSatisfiable(false,4,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5")))
        );
    }
    public void testPatternComplement1_3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5"))),
            NOT(OO(STR("ab"),STR("abc"),STR("abcc")))
        );
    }
    public void testComplement2() throws Exception {
        ValueSpaceSubset main=subset("xsd:string","xsd:pattern",STR_C("ab(c*)"));
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",INT_C("5"));
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertTrue(intersection.hasCardinalityAtLeast(3));
        assertFalse(intersection.hasCardinalityAtLeast(4));
        Set<Object> values=new HashSet<Object>();
        intersection.enumerateDataValues(values);
        assertContainsAll(values,"ab","abc","abcc");
    }
    public void testComplement3() throws Exception {
        ValueSpaceSubset main=subset("rdf:PlainLiteral");
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",INT_C("5"));
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue("abcde"));
        assertTrue(intersection.containsDataValue("abcd"));
        assertTrue(intersection.containsDataValue(new RDFPlainLiteralDataValue("abcdefgh","en")));
        assertFalse(intersection.containsDataValue(new RDFPlainLiteralDataValue("abcdefgh","123")));
    }
    public void testComplement4() throws Exception {
        ValueSpaceSubset main=subset("rdf:PlainLiteral","xsd:pattern",STR_C("a+"));
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",INT_C("5"));
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue("aaaaa"));
        assertTrue(intersection.containsDataValue("aaaa"));
        assertTrue(intersection.containsDataValue(new RDFPlainLiteralDataValue("aaaaaaaa","en")));
        assertFalse(intersection.containsDataValue(new RDFPlainLiteralDataValue("aaaaaaaa","123")));
    }
    public void testLangRange1() throws Exception {
        ValueSpaceSubset main=subset("rdf:PlainLiteral","rdf:langRange",STR_C("en"));
        assertFalse(main.containsDataValue("abc"));
        assertFalse(main.containsDataValue(new RDFPlainLiteralDataValue("abc","de")));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","en")));
        assertFalse(main.containsDataValue(new RDFPlainLiteralDataValue("abc","enn")));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","en-us")));
    }
    public void testLangRange2() throws Exception {
        ValueSpaceSubset main=subset("rdf:PlainLiteral","rdf:langRange",STR_C("*"));
        assertFalse(main.containsDataValue("abc"));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","de")));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","en")));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","enn")));
        assertTrue(main.containsDataValue(new RDFPlainLiteralDataValue("abc","en-us")));
    }
    protected static DatatypeRestriction restriction(String datatypeURI,Object... arguments) {
        String[] facetURIs=new String[arguments.length/2];
        Constant[] facetValues=new Constant[arguments.length/2];
        for (int index=0;index<arguments.length;index+=2) {
            facetURIs[index/2]=Prefixes.STANDARD_PREFIXES.expandAbbreviatedIRI((String)arguments[index]);
            facetValues[index/2]=(Constant)arguments[index+1];
        }
        return DatatypeRestriction.create(Prefixes.STANDARD_PREFIXES.expandAbbreviatedIRI(datatypeURI),facetURIs,facetValues);
    }
    protected static ValueSpaceSubset subset(String datatypeURI,Object... arguments) {
        DatatypeRestriction restriction=restriction(datatypeURI,arguments);
        return DatatypeRegistry.createValueSpaceSubset(restriction);
    }
}
