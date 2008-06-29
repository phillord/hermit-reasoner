// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents an abstract role.
 */
public abstract class AbstractRole implements Serializable {
    private static final long serialVersionUID=-6487260817445541931L;

    public abstract AbstractRole getInverseRole();
    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.INSTANCE);        
    }
}
