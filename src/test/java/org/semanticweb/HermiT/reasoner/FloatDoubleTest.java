package org.semanticweb.HermiT.reasoner;

public class FloatDoubleTest extends AbstractReasonerTest {

    public FloatDoubleTest(String name) {
        super(name);
    }
    public void testINF() throws Exception {
        String axioms = "SubClassOf(:A DataAllValuesFrom(:dp owl:real))"
            + "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"-INF\"^^xsd:float \"-0\"^^xsd:integer)))"
            + "ClassAssertion(:A :a)"
            + "NegativeDataPropertyAssertion(:dp :a \"0\"^^xsd:unsignedInt)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    public void testFloatRange() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:float"),
            NOT(DR("xsd:float"))
        );
    }
    public void testFloatZeroRange_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:float","xsd:minInclusive",FLT("+0"),"xsd:maxInclusive",FLT("+0")),
            OO(FLT("-0"))
        );
    }
    public void testFloatZeroRange_2() throws Exception {
        assertDRSatisfiable(true,2,
            DR("xsd:float","xsd:minInclusive",FLT("+0"),"xsd:maxInclusive",FLT("+0"))
        );
    }
    public void testFloatZeroRange_3() throws Exception {
        assertDRSatisfiable(false,3,
            DR("xsd:float","xsd:minInclusive",FLT("+0"),"xsd:maxInclusive",FLT("+0"))
        );
    }
    public void testFloatRangeEnum() throws Exception {
        assertDRSatisfiableNEQ(false,
            S(FLT("-0"),FLT("+0")),
            DR("xsd:float","xsd:minInclusive",FLT("+0"),"xsd:maxInclusive",FLT("+0"))
        );
    }
    public void testFloatNaN_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:float"),
            OO(FLT("NaN"))
        );
    }
    public void testFloatNaN_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:float","xsd:minInclusive",FLT("2.0")),
            OO(FLT("NaN"))
        );
    }
    public void testFloatNaN_3() throws Exception {
        assertDRSatisfiable(true,
            NOT(DR("xsd:float","xsd:minInclusive",FLT("2.0"))),
            OO(FLT("NaN"))
        );
    }
    public void testNumberOfFloats_1() throws Exception {
        assertDRSatisfiable(true,5,
            DR("xsd:float")
        );
    }
    public void testNumberOfFloats_2() throws Exception {
        assertDRSatisfiable(true,5,
            DR("xsd:float","xsd:minInclusive",FLT("1.0"),"xsd:maxInclusive",FLT("1.0000005"))
        );
    }
    public void testNumberOfFloats_3() throws Exception {
        assertDRSatisfiable(false,6,
            DR("xsd:float","xsd:minInclusive",FLT("1.0"),"xsd:maxInclusive",FLT("1.0000005"))
        );
    }
    public void testFloatAndDouble() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:float"),
            DR("xsd:double")
        );
    }
    public void testDoubleRange() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:double"),
            NOT(DR("xsd:double"))
        );
    }
    public void testDoubleZeroRange_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:double","xsd:minInclusive",DBL("+0"),"xsd:maxInclusive",DBL("+0")),
            OO(DBL("-0"))
        );
    }
    public void testDoubleZeroRange_2() throws Exception {
        assertDRSatisfiable(true,2,
            DR("xsd:double","xsd:minInclusive",DBL("+0"),"xsd:maxInclusive",DBL("+0"))
        );
    }
    public void testDoubleZeroRange_3() throws Exception {
        assertDRSatisfiable(false,3,
            DR("xsd:double","xsd:minInclusive",DBL("+0"),"xsd:maxInclusive",DBL("+0"))
        );
    }
    public void testDoubleRangeEnum() throws Exception {
        assertDRSatisfiableNEQ(false,
            S(DBL("-0"),DBL("+0")),
            DR("xsd:double","xsd:minInclusive",DBL("+0"),"xsd:maxInclusive",DBL("+0"))
        );
    }
    public void testDoubleNaN_1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:double"),
            OO(DBL("NaN"))
        );
    }
    public void testDoubleNaN_2() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:double","xsd:minInclusive",DBL("2.0")),
            OO(DBL("NaN"))
        );
    }
    public void testDoubleNaN_3() throws Exception {
        assertDRSatisfiable(true,
            NOT(DR("xsd:double","xsd:minInclusive",DBL("2.0"))),
            OO(DBL("NaN"))
        );
    }
    public void testNumberOfDoubles_1() throws Exception {
        assertDRSatisfiable(true,5,
            DR("xsd:double")
        );
    }
    public void testNumberOfDoubles_2() throws Exception {
        assertDRSatisfiable(true,5,
            DR("xsd:double","xsd:minInclusive",DBL("1.0"),"xsd:maxInclusive",DBL("1.0000000000000009"))
        );
    }
    public void testNumberOfDoubles_3() throws Exception {
        assertDRSatisfiable(false,6,
            DR("xsd:double","xsd:minInclusive",DBL("1.0"),"xsd:maxInclusive",DBL("1.0000000000000009"))
        );
    }
}
