package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;

public class DatatypeRestrictionDouble extends DatatypeRestriction {
    
    protected Set<DoubleInterval> doubleIntervals = new HashSet<DoubleInterval>();
   
    public DatatypeRestrictionDouble(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
        doubleIntervals.add(new DoubleInterval());
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
        return new DatatypeRestrictionDouble(this.datatypeURI);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (doubleIntervals.isEmpty()) return false;
        for (DoubleInterval i : doubleIntervals) {
            if (i.getMax() == null || i.getMin() == null) {
                hasOnlyFiniteIntervals = false;
            }
        }
        return hasOnlyFiniteIntervals;
    }
    
    public void addFacet(Facets facet, String value) {
        BigDecimal valueDec = null;
        try {
            BigDecimal bd = new BigDecimal(value);
            double d = bd.doubleValue();
            if (facet == Facets.MIN_EXCLUSIVE || facet == Facets.MIN_INCLUSIVE) {
                valueDec = new BigDecimal("" + d);
                if (valueDec.compareTo(bd) < 0 
                        || (valueDec.compareTo(bd) == 0 
                                && facet == Facets.MIN_EXCLUSIVE)) {
                    if (d < Double.MAX_VALUE) d += Double.MIN_NORMAL;
                    valueDec = new BigDecimal("" + d);
                }
            } else {
                valueDec = new BigDecimal("" + d);
                if (valueDec.compareTo(bd) > 0 
                        || (valueDec.compareTo(bd) == 0 
                                && facet == Facets.MAX_EXCLUSIVE)) {
                    if (d > Double.MIN_VALUE) d -= Double.MIN_NORMAL;
                    valueDec = new BigDecimal("" + d);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            for (DoubleInterval i : doubleIntervals) {
                i.intersectWith(new DoubleInterval(valueDec, null));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MIN_EXCLUSIVE: {
            for (DoubleInterval i : doubleIntervals) {
                i.intersectWith(new DoubleInterval(valueDec, null));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_INCLUSIVE: {
            for (DoubleInterval i : doubleIntervals) {
                i.intersectWith(new DoubleInterval(null, valueDec));
                if (i.isEmpty()) {
                    isBottom = true;
                }
            }
        } break;
        case MAX_EXCLUSIVE: {
            for (DoubleInterval i : doubleIntervals) {
                i.intersectWith(new DoubleInterval(null, valueDec));
                if (i.isEmpty()) {
                    isBottom = true;
                }
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
        if (doubleIntervals.isEmpty()) return true;
        BigDecimal intValue = new BigDecimal(constant.getValue());
        for (DoubleInterval i : doubleIntervals) {
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
        if (!(range instanceof DatatypeRestrictionDouble)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        if (!isBottom()) {
            DatatypeRestrictionDouble restr = (DatatypeRestrictionDouble) range;
            if (restr.getIntegerIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (doubleIntervals.isEmpty()) {
                for (DoubleInterval i : restr.getIntegerIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                doubleIntervals.add(new DoubleInterval(null, i.getMin()));
                            }
                            if (i.getMax() != null) {
                                doubleIntervals.add(new DoubleInterval(i.getMax(), null));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        doubleIntervals = restr.getIntegerIntervals();
                    }
                }
            } else {
                Set<DoubleInterval> newIntervals = new HashSet<DoubleInterval>();
                if (restr.isNegated()) {
                    for (DoubleInterval i : doubleIntervals) {
                        for (DoubleInterval iNew : restr.getIntegerIntervals()) {
                            if (!iNew.isEmpty()) {
                                if (iNew.getMin() != null) {
                                    DoubleInterval newInterval = i.getCopy();
                                    BigDecimal newMin = iNew.getMin(); 
                                    if (newMin.compareTo(new BigDecimal("" + Double.MIN_VALUE)) > 0) {
                                        newMin = newMin.subtract(new BigDecimal("" + Double.MIN_NORMAL));
                                        newInterval.intersectWith(new DoubleInterval(null, newMin));
                                        if (!newInterval.isEmpty()) {
                                            newIntervals.add(newInterval);
                                        }
                                    }
                                } 
                                if (iNew.getMax() != null) {
                                    DoubleInterval newInterval = i.getCopy();
                                    BigDecimal newMax = iNew.getMax(); 
                                    if (newMax.compareTo(new BigDecimal("" + Double.MAX_VALUE)) < 0) {
                                        newMax = newMax.add(new BigDecimal("" + Double.MIN_NORMAL));
                                        newInterval.intersectWith(new DoubleInterval(newMax, null));
                                        if (!newInterval.isEmpty()) {
                                            newIntervals.add(newInterval);
                                        }
                                    }
                                }
                            } else {
                                newIntervals.add(i);
                            }
                        }
                    }
                } else {
                    for (DoubleInterval i : doubleIntervals) {
                        for (DoubleInterval iNew : restr.getIntegerIntervals()) {
                            i.intersectWith(iNew);
                            if (!i.isEmpty()) newIntervals.add(i);
                        }
                    }
                }
                if (newIntervals.isEmpty()) {
                    isBottom = true;
                } else {
                    doubleIntervals = newIntervals;
                }
            }
        }
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        BigDecimal doubleValue = new BigDecimal(constant.getValue());
        for (DoubleInterval i : doubleIntervals) {
            if (i.contains(doubleValue) && !notOneOf.contains(constant)) {
                return true;
            }
        }
        return false; 
    }
    
    public boolean hasMinCardinality(int n) {
        if (isNegated || n <= 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (oneOf.size() >= n);
            }
            BigInteger nBig = new BigInteger("" + n);
            BigInteger rangeSize = BigInteger.ZERO;
            for (DoubleInterval i : doubleIntervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigDecimal not = new BigDecimal(constant.getValue());
                for (DoubleInterval i : doubleIntervals) {
                    if (i.contains(not)) {
                        rangeSize = rangeSize.subtract(BigInteger.ONE);
                    }
                }
            }
            return (rangeSize.compareTo(nBig) >= 0);
        }
        return true;
    }
    
    public BigInteger getEnumerationSize() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            BigInteger rangeSize = BigInteger.ZERO;
            for (DoubleInterval i : doubleIntervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigDecimal not = new BigDecimal(constant.getValue());
                for (DoubleInterval i : doubleIntervals) {
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
            SortedSet<DoubleInterval> sortedIntervals = new TreeSet<DoubleInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(doubleIntervals);
            for (DoubleInterval i : sortedIntervals) {
                BigDecimal constant = i.getMin();
                while (constant.compareTo(i.getMax()) <= 0) {
                    DataConstant dataConstant = new DataConstant(datatypeURI, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = constant.add(new BigDecimal("" + Double.MIN_NORMAL));
                }
            }
        }
        return null;
    }
    
    public Set<DoubleInterval> getIntegerIntervals() {
        return doubleIntervals;
    }
    
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (DoubleInterval i : doubleIntervals) {
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
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "double"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "float"));
        return supportedDTs.contains(constant.getDatatypeURI());
    }
    
    
    public class DoubleInterval {
        BigDecimal min = null;
        BigDecimal max = null;
        
        public DoubleInterval() {
            this.min = new BigDecimal("" + Double.MIN_VALUE);
            this.max = new BigDecimal("" + Double.MAX_VALUE);
        }
        
        public DoubleInterval(BigDecimal minInclusive, BigDecimal maxInclusive) {
            this.min = minInclusive;
            this.max = maxInclusive;
        }
        
        public DoubleInterval getCopy() {
            return new DoubleInterval(min, max);
        }
        
        public void intersectWith(DoubleInterval i) {
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
        
        protected boolean isEmpty(BigDecimal lower, BigDecimal upper) {
            return (lower != null 
                    && upper != null 
                    && lower.compareTo(upper) > 0);
        }
        
        public boolean isFinite() {
            return min != null && max != null;
        }
        
        public boolean contains(BigDecimal integer) {
            boolean contains = true;
            if (min != null) {
                contains = contains && (min.compareTo(integer) <= 0);
            }
            if (max != null) {
                contains = contains && (max.compareTo(integer) >= 0);
            }
            return contains;
        }
        
        public boolean contains(DoubleInterval interval) {
            return contains(interval.getMin()) 
                    && contains(interval.getMax());
        }
        
        public boolean disjointWith(DoubleInterval interval) {
            return (min.compareTo(interval.getMax()) >= 0 
                    || max.compareTo(interval.getMin()) <= 0);
        }
        
        public BigInteger getCardinality() {
            if (max.compareTo(min) < 0) return BigInteger.ZERO;
            // Extract the sign and magnitude from 'start'
            long bitsStart  = Double.doubleToRawLongBits(min.doubleValue());
            long bitsEnd = Double.doubleToRawLongBits(max.doubleValue());
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
                return new BigInteger("" + (
                    magnitudeStart + 1 + // the number of values from 'start' to -0
                    magnitudeEnd + 1));   // the number of values from +0 to 'end'
            } else {
                // if (positiveStart && !positiveEnd)
                return BigInteger.ZERO;
            }
        }

        protected boolean isNaN(long bits) {
            return ((bits & 0x7f80000000000000l) == 0x7f80000000000000l) 
                    && ((bits & 0x003fffffffffffffl) != 0);
        }
        
        public BigDecimal getMin() {
            return min;
        }

        public void setMin(BigDecimal min) {
            this.min = min;
        }

        public BigDecimal getMax() {
            return max;
        }

        public void setMaxIncl(BigDecimal max) {
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
    
    protected static class IntervalComparator implements Comparator<DoubleInterval> { 
        public static Comparator<DoubleInterval> INSTANCE = new IntervalComparator();
        public int compare(DoubleInterval i1, DoubleInterval i2) {
            return i1.getMin().compareTo(i2.getMin()); 
        }
    }
    
    public static boolean canHandleAll(Set<URI> datatypeURIs) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "realPlus"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "real"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "decimal"));;
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "integer"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "nonNegativeInteger"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "nonPositiveInteger"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "positiveInteger"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "negativeInteger"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "long"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "int"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "short"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "byte"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "unsignedLong"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "unsignedInt"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "unsignedShort"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "unsignedByte"));
        return supportedDTs.containsAll(datatypeURIs);
    }
}
