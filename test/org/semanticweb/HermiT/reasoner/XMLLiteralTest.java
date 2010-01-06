package org.semanticweb.HermiT.reasoner;

public class XMLLiteralTest extends AbstractReasonerTest {

    public XMLLiteralTest(String name) {
        super(name);
    }
    public void testCanonicalization_1() throws Exception {
        assertDRSatisfiable(false,
            OO(XMLL("abc<a/>")),
            NOT(OO(XMLL("abc<a></a>")))
        );
    }
    public void testCanonicalization_2() throws Exception {
        assertDRSatisfiable(true,
            OO(XMLL("abc<a/>d")),
            NOT(OO(XMLL("abc<a></a>")))
        );
    }
    public void testRange_1() throws Exception {
        assertDRSatisfiable(true,100,
            DR("rdf:XMLLiteral")
        );
    }
    public void testRange_2() throws Exception {
        assertDRSatisfiable(false,
            DR("rdf:XMLLiteral"),
            NOT(DR("rdf:XMLLiteral"))
        );
    }
    public void testRange_3() throws Exception {
        assertDRSatisfiable(false,
            DR("rdf:XMLLiteral"),
            DR("xsd:boolean")
        );
    }
    public void testMembership_1() throws Exception {
        assertDRSatisfiable(true,
            DR("rdf:XMLLiteral"),
            OO(XMLL("<a>bla</a>"))
        );
    }
    public void testMembership_2() throws Exception {
        assertDRSatisfiable(false,
            NOT(DR("rdf:XMLLiteral")),
            OO(XMLL("<a>bla</a>"))
        );
    }
}
