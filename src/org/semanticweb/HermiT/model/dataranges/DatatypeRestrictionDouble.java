/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;

public class DatatypeRestrictionDouble 
        extends DatatypeRestriction 
        implements DoubleFacet, FloatFacet, IntegerFacet {
    
    private static final long serialVersionUID = 6118099138621545215L;
    
    protected Set<DoubleInterval> intervals = new HashSet<DoubleInterval>();
    protected boolean hasExplicitMin = false; // excludes numeric specials
    protected boolean hasExplicitMax = false;
    
    /**
     * An implementation for doubles and floats. 
     * @param datatype A datatype (should use DT.DOUBLE or DT.FLOAT)
     */
    public DatatypeRestrictionDouble(DT datatype) {
        this.datatype = datatype;
        intervals.add(new DoubleInterval());
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
        return new DatatypeRestrictionDouble(this.datatype);
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
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(value);
            BigDecimal originalValue = new BigDecimal(value);
            BigDecimal doubleValueAsBD = new BigDecimal("" + doubleValue);
            if (facet == Facet.MIN_EXCLUSIVE 
                    && doubleValueAsBD.compareTo(originalValue) <= 0) {
                doubleValue = DatatypeRestrictionDouble.nextDouble(doubleValue);
            } else if (facet == Facet.MAX_EXCLUSIVE 
                    && doubleValueAsBD.compareTo(originalValue) >= 0) {
                doubleValue = DatatypeRestrictionDouble.previousDouble(doubleValue);
            }
        } catch (NumberFormatException e) {
            // it wasn't a double, so may be it was a very big/small integer  
            // or decimal, then we use the max/min for doubles
            try {
                BigDecimal bd = new BigDecimal(value);
                if ((facet == Facet.MIN_INCLUSIVE 
                        && bd.compareTo(new BigDecimal("" + Double.MAX_VALUE)) > 0) 
                        || (facet == Facet.MIN_EXCLUSIVE 
                        && bd.compareTo(new BigDecimal("" + Double.MAX_VALUE)) >= 0)
                        || (facet == Facet.MAX_INCLUSIVE 
                        && bd.compareTo(new BigDecimal("" + -Double.MAX_VALUE)) < 0)
                        || (facet == Facet.MAX_EXCLUSIVE 
                        && bd.compareTo(new BigDecimal("" + -Double.MAX_VALUE)) <= 0)) {
                    // impossible, set all intervals to empty
                    hasExplicitMax = true;
                    hasExplicitMin = true;
                    intervals.clear();
                    intervals.add(new DoubleInterval(1.0, 0.0));
                    isBottom = true;
                } // else holds anyways
                return;
            } catch (NumberFormatException nfe2) {
                nfe2.printStackTrace();
                return;
            }
        }
        // if NaN is given as a facet, the value space is empty
        if (isNaN(doubleValue)) {
            isBottom = true;
            return;
        } 
        if ((doubleValue == Double.POSITIVE_INFINITY 
                && facet == Facet.MAX_INCLUSIVE) 
        || (doubleValue == Double.NEGATIVE_INFINITY
                && facet == Facet.MIN_INCLUSIVE)) {
            return; // trivial
        }
        if (doubleValue == Double.POSITIVE_INFINITY 
                && facet == Facet.MAX_EXCLUSIVE) {
            hasExplicitMax = true;
            return; // trivial
        }
        if (doubleValue == Double.NEGATIVE_INFINITY 
                && facet == Facet.MIN_EXCLUSIVE) {
            hasExplicitMin = true;
            return; // trivial
        }
        if (doubleValue == Double.POSITIVE_INFINITY 
                && facet == Facet.MIN_INCLUSIVE) {
            // +INF is the only allowed value
            hasExplicitMin = true;
            intervals.clear();
            intervals.add(new DoubleInterval(1.0, 0.0));
            return; 
        }
        if (doubleValue == Double.NEGATIVE_INFINITY 
                && facet == Facet.MAX_INCLUSIVE) {
            // -INF is the only allowed value
            hasExplicitMax = true;
            intervals.clear();
            intervals.add(new DoubleInterval(1.0, 0.0));
            return; 
        }
        if (doubleValue == Double.POSITIVE_INFINITY 
                || doubleValue == Double.NEGATIVE_INFINITY) {
            // remaining cases: impossible, set all intervals to empty
            isBottom = true;
            return;
        } 
        switch (facet) {
        case MIN_INCLUSIVE: {
            for (DoubleInterval i : intervals) {
                i.intersectWith(new DoubleInterval(doubleValue, Double.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            for (DoubleInterval i : intervals) {
                i.intersectWith(new DoubleInterval(doubleValue, Double.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            for (DoubleInterval i : intervals) {
                i.intersectWith(new DoubleInterval(-Double.MAX_VALUE, doubleValue));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            for (DoubleInterval i : intervals) {
                i.intersectWith(new DoubleInterval(-Double.MAX_VALUE, doubleValue));
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
     * Returns a double such that it is greater than the given one and there is 
     * no double that is smaller than the returned one and greater than the 
     * given one. If the given one is -0.0, then the next greater one is +0.0. 
     * @param value a double
     * @return the next greater double compared to the given one or the value 
     * itself if it is NaN, +Infinity, or -Infinity
     */
    public static double nextDouble(double value) {
        long bits = Double.doubleToRawLongBits(value);
        long magnitude = (bits & 0x7fffffffffffffffl);
        // NaN or +inf or -inf -> no successor
        if (DatatypeRestrictionDouble.isNaN(value) 
                || magnitude == 0x7f80000000000000l) {
            return value;
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
            return Double.longBitsToDouble(newBits);
        }
    }
    
    /**
     * Returns a double such that it is smaller than the given one and there is 
     * no double that is greater than the returned one and smaller than the 
     * given one. If the given one is +0.0, then the next smaller one is -0.0. 
     * @param value a double
     * @return the next greater double compared to the given one or the value 
     * itself if it is NaN, +Infinity, or -Infinity
     */
    public static double previousDouble(double value) {
        long bits = Double.doubleToRawLongBits(value);
        long magnitude = (bits & 0x7fffffffffffffffl);
        // NaN or -inf or +inf -> no predeccessor
        if (DatatypeRestrictionDouble.isNaN(value) 
                || magnitude == 0x7f80000000000000l) {
            return value;
        } else {
            boolean negative = ((bits & 0x8000000000000000l) != 0);
            boolean newNegative;
            long newMagnitude;
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
            long newBits = newMagnitude | (newNegative ? 0x8000000000000000l : 0);
            return Double.longBitsToDouble(newBits);
        }
    }
    
    /**
     * Determines whether the given double is the special value NaN (not a 
     * number). 
     * @param value a double
     * @return true if the given value is NaN and false otherwise
     */
    public static boolean isNaN(double value) {
        long bits = Double.doubleToRawLongBits(value);
        boolean result = (bits & 0x7ff0000000000000l) == 0x7ff0000000000000l;
        result = result && (bits & 0x000fffffffffffffl) != 0;
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        if (!(constant.getImplementation() == Impl.IInteger 
                || constant.getImplementation() == Impl.IDouble
                || constant.getImplementation() == Impl.IDecimal)) {
            return false;
        }
        String value = constant.getValue();
        double doubleValue = Double.parseDouble(value);
        if (doubleValue == Double.POSITIVE_INFINITY) {
            return hasExplicitMax ? false : true; 
        }
        if (doubleValue == Double.NEGATIVE_INFINITY) {
            return hasExplicitMin ? false : true; 
        }
        if (isNaN(doubleValue)) {
            return (hasExplicitMax || hasExplicitMin) ? false : true; 
        }
        if (constant.getImplementation() == Impl.IDecimal) {
            BigDecimal dbd = new BigDecimal(value);
            if (dbd.compareTo(new BigDecimal("" + -Double.MAX_VALUE)) <= 0 
                    || dbd.compareTo(new BigDecimal("" + Double.MAX_VALUE)) >= 0) {
                return false;
            } 
            value = "" + dbd.doubleValue();
        }
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        for (DoubleInterval i : intervals) {
            if (i.contains(doubleValue)) {
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
        if (!(range instanceof DoubleFacet)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DoubleFacet. It is " +
                    "only allowed to add other double facets. ");
        }
        if (!isBottom()) {
            DoubleFacet restr = (DoubleFacet) range;
            if (restr.getDoubleIntervals().size() > 1) {
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
                for (DoubleInterval i : restr.getDoubleIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() > -Double.MAX_VALUE) {
                                intervals.add(new DoubleInterval(-Double.MAX_VALUE, i.getMin()));
                            }
                            if (i.getMax() < Double.MAX_VALUE) {
                                intervals.add(new DoubleInterval(i.getMax(), Double.MAX_VALUE));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getDoubleIntervals();
                    }
                }
            } else {
                Set<DoubleInterval> newIntervals = new HashSet<DoubleInterval>();
                if (restr.isNegated()) {
                    for (DoubleInterval i1 : intervals) {
                        for (DoubleInterval i2 : restr.getDoubleIntervals()) {
                            if (!i2.isEmpty()) {
                                if (i2.getMin() > -Double.MAX_VALUE) {
                                    DoubleInterval newInterval = i1.getCopy();
                                    double newMin = DatatypeRestrictionDouble.previousDouble(i2.getMin());
                                    newInterval.intersectWith(new DoubleInterval(-Double.MAX_VALUE, newMin));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (i2.getMax() < Double.MAX_VALUE) {
                                    DoubleInterval newInterval = i1.getCopy();
                                    double newMax = DatatypeRestrictionDouble.nextDouble(i2.getMax()); 
                                    newInterval.intersectWith(new DoubleInterval(newMax, Double.MAX_VALUE));
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
                    if (restr.getDoubleIntervals().isEmpty()) {
                        newIntervals = intervals;
                    } else {
                        for (DoubleInterval i1 : intervals) {
                            for (DoubleInterval i2 : restr.getDoubleIntervals()) {
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
            for (DoubleInterval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                double not = Double.parseDouble(constant.getValue());
                for (DoubleInterval i : intervals) {
                    if (i.contains(not) 
                            || (DatatypeRestrictionDouble.isNaN(not) && !hasExplicitMax && !hasExplicitMin) 
                            || (Double.POSITIVE_INFINITY == not && !hasExplicitMax) 
                            || (Double.NEGATIVE_INFINITY == not && !hasExplicitMin)) {
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
        for (DoubleInterval i : intervals) {
            rangeSize = rangeSize.add(i.getCardinality());
        }
        // plus NaN, +Inf, -Inf
        int numSpecials = 3;
        if (hasExplicitMax) numSpecials--; // +Inf impossible
        if (hasExplicitMin) numSpecials--; // -Inf impossible
        if (hasExplicitMin || hasExplicitMax) numSpecials--; // NaN impossible
        rangeSize = rangeSize.add(new BigInteger("" + numSpecials));
        for (DataConstant constant : notOneOf) {
            double not = Double.parseDouble(constant.getValue());
            for (DoubleInterval i : intervals) {
                if (i.contains(not) 
                        || (DatatypeRestrictionDouble.isNaN(not) && !hasExplicitMax && !hasExplicitMin) 
                        || (Double.POSITIVE_INFINITY == not && !hasExplicitMax) 
                        || (Double.NEGATIVE_INFINITY == not && !hasExplicitMin)) {
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
            DataConstant dataConstant = new DataConstant(Impl.IDouble, datatype, "Infinity");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMax) {
                return dataConstant;
            }
            dataConstant = new DataConstant(Impl.IDouble, datatype, "-Infinity");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMin) {
                return dataConstant;
            }
            dataConstant = new DataConstant(Impl.IDouble, datatype, "NaN");
            if (!notOneOf.contains(dataConstant) && !hasExplicitMax && !hasExplicitMin) {
                return dataConstant;
            }
            SortedSet<DoubleInterval> sortedIntervals = new TreeSet<DoubleInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (DoubleInterval i : sortedIntervals) {
                double constant = i.getMin();
                while (constant <= i.getMax()) {
                    dataConstant = new DataConstant(Impl.IDouble, datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = DatatypeRestrictionDouble.nextDouble(constant); 
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#getDoubleIntervals()
     */
    public Set<DoubleInterval> getDoubleIntervals() {
        return intervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#hasExplicitMin()
     */
    public boolean hasExplicitMin() {
        return hasExplicitMin;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#hasExplicitMax()
     */
    public boolean hasExplicitMax() {
        return hasExplicitMax;
    }

    public Set<FloatInterval> getFloatIntervals() {
        Set<FloatInterval> floatIntervals = new HashSet<FloatInterval>();
        if (!intervals.isEmpty()) {
            for (DoubleInterval i : intervals) {
                Double min = i.getMin();
                Double max = i.getMax();
                float minF = -Float.MAX_VALUE;
                float maxF = Float.MAX_VALUE;
                boolean isEmpty = false;
                if (min >= -Float.MAX_VALUE) {
                    if (min <= Float.MAX_VALUE) {
                        minF = min.floatValue();
                    } else {
                        // >= max float -> empty range
                        isEmpty = true;
                    }
                } // minF = -Float.MAX_VALUE
                if (max <= Float.MAX_VALUE) {
                    if (max >= -Float.MAX_VALUE) {
                        maxF = max.floatValue();
                    } else {
                        // <= min float -> empty range
                        isEmpty = true;
                    }
                } // maxF = Float.MAX_VALUE
                if (isEmpty) {
                    minF = Float.MAX_VALUE;
                    maxF = Float.MIN_VALUE;
                }
                FloatInterval iFloat = new FloatInterval(minF, maxF);
                floatIntervals.add(iFloat);
            }
        }
        return floatIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerFacet#getIntegerIntervals()
     */
    public Set<IntegerInterval> getIntegerIntervals() {
        Set<IntegerInterval> integerIntervals = new HashSet<IntegerInterval>();
        if (!intervals.isEmpty()) {
            for (DoubleInterval i : intervals) {
                IntegerInterval iInteger;
                Double min = i.getMin();
                Double max = i.getMax();
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
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Namespaces)
     */
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (DoubleInterval i : intervals) {
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
        return constant.getImplementation() == Impl.IDouble
                || constant.getImplementation() == Impl.IFloat
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
    protected static class IntervalComparator implements Comparator<DoubleInterval> { 
        public static Comparator<DoubleInterval> INSTANCE = new IntervalComparator();
        public int compare(DoubleInterval i1, DoubleInterval i2) {
            if (i1.getMin() == i2.getMin()) return 0;
            return (i1.getMin() - i2.getMin() > 0) ? 1 : -1;
        }
    }
}
