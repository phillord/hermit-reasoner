package org.semanticweb.HermiT.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.Namespaces;


public class DataRange implements DLPredicate {
    
    public enum Facets {
        LENGTH, MIN_LENGTH, MAX_LENGTH, PATTERN, MIN_INCLUSIVE, MIN_EXCLUSIVE, MAX_INCLUSIVE, MAX_EXCLUSIVE, TOTAL_DIGITS, FRACTION_DIGITS
    };
    
    protected URI datatypeURI = URI.create("http://www.w3.org/2000/01/rdf-schema#Literal");
    protected List<String> equalsValues = new ArrayList<String>();
    protected boolean isNegated = false;
    
    public int getArity() {
        return 1;
    }
    public URI getDatatypeURI() {
        return datatypeURI;
    }
    public List<String> getEqualsValues() {
        return equalsValues;
    }
    public void setEqualsValues(List<String> equalsValues) {
        this.equalsValues = equalsValues;
    }
    public boolean addEqualsValue(String equalsValue) {
        return this.equalsValues.add(equalsValue);
    }
    public boolean isTop() {
        return !isNegated; 
    }
    public boolean isBottom() {
        return isNegated; 
    }
    public boolean isFinite() {
        return false;
    }
    public boolean isNegated() {
        return isNegated;
    }
    public void negate() {
        isNegated = !isNegated;
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
        for (String value : equalsValues) {
            buffer.append(" " + value);
        }
        buffer.append(")");
        if (isNegated) buffer.append(")");
        return buffer.toString();        
    }
}
