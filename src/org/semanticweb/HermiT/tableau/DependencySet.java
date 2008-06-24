// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

public interface DependencySet {
    boolean containsBranchingPoint(int branchingPoint);
    boolean isEmpty();
    int getMaximumBranchingPoint();
}
