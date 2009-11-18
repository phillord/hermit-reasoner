// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents an annotated equality.
 */
public class AnnotatedEquality implements DLPredicate,Serializable {
    private static final long serialVersionUID=7197886700065386931L;

    protected final int m_cardinality;
    protected final Role m_onRole;
    protected final LiteralConcept m_toConcept;
    
    protected AnnotatedEquality(int cardinality,Role onRole,LiteralConcept toConcept) {
        m_cardinality=cardinality;
        m_onRole=onRole;
        m_toConcept=toConcept;
    }
    public int getCaridnality() {
        return m_cardinality;
    }
    public Role getOnRole() {
        return m_onRole;
    }
    public LiteralConcept getToConcept() {
        return m_toConcept;
    }
    public int getArity() {
        return 3;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toString(Prefixes prefixes) {
        return "==@atMost("+m_cardinality+" "+m_onRole.toString(prefixes)+" "+m_toConcept.toString(prefixes)+")";
    }
    public String toOrderedString(Prefixes prefixes) {
        return toString(prefixes);
    }
    protected static InterningManager<AnnotatedEquality> s_interningManager=new InterningManager<AnnotatedEquality>() {
        protected boolean equal(AnnotatedEquality object1,AnnotatedEquality object2) {
            return object1.m_cardinality==object2.m_cardinality && object1.m_onRole==object2.m_onRole && object1.m_toConcept==object2.m_toConcept;
        }
        protected int getHashCode(AnnotatedEquality object) {
            return object.m_cardinality+object.m_onRole.hashCode()+object.m_toConcept.hashCode();
        }
    };
    
    public static AnnotatedEquality create(int cardinality,Role onRole,LiteralConcept toConcept) {
        return s_interningManager.intern(new AnnotatedEquality(cardinality,onRole,toConcept));
    }
}
