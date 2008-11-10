/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger.IntervalComparator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * An implementation for datatype restrictions of hex encoded binary data. 
 * @author BGlimm
 */
public class DatatypeRestrictionHexBinary 
        extends DatatypeRestriction 
        implements IntegerFacet {
    
    private static final long serialVersionUID = 5175603770168472124L;
    
    protected Set<IntegerInterval> intervals = new HashSet<IntegerInterval>();
    protected Automaton patternMatcher = null;
    protected boolean patternContainsFacets = false;
    
    /**
     * Create an instance of the datatype restriction for hex encoded binary 
     * data.  
     * @param datatype a datatype (should be DT.HEXBINARY)
     */
    public DatatypeRestrictionHexBinary(DT datatype) {
        this.datatype = datatype;
        supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.LENGTH, 
                        Facets.MIN_LENGTH, 
                        Facets.MAX_LENGTH
                })
        );
        intervals.add(new IntegerIntervalFin(0l, null));
        patternMatcher = new RegExp("([0-9a-f][0-9a-f])*").toAutomaton();
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionHexBinary(this.datatype);
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
        if (facet == Facets.LENGTH) {
            addFacet(Facets.MIN_LENGTH, value);
            addFacet(Facets.MAX_LENGTH, value);
            return;
        }
        IntegerInterval iNew = null;
        try {
            BigInteger bi = new BigInteger(value);
            if (facet == Facets.MIN_LENGTH) {
                if (IntegerIntervalFin.isLong(bi)) {
                    iNew = new IntegerIntervalFin(bi.longValue(), null);
                } else {
                    iNew = new IntegerIntervalBig(bi, null);
                }
            } else if (facet == Facets.MAX_LENGTH) {
                if (IntegerIntervalFin.isLong(bi)) {
                    iNew = new IntegerIntervalFin(0l, bi.longValue());
                } else {
                    iNew = new IntegerIntervalBig(BigInteger.ZERO, bi);
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
        if (!(constant.getImplementation() == Impl.IHexBinary)) {
            return false;
        }
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        if (patternContainsFacets) {
            return patternMatcher.run(constant.getValue().toLowerCase());
        } else {
            if (!notOneOf.isEmpty() && notOneOf.contains(constant)) {
                return false;
            } 
            // initial patternMatcher just checks that the value is valid hex
            if (!patternMatcher.run(constant.getValue().toLowerCase())) {
                return false;
            }
            if (intervals.isEmpty()) return true;
            String value = constant.getValue();
            for (IntegerInterval i : intervals) {
                if (i.contains(value.length() / 2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Encodes the length restrictions given by the facets and the unsuitable 
     * assignments (notOneOf) into an automaton that can then be used to 
     * generate suitable assignments for this restriction. It is mainly a kind 
     * of caching, so that we don't construct the automaton each time we need 
     * assignments. 
     */
    protected void compileFacetsIntoPattern() {
        if (!intervals.isEmpty()) {
            SortedSet<IntegerInterval> sortedIntervals 
                    = new TreeSet<IntegerInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (IntegerInterval i : sortedIntervals) {
                if (IntegerIntervalFin.isInt(new BigInteger("" + i.getMin())) 
                    && IntegerIntervalFin.isInt(new BigInteger("" + i.getMax()))) {
                    // intervals are finite since otherwise we do not generate 
                    // assignments
                    String pattern = "([0-9a-f][0-9a-f]){" + i.getMin() + "," + i.getMax() + "}";
                    patternMatcher = new RegExp(pattern).toAutomaton();
                    for (DataConstant d : notOneOf) {
                        patternMatcher = patternMatcher.minus(Automaton.makeString(d.getValue()));
                    }
                } else {
                    // numbers too big
                    throw new RuntimeException("The range is too big to be captured by an automaton. ");
                }
            }
        }
        patternContainsFacets = true;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.dataranges.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof DatatypeRestrictionHexBinary)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionHexBinary. It is " +
                    "only allowed to add facets from other hexBinary " +
                    "datatype restrictions. ");
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
                                intervals.add(i.getInstance(i.getZero(), i.getMin()));
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
                                    IntegerInterval newI = i2.getInstance(i2.getZero(), i2.decreasedMin());
                                    newI = newI.intersectWith(i1);
                                    if (!newI.isEmpty()) {
                                        newIntervals.add(newI);
                                    }
                                } 
                                if (i2.getMax() != null) {
                                    IntegerInterval newI = i2.getInstance(i2.increasedMax(), null);
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
            // compute proper size
            BigInteger rangeSize = BigInteger.ZERO;
            for (IntegerInterval i : intervals) {
                BigInteger num = differentData(i.getMin(), i.getMax());
                // too big values
                if (num == null) {
                    return true;
                } else {
                    rangeSize = rangeSize.add(num);
                }
            }
            for (DataConstant constant : notOneOf) {
                String not = constant.getValue();
                for (IntegerInterval i : intervals) {
                    if (!i.contains(not.length() / 2)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return (rangeSize.compareTo(new BigInteger("" + n)) >= 0);
        }
        return true;
    }
    
    /**
     * Compute how many different values we can have, given the minimal and 
     * maximal number of bytes, i.e., how many strings in hex encoding can we 
     * produce that encode at least minLength bytes and at most maxLength 
     * bytes. We use the geometrical series formular to compute this number. 
     * @param minLength the minimal number of bytes that the strings we are 
     *                  interested in must be able to encode
     * @param maxLength the maximal number of bytes that the strings we are 
     *                  interested in are able to encode
     * @return the number of strings the represent hex encoded data with at 
     *         least minLength bytes and at most maxLength bytes
     */
    protected BigInteger differentData(Number minLength, Number maxLength) {
        if (minLength instanceof BigInteger 
                || maxLength instanceof BigInteger
                || (Long) minLength > Integer.MAX_VALUE 
                || (Long) maxLength >= Integer.MAX_VALUE - 1) {
            // too big
            return null;
        } else {
            int min = minLength.intValue();
            int max = maxLength.intValue();
            // our alphabet has 256 chars (hex tuples), i.e., |alphabet| = 256
            // compute how many words of length up to maxLength we can build
            // use geometrical series formula
            // sum_{i=min}^{max} 256^i = (256^min - 256^{max+1}) / (1 - 256) 
            BigInteger sa = new BigInteger("256");
            BigInteger divider = new BigInteger("-255");
            return (sa.pow(min).subtract(sa.pow(max+1))).divide(divider);
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            // compute proper size
            BigInteger rangeSize = BigInteger.ZERO;
            for (IntegerInterval i : intervals) {
                BigInteger num = differentData(i.getMin(), i.getMax());
                // too big values
                if (num == null) {
                    return null;
                } else {
                    rangeSize = rangeSize.add(num);
                }
            }
            int minus = 0;
            for (DataConstant constant : notOneOf) {
                String not = constant.getValue();
                for (IntegerInterval i : intervals) {
                    if (i.contains(not.length() / 2)) {
                        minus++;
                    }
                }
            }
            rangeSize = rangeSize.subtract(new BigInteger("" + minus));
            return rangeSize;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#notOneOf(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean notOneOf(DataConstant constant) {
        boolean result = true;
        if (!oneOf.isEmpty()) {
            result = oneOf.remove(constant);
            if (oneOf.isEmpty()) isBottom = true;
        } else {
            if (patternContainsFacets) {
                patternMatcher = patternMatcher.minus(Automaton.makeString(constant.getValue()));
            } else {
                result = notOneOf.add(constant);
            }
        }
        return result;
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
            if (!patternContainsFacets) {
                compileFacetsIntoPattern();
            }
            while (!patternMatcher.isEmpty()) {
                String value = patternMatcher.getShortestExample(true);
                return new DataConstant(Impl.IHexBinary, datatype, value);
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
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.HEXBINARY).contains(constant.getDatatype());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.HEXBINARY).contains(datatype);
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
}
