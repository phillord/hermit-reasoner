// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.owlreal;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum NumberRange {
    NOTHING,INTEGER,DECIMAL,RATIONAL,REAL;

    public boolean isDense() {
        return ordinal()>=DECIMAL.ordinal();
    }
    public static NumberRange intersection(NumberRange it1,NumberRange it2) {
        int minOrdinal=Math.min(it1.ordinal(),it2.ordinal());
        return values()[minOrdinal];
    }
    public static NumberRange union(NumberRange it1,NumberRange it2) {
        int maxOrdinal=Math.max(it1.ordinal(),it2.ordinal());
        return values()[maxOrdinal];
    }
    public static boolean isSubsetOf(NumberRange subset,NumberRange superset) {
        return subset.ordinal()<=superset.ordinal();
    }
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