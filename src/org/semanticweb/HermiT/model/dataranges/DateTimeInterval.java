/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/**
 * The class can be used to capture date/time intervals. By default, an 
 * instantiation represents an infinite range. The allowed min and max values 
 * are to be in the range of longs.
 * 
 * @author BGlimm
 */
public class DateTimeInterval implements Serializable {
    
    private static final long serialVersionUID = -2796221384062387741L;
    
    public static final BigInteger longMin = new BigInteger("" + Long.MIN_VALUE);
    public static final BigInteger longMax = new BigInteger("" + Long.MAX_VALUE);
    
    protected Long min = null;
    protected Long max = null;
    
    /**
     * Create an interval with the given possibly null upper and lower bounds. 
     * If a value is null, the range is open in that direction. 
     * @param min the lower bound
     * @param max the upper bound
     */
    public DateTimeInterval(Long min, Long max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * Creates a copy of this interval. 
     * @return an interval object that has the same min and max values as this 
     *         one.
     */
    public DateTimeInterval getCopy() {
        return new DateTimeInterval(min, max);
    }
    
    /**
     * Intersect with the given interval. After the intersection, this interval 
     * will have min and max values that are those of the intersection.  
     * @param i an interval
     */
    public void intersectWith(DateTimeInterval i) {
        Long newMax = i.getMax();
        Long newMin = i.getMin();
        if (max == null) {
            if (newMax != null) {
                max = newMax;
            }
        } else {
            if (newMax != null && newMax < max) {
                max = newMax;
            }
        }
        if (min == null) {
            if (newMin != null) {
                min = newMin;
            }
        } else {
            if (newMin != null && newMin > min) {
                min = newMin;
            }
        }
    }
    
    /**
     * Tests whether the given interger is in the range of long. 
     * @param i a BigInteger
     * @return true if the given BigInteger is in the range of long and false 
     * otherwise. 
     */
    public static boolean isLong(BigInteger i) {
        return (i.compareTo(longMin) >= 0 && i.compareTo(longMax) <= 0);
    }
    
    /**
     * Tests for emptyness.
     * @return true if the interval cannot contain values and false otherwise.  
     */
    public boolean isEmpty() {
        return min != null && max != null && min > max;
    }
    
    /**
     * Tests for finiteness. 
     * @return true if the interval is finite (has min and max values not equal 
     * to null) and false otherwise. 
     */
    public boolean isFinite() {
        return min != null && max != null;
    }
    
    /**
     * Tests containment. 
     * @param integer an integer
     * @return true if the interval contains this integer and false otherwise. 
     */
    public boolean contains(long integer) {
        boolean contains = true;
        if (min != null) {
            contains = contains && (min <= integer);
        }
        if (max != null) {
            contains = contains && (max >= integer);
        }
        return contains;
    }
    
    /**
     * Computes the size of the interval. 
     * @return the number of integer values that are contained in this interval 
     * or null if the interval is not finite
     */
    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max < min) return BigInteger.ZERO;
        return new BigInteger("" + max).subtract(new BigInteger("" + min)).add(BigInteger.ONE);
    }

    /**
     * @return the minimal value for this interval
     */
    public Long getMin() {
        return min;
    }

    /**
     * @return the maximal value for this interval
     */
    public Long getMax() {
        return max;
    }

    /** 
     * @param longValue a long
     * @return the next higher long value or null if that value is no longer in 
     * the range of longs.
     */
    public static Long increase(Long longValue) {
        if (longValue < Long.MAX_VALUE) {
            return longValue + 1;
        } 
        return null;
    }
    
    /**
     * @param longValue a long
     * @return the next lower long value or null if that value is no longer in 
     * the range of longs.
     */
    public static Long decrease(Long longValue) {
        if (longValue > Long.MIN_VALUE) {
            return longValue - 1;
        } 
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (min == null && max == null) return "";
        StringBuffer buffer = new StringBuffer();
        if (min != null) {
            buffer.append(">= " 
                    + DatatypeRestrictionDateTime.dfm.format(new Date(min)));
            if (max != null) buffer.append(" ");
        } 
        if (max != null) {
            buffer.append(">= " 
                + DatatypeRestrictionDateTime.dfm.format(new Date(max)));
        }
        return buffer.toString();
    }
}
