package org.semanticweb.HermiT.model;

import java.math.BigInteger;

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
    
    public boolean isFinite() {
        return (minInclusive == null 
                && maxInclusive == null 
                && equalsValues.isEmpty());
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return false; 
    }
    public boolean supports(Facets facet) {
        for (Facets supportedFacet : Facets.values()) {
            if (facet == supportedFacet) return true;
        }
        return false;
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
}
