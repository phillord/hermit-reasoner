package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger.IntervalComparator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class DatatypeRestrictionHexBinary 
        extends DatatypeRestriction 
        implements IntegerFacet {
    
    protected Set<IntegerInterval> intervals = new HashSet<IntegerInterval>();
    
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
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionHexBinary(this.datatype);
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
        Automaton a = new RegExp("[0-9a-f]{2}*").toAutomaton();
        return a.run(constant.getValue().toLowerCase());
    }
    
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
    
    protected BigInteger differentData(Number minLength, Number maxLength) {
        if (minLength instanceof BigInteger 
                || maxLength instanceof BigInteger
                || (Long) minLength > Integer.MAX_VALUE 
                || (Long) maxLength >= Integer.MAX_VALUE) {
            // too big
            return null;
        } else {
            int min = (Integer) minLength;
            int max = (Integer) maxLength;
            // our alphabet has 256 chars (hex tuples), i.e., |alphabet| = 256
            // compute how many words of length up to maxLength we can build
            // use geometrical series formula
            // sum_{min}^{max} = (256^min - 256^{max+1}) / (1 - 256) 
            BigInteger sa = new BigInteger("256");
            BigInteger d = new BigInteger("-255");
            return (sa.pow(min).subtract(sa.pow(max+1))).divide(d);
        }
    }
    
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
            for (DataConstant constant : notOneOf) {
                String not = constant.getValue();
                for (IntegerInterval i : intervals) {
                    if (!i.contains(not.length() / 2)) {
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
                    String tmpPattern = "{" + i.getMin() + ",";
                    if (i.getMax() != null) {
                        tmpPattern = tmpPattern + i.getMax();
                    }
                    tmpPattern = tmpPattern + "}";
                    Automaton a = new RegExp("([0-9a-f]{2}*)" + tmpPattern).toAutomaton();
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
    
    public Set<IntegerInterval> getIntegerIntervals() {
        return intervals;
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.HEXBINARY).contains(constant.getDatatype());
    }
    
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.HEXBINARY).contains(datatype);
    }
}
