/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class PermanentDependencySet implements DependencySet,Serializable {
    private static final long serialVersionUID=353039301123337446L;

    protected PermanentDependencySet m_rest;
    protected int m_branchingPoint;
    protected PermanentDependencySet m_nextEntry;
    protected int m_usageCounter;
    protected PermanentDependencySet m_previousUnusedSet;
    protected PermanentDependencySet m_nextUnusedSet;
    
    protected PermanentDependencySet() {
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
