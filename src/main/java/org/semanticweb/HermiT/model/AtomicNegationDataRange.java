/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
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
public class AtomicNegationDataRange extends LiteralDataRange {

    protected final AtomicDataRange m_negatedDataRange;
    
    protected AtomicNegationDataRange(AtomicDataRange negatedDataRange) {
        m_negatedDataRange=negatedDataRange;
    }
    public AtomicDataRange getNegatedDataRange() {
        return m_negatedDataRange;
    }
    public LiteralDataRange getNegation() {
        return m_negatedDataRange;
    }
    public boolean isAlwaysTrue() {
        return m_negatedDataRange.isAlwaysFalse();
    }
    public boolean isAlwaysFalse() {
        return m_negatedDataRange.isAlwaysTrue();
    }
    public boolean isNegatedInternalDatatype() {
        return m_negatedDataRange.isInternalDatatype();
    }
    public String toString(Prefixes prefixes) {
        return "not("+m_negatedDataRange.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicNegationDataRange> s_interningManager=new InterningManager<AtomicNegationDataRange>() {
        protected boolean equal(AtomicNegationDataRange object1,AtomicNegationDataRange object2) {
            return object1.m_negatedDataRange==object2.m_negatedDataRange;
        }
        protected int getHashCode(AtomicNegationDataRange object) {
            return -object.m_negatedDataRange.hashCode();
        }
    };
    
    public static AtomicNegationDataRange create(AtomicDataRange negatedDataRange) {
        return s_interningManager.intern(new AtomicNegationDataRange(negatedDataRange));
    }
}
