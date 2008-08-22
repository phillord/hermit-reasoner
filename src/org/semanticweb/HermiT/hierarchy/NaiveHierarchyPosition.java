// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
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
}
