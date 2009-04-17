// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.owlreal;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents rational numbers. The denominator is always a positive integer.
 * The rational is usually reduced -- that is, gcd(m_numerator,m_denominator)==1.
 * Furthermore, m_denominator is usually greated than one.
 * These restrictions are used in .equals(). Sometimes, however,
 * rationals are created that do not satisfy this condition. In such cases,
 * .equals() and .hasCode() may return incorrect values. This hack has been introduced
 * to enable efficient comparison of rationals with other kinds of numbers in the
 * Numbers class. The compareTo() method always returns correct values.
 */
public class BigRational extends Number implements Comparable<BigRational> {
    private static final long serialVersionUID=3883936594384307950L;

    private final BigInteger m_numerator;
    private final BigInteger m_denominator;

    public BigRational(BigInteger numerator,BigInteger denominator) {
        m_numerator=numerator;
        m_denominator=denominator;
    }
    public BigInteger getNumerator() {
        return m_numerator;
    }
    public BigInteger getDenominator() {
        return m_denominator;
    }
    public boolean isFinitelyRepresentable() {
        try {
            new BigDecimal(m_numerator).divide(new BigDecimal(m_denominator)).doubleValue();
            return true;
        }
        catch (ArithmeticException e) {
            return false;
        }
    }
    public int compareTo(BigRational that) {
        return m_numerator.multiply(that.m_denominator).compareTo(m_denominator.multiply(that.m_numerator));
    }
    public boolean equals(Object that) {
        if (that==this)
            return true;
        if (!(that instanceof BigRational) || that==null)
            return false;
        BigRational thatRational=(BigRational)that;
        return m_numerator.equals(thatRational.m_numerator) && m_denominator.equals(thatRational.m_denominator);
    }
    public int hashCode() {
        return m_numerator.hashCode()*3+m_denominator.hashCode();
    }
    public String toString() {
        return m_numerator.toString()+"/"+m_denominator.toString();
    }
    public double doubleValue() {
        return m_numerator.divide(m_denominator).doubleValue();
    }
    public float floatValue() {
        return m_numerator.divide(m_denominator).floatValue();
    }
    public int intValue() {
        return m_numerator.divide(m_denominator).intValue();
    }
    public long longValue() {
        return m_numerator.divide(m_denominator).longValue();
    }
}
