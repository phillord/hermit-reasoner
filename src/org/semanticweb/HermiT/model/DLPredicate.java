package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents a DL predicate.
 */
public interface DLPredicate {
    int getArity();
    String toString(Namespaces namespaces);
}
