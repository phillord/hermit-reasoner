package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents a (complex) concept.
 */
public abstract class Concept implements Serializable {
    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.INSTANCE);
    }
}
