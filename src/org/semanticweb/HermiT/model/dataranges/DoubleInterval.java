package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DoubleInterval {
    
    public static final BigDecimal doubleMin = new BigDecimal("" + Double.MIN_VALUE);
    public static final BigDecimal doubleMax = new BigDecimal("" + Double.MAX_VALUE);
    
    double min = -Double.MAX_VALUE;
    double max = Double.MAX_VALUE;
    
    public DoubleInterval() {
        super();
    }
    
    public DoubleInterval(double minInclusive, double maxInclusive) {
        this.min = minInclusive;
        this.max = maxInclusive;
    }
    
    public DoubleInterval getCopy() {
        return new DoubleInterval(min, max);
    }
    
    public static boolean isDouble(BigDecimal bigDecimal) {
        return (bigDecimal.compareTo(doubleMin) >= 0 && bigDecimal.compareTo(doubleMax) <= 0);
    }
    
    public void intersectWith(DoubleInterval i) {
        if (i.getMax() < max || (isMinusZero(i.getMax()) && isPlusZero(max))) {
            max = i.getMax();
        }
        if (i.getMin() > min || (isPlusZero(i.getMin()) && isMinusZero(min))) {
            min = i.getMin();
        }
    }
    
    public boolean isEmpty() {
        return (min > max);
    }
    
    public boolean isFinite() {
        return true;
    }
    
    public boolean isMinusZero(double value) {
        if (value != 0.0) return false;
        long bits = Double.doubleToRawLongBits(value);
        return ((bits & 0x7fffffffffffffffl) != bits);
    }
    
    public boolean isPlusZero(double value) {
        if (value != 0.0) return false;
        long bits = Double.doubleToRawLongBits(value);
        return ((bits & 0x7fffffffffffffffl) == bits);
    }
    
    public boolean contains(double d) {
        if (isPlusZero(min) && isMinusZero(d)) return false;
        if (isMinusZero(max) && isPlusZero(d)) return false;
        return (min <= d) && (max >= d);
    }

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
    
    public boolean increaseMin() {
        long bits = Double.doubleToRawLongBits(min);
        long magnitude = (bits & 0x7fffffffffffffffl);
        // NaN or +inf -> no successor
        if (DatatypeRestrictionDouble.isNaN(min) 
                || magnitude == 0x7f80000000000000l) {
            return false;
        } else {
            boolean positive = ((bits & 0x8000000000000000l) == 0);
            boolean newPositive;
            long newMagnitude;
            if (positive) {
                newPositive = true;
                newMagnitude = magnitude + 1;
            } else if (!positive && magnitude == 0) {
                // The successor of -0 is +0
                newPositive = true;
                newMagnitude = 0;
            } else { // if (!positive && magnitude!=0)
                newPositive = false;
                newMagnitude = magnitude - 1;
            }
            long newBits = newMagnitude | (newPositive ? 0 : 0x8000000000000000l);
            double oldMin = min;
            min = Double.longBitsToDouble(newBits);
            return min != oldMin;
        }
    }
    
    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(">= " + min);
        buffer.append(" ");
        buffer.append("<= " + max);
        return buffer.toString();
    }
}