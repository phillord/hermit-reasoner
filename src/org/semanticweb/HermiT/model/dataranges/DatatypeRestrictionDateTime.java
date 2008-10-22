package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.net.URI;
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

public class DatatypeRestrictionDateTime extends DatatypeRestriction {
    
    public static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    protected Set<Interval> intervals = new HashSet<Interval>();

    public DatatypeRestrictionDateTime(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
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
        return new DatatypeRestrictionDateTime(this.datatypeURI);
    }
    
    public boolean isFinite() {
        return (isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty())));
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
            for (Interval i : intervals) {
                if (i.getMinIncl() != null) {
                    hasMin = true;
                    if (i.getMinIncl().compareTo(valueInt) > 0) {
                        i.setMinIncl(valueInt);
                    }
                }
            }
            if (!hasMin) {
                intervals.add(new Interval(valueInt, null));
            }
        } break;
        case MAX_INCLUSIVE: {
            boolean hasMax = false;
            for (Interval i : intervals) {
                if (i.getMaxIncl() != null) {
                    hasMax = true;
                    if (i.getMaxIncl().compareTo(valueInt) < 0) {
                        i.setMaxIncl(valueInt);
                    }
                }
            }
            if (!hasMax) {
                intervals.add(new Interval(null, valueInt));
            }
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
        Date date;
        try {
            date = dfm.parse(constant.getValue());
            BigInteger intValue = new BigInteger("" + date.getTime());
            for (Interval i : intervals) {
                if (i.contains(intValue) && !notOneOf.contains(constant)) {
                    return true;
                }
            }
        } catch (ParseException e) {
            return false; 
        }
        return false; 
    }
    
    public void conjoinFacetsFrom(DataRange range) {
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
        if (!isBottom()) {
            DatatypeRestrictionDateTime restr = (DatatypeRestrictionDateTime) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (Interval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isFinite() || i.isEmpty()) {
                            if (i.getMinIncl() != null) {
                                intervals.add(new Interval(i.getMinIncl(), null));
                            }
                            if (i.getMaxIncl() != null) {
                                intervals.add(new Interval(null, i.getMaxIncl()));
                            }
                        } // otherwise i is trivially satisfied
                    } else {
                        intervals.addAll(restr.getIntervals());
                    }
                }
            } else {
                if (restr.isNegated()) {
                    Set<Interval> newIntervals = new HashSet<Interval>();
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
                            if (!iNew.isFinite() || iNew.isEmpty()) {
                                newIntervals.addAll(i.intersectWithNegated(iNew));
                            } else {
                                // the restrictions in restr are trivially sat
                                newIntervals.add(i);
                            }
                        }
                    }
                    intervals = newIntervals;
                } else {
                    Set<Interval> newIntervals = new HashSet<Interval>();
                    for (Interval i : intervals) {
                        for (Interval iNew : restr.getIntervals()) {
                            i.intersectWith(iNew);
                            if (!i.isEmpty()) newIntervals.add(i);
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
                return sortedOneOfs.first();
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
            if (i.getMinIncl() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" >= " + dfm.format(new Date(i.getMinIncl().longValue())));
            }
            if (i.getMaxIncl() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" <= " + dfm.format(new Date(i.getMaxIncl().longValue())));
            }
            firstRun = false;
        }
        return buffer.toString();
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "dateTime"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "dateTime"));
        return supportedDTs.contains(constant.getDatatypeURI());
    }
    
    public static boolean canHandleAll(Set<URI> datatypeURIs) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "dateTime"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "dateTime"));
        return supportedDTs.containsAll(datatypeURIs);
    }
    
    public class Interval {
        
        BigInteger minIncl = null;
        BigInteger maxIncl = null;
        
        public Interval(BigInteger minInclusive, BigInteger maxInclusive) {
            this.minIncl = minInclusive;
            this.maxIncl = maxInclusive;
        }
        
        public Set<Interval> intersectWithNegated(Interval i) {
            Set<Interval> intervals = new HashSet<Interval>();
            if (this.isEmpty()) return intervals;
            BigInteger minInclusive = i.getMinIncl();
            BigInteger maxInclusive = i.getMaxIncl();
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
                minIncl = i.getMinIncl();
            } else {
                if (i.getMinIncl() != null 
                        && i.getMinIncl().compareTo(minIncl) > 0) {
                    minIncl = i.getMinIncl();
                }
            }
        }
        
        public boolean isEmpty() {
            return (minIncl != null 
                    && maxIncl != null 
                    && minIncl.compareTo(maxIncl) > 0);
        }
        
        protected boolean isEmpty(BigInteger lower, BigInteger upper) {
            return (lower != null 
                    && upper != null 
                    && lower.compareTo(upper) > 0);
        }
        
        public boolean isFinite() {
            return minIncl != null && maxIncl != null;
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
            return contains(interval.getMinIncl()) 
            && contains(interval.getMinIncl());
        }
        
        public boolean disjointWith(Interval interval) {
            return (minIncl.compareTo(interval.getMaxIncl()) >= 0 
                    || maxIncl.compareTo(interval.getMinIncl()) <= 0);
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
                buffer.append(">= " 
                        + DatatypeRestrictionDateTime.dfm.format(new Date(minIncl.longValue())));
            }
            if (maxIncl != null) {
                if (minIncl != null) buffer.append(" ");
                buffer.append(">= " 
                        + DatatypeRestrictionDateTime.dfm.format(new Date(maxIncl.longValue())));
            }
            return buffer.toString();
        }
    }
    
    protected static class IntervalComparator implements Comparator<Interval> { 
        public static Comparator<Interval> INSTANCE = new IntervalComparator();
        public int compare(Interval i1, Interval i2) {
            return i1.getMinIncl().compareTo(i2.getMinIncl()); 
        }
    }
}
