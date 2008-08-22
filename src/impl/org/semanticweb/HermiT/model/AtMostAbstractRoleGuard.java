// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents a guard for number restrictions on abstract roles.
 */
public class AtMostAbstractRoleGuard extends AtomicConcept {
    private static final long serialVersionUID=7197886700065386931L;

    protected final int m_cardinality;
    protected final AbstractRole m_onAbstractRole;
    protected final AtomicConcept m_toAtomicConcept;
    
    protected AtMostAbstractRoleGuard(int cardinality,AbstractRole onAbstractRole,AtomicConcept toAtomicConcept) {
        super("internal:(atMost "+cardinality+" "+onAbstractRole.toString()+" "+toAtomicConcept.getURI()+")");
        m_cardinality=cardinality;
        m_onAbstractRole=onAbstractRole;
        m_toAtomicConcept=toAtomicConcept;
    }
    public int getCaridnality() {
        return m_cardinality;
    }
    public AbstractRole getOnAbstractRole() {
        return m_onAbstractRole;
    }
    public AtomicConcept getToAtomicConcept() {
        return m_toAtomicConcept;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Namespaces namespaces) {
        return "(atMost "+m_cardinality+" "+m_onAbstractRole.toString(namespaces)+" "+m_toAtomicConcept.toString(namespaces)+")";
    }

    protected static InterningManager<AtMostAbstractRoleGuard> s_interningManager=new InterningManager<AtMostAbstractRoleGuard>() {
        protected boolean equal(AtMostAbstractRoleGuard object1,AtMostAbstractRoleGuard object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(AtMostAbstractRoleGuard object) {
            return object.m_uri.hashCode();
        }
    };
    
    public static AtMostAbstractRoleGuard create(int cardinality,AbstractRole onAbstractRole,AtomicConcept toAtomicConcept) {
        return s_interningManager.intern(new AtMostAbstractRoleGuard(cardinality,onAbstractRole,toAtomicConcept));
    }
}
