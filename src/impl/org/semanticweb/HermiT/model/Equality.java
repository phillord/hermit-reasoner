// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents the equality predicate.
 */
public class Equality implements DLPredicate,Serializable {
    private static final long serialVersionUID=8308051741088513244L;

    public static final Equality INSTANCE=new Equality();
    
    protected Equality () {
    }
    public int getArity() {
        return 2;
    }
    public String toString(Namespaces namespaces) {
        return "==";
    }
    public String toString() {
        return toString(Namespaces.none);
    }
    protected Object readResolve() {
        return INSTANCE;
    }
    public static Equality create() {
        return INSTANCE;
    }
}
