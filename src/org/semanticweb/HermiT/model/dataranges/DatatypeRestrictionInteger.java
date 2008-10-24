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

public class DatatypeRestrictionInteger extends DatatypeRestriction {
    
    protected Set<Interval> intervals = new HashSet<Interval>();
   
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
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionInteger(this.datatype);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (intervals.isEmpty()) return false;
        for (Interval i : intervals) {
            if (i.getMax() == null || i.getMin() == null) {
                hasOnlyFiniteIntervals = false;
            }
        }
        return hasOnlyFiniteIntervals;
    }
    
    public void addFacet(Facets facet, String value) {
        BigInteger valueInt = null;
        try {
            BigDecimal bd = new BigDecimal(value);
            if (facet == Facets.MIN_EXCLUSIVE || facet == Facets.MAX_INCLUSIVE) {
                bd = bd.setScale(0, BigDecimal.ROUND_FLOOR);
                valueInt = bd.toBigInteger();
            } else {
                bd = bd.setScale(0, BigDecimal.ROUND_CEILING);
                valueInt = bd.toBigInteger();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            // greater or equal X
            if (intervals.isEmpty()) {
                intervals.add(new Interval(valueInt, null));
            } else {
                for (Interval i : intervals) {
                    i.intersectWith(new Interval(valueInt, null));
                    if (i.isEmpty()) {
                        isBottom = true;
                    }
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            // greater than X = greater or equal X + 1
            valueInt = valueInt.add(BigInteger.ONE);
            addFacet(Facets.MIN_INCLUSIVE, valueInt.toString());
        } break;
        case MAX_INCLUSIVE: {
            // smaller or equal X
            if (intervals.isEmpty()) {
                intervals.add(new Interval(null, valueInt));
            } else {
                for (Interval i : intervals) {
                    i.intersectWith(new Interval(null, valueInt));
                    if (i.isEmpty()) {
                        isBottom = true;
                    }
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            // smaller than X = smaller or equal X - 1 
            valueInt = valueInt.subtract(BigInteger.ONE);
            addFacet(Facets.MAX_INCLUSIVE, valueInt.toString());
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
        BigInteger intValue = new BigInteger(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(intValue) && !notOneOf.contains(constant)) {
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
        if (!(range instanceof DatatypeRestrictionInteger)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DatatypeRestrictionInteger restr = (DatatypeRestrictionInteger) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (Interval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                intervals.add(new Interval(null, i.getMin()));
                            }
                            if (i.getMax() != null) {
                                intervals.add(new Interval(i.getMax(), null));
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
                                if (iNew.getMin() != null) {
                                    Interval newInterval = i.getCopy();
                                    newInterval.intersectWith(new Interval(null, iNew.getMin().subtract(BigInteger.ONE)));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() != null) {
                                    Interval newInterval = i.getCopy();
                                    newInterval.intersectWith(new Interval(iNew.getMax().add(BigInteger.ONE), null));
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
        BigInteger intValue = new BigInteger(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(intValue) && !notOneOf.contains(constant)) {
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
                BigInteger not = new BigInteger(constant.getValue());
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
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            BigInteger rangeSize = BigInteger.ZERO;
            for (Interval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (Interval i : intervals) {
                    if (i.contains(not)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return rangeSize;
        } 
        return null;
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
                BigInteger constant = i.getMin();
                while (constant.compareTo(i.getMax()) <= 0) {
                    DataConstant dataConstant = new DataConstant(datatype, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = constant.add(BigInteger.ONE);
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
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.INTEGER).contains(constant.getDatatype());
    }
    
    public boolean canHandleAll(Set<DT> datatypes) {
        return DT.getSubTreeFor(DT.OWLREALPLUS).containsAll(datatypes);
    }
    
    public class Interval {
        BigInteger min = null;
        BigInteger max = null;
        
        public Interval(BigInteger minInclusive, BigInteger maxInclusive) {
            this.min = minInclusive;
            this.max = maxInclusive;
        }
        
        public Interval getCopy() {
            return new Interval(min, max);
        }
        
        public void intersectWith(Interval i) {
            if (max == null) {
                max = i.getMax();
            } else {
                if (i.getMax() != null 
                        && i.getMax().compareTo(max) < 0) {
                    max = i.getMax();
                }
            }
            if (min == null) {
                min = i.getMin();
            } else {
                if (i.getMin() != null 
                        && i.getMin().compareTo(min) > 0) {
                    min = i.getMin();
                }
            }
        }
        
        public boolean isEmpty() {
            return (min != null 
                    && max != null 
                    && min.compareTo(max) > 0);
        }
        
        protected boolean isEmpty(BigInteger lower, BigInteger upper) {
            return (lower != null 
                    && upper != null 
                    && lower.compareTo(upper) > 0);
        }
        
        public boolean isFinite() {
            return min != null && max != null;
        }
        
        public boolean contains(BigInteger integer) {
            boolean contains = true;
            if (min != null) {
                contains = contains && (min.compareTo(integer) <= 0);
            }
            if (max != null) {
                contains = contains && (max.compareTo(integer) >= 0);
            }
            return contains;
        }
        
        public boolean contains(Interval interval) {
            return contains(interval.getMin()) 
                    && contains(interval.getMax());
        }
        
        public boolean disjointWith(Interval interval) {
            return (min.compareTo(interval.getMax()) >= 0 
                    || max.compareTo(interval.getMin()) <= 0);
        }
        
        public BigInteger getCardinality() {
            if (max.compareTo(min) < 0) return BigInteger.ZERO;
            return max.subtract(min).add(BigInteger.ONE);
        }

        public BigInteger getMin() {
            return min;
        }

        public void setMin(BigInteger min) {
            this.min = min;
        }

        public BigInteger getMax() {
            return max;
        }

        public void setMaxIncl(BigInteger max) {
            this.max = max;
        }
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (min != null) {
                buffer.append(">= " + min);
            }
            if (max != null) {
                if (min != null) buffer.append(" ");
                buffer.append("<= " + max);
            }
            return buffer.toString();
        }
    }
    
    protected static class IntervalComparator implements Comparator<Interval> { 
        public static Comparator<Interval> INSTANCE = new IntervalComparator();
        public int compare(Interval i1, Interval i2) {
            return i1.getMin().compareTo(i2.getMin()); 
        }
    }
}
