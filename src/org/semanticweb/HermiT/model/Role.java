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
 * Represents a role.
 */
public abstract class Role implements Serializable {
    private static final long serialVersionUID=-6487260817445541931L;

    /**
     * @return inverse role
     */
    public abstract Role getInverse();
    /**
     * @param term0 term0
     * @param term1 term1
     * @return atom
     */
    public abstract Atom getRoleAssertion(Term term0,Term term1);
    /**
     * @param prefixes prefixes
     * @return toString
     */
    public abstract String toString(Prefixes prefixes);
    @Override
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
}
