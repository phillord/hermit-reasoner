package org.semanticweb.HermiT.model.dataranges;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;


public abstract class DatatypeRestriction implements DataRange, CanonicalDataRange {
    
    protected Set<Facets> supportedFacets = new HashSet<Facets>();
    
    public enum Facets {
        LENGTH, MIN_LENGTH, MAX_LENGTH, PATTERN, MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE, TOTAL_DIGITS, FRACTION_DIGITS
    };
    
    protected URI datatypeURI;
    protected Set<DataConstant> oneOf = new HashSet<DataConstant>();
    protected Set<DataConstant> notOneOf = new HashSet<DataConstant>();
    protected boolean isNegated = false;
    protected boolean isBottom = false;
    
    public int getArity() {
        return 1;
    }
    
    public URI getDatatypeURI() {
        return datatypeURI;
    }
    
    public Set<DataConstant> getOneOf() {
        return oneOf;
    }
    
//    public void setOneOf(Set<DataConstant> oneOf) {
//        throw new RuntimeException("Should use an enumerated data range.");
//    }
//    
//    public boolean addOneOf(DataConstant constant) {
//        throw new RuntimeException("Should use an enumerated data range.");
//    }
//    
//    public boolean removeOneOf(DataConstant constant) {
//        boolean contained = oneOf.remove(constant);
//        if (contained && oneOf.isEmpty()) {
//            // it does not mean it can have arbitrary values now, but rather it 
//            // is bottom if not negated and top if negated, so we have to swap 
//            // negation values
//            isBottom = true;
//        }
//        return contained;
//    }
    
    public boolean hasNonNegatedOneOf() {
        return (!isNegated && !oneOf.isEmpty());
    }
    
    public Set<DataConstant> getNotOneOf() {
        return notOneOf;
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
    
    public boolean isNegated() {
        return isNegated;
    }
    
    public boolean isBottom() {
        if (!isBottom && !hasMinCardinality(1)) {
            isBottom = true;
        }
        return isBottom;
    }
    
    public void negate() {
        isNegated = !isNegated;
    }
    
    public boolean supports(Facets facet) {
        for (Facets supportedFacet : supportedFacets) {
            if (facet == supportedFacet) return true;
        }
        return false;
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
}
