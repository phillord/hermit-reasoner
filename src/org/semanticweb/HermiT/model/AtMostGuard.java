// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents a guard for number restrictions on roles.
 */
public class AtMostGuard extends AtomicConcept {
    private static final long serialVersionUID=7197886700065386931L;

    protected final int m_cardinality;
    protected final Role m_onRole;
    protected final AtomicConcept m_toAtomicConcept;
    
    protected AtMostGuard(int cardinality,Role onRole,AtomicConcept toAtomicConcept) {
        super("internal:grd#(atMost "+cardinality+" "+onRole.toString()+" "+toAtomicConcept.getURI()+")");
        m_cardinality=cardinality;
        m_onRole=onRole;
        m_toAtomicConcept=toAtomicConcept;
    }
    public int getCaridnality() {
        return m_cardinality;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public AtomicConcept getToAtomicConcept() {
        return m_toAtomicConcept;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Namespaces namespaces) {
        return "(atMost "+m_cardinality+" "+m_onRole.toString(namespaces)+" "+m_toAtomicConcept.toString(namespaces)+")";
    }

    protected static InterningManager<AtMostGuard> s_interningManager=new InterningManager<AtMostGuard>() {
        protected boolean equal(AtMostGuard object1,AtMostGuard object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(AtMostGuard object) {
            return object.m_uri.hashCode();
        }
    };
    
    public static AtMostGuard create(int cardinality,Role onRole,AtomicConcept toAtomicConcept) {
        return s_interningManager.intern(new AtMostGuard(cardinality,onRole,toAtomicConcept));
    }
}
