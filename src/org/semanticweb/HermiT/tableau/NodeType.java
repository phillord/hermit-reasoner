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

public enum NodeType {
    NAMED_NODE(0,false,true),NI_NODE(1,false,true),ROOT_CONSTANT_NODE(1,false,false),TREE_NODE(2,true,true),GRAPH_NODE(2,true,true),CONCRETE_NODE(2,false,false);

    protected final int m_mergePrecedence;
    protected final boolean m_isNITarget;
    protected final boolean m_isAbstract;
    
    private NodeType(int mergePrecedence,boolean isNITarget,boolean isAbstract) {
        m_mergePrecedence=mergePrecedence;
        m_isNITarget=isNITarget;
        m_isAbstract=isAbstract;
    }
    public int getMergePrecedence() {
        return m_mergePrecedence;
    }
    public boolean isNITarget() {
        return m_isNITarget;
    }
    public boolean isAbstract() {
        return m_isAbstract;
    }
}
