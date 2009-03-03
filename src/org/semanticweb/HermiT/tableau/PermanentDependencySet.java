// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class PermanentDependencySet implements DependencySet,Serializable {
    private static final long serialVersionUID=353039301123337446L;

    protected final int m_generation;
    protected PermanentDependencySet m_rest;
    protected int m_branchingPoint;
    protected PermanentDependencySet m_nextEntry;
    protected int m_usageCounter;
    protected PermanentDependencySet m_previousUnusedSet;
    protected PermanentDependencySet m_nextUnusedSet;
    
    protected PermanentDependencySet(int generation) {
        m_generation=generation;
        m_rest=null;
        m_branchingPoint=-2;
        m_nextEntry=null;
        m_usageCounter=0;
        m_previousUnusedSet=null;
        m_nextUnusedSet=null;
    }
    public boolean containsBranchingPoint(int branchingPoint) {
        PermanentDependencySet set=this;
        while (set!=null) {
            if (set.m_branchingPoint==branchingPoint)
                return true;
            set=set.m_rest;
        }
        return false;
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
        PermanentDependencySet dependencySet=this;
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
