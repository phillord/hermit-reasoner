package org.semanticweb.HermiT.model;

import java.math.BigInteger;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionNonNegativeInteger extends DatatypeRestrictionInteger {
    
    public DatatypeRestrictionNonNegativeInteger() {
        super();
        this.datatypeURI = XSDVocabulary.NON_NEGATIVE_INTEGER.getURI();
        this.minInclusive = BigInteger.ZERO;
    }
    
    public DataRange getNewInstance() {
        return new DatatypeRestrictionNonNegativeInteger();
    }
    public boolean addOneOf(String constant) {
        boolean result = false;
        try {
            BigInteger bigInt = new BigInteger(constant);
            if (bigInt.compareTo(BigInteger.ZERO) < 0) {
                throw new NumberFormatException("The argument is not a non-negative integer. ");
            }
            return oneOf.add(constant);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void conjoinFacetsFrom(DataRange range) {
        if (!(range instanceof DatatypeRestrictionNonNegativeInteger)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionNonNegativeInteger. " +
                    "It is only allowed to add facets from other " +
                    "NonNegativeInteger datatype restrictions. ");
        }
        DatatypeRestrictionNonNegativeInteger restr = (DatatypeRestrictionNonNegativeInteger) range;
        if (isNegated || restr.isNegated()) {
            throw new RuntimeException("Cannot add facets to or from negated " +
                "data ranges!");
        }
        // both are negated or both are not negated
        if (restr.getMinInclusive() != null) {
            addFacet(Facets.MIN_INCLUSIVE, restr.getMinInclusive().toString()); 
        }
        if (restr.getMaxInclusive() != null) {
            addFacet(Facets.MAX_INCLUSIVE, restr.getMaxInclusive().toString());
        }
    }
    public void addFacet(Facets facet, String value) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated data " +
            		"ranges!");
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
            if (valueInt.compareTo(minInclusive) > 0) {
                minInclusive = valueInt;
            }
        } break;
        case MIN_EXCLUSIVE: {
            valueInt = valueInt.add(BigInteger.ONE);
            if (valueInt.compareTo(minInclusive) > 0) {
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
