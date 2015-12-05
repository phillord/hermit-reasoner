/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.datatypes.owlreal;

import java.util.Collection;

public class NumberInterval {
    protected final NumberRange m_baseRange;
    protected final NumberRange m_excludedRange;
    protected final Number m_lowerBound;
    protected final BoundType m_lowerBoundType;
    protected final Number m_upperBound;
    protected final BoundType m_upperBoundType;
    
    public NumberInterval(NumberRange baseRange,NumberRange excludedRange,Number lowerBound,BoundType lowerBoundType,Number upperBound,BoundType upperBoundType) {
        assert !isIntervalEmpty(baseRange,excludedRange,lowerBound,lowerBoundType,upperBound,upperBoundType);
        m_baseRange=baseRange;
        m_excludedRange=excludedRange;
        if (m_baseRange==NumberRange.INTEGER) {
            // For efficiency, adjust the end-points so that they fit into the INTEGER range.
            if (MinusInfinity.INSTANCE.equals(lowerBound)) {
                m_lowerBound=lowerBound;
                m_lowerBoundType=lowerBoundType;
            }
            else {
                m_lowerBound=Numbers.getNearestIntegerInBound(lowerBound,Numbers.BoundaryDirection.LOWER,lowerBoundType==BoundType.INCLUSIVE);
                m_lowerBoundType=BoundType.INCLUSIVE;
            }
            if (PlusInfinity.INSTANCE.equals(upperBound)) {
                m_upperBound=upperBound;
                m_upperBoundType=upperBoundType;
            }
            else {
                m_upperBound=Numbers.getNearestIntegerInBound(upperBound,Numbers.BoundaryDirection.UPPER,upperBoundType==BoundType.INCLUSIVE);
                m_upperBoundType=BoundType.INCLUSIVE;
            }
        }
        else {
            m_lowerBound=lowerBound;
            m_lowerBoundType=lowerBoundType;
            m_upperBound=upperBound;
            m_upperBoundType=upperBoundType;
        }
    }
    /**
     * Computes the intersection of this interval with the supplied one. If the two intervals
     * do not intersect, the result is null.
     */
    public NumberInterval intersectWith(NumberInterval that) {
        NumberRange newBaseRange=NumberRange.intersection(m_baseRange,that.m_baseRange);
        NumberRange newExcludedRange=NumberRange.union(m_excludedRange,that.m_excludedRange);
        if (NumberRange.isSubsetOf(newBaseRange,newExcludedRange))
            return null;
        Number newLowerBound;
        BoundType newLowerBoundType;
        int lowerBoundComparison=Numbers.compare(m_lowerBound,that.m_lowerBound);
        if (lowerBoundComparison<0) {
            newLowerBound=that.m_lowerBound;
            newLowerBoundType=that.m_lowerBoundType;
        }
        else if (lowerBoundComparison>0) {
            newLowerBound=m_lowerBound;
            newLowerBoundType=m_lowerBoundType;
        }
        else {
            newLowerBound=m_lowerBound;
            newLowerBoundType=BoundType.getMoreRestrictive(m_lowerBoundType,that.m_lowerBoundType);
        }
        Number newUpperBound;
        BoundType newUpperBoundType;
        int upperBoundComparison=Numbers.compare(m_upperBound,that.m_upperBound);
        if (upperBoundComparison<0) {
            newUpperBound=m_upperBound;
            newUpperBoundType=m_upperBoundType;
        }
        else if (upperBoundComparison>0) {
            newUpperBound=that.m_upperBound;
            newUpperBoundType=that.m_upperBoundType;
        }
        else {
            newUpperBound=m_upperBound;
            newUpperBoundType=BoundType.getMoreRestrictive(m_upperBoundType,that.m_upperBoundType);
        }
        if (isIntervalEmpty(newBaseRange,newExcludedRange,newLowerBound,newLowerBoundType,newUpperBound,newUpperBoundType))
            return null;
        // The following lines ensure that we don't create a new interval object unless there is need to.
        // WARNING: The static initializer in OWLRealDatatypeHandler depends on this!
        if (isEqual(newBaseRange,newExcludedRange,newLowerBound,newLowerBoundType,newUpperBound,newUpperBoundType))
            return this;
        else if (that.isEqual(newBaseRange,newExcludedRange,newLowerBound,newLowerBoundType,newUpperBound,newUpperBoundType))
            return that;
        else
            return new NumberInterval(newBaseRange,newExcludedRange,newLowerBound,newLowerBoundType,newUpperBound,newUpperBoundType);
    }
    protected boolean isEqual(NumberRange baseRange,NumberRange excludedRange,Number lowerBound,BoundType lowerBoundType,Number upperBound,BoundType upperBoundType) {
        return m_baseRange.equals(baseRange) && m_excludedRange.equals(excludedRange) && m_lowerBound.equals(lowerBound) && m_lowerBoundType.equals(lowerBoundType) && m_upperBound.equals(upperBound) && m_upperBoundType.equals(upperBoundType);
    }
    /**
     * Subtracts from the given argument the number of distinct objects that are contained in this interval.
     * If the interval contains more objects than argument, the result is zero.
     */
    public int subtractSizeFrom(int argument) {
        if (argument<=0)
            return 0;
        else if (m_lowerBound.equals(m_upperBound)) {
            // The interval is not empty, and we know that it is not empty; hence, it is a singleton.
            return argument-1;
        }
        else {
            // The lower bound must be smaller than the upper bound because the interval is not empty.
            // If the base range is dense, since the excluded range is a proper subset of the base range,
            // there are infinitely many numbers in the interval.
            if (m_baseRange.isDense())
                return 0;
            // The base range is INTEGER; since the excluded range is a proper subset, it must be NOTHING.
            if (MinusInfinity.INSTANCE.equals(m_lowerBound) || PlusInfinity.INSTANCE.equals(m_upperBound))
                return 0;
            // In the constructor, the lower and upper bounds are adjusted to the first integer in the range.
            // Hence, we just subtract the bounds.
            return Numbers.subtractIntegerIntervalSizeFrom(m_lowerBound,m_upperBound,argument);
        }
    }
    public boolean containsNumber(Number number) {
        NumberRange mostSpecificRange=NumberRange.getMostSpecificRange(number);
        if (!NumberRange.isSubsetOf(mostSpecificRange,m_baseRange) || NumberRange.isSubsetOf(mostSpecificRange,m_excludedRange))
            return false;
        int lowerBoundComparison=Numbers.compare(m_lowerBound,number);
        if (lowerBoundComparison>0 || (lowerBoundComparison==0 && m_lowerBoundType==BoundType.EXCLUSIVE))
            return false;
        int upperBoundComparison=Numbers.compare(m_upperBound,number);
        if (upperBoundComparison<0 || (upperBoundComparison==0 && m_upperBoundType==BoundType.EXCLUSIVE))
            return false;
        return true;
    }
    public void enumerateNumbers(Collection<Object> numbers) {
        if (m_lowerBound.equals(m_upperBound)) {
            // The interval is not empty, and we know that it is not empty; hence, it is a singleton.
            numbers.add(m_lowerBound);
        }
        else {
            // The lower bound must be smaller than the upper bound because the interval is not empty.
            // If the base range is dense, since the excluded range is a proper subset of the base range,
            // there are infinitely many numbers in the interval.
            if (m_baseRange.isDense())
                throw new IllegalStateException("The data range is infinite.");
            // The base range is INTEGER; since the excluded range is a proper subset, it must be NOTHING.
            if (MinusInfinity.INSTANCE.equals(m_lowerBound) || PlusInfinity.INSTANCE.equals(m_upperBound))
                throw new IllegalStateException("The data range is infinite.");
            // In the constructor, the lower and upper bounds are adjusted to the first integer in the range.
            // Hence, we just go through all the integers in the range.
            Number integer=m_lowerBound;
            while (!integer.equals(m_upperBound)) {
                numbers.add(integer);
                integer=Numbers.nextInteger(integer);
            }
            numbers.add(m_upperBound);
        }
    }
    protected static boolean isIntervalEmpty(NumberRange baseRange,NumberRange excludedRange,Number lowerBound,BoundType lowerBoundType,Number upperBound,BoundType upperBoundType) {
        if (NumberRange.isSubsetOf(baseRange,excludedRange))
            return true;
        int boundComparison=Numbers.compare(lowerBound,upperBound);
        if (boundComparison>0)
            return true;
        else if (boundComparison==0) {
            if (lowerBoundType==BoundType.EXCLUSIVE || upperBoundType==BoundType.EXCLUSIVE || MinusInfinity.INSTANCE.equals(lowerBound) || PlusInfinity.INSTANCE.equals(lowerBound))
                return true;
            // Both end-points are INCLUSIVE, so the cardinality is at most one
            NumberRange mostSpecificRange=NumberRange.getMostSpecificRange(lowerBound);
            return !NumberRange.isSubsetOf(mostSpecificRange,baseRange) || NumberRange.isSubsetOf(mostSpecificRange,excludedRange);
        }
        else {
            // Lower bound is smaller than the upper bound.
            // If the base range is dense, since the excluded range is a proper subset of the base range
            // there are infinitely many numbers in the interval.
            if (baseRange.isDense())
                return false;
            // The base range is INTEGER; since the excluded range is a proper subset, it must be NOTHING.
            if (MinusInfinity.INSTANCE.equals(lowerBound) || PlusInfinity.INSTANCE.equals(upperBound))
                return false;
            Number lowerBoundInclusive=Numbers.getNearestIntegerInBound(lowerBound,Numbers.BoundaryDirection.LOWER,lowerBoundType==BoundType.INCLUSIVE);
            Number upperBoundInclusive=Numbers.getNearestIntegerInBound(upperBound,Numbers.BoundaryDirection.UPPER,upperBoundType==BoundType.INCLUSIVE);
            return Numbers.compare(lowerBoundInclusive,upperBoundInclusive)>0;
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append(m_baseRange.toString());
        if (m_excludedRange!=NumberRange.NOTHING) {
            buffer.append('\\');
            buffer.append(m_excludedRange.toString());
        }
        if (m_lowerBoundType==BoundType.INCLUSIVE)
            buffer.append('[');
        else
            buffer.append('<');
        buffer.append(m_lowerBound.toString());
        buffer.append(" .. ");
        buffer.append(m_upperBound.toString());
        if (m_upperBoundType==BoundType.INCLUSIVE)
            buffer.append(']');
        else
            buffer.append('>');
        return buffer.toString();
    }
}
