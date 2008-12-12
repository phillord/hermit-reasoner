// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class Classifier<T> {
    public interface Function<T> {
        boolean doesSubsume(T parent, T child);
    }
    
    protected Function<T> func;
    
    public Classifier(Function<T> func) {
        this.func = func;
    }
    
    public Map<T, HierarchyPosition<T>>
        buildHierarchy(T top, T bottom, Collection<T> values) {
        Map<T, HierarchyPosition<T>> out
            = new HashMap<T, HierarchyPosition<T>>();
        NaiveHierarchyPosition<T> topPos
            = new NaiveHierarchyPosition<T>(top);
        out.put(top, topPos);
        if (func.doesSubsume(bottom, top)) {
            topPos.labels.add(bottom);
            out.put(bottom, topPos);
            for (T t : values) {
                topPos.labels.add(t);
                out.put(t, topPos);
            }
        } else {
            NaiveHierarchyPosition<T> botPos
                = new NaiveHierarchyPosition<T>(bottom);
            out.put(bottom, botPos);
            botPos.parents.add(topPos);
            topPos.children.add(botPos);
            for (T t : values) {
                HierarchyPosition<T> pos = findPosition(t, topPos, botPos);
                out.put(t, pos);
                if (!pos.getEquivalents().contains(t)) {
                    // Existing node: just add t to its label
                    ((NaiveHierarchyPosition<T>) pos).labels.add(t);
                } else {
                    // New node: insert it into the hierarchy
                    for (HierarchyPosition<T> parent
                            : pos.getParentPositions()) {
                        NaiveHierarchyPosition<T> p
                            = (NaiveHierarchyPosition<T>) parent;
                        p.children.add(pos);
                        p.children.removeAll(pos.getChildPositions());
                    }
                    for (HierarchyPosition<T> child
                            : pos.getChildPositions()) {
                        NaiveHierarchyPosition<T> c
                            = (NaiveHierarchyPosition<T>) child;
                        c.parents.add(pos);
                        c.parents.removeAll(pos.getParentPositions());
                    }
                }
            }
        }
        return out;
    }
    
   public HierarchyPosition<T>
       findPosition(T value, HierarchyPosition<T> hierTop,
                             HierarchyPosition<T> hierBottom) {
        Set<HierarchyPosition<T>> supers = findDirectSupers(value, hierTop);
        Set<HierarchyPosition<T>> subs = findDirectSubs(value, hierBottom);
        if (supers.equals(subs)) {
            assert supers.size() == 1 && subs.size() == 1;
            return supers.iterator().next();
        } else {
            return new NaiveHierarchyPosition<T>(value, supers, subs);
        }
    }
    
    
    protected Set<HierarchyPosition<T>>
        findDirectSupers(final T value, HierarchyPosition<T> top) {
        Util<HierarchyPosition<T>> util = new Util<HierarchyPosition<T>>() {
            public Set<HierarchyPosition<T>> nexts(HierarchyPosition<T> u)
                { return u.getChildPositions(); }
            public Set<HierarchyPosition<T>> prevs(HierarchyPosition<T> u)
                { return u.getParentPositions(); }
            public boolean trueOf(HierarchyPosition<T> u) {
                return func.doesSubsume(u.getEquivalents().iterator().next(),
                                        value);
            }
        };
        return search(util, top);
    }
    
    protected Set<HierarchyPosition<T>>
        findDirectSubs(final T value, HierarchyPosition<T> bot) {
        Util<HierarchyPosition<T>> util = new Util<HierarchyPosition<T>>() {
            public Set<HierarchyPosition<T>> nexts(HierarchyPosition<T> u)
                { return u.getParentPositions(); }
            public Set<HierarchyPosition<T>> prevs(HierarchyPosition<T> u)
                { return u.getChildPositions(); }
            public boolean trueOf(HierarchyPosition<T> u) {
                return func.doesSubsume(value,
                                        u.getEquivalents().iterator().next());
            }
        };
        return search(util, bot);
    }
    
    protected interface Util<U> {
        Set<U> nexts(U u);
        Set<U> prevs(U u);
        boolean trueOf(U u);
    }
    
    protected <U> Set<U>
        search(final Util<U> f, U begin) {
        class Local {
            Set<U> positives = new HashSet<U>();
            Set<U> negatives = new HashSet<U>();
            boolean trueOf(U pos) {
                if (positives.contains(pos)) {
                    return true;
                } else if (negatives.contains(pos)) {
                    return false;
                } else {
                    for (U prev : f.prevs(pos)) {
                        if (!trueOf(prev)) {
                            negatives.add(pos);
                            return false;
                        }
                    }
                    if (f.trueOf(pos)) {
                        positives.add(pos);
                        return true;
                    } else {
                        negatives.add(pos);
                        return false;
                    }
                }
            }
        };
        Local g = new Local();
        
        Set<U> out = new HashSet<U>();
        Set<U> visited = new HashSet<U>();
        Queue<U> q = new LinkedList<U>();
        q.add(begin);
        while (!q.isEmpty()) {
            U cur = q.remove();
            if (visited.add(cur)) {
                boolean foundNext = false;
                for (U next : f.nexts(cur)) {
                    if (g.trueOf(next)) {
                        foundNext = true;
                        q.add(next);
                    }
                }
                if (!foundNext) out.add(cur);
            }
        }
        return out;
    }
    
}