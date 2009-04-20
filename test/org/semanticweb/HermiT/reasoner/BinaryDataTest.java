package org.semanticweb.HermiT.reasoner;

public class BinaryDataTest extends AbstractReasonerTest {

    public BinaryDataTest(String name) {
        super(name);
    }
    public void testCanonicalization() throws Exception {
        assertDRSatisfiable(false,
            OO(XMLL("abc<a/>")),
            NOT(OO(XMLL("abc<a></a>")))
        );
        assertDRSatisfiable(true,
            OO(XMLL("abc<a/>d")),
            NOT(OO(XMLL("abc<a></a>")))
        );
    }
    public void testRange() throws Exception {
        assertDRSatisfiable(true,100,
            DR("rdf:XMLLiteral")
        );
        assertDRSatisfiable(false,
            DR("rdf:XMLLiteral"),
            NOT(DR("rdf:XMLLiteral"))
        );
        assertDRSatisfiable(false,
            DR("rdf:XMLLiteral"),
            DR("xsd:boolean")
        );
    }
    public void testMembership() throws Exception {
        assertDRSatisfiable(true,
            DR("rdf:XMLLiteral"),
            OO(XMLL("<a>bla</a>"))
        );
        assertDRSatisfiable(false,
            NOT(DR("rdf:XMLLiteral")),
            OO(XMLL("<a>bla</a>"))
        );
    }
}
