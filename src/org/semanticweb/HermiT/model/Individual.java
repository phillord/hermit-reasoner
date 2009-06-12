// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents an individual in a DL clause.
 */
public class Individual extends Term {
    private static final long serialVersionUID=2791684055390160959L;

    protected final String m_uri;
    protected final boolean m_isNamed;
    
    protected Individual(String uri, boolean isNamed) {
        m_uri=uri;
        m_isNamed=isNamed;
    }
    public String getIRI() {
        return m_uri;
    }
    public boolean isNamed() {
        return m_isNamed;
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_uri);
    }

    protected static InterningManager<Individual> s_interningManager=new InterningManager<Individual>() {
        protected boolean equal(Individual object1,Individual object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(Individual object) {
            return object.m_uri.hashCode();
        }
    };
    
    /** Returns an Individual with the given identifier. If this function
        is called multiple times with the same identifier, then the same object
        will be returned on each call (allowing for fast equality testing).
        It is the caller's responsibility to normalize the given URI---this
        function treats the argument as a raw string. */
    public static Individual create(String uri, boolean isNamed) {
        return s_interningManager.intern(new Individual(uri, isNamed));
    }
}
