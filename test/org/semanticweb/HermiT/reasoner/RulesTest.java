package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

@SuppressWarnings("javadoc")
public class RulesTest extends AbstractReasonerTest {

    public RulesTest(String name) {
        super(name);
    }

    public void testSameAsInBody1() throws Exception {
        String axioms = "Declaration(ObjectProperty(:r))\n" + "Declaration(ObjectProperty(:s))\n"
                + "Declaration(ObjectProperty(:t))\n" + "Declaration(Class(:u))\n"
                + "ObjectPropertyAssertion(:r :a :b)\n" + "ObjectPropertyAssertion(:s :a :c)\n"
                + "ObjectPropertyAssertion(:t :a :d)\n" + "ClassAssertion(ObjectComplementOf(:u) :a)\n"
                // r(x1, y1) /\ s(x2, y2) /\ t(x3, y3) x1 = x2 /\ x1 = x3 ->
                // u(x3)
                + "DLSafeRule(\n" + "  Body(\n" + "     ObjectPropertyAtom(:r Variable(:x1) Variable(:y1))\n"
                + "     ObjectPropertyAtom(:s Variable(:x2) Variable(:y2))\n"
                + "     ObjectPropertyAtom(:t Variable(:x3) Variable(:y3))\n"
                + "     SameIndividualAtom(Variable(:x1) Variable(:x2))\n"
                + "     SameIndividualAtom(Variable(:x1) Variable(:x3))\n" + "  )\n"
                + "  Head(ClassAtom(:u Variable(:x3)))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        assertABoxSatisfiable(false);
    }

    public void testSameAsInBody2() throws Exception {
        String axioms = "Declaration(ObjectProperty(:r))\n" + "Declaration(ObjectProperty(:s))\n"
                + "Declaration(ObjectProperty(:t))\n" + "Declaration(Class(:u))\n"
                + "ObjectPropertyAssertion(:r :a1 :b)\n" + "ObjectPropertyAssertion(:s :a2 :c)\n"
                + "ObjectPropertyAssertion(:t :a3 :d)\n" + "ClassAssertion(ObjectComplementOf(:u) :a1)\n"
                // r(x1, y1) /\ s(x2, y2) /\ t(x3, y3) x1 = x2 /\ x1 = x3 ->
                // u(x3)
                + "DLSafeRule(\n" + "  Body(\n" + "     ObjectPropertyAtom(:r Variable(:x1) Variable(:y1))\n"
                + "     ObjectPropertyAtom(:s Variable(:x2) Variable(:y2))\n"
                + "     ObjectPropertyAtom(:t Variable(:x3) Variable(:y3))\n"
                + "     SameIndividualAtom(Variable(:x1) Variable(:x2))\n"
                + "     SameIndividualAtom(Variable(:x1) Variable(:x3))\n" + "  )\n"
                + "  Head(ClassAtom(:u Variable(:x3)))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        assertABoxSatisfiable(true);
    }

    public void testSameAsInBodyWithDataProperties() throws Exception {
        String axioms = "Declaration(DataProperty(:r))\n" + "Declaration(DataProperty(:s))\n"
                + "Declaration(Class(:u))\n" + "DataPropertyAssertion(:r :a \"2\"^^xsd:integer)\n"
                + "ClassAssertion(DataSomeValuesFrom(:s DatatypeRestriction(xsd:integer xsd:minInclusive \"2\"^^xsd:integer xsd:maxInclusive \"2\"^^xsd:integer)) :a)"
                + "\n" + "ClassAssertion(ObjectComplementOf(:u) :a)\n"
                // r(x,y1) /\ s(x,y2) /\ y1==y2 -> u(x)
                + "DLSafeRule(\n" + "  Body(\n" + "     DataPropertyAtom(:r Variable(:x) Variable(:y1))\n"
                + "     DataPropertyAtom(:s Variable(:x) Variable(:y2))\n"
                + "     SameIndividualAtom(Variable(:y1) Variable(:y2))\n" + "  )\n"
                + "  Head(ClassAtom(:u Variable(:x)))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        assertABoxSatisfiable(false);
    }

