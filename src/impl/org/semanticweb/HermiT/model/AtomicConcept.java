// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents an atomic concept.
 */
public class AtomicConcept extends LiteralConcept implements DLPredicate {
    private static final long serialVersionUID=-1078274072706143620L;

    protected final String m_uri;
    
    protected AtomicConcept(String uri) {
        m_uri=uri;
    }
    public String getURI() {
        return m_uri;
    }
    public int getArity() {
        return 1;
    }
    public String toString(Namespaces namespaces) {
        return namespaces.abbreviateAsNamespace(m_uri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicConcept> s_interningManager=new InterningManager<AtomicConcept>() {
        protected boolean equal(AtomicConcept object1,AtomicConcept object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(AtomicConcept object) {
            return object.m_uri.hashCode();
        }
    };
    
    /** Returns an AtomicConcept with the given identifier. If this function
        is called multiple times with the same identifier, then the same object
        will be returned on each call (allowing for fast equality testing).
        It is the caller's responsibility to normalize the given URI---this
        function treats the argument as a raw string. */
    public static AtomicConcept create(String uri) {
        return s_interningManager.intern(new AtomicConcept(uri));
    }
    
    /** If an atomic concept has previously been constructed for this URI then return
        it; otherwise return null. */
    public static AtomicConcept getExisting(String uri) {
        return s_interningManager.getExisting(new AtomicConcept(uri));
    }

    public static final AtomicConcept THING=create("http://www.w3.org/2002/07/owl#Thing");
    public static final AtomicConcept NOTHING=create("http://www.w3.org/2002/07/owl#Nothing");
    public static final AtomicConcept RDFS_LITERAL=create("http://www.w3.org/2000/01/rdf-schema#Literal");
}
