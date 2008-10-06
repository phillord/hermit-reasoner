package org.semanticweb.HermiT.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;


public class DataRange implements DLPredicate {
    
    public enum Facets {
        LENGTH, MIN_LENGTH, MAX_LENGTH, PATTERN, MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE, TOTAL_DIGITS, FRACTION_DIGITS
    };
    
    protected URI datatypeURI;
    protected Set<String> oneOf = new HashSet<String>();
    protected Set<String> notOneOf = new HashSet<String>();
    protected boolean isNegated = false;
    
    public DataRange() {
        this.datatypeURI = URI.create("http://www.w3.org/2000/01/rdf-schema#Literal");
    }
    
    public DataRange getNewInstance() {
        return new DataRange();
    }
    
    public int getArity() {
        return 1;
    }
    public URI getDatatypeURI() {
        return datatypeURI;
    }
    public Set<String> getOneOf() {
        return oneOf;
    }
    public void setOneOf(Set<String> oneOf) {
        this.oneOf = oneOf;
    }
    public boolean addOneOf(String constant) {
        return oneOf.add(constant);
    }
    public void removeOneOf(String constant) {
        boolean contained = oneOf.remove(constant);
        if (contained && oneOf.isEmpty()) {
            // it does not mean it can have arbitrary values now, but rather it 
            // is bottom if not negated and top if  negated, so we have to swap 
            // negation values
            isNegated = !isNegated;
        }
    }
    public boolean hasNonNegatedOneOf() {
        return (!isNegated && !oneOf.isEmpty());
    }
    public Set<String> getNotOneOf() {
        return notOneOf;
    }
    public void setNotOneOf(Set<String> notOneOf) {
        this.notOneOf = notOneOf;
    }
    public boolean addNotOneOf(String constant) {
        return notOneOf.add(constant);
    }
    public boolean addAllToNotOneOf(Set<String> constants) {
        return notOneOf.addAll(constants);
    }
    public boolean isTop() {
        return !isNegated; 
    }
    public boolean isBottom() {
        return isNegated; 
    }
    public boolean isFinite() {
        return isNegated;
    }
    /**
     * @param n
     * @return true if this data range has a minimal cardinality of n
     */
    public boolean hasMinCardinality(int n) {
        return !isNegated;
    }
    public Set<String> getEnumeration() {
        if (isNegated) return new HashSet<String>();
        return null;
    }
    public String getSmallestAssignment() {
        return "";
    }
    public boolean accepts(String string) {
        return !isNegated;
    }
    public boolean isNegated() {
        return isNegated;
    }
    public void negate() {
        isNegated = !isNegated;
    }
    /**
     * If restr is of the same class as this, conjoin the facets of restr to the 
     * facets of this
     * @param range 
     */
    public void conjoinFacetsFrom(DataRange range) {
        // nothing to do here for RDFS Literal
    }
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for RDFS Literal.");
    }
    public boolean supports(DataRange.Facets facet) {
        return false;
    }
    public String toString() {
        return toString(Namespaces.none);
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        if (isNegated) buffer.append("(not"); 
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        for (String value : oneOf) {
            buffer.append(" " + value);
        }
        buffer.append(")");
        if (isNegated) buffer.append(")");
        return buffer.toString();        
    }
}
