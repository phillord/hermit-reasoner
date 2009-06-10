package org.semanticweb.HermiT.structural;

import java.util.List;

public class ClausificationDatatypesTest extends AbstractStructuralTest {

    public ClausificationDatatypesTest(String name) {
        super(name);
    }
    
    public void testDataPropertiesHasValue1() throws Exception {
        String axioms="Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(:Eighteen DataHasValue(:hasAge \"18\"^^xsd:integer))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :hasAge { \"18\"^^xsd:int })(X) :- :Eighteen(X)")
        );
    }

    public void testDataPropertiesHasValue2() throws Exception {
        String axioms="Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(DataHasValue(:hasAge \"18\"^^xsd:integer) :Eighteen)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":Eighteen(X) v not({ \"18\"^^xsd:int })(Y) :- :hasAge(X,Y)")
        );
    }

    public void testDataPropertiesAll1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("xsd:integer(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesAll2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp xsd:integer) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not(xsd:integer))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesSome1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataSomeValuesFrom(:dp xsd:string) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:string)(Y) :- :dp(X,Y)")
        );
    }

    public void testDataPropertiesSome2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataSomeValuesFrom(:dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesDataOneOf1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"Peter\" \"19\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataOneOf2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not({ \"19\"^^xsd:int \"18\"^^xsd:int }))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesDataOneOf3() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not({ \"18\"^^xsd:int \"abc\" }))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesDataOneOf4() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"18\"^^xsd:int \"abc\" }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataComplementOf1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)))))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"19\"^^xsd:int \"18\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataComplementOf2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer))))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not({ \"19\"^^xsd:int \"18\"^^xsd:int })(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesMax1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(1 :dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("Y1 == Y2 v not(xsd:string)(Y1) v not(xsd:string)(Y2) :- :A(X), :dp(X,Y1), :dp(X,Y2)")
        );
    }

    public void testDataPropertiesMax2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(1 :dp xsd:string) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(2 :dp xsd:string)(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesMax3() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMaxCardinality(3 :dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)")
        );
    }

    public void testDataPropertiesMax4() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMaxCardinality(3 :dp xsd:integer) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(4 :dp xsd:integer)(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesMin1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(1 :dp xsd:string) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:string)(Y) :- :dp(X,Y)")
        );
    }

    public void testDataPropertiesMin2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataMinCardinality(3 :dp xsd:string) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 v not(xsd:string)(Y1) v not(xsd:string)(Y2) v not(xsd:string)(Y3) :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")
        );
    }

    public void testDataPropertiesMin3() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(1 :dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesMin4() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataMinCardinality(5 :dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(5 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact1() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(1 :dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("Y1 == Y2 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) :- :A(X), :dp(X,Y1), :dp(X,Y2)",
              "atLeast(1 :dp xsd:integer)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact2() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataExactCardinality(3 :dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)",
              "atLeast(3 :dp xsd:integer)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact3() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(1 :dp xsd:integer) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(2 :dp xsd:integer)(X) v not(xsd:integer)(Y) :- :dp(X,Y)")
        );
    }

    public void testDataPropertiesExact4() throws Exception {
        String axioms="Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(DataExactCardinality(3 :dp xsd:integer) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 v atLeast(4 :dp xsd:integer)(X) v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")
        );
    }
    
    public void testDataIntersectionOf() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger)))"
            + "ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("def:0(:a)", "xsd:nonNegativeInteger(X) :- defdata:0(X)", "atLeast(2 :dp rdfs:Literal)(X) :- def:0(X)", "defdata:0(Y) :- :dp(X,Y)", "xsd:nonPositiveInteger(X) :- defdata:0(X)")
        );
    }
    
    public void testDataIntersectionOf2() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(rdfs:Literal DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))))"
            + "ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("def:0(:a)","xsd:nonNegativeInteger(X) :- defdata:0(X)","atLeast(2 :dp rdfs:Literal)(X) :- def:0(X)","defdata:0(Y) :- :dp(X,Y)","xsd:nonPositiveInteger(X) :- defdata:0(X)")
        );
    }
    
    public void testDataIntersectionOf3() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:decimal xsd:nonNegativeInteger xsd:nonPositiveInteger)))"
            + "ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("def:0(:a)","xsd:nonNegativeInteger(X) :- defdata:0(X)","xsd:decimal(X) :- defdata:0(X)","atLeast(2 :dp rdfs:Literal)(X) :- def:0(X)","defdata:0(Y) :- :dp(X,Y)","xsd:nonPositiveInteger(X) :- defdata:0(X)")
        );
    }
    
    public void testDataUnionOf1() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataUnionOf(xsd:nonNegativeInteger xsd:double)))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("defdata:0(Y) :- :dp(X,Y)", "xsd:double(X) v xsd:nonNegativeInteger(X) :- defdata:0(X)")
        );
    }
    
    public void testDataComplementOf1() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataUnionOf(xsd:nonNegativeInteger xsd:double))))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not(xsd:nonNegativeInteger)(X) :- defdata:0(X)","defdata:0(Y) :- :dp(X,Y)","not(xsd:double)(X) :- defdata:0(X)")
        );
    }
    
    public void testDataComplementOf2() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(xsd:nonNegativeInteger xsd:double))))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("defdata:0(Y) :- :dp(X,Y)", "not(xsd:double)(X) v not(xsd:nonNegativeInteger)(X) :- defdata:0(X)")
        );
    }
    
    public void testDataComplementOf3() throws Exception {
        String axioms="SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(DataOneOf(\"5\"^^xsd:nonNegativeInteger \"4.3\"^^xsd:double) DataOneOf(\"5\"^^xsd:integer)))))";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not({ \"4.3\"^^xsd:double \"5\"^^xsd:int })(X) v not({ \"5\"^^xsd:int })(X) :- defdata:0(X)", "defdata:0(Y) :- :dp(X,Y)")
        );
    }
    
    public void testDataComplementOf4() throws Exception {
        String axioms="SubClassOf(DataAllValuesFrom(:dp DataComplementOf(DataIntersectionOf(DataOneOf(\"5\"^^xsd:nonNegativeInteger \"4.3\"^^xsd:double) DataOneOf(\"5\"^^xsd:integer)))) :A)";
        loadOntologyWithAxioms(axioms);
        List<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"4.3\"^^xsd:double \"5\"^^xsd:int }(X) :- defdata:0(X)", "{ \"5\"^^xsd:int }(X) :- defdata:0(X)", ":A(X) v atLeast(1 :dp defdata:0)(X) :- owl:Thing(X)")
        );
    }
}
