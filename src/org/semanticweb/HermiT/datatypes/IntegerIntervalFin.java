/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

/**
 * The class represents integer intervals and if lower or upper bounds are 
 * given, they are in the range of Longs. If min and max values unequal to null 
 * are given, the range is finite and otherwise it is infinite. 
 * 
 * @author BGlimm
 */
public class IntegerIntervalFin implements IntegerInterval {

    private static final long serialVersionUID = -2266620919096621873L;
    
    public static final BigInteger longMin = new BigInteger("" + Long.MIN_VALUE);
    public static final BigInteger longMax = new BigInteger("" + Long.MAX_VALUE);
    public static final BigInteger intMin = new BigInteger("" + Integer.MIN_VALUE);
    public static final BigInteger intMax = new BigInteger("" + Integer.MAX_VALUE);
    
    public static final Number ZERO = 0l;
    
    protected Long min = null;
    protected Long max = null;
    
    /**
     * Creates an integer interval with the given (possibly null) lower and 
     * upper bound that contains the lower and upper bound values (it is a 
     * closed interval). 
     * @param minInclusive the lower bound
     * @param maxInclusive the upper bound
     */
    public IntegerIntervalFin(Long minInclusive, Long maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getCopy()
     */
    public IntegerInterval getCopy() {
        return new IntegerIntervalFin(min, max);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getInstance(java.lang.Number, java.lang.Number)
     */
    public IntegerInterval getInstance(Number min, Number max) {
        BigInteger minBig = null;
        BigInteger maxBig = null;
        Long minLong = null;
        Long maxLong = null;
        IntegerInterval i = new IntegerIntervalFin(Long.MIN_VALUE, Long.MAX_VALUE);
        boolean needsBig = false;
        if (min != null) {
            minBig = new BigInteger("" + min);
            if (!i.contains(min)) {
                needsBig = true;
            } else {
                minLong = min.longValue();
            }
        }
        if (max != null) {
            maxBig = new BigInteger("" + max);
            if (!i.contains(max)) {
                needsBig = true;
            } else {
                maxLong = max.longValue();
            }
        }        
        if (needsBig) {
            return new IntegerIntervalBig(minBig, maxBig);
        } else {
            return new IntegerIntervalFin(minLong, maxLong);
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#intersectWith(org.semanticweb.HermiT.model.dataranges.IntegerInterval)
     */
    public IntegerInterval intersectWith(IntegerInterval i) {
        if (i instanceof IntegerIntervalBig) {
            return i.intersectWith(this);
        } else {
            Number newMax = i.getMax();
            Number newMin = i.getMin();
            if (max == null) {
                if (i.getMax() != null) {
                    max = newMax.longValue();
                }
            } else {
                if (i.getMax() != null && newMax.longValue() < max) {
                    max = newMax.longValue();
                }
            }
            if (min == null) {
                if (i.getMin() != null) {
                    min = newMin.longValue();
                }
            } else {
                if (i.getMin() != null && newMin.longValue() > min) {
                    min = newMin.longValue();
                }
            }
            return this;
        }
    }
    
    /**
     * Checks whether the given BigInteger is in the range of Longs. 
     * @param i a BigInteger
     * @return true if the input is in the range of Longs and false otherwise
     */
    public static boolean isLong(BigInteger i) {
        return (i.compareTo(longMin) >= 0 && i.compareTo(longMax) <= 0);
    }

    /**
     * Checks whether the given BigInteger is in the range of ints. 
     * @param i a BigInteger
     * @return true if the input is in the range of ints and false otherwise
     */
    public static boolean isInt(BigInteger i) {
        return (i.compareTo(intMin) >= 0 && i.compareTo(intMax) <= 0);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#isEmpty()
     */
    public boolean isEmpty() {
        return min != null && max != null && min > max;
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
        BigInteger intBig = new BigInteger("" + integer); 
        if (IntegerIntervalFin.isLong(intBig)) {
            long longNum = integer.longValue();
            boolean contains = true;
            if (min != null) {
                contains = contains && (min <= longNum);
            }
            if (max != null) {
                contains = contains && (max >= longNum);
            }
            return contains;
        }
        if (min != null && max != null) return false;
        if (min != null) {
            return new BigInteger("" + min).compareTo(intBig) <= 0;
        }
        if (max != null) {
            return new BigInteger("" + max).compareTo(intBig) >= 0;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#getCardinality()
     */
    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max < min) return BigInteger.ZERO;
        return new BigInteger("" + max).subtract(new BigInteger("" + min)).add(BigInteger.ONE);
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
        if (min > Long.MIN_VALUE) {
            return min - 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.decreasedMin();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerInterval#increasedMax()
     */
    public Number increasedMax() {
        if (max < Long.MAX_VALUE) {
            return max + 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.increasedMax();
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
            buffer.append("<= " + max);
        }
        return buffer.toString();
    }
}