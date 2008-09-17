// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents an individual in a DL clause.
 */
public class Individual extends Term {
    private static final long serialVersionUID=2791684055390160959L;

    protected final String m_uri;
    
    protected Individual(String uri) {
        m_uri=uri;
    }
    public String getURI() {
        return m_uri;
    }
    public String toString() {
        return toString(Namespaces.none);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Namespaces namespaces) {
        return namespaces.idFromUri(m_uri);
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
    public static Individual create(String uri) {
        return s_interningManager.intern(new Individual(uri));
    }
    /** If an individual has previously been constructed for this URI then return
        it; otherwise return null. */
    public static Individual getExisting(String uri) {
        return s_interningManager.getExisting(new Individual(uri));
    }

}
