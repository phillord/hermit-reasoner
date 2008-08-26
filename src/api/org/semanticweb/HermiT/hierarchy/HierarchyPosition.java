// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.hierarchy;

import java.util.Set;

public interface HierarchyPosition<T> {
    Set<T> getEquivalents();
    Set<T> getAncestors();
    Set<T> getDescendants();
    Set<HierarchyPosition<T>> getParentPositions();
    Set<HierarchyPosition<T>> getChildPositions();
    Set<HierarchyPosition<T>> getAncestorPositions();
    Set<HierarchyPosition<T>> getDescendantPositions();
}
