package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

public class IntegerIntervalFin implements IntegerInterval {
    
    public static final BigInteger longMin = new BigInteger("" + Long.MIN_VALUE);
    public static final BigInteger longMax = new BigInteger("" + Long.MAX_VALUE);
    public static final BigInteger intMin = new BigInteger("" + Integer.MIN_VALUE);
    public static final BigInteger intMax = new BigInteger("" + Integer.MAX_VALUE);
    
    public static final Number ZERO = 0l;
    
    Long min = null;
    Long max = null;
    
    public IntegerIntervalFin(Long minInclusive, Long maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    public IntegerInterval getCopy() {
        return new IntegerIntervalFin(min, max);
    }
    
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
    
    public static boolean isLong(BigInteger i) {
        return (i.compareTo(longMin) >= 0 && i.compareTo(longMax) <= 0);
    }

    public static boolean isInt(BigInteger i) {
        return (i.compareTo(intMin) >= 0 && i.compareTo(intMax) <= 0);
    }
    
    public boolean isEmpty() {
        return min != null && max != null && min > max;
    }
    
    public boolean isFinite() {
        return min != null && max != null;
    }
    
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

    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max < min) return BigInteger.ZERO;
        return new BigInteger("" + max).subtract(new BigInteger("" + min)).add(BigInteger.ONE);
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }
    
    public Number increasedMin() {
        if (min < Long.MAX_VALUE) {
            return min + 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.increasedMin();
    }
    
    public Number decreasedMin() {
        if (min > Long.MIN_VALUE) {
            return min - 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.decreasedMin();
    }
    
    public Number increasedMax() {
        if (max < Long.MAX_VALUE) {
            return max + 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.increasedMin();
    }
    
    public Number decreasedMax() {
        if (max > Long.MIN_VALUE) {
            return max - 1;
        }
        IntegerInterval i = IntegerIntervalBig.toIntegerIntervalBig(this);
        return i.decreasedMax();
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