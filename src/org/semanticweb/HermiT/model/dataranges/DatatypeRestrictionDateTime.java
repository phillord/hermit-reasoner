package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionDateTime extends DatatypeRestriction {
    
    public static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    protected Set<Interval> intervals = new HashSet<Interval>();

    public DatatypeRestrictionDateTime() {
        this.datatypeURI = XSDVocabulary.DATE_TIME.getURI();
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
        return new DatatypeRestrictionDateTime();
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (intervals.isEmpty()) return false;
        for (Interval i : intervals) {
            if (i.getMaxIncl() == null || i.getMinIncl() == null) {
                hasOnlyFiniteIntervals = false;
            }
        }
        return hasOnlyFiniteIntervals;
    }
    
    public boolean isFinite() {
        return (isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty())));
    }
    
    public boolean hasMinCardinality(int n) {
        if (n <= 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (oneOf.size() >= n);
            }
            BigInteger nBig = new BigInteger("" + n);
            BigInteger subtract = BigInteger.ZERO;
            BigInteger rangeSize = BigInteger.ZERO;
            for (Interval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (Interval i : intervals) {
                    if (i.contains(not)) {
                        subtract = subtract.subtract(BigInteger.ONE);
                    }
                }
            }
            rangeSize = rangeSize.subtract(subtract);
            return (rangeSize.compareTo(nBig) >= 0);
        }
        return true;
    }
    
    public BigInteger getEnumerationSize() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            BigInteger subtract = BigInteger.ZERO;
            BigInteger rangeSize = BigInteger.ZERO;
            for (Interval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (Interval i : intervals) {
                    if (i.contains(not)) {
                        subtract = subtract.subtract(BigInteger.ONE);
                    }
                }
            }
            return rangeSize.subtract(subtract);
        } 
        return null;
    }

    public DataConstant getSmallestAssignment() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                for (DataConstant constant : sortedOneOfs) {
                    if (!notOneOf.contains(constant)) return constant;
                }
                return null;
            }
            SortedSet<Interval> sortedIntervals = new TreeSet<Interval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (Interval i : sortedIntervals) {
                BigInteger constant = i.getMinIncl();
                while (constant.compareTo(i.getMaxIncl()) <= 0) {
                    DataConstant dataConstant = new DataConstant(datatypeURI, "" + dfm.format(new Date(constant.longValue())));
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = constant.add(BigInteger.ONE);
                }
            }
        }
        return null;
    }
    
    protected static class IntervalComparator implements Comparator<Interval> { 
        public static Comparator<Interval> INSTANCE = new IntervalComparator();
        public int compare(Interval i1, Interval i2) {
            return i1.getMinIncl().compareTo(i2.getMinIncl()); 
        }
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        try {
            Date dateValue = dfm.parse(constant.getValue());
            BigInteger intValue = new BigInteger("" + dateValue.getTime());
            for (Interval i : intervals) {
                if (i.contains(intValue) && !notOneOf.contains(constant)) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; 
    }

    public boolean addOneOf(DataConstant constant) {
        if (!intervals.isEmpty()) {
            throw new RuntimeException("Can only add oneOfs if no facets are present. ");
        }
        boolean result = false;
        try {
         // type check
            dfm.parse(constant.getValue());
            return oneOf.add(constant);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return result;
    }
    
    public void setOneOf(Set<DataConstant> oneOf) {
        if (!intervals.isEmpty()) {
            throw new RuntimeException("Can only add oneOfs if no facets are present. ");
        }
        this.oneOf = oneOf;
    }
    
    public boolean removeOneOf(DataConstant constant) {
        boolean contained = oneOf.remove(constant);
        if (contained && oneOf.isEmpty()) {
            // it does not mean it can have arbitrary values now, but rather it 
            // is bottom if not negated and top if negated, so we have to swap 
            // negation values
            isBottom = true;
        }
        return contained;
    }
    
    public void conjoinFacetsFrom(CanonicalDataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof DatatypeRestrictionDateTime)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionDateTime. It is " +
                    "only allowed to add facets from other date time " +
                    "datatype restrictions. ");
        }
        if (!isBottom) {
            DatatypeRestrictionDateTime restr = (DatatypeRestrictionDateTime) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                intervals.addAll(restr.getIntervals());
            } else {
                if (restr.isNegated()) {
                    Set<Interval> newIntervals = new HashSet<Interval>();
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
                            newIntervals.addAll(i.intersectWithNegated(iNew.getMinIncl(), iNew.getMaxIncl()));
                        }
                    }
                    intervals = newIntervals;
                } else {
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
                            i.intersectWith(iNew);
                        }
                    }
                }
            }
            // remove empty ranges
            Set<Interval> emptyIntervals = new HashSet<Interval>();
            for (Interval i : intervals) {
                if (i.isEmpty()) emptyIntervals.add(i);
            }
            if (!emptyIntervals.isEmpty()) {
                intervals.removeAll(emptyIntervals);
                if (emptyIntervals.isEmpty()) isBottom = true;
            }
        }
    }
    
    public void addFacet(Facets facet, String value) {
        BigInteger valueInt = null;
        try {
            valueInt = new BigInteger("" + dfm.parse(value).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            if (isNegated) {
                // not greater or equal X = smaller or equal X - 1
                valueInt = valueInt.subtract(BigInteger.ONE);
                addNegatedFacet(Facets.MAX_INCLUSIVE, valueInt);
            } else {
                // greater or equal X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(valueInt, null));
                } else {
                    for (Interval i : intervals) {
                        if (i.getMinIncl() == null 
                                || i.getMinIncl().compareTo(valueInt) < 0) {
                            i.setMinIncl(valueInt);
                        }
                        if (i.isEmpty()) {
                            isBottom = true;
                        }
                    }
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            if (isNegated) {
                // not greater X = smaller or equal X
                addNegatedFacet(Facets.MAX_INCLUSIVE, valueInt);
            } else {
                // greater than X = greater or equal X + 1
                valueInt = valueInt.add(BigInteger.ONE);
                addFacet(Facets.MIN_INCLUSIVE, valueInt.toString());
            }
        } break;
        case MAX_INCLUSIVE: {
            if (isNegated) {
                // not smaller or equal X = greater or equal X - 1
                valueInt = valueInt.add(BigInteger.ONE);
                addNegatedFacet(Facets.MIN_INCLUSIVE, valueInt);
            } else {
                // smaller or equal X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(null, valueInt));
                } else {
                    for (Interval i : intervals) {
                        if (i.getMaxIncl() == null 
                                || i.getMaxIncl().compareTo(valueInt) > 0) {
                            i.setMaxIncl(valueInt);
                        }
                        if (i.isEmpty()) isBottom = true;
                    }
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            if (isNegated) {
                // not smaller than X => greater or equal X
                addNegatedFacet(Facets.MIN_INCLUSIVE, valueInt);
            } else {
                // smaller than X = smaller or equal X - 1 
                valueInt = valueInt.subtract(BigInteger.ONE);
                addFacet(Facets.MAX_INCLUSIVE, valueInt.toString());
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    
    protected void addNegatedFacet(Facets facet, BigInteger valueInt) {
        switch (facet) {
        case MIN_INCLUSIVE: {
            boolean hasMin = false;
            boolean isTop = false;
            for (Interval i : intervals) {
                if (i.getMinIncl() != null) {
                    hasMin = true;
                    if (i.getMinIncl().compareTo(valueInt) > 0) {
                        i.setMinIncl(valueInt);
                    }
                } else {
                    if (i.getMaxIncl().compareTo(valueInt) >= 0) {
                        isTop = true;
                    }
                }
            }
            if (isTop) {
                intervals.clear();
            } else {
                if (!hasMin) {
                    intervals.add(new Interval(valueInt, null));
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            boolean hasMax = false;
            boolean isTop = false;
            for (Interval i : intervals) {
                if (i.getMaxIncl() != null) {
                    hasMax = true;
                    if (i.getMaxIncl().compareTo(valueInt) < 0) {
                        i.setMaxIncl(valueInt);
                    }
                } else {
                    if (i.getMinIncl().compareTo(valueInt) <= 0) {
                        isTop = true;
                    }
                }
            }
            if (isTop) {
                intervals.clear();
            } else {
                if (!hasMax) {
                    intervals.add(new Interval(null, valueInt));
                }
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    public Set<Interval> getIntervals() {
        return intervals;
    }

    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (isNegated) buffer.append("not ");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        boolean firstRun = true;
        for (Interval i : intervals) {
            if (i.getMinIncl() != null) {
                buffer.append(" >= " + dfm.format(new Date(i.getMinIncl().longValue())));
            }
            if (i.getMaxIncl() != null) {
                buffer.append(" <= " + dfm.format(new Date(i.getMaxIncl().longValue())));
            }
            if (!firstRun) {
                buffer.append(" or ");
            }
            firstRun = false;
        }
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append(" OneOf(");
            firstRun = true;
            for (DataConstant constant : oneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(" " + constant);
            }
            buffer.append(")");
        }
        if (!notOneOf.isEmpty()) {
            buffer.append(" not OneOf(");
            firstRun = true;
            for (DataConstant constant : notOneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(" not " + constant);
            }
            buffer.append(")");
        }
        buffer.append(")");
        return buffer.toString();        
    }
    
    public class Interval {
        
        BigInteger minIncl = null;
        BigInteger maxIncl = null;
        
        public Interval(BigInteger minInclusive, BigInteger maxInclusive) {
            this.minIncl = minInclusive;
            this.maxIncl = maxInclusive;
        }
        
        public Set<Interval> intersectWithNegated(BigInteger minInclusive, BigInteger maxInclusive) {
            Set<Interval> intervals = new HashSet<Interval>();
            if (minIncl.compareTo(maxIncl) > 0) return intervals;
            if ((maxInclusive != null 
                    && maxIncl != null 
                    && maxInclusive.compareTo(maxIncl) > 0) 
                    || (minInclusive != null 
                            && minIncl != null 
                            && minInclusive.compareTo(minIncl) < 0)) {
                intervals.add(new Interval(minIncl, maxIncl));
                return intervals;
            }
            if (minInclusive != null) {
                if (!isEmpty(minInclusive, maxIncl)) {
                    intervals.add(new Interval(minInclusive, maxIncl));
                }
            }
            if (maxInclusive != null) {
                if (!isEmpty(minIncl, maxInclusive)) {
                    intervals.add(new Interval(minIncl, maxInclusive));
                }
            }
            return intervals;
        }
        
        public void intersectWith(Interval i) {
            if (maxIncl == null) {
                maxIncl = i.getMaxIncl();
            } else {
                if (i.getMaxIncl() != null 
                        && i.getMaxIncl().compareTo(maxIncl) < 0) {
                    maxIncl = i.getMaxIncl();
                }
            }
            if (minIncl == null) {
                minIncl = i.getMaxIncl();
            } else {
                if (i.getMinIncl() != null 
                        && i.getMinIncl().compareTo(minIncl) > 0) {
                    minIncl = i.getMinIncl();
                }
            }
        }
        
        public boolean isEmpty() {
            if (minIncl == null && maxIncl == null) return true;
            if (minIncl != null && maxIncl != null) {
                return minIncl.compareTo(maxIncl) > 0;
            } else return false;
        }
        
        protected boolean isEmpty(BigInteger lower, BigInteger upper) {
            if (lower == null && upper == null) return true;
            if (lower != null && upper != null) {
                return lower.compareTo(upper) <= 0;
            } else return false;
        }
        
        public boolean contains(BigInteger integer) {
            boolean contains = true;
            if (minIncl != null) {
                contains = contains && (minIncl.compareTo(integer) <= 0);
            }
            if (maxIncl != null) {
                contains = contains && (maxIncl.compareTo(integer) >= 0);
            }
            return contains;
        }
        
        public boolean contains(Interval interval) {
            return (minIncl.compareTo(interval.minIncl) >= 0 && maxIncl.compareTo(interval.maxIncl) <= 0);
        }
        
        public boolean disjointWith(Interval interval) {
            return (minIncl.compareTo(interval.maxIncl) >= 0 || maxIncl.compareTo(interval.minIncl) <= 0);
        }
        
        public BigInteger getCardinality() {
            if (maxIncl.compareTo(minIncl) < 0) return BigInteger.ZERO;
            return maxIncl.subtract(minIncl).add(BigInteger.ONE);
        }

        public BigInteger getMinIncl() {
            return minIncl;
        }

        public void setMinIncl(BigInteger minIncl) {
            this.minIncl = minIncl;
        }

        public BigInteger getMaxIncl() {
            return maxIncl;
        }

        public void setMaxIncl(BigInteger maxIncl) {
            this.maxIncl = maxIncl;
        }
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (minIncl != null) {
                buffer.append(">= " + DatatypeRestrictionDateTime.dfm.format(new Date(minIncl.longValue())));
            }
            if (maxIncl != null) {
                if (minIncl != null) buffer.append(" ");
                buffer.append(">= " + DatatypeRestrictionDateTime.dfm.format(new Date(maxIncl.longValue())));
            }
            return buffer.toString();
        }
    }
}
