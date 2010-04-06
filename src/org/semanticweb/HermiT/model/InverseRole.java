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
 * Represents an inverse role.
 */
public class InverseRole extends Role {
    private static final long serialVersionUID=3351701933728011998L;

    protected final AtomicRole m_inverseOf;

    public InverseRole(AtomicRole inverseOf) {
        m_inverseOf=inverseOf;
    }
    public AtomicRole getInverseOf() {
        return m_inverseOf;
    }
    public Role getInverse() {
        return m_inverseOf;
    }
    public Atom getRoleAssertion(Term term0,Term term1) {
        return Atom.create(m_inverseOf,term1,term0);
    }
    public String toString(Prefixes prefixes) {
        return "inv("+m_inverseOf.toString(prefixes)+")";
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<InverseRole> s_interningManager=new InterningManager<InverseRole>() {
        protected boolean equal(InverseRole object1,InverseRole object2) {
            return object1.m_inverseOf==object2.m_inverseOf;
        }
        protected int getHashCode(InverseRole object) {
            return -object.m_inverseOf.hashCode();
        }
    };

    public static InverseRole create(AtomicRole inverseOf) {
        return s_interningManager.intern(new InverseRole(inverseOf));
    }
}
