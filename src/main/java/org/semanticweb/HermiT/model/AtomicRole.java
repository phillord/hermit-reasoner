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
 * Represents an atomic role.
 */
public class AtomicRole extends Role implements DLPredicate {
    private static final long serialVersionUID=3766087788313643809L;

    protected final String m_iri;

    protected AtomicRole(String iri) {
        m_iri=iri;
    }
    public String getIRI() {
        return m_iri;
    }
    public int getArity() {
        return 2;
    }
    public Role getInverse() {
        if (this==TOP_OBJECT_ROLE || this==BOTTOM_OBJECT_ROLE)
            return this;
        else
            return InverseRole.create(this);
    }
    public Atom getRoleAssertion(Term term0,Term term1) {
        return Atom.create(this,term0,term1);
    }
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_iri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicRole> s_interningManager=new InterningManager<AtomicRole>() {
        protected boolean equal(AtomicRole object1,AtomicRole object2) {
            return object1.m_iri.equals(object2.m_iri);
        }
        protected int getHashCode(AtomicRole object) {
            return object.m_iri.hashCode();
        }
    };

    public static AtomicRole create(String iri) {
        return s_interningManager.intern(new AtomicRole(iri));
    }

    public static final AtomicRole TOP_OBJECT_ROLE=create("http://www.w3.org/2002/07/owl#topObjectProperty");
    public static final AtomicRole BOTTOM_OBJECT_ROLE=create("http://www.w3.org/2002/07/owl#bottomObjectProperty");
    public static final AtomicRole TOP_DATA_ROLE=create("http://www.w3.org/2002/07/owl#topDataProperty");
    public static final AtomicRole BOTTOM_DATA_ROLE=create("http://www.w3.org/2002/07/owl#bottomDataProperty");
}
