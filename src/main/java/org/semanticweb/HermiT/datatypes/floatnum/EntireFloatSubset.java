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
package org.semanticweb.HermiT.datatypes.floatnum;

import java.util.Collection;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class EntireFloatSubset implements ValueSpaceSubset {

    public boolean hasCardinalityAtLeast(int number) {
        int leftover=FloatInterval.subtractIntervalSizeFrom(Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY,number);
        // The following check contains 1 because there is one NaN in the value space.
        return leftover<=1;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof Float)
            return true;
        return false;
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