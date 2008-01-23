package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class DependencySet implements Serializable {
    private static final long serialVersionUID=353039301123337446L;

    protected final DependencySet m_rest;
    protected final int m_branchingPoint;
    protected DependencySet m_nextEntry;
    
    protected DependencySet(DependencySet rest,int branchingPoint,DependencySet nextEntry) {
        m_rest=rest;
        m_branchingPoint=branchingPoint;
        m_nextEntry=nextEntry;
    }
    public boolean containsBranchingPoint(int branchingPoint) {
        DependencySet set=this;
        while (set!=null) {
            if (set.m_branchingPoint==branchingPoint)
                return true;
            set=set.m_rest;
        }
        return false;
    }
    public boolean isSameAs(DependencySet that) {
        return this==that;
    }
    public boolean isEmpty() {
        return m_branchingPoint==-1;
    }
    public int getMaximumBranchingPoint() {
        return m_branchingPoint;
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("{ ");
        DependencySet dependencySet=this;
        while (dependencySet.m_branchingPoint!=-1) {
            buffer.append(dependencySet.m_branchingPoint);
            if (dependencySet.m_rest.m_branchingPoint!=-1)
                buffer.append(',');
            dependencySet=dependencySet.m_rest;
        }
        buffer.append(" }");
        return buffer.toString();
    }
}
