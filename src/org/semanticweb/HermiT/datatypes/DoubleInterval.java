/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The class can be used to capture double intervals. An instantiation always 
 * represents a finite range since the number of doubles is finite. The allowed 
 * min and max values are to be in the range of doubles.
 * 
 * @author BGlimm
 */
public class DoubleInterval implements Serializable {

    private static final long serialVersionUID = 9196389740657611233L;
    
    public static final BigDecimal doubleMin = new BigDecimal("" + -Double.MAX_VALUE);
    public static final BigDecimal doubleMax = new BigDecimal("" + Double.MAX_VALUE);
    
    protected double min = -Double.MAX_VALUE;
    protected double max = Double.MAX_VALUE;
    
    /**
     * Creates a double interval with the min and max values for doubles as 
     * lower and upper bound respectively. 
     */
    public DoubleInterval() {
        super();
    }
    
    /**
     * Creates a double interval with the given min and max values. 
     * @param minInclusive the lower bound
     * @param maxInclusive the upper bound
     */
    public DoubleInterval(double minInclusive, double maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    /**
     * Creates a copy of this interval. 
     * @return an interval object that has the same min and max values as this 
     *         one.
     */
    public DoubleInterval getCopy() {
        return new DoubleInterval(min, max);
    }
    
    /**
     * Checks whether the given number is in the range of doubles.
     * @param bigDecimal a decimal
     * @return true if the decimal is in the range of double and false otherwise
     */
    public static boolean isDouble(BigDecimal bigDecimal) {
        return (bigDecimal.compareTo(doubleMin) >= 0 && bigDecimal.compareTo(doubleMax) <= 0);
    }
    
    /**
     * Intersect with the given interval. After the intersection, this interval 
     * will have min and max values that are those of the intersection.  
     * @param i an interval
     */
    public void intersectWith(DoubleInterval i) {
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
     * @param value a double
     * @return true if the input is -0.0 and false otherwise
     */
    public boolean isMinusZero(double value) {
        if (value != 0.0) return false;
        long bits = Double.doubleToRawLongBits(value);
        return ((bits & 0x7fffffffffffffffl) != bits);
    }
    
    /**
     * Tests whether the given value is +0.0
     * @param value a double
     * @return true if the input is +0.0 and false otherwise
     */
    public boolean isPlusZero(double value) {
        if (value != 0.0) return false;
        long bits = Double.doubleToRawLongBits(value);
        return ((bits & 0x7fffffffffffffffl) == bits);
    }
    
    /**
     * Tests containment. 
     * @param d a double
     * @return true if the interval contains this double and false otherwise. 
     */
    public boolean contains(double d) {
        if (isPlusZero(min) && isMinusZero(d)) return false;
        if (isMinusZero(max) && isPlusZero(d)) return false;
        return (min <= d) && (max >= d);
    }

    /**
     * Computes the size of the interval. 
     * @return the number of double values that are contained in this interval.  
     */
    public BigInteger getCardinality() {
        if (max < min) return BigInteger.ZERO;
        // Extract the sign and magnitude from 'start'
        long bitsStart  = Double.doubleToRawLongBits(min);
        long bitsEnd = Double.doubleToRawLongBits(max);
        if (DatatypeRestrictionDouble.isNaN(min) 
                || DatatypeRestrictionDouble.isNaN(max)) {
            return BigInteger.ZERO;
        }
        boolean positiveStart = ((bitsStart & 0x8000000000000000l) == 0);
        boolean positiveEnd = ((bitsEnd & 0x8000000000000000l) == 0);
        long magnitudeStart = bitsStart & 0x7fffffffffffffffl;
        long magnitudeEnd = bitsEnd & 0x7fffffffffffffffl;
        
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
        double oldMin = min;
        min = DatatypeRestrictionDouble.nextDouble(oldMin);
        return min != oldMin;
    }
    
    /**
     * @return the minimal value for this interval
     */
    public double getMin() {
        return min;
    }

    /**
     * @return the maximal value for this interval
     */
    public double getMax() {
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