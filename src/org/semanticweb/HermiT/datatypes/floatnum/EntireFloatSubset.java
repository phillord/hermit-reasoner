/**
 * 
 */
package org.semanticweb.HermiT.datatypes.floatnum;

import java.util.Collection;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class EntireFloatSubset implements ValueSpaceSubset {

    public String getDatatypeURI() {
        return FloatDatatypeHandler.XSD_FLOAT;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int leftover=FloatInterval.subtractIntervalSizeFrom(Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY,number);
        // The following check contains 1 because there is one NaN in the value space.
        return leftover<=1;
    }
    public boolean containsDataValue(Object dataValue) {
        assert dataValue instanceof Float;
        return true;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        dataValues.add(Float.NaN);
        float number=Float.NEGATIVE_INFINITY;
        while (!FloatInterval.areIdentical(number,Float.POSITIVE_INFINITY)) {
            dataValues.add(number);
            number=FloatInterval.nextFloat(number);
        }
        dataValues.add(Float.POSITIVE_INFINITY);
    }
    public String toString() {
        return "xsd:float";
    }
}