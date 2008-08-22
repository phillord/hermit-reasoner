// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents an inverse abstract role.
 */
public class InverseAbstractRole extends AbstractRole {
    private static final long serialVersionUID=3351701933728011998L;

    protected final AtomicAbstractRole m_inverseOf;
    
    public InverseAbstractRole(AtomicAbstractRole inverseOf) {
        m_inverseOf=inverseOf;
    }
    public AtomicAbstractRole getInverseOf() {
        return m_inverseOf;
    }
    public AbstractRole getInverseRole() {
        return m_inverseOf;
    }
    public String toString(Namespaces namespaces) {
        return "(inv-"+m_inverseOf.toString(namespaces)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<InverseAbstractRole> s_interningManager=new InterningManager<InverseAbstractRole>() {
        protected boolean equal(InverseAbstractRole object1,InverseAbstractRole object2) {
            return object1.m_inverseOf==object2.m_inverseOf;
        }
        protected int getHashCode(InverseAbstractRole object) {
            return -object.m_inverseOf.hashCode();
        }
    };
    
    public static InverseAbstractRole create(AtomicAbstractRole inverseOf) {
        return s_interningManager.intern(new InverseAbstractRole(inverseOf));
    }
}
