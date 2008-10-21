package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;

public class DatatypeRestrictionOWLRealPlus extends DatatypeRestriction {
    
    protected Set<Interval> intervals = new HashSet<Interval>();

    public DatatypeRestrictionOWLRealPlus(URI datatypeURI) {
        this(datatypeURI, true);
    }
    
    public DatatypeRestrictionOWLRealPlus(URI datatypeURI, boolean allowSpecials) {
        this.datatypeURI = datatypeURI;
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, 
                        Facets.MIN_EXCLUSIVE, 
                        Facets.MAX_INCLUSIVE, 
                        Facets.MAX_EXCLUSIVE
                })
        );
        if (!allowSpecials) {
            notOneOf.addAll(DataConstant.numericSpecials);
        }
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionOWLRealPlus(this.datatypeURI);
    }
    
    public boolean isFinite() {
        return isBottom() || !oneOf.isEmpty();
    }
    
    public void addFacet(Facets facet, String value) {
        if ("NaN".equalsIgnoreCase(value)) {
            isBottom = true;
        } else {
            BigDecimal valueDec = null;
            try {
                valueDec = new BigDecimal(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            switch (facet) {
            case MIN_INCLUSIVE: {
                // greater or equal X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(valueDec, null, false, true));
                } else {
                    for (Interval i : intervals) {
                        i.intersectWith(new Interval(valueDec, null, false, true));
                    }
                }
            } break;
            case MIN_EXCLUSIVE: {
                // greater than X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(valueDec, null, true, true));
                } else {
                    for (Interval i : intervals) {
                        i.intersectWith(new Interval(valueDec, null, true, true));
                    }
                }
            } break;
            case MAX_INCLUSIVE: {
                // smaller or equal X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(null, valueDec, true, false));
                } else {
                    for (Interval i : intervals) {
                        i.intersectWith(new Interval(null, valueDec, true, false));
                    }
                }
            } break;
            case MAX_EXCLUSIVE: {
                // smaller than X
                if (intervals.isEmpty()) {
                    intervals.add(new Interval(null, valueDec, true, true));
                } else {
                    for (Interval i : intervals) {
                        i.intersectWith(new Interval(null, valueDec, true, true));
                    }
                }
            } break;
            default:
                throw new IllegalArgumentException("Unsupported facet.");
            }
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
        BigDecimal decValue = new BigDecimal(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(decValue)) {
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
        if (!(range instanceof DatatypeRestrictionOWLRealPlus)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionOWLReal. It is only " +
                    "allowed to add facets from other owl real datatype " +
                    "restrictions.");
        }
        if (!isBottom()) {
            DatatypeRestrictionOWLRealPlus restr = (DatatypeRestrictionOWLRealPlus) range;
            if (restr.getIntervals().size() > 1) {
                throw new IllegalArgumentException("The given parameter " +
                        "contains more than one interval. ");
            }
            if (intervals.isEmpty()) {
                for (Interval i : restr.getIntervals()) {
                    if (restr.isNegated()) {
                        if (!i.isEmpty()) {
                            if (i.getMin() != null) {
                                intervals.add(new Interval(null, i.getMin(), true, !i.isOpenMin()));
                            }
                            if (i.getMax() != null) {
                                intervals.add(new Interval(i.getMax(), null, !i.isOpenMax(), true));
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
                                    newInterval.intersectWith(new Interval(null, iNew.getMin(), true, !iNew.isOpenMin()));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                } 
                                if (iNew.getMax() != null) {
                                    Interval newInterval = i.getCopy();
                                    newInterval.intersectWith(new Interval(iNew.getMax(), null, !iNew.isOpenMax(), true));
                                    if (!newInterval.isEmpty()) {
                                        newIntervals.add(newInterval);
                                    }
                                }
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
        BigDecimal decValue = new BigDecimal(constant.getValue());
        for (Interval i : intervals) {
            if (i.contains(decValue) && !notOneOf.contains(constant)) {
                return true;
            }
        }
        return false; 
    }
    
    public boolean hasMinCardinality(int n) {
        if (!oneOf.isEmpty()) {
            return (oneOf.size() >= n);
        }
        return true;
    }
    
    public BigInteger getEnumerationSize() {
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        }
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
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
                buffer.append((i.isOpenMin() ? "> " : ">= ") + i.getMin());
            }
            if (i.getMax() != null) {
                if (i.getMin() != null) buffer.append(" ");
                buffer.append((i.isOpenMax() ? "< " : "<= ") + i.getMax());
            }
            firstRun = false;
        }
        return buffer.toString();
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "decimal"));
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
    
    public static boolean canHandleAll(Set<URI> datatypeURIs) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "realPlus"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "real"));
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "decimal"));
        return supportedDTs.containsAll(datatypeURIs);
    }
    
    public class Interval {
        BigDecimal min = null;
        BigDecimal max = null;
        boolean openMin = true;
        boolean openMax = true;
        
        public Interval(BigDecimal min, BigDecimal max, 
                boolean openMin, boolean openMax) {
            this.min = min;
            this.max = max;
            this.openMin = openMin;
            this.openMax = openMax;
        }
        
        public Interval getCopy() {
            return new Interval(min, max, openMin, openMax);
        }
        
        public void intersectWith(Interval i) {
            if (max == null) {
                max = i.getMax();
                openMax = i.isOpenMax();
            } else {
                if (i.getMax() != null) {
                    // both not null
                    if (i.getMax().compareTo(max) == 0 && !i.isOpenMax()) {
                        openMax = false;
                    } else if (i.getMax().compareTo(max) < 0) {
                        max = i.getMax();
                        openMax = i.isOpenMax();
                    }
                }
            }
            if (min == null) {
                min = i.getMin();
                openMin = i.isOpenMin();
            } else {
                if (i.getMin() != null) {
                    // both not null
                    if (i.getMin().compareTo(min) == 0 && !i.isOpenMin()) {
                        openMin = false;
                    } else if (i.getMin().compareTo(min) > 0) {
                        min = i.getMin();
                        openMin = i.isOpenMin();
                    }
                }
            }
        }
        
        public boolean isEmpty() {
            return (min != null && max != null && min.compareTo(max) > 0);
        }
        
        protected boolean isEmpty(BigDecimal lower, BigDecimal upper) {
            return (lower != null && upper != null && lower.compareTo(upper) > 0);
        }
        
        public boolean isFinite() {
            return isEmpty();
        }
        
        public boolean contains(BigDecimal decimal) {
            boolean contains = true;
            if (min != null) {
                contains = contains 
                        && (min.compareTo(decimal) <= 0 
                        && (!openMin || min.compareTo(decimal) != 0));
            }
            if (max != null) {
                contains = contains 
                        && (max.compareTo(decimal) >= 0 
                        && (!openMin || max.compareTo(decimal) != 0));
            }
            return contains;
        }
        
        public boolean contains(Interval interval) {
            return contains(interval.getMin()) && contains(interval.getMax());
        }
        
        public boolean disjointWith(Interval interval) {
            return (max != null && interval.getMin() != null 
                    && (max.compareTo(interval.getMin()) < 0 
                            || (max.compareTo(interval.getMin()) == 0 && (!openMax || !interval.isOpenMin())))) 
            || (min != null && interval.getMax() != null 
                    && (min.compareTo(interval.getMax()) > 0 
                            || (min.compareTo(interval.getMax()) == 0 && (!openMin || !interval.isOpenMax()))));
        }
        
        public BigDecimal getCardinality() {
            if (isEmpty()) return BigDecimal.ZERO;
            return null;
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

        public void setMax(BigDecimal max) {
            this.max = max;
        }
        
        public boolean isOpenMin() {
            return openMin;
        }

        public void setOpenMin(boolean openMin) {
            this.openMin = openMin;
        }

        public boolean isOpenMax() {
            return openMax;
        }

        public void setOpenMax(boolean openMax) {
            this.openMax = openMax;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (min != null) {
                buffer.append((isOpenMin() ? "> " : ">= ") + min);
            }
            if (max != null) {
                if (min != null) buffer.append(" ");
                buffer.append((isOpenMax() ? "< " : "<= ") + max);
            }
            return buffer.toString();
        }
    }
}
