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
package org.semanticweb.HermiT.datatypes.owlreal;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Number range.
 */
public enum NumberRange {
    /**
     * Nothing.
     */
    NOTHING,
    /**
     * Integer.
     */
    INTEGER,
    /**
     * Decimal.
     */
    DECIMAL,
    /**
     * Rational.
     */
    RATIONAL,
    /**
     * Real.
     */
    REAL;

    /**
     * @return true if dense
     */
    public boolean isDense() {
        return ordinal()>=DECIMAL.ordinal();
    }
    /**
     * @param it1 range 1
     * @param it2 range 2
     * @return intersection
     */
    public static NumberRange intersection(NumberRange it1,NumberRange it2) {
        int minOrdinal=Math.min(it1.ordinal(),it2.ordinal());
        return values()[minOrdinal];
    }
    /**
     * @param it1 range 1
     * @param it2 range 2
     * @return union
     */
    public static NumberRange union(NumberRange it1,NumberRange it2) {
        int maxOrdinal=Math.max(it1.ordinal(),it2.ordinal());
        return values()[maxOrdinal];
    }
    /**
     * @param subset subset
     * @param superset superset
     * @return true if subset
     */
    public static boolean isSubsetOf(NumberRange subset,NumberRange superset) {
        return subset.ordinal()<=superset.ordinal();
    }
    /**
     * @param n number
     * @return most specific range
     */
    public static NumberRange getMostSpecificRange(Number n) {
        if (n instanceof Integer || n instanceof Long || n instanceof BigInteger)
            return INTEGER;
        else if (n instanceof BigDecimal)
            return DECIMAL;
        else if (n instanceof BigRational)
            return RATIONAL;
        else
            throw new IllegalArgumentException();
    }
}