package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;


public abstract class DataRange implements DLPredicate {
    public int getArity() {
        return 1;
    }
    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.INSTANCE);
    }
}
