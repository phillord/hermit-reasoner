package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents an abstract role.
 */
public abstract class AbstractRole implements Serializable {
    public abstract AbstractRole getInverseRole();
    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.INSTANCE);        
    }
}
