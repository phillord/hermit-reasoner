package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;

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
    
    public static BigRational parseRational(String s) {
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

    public BigDecimal bigDecimalValue() throws ArithmeticException {
        return (new BigDecimal(num)).divide(new BigDecimal(den));
    }
    
    public double doubleValue() {
        return (new BigDecimal(num)).divide(new BigDecimal(den)).doubleValue();
    }

    public float floatValue() {
        return (new BigDecimal(num)).divide(new BigDecimal(den)).floatValue();
    }

    public int intValue() {
        return new BigDecimal(num).divide(new BigDecimal(den)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public long longValue() {
        return new BigDecimal(num).divide(new BigDecimal(den)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
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
