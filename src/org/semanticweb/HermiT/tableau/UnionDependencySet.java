package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class UnionDependencySet implements DependencySet,Serializable{
    private static final long serialVersionUID=8296150535316233960L;

    protected final DependencySet[] m_dependencySets;

    public UnionDependencySet(int arity) {
        m_dependencySets=new DependencySet[arity];
    }
    public DependencySet[] getConstituents() {
        return m_dependencySets;
    }
    public boolean containsBranchingPoint(int branchingPoint) {
        for (int index=m_dependencySets.length-1;index>=0;--index)
            if (m_dependencySets[index].containsBranchingPoint(branchingPoint))
                return true;
        return false;
    }
    public int getMaximumBranchingPoint() {
        int maximumSoFar=m_dependencySets[0].getMaximumBranchingPoint();
        for (int index=m_dependencySets.length-1;index>=1;--index)
            maximumSoFar=Math.max(maximumSoFar,m_dependencySets[index].getMaximumBranchingPoint());
        return maximumSoFar;
    }
    public boolean isEmpty() {
        for (int index=m_dependencySets.length-1;index>=0;--index)
            if (!m_dependencySets[index].isEmpty())
                return false;
        return true;
    }
}
