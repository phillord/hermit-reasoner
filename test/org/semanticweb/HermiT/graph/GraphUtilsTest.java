package org.semanticweb.HermiT.graph;

import org.semanticweb.HermiT.AbstractHermiTTest;
import org.semanticweb.HermiT.graph.Graph;

public class GraphUtilsTest extends AbstractHermiTTest {

    public GraphUtilsTest(String name) {
        super(name);
    }
    public void testTransClosure() throws Exception {
        Graph<Integer> g=new Graph<Integer>();
        add(g,0,1);
        add(g,1,2);
        add(g,2,9);
        add(g,9,8);
        add(g,8,7);
        add(g,7,6);
        g.transitivelyClose();

        assertContainsAll(g.getSuccessors(0),1,2,6,7,8,9);
        assertContainsAll(g.getSuccessors(1),2,6,7,8,9);
        assertContainsAll(g.getSuccessors(2),6,7,8,9);
        assertContainsAll(g.getSuccessors(9),6,7,8);
        assertContainsAll(g.getSuccessors(8),6,7);
        assertContainsAll(g.getSuccessors(7),6);
    }
    protected static void add(Graph<Integer> graph,int from,int... successors) {
        for (int successor : successors)
            graph.addEdge(from,successor);
    }
}
