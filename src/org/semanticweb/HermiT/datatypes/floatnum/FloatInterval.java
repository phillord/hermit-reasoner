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

public class FloatInterval {
    protected final float m_lowerBoundInclusive;
    protected final float m_upperBoundInclusive;

    public FloatInterval(float lowerBoundInclusive,float upperBoundInclusive) {
        assert !isIntervalEmpty(lowerBoundInclusive,upperBoundInclusive);
        m_lowerBoundInclusive=lowerBoundInclusive;
        m_upperBoundInclusive=upperBoundInclusive;
    }
    /**
     * Computes the intersection of this interval with the supplied one. If the two intervals do not intersect, the result is null.
     */
    public FloatInterval intersectWith(FloatInterval that) {
        // This code uses the assumption no bound in either interval contains NaN.
        float newLowerBoundInclusive;
        if (isSmallerEqual(m_lowerBoundInclusive,that.m_lowerBoundInclusive))
            newLowerBoundInclusive=that.m_lowerBoundInclusive;
        else
            newLowerBoundInclusive=m_lowerBoundInclusive;
        float newUpperBoundInclusive;
        if (isSmallerEqual(m_upperBoundInclusive,that.m_upperBoundInclusive))
            newUpperBoundInclusive=m_upperBoundInclusive;
        else
            newUpperBoundInclusive=that.m_upperBoundInclusive;
        if (isIntervalEmpty(newLowerBoundInclusive,newUpperBoundInclusive))
            return null;
        else if (isEqual(newLowerBoundInclusive,newUpperBoundInclusive))
            return this;
        else if (that.isEqual(newLowerBoundInclusive,newUpperBoundInclusive))
            return that;
        else
            return new FloatInterval(newLowerBoundInclusive,newUpperBoundInclusive);
    }
    protected boolean isEqual(float lowerBoundInclusive,float upperBoundInclusive) {
        return areIdentical(m_lowerBoundInclusive,lowerBoundInclusive) && areIdentical(m_upperBoundInclusive,upperBoundInclusive);
    }
    public int subtractSizeFrom(int argument) {
        return subtractIntervalSizeFrom(m_lowerBoundInclusive,m_upperBoundInclusive,argument);
    }
    public boolean contains(float value) {
        return contains(m_lowerBoundInclusive,m_upperBoundInclusive,value);
    }
    public void enumerateNumbers(Collection<Object> numbers) {
        // We know that the interval is not empty; hence, neither bound is NaN.
        float number=m_lowerBoundInclusive;
        while (!areIdentical(number,m_upperBoundInclusive)) {
            numbers.add(number);
            number=nextFloat(number);
        }
        numbers.add(m_upperBoundInclusive);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("FLOAT[");
        buffer.append(m_lowerBoundInclusive);
        buffer.append("..");
        buffer.append(m_upperBoundInclusive);
        buffer.append(']');
        return buffer.toString();
    }
    public static boolean isNaN(int bits) {
        return ((bits & 0x7f800000)==0x7f800000) && ((bits & 0x003fffff)!=0);
    }
    protected static boolean isIntervalEmpty(float lowerBoundInclusive,float upperBoundInclusive) {
        return !isSmallerEqual(lowerBoundInclusive,upperBoundInclusive);
    }
    public static boolean areIdentical(float value1,float value2) {
        return Float.floatToIntBits(value1)==Float.floatToIntBits(value2);
    }
    public static float nextFloat(float value) {
        int bits=Float.floatToIntBits(value);
        int magnitude=(bits & 0x7fffffff);
        boolean positive=((bits & 0x80000000)==0);
        // The successors of NaN and +INF are these numbers themselves.
        if (isNaN(bits) || (magnitude==0x7f800000 && positive))
            return value;
        else {
            boolean newPositive;
            int newMagnitude;
            if (positive) {
                newPositive=true;
                newMagnitude=magnitude+1;
            }
            else if (!positive && magnitude==0) {
                // The successor of -0 is +0
                newPositive=true;
                newMagnitude=0;
            }
            else { // if (!positive && magnitude!=0)
                newPositive=false;
                newMagnitude=magnitude-1;
            }
            int newBits=newMagnitude | (newPositive ? 0 : 0x80000000);
            return Float.intBitsToFloat(newBits);
        }
    }
    public static float previousFloat(float value) {
        int bits=Float.floatToIntBits(value);
        int magnitude=(bits & 0x7fffffff);
        boolean positive=((bits & 0x80000000)==0);
        // The predecessors of NaN and -INF are these numbers themselves.
        if (isNaN(bits) || (magnitude==0x7f800000 && !positive))
            return value;
        else {
            boolean newPositive;
            int newMagnitude;
            if (!positive) {
                newPositive=false;
                newMagnitude=magnitude+1;
            }
            else if (positive && magnitude==0) {
                // The predecessor of +0 is -0
                newPositive=false;
                newMagnitude=0;
            }
            else { // if (positive && magnitude!=0)
                newPositive=true;
                newMagnitude=magnitude-1;
            }
            int newBits=newMagnitude | (newPositive ? 0 : 0x80000000);
            return Float.intBitsToFloat(newBits);
        }
    }
    public static int subtractIntervalSizeFrom(float lowerBoundInclusive,float upperBoundInclusive,int argument) {
        if (argument<=0)
            return 0;
        int bitsLowerBoundInclusive=Float.floatToIntBits(lowerBoundInclusive);
        int bitsUpperBoundInclusive=Float.floatToIntBits(upperBoundInclusive);
        if (isNaN(bitsLowerBoundInclusive) || isNaN(bitsUpperBoundInclusive))
            return argument;
        boolean positiveLowerBoundInclusive=((bitsLowerBoundInclusive & 0x80000000)==0);
        boolean positiveUpperBoundInclusive=((bitsUpperBoundInclusive & 0x80000000)==0);
        int magnitudeLowerBoundInclusive=(bitsLowerBoundInclusive & 0x7fffffff);
        int magnitudeUpperBoundInclusive=(bitsUpperBoundInclusive & 0x7fffffff);
        // Check whether the given interval is correctly oriented.
        if (!isSmallerEqual(positiveLowerBoundInclusive,magnitudeLowerBoundInclusive,positiveUpperBoundInclusive,magnitudeUpperBoundInclusive))
            return argument;
        // Now determine the number of elements. This works even if 'lowerBoundInclusive' or 'upperBoundInclusive' is +INF or -INF.
        if (positiveLowerBoundInclusive && positiveUpperBoundInclusive) {
            // It must be that magnitudeLowerBoundInclusive<magnitudeUpperBoundInclusive.
            int size=magnitudeUpperBoundInclusive-magnitudeLowerBoundInclusive+1;
            return Math.max(argument-size,0);
        }
        else if (!positiveLowerBoundInclusive && !positiveUpperBoundInclusive) {
            // It must be that magnitudeUpperBoundInclusive<magnitudeLowerBoundInclusive.
            int size=magnitudeLowerBoundInclusive-magnitudeUpperBoundInclusive+1;
            return Math.max(argument-size,0);
        }
        else if (!positiveLowerBoundInclusive && positiveUpperBoundInclusive) {
            // the number of values from 'lowerBoundInclusive' to -0
            int startToMinusZero=magnitudeLowerBoundInclusive+1;
            if (startToMinusZero>=argument)
                return 0;
            argument=argument-startToMinusZero;
            // The number of values from +0 to 'upperBoundInclusive'.
            int plusZeroToEnd=1+magnitudeUpperBoundInclusive;
            if (plusZeroToEnd>=argument)
                return 0;
            return argument-plusZeroToEnd;
        }
        else // if (positiveLowerBoundInclusive && !positiveUpperBoundInclusiev) is impossible at this point
            throw new IllegalStateException();
    }
    public static boolean contains(float startInclusive,float endInclusive,float value) {
        int bitsStart=Float.floatToIntBits(startInclusive);
        int bitsEnd=Float.floatToIntBits(endInclusive);
        int bitsValue=Float.floatToIntBits(value);
        if (isNaN(bitsStart) || isNaN(bitsEnd) || isNaN(bitsValue))
            return false;
        boolean positiveStart=((bitsStart & 0x80000000)==0);
        boolean positiveEnd=((bitsEnd & 0x80000000)==0);
        boolean positiveValue=((bitsValue & 0x80000000)==0);
        int magnitudeStart=(bitsStart & 0x7fffffff);
        int magnitudeEnd=(bitsEnd & 0x7fffffff);
        int magnitudeValue=(bitsValue & 0x7fffffff);
        return isSmallerEqual(positiveStart,magnitudeStart,positiveValue,magnitudeValue) && isSmallerEqual(positiveValue,magnitudeValue,positiveEnd,magnitudeEnd);
    }
    public static boolean isSmallerEqual(float value1,float value2) {
        int bitsValue1=Float.floatToIntBits(value1);
        int bitsValue2=Float.floatToIntBits(value2);
        if (isNaN(bitsValue1) || isNaN(bitsValue2))
            return false;
        boolean positiveValue1=((bitsValue1 & 0x80000000)==0);
        boolean positiveValue2=((bitsValue2 & 0x80000000)==0);
        int magnitudeValue1=(bitsValue1 & 0x7fffffff);
        int magnitudeValue2=(bitsValue2 & 0x7fffffff);
        return isSmallerEqual(positiveValue1,magnitudeValue1,positiveValue2,magnitudeValue2);
    }
    public static boolean isSmallerEqual(boolean positive1,int magnitude1,boolean positive2,int magnitude2) {
        if (positive1 && positive2)
            return magnitude1<=magnitude2;
        else if (!positive1 && positive2)
            return true;
        else if (positive1 && !positive2)
            return false;
        else // if (!positive1 && !positive2)
            return magnitude1>=magnitude2;
    }
}
