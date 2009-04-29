package org.semanticweb.HermiT.reasoner;


public class NumericsTest extends AbstractReasonerTest {

    public NumericsTest(String name) {
        super(name);
    }
    public void testIntegerRange1() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:int"),
            NOT(DR("xsd:integer"))
        );
    }
    public void testIntegerRange2() throws Exception {
        assertDRSatisfiable(true,
            NOT(DR("xsd:int")),
            DR("xsd:integer")
        );
        assertDRSatisfiable(false,
            NOT(DR("xsd:int")),
            DR("xsd:integer"),
            OO(INT("0"))
        );
        assertDRSatisfiable(false,
            NOT(DR("xsd:int")),
            DR("xsd:integer"),
            OO(INT("2147483647"))
        );
        assertDRSatisfiable(true,
            NOT(DR("xsd:int")),
            DR("xsd:integer"),
            OO(INT("2147483648"))
        );
    }
    public void testDecimalNotInteger() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:decimal"),
            NOT(DR("xsd:integer")),
            OO(INT("2147483648"))
        );
        assertDRSatisfiable(false,
            DR("xsd:decimal"),
            NOT(DR("xsd:integer")),
            OO(DEC("2147483648.0"))
        );
        assertDRSatisfiable(true,
            DR("xsd:decimal"),
            NOT(DR("xsd:integer")),
            OO(DEC("2147483648.1"))
        );
    }
    public void testRealNotDecimal() throws Exception {
        assertDRSatisfiable(true,
            DR("owl:real"),
            NOT(DR("xsd:decimal")),
            OO(RAT("1","3"))
        );
        assertDRSatisfiable(false,
            DR("owl:real"),
            NOT(DR("xsd:decimal")),
            OO(RAT("5","2"))
        );
    }
    public void testMinInclusiveInt() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2")),
            OO(INT("2"))
        );
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2")),
            OO(INT("3"))
        );
    }
    public void testMinExclusiveDec() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:minExclusive",DEC("2.2")),
            OO(DEC("2.2"))
        );
    }
    public void testMaxInclusiveInt() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:integer","xsd:maxInclusive",DEC("2.2")),
            OO(INT("3"))
        );
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:maxInclusive",DEC("2.2")),
            OO(INT("2"))
        );
    }
    public void testMaxExclusiveDec() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:maxExclusive",DEC("2.2")),
            OO(DEC("2.2"))
        );
    }
    public void testEnumInt() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2")),
            NOT(OO(INT("3"),INT("4")))
        );
        assertDRSatisfiable(false,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2")),
            NOT(OO(INT("3"),INT("4"),INT("5")))
        );
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2")),
            NOT(OO(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")))
        );
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",INT("10000")),
            NOT(OO(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")))
        );
        assertDRSatisfiable(true,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",INT("1000000000000000000000000000000000000000")),
            NOT(OO(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")))
        );
    }
    public void testEnumIntNEQ() throws Exception {
        assertDRSatisfiableNEQ(true,
            S(INT("3"),INT("4")),
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))
        );
        assertDRSatisfiableNEQ(false,
            S(INT("3"),INT("4"),INT("5")),
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))
        );
        assertDRSatisfiableNEQ(true,
            S(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")),
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"))
        );
        assertDRSatisfiableNEQ(true,
            S(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")),
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",INT("10000"))
        );
        assertDRSatisfiableNEQ(true,
            S(INT("3"),INT("4"),INT("5"),INT("6"),INT("7"),INT("8")),
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",INT("1000000000000000000000000000000000000000"))
        );
    }
    public void testMinMaxEqual() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("2.2"))
        );
        assertDRSatisfiable(true,
            DR("xsd:decimal","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("2.2"))
        );
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:minInclusive",RAT("1","3"),"xsd:maxInclusive",RAT("1","3"))
        );
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:minExclusive",DEC("2.2"),"xsd:maxInclusive",DEC("2.2"))
        );
    }
    public void testInvalidMinMax() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:integer","xsd:minInclusive",DEC("5.2"),"xsd:maxInclusive",DEC("2.2"))
        );
    }
    public void testDecimalMinusInt() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            OO(INT("4"))
        );
        assertDRSatisfiable(true,
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int")
        );
        assertDRSatisfiable(true,
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int"),
            NOT(OO(DEC("6.0"),DEC("7.0")))
        );
        assertDRSatisfiable(false,
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int"),
            NOT(OO(INT("2"),DEC("6.0"),DEC("7.0")))
        );
    }
    public void testDecimalMinusIntNEQ() throws Exception {
        assertDRSatisfiableNEQ(true,
            S(DEC("6.0"),DEC("7.0")),
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int")
        );
        assertDRSatisfiableNEQ(true,
            S(DEC("6.0"),DEC("7.0")),
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int")
        );
        assertDRSatisfiableNEQ(false,
            S(INT("2"),DEC("6.0"),DEC("7.0")),
            DR("xsd:decimal","xsd:minInclusive",DEC("1.2"),"xsd:maxInclusive",DEC("7.2")),
            NOT(DR("xsd:integer","xsd:minInclusive",DEC("2.2"),"xsd:maxInclusive",DEC("5.2"))),
            DR("xsd:int")
        );
    }
    public void testLargeRange1() throws Exception {
        assertDRSatisfiable(true,255,
            DR("xsd:byte")
        );
        assertDRSatisfiable(true,256,
            DR("xsd:byte")
        );
        assertDRSatisfiable(false,257,
            DR("xsd:byte")
        );
    }
    public void testLargeRange2() throws Exception {
        assertDRSatisfiable(true,127,
            DR("xsd:byte"),
            DR("xsd:nonNegativeInteger")
        );
        assertDRSatisfiable(true,128,
            DR("xsd:byte"),
            DR("xsd:nonNegativeInteger")
        );
        assertDRSatisfiable(false,129,
            DR("xsd:byte"),
            DR("xsd:nonNegativeInteger")
        );
    }
    public void testClique() throws Exception {
        assertDRSatisfiable(true,2,
            DR("xsd:integer","xsd:minInclusive",INT("1"),"xsd:maxInclusive",INT("2")),
            NOT(OO(INT("3")))
        );
    }
}
