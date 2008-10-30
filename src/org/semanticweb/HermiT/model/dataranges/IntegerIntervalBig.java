package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

public class IntegerIntervalBig implements IntegerInterval {
    
    public static final Number ZERO = BigInteger.ZERO;
    
    protected BigInteger min = null;
    protected BigInteger max = null;
    
    public IntegerIntervalBig(BigInteger minInclusive, BigInteger maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    public IntegerInterval getCopy() {
        return new IntegerIntervalBig(min, max);
    }
    
    public IntegerInterval getInstance(Number min, Number max) {
        BigInteger minBig = min == null ? null : new BigInteger("" + min);
        BigInteger maxBig = max == null ? null : new BigInteger("" + max);
        return new IntegerIntervalBig(minBig, maxBig);
    }
    
    public static IntegerIntervalBig toIntegerIntervalBig(IntegerInterval interval) {
        if (interval instanceof IntegerIntervalBig) {
            return (IntegerIntervalBig) interval;
        } else {
            return new IntegerIntervalBig(
                    new BigInteger("" + interval.getMin()), 
                    new BigInteger("" + interval.getMax()));
        }
    }
    
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
    
    public boolean isEmpty() {
        return (min != null 
                && max != null 
                && min.compareTo(max) > 0);
    }
    
    public boolean isFinite() {
        return min != null && max != null;
    }
    
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
    
    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max.compareTo(min) < 0) return BigInteger.ZERO;
        return max.subtract(min).add(BigInteger.ONE);
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }
    
    public Number increasedMin() {
        return min.add(BigInteger.ONE);
    }
    
    public Number decreasedMin() {
        return min.subtract(BigInteger.ONE);
    }
    
    public Number increasedMax() {
        return max.add(BigInteger.ONE);
    }
    
    public Number decreasedMax() {
        return  max.subtract(BigInteger.ONE);
    }
    
    public Number getZero() {
        return ZERO;
    }
    
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
