package org.semanticweb.HermiT.util;

import junit.framework.TestCase;

import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

public class GraphUtilsTest extends TestCase {
    public GraphUtilsTest(String name) {
        super(name);
    }
    
    public void testTransClosure() throws Exception {
        class Relation {
            public Map<Integer, Set<Integer>> map;
            public Relation() {
                map = new TreeMap<Integer, Set<Integer>>();
            }
            public void add(int node, int... successors) {
                Set<Integer> neighbors = map.get(new Integer(node));
                if (neighbors == null) {
                    neighbors = new TreeSet<Integer>();
                    map.put(new Integer(node), neighbors);
                }
                for (int i : successors) neighbors.add(new Integer(i));
            }
        }
        Relation r = new Relation();
        r.add(0, 1);
        r.add(1, 2);
        r.add(2, 9);
        r.add(9, 8);
        r.add(8, 7);
        r.add(7, 6);
        GraphUtils.transitivelyClose(r.map);

        Relation s = new Relation();
        s.add(0, 1, 2, 6, 7, 8, 9);
        s.add(1, 2, 6, 7, 8, 9);
        s.add(2, 6, 7, 8, 9);
        s.add(9, 6, 7, 8);
        s.add(8, 6, 7);
        s.add(7, 6);

        assertEquals(r.map, s.map);
    }
    
}
