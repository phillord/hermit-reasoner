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

public class DatatypeRestrictionDouble extends DatatypeRestriction {
    
    protected Set<Interval> intervals = new HashSet<Interval>();
   
    public DatatypeRestrictionDouble(DT datatype) {
        this.datatype = datatype;
        intervals.add(new Interval());
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
                    doubleValue = nextDouble(doubleValue);
                }
            } else {
                if (doubleValueAsBD.compareTo(originalValue) > 0 
                        || (doubleValueAsBD.compareTo(originalValue) == 0 
                                && facet == Facets.MAX_EXCLUSIVE)) {
                    //if (d > Double.MIN_VALUE) d -= Double.MIN_NORMAL;
                    doubleValue = previousDouble(doubleValue);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            for (Interval i : intervals) {
                i.intersectWith(new Interval(doubleValue, Double.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            for (Interval i : intervals) {
                i.intersectWith(new Interval(doubleValue, Double.MAX_VALUE));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            for (Interval i : intervals) {
                i.intersectWith(new Interval(-Double.MAX_VALUE, doubleValue));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            for (Interval i : intervals) {
                i.intersectWith(new Interval(-Double.MAX_VALUE, doubleValue));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    
    public double nextDouble(double value) {
        long bits = Double.doubleToRawLongBits(value);
        long magnitude = (bits & 0x7fffffffffffffffl);
        // NaN or +inf or -inf -> no successor
        if (isNaN(bits) || magnitude == 0x7f80000000000000l) {
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
    
    public double previousDouble(double value) {
        long bits = Double.doubleToRawLongBits(value);
        long magnitude = (bits & 0x7fffffffffffffffl);
        // NaN or -inf or +inf -> no predeccessor
        if (isNaN(bits) || magnitude == 0x7f80000000000000l) {
            return value;
        } else {
            boolean negative = ((bits & 0x8000000000000000l) == 1);
            boolean newNegative;
            long newMagnitude;
            if (negative) {
                newNegative = true;
                newMagnitude = magnitude - 1;
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
    
    public boolean isNaN(long bits) {
        return ((bits & 0x7f80000000000000l) == 0x7f80000000000000l) 
                && ((bits & 0x003fffffffffffffl) != 0);
    }
    
    public boolean facetsAccept(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        double doubleValue = Double.parseDouble(constant.getValue());
        for (Interval i : intervals) {
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
        if (!(range instanceof DatatypeRestrictionDouble)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DatatypeRestrictionDouble restr = (DatatypeRestrictionDouble) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (Interval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() > -Double.MAX_VALUE) {
                                intervals.add(new Interval(-Double.MAX_VALUE, i.getMin()));
                            }
                            if (i.getMax() < Double.MAX_VALUE) {
                                intervals.add(new Interval(i.getMax(), Double.MAX_VALUE));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getIntervals();
                    }
                }
            } else {
                Set<Interval> newIntervals = new HashSet<Interval>();
                if (restr.isNegated()) {
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
                            if (!iNew.isEmpty()) {
                                if (iNew.getMin() > -Double.MAX_VALUE) {
                                    Interval newInterval = i.getCopy();
                                    double newMin = previousDouble(iNew.getMin());
                                    newInterval.intersectWith(new Interval(-Double.MAX_VALUE, newMin));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() < Double.MAX_VALUE) {
                                    Interval newInterval = i.getCopy();
                                    double newMax = nextDouble(iNew.getMax()); 
                                    newInterval.intersectWith(new Interval(newMax, Double.MAX_VALUE));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                }
                            } else {
                                newIntervals.add(i);
                            }
                        }
                    }
                } else {
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
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
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        double doubleValue = Double.parseDouble(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(doubleValue) && !notOneOf.contains(constant)) {
                return true;
            }
        }
        return false; 
    }

    public boolean hasMinCardinality(BigInteger n) {
        if (isNegated || n.compareTo(BigInteger.ZERO) <= 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (n.compareTo(new BigInteger("" + oneOf.size())) >= 0);
            }
            BigInteger rangeSize = BigInteger.ZERO;
            for (Interval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                double not = Double.parseDouble(constant.getValue());
                for (Interval i : intervals) {
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
        for (Interval i : intervals) {
            rangeSize = rangeSize.add(i.getCardinality());
        }
        for (DataConstant constant : notOneOf) {
            double not = Double.parseDouble(constant.getValue());
            for (Interval i : intervals) {
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
            SortedSet<Interval> sortedIntervals = new TreeSet<Interval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (Interval i : sortedIntervals) {
                double constant = i.getMin();
                while (constant <= i.getMax()) {
                    DataConstant dataConstant = new DataConstant(datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = nextDouble(constant); 
                }
            }
        }
        return null;
    }
    
    public Set<Interval> getIntervals() {
        return intervals;
    }
    
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (Interval i : intervals) {
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
    
    public class Interval {
        double min = -Double.MAX_VALUE;
        double max = Double.MAX_VALUE;
        
        public Interval() {
            super();
        }
        
        public Interval(double minInclusive, double maxInclusive) {
            this.min = minInclusive;
            this.max = maxInclusive;
        }
        
        public Interval getCopy() {
            return new Interval(min, max);
        }
        
        public void intersectWith(Interval i) {
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
        
        protected boolean isEmpty(double lower, double upper) {
            return (lower > upper);
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
        
        public boolean contains(Interval interval) {
            return contains(interval.getMin()) 
                    && contains(interval.getMax());
        }
        
        public boolean disjointWith(Interval interval) {
            return (min >= interval.getMax() || max <= interval.getMin());
        }
        
        public BigInteger getCardinality() {
            if (max < min) return BigInteger.ZERO;
            // Extract the sign and magnitude from 'start'
            long bitsStart  = Double.doubleToRawLongBits(min);
            long bitsEnd = Double.doubleToRawLongBits(max);
            if (isNaN(bitsStart) || isNaN(bitsEnd)) {
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
                BigInteger rangeSize = new BigInteger("" + (magnitudeStart + 1));
                // the number of values from +0 to 'end'
                rangeSize = rangeSize.add(new BigInteger("" + (magnitudeEnd + 1)));
                return rangeSize;   
            } else {
                // if (positiveStart && !positiveEnd)
                return BigInteger.ZERO;
            }
        }

        protected boolean isNaN(long bits) {
            return ((bits & 0x7f80000000000000l) == 0x7f80000000000000l) 
                    && ((bits & 0x003fffffffffffffl) != 0);
        }
        
        public boolean increaseMin() {
            long bits = Double.doubleToRawLongBits(min);
            long magnitude = (bits & 0x7fffffffffffffffl);
            // NaN or +inf -> no successor
            if (isNaN(bits) || magnitude == 0x7f80000000000000l) {
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

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMaxIncl(double max) {
            this.max = max;
        }
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(">= " + min);
            buffer.append(" ");
            buffer.append("<= " + max);
            return buffer.toString();
        }
    }
    
    protected static class IntervalComparator implements Comparator<Interval> { 
        public static Comparator<Interval> INSTANCE = new IntervalComparator();
        public int compare(Interval i1, Interval i2) {
            if (i1.getMin() == i2.getMin()) return 0;
            return (i1.getMin() - i2.getMin() > 0) ? 1 : -1;
        }
    }
}
