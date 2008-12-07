/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

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
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;

/**
 * An implementation for dateTime datatypes.
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionDateTime extends DatatypeRestriction {
    
    private static final long serialVersionUID = 7185773970247024012L;

    public static DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    protected Set<DateTimeInterval> intervals = new HashSet<DateTimeInterval>();

    /**
     * Create a dateTime restriction, should use DT.DATETIME or DT.OWLDATETIME
     * @param datatype a datatype (DT.DATETIME or DT.OWLDATETIME)
     */
    public DatatypeRestrictionDateTime(DT datatype) {
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
        return new DatatypeRestrictionDateTime(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return (isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty())));
    }
    
    /**
     * Determines if if all intervals are finite. 
     * @return true if intervals are given and all intervals have a lower and an 
     * upper bound and false otherwise. 
     */
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (intervals.isEmpty()) return false;
        for (DateTimeInterval i : intervals) {
            hasOnlyFiniteIntervals  = hasOnlyFiniteIntervals  && i.isFinite();
        }
        return hasOnlyFiniteIntervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facets facet, String value) {
        Long longValue =  null;
        DateTimeInterval iNew = null;
        try {
            longValue = dfm.parse(value).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        if (facet == Facets.MIN_EXCLUSIVE) {
            longValue = DateTimeInterval.increase(longValue);
        }
        if (facet == Facets.MAX_EXCLUSIVE) {
            longValue = DateTimeInterval.decrease(longValue);
        }
        if (longValue == null) {
            throw new IllegalArgumentException("The value " + value + " is out of " +
            		"the supported range. ");
        }
        if (facet == Facets.MIN_EXCLUSIVE 
                || facet == Facets.MIN_INCLUSIVE) {
            iNew = new DateTimeInterval(longValue, null);
        } else if (facet == Facets.MAX_EXCLUSIVE 
                || facet == Facets.MAX_INCLUSIVE) {
            iNew = new DateTimeInterval(null, longValue);
        } else {
            throw new IllegalArgumentException("Unsupported facet.");
        }
        if (intervals.isEmpty()) {
            intervals.add(iNew);
        } else {
            for (DateTimeInterval i : intervals) {
                iNew.intersectWith(i);
            }
            if (iNew.isEmpty()) {
                isBottom = true;
            } else {
                intervals.clear();
                intervals.add(iNew);
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
        Date date;
        try {
            date = dfm.parse(constant.getValue());
            long longValue = date.getTime();
            for (DateTimeInterval i : intervals) {
                if (i.contains(longValue)) {
                    return true;
                }
            }
        } catch (ParseException e) {
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
        if (!(range instanceof DatatypeRestrictionDateTime)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DatatypeRestrictionDateTime restr = (DatatypeRestrictionDateTime) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (DateTimeInterval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                intervals.add(new DateTimeInterval(null, i.getMin()));
                            }
                            if (i.getMax() != null) {
                                intervals.add(new DateTimeInterval(i.getMax(), null));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        intervals = restr.getIntervals();
                    }
                }
            } else {
                Set<DateTimeInterval> newIntervals = new HashSet<DateTimeInterval>();
                if (restr.isNegated()) {
                    for (DateTimeInterval i1 : intervals) {
                        for (DateTimeInterval i2 : restr.getIntervals()) {
                            if (!i2.isEmpty()) {
                                if (i2.getMin() != null) {
                                    Long newMax = DateTimeInterval.decrease(i2.getMin());
                                    if (newMax == null) {
                                        throw new RuntimeException("The " +
                                                "minimum for a date is out " +
                                                "of range. ");
                                    }
                                    DateTimeInterval newI = new DateTimeInterval(null, newMax);
                                    newI.intersectWith(i1);
                                    if (!newI.isEmpty()) {
                                        newIntervals.add(newI);
                                    }
                                } 
                                if (i2.getMax() != null) {
                                    Long newMin = DateTimeInterval.increase(i2.getMax());
                                    if (newMin == null) {
                                        throw new RuntimeException("The " +
                                                "maximum for a date is out " +
                                                "of range. ");
                                    }
                                    DateTimeInterval newI = new DateTimeInterval(newMin, null);
                                    newI.intersectWith(i1);
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
                    if (restr.getIntervals().isEmpty()) {
                        newIntervals = intervals;
                    } else {
                        for (DateTimeInterval i1 : intervals) {
                            for (DateTimeInterval i2 : restr.getIntervals()) {
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
            for (DateTimeInterval i : intervals) {
                if (!i.isFinite()) return true;
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                try {
                    Long not = DatatypeRestrictionDateTime.dfm.parse(constant.getValue()).getTime();
                    for (DateTimeInterval i : intervals) {
                        if (i.contains(not)) {
                            rangeSize = rangeSize.subtract(BigInteger.ONE);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Unparsable date encountered. ");
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
            for (DateTimeInterval i : intervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                try {
                    Long not = DatatypeRestrictionDateTime.dfm.parse(constant.getValue()).getTime();
                    for (DateTimeInterval i : intervals) {
                        if (i.contains(not)) {
                            rangeSize = rangeSize.subtract(BigInteger.ONE);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Unparsable date encountered. ");
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
                SortedSet<DateTimeInterval> sortedIntervals 
                        = new TreeSet<DateTimeInterval>(IntervalComparator.INSTANCE);
                sortedIntervals.addAll(intervals);
                for (DateTimeInterval i : sortedIntervals) {
                    long constant = i.getMin();
                    while (constant < i.getMax()) {
                        DataConstant dataConstant = new DataConstant(
                                Impl.IDateTime, datatype, 
                                "" + dfm.format(new Date(constant)));
                        if (!notOneOf.contains(dataConstant)) return dataConstant;
                        constant++;
                    }
                    // now we have the maximal supported date ans any further 
                    // increase will resuslt in a funny number due to overflow
                    DataConstant dataConstant = new DataConstant(
                            Impl.IDateTime, datatype, 
                            "" + dfm.format(new Date(constant)));
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the intervals that describe the possible values for this 
     * datatype. 
     * @return a set of intervals
     */
    public Set<DateTimeInterval> getIntervals() {
        return intervals;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Namespaces)
     */
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (DateTimeInterval i : intervals) {
            if (!firstRun && !isNegated) {
                buffer.append(" or ");
            }
            if (i.getMin() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" >= " + dfm.format(new Date(i.getMin())));
            }
            if (i.getMax() != null) {
                if (isNegated) buffer.append(" or ");
                buffer.append(" <= " + dfm.format(new Date(i.getMax())));
            }
            firstRun = false;
        }
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return constant.getImplementation() == Impl.IDateTime;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        Set<DT> supportedDTs = new HashSet<DT>();
        supportedDTs.add(DT.DATETIME);
        supportedDTs.add(DT.OWLDATETIME);
        return supportedDTs.contains(datatype);
    }

    /**
     * A comparator that can be used to order the intervals according to their 
     * min values. We assume here that all intervals are disjoint. 
     * @author BGlimm
     */
    protected static class IntervalComparator implements Comparator<DateTimeInterval> { 
        
        public static Comparator<DateTimeInterval> INSTANCE = new IntervalComparator();
        
        public int compare(DateTimeInterval i1, DateTimeInterval i2) {
            if (i1.getMin() == i2.getMin()) return 0;
            return (i1.getMin() - i2.getMin() > 0) ? 1 : -1; 
        }
    }
}
