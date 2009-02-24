// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Classifier<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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
    
    static private <T> Collection<T> just(T t) {
        Collection<T> out = new ArrayList<T>(1);
        out.add(t);
        return out;
    }
    
   public HierarchyPosition<T>
       findPosition(T value, HierarchyPosition<T> hierTop,
                             HierarchyPosition<T> hierBottom) {
        Set<HierarchyPosition<T>> supers = findDirectSupers(value, hierTop);
        Set<HierarchyPosition<T>> subs = findDirectSubs(value, hierBottom, supers);
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
        return search(util, just(top), null);
    }
    
    protected Set<HierarchyPosition<T>>
        findDirectSubs(final T value, HierarchyPosition<T> hierBottom,
                        Set<HierarchyPosition<T>> supers) {
        if (supers.size() == 1 &&
            func.doesSubsume(value,
                supers.iterator().next().getEquivalents()
                                            .iterator().next())) {
            return supers;
        } else {
            Iterator<HierarchyPosition<T>> i = supers.iterator();
            Set<HierarchyPosition<T>> marked
                = new HashSet<HierarchyPosition<T>>(i.next().getDescendantPositions());
            while (i.hasNext()) {
                Set<HierarchyPosition<T>> freshlyMarked
                    = new HashSet<HierarchyPosition<T>>();
                Set<HierarchyPosition<T>> visited
                    = new HashSet<HierarchyPosition<T>>();
                Queue<HierarchyPosition<T>> q
                    = new LinkedList<HierarchyPosition<T>>();
                q.add(i.next());
                while (!q.isEmpty()) {
                    for (HierarchyPosition<T> pos
                            : q.remove().getChildPositions()) {
                        if (marked.contains(pos)) {
                            freshlyMarked.add(pos);
                        } else if (visited.add(pos)) {
                            q.add(pos);
                        }
                    }
                }
                q.addAll(freshlyMarked);
                while (!q.isEmpty()) {
                    for (HierarchyPosition<T> pos
                            : q.remove().getChildPositions()) {
                        if (freshlyMarked.add(pos)) {
                            q.add(pos);
                        }
                    }
                }
                marked = freshlyMarked;
            }
            Set<HierarchyPosition<T>> bots
                = new HashSet<HierarchyPosition<T>>();
            for (HierarchyPosition<T> pos : marked) {
                if (pos.getChildPositions().contains(hierBottom) &&
                    func.doesSubsume(value,
                                pos.getEquivalents().iterator().next())) {
                    bots.add(pos);
                }
            }
            Util<HierarchyPosition<T>> util
                = new Util<HierarchyPosition<T>>() {
                public Set<HierarchyPosition<T>>
                    nexts(HierarchyPosition<T> u)
                    { return u.getParentPositions(); }
                public Set<HierarchyPosition<T>>
                    prevs(HierarchyPosition<T> u)
                    { return u.getChildPositions(); }
                public boolean trueOf(HierarchyPosition<T> u) {
                    return func.doesSubsume(value,
                                u.getEquivalents().iterator().next());
                }
            };
            if (bots.isEmpty()) {
                return new HashSet<HierarchyPosition<T>>(just(hierBottom));
            } else {
                return search(util, bots, marked);
            }
        }
    }
    
    protected interface Util<U> {
        Set<U> nexts(U u);
        Set<U> prevs(U u);
        boolean trueOf(U u);
    }
    
    protected static final class Local<U> {
        final Util<U> f;
        final Set<U> possibilities;
        
        public Local(Util<U> f, Set<U> possibilities) {
            this.f = f;
            this.possibilities = possibilities;
        }
        
        Set<U> positives = new HashSet<U>();
        Set<U> negatives = new HashSet<U>();
        boolean trueOf(U pos) {
            if (positives.contains(pos)) {
                return true;
            } else if (negatives.contains(pos) ||
                        (possibilities != null &&
                            !possibilities.contains(pos))) {
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
    protected <U> Set<U>
        search(final Util<U> f, Collection<U> begin,
                final Set<U> possibilities) {
        final Local<U> g = new Local<U>(f, possibilities);
        
        Set<U> out = new HashSet<U>();
        Set<U> visited = new HashSet<U>(begin);
        Queue<U> q = new LinkedList<U>(visited);
        while (!q.isEmpty()) {
            U cur = q.remove();
            boolean foundNext = false;
            for (U next : f.nexts(cur)) {
                if (g.trueOf(next)) {
                    foundNext = true;
                    if (visited.add(next)) q.add(next);
                }
            }
            if (!foundNext) out.add(cur);
        }
        return out;
    }
    
}