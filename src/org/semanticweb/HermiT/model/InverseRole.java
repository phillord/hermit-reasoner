// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents an inverse role.
 */
public class InverseRole extends Role {
    private static final long serialVersionUID=3351701933728011998L;

    protected final AtomicRole m_inverseOf;
    
    public InverseRole(AtomicRole inverseOf) {
        m_inverseOf=inverseOf;
    }
    public AtomicRole getInverseOf() {
        return m_inverseOf;
    }
    public Role getInverse() {
        return m_inverseOf;
    }
    public String toString(Prefixes prefixes) {
        return "inv("+m_inverseOf.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<InverseRole> s_interningManager=new InterningManager<InverseRole>() {
        protected boolean equal(InverseRole object1,InverseRole object2) {
            return object1.m_inverseOf==object2.m_inverseOf;
        }
        protected int getHashCode(InverseRole object) {
            return -object.m_inverseOf.hashCode();
        }
    };
    
    public static InverseRole create(AtomicRole inverseOf) {
        return s_interningManager.intern(new InverseRole(inverseOf));
    }
}
