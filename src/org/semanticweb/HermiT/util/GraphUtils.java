// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class GraphUtils {

    public static <T> void transitivelyClose(Map<T, Set<T>> relation) {
        // We follow the outline of Warshall's algorithm, which runs in O(n^3),
        // but do our best to avoid the cubic blowup if we can:
        HashSet<T> toProcess = new HashSet<T>();
        for (Set<T> reachable : relation.values()) {
            toProcess.clear();
            toProcess.addAll(reachable);
            toProcess.retainAll(relation.keySet());
            while (!toProcess.isEmpty()) {
                // In the worst case we end up visiting every possible value.
                T intermediate = toProcess.iterator().next();
                toProcess.remove(intermediate);
                for (T fresh : relation.get(intermediate)) {
                    if (reachable.add(fresh) &&
                        relation.containsKey(fresh)) {
                        toProcess.add(fresh);
                    }
                }
            }
        }
    } // end transitivelyClose
    
    public static class CompareByPosition<T> implements Comparator<T> {
        private Map<T, Integer> positions = new HashMap<T, Integer>();
        public CompareByPosition(Collection<T> order) {
            int i = 0;
            for (T t : order) {
                positions.put(t, new Integer(i++));
            }
        }
        public CompareByPosition(Map<T, Integer> positions) {
            this.positions = positions;
        }
        public int compare(T o1, T o2) {
            // We sort any values not in the original ordering last:
            Integer i1 = positions.get(o1);
            Integer i2 = positions.get(o2);
            if (i1 != null && i2 != null) {
                return i1.compareTo(i2);
            } else if (i1 != null) {
                return -1;
            } else if (i2 != null) {
                return 1;
            } else {
                return 0;
            }
        }
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            return (obj instanceof CompareByPosition &&
                    ((CompareByPosition) obj).positions.equals(positions));
        }
    } // end class CompareByPosition
    
    public static <T> Map<T, Set<T>> reversed(Map<T, Set<T>> graph) {
        Map<T, Set<T>> out = new HashMap<T, Set<T>>();
        for (Map.Entry<T, Set<T>> e : graph.entrySet()) {
            for (T t : e.getValue()) {
                Set<T> tPred = out.get(t);
                if (tPred == null) {
                    tPred = new HashSet<T>();
                    out.put(t, tPred);
                }
                tPred.add(e.getKey());
            }
        }
        return out;
    } // end reversed
    
    public static <T> void prune(Map<T, Set<T>> graph) {
        Collection<T> toPrune = new ArrayList<T>();
        for (Map.Entry<T, Set<T>> e : graph.entrySet()) {
            if (e.getValue().isEmpty()) {
                toPrune.add(e.getKey());
            }
        }
        for (T t : toPrune) {
            graph.remove(t);
        }
    }
    
    public static <T> List<T> topologicalSort(Map<T, Set<T>> graph) {
        List<T> out = new ArrayList<T>();
        Map<T, Set<T>> incoming = reversed(graph);
        prune(incoming);
        Set<T> noIncoming = new HashSet<T>(graph.keySet());
        noIncoming.removeAll(incoming.keySet());
        while (!noIncoming.isEmpty()) {
            T t = noIncoming.iterator().next();
            noIncoming.remove(t);
            out.add(t);
            Set<T> successors = graph.get(t);
            if (successors != null) {
                for (T succ : successors) {
                    Set<T> succIncoming = incoming.get(succ);
                    assert succIncoming != null;
                    assert succIncoming.contains(t);
                    succIncoming.remove(t);
                    if (succIncoming.isEmpty()) {
                        incoming.remove(succ);
                        noIncoming.add(succ);
                    }
                }
            }
        }
        if (!incoming.isEmpty()) {
            throw new IllegalArgumentException("Unable to topologically sort a graph containing cycles.");
        }
        return out;
    } // end topologicalSort

    public static <T> Set<Set<T>>
        stronglyConnectedComponents(final Map<T, Set<T>> graph) {
        final Set<Set<T>> out = new HashSet<Set<T>>();
        final List<T> stack = new ArrayList<T>();
        final Set<T> stackSet = new HashSet<T>();
        final Map<T, Integer> indices = new HashMap<T, Integer>();
        final Map<T, Integer> lows = new HashMap<T, Integer>();
        class Tarjan {
            int index = 0;
            void tarjan(T t) {
                Integer tIndex = new Integer(index++);
                indices.put(t, tIndex);
                Integer tLow = tIndex;
                lows.put(t, tLow);
                stack.add(t);
                stackSet.add(t);
                Set<T> successors = graph.get(t);
                if (successors != null) {
                    for (T succ : successors) {
                        if (!indices.containsKey(succ)) {
                            tarjan(succ);
                            Integer succLow = lows.get(succ);
                            assert succLow != null; // set by tarjan(succ)
                            if (tLow < succLow) {
                                lows.put(t, succLow);
                                tLow = succLow;
                            }
                        } else if (stackSet.contains(succ)) {
                            Integer succLow = lows.get(succ);
                            assert succLow != null; // always set if on stack
                            if (tLow < succLow) {
                                lows.put(t, succLow);
                                tLow = succLow;
                            }
                        }
                    }
                }
                if (tLow.equals(tIndex)) {
                    Set<T> scc = new HashSet<T>();
                    T member;
                    do {
                        member = stack.remove(stack.size() - 1);
                        stackSet.remove(member);
                        scc.add(member);
                    } while (member != t);
                    out.add(scc);
                }
            } // end tarjan
        } // end Tarjan class
        Tarjan tar = new Tarjan();
        for (T t : graph.keySet()) {
            if (!indices.containsKey(t)) {
                tar.tarjan(t);
            }
        }
        return out;
    } // end stronglyConnectedComponents
    
    public static class Acyclic<T> {
        public Map<T, Set<T>> graph = new HashMap<T, Set<T>>();
        public Map<T, T> canonical = new HashMap<T, T>();
        public Map<T, Set<T>> equivs = new HashMap<T, Set<T>>();
        public Acyclic(Map<T, Set<T>> graph) {
            for (Set<T> scc : sccs(graph)) {
                assert !scc.isEmpty();
                T canon = scc.iterator().next();
                equivs.put(canon, scc);
                for (T t : scc) {
                    canonical.put(t, canon);
                }
            }
            for (Map.Entry<T, Set<T>> e : graph.entrySet()) {
                T t = canonical.get(e.getKey());
                Set<T> acycSuccessors = this.graph.get(t);
                for (T succ : e.getValue()) {
                    succ = canonical.get(succ);
                    if (succ != t) {
                        if (acycSuccessors == null) {
                            acycSuccessors = new HashSet<T>();
                            this.graph.put(t, acycSuccessors);
                        }
                        acycSuccessors.add(succ);
                    }
                }
            }
        } // end constructor
    } // end class Acyclic
    
    public static class TransAnalyzed<T> {
        public Map<T, Set<T>> reduced = new HashMap<T, Set<T>>();
        public Map<T, Set<T>> closed = new HashMap<T, Set<T>>();
        public TransAnalyzed(Map<T, Set<T>> graph) {
            // System.err.println("analyzing graph with " + String.valueOf(graph.size()) + " nodes");
            List<T> order = topologicalSort(graph);
            // System.err.println("sorted " + String.valueOf(order.size()) + " elements");
            Comparator<T> cmp = new CompareByPosition<T>(order);
            Collections.reverse(order);
            // System.err.println("ordered " + String.valueOf(order.size()) + " elements");
            for (T t : order) {
                Set<T> reached = new HashSet<T>();
                Set<T> tSucc = graph.get(t);
                if (tSucc != null) {
                    Set<T> reducedSucc = reduced.get(t);
                    if (reducedSucc == null) {
                        reducedSucc = new HashSet<T>();
                        reduced.put(t, reducedSucc);
                    }
                    Set<T> closedSucc = closed.get(t);
                    if (closedSucc == null) {
                        closedSucc = new HashSet<T>();
                        closed.put(t, closedSucc);
                    }
                    List<T> successors = new ArrayList<T>(tSucc);
                    Collections.sort(successors, cmp);
                    for (T succ : successors) {
                        if (!reached.contains(succ)) {
                            reducedSucc.add(succ);
                            closedSucc.add(succ);
                            Set<T> reachable = closed.get(succ);
                            if (reachable != null) {
                                for (T desc : reachable) {
                                    if (!reached.contains(desc)) {
                                        reached.add(desc);
                                        closedSucc.add(desc);
                                    }
                                }
                            }
                        }
                    } // end for succ
                } // end if tSucc
            } // end for t
        } // end constructor
    } // end class TransAnalyzed
    
    static class DepthFirstSearch<T> {
        public Map<T, Integer> discovered = new HashMap<T, Integer>();
        public Map<T, Integer> finished = new HashMap<T, Integer>();
        public Map<T, T> predecessor = new HashMap<T, T>();
        int totalTime = 0;
        
        public DepthFirstSearch(Map<T, Set<T>> graph, Collection<T> order) {
            if (order == null) {
                order = graph.keySet();
            }
            for (T t : order) {
                if (!discovered.containsKey(t)) {
                    visit(t, graph);
                }
            }
        }
        
        private void visit(T t, Map<T, Set<T>> graph) {
            discovered.put(t, new Integer(++totalTime));
            Set<T> successors = graph.get(t);
            if (successors != null) {
                for (T u : successors) {
                    if (!discovered.containsKey(u)) {
                        predecessor.put(u, t);
                        visit(u, graph);
                    }
                }
            }
            finished.put(t, new Integer(++totalTime));
        }
    }
    
    static <T> Set<Set<T>> sccs(Map<T, Set<T>> graph) {
        final Map<T, Integer> positions = new DepthFirstSearch<T>(graph, null).finished;
        final List<T> elements = new ArrayList<T>(positions.keySet());
        // for (T t : elements) { System.out.print(t); System.out.print(" "); }
        // System.out.println("");
        Collections.sort(elements, new CompareByPosition<T>(positions));
        Collections.reverse(elements);
        // for (T t : elements) { System.out.print(t); System.out.print(" "); }
        // System.out.println("");
        final Map<T, T> predecessor = new DepthFirstSearch<T>(reversed(graph), elements).predecessor;
        // for (Map.Entry<T,T> e : predecessor.entrySet()) {
        //     System.out.println(String.valueOf(e.getKey()) + " <- " + String.valueOf(e.getValue()));
        // }
        class Components {
            Map<T, Set<T>> map = new HashMap<T, Set<T>>();
            Set<T> component(T t) {
                Set<T> out = map.get(t);
                if (out == null) {
                    T pred = predecessor.get(t);
                    if (pred == null) {
                        out = new HashSet<T>();
                    } else {
                        out = component(pred);
                    }
                    out.add(t);
                    map.put(t, out);
                }
                return out;
            }
            Components() {
                for (T t : elements) {
                    component(t);
                }
            }
        }
        return new HashSet<Set<T>>(new Components().map.values());
    }
    
    static <T> void printGraph(Map<T, Set<T>> graph) {
        for (Map.Entry<T, Set<T>> e : graph.entrySet()) {
            for (T t : e.getValue()) {
                System.out.print(e.getKey());
                System.out.print(" -> ");
                System.out.println(t);
            }
        }
    }
    
    public static void main(String[] args) {
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
        // r.add(0, 0);
        // r.add(1, 1);
        // r.add(2, 2);
        r.add(0, 1);
        // r.add(2, 1);
        r.add(1, 2);
        printGraph(r.map);
        System.out.println("reversed:");
        printGraph(reversed(r.map));
        for (Set<Integer> comp : sccs(r.map)) {
            System.out.println("component:");
            for (Integer i : comp) {
                System.out.println(i);
            }
        }
        Acyclic<Integer> acyc = new Acyclic<Integer>(r.map);
        System.out.println("acyclic:");
        printGraph(acyc.graph);
        List<Integer> sorted = topologicalSort(acyc.graph);
        for (Integer i : sorted) {
            System.out.println(i);
        }
    }

}
