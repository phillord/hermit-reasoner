package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents a term in a DL clause.
 */
public abstract class Term implements Serializable {
    public abstract String toString(Namespaces namespaces);
}
