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
 * Represents an atomic concept.
 */
public class AtomicConcept extends LiteralConcept implements DLPredicate {
    private static final long serialVersionUID=-1078274072706143620L;

    protected final String m_iri;
    
    protected AtomicConcept(String iri) {
        m_iri=iri;
    }
    public String getIRI() {
        return m_iri;
    }
    public int getArity() {
        return 1;
    }
    public LiteralConcept getNegation() {
        if (this==THING)
            return NOTHING;
        else if (this==NOTHING)
            return THING;
        else
            return AtomicNegationConcept.create(this);
    }
    public boolean isAlwaysTrue() {
        return this==THING;
    }
    public boolean isAlwaysFalse() {
        return this==NOTHING;
    }
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_iri);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<AtomicConcept> s_interningManager=new InterningManager<AtomicConcept>() {
        protected boolean equal(AtomicConcept object1,AtomicConcept object2) {
            return object1.m_iri.equals(object2.m_iri);
        }
        protected int getHashCode(AtomicConcept object) {
            return object.m_iri.hashCode();
        }
    };
    
    public static AtomicConcept create(String uri) {
        return s_interningManager.intern(new AtomicConcept(uri));
    }

    public static final AtomicConcept THING=create("http://www.w3.org/2002/07/owl#Thing");
    public static final AtomicConcept NOTHING=create("http://www.w3.org/2002/07/owl#Nothing");
    public static final AtomicConcept INTERNAL_NAMED=create("internal:nam#Named");
}
