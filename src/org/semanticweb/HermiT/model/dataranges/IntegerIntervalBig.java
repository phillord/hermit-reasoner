/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

/**
 * The class represents integer intervals of any size (uses BigInteger). If min 
 * and max values unequal to null are given, the range is finite and otherwise 
 * it is infinite. 
 * 
 * @author BGlimm
 */
public class IntegerIntervalBig implements IntegerInterval {

    private static final long serialVersionUID = 722684568940634586L;

    public static final Number ZERO = BigInteger.ZERO;
    
    protected BigInteger min = null;
    protected BigInteger max = null;
    
    /**
     * Creates an integer interval with the given (possibly null) lower and 
     * upper bound that contains the lower and upper bound values (it is a 
     * closed interval). 
     * @param minInclusive the lower bound
     * @param maxInclusive the upper bound
     */
    public IntegerIntervalBig(BigInteger minInclusive, BigInteger maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getCopy()
     */
    public IntegerInterval getCopy() {
        return new IntegerIntervalBig(min, max);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getInstance(java.lang.Number, java.lang.Number)
     */
    public IntegerInterval getInstance(Number min, Number max) {
        BigInteger minBig = min == null ? null : new BigInteger("" + min);
        BigInteger maxBig = max == null ? null : new BigInteger("" + max);
        return new IntegerIntervalBig(minBig, maxBig);
    }
    
    /**
     * Creates an interval that has the same upper and lower bound as the given 
     * one and uses internally BigIntegers. 
     * @param interval an IntegerInterval
     * @return an IntegerInterval that has the same min max values as the given 
     * one and uses internally BigIntegers
     */
    public static IntegerIntervalBig toIntegerIntervalBig(IntegerInterval interval) {
        if (interval instanceof IntegerIntervalBig) {
            return (IntegerIntervalBig) interval;
        } else {
            return new IntegerIntervalBig(
                    new BigInteger("" + interval.getMin()), 
                    new BigInteger("" + interval.getMax()));
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#intersectWith(org.semanticweb.HermiT.model.dataranges.IntegerInterval)
     */
    public IntegerInterval intersectWith(IntegerInterval i) {
        BigInteger newMax = null;
        BigInteger newMin = null;
        if (i.getMax() != null) {
            newMax = new BigInteger("" + i.getMax());
        }
        if (i.getMin() != null) {
            newMin = new BigInteger("" + i.getMin());
        }
        if (max == null) {
            max = newMax;
        } else {
            if (newMax != null && newMax.compareTo(max) < 0) {
                max = newMax;
            }
        }
        if (min == null) {
            min = newMin;
        } else {
            if (newMin != null && newMin.compareTo(min) > 0) {
                min = newMin;
            }
        }
        return this;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#isEmpty()
     */
    public boolean isEmpty() {
        return (min != null 
                && max != null 
                && min.compareTo(max) > 0);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#isFinite()
     */
    public boolean isFinite() {
        return min != null && max != null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#contains(java.lang.Number)
     */
    public boolean contains(Number integer) {
        boolean contains = true;
        BigInteger bigInteger = new BigInteger("" + integer);
        if (min != null) {
            contains = contains && (min.compareTo(bigInteger) <= 0);
        }
        if (max != null) {
            contains = contains && (max.compareTo(bigInteger) >= 0);
        }
        return contains;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getCardinality()
     */
    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max.compareTo(min) < 0) return BigInteger.ZERO;
        return max.subtract(min).add(BigInteger.ONE);
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getMin()
     */
    public Number getMin() {
        return min;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getMax()
     */
    public Number getMax() {
        return max;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#decreasedMin()
     */
    public Number decreasedMin() {
        return min.subtract(BigInteger.ONE);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#increasedMax()
     */
    public Number increasedMax() {
        return max.add(BigInteger.ONE);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getZero()
     */
    public Number getZero() {
        return ZERO;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (min == null && max == null) return "";
        StringBuffer buffer = new StringBuffer();
        if (min != null) {
            buffer.append(">= " + min);
            if (max != null) buffer.append(" ");
        } 
        if (max != null) {
            buffer.append(">= " + max);
        }
        return buffer.toString();
    }
}
