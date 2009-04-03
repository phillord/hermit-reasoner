package org.semanticweb.HermiT.structural;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLClausification;

public class ClausificationDatatypesTest extends AbstractStructuralTest {

    public ClausificationDatatypesTest(String name) {
        super(name);
    }

    public void testDataPropertiesHasValue1() throws Exception {
        String axioms="SubClassOf(Eighteen DataHasValue(hasAge \"18\"^^xsd:integer))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :hasAge { \"18\"^^xsd:int })(X) :- :Eighteen(X)")
        );
    }

    public void testDataPropertiesHasValue2() throws Exception {
        String axioms="SubClassOf(DataHasValue(hasAge \"18\"^^xsd:integer) Eighteen)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":Eighteen(X) v not({ \"18\"^^xsd:int })(Y) :- :hasAge(X,Y)")
        );
    }

    public void testDataPropertiesAll1() throws Exception {
        String axioms="SubClassOf(A DataAllValuesFrom(dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("xsd:integer(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesAll2() throws Exception {
        String axioms="SubClassOf(DataAllValuesFrom(dp xsd:integer) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not(xsd:integer))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesSome1() throws Exception {
        String axioms="SubClassOf(DataSomeValuesFrom(dp xsd:string) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:string)(Y) :- :dp(X,Y)")
        );
    }

    public void testDataPropertiesSome2() throws Exception {
        String axioms="SubClassOf(A DataSomeValuesFrom(dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesDataOneOf1() throws Exception {
        String axioms="SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"Peter\"^^xsd:string \"19\"^^xsd:integer)))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"19\"^^xsd:int \"Peter\" }(Y) :- :A(X), :dp(X,Y)"),
            S("{ \"Peter\" \"19\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataOneOf2() throws Exception {
        String axioms="SubClassOf(DataAllValuesFrom(dp DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not({ \"18\"^^xsd:int \"19\"^^xsd:int }))(X) :- owl:Thing(X)"),
            S(":A(X) v atLeast(1 :dp not({ \"19\"^^xsd:int \"18\"^^xsd:int }))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesDataOneOf3() throws Exception {
        String axioms="SubClassOf(DataAllValuesFrom(dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(1 :dp not({ \"18\"^^xsd:int \"abc\" }))(X) :- owl:Thing(X)"),
            S(":A(X) v atLeast(1 :dp not({ \"abc\" \"18\"^^xsd:int }))(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesDataOneOf4() throws Exception {
        String axioms="SubClassOf(A DataAllValuesFrom(dp DataOneOf(\"18\"^^xsd:integer \"abc\"^^xsd:string)))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"18\"^^xsd:int \"abc\" }(Y) :- :A(X), :dp(X,Y)"),
            S("{ \"abc\" \"18\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataComplementOf1() throws Exception {
        String axioms="SubClassOf(A DataAllValuesFrom(dp DataComplementOf(DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer)))))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("{ \"18\"^^xsd:int \"19\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)"),
            S("{ \"19\"^^xsd:int \"18\"^^xsd:int }(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesDataComplementOf2() throws Exception {
        String axioms="SubClassOf(A DataAllValuesFrom(dp DataComplementOf(DataOneOf(\"18\"^^xsd:integer \"19\"^^xsd:integer))))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not({ \"18\"^^xsd:int })(Y) :- :A(X), :dp(X,Y)",
              "not({ \"19\"^^xsd:int })(Y) :- :A(X), :dp(X,Y)")
        );
    }

    public void testDataPropertiesMax1() throws Exception {
        String axioms="SubClassOf(A DataMaxCardinality(1 dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not(xsd:string)(Y1) v not(xsd:string)(Y2) v Y1 == Y2 :- :A(X), :dp(X,Y1), :dp(X,Y2)")
        );
    }

    public void testDataPropertiesMax2() throws Exception {
        String axioms="SubClassOf(DataMaxCardinality(1 dp xsd:string) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(2 :dp xsd:string)(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesMax3() throws Exception {
        String axioms="SubClassOf(A DataMaxCardinality(3 dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) v Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)")
        );
    }

    public void testDataPropertiesMax4() throws Exception {
        String axioms="SubClassOf(DataMaxCardinality(3 dp xsd:integer) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v atLeast(4 :dp xsd:integer)(X) :- owl:Thing(X)")
        );
    }

    public void testDataPropertiesMin1() throws Exception {
        String axioms="SubClassOf(DataMinCardinality(1 dp xsd:string) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:string)(Y1) :- :dp(X,Y1)")
        );
    }

    public void testDataPropertiesMin2() throws Exception {
        String axioms="SubClassOf(DataMinCardinality(3 dp xsd:string) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:string)(Y1) v not(xsd:string)(Y2) v not(xsd:string)(Y3) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")
        );
    }

    public void testDataPropertiesMin3() throws Exception {
        String axioms="SubClassOf(A DataMinCardinality(1 dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(1 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesMin4() throws Exception {
        String axioms="SubClassOf(A DataMinCardinality(5 dp xsd:string))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("atLeast(5 :dp xsd:string)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact1() throws Exception {
        String axioms="SubClassOf(A DataExactCardinality(1 dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v Y1 == Y2 :- :A(X), :dp(X,Y1), :dp(X,Y2)",
              "atLeast(1 :dp xsd:integer)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact2() throws Exception {
        String axioms="SubClassOf(A DataExactCardinality(3 dp xsd:integer))";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S("not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v not(xsd:integer)(Y4) v Y1 == Y2 v Y1 == Y3 v Y1 == Y4 v Y2 == Y3 v Y2 == Y4 v Y3 == Y4 :- :A(X), :dp(X,Y1), :dp(X,Y2), :dp(X,Y3), :dp(X,Y4)",
              "atLeast(3 :dp xsd:integer)(X) :- :A(X)")
        );
    }

    public void testDataPropertiesExact3() throws Exception {
        String axioms="SubClassOf(DataExactCardinality(1 dp xsd:integer) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:integer)(Y1) v atLeast(2 :dp xsd:integer)(X) :- :dp(X,Y1)")
        );
    }

    public void testDataPropertiesExact4() throws Exception {
        String axioms="SubClassOf(DataExactCardinality(3 dp xsd:integer) A)";
        loadOntologyWithAxioms(axioms);
        Set<String> clauses=getDLClauses();
        assertContainsAll(this.getName(),clauses,
            S(":A(X) v not(xsd:integer)(Y1) v not(xsd:integer)(Y2) v not(xsd:integer)(Y3) v Y1 == Y2 v Y1 == Y3 v Y2 == Y3 v atLeast(4 :dp xsd:integer)(X) :- :dp(X,Y1), :dp(X,Y2), :dp(X,Y3)")
        );
    }

    protected Set<String> getDLClauses() throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausifier.clausify(m_ontologyManager,m_ontology,noDescriptionGraphs);
        Set<String> actualStrings=new HashSet<String>();
        Prefixes prefixes=new Prefixes();
        prefixes.declareSemanticWebPrefixes();
        prefixes.declareInternalPrefixes(Collections.singleton(m_ontology.getURI()+"#"));
        prefixes.declareDefaultPrefix(m_ontology.getURI()+"#");
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not "+atom.toString(prefixes));
        return actualStrings;
    }
}
