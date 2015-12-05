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
