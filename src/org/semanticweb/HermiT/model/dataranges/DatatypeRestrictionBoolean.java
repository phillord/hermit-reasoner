package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
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
    
    public void setOneOf(Set<DataConstant> oneOf) {
        for (DataConstant constant : oneOf) {
            if (!(constant.equals(DatatypeRestrictionBoolean.TRUE) 
                    || constant.equals(DatatypeRestrictionBoolean.FALSE))) {
                isBottom = true;
            }
        }
        this.oneOf = oneOf;
    }
    
    public boolean addOneOf(DataConstant constant) {
        boolean result = false;
        if (!(constant.equals(DatatypeRestrictionBoolean.TRUE) 
                || constant.equals(DatatypeRestrictionBoolean.FALSE))) {
            isBottom = true;
        }
        result = oneOf.add(constant);
        if (oneOf.size() == 2) {
            // this means it is top, so oneOfs is unnecessary
            oneOf.clear();
        }
        return result;
    }
    
    public boolean removeOneOf(DataConstant constant) {
        boolean contained = oneOf.remove(constant);
        if (contained && oneOf.isEmpty()) {
            // it does not mean it can have arbitrary values now, but rather it 
            // is bottom if not negated and top if negated, so we have to swap 
            // negation values
            isBottom = true;
        }
        return contained;
    }
    
    public boolean hasMinCardinality(int n) {
        if (isNegated) return true;
        if (n > 2) return false;
        if (!oneOf.isEmpty()) {
          return (oneOf.size() >= n);
        }
        return true;
    }
    
    public BigInteger getEnumerationSize() {
        if (isFinite()) { 
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            } else {
                return new BigInteger("2");
            }
        } 
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        if (isFinite()) { 
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                for (DataConstant constant : sortedOneOfs) {
                    if (!notOneOf.contains(constant)) return constant;
                }
                return null;
            }
            if (hasMinCardinality(1)) {
                if (notOneOf.contains(DatatypeRestrictionBoolean.TRUE)) {
                    return DatatypeRestrictionBoolean.FALSE;
                } else {
                    return DatatypeRestrictionBoolean.TRUE;
                }
            }
        } 
        return null;
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return (constant.equals(DatatypeRestrictionBoolean.TRUE) 
                || constant.equals(DatatypeRestrictionBoolean.FALSE)); 
    }
    
    public boolean isBottom() {
        return isBottom || !hasMinCardinality(1); 
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        throw new RuntimeException("Cannot conjoin any facets to boolean " +
        		"datatype restrictions. ");
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
        		"Boolean values.");
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (isNegated) buffer.append("not ");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        boolean firstRun = true;
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append(" OneOf(");
            firstRun = true;
            for (DataConstant constant : oneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(" " + constant);
            }
            buffer.append(")");
        }
        if (!notOneOf.isEmpty()) {
            buffer.append(" not OneOf(");
            firstRun = true;
            for (DataConstant constant : notOneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(" not " + constant);
            }
            buffer.append(")");
        }
        buffer.append(")");
        return buffer.toString();        
    }
}
