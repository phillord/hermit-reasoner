// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.util;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class GraphUtils {
    public static <T> void transitivelyClose(Map<T, Set<T>> relation) {
        boolean changed = true;
        List<T> temp = new ArrayList<T>();
        while (changed) {
            changed = false;
            for (Map.Entry<T, Set<T>> e : relation.entrySet()) {
                temp.clear();
                temp.addAll(e.getValue());
                for (int i = temp.size() - 1; i >= 0; --i) {
                    Set<T> sub = relation.get(temp.get(i));
                    if (sub != null) {
                        if (e.getValue().addAll(sub)) {
                            changed = true;
                        }
                    }
                }
            }
        }
    }
}
