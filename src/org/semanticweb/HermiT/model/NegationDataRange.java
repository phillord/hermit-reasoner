/* Copyright 2008, 2009 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;

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
