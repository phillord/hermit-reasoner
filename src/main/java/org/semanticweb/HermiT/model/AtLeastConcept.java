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
public class AtLeastConcept extends AtLeast {
    private static final long serialVersionUID=4326267535193393030L;

    protected final LiteralConcept m_toConcept;
    
    protected AtLeastConcept(int number,Role onRole,LiteralConcept toConcept) {
        super(number,onRole);
        m_toConcept=toConcept;
    }
    public LiteralConcept getToConcept() {
        return m_toConcept;
    }
    public boolean isAlwaysFalse() {
        return m_toConcept.isAlwaysFalse();
    }
    public String toString(Prefixes prefixes) {
        return "atLeast("+m_number+' '+m_onRole.toString(prefixes)+' '+m_toConcept.toString(prefixes)+')';
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtLeastConcept> s_interningManager=new InterningManager<AtLeastConcept>() {
        protected boolean equal(AtLeastConcept object1,AtLeastConcept object2) {
            return object1.m_number==object2.m_number && object1.m_onRole==object2.m_onRole && object1.m_toConcept==object2.m_toConcept;
        }
        protected int getHashCode(AtLeastConcept object) {
            return (object.m_number*7+object.m_onRole.hashCode())*7+object.m_toConcept.hashCode();
        }
    };
    
    public static AtLeastConcept create(int number,Role onRole,LiteralConcept toConcept) {
        return s_interningManager.intern(new AtLeastConcept(number,onRole,toConcept));
    }
}
