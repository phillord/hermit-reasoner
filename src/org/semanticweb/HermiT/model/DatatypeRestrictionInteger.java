package org.semanticweb.HermiT.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionInteger extends DatatypeRestrictionLiteral {
    
    protected BigInteger minInclusive = null;
    protected BigInteger maxInclusive = null;

    public DatatypeRestrictionInteger() {
        this.datatypeURI = XSDVocabulary.INTEGER.getURI();
        this.supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.MIN_INCLUSIVE, Facets.MIN_EXCLUSIVE, Facets.MAX_INCLUSIVE, Facets.MAX_EXCLUSIVE
                })
        );
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
            for (String constant : sortedOneOfs) {
                if (!notOneOf.contains(constant)) return sortedOneOfs.first();
            }
            return null;
        }
        BigInteger actualSize = maxInclusive.subtract(minInclusive);
        if (actualSize.compareTo(BigInteger.ZERO) >= 0) {
            BigInteger constant = minInclusive;
            while (constant.compareTo(maxInclusive) <= 0) {
                if (!notOneOf.contains(constant.toString())) return constant.toString();
                constant = constant.add(BigInteger.ONE);
            }
            return null;
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
    public boolean addOneOf(String constant) {
        boolean result = false;
        try {
            new BigInteger(constant);
            return oneOf.add(constant);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void conjoinFacetsFrom(DataRange range) {
        if (!(range instanceof DatatypeRestrictionInteger)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionInteger. It is " +
                    "only allowed to add facets from other integer " +
                    "datatype restrictions. ");
        }
        DatatypeRestrictionInteger restr = (DatatypeRestrictionInteger) range;
        if (isNegated || restr.isNegated()) {
            throw new RuntimeException("Cannot add facets to or from negated " +
            		"data ranges!");
        }
        if (restr.getMinInclusive() != null) {
            addFacet(Facets.MIN_INCLUSIVE, restr.getMinInclusive().toString()); 
        }
        if (restr.getMaxInclusive() != null) {
            addFacet(Facets.MAX_INCLUSIVE, restr.getMaxInclusive().toString());
        }
    }
    public void addFacet(Facets facet, String value) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to or from negated " +
            		"data ranges!");
        }
        BigInteger valueInt = null;
        try {
            valueInt = new BigInteger(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (facet) {
        case MIN_INCLUSIVE: {
            if (minInclusive != null) {
                if (valueInt.compareTo(minInclusive) > 0) {
                    minInclusive = valueInt;
                }
            } else {
                minInclusive = valueInt;
            }
        } break;
        case MIN_EXCLUSIVE: {
            valueInt = valueInt.add(BigInteger.ONE);
            if (minInclusive != null) {
                if (valueInt.compareTo(minInclusive) > 0) {
                    minInclusive = valueInt;
                }
            } else {
                minInclusive = valueInt;
            }
        } break;
        case MAX_INCLUSIVE: {
            if (maxInclusive != null) {
                if (valueInt.compareTo(maxInclusive) < 0) {
                    maxInclusive = valueInt;
                }
            } else {
                maxInclusive = valueInt;
            }
        } break;
        case MAX_EXCLUSIVE:  {
            valueInt = valueInt.subtract(BigInteger.ONE);
            if (maxInclusive != null) {
                if (valueInt.compareTo(maxInclusive) < 0) {
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
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        if (isNegated) buffer.append("(not"); 
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        for (String value : oneOf) {
            buffer.append(" " + value);
        }
        for (String value : notOneOf) {
            buffer.append(" not " + value);
        }
        if (minInclusive != null) {
            buffer.append(" >= " + minInclusive);
        }
        if (maxInclusive != null) {
            buffer.append(" <= " + maxInclusive);
        }
        buffer.append(")");
        if (isNegated) buffer.append(")");
        return buffer.toString();        
    }
}
