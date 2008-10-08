// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

public class NaiveHierarchyPosition<T> implements HierarchyPosition<T> {
    public Set<HierarchyPosition<T>> parents;
    public Set<HierarchyPosition<T>> children;
    public Set<T> labels;
    public NaiveHierarchyPosition() {
        parents = new HashSet<HierarchyPosition<T>>();
        children = new HashSet<HierarchyPosition<T>>();
        labels = new HashSet<T>();
    }
    public Set<T> getEquivalents() {
        return labels;
    }
    public Set<T> getAncestors() {
        Set<T> output = new HashSet<T>();
        for (HierarchyPosition<T> current : getAncestorPositions()) {
            // output.addAll(current.getEquivalents()) would be sufficient modulo debugging
            for (T t : current.getEquivalents()) {
                if (!output.add(t)) {
                    throw new RuntimeException("'" + t.toString() + "' appears in multiple positions in the same hierarchy");
                }
            }
        }
        return output;
    }
    public Set<T> getDescendants() {
        Set<T> output = new HashSet<T>();
        for (HierarchyPosition<T> current : getDescendantPositions()) {
            // output.addAll(current.getEquivalents()) would be sufficient modulo debugging
            for (T t : current.getEquivalents()) {
                if (!output.add(t)) {
                    throw new RuntimeException("'" + t.toString() + "' appears in multiple positions in the same hierarchy");
                }
            }
        }
        return output;
    }
    public Set<HierarchyPosition<T>> getParentPositions() {
        return parents;
    }
    public Set<HierarchyPosition<T>> getChildPositions() {
        return children;
    }
    public Set<HierarchyPosition<T>> getAncestorPositions() {
        Set<HierarchyPosition<T>> output = new HashSet<HierarchyPosition<T>>();
        Queue<HierarchyPosition<T>> toVisit = new LinkedList<HierarchyPosition<T>>();
        for (HierarchyPosition<T> current = this; current != null; current = toVisit.poll()) {
            if (output.add(current)) {
                toVisit.addAll(current.getParentPositions());
            }
        }
        return output;
    }
    public Set<HierarchyPosition<T>> getDescendantPositions() {
        Set<HierarchyPosition<T>> output = new HashSet<HierarchyPosition<T>>();
        Queue<HierarchyPosition<T>> toVisit = new LinkedList<HierarchyPosition<T>>();
        for (HierarchyPosition<T> current = this; current != null; current = toVisit.poll()) {
            if (output.add(current)) {
                toVisit.addAll(current.getChildPositions());
            }
        }
        return output;
    }
    
    Set<HierarchyPosition<T>> topSearch(T val, Ordering<T> cmp) {
        Set<HierarchyPosition<T>> out = new HashSet<HierarchyPosition<T>>();
        Set<HierarchyPosition<T>> visited = new HashSet<HierarchyPosition<T>>();
        Queue<HierarchyPosition<T>> q = new LinkedList<HierarchyPosition<T>>();
        q.add(this);
        while (!q.isEmpty()) {
            HierarchyPosition<T> cur = q.remove();
            if (visited.add(cur)) {
                boolean foundMoreSpecific = false;
                for (HierarchyPosition<T> child : cur.getChildPositions()) {
                    if (cmp.less(child.getEquivalents().iterator().next(), val)) {
                        foundMoreSpecific = true;
                        q.add(child);
                    }
                }
                if (!foundMoreSpecific) {
                    out.add(cur);
                }
            }
        }
        return out;
    }

    Set<HierarchyPosition<T>> botSearch(T val, Ordering<T> cmp) {
        Set<HierarchyPosition<T>> out = new HashSet<HierarchyPosition<T>>();
        Set<HierarchyPosition<T>> visited = new HashSet<HierarchyPosition<T>>();
        Queue<HierarchyPosition<T>> q = new LinkedList<HierarchyPosition<T>>();
        q.add(this);
        while (!q.isEmpty()) {
            HierarchyPosition<T> cur = q.remove();
            if (visited.add(cur)) {
                boolean foundMoreGeneral = false;
                for (HierarchyPosition<T> parent : cur.getParentPositions()) {
                    if (cmp.less(val, parent.getEquivalents().iterator().next())) {
                        foundMoreGeneral = true;
                        q.add(parent);
                    }
                }
                if (!foundMoreGeneral) {
                    out.add(cur);
                }
            }
        }
        return out;
    }
    
    public static <T> Map<T, HierarchyPosition<T>> buildHierarchy(T topVal, T botVal,
        Collection<T> values, Ordering<T> cmp) {
        if (!cmp.less(botVal, topVal)) {
            throw new RuntimeException("hierarchy top must be at least as big as hierarchy bottom");
        }
        Map<T, HierarchyPosition<T>> out = new HashMap<T, HierarchyPosition<T>>();
        NaiveHierarchyPosition<T> top = new NaiveHierarchyPosition<T>();
        top.labels.add(topVal);
        out.put(topVal, top);
        if (cmp.less(topVal, botVal)) {
            top.labels.add(botVal);
            out.put(botVal, top);
            for (T t : values) {
                top.labels.add(t);
                out.put(t, top);
            }
        } else {
            NaiveHierarchyPosition<T> bot = new NaiveHierarchyPosition<T>();
            bot.labels.add(botVal);
            out.put(botVal, bot);
            top.children.add(bot);
            bot.parents.add(top);
            for (T t : values) {
                Set<HierarchyPosition<T>> parents = top.topSearch(t, cmp);
                Set<HierarchyPosition<T>> children = bot.botSearch(t, cmp);
                if (parents.equals(children)) {
                    assert parents.size() == 1;
                    HierarchyPosition<T> pos = parents.iterator().next();
                    ((NaiveHierarchyPosition<T>) pos).labels.add(t);
                    out.put(t, pos);
                } else {
                    NaiveHierarchyPosition<T> pos = new NaiveHierarchyPosition<T>();
                    pos.parents = parents;
                    pos.children = children;
                    pos.labels.add(t);
                    out.put(t, pos);
                }
            }
        }
        return out;
    }
    
    static public interface Ordering<T> {
        boolean less(T lhs, T rhs);
    }
    
}
