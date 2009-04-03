// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

public enum NodeType {
    NAMED_NODE(0,false),ROOT_NODE(1,false),CONCRETE_ROOT_NODE(1,false),TREE_NODE(2,true),CONCRETE_NODE(2,true),GRAPH_NODE(2,true);

    protected final int m_mergePrecedence;
    protected final boolean m_inTreePart; 
    
    private NodeType(int mergePrecedence,boolean inTreePart) {
        m_mergePrecedence=mergePrecedence;
        m_inTreePart=inTreePart;
    }
    public int getMergePrecedence() {
        return m_mergePrecedence;
    }
    public boolean isInTreePart() {
        return m_inTreePart;
    }
}
