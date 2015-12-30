package org.semanticweb.HermiT.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.datalog.ConjunctiveQuery;
import org.semanticweb.HermiT.datalog.DatalogEngine;
import org.semanticweb.HermiT.datalog.QueryResultCollector;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

@SuppressWarnings("javadoc")
public class DatalogEngineTest extends AbstractReasonerTest {

    public DatalogEngineTest(String name) {
        super(name);
    }

    public void testBasic() throws Exception {
        loadOntologyWithAxioms(
                "SubClassOf( ObjectSomeValuesFrom( :R :A ) :A )\n" + "SubClassOf( ObjectSomeValuesFrom( :R :B ) :B )\n"
                        + "SubClassOf( ObjectIntersectionOf( :A :B ) :C )\n" +

        "ClassAssertion( :A :a )\n" + "ObjectPropertyAssertion( :R :b :a )\n" + "ObjectPropertyAssertion( :R :c :b )\n"
                        + "ObjectPropertyAssertion( :R :d :c )\n" +

        "ClassAssertion( :B :k )\n" + "ObjectPropertyAssertion( :R :l :k )\n" + "ObjectPropertyAssertion( :R :m :l )\n"
                        + "ObjectPropertyAssertion( :R :c :m )\n" + "ObjectPropertyAssertion( :R :n :c )");
        createReasoner();
        DatalogEngine datalogEngine = new DatalogEngine(m_reasoner.getDLOntology());
        QueryChecker queryChecker = new QueryChecker();

        new ConjunctiveQuery(datalogEngine, AS(A(CN("A"), V("X"))), TS(V("X"))).evaluate(queryChecker);
        queryChecker.add(I("a")).add(I("b")).add(I("c")).add(I("d")).add(I("n")).assertEquals();

        new ConjunctiveQuery(datalogEngine, AS(A(CN("B"), V("X"))), TS(V("X"))).evaluate(queryChecker);
        queryChecker.add(I("c")).add(I("d")).add(I("k")).add(I("l")).add(I("m")).add(I("n")).assertEquals();

        new ConjunctiveQuery(datalogEngine, AS(A(CN("C"), V("X"))), TS(V("X"))).evaluate(queryChecker);
        queryChecker.add(I("c")).add(I("d")).add(I("n")).assertEquals();
    }

    public void testEquality() throws Exception {
        loadOntologyWithAxioms("FunctionalObjectProperty( :R )\n" +

        "ObjectPropertyAssertion( :R :b :a )\n" + "ObjectPropertyAssertion( :R :b :c )\n"
                + "ObjectPropertyAssertion( :R :d :c )\n" + "ObjectPropertyAssertion( :R :d :e )\n"
                + "ObjectPropertyAssertion( :R :f :e )\n" + "ObjectPropertyAssertion( :R :f :g )");
        createReasoner();
        DatalogEngine datalogEngine = new DatalogEngine(m_reasoner.getDLOntology());
        datalogEngine.materialize();
        assertContainsAll(datalogEngine.getEquivalenceClass(I("a")), TS(I("a"), I("c"), I("e"), I("g")));
        QueryChecker queryChecker = new QueryChecker();

        new ConjunctiveQuery(datalogEngine, AS(A(R("R"), V("X"), V("Y"))), TS(V("X"), V("Y"))).evaluate(queryChecker);
        queryChecker.add(I("b"), datalogEngine.getRepresentative(I("a")))
                .add(I("d"), datalogEngine.getRepresentative(I("a")))
                .add(I("f"), datalogEngine.getRepresentative(I("a"))).assertEquals();
    }

    public void testQueryWithIndividualsAndEquality() throws Exception {
        loadOntologyWithAxioms("ObjectPropertyAssertion( :R :c :b )\n" + "ObjectPropertyAssertion( :S :c :a )\n"
                + "SameIndividual( :a :b )\n" +

        "ObjectPropertyAssertion( :R :d :e )\n" + "ObjectPropertyAssertion( :S :d :f )");
        createReasoner();
        DatalogEngine datalogEngine = new DatalogEngine(m_reasoner.getDLOntology());
        datalogEngine.materialize();
        assertContainsAll(datalogEngine.getEquivalenceClass(I("a")), TS(I("a"), I("b")));
        QueryChecker queryChecker = new QueryChecker();

        new ConjunctiveQuery(datalogEngine, AS(A(R("R"), V("X"), I("a")), A(R("S"), V("X"), I("b"))), TS(V("X")))
                .evaluate(queryChecker);
        queryChecker.add(I("c")).assertEquals();
    }

