// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents a negated atomic role
 */
public class NegatedAtomicRole {
    
    protected final AtomicRole m_negatedAtomicRole;
    
    public NegatedAtomicRole(AtomicRole negatedAtomicRole) {
        m_negatedAtomicRole=negatedAtomicRole;
    }
    public AtomicRole getNegatedAtomicRole() {
        return m_negatedAtomicRole;
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toString(Prefixes prefixes) {
        return "not("+m_negatedAtomicRole.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<NegatedAtomicRole> s_interningManager=new InterningManager<NegatedAtomicRole>() {
        protected boolean equal(NegatedAtomicRole object1,NegatedAtomicRole object2) {
            return object1.m_negatedAtomicRole==object2.m_negatedAtomicRole;
        }
        protected int getHashCode(NegatedAtomicRole object) {
            return -object.m_negatedAtomicRole.hashCode();
        }
    };
    
    public static NegatedAtomicRole create(AtomicRole negatedAtomicRole) {
        return s_interningManager.intern(new NegatedAtomicRole(negatedAtomicRole));
    }
}
