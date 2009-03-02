/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The class can be used to capture float intervals. An instantiation always 
 * represents a finite range since the number of floats is finite. The allowed 
 * min and max values are to be in the range of floats.
 * 
 * @author BGlimm
 */
public class FloatInterval implements Serializable {

    private static final long serialVersionUID = 9196389740657611233L;
    
    public static final BigDecimal floatMin = new BigDecimal("" + -Float.MAX_VALUE);
    public static final BigDecimal floatMax = new BigDecimal("" + Float.MAX_VALUE);
    
    protected float min = -Float.MAX_VALUE;
    protected float max = Float.MAX_VALUE;
    
    /**
     * Creates a float interval with the min and max values for floats as 
     * lower and upper bound respectively. 
     */
    public FloatInterval() {
        super();
    }
    
    /**
     * Creates a float interval with the given min and max values. 
     * @param minInclusive the lower bound
     * @param maxInclusive the upper bound
     */
    public FloatInterval(float minInclusive, float maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    /**
     * Creates a copy of this interval. 
     * @return an interval object that has the same min and max values as this 
     *         one.
     */
    public FloatInterval getCopy() {
        return new FloatInterval(min, max);
    }
    
    /**
     * Checks whether the given number is in the range of floats.
     * @param bigDecimal a decimal
     * @return true if the decimal is in the range of float and false otherwise
     */
    public static boolean isFloat(BigDecimal bigDecimal) {
        return (bigDecimal.compareTo(floatMin) >= 0 && bigDecimal.compareTo(floatMax) <= 0);
    }
    
    /**
     * Intersect with the given interval. After the intersection, this interval 
     * will have min and max values that are those of the intersection.  
     * @param i an interval
     */
    public void intersectWith(FloatInterval i) {
        if (i.getMax() < max || (isMinusZero(i.getMax()) && isPlusZero(max))) {
            max = i.getMax();
        }
        if (i.getMin() > min || (isPlusZero(i.getMin()) && isMinusZero(min))) {
            min = i.getMin();
        }
    }
    
    /**
     * Tests for emptiness.
     * @return true if the interval cannot contain values and false otherwise.  
     */
    public boolean isEmpty() {
        return (min > max);
    }
    
    /**
     * Tests for finiteness. 
     * @return always true
     */
    public boolean isFinite() {
        return true;
    }
    
    /**
     * Tests whether the given value is -0.0
     * @param value a float
     * @return true if the input is -0.0 and false otherwise
     */
    public boolean isMinusZero(float value) {
        if (value != 0.0) return false;
        int bits = Float.floatToRawIntBits(value);
        return ((bits & 0x7fffffff) != bits);
    }
    
    /**
     * Tests whether the given value is +0.0
     * @param value a float
     * @return true if the input is +0.0 and false otherwise
     */
    public boolean isPlusZero(float value) {
        if (value != 0.0) return false;
        int bits = Float.floatToRawIntBits(value);
        return ((bits & 0x7fffffff) == bits);
    }
    
    /**
     * Tests containment. 
     * @param d a float
     * @return true if the interval contains this float and false otherwise. 
     */
    public boolean contains(float d) {
        if (isPlusZero(min) && isMinusZero(d)) return false;
        if (isMinusZero(max) && isPlusZero(d)) return false;
        return (min <= d) && (max >= d);
    }

    /**
     * Computes the size of the interval. 
     * @return the number of float values that are contained in this interval.  
     */
    public BigInteger getCardinality() {
        if (max < min) return BigInteger.ZERO;
        // Extract the sign and magnitude from 'start'
        int bitsStart  = Float.floatToRawIntBits(min);
        int bitsEnd = Float.floatToRawIntBits(max);
        if (DatatypeRestrictionFloat.isNaN(min) 
                || DatatypeRestrictionFloat.isNaN(max)) {
            return BigInteger.ZERO;
        }
        boolean positiveStart = ((bitsStart & 0x80000000) == 0);
        boolean positiveEnd = ((bitsEnd & 0x80000000) == 0);
        int magnitudeStart = bitsStart & 0x7fffffff;
        int magnitudeEnd = bitsEnd & 0x7fffffff;
        
        // Now determine the number of elements. This works even if either 
        // of 'start' and 'end' is +inf or -inf.
        if (positiveStart && positiveEnd) {
            return new BigInteger("" + (magnitudeEnd - magnitudeStart + 1));
        } else if (!positiveStart && !positiveEnd) {
            return new BigInteger("" + (magnitudeStart - magnitudeEnd + 1));
        } else if (!positiveStart && positiveEnd) {
            // the number of values from 'start' to -0
            BigInteger rangeSize = (new BigInteger("" + magnitudeStart)).add(BigInteger.ONE);
            // the number of values from +0 to 'end'
            rangeSize = rangeSize.add((new BigInteger("" + magnitudeEnd)).add(BigInteger.ONE));
            return rangeSize;
        } else {
            // if (positiveStart && !positiveEnd)
            return BigInteger.ZERO;
        }
    }
    
    /**
     * Increases the lower bound to the next higher value if there is one. 
     * @return true if the value was increased and false otherwise
     */
    public boolean increaseMin() {
        float oldMin = min;
        min = DatatypeRestrictionFloat.nextFloat(oldMin);
        return min != oldMin;
    }
    
    /**
     * @return the minimal value for this interval
     */
    public float getMin() {
        return min;
    }

    /**
     * @return the maximal value for this interval
     */
    public float getMax() {
        return max;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(">= " + min);
        buffer.append(" ");
        buffer.append("<= " + max);
        return buffer.toString();
    }
}