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
package org.semanticweb.HermiT.datatypes.doublenum;

import java.util.Collection;

public class DoubleInterval {
    protected final double m_lowerBoundInclusive;
    protected final double m_upperBoundInclusive;

    public DoubleInterval(double lowerBoundInclusive,double upperBoundInclusive) {
        assert !isIntervalEmpty(lowerBoundInclusive,upperBoundInclusive);
        m_lowerBoundInclusive=lowerBoundInclusive;
        m_upperBoundInclusive=upperBoundInclusive;
    }
    /**
     * Computes the intersection of this interval with the supplied one. If the two intervals do not intersect, the result is null.
     */
    public DoubleInterval intersectWith(DoubleInterval that) {
        // This code uses the assumption no bound in either interval contains NaN.
        double newLowerBoundInclusive;
        if (isSmallerEqual(m_lowerBoundInclusive,that.m_lowerBoundInclusive))
            newLowerBoundInclusive=that.m_lowerBoundInclusive;
        else
            newLowerBoundInclusive=m_lowerBoundInclusive;
        double newUpperBoundInclusive;
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
            return new DoubleInterval(newLowerBoundInclusive,newUpperBoundInclusive);
    }
    protected boolean isEqual(double lowerBoundInclusive,double upperBoundInclusive) {
        return areIdentical(m_lowerBoundInclusive,lowerBoundInclusive) && areIdentical(m_upperBoundInclusive,upperBoundInclusive);
    }
    public int subtractSizeFrom(int argument) {
        return subtractIntervalSizeFrom(m_lowerBoundInclusive,m_upperBoundInclusive,argument);
    }
    public boolean contains(double value) {
        return contains(m_lowerBoundInclusive,m_upperBoundInclusive,value);
    }
    public void enumerateNumbers(Collection<Object> numbers) {
        // We know that the interval is not empty; hence, neither bound is NaN.
        double number=m_lowerBoundInclusive;
        while (!areIdentical(number,m_upperBoundInclusive)) {
            numbers.add(number);
            number=nextDouble(number);
        }
        numbers.add(m_upperBoundInclusive);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("DOUBLE[");
        buffer.append(m_lowerBoundInclusive);
        buffer.append("..");
        buffer.append(m_upperBoundInclusive);
        buffer.append(']');
        return buffer.toString();
    }
    public static boolean isNaN(long bits) {
        return ((bits & 0x7ff0000000000000L)==0x7ff0000000000000L) && ((bits & 0x000fffffffffffffL)!=0);
    }
    protected static boolean isIntervalEmpty(double lowerBoundInclusive,double upperBoundInclusive) {
        return !isSmallerEqual(lowerBoundInclusive,upperBoundInclusive);
    }
    public static boolean areIdentical(double value1,double value2) {
        return Double.doubleToLongBits(value1)==Double.doubleToLongBits(value2);
    }
    public static double nextDouble(double value) {
        long bits=Double.doubleToRawLongBits(value);
        long magnitude=(bits & 0x7fffffffffffffffL);
        boolean positive=((bits & 0x8000000000000000L)==0);
        // The successors of NaN and +INF are these numbers themselves.
        if (isNaN(bits) || (magnitude==0x7ff0000000000000L && positive))
            return value;
        else {
            boolean newPositive;
            long newMagnitude;
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
            long newBits=newMagnitude | (newPositive ? 0 : 0x8000000000000000L);
            return Double.longBitsToDouble(newBits);
        }
    }
    public static double previousDouble(double value) {
        long bits=Double.doubleToRawLongBits(value);
        long magnitude=(bits & 0x7fffffffffffffffL);
        boolean positive=((bits & 0x8000000000000000L)==0);
        // The predecessors of NaN and -INF are these numbers themselves.
        if (isNaN(bits) || (magnitude==0x7ff0000000000000L && !positive))
            return value;
        else {
            boolean newPositive;
            long newMagnitude;
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
            long newBits=newMagnitude | (newPositive ? 0 : 0x8000000000000000L);
            return Double.longBitsToDouble(newBits);
        }
    }
    public static int subtractIntervalSizeFrom(double lowerBoundInclusive,double upperBoundInclusive,int argument) {
        if (argument<=0)
            return 0;
        long bitsLowerBoundInclusive=Double.doubleToRawLongBits(lowerBoundInclusive);
        long bitsUpperBoundInclusive=Double.doubleToRawLongBits(upperBoundInclusive);
        if (isNaN(bitsLowerBoundInclusive) || isNaN(bitsUpperBoundInclusive))
            return argument;
        boolean positiveLowerBoundInclusive=((bitsLowerBoundInclusive & 0x8000000000000000L)==0);
        boolean positiveUpperBoundInclusive=((bitsUpperBoundInclusive & 0x8000000000000000L)==0);
        long magnitudeLowerBoundInclusive=(bitsLowerBoundInclusive & 0x7fffffffffffffffL);
        long magnitudeUpperBoundInclusive=(bitsUpperBoundInclusive & 0x7fffffffffffffffL);
        // Check whether the given interval is correctly oriented.
        if (!isSmallerEqual(positiveLowerBoundInclusive,magnitudeLowerBoundInclusive,positiveUpperBoundInclusive,magnitudeUpperBoundInclusive))
            return argument;
        // Now determine the number of elements. This works even if 'lowerBoundInclusive' or 'upperBoundInclusive' is +INF or -INF.
        if (positiveLowerBoundInclusive && positiveUpperBoundInclusive) {
            // It must be that magnitudeLowerBoundInclusive<magnitudeUpperBoundInclusive.
            long size=magnitudeUpperBoundInclusive-magnitudeLowerBoundInclusive+1;
            return (int)Math.max(((long)argument)-size,0);
        }
        else if (!positiveLowerBoundInclusive && !positiveUpperBoundInclusive) {
            // It must be that magnitudeUpperBoundInclusive<magnitudeLowerBoundInclusive.
            long size=magnitudeLowerBoundInclusive-magnitudeUpperBoundInclusive+1;
            return (int)Math.max(((long)argument)-size,0);
        }
        else if (!positiveLowerBoundInclusive && positiveUpperBoundInclusive) {
            // the number of values from 'lowerBoundInclusive' to -0
            long startToMinusZero=magnitudeLowerBoundInclusive+1;
            if (startToMinusZero>=argument)
                return 0;
            argument=(int)(argument-startToMinusZero);
            // The number of values from +0 to 'upperBoundInclusive'.
            long plusZeroToEnd=1+magnitudeUpperBoundInclusive;
            if (plusZeroToEnd>=argument)
                return 0;
            return (int)(argument-plusZeroToEnd);
        }
        else // if (positiveLowerBoundInclusive && !positiveUpperBoundInclusiev) is impossible at this point
            throw new IllegalStateException();
    }
    public static boolean contains(double startInclusive,double endInclusive,double value) {
        long bitsStart=Double.doubleToRawLongBits(startInclusive);
        long bitsEnd=Double.doubleToRawLongBits(endInclusive);
        long bitsValue=Double.doubleToRawLongBits(value);
        if (isNaN(bitsStart) || isNaN(bitsEnd) || isNaN(bitsValue))
            return false;
        boolean positiveStart=((bitsStart & 0x8000000000000000L)==0);
        boolean positiveEnd=((bitsEnd & 0x8000000000000000L)==0);
        boolean positiveValue=((bitsValue & 0x8000000000000000L)==0);
        long magnitudeStart=(bitsStart & 0x7fffffffffffffffL);
        long magnitudeEnd=(bitsEnd & 0x7fffffffffffffffL);
        long magnitudeValue=(bitsValue & 0x7fffffffffffffffL);
        return isSmallerEqual(positiveStart,magnitudeStart,positiveValue,magnitudeValue) && isSmallerEqual(positiveValue,magnitudeValue,positiveEnd,magnitudeEnd);
    }
    public static boolean isSmallerEqual(double value1,double value2) {
        long bitsValue1=Double.doubleToRawLongBits(value1);
        long bitsValue2=Double.doubleToRawLongBits(value2);
        if (isNaN(bitsValue1) || isNaN(bitsValue2))
            return false;
        boolean positiveValue1=((bitsValue1 & 0x8000000000000000L)==0);
        boolean positiveValue2=((bitsValue2 & 0x8000000000000000L)==0);
        long magnitudeValue1=(bitsValue1 & 0x7fffffffffffffffL);
        long magnitudeValue2=(bitsValue2 & 0x7fffffffffffffffL);
        return isSmallerEqual(positiveValue1,magnitudeValue1,positiveValue2,magnitudeValue2);
    }
    public static boolean isSmallerEqual(boolean positive1,long magnitude1,boolean positive2,long magnitude2) {
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
