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
 * Represents an individual in a DL clause.
 */
public class Individual extends Term {
    private static final long serialVersionUID=2791684055390160959L;

    protected final String m_uri;

    protected Individual(String uri) {
        m_uri=uri;
    }
    public String getIRI() {
        return m_uri;
    }
    public boolean isAnonymous() {
        return m_uri.startsWith("internal:anonymous#");
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(m_uri);
    }

    protected static InterningManager<Individual> s_interningManager=new InterningManager<Individual>() {
        protected boolean equal(Individual object1,Individual object2) {
            return object1.m_uri.equals(object2.m_uri);
        }
        protected int getHashCode(Individual object) {
            return object.m_uri.hashCode();
        }
    };

    /** Returns an Individual with the given identifier. If this function
        is called multiple times with the same identifier, then the same object
        will be returned on each call (allowing for fast equality testing).
        It is the caller's responsibility to normalize the given URI---this
        function treats the argument as a raw string. */
    public static Individual create(String uri) {
        return s_interningManager.intern(new Individual(uri));
    }
    public static Individual createAnonymous(String id) {
        return create(getAnonymousURI(id));
    }
    public static String getAnonymousURI(String id) {
        return "internal:anonymous#"+id;
    }
}
