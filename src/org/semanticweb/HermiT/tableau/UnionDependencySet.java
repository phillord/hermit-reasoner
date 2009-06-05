// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Arrays;

public final class UnionDependencySet implements DependencySet,Serializable{
    private static final long serialVersionUID=8296150535316233960L;

    protected int m_numberOfConstituents;
    protected DependencySet[] m_dependencySets;

    public UnionDependencySet(int numberOfConstituents) {
        m_dependencySets=new DependencySet[numberOfConstituents];
        m_numberOfConstituents=numberOfConstituents;
    }
    public boolean containsBranchingPoint(int branchingPoint) {
        for (int index=m_numberOfConstituents-1;index>=0;--index)
            if (m_dependencySets[index].containsBranchingPoint(branchingPoint))
                return true;
        return false;
    }
    public int getMaximumBranchingPoint() {
        int maximumSoFar=m_dependencySets[0].getMaximumBranchingPoint();
        for (int index=m_numberOfConstituents-1;index>=1;--index)
            maximumSoFar=Math.max(maximumSoFar,m_dependencySets[index].getMaximumBranchingPoint());
        return maximumSoFar;
    }
    public boolean isEmpty() {
        for (int index=m_numberOfConstituents-1;index>=0;--index)
            if (!m_dependencySets[index].isEmpty())
                return false;
        return true;
    }
    public void clearConstituents() {
        Arrays.fill(m_dependencySets,null);
        m_numberOfConstituents=0;
    }
    public void addConstituent(DependencySet constituent) {
        if (m_numberOfConstituents==m_dependencySets.length) {
            DependencySet[] newDependencySets=new DependencySet[m_numberOfConstituents*3/2];
            System.arraycopy(m_dependencySets,0,newDependencySets,0,m_dependencySets.length);
            m_dependencySets=newDependencySets;
        }
        m_dependencySets[m_numberOfConstituents++]=constituent;
    }
}
