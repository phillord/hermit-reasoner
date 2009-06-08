// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents an atomic role.
 */
public class AtomicRole extends Role implements DLPredicate {
    private static final long serialVersionUID=3766087788313643809L;

    protected final String m_iri;

    protected AtomicRole(String iri) {
        m_iri=iri;
    }
    public String getIRI() {
        return m_iri;
    }
    public int getArity() {
        return 2;
    }
    public Role getInverse() {
        if (this==TOP_OBJECT_ROLE || this==BOTTOM_OBJECT_ROLE)
            return this;
        else
            return InverseRole.create(this);
    }
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_iri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicRole> s_interningManager=new InterningManager<AtomicRole>() {
        protected boolean equal(AtomicRole object1,AtomicRole object2) {
            return object1.m_iri.equals(object2.m_iri);
        }
        protected int getHashCode(AtomicRole object) {
            return object.m_iri.hashCode();
        }
    };

    public static AtomicRole create(String uri) {
        return s_interningManager.intern(new AtomicRole(uri));
    }

    public static final AtomicRole TOP_OBJECT_ROLE=create("http://www.w3.org/2002/07/owl#topObjectProperty");
    public static final AtomicRole BOTTOM_OBJECT_ROLE=create("http://www.w3.org/2002/07/owl#bottomObjectProperty");
    public static final AtomicRole TOP_DATA_ROLE=create("http://www.w3.org/2002/07/owl#topDataProperty");
    public static final AtomicRole BOTTOM_DATA_ROLE=create("http://www.w3.org/2002/07/owl#bottomDataProperty");
}
