package org.semanticweb.HermiT.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

public class GraphTest extends AbstractReasonerTest {

    protected Set<DescriptionGraph> m_descriptionGraphs;

    public GraphTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
        m_descriptionGraphs = new HashSet<DescriptionGraph>();
    }

//    public void testTransClosure() throws Exception {
//        Graph<Integer> g = new Graph<Integer>();
//        add(g, 0, 1);
//        add(g, 1, 2);
//        add(g, 2, 9);
//        add(g, 9, 8);
//        add(g, 8, 7);
//        add(g, 7, 6);
//        g.transitivelyClose();
//
//        assertContainsAll(g.getSuccessors(0), 1, 2, 6, 7, 8, 9);
//        assertContainsAll(g.getSuccessors(1), 2, 6, 7, 8, 9);
//        assertContainsAll(g.getSuccessors(2), 6, 7, 8, 9);
//        assertContainsAll(g.getSuccessors(9), 6, 7, 8);
//        assertContainsAll(g.getSuccessors(8), 6, 7);
//        assertContainsAll(g.getSuccessors(7), 6);
//    }
//
//    public void testGraphMerging() throws Exception {
//        DescriptionGraph graph = G(new String[] { GraphTest.NS+"A", // 0
//                GraphTest.NS+"B", // 1
//                GraphTest.NS+"C", // 2
//        }, new DescriptionGraph.Edge[] { E(GraphTest.NS+"R", 0, 1), E(GraphTest.NS+"R", 1, 2), },
//                new String[] { GraphTest.NS+"A", GraphTest.NS+"B", GraphTest.NS+"C", });
//        m_descriptionGraphs.add(graph);
//        Tableau tableau = getTableau(m_descriptionGraphs);
//        tableau.clear();
//        ExtensionManager extensionManager = tableau.getExtensionManager();
//        DependencySet emptySet = tableau.getDependencySetFactory().emptySet();
//        Node n1 = tableau.createNewNamedNode(emptySet);
//        Node n2 = tableau.createNewNamedNode(emptySet);
//        Node n3 = tableau.createNewNamedNode(emptySet);
//        Node n4 = tableau.createNewNamedNode(emptySet);
//        Node n5 = tableau.createNewNamedNode(emptySet);
//        Node n6 = tableau.createNewNamedNode(emptySet);
//        AtomicConcept r = AtomicConcept.create(GraphTest.NS+"R");
//        AtomicConcept s = AtomicConcept.create(GraphTest.NS+"S");
//        extensionManager.addTuple(new Object[] { graph, n1, n2, n3 }, emptySet, true);
//        extensionManager.addTuple(new Object[] { graph, n4, n5, n6 }, emptySet, true);
//        extensionManager.addConceptAssertion(r, n1, emptySet, true);
//        extensionManager.addConceptAssertion(s, n6, emptySet, true);
//
//        // The following tuple should make the existing two tuples to merge
//        Node n7 = tableau.createNewNamedNode(emptySet);
//        extensionManager.addTuple(new Object[] { graph, n1, n7, n6 }, emptySet, true);
//
//        // No merging should occur automatically
//        assertTrue(extensionManager.containsTuple(new Object[] { graph, n1, n2, n3 }));
//        assertTrue(extensionManager.containsTuple(new Object[] { graph, n4, n5, n6 }));
//        assertTrue(extensionManager.containsTuple(new Object[] { graph, n1, n7, n6 }));
//
//        // Merging occurs only if we start the saturation
//        assertTrue(tableau.isSatisfiable());
//
//        // Now do the checking
//        assertSame(n1, n1.getCanonicalNode());
//        assertSame(n7, n2.getCanonicalNode());
//        assertSame(n6, n3.getCanonicalNode());
//        assertSame(n1, n4.getCanonicalNode());
//        assertSame(n7, n5.getCanonicalNode());
//        assertSame(n6, n6.getCanonicalNode());
//        assertSame(n7, n7.getCanonicalNode());
////        assertContainsAll(n1.getPositiveLabel(), r);
////        assertContainsAll(n5.getPositiveLabel());
////        assertContainsAll(n6.getPositiveLabel(), s);
//
//        assertTrue(extensionManager.containsTuple(new Object[] { graph, n1, n7, n6 }));
//    }
//
    public void testContradictionOnGraph() throws Exception {
        DescriptionGraph graph=G(
            new String[] {
                GraphTest.NS+"A", // 0
                GraphTest.NS+"B", // 1
            },
            new DescriptionGraph.Edge[] {
                E(GraphTest.NS+"R",0,1),
            },
            new String[] {
                    GraphTest.NS+"A",GraphTest.NS+"B",
            }
        );
        m_descriptionGraphs.add(graph);
        String axioms = "ClassAssertion(:A :a) ClassAssertion(:B :b) DisjointClasses(:A :B) ObjectPropertyAssertion(:r :a :b)"
            // R(x, y) -> SameIndividual(x, y)
            + "DLSafeRule(Body(ObjectPropertyAtom(:R Variable(:x) Variable(:y))) Head(SameIndividualAtom(Variable(:x) Variable(:y))))";
        loadOntologyWithAxioms(axioms);

        Tableau tableau=getTableau(m_descriptionGraphs);
        Individual i1=Individual.create("i1");
        Individual i2=Individual.create("i2");
        AtomicRole r=AtomicRole.create(GraphTest.NS + "R");
        Set<Atom> positiveFacts=new HashSet<Atom>();
        positiveFacts.add(Atom.create(graph,i1,i2));
        positiveFacts.add(Atom.create(r,i1,i2));
        assertFalse(tableau.isSatisfiable(false,false,positiveFacts,null,null,null,null,ReasoningTaskDescription.isABoxSatisfiable()));
    }

    public void testGraph1() throws Exception {
        m_descriptionGraphs.add(G(
            new String[] {
                GraphTest.NS+"A", // 0
                GraphTest.NS+"B", // 1
                GraphTest.NS+"C", // 2
                GraphTest.NS+"A", // 3
            },
            new DescriptionGraph.Edge[] {
                E(GraphTest.NS+"R",0,1),
                E(GraphTest.NS+"R",3,2),
            },
            new String[] {
                GraphTest.NS+"A",
            }
        ));

        String axioms="SubClassOf(:A ObjectSomeValuesFrom(:S :A))"
            + "SubClassOf(:A ObjectSomeValuesFrom(:S :D))"
            + "SubClassOf(:B ObjectSomeValuesFrom(:T :A))"
            + "SubClassOf(:C ObjectSomeValuesFrom(:T :A))"
            + "FunctionalObjectProperty(:S)"
            + "ClassAssertion(:A :i)";
        loadOntologyWithAxioms(axioms);
        Tableau tableau=getTableau(m_descriptionGraphs);
        assertTrue(tableau.isSatisfiable(true,true,null,null,null,null,null,ReasoningTaskDescription.isABoxSatisfiable()));
    }

    public void testGraph2() throws Exception {
        m_descriptionGraphs.add(G(
            new String[] {
                GraphTest.NS+"LP", // 0
                GraphTest.NS+"RP", // 1
                GraphTest.NS+"P",  // 2
                GraphTest.NS+"P",  // 3
            },
            new DescriptionGraph.Edge[] {
                E(GraphTest.NS+"S",0,1),
                E(GraphTest.NS+"R",0,2),
                E(GraphTest.NS+"R",1,3),
            },
            new String[] {
                GraphTest.NS+"P",
            }
        ));

        String axioms="SubClassOf(:A ObjectSomeValuesFrom(:T :P))"
            + "SubClassOf(ObjectSomeValuesFrom(:T :D) :B)"
            //P(v), R(x,v), LP(x), S(x,y), RP(y), R(y,w), P(w) -> conn(v,w)
            + "DLSafeRule(Body("
                    + "ClassAtom(:P Variable(:v)) "
            		+ "ObjectPropertyAtom(:R Variable(:x) Variable(:v))"
            		+ "ClassAtom(:LP Variable(:x)) "
            		+ "ObjectPropertyAtom(:S Variable(:x) Variable(:y))"
            		+ "ClassAtom(:RP Variable(:y)) "
            		+ "ObjectPropertyAtom(:R Variable(:y) Variable(:w))"
            		+ "ClassAtom(:P Variable(:w)) "
            		+ ") Head(ObjectPropertyAtom(:conn Variable(:v) Variable(:w))))"
             // conn(x,y) -> D(x)
             + "DLSafeRule(Body(ObjectPropertyAtom(:conn Variable(:x) Variable(:y))) Head(ClassAtom(:D Variable(:x))))"
             // conn(x,y) -> D(y)
             + "DLSafeRule(Body(ObjectPropertyAtom(:conn Variable(:x) Variable(:y))) Head(ClassAtom(:D Variable(:y))))";
        loadOntologyWithAxioms(axioms);
        Tableau t=getTableau(m_descriptionGraphs);
        Individual freshNode=Individual.create("ind");
        assertTrue(!t.isSatisfiable(false,Collections.singleton(Atom.create(AtomicConcept.create(GraphTest.NS+"A"),freshNode)),null,null,Collections.singleton(Atom.create(AtomicConcept.create(GraphTest.NS+"B"),freshNode)),null,ReasoningTaskDescription.isABoxSatisfiable()));
    }

    protected static void add(Graph<Integer> graph,int from,int... successors) {
        for (int successor : successors)
            graph.addEdge(from,successor);
    }
    protected static DescriptionGraph G(String[] vertexAtomicConcepts,DescriptionGraph.Edge[] edges,String[] startAtomicConcepts) {
        AtomicConcept[] atomicConceptsByVertices = new AtomicConcept[vertexAtomicConcepts.length];
        for (int index=0;index<vertexAtomicConcepts.length;index++)
            atomicConceptsByVertices[index]=AtomicConcept.create(vertexAtomicConcepts[index]);
        Set<AtomicConcept> startConcepts=new HashSet<AtomicConcept>();
        for (String atomicConcept : startAtomicConcepts)
            startConcepts.add(AtomicConcept.create(atomicConcept));
        return new DescriptionGraph("G",atomicConceptsByVertices,edges,startConcepts);
    }
    protected static DescriptionGraph.Edge E(String atomicRoleName,int from,int to) {
        AtomicRole atomicRole=AtomicRole.create(atomicRoleName);
        return new DescriptionGraph.Edge(atomicRole,from,to);
    }
}
