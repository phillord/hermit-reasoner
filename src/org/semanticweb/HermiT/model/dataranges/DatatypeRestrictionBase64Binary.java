package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger.IntervalComparator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Datatypes;
import dk.brics.automaton.RegExp;

public class DatatypeRestrictionBase64Binary 
        extends DatatypeRestriction 
        implements IntegerFacet {
    
    // A general note about base64 encoding: 
    // We have 64 characters (a-z, A-Z, 0-9, +,/) that are allowed to occur in a 
    // string of a data constant for the base64binary data types. Plus padding 
    // (=) and whitespace, but this does not count when we compute the length of 
    // the facets. 
    // Each one letter sequence encodes 6 bits (2^6 = 64). That is a bit 
    // strange, since we encode octets, i.e., bytes = chunks of 8 bits. 
    // Therefore, base64 encodes binary comes in blocks of 4 characters and 
    // the last block might have padding characters if not all 24 bits = 
    // 3 bytes are needed. 
    // For the facets, we are counting bytes (bit octets) and disregard all 
    // whitespace. 
    // So, we remove the padding at the end, and do integer devision by 4 to 
    // find how many complete 3 byte chunks we have. Then we check whether 
    // the last block is a complete one by checking whether numOfChars mod 4 
    // leaves a remainder. If so, the remainder can be 2 or 3 (if 1 it was 
    // not valid base64), which means we have to add 1 or 2 bytes 
    // respectively to the overall number of bytes. 
    
    protected Set<IntegerInterval> intervals = new HashSet<IntegerInterval>();
    protected Automaton patternMatcher = null;
      
    public DatatypeRestrictionBase64Binary(DT datatype) {
        this.datatype = datatype;
        supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.LENGTH, 
                        Facets.MIN_LENGTH, 
                        Facets.MAX_LENGTH
                })
        );
        intervals.add(new IntegerIntervalFin(0l, null));
        patternMatcher = Datatypes.get("base64Binary");
    }

    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionBase64Binary(this.datatype);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (intervals.isEmpty()) return false;
        for (IntegerInterval i : intervals) {
            hasOnlyFiniteIntervals  = hasOnlyFiniteIntervals  && i.isFinite();
        }
        return hasOnlyFiniteIntervals;
    }
    
    public void addFacet(Facets facet, String value) {
        if (facet == Facets.LENGTH) {
            addFacet(Facets.MIN_LENGTH, value);
            addFacet(Facets.MAX_LENGTH, value);
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
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return patternMatcher.run(constant.getValue());
    }
    
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
        // rounding will autoatically handle the last possibly incomplete chunk
        return (numChars * 3) / 4;
    }
    
    protected BigInteger differentData(Number minLength, Number maxLength) {
        // for counting bytes (with 8 bits) with minLength n and maxLength m: 
        // 2^{m*8} - 2^{n*8} = (2^m)^8 - (2^n)^8 
        if (minLength instanceof BigInteger 
                || maxLength instanceof BigInteger
                || (Long) minLength > Integer.MAX_VALUE 
                || (Long) maxLength > Integer.MAX_VALUE) {
            // too big
            return null;
        } else {
            int min = (Integer) minLength;
            int max = (Integer) maxLength;
            BigInteger two = new BigInteger("2");
            BigInteger num = ((two.pow(max)).pow(8));
            if (min != 0) {
                num = num.subtract((two.pow(min)).pow(8));
            }
            return num;
        }
    }
    
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
    
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        if (!intervals.isEmpty()) {
            SortedSet<IntegerInterval> sortedIntervals = new TreeSet<IntegerInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(intervals);
            for (IntegerInterval i : sortedIntervals) {
                if (IntegerIntervalFin.isInt(new BigInteger("" + i.getMin())) 
                    && IntegerIntervalFin.isInt(new BigInteger("" + i.getMax()))) {
                    Automaton a = Datatypes.get("base64Binary");
                    if ((Integer) i.getMin() > 0) {
                        String pattern = ".{" + minBytesToChars((Integer) i.getMin()) + ",}";
                        a = a.intersection(new RegExp(pattern).toAutomaton());
                    }
                    int maxBytes = (Integer) i.getMax();
                    String patternMax = "";
                    String threeBytes = "([A-Za-z0-9+/]{4})";
                    String twoBytes = "(([A-Za-z0-9+/]{2})[AEIMQUYcgkosw048]=)";
                    String oneByte = "(([A-Za-z0-9+/]{1})[AQgw]==)";
                    if (maxBytesToChunksOfFour(maxBytes) > 0) {
                        patternMax = threeBytes + "{0," + maxBytesToChunksOfFour(maxBytes) + "}";
                    }
                    if (maxBytesRest(maxBytes) == 2) {
                        patternMax = patternMax + twoBytes;
                    } else if (maxBytesRest(maxBytes) == 1) {
                        patternMax = patternMax + oneByte;
                    }
                    a = a.intersection(new RegExp(patternMax).toAutomaton());
                    while (!a.isEmpty()) {
                        String value = a.getShortestExample(true);
                        DataConstant dataConstant = new DataConstant(
                                Impl.IHexBinary, datatype, value);
                        if (!notOneOf.contains(dataConstant)) {
                            return dataConstant;
                        }
                        a = a.minus(Automaton.makeString(value));
                    }
                } else {
                    // numbers too big
                    throw new RuntimeException("Too many possibilities of " +
                                "assigning a value to the data constant. ");
                }
            }
        }
        return null;
    }
    
    protected int minBytesToChars(int minBytes) {
        int numChars = (minBytes * 8) / 6; 
        // must be dividable by 4
        return numChars + (4 - (numChars % 4));
    }
    
    protected int maxBytesToChunksOfFour(int maxBytes) {
        return maxBytes / 3; 
    }
    
    protected int maxBytesRest(int maxBytes) {
        return maxBytes % 3; 
    }
    
    public Set<IntegerInterval> getIntegerIntervals() {
        return intervals;
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.BASE64BINARY).contains(constant.getDatatype());
    }
    
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.BASE64BINARY).contains(datatype);
    }
}
