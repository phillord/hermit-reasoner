/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DataConstant.Impl;

public class DatatypeRestrictionFloat 
        extends DatatypeRestriction 
        implements FloatFacet, IntegerFacet {
    
    private static final long serialVersionUID = 6118099138621545215L;
    
    protected Set<FloatInterval> intervals = new HashSet<FloatInterval>();
    protected boolean hasExplicitMin = false; // excludes numeric specials
    protected boolean hasExplicitMax = false;
    
    /**
     * An implementation for floats. 
     * @param datatype A datatype (should use DT.FLOAT)
     */
    public DatatypeRestrictionFloat(DT datatype) {
        this.datatype = datatype;
        intervals.add(new FloatInterval());
        this.supportedFacets = new HashSet<Facet>(
                Arrays.asList(new Facet[] {
                        Facet.MIN_INCLUSIVE, 
                        Facet.MIN_EXCLUSIVE, 
                        Facet.MAX_INCLUSIVE, 
                        Facet.MAX_EXCLUSIVE
                })
        );
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionFloat(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return !isNegated;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facet facet, String value) {
        if (facet == Facet.MIN_EXCLUSIVE || facet == Facet.MIN_INCLUSIVE) {
            hasExplicitMin = true;
        } else {
            hasExplicitMax = true;
        }
        float floatValue;
        try {
            floatValue = Float.parseFloat(value);
            // if NaN is given as a facet, or min value is supposed to be +INF, 
            // or max value is supposed to be -INF, the value space is empty
            if (isNaN(floatValue) || (Float.POSITIVE_INFINITY == floatValue 
                    && (facet == Facet.MIN_EXCLUSIVE 
                            || facet == Facet.MIN_INCLUSIVE)) 
                            || (Float.NEGATIVE_INFINITY == floatValue 
                                    && (facet == Facet.MAX_EXCLUSIVE 
                                            || facet == Facet.MAX_INCLUSIVE))) {
                isBottom = true;
                return;
            } 
            // a min value of -INF or max value of +INF are not really 
            // restricting the value space, ignore
            if (Float.POSITIVE_INFINITY == floatValue 
                    || Float.NEGATIVE_INFINITY == floatValue) {
                return;
            }
            BigDecimal originalValue = new BigDecimal(value);
            BigDecimal floatValueAsBD = new BigDecimal("" + floatValue);
            if (facet == Facet.MIN_EXCLUSIVE 
                    && floatValueAsBD.compareTo(originalValue) <= 0) {
                floatValue = DatatypeRestrictionFloat.nextFloat(floatValue);
            } else if (facet == Facet.MAX_EXCLUSIVE 
                    && floatValueAsBD.compareTo(originalValue) >= 0) {
                floatValue = DatatypeRestrictionFloat.previousFloat(floatValue);
            }
        } catch (NumberFormatException e) {
            // ok, it wasn't a float, but maybe it is a very big/small integer  
            // or decimal or double, then we use the max/min for floats
            try {
                BigDecimal bd = new BigDecimal(value);
                if ((facet == Facet.MIN_INCLUSIVE && bd.compareTo(new BigDecimal("" + Float.MAX_VALUE)) > 0) 
                        || (facet == Facet.MIN_EXCLUSIVE && bd.compareTo(new BigDecimal("" + Float.MAX_VALUE)) >= 0)
                        || (facet == Facet.MAX_INCLUSIVE && bd.compareTo(new BigDecimal("" + -Float.MAX_VALUE)) < 0)
                        || (facet == Facet.MAX_EXCLUSIVE && bd.compareTo(new BigDecimal("" + -Float.MAX_VALUE)) <= 0)) {
                    // impossible, set all intervals to empty
                    intervals.clear();
                    intervals.add(new FloatInterval(1.0f, 0.0f));
                    isBottom = true;
                } // else holds anyways
                return;
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                return;
            }
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            for (FloatInterval i : intervals) {
                i.intersectWith(new FloatInterval(floatValue, Float.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            for (FloatInterval i : intervals) {
                i.intersectWith(new FloatInterval(floatValue, Float.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            for (FloatInterval i : intervals) {
                i.intersectWith(new FloatInterval(-Float.MAX_VALUE, floatValue));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            for (FloatInterval i : intervals) {
                i.intersectWith(new FloatInterval(-Float.MAX_VALUE, floatValue));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    
    /**
     * Returns a float such that it is greater than the given one and there is 
     * no float that is smaller than the returned one and greater than the 
     * given one. If the given one is -0.0, then the next greater one is +0.0. 
     * @param value a float
     * @return the next greater float compared to the given one or the value 
     * itself if it is NaN, +Infinity, or -Infinity
     */
    public static float nextFloat(float value) {
        int bits = Float.floatToRawIntBits(value);
        int magnitude = bits & 0x7fffffff;
        // NaN or +inf or -inf -> no successor
        if (DatatypeRestrictionFloat.isNaN(value) 
                || magnitude == 0x7f800000) {
            return value;
        } else {
            boolean positive = ((bits & 0x80000000) == 0);
            boolean newPositive;
            int newMagnitude;
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
            int newBits = newMagnitude | (newPositive ? 0 : 0x80000000);
            return Float.intBitsToFloat(newBits);
        }
    }
    
    /**
     * Returns a float such that it is smaller than the given one and there is 
     * no float that is greater than the returned one and smaller than the 
     * given one. If the given one is +0.0, then the next smaller one is -0.0. 
     * @param value a float
     * @return the next greater float compared to the given one or the value 
     * itself if it is NaN, +Infinity, or -Infinity
     */
    public static float previousFloat(float value) {
        int bits = Float.floatToRawIntBits(value);
        int magnitude = (bits & 0x7fffffff);
        // NaN or -inf or +inf -> no predeccessor
        if (DatatypeRestrictionFloat.isNaN(value) 
                || magnitude == 0x7f800000) {
            return value;
        } else {
            boolean negative = ((bits & 0x80000000) != 0);
            boolean newNegative;
            int newMagnitude;
            if (negative) {
                newNegative = true;
                newMagnitude = magnitude + 1;
            } else if (!negative && magnitude == 0) {
                // The predeccessor of +0 is -0
                newNegative = true;
                newMagnitude = 0;
            } else { // if (!negative && magnitude!=0)
                newNegative = false;
                newMagnitude = magnitude - 1;
            }
            int newBits = newMagnitude | (newNegative ? 0x80000000 : 0);
            return Float.intBitsToFloat(newBits);
        }
    }
    
    /**
     * Determines whether the given float is the special value NaN (not a 
     * number). 
     * @param value a float
     * @return true if the given value is NaN and false otherwise
     */
    public static boolean isNaN(float value) {
        int bits = Float.floatToRawIntBits(value);
        boolean result = (bits & 0x7f800000) == 0x7f800000;
        result = result && (bits & 0x003fffff) != 0;
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        if (!(constant.getImplementation() == Impl.IInteger 
                || constant.getImplementation() == Impl.IFloat
                || constant.getImplementation() == Impl.IDouble
                || constant.getImplementation() == Impl.IDecimal)) {
            return false;
        }
        String value = constant.getValue();
        float floatValue = Float.parseFloat(value);
        if (floatValue == Float.POSITIVE_INFINITY) {
            return hasExplicitMax ? false : true; 
        }
        if (floatValue == Float.NEGATIVE_INFINITY) {
            return hasExplicitMin ? false : true; 
        }
        if (isNaN(floatValue)) {
            return (hasExplicitMax || hasExplicitMin) ? false : true; 
        }
        if (constant.getImplementation() != Impl.IFloat) {
            BigDecimal dbd = new BigDecimal(value);
            if (dbd.compareTo(new BigDecimal("" + -Float.MAX_VALUE)) <= 0 
                    || dbd.compareTo(new BigDecimal("" + Float.MAX_VALUE)) >= 0) {
                return false;
            } 
            value = "" + dbd.floatValue();
        }
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        for (FloatInterval i : intervals) {
            if (i.contains(floatValue)) {
                return true;
            }
        }
        return false; 
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.dataranges.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof FloatFacet)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of FloatFacet. It is " +
                    "only allowed to add facets from other float facets. ");
        }
        if (!isBottom()) {
            FloatFacet restr = (FloatFacet) range;
            if (restr.getFloatIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (restr.isNegated()) {
                hasExplicitMax = hasExplicitMax || restr.hasExplicitMin();
                hasExplicitMin = hasExplicitMin || restr.hasExplicitMax();
            } else {
                hasExplicitMax = hasExplicitMax || restr.hasExplicitMax();
                hasExplicitMin = hasExplicitMin || restr.hasExplicitMin();
            }
            if (intervals.isEmpty()) {
                for (FloatInterval i : restr.getFloatIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() > -Float.MAX_VALUE) {
                                intervals.add(new FloatInterval(-Float.MAX_VALUE, i.getMin()));
                            }
                            if (i.getMax() < Float.MAX_VALUE) {
                                intervals.add(new FloatInterval(i.getMax(), Float.MAX_VALUE));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getFloatIntervals();
                    }
                }
            } else {
                Set<FloatInterval> newIntervals = new HashSet<FloatInterval>();
                if (restr.isNegated()) {
                    for (FloatInterval i1 : intervals) {
                        for (FloatInterval i2 : restr.getFloatIntervals()) {
                            if (!i2.isEmpty()) {
                                if (i2.getMin() > -Float.MAX_VALUE) {
                                    FloatInterval newInterval = i1.getCopy();
                                    float newMin = DatatypeRestrictionFloat.previousFloat(i2.getMin());
                                    newInterval.intersectWith(new FloatInterval(-Float.MAX_VALUE, newMin));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (i2.getMax() < Float.MAX_VALUE) {
                                    FloatInterval newInterval = i1.getCopy();
                                    float newMax = DatatypeRestrictionFloat.nextFloat(i2.getMax()); 
                                    newInterval.intersectWith(new FloatInterval(newMax, Float.MAX_VALUE));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                }
                            } else {
                                newIntervals.add(i1);
                            }
                        }
                    }
                } else {
                    if (restr.getFloatIntervals().isEmpty()) {
                        newIntervals = intervals;
                    } else {
                        for (FloatInterval i1 : intervals) {
                            for (FloatInterval i2 : restr.getFloatIntervals()) {
                                i1.intersectWith(i2);
                                if (!i1.isEmpty()) newIntervals.add(i1);
                            }
                        }
                    }
                }
                if (newIntervals.isEmpty()) {
                    isBottom = true;
                } else {
                    intervals = newIntervals;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#hasMinCardinality(java.math.BigInteger)
     */
    public boolean hasMinCardinality(BigInteger n) {
        if (isNegated || n.compareTo(BigInteger.ZERO) <= 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (n.compareTo(new BigInteger("" + oneOf.size())) >= 0);
            }
            BigInteger rangeSize = BigInteger.ZERO;
            // +INF
            if (!hasExplicitMax) rangeSize = rangeSize.add(BigInteger.ONE);
            // -INF
            if (!hasExplicitMin) rangeSize = rangeSize.add(BigInteger.ONE);
            // NaN
            if (!hasExplicitMax && !hasExplicitMin) {
                rangeSize = rangeSize.add(BigInteger.ONE);
            }
            for (FloatInterval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                float not = Float.parseFloat(constant.getValue());
                for (FloatInterval i : intervals) {
                    if (i.contains(not) 
                            || (DatatypeRestrictionFloat.isNaN(not) && !hasExplicitMax && !hasExplicitMin) 
                            || (Float.POSITIVE_INFINITY == not && !hasExplicitMax) 
                            || (Float.NEGATIVE_INFINITY == not && !hasExplicitMin)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return (rangeSize.compareTo(new BigInteger("" + n)) >= 0);
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() {
        if (isBottom) return BigInteger.ZERO;
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        }
        BigInteger rangeSize = BigInteger.ZERO;
        for (FloatInterval i : intervals) {
            rangeSize = rangeSize.add(i.getCardinality());
        }
        // plus NaN, +Inf, -Inf
        int numSpecials = 3;
        if (hasExplicitMax) numSpecials--; // +Inf impossible
        if (hasExplicitMin) numSpecials--; // -Inf impossible
        if (hasExplicitMin || hasExplicitMax) numSpecials--; // NaN impossible
        rangeSize = rangeSize.add(new BigInteger("" + numSpecials));
        for (DataConstant constant : notOneOf) {
            float not = Float.parseFloat(constant.getValue());
            for (FloatInterval i : intervals) {
                if (i.contains(not) 
                        || (DatatypeRestrictionFloat.isNaN(not) && !hasExplicitMax && !hasExplicitMin) 
                        || (Float.POSITIVE_INFINITY == not && !hasExplicitMax) 
                        || (Float.NEGATIVE_INFINITY == not && !hasExplicitMin)) {
                    rangeSize = rangeSize.subtract(BigInteger.ONE);
                }
            }
        }
        return rangeSize;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getSmallestAssignment()
     */
    public DataConstant getSmallestAssignment() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                return sortedOneOfs.first();
            }
            DataConstant dataConstant = new DataConstant(Impl.IFloat, datatype, "Infinity");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMax) {
                return dataConstant;
            }
            dataConstant = new DataConstant(Impl.IFloat, datatype, "-Infinity");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMin) {
                return dataConstant;
            }
            dataConstant = new DataConstant(Impl.IFloat, datatype, "NaN");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMax && !hasExplicitMin) {
                return dataConstant;
            }
            SortedSet<FloatInterval> sortedIntervals = new TreeSet<FloatInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (FloatInterval i : sortedIntervals) {
                float constant = i.getMin();
                while (constant <= i.getMax()) {
                    dataConstant = new DataConstant(Impl.IFloat, datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = DatatypeRestrictionFloat.nextFloat(constant); 
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.FloatFacet#getFloatIntervals()
     */
    public Set<FloatInterval> getFloatIntervals() {
        return intervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.FloatFacet#hasExplicitMin()
     */
    public boolean hasExplicitMin() {
        return hasExplicitMin;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.FloatFacet#hasExplicitMax()
     */
    public boolean hasExplicitMax() {
        return hasExplicitMax;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerFacet#getIntegerIntervals()
     */
    public Set<IntegerInterval> getIntegerIntervals() {
        Set<IntegerInterval> integerIntervals = new HashSet<IntegerInterval>();
        if (!intervals.isEmpty()) {
            for (FloatInterval i : intervals) {
                IntegerInterval iInteger;
                Float min = i.getMin();
                Float max = i.getMax();
                BigInteger minBig = new BigDecimal("" + Math.ceil(min)).toBigInteger();
                BigInteger maxBig = new BigDecimal("" + Math.floor(max)).toBigInteger();
                if (IntegerIntervalFin.isLong(minBig) 
                        && IntegerIntervalFin.isLong(maxBig)) {
                    long minLong = minBig.longValue();
                    long maxLong = maxBig.longValue();
                    iInteger = new IntegerIntervalFin(minLong, maxLong);                    
                } else {
                    iInteger = new IntegerIntervalBig(minBig, maxBig);
                }
                if (!iInteger.isEmpty()) {
                    integerIntervals.add(iInteger);
                }
            }
        }
        return integerIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Prefixes)
     */
    protected String printExtraInfo(Prefixes prefixes) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (FloatInterval i : intervals) {
            if (!firstRun && !isNegated) {
                buffer.append(" or ");
            }
            if (isNegated) buffer.append(" or ");
            buffer.append(" >= " + i.getMin());
            if (isNegated) buffer.append(" or ");
            buffer.append(" <= " + i.getMax());
            firstRun = false;
        }
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return constant.getImplementation() == Impl.IFloat
                || constant.getImplementation() == Impl.IInteger;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.OWLREALPLUS).contains(datatype);
    }
    
    /**
     * A comparator that can be used to order the intervals according to their 
     * min values. We assume here that all intervals are disjoint. 
     * @author BGlimm
     */
    protected static class IntervalComparator implements Comparator<FloatInterval> { 
        public static Comparator<FloatInterval> INSTANCE = new IntervalComparator();
        public int compare(FloatInterval i1, FloatInterval i2) {
            if (i1.getMin() == i2.getMin()) return 0;
            return (i1.getMin() - i2.getMin() > 0) ? 1 : -1;
        }
    }
}
