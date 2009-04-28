// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents a predicate that whether the IDs of the argument nodes are all strictly ascending or all equal.
 */
public class NodeIDsAscendingOrEqual implements DLPredicate,Serializable {
    private static final long serialVersionUID=7197886700065386931L;

    protected final int m_arity;
    
    protected NodeIDsAscendingOrEqual(int arity) {
        m_arity=arity;
    }
    public int getArity() {
        return m_arity;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Prefixes prefixes) {
        return "NodeIDsAscendingOrEqual";
    }

    protected static InterningManager<NodeIDsAscendingOrEqual> s_interningManager=new InterningManager<NodeIDsAscendingOrEqual>() {
        protected boolean equal(NodeIDsAscendingOrEqual object1,NodeIDsAscendingOrEqual object2) {
            return object1.m_arity==object2.m_arity;
        }
        protected int getHashCode(NodeIDsAscendingOrEqual object) {
            return object.m_arity;
        }
    };
    
    public static NodeIDsAscendingOrEqual create(int arity) {
        return s_interningManager.intern(new NodeIDsAscendingOrEqual(arity));
    }
}
