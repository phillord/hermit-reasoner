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

/**
 * Represents a literal concept -- that is, an atomic concept, a negation of an atomic concept, or a data range.
 */
public abstract class LiteralDataRange extends DataRange implements DLPredicate {
    private static final long serialVersionUID=-2302452747339289424L;
    
    /**
     * @return negation
     */
    public abstract LiteralDataRange getNegation();
    /**
     * @return true if internal
     */
    public boolean isInternalDatatype() {
        return false;
    }
    /**
     * @return true if negated internal
     */
    public boolean isNegatedInternalDatatype() {
        return false;
    }
}
