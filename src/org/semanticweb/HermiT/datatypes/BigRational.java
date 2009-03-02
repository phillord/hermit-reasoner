package org.semanticweb.HermiT.datatypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/*************************************************************************
 *  Invariants
 *  gcd(num, den) = 1, i.e., rational number is in reduced form
 *  den >= 1, i.e., the denominator is always a positive integer
 *************************************************************************/

public class BigRational extends Number implements Comparable<BigRational> {

    private static final long serialVersionUID = 3883936594384307950L;

    private BigInteger num;
    private BigInteger den;

    public BigRational(BigInteger numerator, BigInteger denominator) {
        // deal with x / 0
        if (denominator.equals(BigInteger.ZERO)) {
           throw new RuntimeException("Denominator is zero");
        }

        // reduce fraction
        BigInteger g = numerator.gcd(denominator);
        num = numerator.divide(g);
        den = denominator.divide(g);

        // to ensure invariant that denominator is positive
        if (den.compareTo(BigInteger.ZERO) < 0) {
            den = den.negate();
            num = num.negate();
        }
    }
    
    public static BigRational convertToRational(BigDecimal decimal) {
        BigInteger wholePart = decimal.toBigInteger();
        BigDecimal fractionPart = decimal.subtract(new BigDecimal("" + wholePart));
        BigInteger denominator = new BigInteger("10").pow(fractionPart.scale());
        BigInteger numerator = wholePart.multiply(denominator);
        numerator = numerator.add(fractionPart.scaleByPowerOfTen(fractionPart.scale()).toBigInteger());
        return new BigRational(numerator, denominator);
    }
    
    /**
     * Parses a rational from a String
     * @param s a String represenation of the rational (e.g., 1/3)
     * @return the corresponding Rational object
     * @throws NumberFormatException if s cannot be parsed into a rational
     */
    public static BigRational parseRational(String s) throws NumberFormatException {
        String[] tokens = s.split("/");
        if (tokens.length == 2) {
            return new BigRational(new BigInteger(tokens[0]), new BigInteger(tokens[1]));
        } else if (tokens.length == 1) {
            return new BigRational(new BigInteger(tokens[0]), BigInteger.ONE);
        } else {
            throw new NumberFormatException("The given string cannot be parsed into a BigRational. ");
        }
    }
    
    public boolean isFinitelyRepresentable() {
        try {
            new BigDecimal(num).divide(new BigDecimal(den)).doubleValue();
            return true;
        } catch (ArithmeticException e) {
            return false;
        }
    }
    
    public BigRational negate() {
        return new BigRational(num.negate(), den);
    }

    public int compareTo(BigRational r) {
        return num.multiply(r.getDenominator()).compareTo(den.multiply(r.getNumerator()));
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BigRational) || o == null) return false;
        return compareTo((BigRational)o) == 0;
    }
    
    public int hashCode() {
        return this.toString().hashCode();
    }

    public BigDecimal bigDecimalValueExact() throws ArithmeticException {
        return (new BigDecimal(num)).divide(new BigDecimal(den));
    }
    
    public BigDecimal bigDecimalValue(RoundingMode roundingMode) {
        return (new BigDecimal(num)).divide(new BigDecimal(den), roundingMode);
    }
    
    public double doubleValue() {
        return (new BigDecimal(num)).divide(new BigDecimal(den)).doubleValue();
    }

    public double doubleValue(int roundingMode) {
        return (new BigDecimal(num)).divide(new BigDecimal(den), roundingMode).doubleValue();
    }
    
    public float floatValue() {
        return (new BigDecimal(num)).divide(new BigDecimal(den)).floatValue();
    }

    public int intValue() {
        return new BigDecimal(num).divide(new BigDecimal(den)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public int intValue(int roundingMode) {
        return new BigDecimal(num).divide(new BigDecimal(den), roundingMode).setScale(0, roundingMode).intValue();
    }
    
    public BigInteger integerValue() {
        return new BigDecimal(num).divide(new BigDecimal(den)).setScale(0, BigDecimal.ROUND_HALF_UP).toBigIntegerExact();
    }

    public BigInteger integerValue(int roundingMode) {
        return new BigDecimal(num).divide(new BigDecimal(den), roundingMode).setScale(0, roundingMode).toBigInteger();
    }
    
    public long longValue() {
        return new BigDecimal(num).divide(new BigDecimal(den)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    public long longValue(int roundingMode) {
        return new BigDecimal(num).divide(new BigDecimal(den), roundingMode).setScale(0, roundingMode).longValue();
    }
    
    public BigInteger getNumerator()   { 
        return num; 
    }
    
    public BigInteger getDenominator() { 
        return den; 
    }
    
    public String toString() { 
        if (den.equals(BigInteger.ONE)) {
            return num + "";
        } else {
            return num + "/" + den;
        }
    }
}