    public void testQueryWithIndividuals() throws Exception {
        loadOntologyWithAxioms("DLSafeRule(Body(ClassAtom(:D0 Variable(:X))) Head(ClassAtom(:A Variable(:X))))\n"
                + "DLSafeRule(Body(ClassAtom(:D0 Variable(:X))) Head(ClassAtom(:B Variable(:X))))\n"
                + "DLSafeRule(Body(ClassAtom(:A Variable(:X))ClassAtom(:RD0 Variable(:Z))) Head(ClassAtom(:D0 Variable(:Z))))\n"
                + "DLSafeRule(Body(ClassAtom(:A Variable(:X))ClassAtom(:RD0 Variable(:Z))) Head(ObjectPropertyAtom(:R Variable(:X) Variable(:Z))))\n" 
                + "ClassAssertion( owl:Thing :a )\n" + "ClassAssertion( :RD0 :rd0 )\n"
                + "ClassAssertion( :A :a )");
        createReasoner();
        DatalogEngine datalogEngine = new DatalogEngine(m_reasoner.getDLOntology());
        datalogEngine.materialize();
        QueryChecker queryChecker = new QueryChecker();

        new ConjunctiveQuery(datalogEngine, AS(A(R("R"), V("X"), I("a"))), TS(V("X"))).evaluate(queryChecker);
        queryChecker.assertEquals();
    }

    protected static class AnswerTuple {
        protected final Term[] m_terms;
        protected final int m_hashCode;

        public AnswerTuple(Term[] terms) {
            m_terms = terms;
            int hashCode = 0;
            for (Term term : terms)
                hashCode = hashCode * 7 + term.hashCode();
            m_hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return m_hashCode;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that)
                return true;
            if (!(that instanceof AnswerTuple))
                return false;
            AnswerTuple thatTuple = (AnswerTuple) that;
            int arity = m_terms.length;
            if (arity != thatTuple.m_terms.length)
                return false;
            for (int index = 0; index < arity; ++index)
                if (!m_terms[index].equals(thatTuple.m_terms[index]))
                    return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append('[');
            for (int index = 0; index < m_terms.length; ++index) {
                if (index != 0)
                    buffer.append(", ");
                buffer.append(m_terms[index].toString());
            }
            buffer.append(']');
            return buffer.toString();
        }
    }

    protected static class QueryChecker implements QueryResultCollector {
        protected final Set<AnswerTuple> m_answerTuples;
        protected final List<AnswerTuple> m_controlTuples;

        public QueryChecker() {
            m_answerTuples = new HashSet<>();
            m_controlTuples = new ArrayList<>();
        }

        @Override
        public void processResult(ConjunctiveQuery conjunctiveQuery, Term[] result) {
            m_answerTuples.add(new AnswerTuple(result.clone()));
        }

        public QueryChecker add(Term... terms) {
            m_controlTuples.add(new AnswerTuple(terms));
            return this;
        }

        public void assertEquals() {
            assertContainsAll(m_answerTuples, m_controlTuples.toArray(new AnswerTuple[m_controlTuples.size()]));
            m_answerTuples.clear();
            m_controlTuples.clear();
        }
    }

    protected static Variable V(String name) {
        return Variable.create(name);
    }

    protected static Individual I(String localName) {
        return Individual.create(NS + localName);
    }

    protected static AtomicConcept CN(String localName) {
        return AtomicConcept.create(NS + localName);
    }

    protected static AtomicRole R(String localName) {
        return AtomicRole.create(NS + localName);
    }

    protected static Atom A(DLPredicate dlPredicate, Term... arguments) {
        return Atom.create(dlPredicate, arguments);
    }

    protected static Term[] TS(Term... terms) {
        return terms;
    }

    protected static Atom[] AS(Atom... atoms) {
        return atoms;
    }

}