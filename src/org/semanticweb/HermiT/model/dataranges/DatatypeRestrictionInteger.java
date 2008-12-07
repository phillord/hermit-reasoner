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

/**
 * An implementation for integer value. The implementation should also be used 
 * for doubles and decimals that represent integer values, e.g., 3.0 is to be 
 * treated as integer 3. The implementation uses longs for the ranges that are 
 * defined by the facets unless the given facets cannot be captured by longs. 
 * The implementation then switches automatically to BigIntegers.
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionInteger extends DatatypeRestriction 
        implements IntegerFacet {
    
    private static final long serialVersionUID = -2125671793326110719L;
    
    protected Set<IntegerInterval> intervals = new HashSet<IntegerInterval>();

    /**
     * An implementation for integers. Can handle all integer based types, e.g., 
     * short, positiveInteger, etc. 
     * @param datatype an integer based datatype (e.g., DT.BYTE)
     */
    public DatatypeRestrictionInteger(DT datatype) {
        this.datatype = datatype;
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, 
                        Facets.MIN_EXCLUSIVE, 
                        Facets.MAX_INCLUSIVE, 
                        Facets.MAX_EXCLUSIVE
                })
        );
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionInteger(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    /**
     * Determines if if all intervals are finite. 
     * @return true if intervals are given and all intervals have a lower and an 
     * upper bound and false otherwise. 
     */
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (intervals.isEmpty()) return false;
        for (IntegerInterval i : intervals) {
            hasOnlyFiniteIntervals  = hasOnlyFiniteIntervals  && i.isFinite();
        }
        return hasOnlyFiniteIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facets facet, String value) {
        IntegerInterval iNew = null;
        try {
            BigDecimal bd = new BigDecimal(value);
            if (facet == Facets.MIN_EXCLUSIVE || facet == Facets.MAX_INCLUSIVE) {
                bd = bd.setScale(0, BigDecimal.ROUND_FLOOR);
            } else {
                bd = bd.setScale(0, BigDecimal.ROUND_CEILING);
            }
            if (facet == Facets.MIN_EXCLUSIVE) {
                bd = bd.add(BigDecimal.ONE);
            }
            if (facet == Facets.MAX_EXCLUSIVE) {
                bd = bd.subtract(BigDecimal.ONE);
            }
            if (facet == Facets.MIN_EXCLUSIVE 
                    || facet == Facets.MIN_INCLUSIVE) {
                try {
                    iNew = new IntegerIntervalFin(bd.longValueExact(), null);
                } catch (ArithmeticException e) {
                    iNew = new IntegerIntervalBig(bd.toBigInteger(), null);
                }
            } else if (facet == Facets.MAX_EXCLUSIVE 
                    || facet == Facets.MAX_INCLUSIVE) {
                try {
                    iNew = new IntegerIntervalFin(null, bd.longValueExact());
                } catch (ArithmeticException e) {
                    iNew = new IntegerIntervalBig(null, bd.toBigInteger());
                }
            } else {
                throw new IllegalArgumentException("Unsupported facet.");
            }
            if (intervals.isEmpty()) {
                intervals.add(iNew);
            } else {
                for (IntegerInterval i : intervals) {
                    i = i.intersectWith(iNew);
                    if (i.isEmpty()) {
                        isBottom = true;
                    } else {
                        intervals.clear();
                        intervals.add(i);
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
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
        if (constant.getImplementation() == Impl.IDecimal 
                && !(new BigDecimal(value).scale() == 0)) {
            return false;
        }
        if (constant.getImplementation() == Impl.IDouble 
                && !(new Double(value).longValue() == new Double(value))) {
            return false;
        }
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        Number numValue = null;
        try {
            BigInteger valueBig = new BigInteger(value);
            if (IntegerIntervalFin.isLong(valueBig)) {
                numValue = valueBig.longValue();
                for (IntegerInterval i : intervals) {
                    if (i.contains(numValue)) {
                        return true;
                    }
                }
            } else {
                for (IntegerInterval i : intervals) {
                    if (IntegerIntervalBig.toIntegerIntervalBig(i).contains(valueBig)) {
                        return true;
                    }
                }
            }
        } catch (NumberFormatException nfe1) {
            return false;
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
        if (!(range instanceof IntegerFacet)) {
            throw new IllegalArgumentException("The given parameter does not " +
                    "allow for integer facets. It is only allowed to add " +
                    "facets from other integer restrictions. ");
        }
        if (!isBottom()) {
            IntegerFacet restr = (IntegerFacet) range;
            if (restr.getIntegerIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (IntegerInterval i : restr.getIntegerIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                intervals.add(i.getInstance(null, i.getMin()));
                            }
                            if (i.getMax() != null) {
                                intervals.add(i.getInstance(i.getMax(), null));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getIntegerIntervals();
                    }
                }
            } else {
                Set<IntegerInterval> newIntervals = new HashSet<IntegerInterval>();
                if (restr.isNegated()) {
                    for (IntegerInterval i1 : intervals) {
                        for (IntegerInterval i2 : restr.getIntegerIntervals()) {
                            if (!i2.isEmpty()) {
                                if (i2.getMin() != null) {
                                    IntegerInterval newI 
                                        = i2.getInstance(null, i2.decreasedMin());
                                    newI = newI.intersectWith(i1);
                                    if (!newI.isEmpty()) {
                                        newIntervals.add(newI);
                                    }
                                } 
                                if (i2.getMax() != null) {
                                    IntegerInterval newI 
                                        = i2.getInstance(i2.increasedMax(), null);
                                    newI = newI.intersectWith(i1);
                                    if (!newI.isEmpty()) {
                                        newIntervals.add(newI);
                                    }
                                }
                            } else {
                                newIntervals.add(i1);
                            }
                        }
                    }
                } else {
                    if (restr.getIntegerIntervals().isEmpty()) {
                        newIntervals = intervals;
                    } else {
                        for (IntegerInterval i1 : intervals) {
                            for (IntegerInterval i2 : restr.getIntegerIntervals()) {
                                i1 = i1.intersectWith(i2);
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
            for (IntegerInterval i : intervals) {
                if (!i.isFinite()) return true;
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (IntegerInterval i : intervals) {
                    if (i.contains(not)) {
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
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            BigInteger rangeSize = BigInteger.ZERO;
            for (IntegerInterval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (IntegerInterval i : intervals) {
                    if (i.contains(not)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return rangeSize;
        } 
        return null;
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
            if (!intervals.isEmpty()) {
                SortedSet<IntegerInterval> sortedIntervals = new TreeSet<IntegerInterval>(IntervalComparator.INSTANCE);
                sortedIntervals.addAll(intervals);
                for (IntegerInterval i : sortedIntervals) {
                    BigInteger constant = new BigInteger("" + i.getMin());
                    while (i.contains(constant)) {
                        DataConstant dataConstant = new DataConstant(Impl.IInteger, datatype, "" + constant);
                        if (!notOneOf.contains(dataConstant)) return dataConstant;
                        constant = constant.add(BigInteger.ONE);
                    }
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerFacet#getIntegerIntervals()
     */
    public Set<IntegerInterval> getIntegerIntervals() {
        return intervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Namespaces)
     */
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (IntegerInterval i : intervals) {
            if (!firstRun && !isNegated) {
                buffer.append(" or ");
            }
            if (i.getMin() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" >= " + i.getMin());
            }
            if (i.getMax() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" <= " + i.getMax());
            }
            firstRun = false;
        }
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return constant.getImplementation() == Impl.IInteger;
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
    protected static class IntervalComparator implements Comparator<IntegerInterval> { 
        public static Comparator<IntegerInterval> INSTANCE = new IntervalComparator();
        public int compare(IntegerInterval i1, IntegerInterval i2) {
            if (i1 instanceof IntegerIntervalFin && i2 instanceof IntegerIntervalFin) { 
                IntegerIntervalFin i1Fin = (IntegerIntervalFin) i1;
                IntegerIntervalFin i2Fin = (IntegerIntervalFin) i2;
                if (i1Fin.getMin() == i2Fin.getMin()) return 0;
                return (i1Fin.getMin().longValue() - i2Fin.getMin().longValue() > 0) ? 1 : -1; 
            } else {
                IntegerIntervalBig i1Big;
                IntegerIntervalBig i2Big;
                if (i1 instanceof IntegerIntervalBig) {
                    i1Big = (IntegerIntervalBig) i1;
                } else {
                    i1Big = IntegerIntervalBig.toIntegerIntervalBig(i1);
                }
                if (i2 instanceof IntegerIntervalBig) {
                    i2Big = (IntegerIntervalBig) i2;
                } else {
                    i2Big = IntegerIntervalBig.toIntegerIntervalBig(i2);
                }
                BigInteger min1 = (BigInteger) i1Big.getMin();
                BigInteger min2 = (BigInteger) i2Big.getMin();
                return min1.compareTo(min2); 
            }
        }
    }
}
