package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Date;

public class DateTimeInterval {
    
    public static final BigInteger longMin = new BigInteger("" + Long.MIN_VALUE);
    public static final BigInteger longMax = new BigInteger("" + Long.MAX_VALUE);
    
    Long min = null;
    Long max = null;
    
    public DateTimeInterval(Long min, Long max) {
        this.min = min;
        this.max = max;
    }
    
    public DateTimeInterval getCopy() {
        return new DateTimeInterval(min, max);
    }
    
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
    
    public static boolean isLong(BigInteger i) {
        return (i.compareTo(longMin) >= 0 && i.compareTo(longMax) <= 0);
    }
    
    public boolean isEmpty() {
        return min != null && max != null && min > max;
    }
    
    public boolean isFinite() {
        return min != null && max != null;
    }
    
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
    
    public BigInteger getCardinality() {
        if (min == null || max == null) return null;
        if (max < min) return BigInteger.ZERO;
        return new BigInteger("" + max).subtract(new BigInteger("" + min)).add(BigInteger.ONE);
    }

    public Long getMin() {
        return min;
    }

    public Long getMax() {
        return max;
    }

    public static Long increase(Long longValue) {
        if (longValue < Long.MAX_VALUE) {
            return longValue + 1;
        } 
        return null;
    }
    
    public static Long decrease(Long longValue) {
        if (longValue > Long.MIN_VALUE) {
            return longValue - 1;
        } 
        return null;
    }
    
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
