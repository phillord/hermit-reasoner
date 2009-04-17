// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.doublenum;

import java.util.Collection;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class EntireDoubleSubset implements ValueSpaceSubset {

    public String getDatatypeURI() {
        return DoubleDatatypeHandler.XSD_DOUBLE;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int leftover=DoubleInterval.subtractIntervalSizeFrom(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,number);
        // The following check contains 1 because there is one NaN in the value space.
        return leftover<=1;
    }
    public boolean containsDataValue(Object dataValue) {
        assert dataValue instanceof Double;
        return true;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        dataValues.add(Double.NaN);
        double number=Double.NEGATIVE_INFINITY;
        while (!DoubleInterval.areIdentical(number,Double.POSITIVE_INFINITY)) {
            dataValues.add(number);
            number=DoubleInterval.nextDouble(number);
        }
        dataValues.add(Double.POSITIVE_INFINITY);
    }
    public String toString() {
        return "xsd:double";
    }
}