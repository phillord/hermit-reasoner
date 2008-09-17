// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents a negation of an atomic concept.
 */
public class AtomicNegationConcept extends LiteralConcept {
    private static final long serialVersionUID=-4635386233266966577L;

    protected final AtomicConcept m_negatedAtomicConcept;
    
    protected AtomicNegationConcept(AtomicConcept negatedAtomicConcept) {
        m_negatedAtomicConcept=negatedAtomicConcept;
    }
    public AtomicConcept getNegatedAtomicConcept() {
        return m_negatedAtomicConcept;
    }
    public String toString(Namespaces namespaces) {
        return "not("+m_negatedAtomicConcept.toString(namespaces)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicNegationConcept> s_interningManager=new InterningManager<AtomicNegationConcept>() {
        protected boolean equal(AtomicNegationConcept object1,AtomicNegationConcept object2) {
            return object1.m_negatedAtomicConcept==object2.m_negatedAtomicConcept;
        }
        protected int getHashCode(AtomicNegationConcept object) {
            return -object.m_negatedAtomicConcept.hashCode();
        }
    };
    
    public static AtomicNegationConcept create(AtomicConcept negatedAtomicConcept) {
        return s_interningManager.intern(new AtomicNegationConcept(negatedAtomicConcept));
    }
}
