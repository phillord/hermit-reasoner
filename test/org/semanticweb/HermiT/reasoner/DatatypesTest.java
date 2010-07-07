package org.semanticweb.HermiT.reasoner;



public class DatatypesTest extends AbstractReasonerTest {

    public DatatypesTest(String name) {
        super(name);
    }
//    public void testLiteralCustomDatatype() throws Exception {
//        String axioms = "Declaration(Datatype(:MyDatatype))"
//            + "Declaration(NamedIndividual(:a))"
//            + "Declaration(DataProperty(:dp))"
//            + "DatatypeDefinition(:MyDatatype DataOneOf(\"1\"^^xsd:string \"1\"^^xsd:integer))"
//            + "DataPropertyAssertion(:dp :a \"1\"^^:MyDatatype)";
//        boolean exceptionThrown = false;
//        try {
//            loadReasonerWithAxioms(axioms);
//        } catch (RuntimeException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//    }       
//    public void testINF() throws Exception {
//        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) "
//                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"Infinity\"^^xsd:double)))"
//                + "SubClassOf(:A DataSomeValuesFrom(:dp rdfs:Literal))"
//                + "ClassAssertion(:A :a)"
//                + "NegativeDataPropertyAssertion(:dp :a \"Infinity\"^^xsd:double)";
//        loadReasonerWithAxioms(axioms);
//        assertABoxSatisfiable(false);
//    }
//    public void testFreshEntitiesQuery() throws Exception {
//        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A))" 
//        	+ "ClassAssertion(:A :a)";
//        loadReasonerWithAxioms(axioms);
//        OWLDataProperty dp=m_dataFactory.getOWLDataProperty(IRI.create(NS+"dp"));
//        OWLDataProperty dp2=m_dataFactory.getOWLDataProperty(IRI.create(NS+"dp2"));
//        assertTrue(m_reasoner.getSubDataProperties(dp, false).containsEntity(m_dataFactory.getOWLBottomDataProperty()));
//        assertTrue(m_reasoner.getSuperDataProperties(dp, false).containsEntity(m_dataFactory.getOWLTopDataProperty()));
//        assertFalse(m_reasoner.getSuperDataProperties(dp, false).containsEntity(dp2));
//        assertFalse(m_reasoner.getSubDataProperties(dp, false).containsEntity(dp2));
//    }
//    
//    public void testParsingError() throws Exception {
//        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer \"4a\"^^xsd:int))) " 
//                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:integer)))"
//                + "ClassAssertion(:A :a)"
//                + "ClassAssertion(DataSomeValuesFrom(:dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))) :a)";
//        boolean exceptionThrown = false;
//        try {
//            loadReasonerWithAxioms(axioms);
//        } catch (RuntimeException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//    }
//    
//    public void testStringAbbreviation() throws Exception {
//        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) "
//                + "DataPropertyAssertion(:dp :a \"abc\"^^xsd:string)" 
//                + "DataPropertyAssertion(:dp :a \"abc\")" 
//                + "ClassAssertion(DataMaxCardinality(1 :dp) :a)";
//        loadReasonerWithAxioms(axioms);
//        assertABoxSatisfiable(true);
//    }
    
