/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.datatypes.DataConstant.Impl;
import org.semanticweb.HermiT.datatypes.DatatypeRestrictionInteger.IntervalComparator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * This class represents datatype restrictions for the base64 encoded binary 
 * data. It assumes that all binary data is normalised, i.e., all whitespace is 
 * removed. The facets are regarding the number of bytes that are encoded in the 
 * binary data. 
 * A general note about base64 encoding: 
 * We have 64 characters (a-z, A-Z, 0-9, +,/) that are allowed to occur in a 
 * string of a data constant for the base64binary data types. Plus padding (=) 
 * and whitespace, but this does not count when we compute the length for the 
 * facets. 
 * Each one letter sequence encodes 6 bits (2^6 = 64). That is a bit strange, 
 * since we encode octets, i.e., bytes = chunks of 8 bits. Therefore, base64 
 * encoded binary data comes in blocks of 4 characters and the last block might 
 * have padding characters if not all 24 bits = 3 bytes are needed. 
 * For the facets, we are counting bytes (bit octets) and disregard all 
 * whitespace. 
 * So, we remove the padding at the end, and do integer devision by 4 to find 
 * how many complete 3 byte chunks we have. Then we check whether the last block 
 * is a complete one by checking whether numOfChars mod 4 leaves a remainder. If 
 * so, the remainder can be 2 or 3 (if 1 it was not valid base64), which means 
 * we have to add 1 or 2 bytes respectively to the overall number of bytes. 
 * @author BGlimm
 */
