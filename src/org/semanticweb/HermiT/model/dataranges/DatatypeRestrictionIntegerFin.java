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

public class DatatypeRestrictionIntegerFin extends DatatypeRestriction {
    
    protected Set<Interval> intervals = new HashSet<Interval>();
   
    public DatatypeRestrictionIntegerFin(DT datatype) {
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
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionIntegerFin(this.datatype);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        return !intervals.isEmpty();
    }
    
    public void addFacet(Facets facet, String value) {
        long longValue;
        try {
            BigDecimal bd = new BigDecimal(value);
            if (facet == Facets.MIN_EXCLUSIVE || facet == Facets.MAX_INCLUSIVE) {
                bd = bd.setScale(0, BigDecimal.ROUND_FLOOR);
                longValue = bd.longValueExact();
            } else {
                bd = bd.setScale(0, BigDecimal.ROUND_CEILING);
                longValue = bd.longValueExact();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            // greater or equal X
            if (intervals.isEmpty()) {
                intervals.add(new Interval(longValue, Long.MAX_VALUE));
            } else {
                for (Interval i : intervals) {
                    i.intersectWith(new Interval(longValue, Long.MAX_VALUE));
                    if (i.isEmpty()) {
                        isBottom = true;
                    }
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            // greater than X = greater or equal X + 1
            longValue++;
            addFacet(Facets.MIN_INCLUSIVE, "" + longValue);
        } break;
        case MAX_INCLUSIVE: {
            // smaller or equal X
            if (intervals.isEmpty()) {
                intervals.add(new Interval(Long.MIN_VALUE, longValue));
            } else {
                for (Interval i : intervals) {
                    i.intersectWith(new Interval(Long.MIN_VALUE, longValue));
                    if (i.isEmpty()) {
                        isBottom = true;
                    }
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            // smaller than X = smaller or equal X - 1 
            longValue--;
            addFacet(Facets.MAX_INCLUSIVE, "" + longValue);
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    
    public boolean facetsAccept(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
            return false;
        } 
        if (intervals.isEmpty()) return true;
        long longValue = Long.parseLong(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(longValue) && !notOneOf.contains(constant)) {
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
        if (!(range instanceof DatatypeRestrictionIntegerFin)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DatatypeRestrictionIntegerFin restr = (DatatypeRestrictionIntegerFin) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (Interval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() > Long.MIN_VALUE) {
                                intervals.add(new Interval(Long.MIN_VALUE, i.getMin()));
                            }
                            if (i.getMax() < Long.MAX_VALUE) {
                                intervals.add(new Interval(i.getMax(), Long.MAX_VALUE));
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
                                if (iNew.getMin() > Long.MIN_VALUE) {
                                    Interval newInterval = i.getCopy();
                                    long newMin = iNew.getMin() - 1;
                                    newInterval.intersectWith(new Interval(Long.MIN_VALUE, newMin));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() < Long.MAX_VALUE) {
                                    Interval newInterval = i.getCopy();
                                    long newMax = iNew.getMax() + 1; 
                                    newInterval.intersectWith(new Interval(newMax, Long.MAX_VALUE));
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
        if (notOneOf.contains(constant)) return false;
        long longValue = Long.parseLong(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(longValue)) {
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
                long not = Long.parseLong(constant.getValue());
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
            long not = Long.parseLong(constant.getValue());
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
                long constant = i.getMin();
                while (constant <= i.getMax()) {
                    DataConstant dataConstant = new DataConstant(datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant++;
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
        return DT.getSubTreeFor(DT.INTEGER).contains(constant.getDatatype());
    }
    
    public boolean canHandleAll(Set<DT> datatypes) {
        return DT.getSubTreeFor(DT.OWLREALPLUS).containsAll(datatypes);
    }
    
    public class Interval {
        long min = Long.MIN_VALUE;
        long max = Long.MAX_VALUE;
        
        public Interval() {
            super();
        }
        
        public Interval(long min, long max) {
            this.min = min;
            this.max = max;
        }
        
        public Interval getCopy() {
            return new Interval(min, max);
        }
        
        public void intersectWith(Interval i) {
            if (i.getMax() < max) {
                max = i.getMax();
            }
            if (i.getMin() > min) {
                min = i.getMin();
            }
        }
        
        public boolean isEmpty() {
            return min > max;
        }
        
        protected boolean isEmpty(long lower, long upper) {
            return lower > upper;
        }
        
        public boolean isFinite() {
            return true;
        }
        
        public boolean contains(long integer) {
            return min <= integer && max >= integer;
        }
        
        public boolean contains(Interval i) {
            return contains(i.getMin()) && contains(i.getMax());
        }
        
        public boolean disjointWith(Interval i) {
            return min >= i.getMax() || max <= i.getMin();
        }
        
        public BigInteger getCardinality() {
            if (max < min) return BigInteger.ZERO;
            return new BigInteger("" + max).subtract(new BigInteger("" + min)).add(BigInteger.ONE);
        }

        public long getMin() {
            return min;
        }

        public void setMin(long min) {
            this.min = min;
        }

        public long getMax() {
            return max;
        }

        public void setMaxIncl(long max) {
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
