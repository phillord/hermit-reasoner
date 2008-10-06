package org.semanticweb.HermiT.model;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionInteger extends DataRange {
    
    public enum Facets {
        MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE
    };
    
    protected BigInteger minInclusive = null;
    protected BigInteger maxInclusive = null;

    public DatatypeRestrictionInteger() {
        this.datatypeURI = XSDVocabulary.INTEGER.getURI();
    }
    
    public DataRange getNewInstance() {
        return new DatatypeRestrictionInteger();
    }
    
    public boolean isFinite() {
        return ((minInclusive != null && maxInclusive != null) 
                || !oneOf.isEmpty());
    }
    public boolean hasMinCardinality(int n) {
        if (n == 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (oneOf.size() >= n);
            }
            BigInteger nBig = new BigInteger("" + n);
            int substract = 0;
            for (String constant : notOneOf) {
                BigInteger not = new BigInteger(constant);
                if ((not.compareTo(maxInclusive) <= 0)
                        && (not.compareTo(minInclusive) >= 0)) {
                    substract++;
                } else {
                    removeOneOf(constant);
                }
            }
            BigInteger rangeSize = maxInclusive.subtract(minInclusive);
            rangeSize = rangeSize.add(BigInteger.ONE);
            rangeSize = rangeSize.subtract(new BigInteger("" + substract));
            return (rangeSize.compareTo(nBig) >= 0);
        }
        return true;
    }
    public Set<String> getEnumeration() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return oneOf;
            }
            BigInteger actualSize = maxInclusive.subtract(minInclusive);
            if (actualSize.compareTo(new BigInteger("" + Integer.MAX_VALUE)) > 0) {
                System.err.println("Datatype checking produced a set with more than " + Integer.MAX_VALUE + "entries!");
                System.err.println("I give up!");
                return null;
            } else {
                int bound = actualSize.intValue();
                if (bound > 1000) {
                    System.err.println("Datatype checking produces set with more than 1000 entries!");
                    System.err.println("I'll try to handle that!");
                }
                Set<String> result = new HashSet<String>();
                long lower = minInclusive.intValue();
                long upper = maxInclusive.intValue();
                for (long i = lower; i <= upper; i++) {
                    result.add("" + i);
                }
                return result;
            }
        } 
        return null;
    }
    public String getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<String> sortedOneOfs = new TreeSet<String>(oneOf);
            return sortedOneOfs.first();
        }
        BigInteger actualSize = maxInclusive.subtract(minInclusive);
        if (actualSize.compareTo(BigInteger.ZERO) > 0) {
            return minInclusive.toString();
        }
        return null;
    }
    public boolean accepts(String constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        boolean accepted = true;
        BigInteger intValue = new BigInteger(constant);
        if (minInclusive != null) {
            accepted = accepted && (minInclusive.compareTo(intValue) >= 0);
        }
        if (maxInclusive != null) {
            accepted = accepted && (maxInclusive.compareTo(intValue) <= 0);
        }
        accepted = accepted && !notOneOf.contains(constant);
        return accepted; 
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return !hasMinCardinality(1);
    }
    public boolean supports(Facets facet) {
        for (Facets supportedFacet : Facets.values()) {
            if (facet == supportedFacet) return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        if (range instanceof DatatypeRestrictionInteger) {
            DatatypeRestrictionInteger restr = (DatatypeRestrictionInteger) range;
            if (!(isNegated ^ restr.isNegated())) {
                // both are negated or both are not negated
                if (restr.getMinInclusive() != null) {
                    addFacet(Facets.MIN_INCLUSIVE, restr.getMinInclusive().toString()); 
                }
                if (restr.getMaxInclusive() != null) {
                    addFacet(Facets.MAX_INCLUSIVE, restr.getMaxInclusive().toString());
                }
            } else {
                // only one is negated
                if (restr.getMinInclusive() != null) {
                    BigInteger newValue = restr.getMinInclusive().subtract(BigInteger.ONE);
                    addFacet(Facets.MAX_INCLUSIVE, newValue.toString()); 
                }
                if (restr.getMaxInclusive() != null) {
                    BigInteger newValue = restr.getMinInclusive().add(BigInteger.ONE);
                    addFacet(Facets.MIN_INCLUSIVE, newValue.toString());
                }
            }
        }
    }
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case MIN_INCLUSIVE: {
            BigInteger valueInt = new BigInteger(value);
            if (minInclusive != null) {
                if (valueInt.compareTo(minInclusive) < 0) {
                    minInclusive = valueInt;
                }
            } else {
                minInclusive = valueInt;
            }
        } break;
        case MIN_EXCLUSIVE: {
            BigInteger valueInt = (new BigInteger(value)).add(BigInteger.ONE);
            if (minInclusive != null) {
                if (valueInt.compareTo(minInclusive) < 0) {
                    minInclusive = valueInt;
                }
            } else {
                minInclusive = valueInt;
            }
        } break;
        case MAX_INCLUSIVE: {
            BigInteger valueInt = new BigInteger(value);
            if (maxInclusive != null) {
                if (valueInt.compareTo(maxInclusive) > 0) {
                    maxInclusive = valueInt;
                }
            } else {
                maxInclusive = valueInt;
            }
        } break;
        case MAX_EXCLUSIVE:  {
            BigInteger valueInt = (new BigInteger(value)).subtract(BigInteger.ONE);
            if (maxInclusive != null) {
                if (valueInt.compareTo(maxInclusive) > 0) {
                    maxInclusive = valueInt;
                }
            } else {
                maxInclusive = valueInt;
            }
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
    public BigInteger getMinInclusive() {
        return minInclusive;
    }
    public BigInteger getMaxInclusive() {
        return maxInclusive;
    }
}
