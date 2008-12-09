// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
    }
}
