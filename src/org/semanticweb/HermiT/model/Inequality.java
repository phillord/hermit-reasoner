// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents the inequality predicate.
 */
public class Inequality implements DLPredicate,Serializable {
    private static final long serialVersionUID=296924110684230279L;

    public static final Inequality INSTANCE=new Inequality();
    
    protected Inequality () {
    }
    public int getArity() {
        return 2;
    }
    public String toString(Prefixes prefixes) {
        return "!=";
    }
    public String toOrderedString(Prefixes prefixes) {
        return toString(prefixes);
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return INSTANCE;
    }
    public static Inequality create() {
        return INSTANCE;
    }
}
