// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents a negation of a data range.
 */
@SuppressWarnings("serial")
public class NegationDataRange extends DataRange {

    protected final DataRange m_negatedDataRange;
    
    protected NegationDataRange(DataRange negatedDataRange) {
        m_negatedDataRange=negatedDataRange;
    }
    public DataRange getNegatedDataRange() {
        return m_negatedDataRange;
    }
    public LiteralConcept getNegation() {
        return m_negatedDataRange;
    }
    public boolean isAlwaysTrue() {
        return m_negatedDataRange.isAlwaysFalse();
    }
    public boolean isAlwaysFalse() {
        return m_negatedDataRange.isAlwaysTrue();
    }
    public String toString(Prefixes prefixes) {
        return "not("+m_negatedDataRange.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<NegationDataRange> s_interningManager=new InterningManager<NegationDataRange>() {
        protected boolean equal(NegationDataRange object1,NegationDataRange object2) {
            return object1.m_negatedDataRange==object2.m_negatedDataRange;
        }
        protected int getHashCode(NegationDataRange object) {
            return -object.m_negatedDataRange.hashCode();
        }
    };
    
    public static NegationDataRange create(DataRange negatedDataRange) {
        return s_interningManager.intern(new NegationDataRange(negatedDataRange));
    }
}
