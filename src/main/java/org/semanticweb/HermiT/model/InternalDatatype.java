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
 * Represents an internal datatype. Such objects are used in DL-clauses (e.g., in structural transformation of complex data ranges), but are ignored by the datatype manager.
 */
public class InternalDatatype extends AtomicDataRange implements DLPredicate {
    private static final long serialVersionUID=-1078274072706143620L;

    protected final String m_iri;

    protected InternalDatatype(String iri) {
        m_iri=iri;
    }
    /**
     * @return iri
     */
    public String getIRI() {
        return m_iri;
    }
    @Override
    public int getArity() {
        return 1;
    }
    @Override
    public LiteralDataRange getNegation() {
        return AtomicNegationDataRange.create(this);
    }
    @Override
    public boolean isAlwaysTrue() {
        return this==RDFS_LITERAL;
    }
    @Override
    public boolean isAlwaysFalse() {
        return false;
    }
    @Override
    public boolean isInternalDatatype() {
        return true;
    }
    @Override
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_iri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected final static InterningManager<InternalDatatype> s_interningManager=new InterningManager<InternalDatatype>() {
        @Override
        protected boolean equal(InternalDatatype object1,InternalDatatype object2) {
            return object1.m_iri.equals(object2.m_iri);
        }
        @Override
        protected int getHashCode(InternalDatatype object) {
            return object.m_iri.hashCode();
        }
    };

    /**
     * @param uri iri
     * @return datatype
     */
    public static InternalDatatype create(String uri) {
        return s_interningManager.intern(new InternalDatatype(uri));
    }

    /**Literal.*/
    public static final InternalDatatype RDFS_LITERAL=create("http://www.w3.org/2000/01/rdf-schema#Literal");
}
