// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents the atomic abstract role.
 */
public class AtomicConcreteRole implements DLPredicate {

    private static final long serialVersionUID = -1444971536484084416L;

    protected String m_uri;

    protected AtomicConcreteRole(String uri) {
        m_uri = uri;
    }

    public String getURI() {
        return m_uri;
    }

    public int getArity() {
        return 2;
    }

    public String toString(Namespaces namespaces) {
        return namespaces.abbreviateAsNamespace(m_uri);
    }

    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicConcreteRole> s_interningManager = new InterningManager<AtomicConcreteRole>() {
        protected boolean equal(AtomicConcreteRole object1,
                AtomicConcreteRole object2) {
            return object1.m_uri.equals(object2.m_uri);
        }

        protected int getHashCode(AtomicConcreteRole object) {
            return object.m_uri.hashCode();
        }
    };

    public static AtomicConcreteRole create(String uri) {
        return s_interningManager.intern(new AtomicConcreteRole(uri));
    }
}
