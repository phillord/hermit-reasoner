/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;

/**
 * An implementation of owlReal and owlRealPlus datatype restrictions. 
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionRational 
        extends DatatypeRestriction 
        implements IntegerFacet, FloatFacet, DoubleFacet, DecimalFacet {

    private static final long serialVersionUID = -5733278766461283273L;
    
    protected Set<RationalInterval> intervals = new HashSet<RationalInterval>();

    /**
     * An implementation for owlReal and owlRealPlus. If this constructor is 
     * used, the specials (NaN, -0.0, +INF, -INF) are automatically contained in 
     * the range. 
     * @param datatype a datatype (should use DT.OWLREALPLUS)
     */
    public DatatypeRestrictionRational(DT datatype) {
        this(datatype, true);
    }
    
    /**
     * An implementation for owlReal and owlRealPlus. With this constructor one 
     * can choose whether specials (NaN, -0.0, +INF, -INF) are included or not. 
     * @param datatype a datatyp (should be DT.OWLREAL or DT.OWLREALPLUS)
     * @param allowSpecials if true, specials are part of the range
     */
    public DatatypeRestrictionRational(DT datatype, boolean allowSpecials) {
        this.datatype = datatype;
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, 
                        Facets.MIN_EXCLUSIVE, 
                        Facets.MAX_INCLUSIVE, 
                        Facets.MAX_EXCLUSIVE
                })
        );
        if (!allowSpecials) {
            notOneOf.addAll(DataConstant.numericSpecials);
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionRational(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return isBottom() || !oneOf.isEmpty();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facets facet, String value) {
        BigDecimal valueDec = null;
        BigRational valueR = null;
        try {
            valueR = BigRational.parseRational(value);
        } catch (NumberFormatException e) {
            // maybe it is not a rational
            try {
                valueDec = new BigDecimal(value);
                valueR = BigRational.convertToRational(valueDec);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("The given facet restriction " 
                        + value + " is not numeric. ");
            }
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            // greater or equal X
            if (intervals.isEmpty()) {
                intervals.add(new RationalInterval(valueR, null, false, true));
            } else {
                for (RationalInterval i : intervals) {
                    i.intersectWith(new RationalInterval(valueR, null, false, true));
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            // greater than X
            if (intervals.isEmpty()) {
                intervals.add(new RationalInterval(valueR, null, true, true));
            } else {
                for (RationalInterval i : intervals) {
                    i.intersectWith(new RationalInterval(valueR, null, true, true));
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            // smaller or equal X
            if (intervals.isEmpty()) {
                intervals.add(new RationalInterval(null, valueR, true, false));
            } else {
                for (RationalInterval i : intervals) {
                    i.intersectWith(new RationalInterval(null, valueR, true, false));
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            // smaller than X
            if (intervals.isEmpty()) {
                intervals.add(new RationalInterval(null, valueR, true, true));
            } else {
                for (RationalInterval i : intervals) {
                    i.intersectWith(new RationalInterval(null, valueR, true, true));
                }
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof DatatypeRestrictionRational)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionRational. It is only " +
                    "allowed to add facets from other owl rational datatype " +
                    "restrictions.");
        }
        if (!isBottom()) {
            DatatypeRestrictionRational restr = (DatatypeRestrictionRational) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (RationalInterval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                intervals.add(new RationalInterval(null, i.getMin(), true, !i.isOpenMin()));
                            }
                            if (i.getMax() != null) {
                                intervals.add(new RationalInterval(i.getMax(), null, !i.isOpenMax(), true));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getIntervals();
                    }
                }
            } else {
                Set<RationalInterval> newIntervals = new HashSet<RationalInterval>();
                if (restr.isNegated()) {
                    for (RationalInterval i : intervals) {
                        for (RationalInterval iNew : restr.getIntervals()) {
                            if (!iNew.isEmpty()) {
                                if (iNew.getMin() != null) {
                                    RationalInterval newInterval = i.getCopy();
                                    newInterval.intersectWith(new RationalInterval(null, iNew.getMin(), true, !iNew.isOpenMin()));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() != null) {
                                    RationalInterval newInterval = i.getCopy();
                                    newInterval.intersectWith(new RationalInterval(iNew.getMax(), null, !iNew.isOpenMax(), true));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (restr.getIntervals().isEmpty()) {
                        newIntervals = intervals;
                    } else {
                        for (RationalInterval i1 : intervals) {
                            for (RationalInterval i2 : restr.getIntervals()) {
                                i1.intersectWith(i2);
                                if (!i1.isEmpty()) newIntervals.add(i1);
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
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        BigRational value = BigRational.parseRational(constant.getValue());
        for (RationalInterval i : intervals) {
            if (i.contains(value)) {
                return true;
            }
        }
        return false; 
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#hasMinCardinality(java.math.BigInteger)
     */
    public boolean hasMinCardinality(BigInteger n) {
        if (!oneOf.isEmpty()) {
            return (n.compareTo(new BigInteger("" + oneOf.size())) >= 0);
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() {
        return new BigInteger("" + oneOf.size());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getSmallestAssignment()
     */
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        return null;
    }
    

    /**
     * Returns the inervals for this datatype restriction. 
     * @return a set of decimal intervals
     */
    public Set<RationalInterval> getIntervals() {
        return intervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.IntegerFacet#getIntegerIntervals()
     */
    public Set<IntegerInterval> getIntegerIntervals() {
        Set<IntegerInterval> integerIntervals = new HashSet<IntegerInterval>();
        if (!intervals.isEmpty()) {
            for (RationalInterval i : intervals) {
                BigRational min = i.getMin();
                BigRational max = i.getMax();
                BigInteger minInt = null;
                BigInteger maxInt = null;
                Long minLong = null;
                Long maxLong = null;
                boolean hasBig = false;
                if (min != null) {
                    minInt = min.integerValue(BigDecimal.ROUND_CEILING);
                    if (i.isOpenMin() && min.equals(new BigRational(minInt, BigInteger.ONE))) {
                        minInt = minInt.add(BigInteger.ONE);
                    }
                    try {
                        minLong = new BigDecimal(minInt).longValueExact();
                    } catch (ArithmeticException e) {
                        hasBig = true;
                    }
                }
                if (max != null) {
                    maxInt = max.integerValue(BigDecimal.ROUND_FLOOR);
                    if (i.isOpenMax() && max.equals(new BigDecimal(maxInt))) {
                        maxInt = maxInt.subtract(BigInteger.ONE);
                    }
                    try {
                        maxLong = new BigDecimal(maxInt).longValueExact();
                    } catch (ArithmeticException e) {
                        hasBig = true;
                    }
                }
                IntegerInterval iInteger = hasBig ? 
                        new IntegerIntervalBig(minInt, maxInt) : 
                        new IntegerIntervalFin(minLong, maxLong);
                integerIntervals.add(iInteger);
            }
        }
        return integerIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#hasExplicitMin()
     */
    public boolean hasExplicitMin() {
        for (RationalInterval i : intervals) {
            if (i.getMin() != null) return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#hasExplicitMax()
     */
    public boolean hasExplicitMax() {
        for (RationalInterval i : intervals) {
            if (i.getMax() != null) return true;
        }
        return false;
    }

    public Set<DecimalInterval> getDecimalIntervals() {
        Set<DecimalInterval> decimalIntervals = new HashSet<DecimalInterval>();
        if (!intervals.isEmpty()) {
            for (RationalInterval i : intervals) {
                BigRational min = i.getMin();
                BigRational max = i.getMax();
                BigDecimal minBD = null;
                BigDecimal maxBD = null;
                try {
                    minBD = min.bigDecimalValueExact();
                } catch (ArithmeticException e) {
                    minBD = min.bigDecimalValue(RoundingMode.CEILING);
                }
                try {
                    maxBD = max.bigDecimalValueExact();
                } catch (ArithmeticException e) {
                    maxBD = max.bigDecimalValue(RoundingMode.FLOOR);
                }
                DecimalInterval iDecimal = new DecimalInterval(minBD, maxBD, i.isOpenMin(), i.isOpenMax());
                decimalIntervals.add(iDecimal);
            }
        }
        return decimalIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DoubleFacet#getDoubleIntervals()
     */
    public Set<DoubleInterval> getDoubleIntervals() {
        Set<DoubleInterval> doubleIntervals = new HashSet<DoubleInterval>();
        if (!intervals.isEmpty()) {
            for (RationalInterval i : intervals) {
                BigRational min = i.getMin();
                BigRational max = i.getMax();
                BigRational maxDouble = BigRational.convertToRational(new BigDecimal("" + Double.MAX_VALUE));
                BigRational minDouble = maxDouble.negate();
                Double minD = -Double.MAX_VALUE;
                Double maxD = Double.MAX_VALUE;
                boolean isEmpty = false;
                if (min.compareTo(minDouble) >= 0) {
                    if (min.compareTo(maxDouble) <= 0) {
                        minD = min.doubleValue();
                        if (minD.equals(Double.NEGATIVE_INFINITY)) {
                            minD = -Double.MAX_VALUE;
                        }
                        if (min.compareTo(BigRational.convertToRational(new BigDecimal("" + minD))) >= 0 
                                && i.isOpenMin()) {
                            minD = DatatypeRestrictionDouble.nextDouble(minD);
                        }
                    } else {
                        // >= max double -> empty range
                        isEmpty = true;
                    }
                } // minD = -Double.MAX_VALUE
                if (max.compareTo(maxDouble) <= 0) {
                    if (max.compareTo(minDouble) >= 0) {
                        maxD = max.doubleValue();
                        if (maxD.equals(Double.POSITIVE_INFINITY)) {
                            maxD = Double.MAX_VALUE;
                        }
                        if (max.compareTo(BigRational.convertToRational(new BigDecimal("" + maxD))) <= 0 
                                && i.isOpenMax()) {
                            maxD = DatatypeRestrictionDouble.previousDouble(maxD);
                        }
                    } else {
                        // <= min double -> empty range
                        isEmpty = true;
                    }
                } // maxD = Double.MAX_VALUE
                if (isEmpty) {
                    minD = Double.MAX_VALUE;
                    maxD = Double.MIN_VALUE;
                }
                DoubleInterval iDouble = new DoubleInterval(minD, maxD);
                doubleIntervals.add(iDouble);
            }
        }
        return doubleIntervals;
    }
    
    public Set<FloatInterval> getFloatIntervals() {
        Set<FloatInterval> floatIntervals = new HashSet<FloatInterval>();
        if (!intervals.isEmpty()) {
            for (RationalInterval i : intervals) {
                BigRational min = i.getMin();
                BigRational max = i.getMax();
                BigRational maxFloat = BigRational.convertToRational(new BigDecimal("" + Float.MAX_VALUE));
                BigRational minFloat = maxFloat.negate();
                Float minF = -Float.MAX_VALUE;
                Float maxF = Float.MAX_VALUE;
                boolean isEmpty = false;
                if (min.compareTo(minFloat) >= 0) {
                    if (min.compareTo(maxFloat) <= 0) {
                        minF = min.floatValue();
                        if (minF.equals(Float.NEGATIVE_INFINITY)) {
                            minF = -Float.MAX_VALUE;
                        }
                        if (min.compareTo(BigRational.convertToRational(new BigDecimal("" + minF))) >= 0 
                                && i.isOpenMin()) {
                            minF = DatatypeRestrictionFloat.nextFloat(minF);
                        }
                    } else {
                        // >= max double -> empty range
                        isEmpty = true;
                    }
                } // minD = -Double.MAX_VALUE
                if (max.compareTo(maxFloat) <= 0) {
                    if (max.compareTo(minFloat) >= 0) {
                        maxF = max.floatValue();
                        if (max.compareTo(BigRational.convertToRational(new BigDecimal("" + maxF))) <= 0 
                                && i.isOpenMax()) {
                            maxF = DatatypeRestrictionFloat.previousFloat(maxF);
                        }
                    } else {
                        // <= min double -> empty range
                        isEmpty = true;
                    }
                } // maxD = Double.MAX_VALUE
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
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Namespaces)
     */
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (RationalInterval i : intervals) {
            if (!firstRun && !isNegated) {
                buffer.append(" or ");
            }
            if (i.getMin() != null) {
                buffer.append((i.isOpenMin() ? "> " : ">= ") + i.getMin());
            }
            if (i.getMax() != null) {
                if (i.getMin() != null) buffer.append(" ");
                buffer.append((i.isOpenMax() ? "< " : "<= ") + i.getMax());
            }
            firstRun = false;
        }
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.RATIONAL).contains(constant.getDatatype());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        Set<DT> dts = DT.getSubTreeFor(DT.RATIONAL);
        dts.removeAll(DT.getSubTreeFor(DT.DECIMAL)); 
        return dts.contains(datatype);
    }
}
