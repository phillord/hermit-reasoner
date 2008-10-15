package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.DLPredicate;


public class DatatypeRestrictionLiteral extends DatatypeRestriction implements DLPredicate, DataRange {
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionLiteral();
    }

    public void setOneOf(Set<DataConstant> oneOf) {
        throw new RuntimeException("Should use an enumerated data range.");
    }
    
    public boolean addOneOf(DataConstant constant) {
        throw new RuntimeException("Should use an enumerated data range.");
    }
    
    public boolean removeOneOf(DataConstant constant) {
        throw new RuntimeException("Should use an enumerated data range.");
    }
    
    public void setNotOneOf(Set<DataConstant> notOneOf) {
        this.notOneOf = notOneOf;
    }
    
    public boolean addNotOneOf(DataConstant constant) {
        return notOneOf.add(constant);
    }
    
    public boolean addAllToNotOneOf(Set<DataConstant> constants) {
        return notOneOf.addAll(constants);
    }
    
    public boolean isBottom() {
        return isNegated; 
    }
    
    public boolean isFinite() {
        return isNegated;
    }

    public boolean hasMinCardinality(int n) {
        return !isNegated;
    }
    
    public BigInteger getEnumerationSize() {
        if (isNegated) return BigInteger.ZERO;
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        return null;
    }

    public void conjoinFacetsFrom(DataRange range) {
        throw new RuntimeException("Cannot conjoin any facets to RDFS " +
        "literal datatype restrictions. ");
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
        		"RDFS Literal.");
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (isNegated) buffer.append("not "); 
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        buffer.append(")");
        return buffer.toString();        
    }

    public boolean accepts(DataConstant constant) {
        return false;
    }
}