    public void testLangAbbreviation() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) DataPropertyAssertion(:dp :a \"abc@es\"^^rdf:PlainLiteral)" 
                + "DataPropertyAssertion(:dp :a \"abc\"@es)" 
                + "ClassAssertion(DataMaxCardinality(1 :dp) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypesUnsat1() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataAllValuesFrom(:dp xsd:string)) "
                + "SubClassOf(:A DataSomeValuesFrom(:dp xsd:integer)) "
                + "ClassAssertion(:A :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testDatatypesUnsat2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:Eighteen)) Declaration(DataProperty(:hasAge)) SubClassOf(DataHasValue(:hasAge \"18\"^^xsd:integer) :Eighteen) "
                + "ClassAssertion(DataHasValue(:hasAge \"18\"^^xsd:integer) :a) " 
                + "ClassAssertion(ObjectComplementOf(:Eighteen) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat3() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:hasAge)) DataPropertyRange(:hasAge xsd:integer) "
                + "ClassAssertion(DataHasValue(:hasAge \"aString\"^^xsd:string) :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesUnsat4() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) FunctionalDataProperty(:hasAge) "
            + "ClassAssertion(DataHasValue(:hasAge \"18\"^^xsd:integer) :a) " 
            + "ClassAssertion(DataHasValue(:hasAge \"19\"^^xsd:integer) :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypesSat() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataHasValue(:dp \"18\"^^xsd:integer)) "
                + "ClassAssertion(:A :a) "
                + "ClassAssertion(DataAllValuesFrom(:dp xsd:integer) :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testMinInclusiveMaxInclusive() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"18\"^^xsd:integer))) "
                + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:maxInclusive \"10\"^^xsd:integer))) " 
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsUnsat() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp1)) Declaration(DataProperty(:dp2)) " 
                + "DisjointDataProperties(:dp1 :dp2) " 
                + "DataPropertyAssertion(:dp1 :a \"10\"^^xsd:integer)"
                + "SubClassOf(:A DataSomeValuesFrom(:dp2 DatatypeRestriction(xsd:integer xsd:minInclusive \"10\"^^xsd:integer xsd:maxInclusive \"10\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsUnsatStrings() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp1)) Declaration(DataProperty(:dp2)) " 
                + "DisjointDataProperties(:dp1 :dp2) " 
                + "DataPropertyAssertion(:dp1 :a \"ab\"^^xsd:string)"
                + "DataPropertyAssertion(:dp1 :a \"ac\"^^xsd:string)"
                + "SubClassOf(:A DataSomeValuesFrom(:dp2 DataOneOf(\"ab\"^^xsd:string \"ac\"^^xsd:string)))"
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDisjointDPsSatInteger() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp1)) Declaration(DataProperty(:dp2)) " 
                + "DisjointDataProperties(:dp1 :dp2) " 
                + "DataPropertyAssertion(:dp1 :a \"10\"^^xsd:integer)"
                + "SubClassOf(:A DataSomeValuesFrom(:dp2 DatatypeRestriction(xsd:integer xsd:minInclusive \"18\"^^xsd:integer xsd:maxInclusive \"18\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }

    public void testAllValuesFromInteger1() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:integer))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:integer \"3\"^^xsd:integer)))"
                + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"4\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromInteger2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:integer))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:integer \"3\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testAllValuesFromMixed1() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3.0\"^^xsd:decimal \"4\"^^xsd:integer))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3.0\"^^xsd:decimal)))"
                + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:maxInclusive \"3\"^^xsd:short)))"
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testAllValuesFromMixed2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3.0\"^^xsd:double \"4\"^^xsd:integer))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:integer \"3.0\"^^xsd:decimal)))"
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromDifferentTypes1() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:int))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:integer)))"
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataComplementOf(DataOneOf(\"3\"^^xsd:integer))) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromDifferentTypes2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp xsd:byte)) " 
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataOneOf(\"6542145\"^^xsd:integer)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testAllValuesFromDifferentTypes3() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:int))) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:int)))"
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataOneOf(\"3\"^^xsd:integer)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testNegZero1Integer() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"0\"^^xsd:integer))) " 
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataOneOf(\"-0\"^^xsd:integer)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testNegZero2Integer() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
                + "SubClassOf(:A DataAllValuesFrom(:dp owl:real)) " 
                + "ClassAssertion(:A :a)"
                + "ClassAssertion(DataSomeValuesFrom(:dp DataOneOf(\"-0\"^^xsd:integer)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testIntPlusDecimal() throws Exception {
        // forall :dp integer >= 5 <=7
        // forall :dp decimal >=6 <=6.8
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:byte xsd:minInclusive \"4.5\"^^xsd:decimal xsd:maxInclusive \"7\"^^xsd:short))) "
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:decimal xsd:minInclusive \"6.0\"^^xsd:decimal xsd:maxInclusive \"6.8\"^^xsd:decimal))) " 
            + "ClassAssertion(:A :a) " 
            + "ClassAssertion(DataSomeValuesFrom(:dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testIntPlusDecimal2() throws Exception {
        // forall :dp integer >= 5 <=7
        // forall :dp decimal >=6.0 <=6.8
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:byte xsd:minInclusive \"4.5\"^^xsd:decimal xsd:maxInclusive \"7\"^^xsd:short))) "
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:decimal xsd:minInclusive \"6.0\"^^xsd:decimal xsd:maxInclusive \"6.8\"^^xsd:decimal))) "
            + "SubClassOf(:A DataSomeValuesFrom(:dp owl:real))"
            + "ClassAssertion(:A :a) "
            + "NegativeDataPropertyAssertion(:dp :a \"6\"^^xsd:unsignedInt) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testDecimals() throws Exception {
        // forall :dp decimal >= 5 <=7.2
        // forall :dp decimal >=6.0 <=6.8
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:decimal xsd:minInclusive \"5\"^^xsd:byte xsd:maxInclusive \"7.2\"^^xsd:decimal))) "
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:decimal xsd:minInclusive \"6.0\"^^xsd:decimal xsd:maxInclusive \"6.8\"^^xsd:decimal))) "
            + "SubClassOf(:A DataSomeValuesFrom(:dp owl:real))"
            + "ClassAssertion(:A :a) "
            + "NegativeDataPropertyAssertion(:dp :a \"6\"^^xsd:unsignedInt) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDecimalPlusOWLreal() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp owl:real)) "
            + "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"-22.5\"^^xsd:decimal \"-0\"^^xsd:integer)))"
            + "ClassAssertion(:A :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDecimalPlusInteger() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp xsd:integer)) "
            + "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"-2.2\"^^xsd:decimal \"-0\"^^xsd:integer)))"
            + "ClassAssertion(:A :a) "
            + "NegativeDataPropertyAssertion(:dp :a \"0\"^^xsd:unsignedInt) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDifferentOneOfs() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataAllValuesFrom(:dp DataOneOf(\"3.0\"^^xsd:decimal \"3\"^^xsd:integer)))"
            + "ClassAssertion(DataMinCardinality(2 :dp) :a) "
            + "ClassAssertion(:A :a) ";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testFloatZeros() throws Exception {
        // +0 and -0 are not equal 
        String axioms = "Declaration(NamedIndividual(:Meg)) Declaration(DataProperty(:numberOfChildren)) " 
            + "DataPropertyAssertion(:numberOfChildren :Meg \"+0.0\"^^xsd:float) "
            + "DataPropertyAssertion(:numberOfChildren :Meg \"-0.0\"^^xsd:float) " 
            + "FunctionalDataProperty(:numberOfChildren)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testFloatEnumInconsistent() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) " 
            + "ClassAssertion(DataSomeValuesFrom( :dp DatatypeRestriction( " 
            + "xsd:float xsd:minExclusive \"0.0\"^^xsd:float " 
            + "xsd:maxExclusive \"1.401298464324817e-45\"^^xsd:float)) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testRationals1() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) " 
            + "ClassAssertion(DataAllValuesFrom(:dp " 
            + "owl:rational) :a) " 
            + "ClassAssertion(DataMinCardinality(2 :dp) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testRationals2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) " 
            + "ClassAssertion(DataAllValuesFrom(:dp " 
            + "DataOneOf(\"1/2\"^^owl:rational \"0.5\"^^xsd:decimal)) :a)" 
            + "ClassAssertion(DataMinCardinality(2 :dp) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testRationals3() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) " 
            + "ClassAssertion(DataAllValuesFrom(:dp " 
            + "DataOneOf(\"1/3\"^^owl:rational \"0.33333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333\"^^xsd:decimal)) :a)" 
            + "ClassAssertion(DataMinCardinality(2 :dp) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDateTime1() throws Exception {
        String axioms = "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:minInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime))) "
                + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:maxInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime))) " 
                + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDateTime2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(DataProperty(:dp)) " 
            + "SubClassOf(:A DataHasValue(:dp \"2007-10-08T20:44:11.656+01:00\"^^xsd:dateTime)) "
            + "SubClassOf(:A DataAllValuesFrom(:dp DatatypeRestriction(xsd:dateTime xsd:minInclusive \"2008-07-08T20:44:11.656+01:00\"^^xsd:dateTime xsd:maxInclusive \"2008-10-08T20:44:11.656+01:00\"^^xsd:dateTime))) " 
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testSelfInequality() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a)) Declaration(Class(:A)) Declaration(DataProperty(:dp1)) Declaration(DataProperty(:dp2)) Declaration(DataProperty(:dp3)) " 
            + "DisjointDataProperties(:dp1 :dp2) " 
            + "SubDataPropertyOf(:dp3 :dp1) " 
            + "SubDataPropertyOf(:dp3 :dp2) " 
            + "SubClassOf(:A DataSomeValuesFrom(:dp3 rdfs:Literal))"
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeDef1() throws Exception {
        String axioms = "Declaration(Datatype(:SSN))" 
            + "DatatypeDefinition(:SSN DatatypeRestriction(xsd:string xsd:pattern \"[0-9]{3}-[0-9]{2}-[0-9]{4}\"))"
            + "DataPropertyRange(:hasSSN :SSN)"
            + "DataPropertyAssertion(:hasSSN :Peter \"123-45-6789\")";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    } 
    
    public void testDatatypeDef2() throws Exception {
        String axioms = "Declaration(Datatype(:SSN))" 
            + "DatatypeDefinition(:SSN DatatypeRestriction(xsd:string xsd:pattern \"[0-9]{3}-[0-9]{2}-[0-9]{4}\"))"
            + "DataPropertyRange(:hasSSN :SSN)"
            + "DataPropertyAssertion(:hasSSN :Peter \"13-42-64\")";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeDef3() throws Exception {
        String axioms = "Declaration(Datatype(:newDT))" 
            + "DatatypeDefinition(:newDT DatatypeRestriction(xsd:integer xsd:minInclusive \"15\"^^xsd:integer))"
            + "DataPropertyRange(:dp :newDT)"
            + "DataPropertyAssertion(:dp :a \"13\"^^xsd:integer)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeDef4() throws Exception {
        String axioms = "Declaration(Datatype(:newDT))" 
            + "DatatypeDefinition(:newDT DatatypeRestriction(xsd:integer xsd:minInclusive \"15\"^^xsd:integer))"
            + "DataPropertyRange(:dp DataComplementOf(:newDT))"
            + "DataPropertyAssertion(:dp :a \"13\"^^xsd:integer)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypeDef5() throws Exception {
        String axioms = "Declaration(Datatype(:newDT))" 
            + "DatatypeDefinition(:newDT DatatypeRestriction(xsd:integer xsd:minInclusive \"15\"^^xsd:integer))"
            + "DataPropertyRange(:dp DataComplementOf(:newDT))"
            + "DataPropertyAssertion(:dp :a \"16\"^^xsd:integer)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeDef6() throws Exception {
        String axioms = "Declaration(Datatype(:newDT))" 
            + "DatatypeDefinition(:newDT DataComplementOf(DatatypeRestriction(xsd:integer xsd:minInclusive \"15\"^^xsd:integer)))"
            + "DataPropertyRange(:dp DataComplementOf(:newDT))"
            + "DataPropertyAssertion(:dp :a \"16\"^^xsd:integer)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypeUnion1() throws Exception {
        String axioms = "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger)))"
            + "ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }

    public void testDatatypeUnion2() throws Exception {
        String axioms = "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger)))"
            + "ClassAssertion(DataMinCardinality(1 :dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypeUnion3() throws Exception {
        String axioms = "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))))"
            + "ClassAssertion(DataMinCardinality(2 :dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypeUnion4() throws Exception {
        String axioms = "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))))"
            + "ClassAssertion(DataMinCardinality(3 :dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeUnionIntersection1() throws Exception {
        String axioms = "SubClassOf(owl:Thing DataAllValuesFrom(:dp DataComplementOf(DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger)))))"
            + "ClassAssertion(DataMinCardinality(3 :dp rdfs:Literal) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testDatatypeUnionIntersection2() throws Exception {
        String axioms = "SubClassOf(DataSomeValuesFrom(:dp DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))) :A)"
            + "DataPropertyAssertion(:dp :a \"0\"^^xsd:integer)"
            + "ClassAssertion(ObjectComplementOf(:A) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeUnionIntersection3() throws Exception {
        String axioms = "SubClassOf(DataSomeValuesFrom(:dp DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))) :A)"
            + "DataPropertyAssertion(:dp :a \"abc\"^^xsd:string)"
            + "ClassAssertion(ObjectComplementOf(:A) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
    
    public void testDatatypeUnionIntersection4() throws Exception {
        String axioms = "SubClassOf(DataSomeValuesFrom(:dp DataUnionOf(DataOneOf(\"abc\") DataIntersectionOf(xsd:nonNegativeInteger xsd:nonPositiveInteger))) :A)"
            + "DataPropertyAssertion(:dp :a \"5\"^^xsd:integer)"
            + "ClassAssertion(ObjectComplementOf(:A) :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(true);
    }
    
    public void testNegativeDPAssertions() throws Exception {
        String axioms = "SubClassOf(:A DataSomeValuesFrom(:dp DataOneOf(\"5\"^^xsd:integer)))"
            + "NegativeDataPropertyAssertion(:dp :a \"5\"^^xsd:integer)"
            + "ClassAssertion(:A :a)";
        loadReasonerWithAxioms(axioms);
        assertABoxSatisfiable(false);
    }
}
