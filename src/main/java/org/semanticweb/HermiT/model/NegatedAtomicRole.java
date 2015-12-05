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
 * Represents a negated atomic role
 */
public class NegatedAtomicRole {
    
    protected final AtomicRole m_negatedAtomicRole;
    
    public NegatedAtomicRole(AtomicRole negatedAtomicRole) {
        m_negatedAtomicRole=negatedAtomicRole;
    }
    public AtomicRole getNegatedAtomicRole() {
        return m_negatedAtomicRole;
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toString(Prefixes prefixes) {
        return "not("+m_negatedAtomicRole.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<NegatedAtomicRole> s_interningManager=new InterningManager<NegatedAtomicRole>() {
        protected boolean equal(NegatedAtomicRole object1,NegatedAtomicRole object2) {
            return object1.m_negatedAtomicRole==object2.m_negatedAtomicRole;
        }
        protected int getHashCode(NegatedAtomicRole object) {
            return -object.m_negatedAtomicRole.hashCode();
        }
    };
    
    public static NegatedAtomicRole create(AtomicRole negatedAtomicRole) {
        return s_interningManager.intern(new NegatedAtomicRole(negatedAtomicRole));
    }
}
