// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.*;

/**
 * Represents a role.
 */
public abstract class Role implements Serializable {
    private static final long serialVersionUID=-6487260817445541931L;

    public abstract Role getInverse();
    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.none);
    }
}
