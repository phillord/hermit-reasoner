package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;

public class DatatypeRestrictionInteger extends DatatypeRestriction {
    
    protected Set<IntegerInterval> integerIntervals = new HashSet<IntegerInterval>();
   
    public DatatypeRestrictionInteger(URI datatypeURI) {
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
        return new DatatypeRestrictionInteger(this.datatypeURI);
    }
    
    public boolean isFinite() {
        return isBottom || (!isNegated && (hasOnlyFiniteIntervals() || !oneOf.isEmpty()));
    }
    
    protected boolean hasOnlyFiniteIntervals() {
        boolean hasOnlyFiniteIntervals = true;
        if (integerIntervals.isEmpty()) return false;
        for (IntegerInterval i : integerIntervals) {
            if (i.getMax() == null || i.getMin() == null) {
                hasOnlyFiniteIntervals = false;
            }
        }
        return hasOnlyFiniteIntervals;
    }
    
    public void addFacet(Facets facet, String value) {
        BigInteger valueInt = null;
        try {
            valueInt = new BigInteger(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            // greater or equal X
            if (integerIntervals.isEmpty()) {
                integerIntervals.add(new IntegerInterval(valueInt, null));
            } else {
                for (IntegerInterval i : integerIntervals) {
                    i.intersectWith(new IntegerInterval(valueInt, null));
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
            if (integerIntervals.isEmpty()) {
                integerIntervals.add(new IntegerInterval(null, valueInt));
            } else {
                for (IntegerInterval i : integerIntervals) {
                    i.intersectWith(new IntegerInterval(null, valueInt));
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
        if (integerIntervals.isEmpty()) return true;
        BigInteger intValue = new BigInteger(constant.getValue());
        for (IntegerInterval i : integerIntervals) {
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
            if (restr.getIntegerIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (integerIntervals.isEmpty()) {
                for (IntegerInterval i : restr.getIntegerIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                integerIntervals.add(new IntegerInterval(null, i.getMin()));
                            }
                            if (i.getMax() != null) {
                                integerIntervals.add(new IntegerInterval(i.getMax(), null));
                            }
                        } // otherwise i is trivially satisfied 
                    } else {
                        integerIntervals = restr.getIntegerIntervals();
                    }
                }
            } else {
                Set<IntegerInterval> newIntervals = new HashSet<IntegerInterval>();
                if (restr.isNegated()) {
                    for (IntegerInterval i : integerIntervals) {
                        for (IntegerInterval iNew : restr.getIntegerIntervals()) {
                            if (!iNew.isEmpty()) {
                                if (iNew.getMin() != null) {
                                    IntegerInterval newInterval = i.getCopy();
                                    newInterval.intersectWith(new IntegerInterval(null, iNew.getMin().subtract(BigInteger.ONE)));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() != null) {
                                    IntegerInterval newInterval = i.getCopy();
                                    newInterval.intersectWith(new IntegerInterval(iNew.getMax().add(BigInteger.ONE), null));
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
                    for (IntegerInterval i : integerIntervals) {
                        for (IntegerInterval iNew : restr.getIntegerIntervals()) {
                            i.intersectWith(iNew);
                            if (!i.isEmpty()) newIntervals.add(i);
                        }
                    }
                }
                if (newIntervals.isEmpty()) {
                    isBottom = true;
                } else {
                    integerIntervals = newIntervals;
                }
            }
        }
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        BigInteger intValue = new BigInteger(constant.getValue());
        for (IntegerInterval i : integerIntervals) {
            if (i.contains(intValue) && !notOneOf.contains(constant)) {
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
            BigInteger subtract = BigInteger.ZERO;
            BigInteger rangeSize = BigInteger.ZERO;
            for (IntegerInterval i : integerIntervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (IntegerInterval i : integerIntervals) {
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
            for (IntegerInterval i : integerIntervals) {
                rangeSize = rangeSize.add(i.getCardinality());
            }
            for (DataConstant constant : notOneOf) {
                BigInteger not = new BigInteger(constant.getValue());
                for (IntegerInterval i : integerIntervals) {
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
            SortedSet<IntegerInterval> sortedIntervals = new TreeSet<IntegerInterval>(IntervalComparator.INSTANCE);
            sortedIntervals.addAll(integerIntervals);
            for (IntegerInterval i : sortedIntervals) {
                BigInteger constant = i.getMin();
                while (constant.compareTo(i.getMax()) <= 0) {
                    DataConstant dataConstant = new DataConstant(datatypeURI, "" + constant);
                    if (!notOneOf.contains(dataConstant)) return dataConstant;
                    constant = constant.add(BigInteger.ONE);
                }
            }
        }
        return null;
    }
    
    public Set<IntegerInterval> getIntegerIntervals() {
        return integerIntervals;
    }
    
    protected String printExtraInfo(Namespaces namespaces) {
        boolean firstRun = true;
        StringBuffer buffer = new StringBuffer();
        for (IntegerInterval i : integerIntervals) {
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
        return supportedDTs.contains(constant.getDatatypeURI());
    }
    
    
    public class IntegerInterval {
        BigInteger min = null;
        BigInteger max = null;
        
        public IntegerInterval(BigInteger minInclusive, BigInteger maxInclusive) {
            this.min = minInclusive;
            this.max = maxInclusive;
        }
        
        public IntegerInterval getCopy() {
            return new IntegerInterval(min, max);
        }
        
        public void intersectWith(IntegerInterval i) {
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
        
        public boolean contains(IntegerInterval interval) {
            return contains(interval.getMin()) 
                    && contains(interval.getMax());
        }
        
        public boolean disjointWith(IntegerInterval interval) {
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
    
    protected static class IntervalComparator implements Comparator<IntegerInterval> { 
        public static Comparator<IntegerInterval> INSTANCE = new IntervalComparator();
        public int compare(IntegerInterval i1, IntegerInterval i2) {
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
