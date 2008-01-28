package org.semanticweb.HermiT.tableau;

public interface DependencySet {
    boolean containsBranchingPoint(int branchingPoint);
    boolean isEmpty();
    int getMaximumBranchingPoint();
}