    public void testDataPropertiesInBody() throws Exception {
        String axioms = "Declaration(DataProperty(:r))\n" + "Declaration(DataProperty(:s))\n"
                + "Declaration(Class(:u))\n" + "DataPropertyAssertion(:r :a \"2\"^^xsd:integer)\n"
                + "ClassAssertion(DataSomeValuesFrom(:s DatatypeRestriction(xsd:integer xsd:minInclusive \"2\"^^xsd:integer xsd:maxInclusive \"2\"^^xsd:integer)) :a)"
                + "\n" + "ClassAssertion(ObjectComplementOf(:u) :a)\n"
                // r(x,y) /\ s(x,y) -> u(x)
                + "DLSafeRule(\n" + "  Body(\n" + "     DataPropertyAtom(:r Variable(:x) Variable(:y))\n"
                + "     DataPropertyAtom(:s Variable(:x) Variable(:y))\n" + "  )\n"
                + "  Head(ClassAtom(:u Variable(:x)))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        assertABoxSatisfiable(false);
    }

    public void testIndividualsInRules() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a))\n" + "Declaration(NamedIndividual(:b))\n"
                + "Declaration(Class(:c))\n" + "Declaration(Class(:d))\n" + "ClassAssertion(:c :a)\n"
                + "ClassAssertion(ObjectComplementOf(:d) :b)\n"
                // c(a) -> d(b)
                + "DLSafeRule(\n" + "  Body(ClassAtom(:c :a))\n" + "  Head(ClassAtom(:d :b))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        assertABoxSatisfiable(false);
    }

    public void testRuleNonSimple() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a))\n" + "Declaration(NamedIndividual(:b))\n"
                + "Declaration(ObjectProperty(:t))\n" + "Declaration(ObjectProperty(:s))\n"
                + "TransitiveObjectProperty(:t)\n"
                + "ClassAssertion(ObjectSomeValuesFrom(:t ObjectSomeValuesFrom(:t ObjectOneOf(:b))) :a)\n"
                // t(x, y) -> s(x, y)
                + "DLSafeRule(\n" + "  Body(ObjectPropertyAtom(:t Variable(:x) Variable(:y)))\n"
                + "  Head(ObjectPropertyAtom(:s Variable(:x) Variable(:y)))\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLObjectProperty t = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "t"));
        // OWLObjectProperty
        // s=m_dataFactory.getOWLObjectProperty(IRI.create(AbstractReasonerTest.NS
        // + "s"));
        assertTrue(m_reasoner.hasObjectPropertyRelationship(a, t, b));
        // The following fails because transitive properties in rules do not
        // work correctly
        // assertTrue(m_reasoner.hasObjectPropertyRelationship(a, s, b));
    }

    public void testRuleNotAxiom() throws Exception {
        String axioms = "Declaration(NamedIndividual(:a))\n" + "Declaration(NamedIndividual(:b))\n"
                + "Declaration(Class(:A))\n" + "Declaration(Class(:B))\n" + "ClassAssertion(:A :a)\n"
                + "ClassAssertion(:A :b)\n"
                // A(x) -> B(x)
                + "DLSafeRule(\n" + "  Body(ClassAtom(:A Variable(:x)))\n" + "  Head(ClassAtom(:B Variable(:x)))"
                + "\n" + ")";
        loadOntologyWithAxioms(axioms);
        createReasoner();

        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        assertFalse(m_reasoner.isEntailed(m_dataFactory.getOWLSubClassOfAxiom(A, B)));
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
    }

    public void testSimpleRule2() throws Exception {
        String axioms = "Declaration(NamedIndividual(:sensor))" + "Declaration(NamedIndividual(:kitchen))"
                + "Declaration(NamedIndividual(:pda))" + "Declaration(Class(:BluetoothSensor))"
                + "Declaration(Class(:Location))" + "Declaration(Class(:BluetoothDevice))"
                + "Declaration(ObjectProperty(:hasLocation))" + "Declaration(ObjectProperty(:detects))"
                + "ClassAssertion(:BluetoothDevice :pda)\n" + "ClassAssertion(:BluetoothSensor :sensor)\n"
                + "ClassAssertion(:Location :kitchen)\n" + "ObjectPropertyAssertion(:detects :sensor :pda)\n"
                + "ObjectPropertyAssertion(:hasLocation :sensor :kitchen)\n"
                // BluetoothDevice(vbd) /\ BluetoothSensor(vbs) /\ Location(vl)
                // /\ detects(vbs, vl) /\ hasLocation(vbs, vl) ->
                // hasLocation(vbd, vl)
                + "DLSafeRule(" + "  Body(" + "    ClassAtom(:BluetoothDevice Variable(:vbd)) "
                + "    ClassAtom(:BluetoothSensor Variable(:vbs)) " + "    ClassAtom(:Location Variable(:vl)) "
                + "    ObjectPropertyAtom(:detects Variable(:vbs) Variable(:vbd)) "
                + "    ObjectPropertyAtom(:hasLocation Variable(:vbs) Variable(:vl))" + "  )"
                + "  Head(ObjectPropertyAtom(:hasLocation Variable(:vbd) Variable(:vl)))" + ")";
        loadOntologyWithAxioms(axioms);

        OWLNamedIndividual pda = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "pda"));
        OWLNamedIndividual k = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "kitchen"));
        OWLObjectProperty hasLocation = m_dataFactory
                .getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "hasLocation"));

        OWLReasoner owlReasoner = new ReasonerFactory().createReasoner(m_ontology);
        assertTrue(owlReasoner.getObjectPropertyValues(pda, hasLocation).containsEntity(k));
    }

    public void testSimpleRule() throws Exception {
        String axioms = "SubClassOf(:A :B)\n" + "ClassAssertion(:A :a)\n" + "ClassAssertion(:D :b)\n"
        // B(x) -> C(x)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x))) Head(ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);

        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));

        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(b));
    }

    public void testRuleWithConstants() throws Exception {
        String axioms = "SubClassOf(:A :B)" + "ClassAssertion(:A :a)" + "ClassAssertion(:D :b)"
        // B(x) -> C(a)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x))) Head(ClassAtom(:C :a)))";
        loadOntologyWithAxioms(axioms);

        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(b));
    }

    public void testRuleWithConstants2() throws Exception {
        String axioms = "ClassAssertion(ObjectSomeValuesFrom(:r owl:Thing) :a)" + "ObjectPropertyAssertion(:r :a :b)"
        // r(x, y) -> s(x, y)
                + "DLSafeRule(Body(ObjectPropertyAtom(:r Variable(:x) Variable(:y))) Head(ObjectPropertyAtom(:s Variable(:x) Variable(:y))))"
                // r(x, b) -> sb(x, b)
                + "DLSafeRule(Body(ObjectPropertyAtom(:r Variable(:x) :b)) Head(ObjectPropertyAtom(:sb Variable(:x) :b)))"
                // s(a, x) -> sa(a, b)
                + "DLSafeRule(Body(ObjectPropertyAtom(:s :a Variable(:x))) Head(ObjectPropertyAtom(:sa :a :b)))"
                // r(a, b) -> q(a, b)
                + "DLSafeRule(Body(ObjectPropertyAtom(:r :a :b)) Head(ObjectPropertyAtom(:q :a :b)))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLObjectProperty r = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "r"));
        OWLObjectProperty s = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "s"));
        OWLObjectProperty sa = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "sa"));
        OWLObjectProperty sb = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "sb"));
        OWLObjectProperty q = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "q"));
        createReasoner();
        NodeSet<OWLNamedIndividual> result = m_reasoner.getObjectPropertyValues(a, r);
        assertTrue(result.isSingleton() && result.containsEntity(b));
        result = m_reasoner.getObjectPropertyValues(a, s);
        assertTrue(result.isSingleton() && result.containsEntity(b));
        result = m_reasoner.getObjectPropertyValues(a, sa);
        assertTrue(result.isSingleton() && result.containsEntity(b));
        result = m_reasoner.getObjectPropertyValues(a, sb);
        assertTrue(result.isSingleton() & result.containsEntity(b));
        result = m_reasoner.getObjectPropertyValues(a, q);
        assertTrue(result.isSingleton() && result.containsEntity(b));
    }

    public void testRuleWithDatatypes() throws Exception {
        String axioms = "DataPropertyAssertion(:dp :a \"18\"^^xsd:short)"
                + "DataPropertyAssertion(:dp :b \"17\"^^xsd:short)"
                // dp(x, "18"^^xsd:integer) -> C(x)
                + "DLSafeRule(Body(DataPropertyAtom(:dp Variable(:x) \"18\"^^xsd:integer)) Head(ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertFalse(m_reasoner.getInstances(C, false).containsEntity(b));
    }

    public void testRuleWithDatatypes2() throws Exception {
        String axioms = "ClassAssertion(DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"10\"^^xsd:integer)) :a)"
                + "DataPropertyAssertion(:dp :b \"10\"^^xsd:short)"
                + "DataPropertyAssertion(:dp :c \"25\"^^xsd:integer)" + "ClassAssertion(ObjectComplementOf(:C) :a)"
                // dp(x, y) /\ DatatypeRestriction(xsd:int xsd:minInclusive
                // "15"^^xsd:int)(y) -> C(x)
                + "DLSafeRule(Body(DataPropertyAtom(:dp Variable(:x) Variable(:y)) DataRangeAtom(DatatypeRestriction(xsd:int xsd:minInclusive \"15\"^^xsd:int) Variable(:y))) Head(ClassAtom(:C Variable(:x))))"
                // dp(x, y) /\ DataComplementOf(DatatypeRestriction(xsd:int
                // xsd:minInclusive "15"^^xsd:int))(y) -> D(x)
                + "DLSafeRule(Body(DataPropertyAtom(:dp Variable(:x) Variable(:y)) DataRangeAtom(DataComplementOf(DatatypeRestriction(xsd:int xsd:minInclusive \"15\"^^xsd:int)) Variable(:y))) Head(ClassAtom(:D Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "c"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "D"));

        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(c));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(D, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(D, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(D, false).containsEntity(c));
    }

    public void testRuleWithFreshIndividuals() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
                // A(x) -> B(b)
                + "DLSafeRule(Body(ClassAtom(:A Variable(:x))) Head(ClassAtom(:B :b)))"
                // B(x) -> C(x)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x))) Head(ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(b));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(B, false).containsEntity(a));
    }

    public void testAddingFactsByRules() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
                // -> B(a)
                + "DLSafeRule(Body() Head(ClassAtom(:B :a)))"
                // -> B(b)
                + "DLSafeRule(Body() Head(ClassAtom(:B :b)))"
                // B(x) -> C(x)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x))) Head(ClassAtom(:C Variable(:x))))"
                // B(x) /\ A(x) -> D(x)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x)) ClassAtom(:A Variable(:x))) Head(ClassAtom(:D Variable(:x))))"
                // B(x) /\ D(y) -> E(e)
                + "DLSafeRule(Body(ClassAtom(:B Variable(:x)) ClassAtom(:D Variable(:y))) Head(ClassAtom(:E :e)))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "e"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        OWLClass D = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "D"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "E"));
        createReasoner();
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(A, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(b));
        assertTrue(!m_reasoner.getInstances(B, false).containsEntity(e));
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(b));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(e));
        assertTrue(m_reasoner.getInstances(D, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(D, false).containsEntity(b));
        assertTrue(!m_reasoner.getInstances(D, false).containsEntity(e));
        assertTrue(!m_reasoner.getInstances(E, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(E, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(E, false).containsEntity(e));
    }

    public void testLloydTopor() throws Exception {
        String axioms = "ClassAssertion(:A :a)" + "ClassAssertion(:B :b)"
        // A(x) -> B(x) /\ C(x)
                + "DLSafeRule(Body(ClassAtom(:A Variable(:x))) Head(ClassAtom(:B Variable(:x)) ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));

        createReasoner();
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(A, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(b));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(b));
    }

    public void testDataRangeSafety() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
                // A(x) /\ xsd:integer(y) -> dp(x, y)
                + "DLSafeRule(Body(ClassAtom(:A Variable(:x)) DataRangeAtom(xsd:integer Variable(:y))) Head(DataPropertyAtom(:dp Variable(:x) Variable(:y))))";
        loadOntologyWithAxioms(axioms);
        try {
            createReasoner();
            fail();
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
        }
    }

    public void testSeveralVars() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) ClassAssertion(:C :c) ClassAssertion(:D :d) ClassAssertion(:E :e)"
                + "ObjectPropertyAssertion(:rab :a :b) ObjectPropertyAssertion(:rac :a :c) ObjectPropertyAssertion(:rcd :c :d)"
                // A(xa) /\ B(b) /\ rab(xa, xb) /\ rac(xa, xc) /\ rcd(xc, xd) /\
                // E(xe) -> Ap(xa) /\ Bp(xb) /\ Cp(c) /\ Dp(xd) /\ Ep(xe) /\
                // rae(xa, xe)
                + "DLSafeRule(Body(" + "ClassAtom(:A Variable(:xa)) " + "ClassAtom(:B :b) "
                + "ObjectPropertyAtom(:rab Variable(:xa) Variable(:xb))"
                + "ObjectPropertyAtom(:rac Variable(:xa) Variable(:xc))"
                + "ObjectPropertyAtom(:rcd Variable(:xc) Variable(:xd))" + "ClassAtom(:E Variable(:xe)) " + ") Head("
                + "ClassAtom(:Ap Variable(:xa)) " + "ClassAtom(:Bp Variable(:xb)) " + "ClassAtom(:Cp :c) "
                + "ClassAtom(:Dp Variable(:xd)) " + "ClassAtom(:Ep Variable(:xe)) "
                + "ObjectPropertyAtom(:rae Variable(:xa) Variable(:xe))" + "))";
        loadOntologyWithAxioms(axioms);
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));
        OWLNamedIndividual c = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "c"));
        OWLNamedIndividual d = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "d"));
        OWLNamedIndividual e = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "e"));
        OWLClass A = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "A"));
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLClass E = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "E"));
        OWLClass Ap = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "Ap"));
        OWLClass Bp = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "Bp"));
        OWLClass Cp = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "Cp"));
        OWLClass Dp = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "Dp"));
        OWLClass Ep = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "Ep"));
        OWLObjectProperty rcd = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "rcd"));
        OWLObjectProperty rae = m_dataFactory.getOWLObjectProperty(IRI.create(AbstractOntologyTest.NS + "rae"));

        createReasoner();
        assertTrue(m_reasoner.getInstances(Ap, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(Bp, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(Cp, false).containsEntity(c));
        assertTrue(m_reasoner.getInstances(Dp, false).containsEntity(d));
        assertTrue(m_reasoner.getInstances(Ep, false).containsEntity(e));
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(A, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(b));
        assertTrue(m_reasoner.getInstances(E, false).containsEntity(e));
        NodeSet<OWLNamedIndividual> result = m_reasoner.getObjectPropertyValues(c, rcd);
        assertTrue(result.containsEntity(d));
        result = m_reasoner.getObjectPropertyValues(a, rae);
        assertTrue(result.containsEntity(e));
        assertTrue(result.isSingleton());
    }

    public void testPositiveBodyDataRange() throws Exception {
        String axioms = "ClassAssertion(:A :a) SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
                // dp(x, y) /\ ((xsd:integer >= 5) and (xsd:decimal <= 10))(y)
                // /\ (xsd:int <= 9)(y) -> B(x)
                + "DLSafeRule(Body(" + "DataPropertyAtom(:dp Variable(:x) Variable(:y)) "
                + "DataRangeAtom(DataIntersectionOf(DatatypeRestriction(xsd:integer xsd:minInclusive \"5\"^^xsd:int) DatatypeRestriction(xsd:decimal xsd:maxInclusive \"10\"^^xsd:int)) Variable(:y))"
                + ") Head(ClassAtom(:B Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLClass B = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        createReasoner();
        assertTrue(m_reasoner.getInstances(B, false).containsEntity(a));
    }

    public void testNegativeBodyDataRange() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b)"
                + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
                + "SubClassOf(:B DataHasValue(:dp \"abc\"))"
                // dp(x, y) /\ not((xsd:integer >= 5) and (xsd:decimal <=
                // 10))(y) -> C(x)
                + "DLSafeRule(Body(" + "DataPropertyAtom(:dp Variable(:x) Variable(:y)) "
                + "DataRangeAtom(DataComplementOf(DataIntersectionOf(DatatypeRestriction(xsd:integer xsd:minInclusive \"5\"^^xsd:int) DatatypeRestriction(xsd:decimal xsd:maxInclusive \"10\"^^xsd:int))) Variable(:y))"
                + ") Head(ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);
        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "B"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));

        createReasoner();
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(b));
    }

    public void testNegDRInHead() throws Exception {
        String axioms = "ClassAssertion(:A :a)"
                + "SubClassOf(:A DataSomeValuesFrom(:dp DatatypeRestriction(xsd:integer xsd:minInclusive \"6\"^^xsd:integer xsd:maxInclusive \"9\"^^xsd:integer)))"
                // dp(x, y) -> not(xsd:short >= 1)(y)
                + "DLSafeRule(Body(" + "DataPropertyAtom(:dp Variable(:x) Variable(:y)) " + ") Head("
                + "DataRangeAtom(DataComplementOf(DatatypeRestriction(xsd:short xsd:minInclusive \"1\"^^xsd:int)) Variable(:y))"
                + "))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }

    public void testSameAs() throws Exception {
        String axioms = "ClassAssertion(:A :a) " + "ClassAssertion(:B :b) " + "DisjointClasses(:A :B) "
                + "ObjectPropertyAssertion(:r :a :b)"
                // r(x, y) -> SameAs(x, y)
                + "DLSafeRule(Body(ObjectPropertyAtom(:r Variable(:x) Variable(:y))) Head(SameIndividualAtom(Variable(:x) Variable(:y))))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }

    public void testDifferentFrom() throws Exception {
        String axioms = "ObjectPropertyAssertion(:f :a :b)" + "ObjectPropertyAssertion(:f :a :c)"
                + "FunctionalObjectProperty(:f)"
                // f(x, y) /\ f(x, z) -> DifferentFrom(y, z)
                + "DLSafeRule(Body(ObjectPropertyAtom(:f Variable(:x) Variable(:y)) ObjectPropertyAtom(:f Variable(:x) Variable(:z))) Head(DifferentIndividualsAtom(Variable(:y) Variable(:z))))";
        loadOntologyWithAxioms(axioms);
        createReasoner();
        assertTrue(!m_reasoner.isConsistent());
    }

    public void testDiffrentFrom2() throws Exception {
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) DisjointClasses(:A :B) ObjectPropertyAssertion(:r :a :b)"
                // r(x, y) /\ DifferentFrom(x, y)-> C(x)
                + "DLSafeRule(Body(" + "ObjectPropertyAtom(:r Variable(:x) Variable(:y)) "
                + "DifferentIndividualsAtom(Variable(:x) Variable(:y))) Head(ClassAtom(:C Variable(:x))))";
        loadOntologyWithAxioms(axioms);

        OWLClass C = m_dataFactory.getOWLClass(IRI.create(AbstractOntologyTest.NS + "C"));
        OWLNamedIndividual a = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "a"));
        OWLNamedIndividual b = m_dataFactory.getOWLNamedIndividual(IRI.create(AbstractOntologyTest.NS + "b"));

        createReasoner();
        assertTrue(m_reasoner.getInstances(C, false).containsEntity(a));
        assertTrue(!m_reasoner.getInstances(C, false).containsEntity(b));
    }
}
