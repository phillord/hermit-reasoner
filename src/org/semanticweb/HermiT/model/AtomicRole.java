// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents an atomic role.
 */
public class AtomicRole extends Role implements DLPredicate {
    private static final long serialVersionUID=3766087788313643809L;

    protected final String m_uri;
    protected final boolean m_isDatatypeRole;

    protected AtomicRole(String uri,boolean isDatatypeRole) {
        m_uri=uri;
        m_isDatatypeRole=isDatatypeRole;
    }
    public String getURI() {
        return m_uri;
    }
    public boolean isRestrictedToDatatypes() {
        return m_isDatatypeRole;
    }
    public int getArity() {
        return 2;
    }
    public Role getInverse() {
        return InverseRole.create(this);
    }
    public String toString(Namespaces namespaces) {
        String out=namespaces.idFromUri(m_uri);
        if (m_isDatatypeRole)
            return out+"*";
        else
            return out;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicRole> s_interningManager=new InterningManager<AtomicRole>() {
        protected boolean equal(AtomicRole object1,AtomicRole object2) {
            return object1.m_uri.equals(object2.m_uri) && object1.m_isDatatypeRole==object2.m_isDatatypeRole;
        }
        protected int getHashCode(AtomicRole object) {
            return object.m_uri.hashCode();
        }
    };

    public static AtomicRole createObjectRole(String uri) {
        return s_interningManager.intern(new AtomicRole(uri,false));
    }
    public static AtomicRole createDataRole(String uri) {
        return s_interningManager.intern(new AtomicRole(uri,true));
    }

    public static final AtomicRole TOP_OBJECT_ROLE=createObjectRole("http://www.w3.org/2002/07/owl#TopObjectProperty");
    public static final AtomicRole BOTTOM_OBJECT_ROLE=createObjectRole("http://www.w3.org/2002/07/owl#BottomObjectProperty");
    public static final AtomicRole TOP_DATA_ROLE=createDataRole("http://www.w3.org/2002/07/owl#TopDataProperty");
    public static final AtomicRole BOTTOM_DATA_ROLE=createDataRole("http://www.w3.org/2002/07/owl#BottomDataProperty");
}
