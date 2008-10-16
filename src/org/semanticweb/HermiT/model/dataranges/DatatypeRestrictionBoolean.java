package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionBoolean extends DatatypeRestriction {

    public static DataConstant TRUE = new DataConstant(XSDVocabulary.BOOLEAN.getURI(), "true");
    public static DataConstant FALSE = new DataConstant(XSDVocabulary.BOOLEAN.getURI(), "false");
    
    public DatatypeRestrictionBoolean() {
        this.datatypeURI = XSDVocabulary.BOOLEAN.getURI();
        this.supportedFacets = new HashSet<Facets>();
    }

    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionBoolean();
    }
    
    public boolean isFinite() {
        return true;
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
                        "Boolean values.");
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        throw new RuntimeException("Cannot conjoin any facets to boolean " +
                        "datatype restrictions. ");
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return (constant.equals(DatatypeRestrictionBoolean.TRUE) 
                || constant.equals(DatatypeRestrictionBoolean.FALSE)); 
    }
    
    public boolean hasMinCardinality(int n) {
        if (isNegated || n <= 0) return true;
        if (n > 2) return false;
        if (!oneOf.isEmpty()) {
            return (oneOf.size() >= n);
        } else {
            return (2 - notOneOf.size()) >= n;
        }
    }
    
    public BigInteger getEnumerationSize() {
        if (isFinite()) { 
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            } else {
                return (new BigInteger("2")).subtract(new BigInteger("" + notOneOf.size()));
            }
        } 
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        if (isFinite()) { 
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                return sortedOneOfs.first();
            }
            if (!isBottom()) {
                if (notOneOf.contains(DatatypeRestrictionBoolean.TRUE)) {
                    return DatatypeRestrictionBoolean.FALSE;
                } else {
                    return DatatypeRestrictionBoolean.TRUE;
                }
            }
        } 
        return null;
    }
}