public class DatatypeRestrictionBase64Binary 
        extends DatatypeRestriction 
        implements IntegerFacet {
    
    private static final long serialVersionUID = -8204147646238627413L;
    
    protected Set<IntegerInterval> intervals = new HashSet<IntegerInterval>();
    protected Automaton patternMatcher = null;
    protected boolean patternContainsFacets = false;
    
    /**
     * Create a restriction for base64 encoded binary data. 
     * @param datatype this restriction can only be used for DT.BASE64BINARY
     */
    public DatatypeRestrictionBase64Binary(DT datatype) {
        this.datatype = datatype;
        supportedFacets = new HashSet<Facet>(
                Arrays.asList(new Facet[] {
                        Facet.LENGTH, 
                        Facet.MIN_LENGTH, 
                        Facet.MAX_LENGTH
                })
        );
        intervals.add(new IntegerIntervalFin(0l, null));
        String threeBytes = "([A-Za-z0-9+/][A-Za-z0-9+/][A-Za-z0-9+/][A-Za-z0-9+/])";
        String twoBytes = "([A-Za-z0-9+/][A-Za-z0-9+/][AEIMQUYcgkosw048]=)";
        String oneByte = "([A-Za-z0-9+/][AQgw]==)";
        String base64pattern = "((" + threeBytes + "*(" + threeBytes 
        + "|" + twoBytes + "|" + oneByte + "))?)";
        patternMatcher = new RegExp(base64pattern).toAutomaton();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionBase64Binary(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    /**
     * @return true if intervals are given and all intervals have both a lower 
     * and an upper bound and false otherwise.
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
    public void addFacet(Facet facet, String value) {
        if (facet == Facet.LENGTH) {
            addFacet(Facet.MIN_LENGTH, value);
            addFacet(Facet.MAX_LENGTH, value);
            return;
        }
        IntegerInterval iNew = null;
        try {
            BigInteger bi = new BigInteger(value);
            if (facet == Facet.MIN_LENGTH) {
                if (IntegerIntervalFin.isLong(bi)) {
                    iNew = new IntegerIntervalFin(bi.longValue(), null);
                } else {
                    iNew = new IntegerIntervalBig(bi, null);
                }
            } else if (facet == Facet.MAX_LENGTH) {
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
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        } 
        if (!patternContainsFacets) {
            compileFacetsIntoPattern();
        }
        return patternMatcher.run(constant.getValue());
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
                    String maxOneByte = "(([A-Za-z0-9+/]{2})==)";
                    String threeBytes = "([A-Za-z0-9+/]{4})";
                    String anyThree = "([A-Za-z0-9+/=]{4})";

                    // patternMin captures those that are too short and thus 
                    // below the min length. We are negating that one later.
                    String patternMin = "";
                    int minBytes = 0;
                    if (i.getMin().intValue() > 0) {
                        minBytes = i.getMin().intValue();
                        if (bytesInIncompleteChunk(minBytes) == 1) {
                            patternMin = "~(" + anyThree + "{0," + numFullChunks(minBytes) + "})";
                        } else if (bytesInIncompleteChunk(minBytes) == 2) {
                            patternMin = "~((" + anyThree + "{0," + numFullChunks(minBytes) + "})" + maxOneByte + "?)";
                        } else {
                            patternMin = threeBytes + "{" + numFullChunks(minBytes) + ",}";
                        }
                    } 
                    
                    // pattern for the max length
                    int maxBytes = i.getMax().intValue();
                    String patternMax = "";
                    if (bytesInIncompleteChunk(maxBytes) == 2) {
                        patternMax = patternMax + "~(" + threeBytes + "{" + (numFullChunks(maxBytes) + 1) + ",})";
                    } else if (bytesInIncompleteChunk(maxBytes) == 1) {
                        patternMax = anyThree + "{0," + numFullChunks(maxBytes) + "}" + maxOneByte + "?";
                    } else {
                        patternMax = anyThree + "{0," + numFullChunks(maxBytes) + "}";
                    }
                    // create an automaton that accepts base64 data and 
                    // conforms to the maxLength conditions captured by the 
                    // maxA automaton and does not accept values that are 
                    // of length below min length as captured by the minA 
                    // automaton
                    Automaton minA = new RegExp(patternMin).toAutomaton();
                    Automaton maxA = new RegExp(patternMax).toAutomaton();
                    patternMatcher = patternMatcher.intersection(maxA.intersection(minA));
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
        if (!(range instanceof DatatypeRestrictionBase64Binary)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionBase64Binary. It is " +
                    "only allowed to add facets from other base64Binary " +
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
                    if (!i.contains(numOfBytes(not))) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return (rangeSize.compareTo(new BigInteger("" + n)) >= 0);
        }
        return true;
    }
    
    /**
     * Count how many bytes are encoded in the given string.
     * @param base64String a string representing base64 encoded binary data
     * @return the number of bytes encoded by the input string
     */
    protected int numOfBytes(String base64String) {
        char[] chars = base64String.toCharArray();
        int numChars = chars.length;
        if (numChars % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded " +
            		"input string is not a multiple of 4.");
        }
        // remove trailing padding chars (=)
        while (numChars > 0 && chars[numChars-1] == '=') numChars--;
        // each char represents 6 bits, so a block of 4 is 3 bytes
        // rounding will automatically handle the last possibly incomplete chunk
        return (numChars * 3) / 4;
    }
    
    /**
     * Compute how many different values we can have, given the minimal and 
     * maximal numbers of bytes, i.e., how many strings in base64 encoding 
     * can we produce that encode at least minLength bytes and at most maxLength 
     * bytes. We use the geometrical series formular to compute this number. 
     * @param minLength the minimal number of bytes that the strings we are 
     *                  interested in must be able to encode
     * @param maxLength the maximal number of bytes that the strings we are 
     *                  interested in are able to encode
     * @return the number of strings the represent base64 encoded data with at 
     *         least minLength bytes and at most maxLength bytes
     */
    protected BigInteger differentData(Number minLength, Number maxLength) {
        // for counting bytes (with 8 bits) with minLength n and maxLength m: 
        // 2^{m*8} - 2^{n*8} = (2^m)^8 - (2^n)^8 
        if (minLength instanceof BigInteger 
                || maxLength instanceof BigInteger
                || (Long) minLength > Integer.MAX_VALUE 
                || (Long) maxLength > Integer.MAX_VALUE - 1) {
            // too big
            return null;
        } else {
            int min = minLength.intValue();
            int max = maxLength.intValue();
            // our alphabet has 256 chars (each byte of 8 bits can encode 256 
            // values), i.e., |alphabet| = 256
            // compute how many wordsof length up to maxLength we can build
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
            for (DataConstant constant : notOneOf) {
                String not = constant.getValue();
                for (IntegerInterval i : intervals) {
                    if (!i.contains(numOfBytes(not))) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
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
                return new DataConstant(Impl.IBase64Binary, datatype, value);
            }
        }
        return null;
    }
    
    /**
     * Compute how many complete base64 encoded 4 character chunks are needed in 
     * order to encode the given number of bytes (uses integer division by 3)
     * @param bytes the number of bytes we want to encode
     * @return the number of base64 encoded 4 character chunks are required
     */
    protected int numFullChunks(int bytes) {
        return bytes / 3; 
    }
    
    /**
     * Compute how many bytes we have for the last possibly incomplete 3 byte 
     * chunk. 
     * @param bytes the number of bytes to encode in base64
     * @return 0, 1, 2 for the number of bytes in the last chunk
     */
    protected int bytesInIncompleteChunk(int bytes) {
        return bytes % 3; 
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
        return constant.getImplementation() == Impl.IBase64Binary;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.BASE64BINARY).contains(datatype);
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
