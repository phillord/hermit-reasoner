// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents the built-in predicate that is used to enforce ordering on nodes in the translation of at-most concepts.
 */
public class NodeIDLessThan implements DLPredicate,Serializable {
    private static final long serialVersionUID=5572346926189452451L;
    public static final NodeIDLessThan INSTANCE=new NodeIDLessThan();
    
    protected NodeIDLessThan () {
    }
    public int getArity() {
        return 2;
    }
    public String toString(Prefixes prefixes) {
        return "<";
    }
    public String toString() {
        return toString(Prefixes.EMPTY);
    }
    protected Object readResolve() {
        return INSTANCE;
    }
    public static NodeIDLessThan create() {
        return INSTANCE;
    }
}
