package org.semanticweb.HermiT.tableau;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DescriptionGraph;

public class GraphTest extends AbstractReasonerInternalsTest {
    protected Set<DescriptionGraph> m_descriptionGraphs;

    public GraphTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        m_descriptionGraphs=new HashSet<DescriptionGraph>();
    }
    
    public void testGraphMerging() throws Exception {
        DescriptionGraph graph=G(
            new String[] {
                "A", // 0
                "B", // 1
                "C", // 2
            },
            new DescriptionGraph.Edge[] {
                E("R",0,1),
                E("R",1,2),
            },
            new String[] {
                "A","B","C",
            }
        );
        m_descriptionGraphs.add(graph);
        loadOntologyWithAxioms("");
        Tableau tableau=getTableau(m_descriptionGraphs);
        tableau.clear();
        ExtensionManager extensionManager=tableau.getExtensionManager();
        DependencySet emptySet=tableau.getDependencySetFactory().emptySet();
        Node n1=tableau.createNewNINode(emptySet);
        Node n2=tableau.createNewNINode(emptySet);
        Node n3=tableau.createNewNINode(emptySet);
        Node n4=tableau.createNewNINode(emptySet);
        Node n5=tableau.createNewNINode(emptySet);
        Node n6=tableau.createNewNINode(emptySet);
        AtomicConcept r=AtomicConcept.create("R");
        AtomicConcept s=AtomicConcept.create("S");
        extensionManager.addTuple(new Object[] { graph,n1,n2,n3 },emptySet,false);
        extensionManager.addTuple(new Object[] { graph,n4,n5,n6 },emptySet,false);
        extensionManager.addConceptAssertion(r,n1,emptySet,false);
        extensionManager.addConceptAssertion(s,n6,emptySet,false);

        // The following tuple should make the existing two tuples to merge
        Node n7=tableau.createNewNINode(emptySet);
        extensionManager.addTuple(new Object[] { graph,n1,n7,n6 },emptySet,false);

        // No merging should occur automatically
        assertTrue(extensionManager.containsTuple(new Object[] { graph,n1,n2,n3 }));
        assertTrue(extensionManager.containsTuple(new Object[] { graph,n4,n5,n6 }));
        assertTrue(extensionManager.containsTuple(new Object[] { graph,n1,n7,n6 }));
        
        // Merging occurs only if we start the saturation
        assertTrue(tableau.runCalculus());

        // Now do the checking
        assertSame(n1,n1.getCanonicalNode());
        assertSame(n7,n2.getCanonicalNode());
        assertSame(n6,n3.getCanonicalNode());
        assertSame(n1,n4.getCanonicalNode());
        assertSame(n7,n5.getCanonicalNode());
        assertSame(n6,n6.getCanonicalNode());
        assertSame(n7,n7.getCanonicalNode());
        assertLabel(tableau,n1,r);
        assertLabel(tableau,n5);
        assertLabel(tableau,n6,s);

        assertTrue(extensionManager.containsTuple(new Object[] { graph,n1,n7,n6 }));
    }
    
//    public void testContradictionOnGraph() throws Exception {
//        DescriptionGraph graph=G(
//            new String[] {
//                "A", // 0
//                "B", // 1
//            },
//            new DescriptionGraph.Edge[] {
//                E("R",0,1),
//            },
//            new String[] {
//                "A","B",
//            }
//        );
//        m_descriptionGraphs.add(graph);
////        m_ontologyManager.addAxiom(m_ontology, axiom);
//        addAxiom(
//            "[rule ["+
//                "[[pred owl:sameAs] X Y]"+
//             "] ["+
//                "[[oprop R] X Y]"+
//            "]]"
//        );
//        Tableau tableau=getTableau();
//        tableau.clear();
//        ExtensionManager extensionManager=tableau.getExtensionManager();
//        DependencySet emptySet=tableau.getDependencySetFactory().emptySet();
//        Node n1=tableau.createNewRootNode(emptySet,0);
//        Node n2=tableau.createNewRootNode(emptySet,0);
//        AtomicRole r=AtomicRole.createObjectRole("R");
//        extensionManager.addTuple(new Object[] { graph,n1,n2 },emptySet);
//        extensionManager.addRoleAssertion(r,n1,n2,emptySet);
//        
//        assertFalse(tableau.isSatisfiable());
//    }
    
    public void testGraph1() throws Exception {
        m_descriptionGraphs.add(G(
            new String[] {
                "A", // 0
                "B", // 1
                "C", // 2
                "A", // 3
            },
            new DescriptionGraph.Edge[] {
                E("R",0,1),
                E("R",3,2),
            },
            new String[] {
                "A",
            }
        ));
        
        String axioms="SubClassOf(:A ObjectSomeValuesFrom(:S :A))"
                + "SubClassOf(:A ObjectSomeValuesFrom(:S :D))"
                + "SubClassOf(:B ObjectSomeValuesFrom(:T :A))"
                + "SubClassOf(:C ObjectSomeValuesFrom(:T :A))"
                + "FunctionalObjectProperty(:S)"
                + "ClassAssertion(:A :i)";
        loadOntologyWithAxioms(axioms);
        Configuration c = new Configuration();
        c.directBlockingType = Configuration.DirectBlockingType.PAIR_WISE;
        c.blockingStrategyType = Configuration.BlockingStrategyType.ANYWHERE;
        c.existentialStrategyType = Configuration.ExistentialStrategyType.CREATION_ORDER;
        createReasoner(c, m_descriptionGraphs);

        assertABoxSatisfiable(true);
    }
    
//    public void testGraph2() throws Exception {
//        m_descriptionGraphs.add(G(
//            new String[] {
//                "LP", // 0
//                "RP", // 1
//                "P",  // 2
//                "P",  // 3
//            },
//            new DescriptionGraph.Edge[] {
//                E("S",0,1),
//                E("R",0,2),
//                E("R",1,3),
//            },
//            new String[] {
//                "P",
//            }
//        ));
//        
//        addAxiom("[subClassOf A [some T P]]");
//        addAxiom("[subClassOf [some T D] B]");
//        addAxiom(
//            "[rule ["+
//                "[[oprop conn] V W]"+
//             "] ["+
//                "[[desc P] V] [[oprop R] X V] [[desc LP] X] [[oprop S] X Y] [[desc RP] Y] [[oprop R] Y W] [[desc P] W]"+
//            "]]"
//        );
//        addAxiom(
//            "[rule ["+
//                "[[desc D] X]"+
//             "] ["+
//                "[[oprop conn] X Y]"+
//            "]]"
//        );
//        addAxiom(
//            "[rule ["+
//                "[[desc D] Y]"+
//             "] ["+
//                "[[oprop conn] X Y]"+
//            "]]"
//        );
//        
//        assertSubsumedBy("A","B",true);
//    }
    
    protected static DescriptionGraph G(String[] vertexAtomicConcepts,DescriptionGraph.Edge[] edges,String[] startAtomicConcepts) {
        AtomicConcept[] atomicConceptsByVertices=new AtomicConcept[vertexAtomicConcepts.length];
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
