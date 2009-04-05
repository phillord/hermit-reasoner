// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

public enum NodeType {
    NAMED_NODE(0,false,true),ROOT_NODE(1,false,true),CONCRETE_ROOT_NODE(1,false,false),TREE_NODE(2,true,true),CONCRETE_NODE(2,true,false),GRAPH_NODE(2,true,true);

    protected final int m_mergePrecedence;
    protected final boolean m_inTreePart;
    protected final boolean m_isAbstract;
    
    private NodeType(int mergePrecedence,boolean inTreePart,boolean isAbstract) {
        m_mergePrecedence=mergePrecedence;
        m_inTreePart=inTreePart;
        m_isAbstract=isAbstract;
    }
    public int getMergePrecedence() {
        return m_mergePrecedence;
    }
    public boolean isInTreePart() {
        return m_inTreePart;
    }
    public boolean isAbstract() {
        return m_isAbstract;
    }
}
