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

public class DatatypeRestrictionDouble 
        extends DatatypeRestriction 
        implements DoubleFacet, IntegerFacet {
    
    protected Set<DoubleInterval> intervals = new HashSet<DoubleInterval>();
   
    public DatatypeRestrictionDouble(DT datatype) {
        this.datatype = datatype;
        intervals.add(new DoubleInterval());
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, 
                        Facets.MIN_EXCLUSIVE, 
                        Facets.MAX_INCLUSIVE, 
                        Facets.MAX_EXCLUSIVE
                })
        );
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionDouble(this.datatype);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        return !intervals.isEmpty();
    }
    
    public void addFacet(Facets facet, String value) {
        double doubleValue;
        try {
            BigDecimal originalValue = new BigDecimal(value);
            doubleValue = Double.parseDouble(value);
            BigDecimal doubleValueAsBD = new BigDecimal("" + doubleValue);
            if (facet == Facets.MIN_EXCLUSIVE || facet == Facets.MIN_INCLUSIVE) {
                if (doubleValueAsBD.compareTo(originalValue) < 0 
                        || (doubleValueAsBD.compareTo(originalValue) == 0 
                                && facet == Facets.MIN_EXCLUSIVE)) {
                    doubleValue = DatatypeRestrictionDouble.nextDouble(doubleValue);
                }
            } else {
                if (doubleValueAsBD.compareTo(originalValue) > 0 
                        || (doubleValueAsBD.compareTo(originalValue) == 0 
                                && facet == Facets.MAX_EXCLUSIVE)) {
                    //if (d > Double.MIN_VALUE) d -= Double.MIN_NORMAL;
                    doubleValue = DatatypeRestrictionDouble.previousDouble(doubleValue);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
    
    public static boolean isNaN(double value) {
        long bits = Double.doubleToRawLongBits(value);
        boolean result = (bits & 0x7ff0000000000000l) == 0x7ff0000000000000l;
        result = result && (bits & 0x000fffffffffffffl) != 0;
        return result;
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        double doubleValue = Double.parseDouble(constant.getValue());
        for (DoubleInterval i : intervals) {
            if (i.contains(doubleValue)) {
                return true;
            }
        }
        return false; 
    }

    public void conjoinFacetsFrom(DataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof DoubleFacet)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DoubleFacet restr = (DoubleFacet) range;
            if (restr.getDoubleIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
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
                    for (DoubleInterval i : intervals) {
                        for (DoubleInterval iNew : restr.getDoubleIntervals()) {
                            i.intersectWith(iNew);
                            if (!i.isEmpty()) newIntervals.add(i);
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

    public boolean hasMinCardinality(BigInteger n) {
        if (isNegated || n.compareTo(BigInteger.ZERO) <= 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (n.compareTo(new BigInteger("" + oneOf.size())) >= 0);
            }
            BigInteger rangeSize = BigInteger.ZERO;
            for (DoubleInterval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                double not = Double.parseDouble(constant.getValue());
                for (DoubleInterval i : intervals) {
                    if (i.contains(not)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return (rangeSize.compareTo(new BigInteger("" + n)) >= 0);
        }
        return true;
    }
    
    public BigInteger getEnumerationSize() {
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        }
        BigInteger rangeSize = BigInteger.ZERO;
        for (DoubleInterval i : intervals) {
            rangeSize = rangeSize.add(i.getCardinality());
        }
        for (DataConstant constant : notOneOf) {
            double not = Double.parseDouble(constant.getValue());
            for (DoubleInterval i : intervals) {
                if (i.contains(not)) {
                    rangeSize = rangeSize.subtract(BigInteger.ONE);
                }
            }
        }
        return rangeSize;
    }
    
    public DataConstant getSmallestAssignment() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                return sortedOneOfs.first();
            }
            SortedSet<DoubleInterval> sortedIntervals = new TreeSet<DoubleInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (DoubleInterval i : sortedIntervals) {
                double constant = i.getMin();
                while (constant <= i.getMax()) {
                    DataConstant dataConstant = new DataConstant(datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = DatatypeRestrictionDouble.nextDouble(constant); 
                }
            }
        }
        return null;
    }
    
    public Set<DoubleInterval> getDoubleIntervals() {
        return intervals;
    }
    
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
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.DOUBLE).contains(constant.getDatatype());
    }
    
    public boolean canHandleAll(Set<DT> datatypes) {
        return DT.getSubTreeFor(DT.OWLREALPLUS).containsAll(datatypes);
    }
    
    protected static class IntervalComparator implements Comparator<DoubleInterval> { 
        public static Comparator<DoubleInterval> INSTANCE = new IntervalComparator();
        public int compare(DoubleInterval i1, DoubleInterval i2) {
            if (i1.getMin() == i2.getMin()) return 0;
            return (i1.getMin() - i2.getMin() > 0) ? 1 : -1;
        }
    }
}
