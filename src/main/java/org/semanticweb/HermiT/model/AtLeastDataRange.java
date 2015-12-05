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
 * Represents at-least concept.
 */
public class AtLeastDataRange extends AtLeast {
    private static final long serialVersionUID=4326267535193393030L;

    protected final LiteralDataRange m_toDataRange;
    
    protected AtLeastDataRange(int number,Role onRole,LiteralDataRange toConcept) {
        super(number, onRole);
        m_toDataRange=toConcept;
    }
    public LiteralDataRange getToDataRange() {
        return m_toDataRange;
    }
    public boolean isAlwaysFalse() {
        return m_toDataRange.isAlwaysFalse();
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Prefixes prefixes) {
        return "atLeast("+m_number+' '+m_onRole.toString(prefixes)+' '+m_toDataRange.toString(prefixes)+')';
    }

    protected static InterningManager<AtLeastDataRange> s_interningManager=new InterningManager<AtLeastDataRange>() {
        protected boolean equal(AtLeastDataRange object1,AtLeastDataRange object2) {
            return object1.m_number==object2.m_number && object1.m_onRole==object2.m_onRole && object1.m_toDataRange==object2.m_toDataRange;
        }
        protected int getHashCode(AtLeastDataRange object) {
            return (object.m_number*7+object.m_onRole.hashCode())*7+object.m_toDataRange.hashCode();
        }
    };
    
    public static AtLeastDataRange create(int number,Role onRole,LiteralDataRange toDataRange) {
        return s_interningManager.intern(new AtLeastDataRange(number,onRole,toDataRange));
    }
}
