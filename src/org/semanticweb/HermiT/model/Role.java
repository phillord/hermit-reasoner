// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents a role.
 */
public abstract class Role implements Serializable {
    private static final long serialVersionUID=-6487260817445541931L;

    public abstract Role getInverse();
    public abstract String toString(Prefixes prefixes);
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toOrderedString(Prefixes prefixes) {
        return toString(prefixes);
    }
}
