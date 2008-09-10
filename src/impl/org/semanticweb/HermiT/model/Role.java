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

    public abstract boolean isRestrictedToDatatypes();

    public abstract String toString(Namespaces namespaces);
    public String toString() {
        return toString(Namespaces.INSTANCE);        
    }
    public static Role fromString(String s, Namespaces n) {
        if (s.startsWith("(inv-")) {
            return fromString(s.substring(5, s.length()-1), n).getInverse();
        } else {
            if (s.endsWith("*")) {
                return AtomicRole.createDataRole(
                    n.expandString(s.substring(0, s.length()-1)));
            } else {
                return AtomicRole.createObjectRole(n.expandString(s));
            }
        }
    }
    public static Role fromString(String s) {
        return fromString(s, Namespaces.INSTANCE);
    }
}
