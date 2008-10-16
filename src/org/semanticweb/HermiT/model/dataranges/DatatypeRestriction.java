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
    
    public boolean isNegated() {
        return isNegated;
    }
    
    public void negate() {
        isNegated = !isNegated;
    }
    
    public boolean isBottom() {
        if (!isBottom) {
            if (!hasMinCardinality(1)) {
                isBottom = true;
            }
        }
        return isBottom;
    }
    
    public Set<DataConstant> getOneOf() {
        return oneOf;
    }
    
    public void setOneOf(Set<DataConstant> oneOf) {
        this.oneOf = oneOf;
    }
    
    public boolean addOneOf(DataConstant constant) {
        return oneOf.add(constant);
    }
    
    public void setNotOneOf(Set<DataConstant> notOneOf) {
        this.notOneOf = notOneOf;
    }
    
    public boolean notOneOf(DataConstant constant) {
        boolean result = true;
        if (!oneOf.isEmpty()) {
            result = oneOf.remove(constant);
            if (oneOf.isEmpty()) isBottom = true;
        } else {
            result = notOneOf.add(constant); 
        }
        return result;
    }
    
    public boolean facetsAccept(DataConstant constant) {
        return true;
    }
    
    public boolean supports(Facets facet) {
        return supportedFacets.contains(facet);
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (datatypeURI != null) {
            if (isNegated) buffer.append("not ");
            buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        }
        buffer.append(printExtraInfo(namespaces));
        boolean firstRun = true;
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append("oneOf(");
            firstRun = true;
            for (DataConstant constant : oneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(constant.toString(namespaces));
            }
            buffer.append(")");
        }
        if (!notOneOf.isEmpty()) {
            // only in non-negated canonical ranges
            firstRun = true;
            buffer.append(" (");
            for (DataConstant constant : notOneOf) {
                if (!firstRun) {
                    buffer.append(" and");
                    firstRun = false;
                }
                buffer.append(" not " + constant.toString(namespaces));
            }
            buffer.append(")");
        }
        buffer.append(")");
        return buffer.toString();        
    }
    
    /**
     * Can be overwritten by the sub-classes, to print something between the 
     * datatype restriction and the list of oneOfs/notOneOfs 
     * @return a string with extra information for the toString method, e.g., 
     * about facet values
     */
    protected String printExtraInfo(Namespaces namespaces) {
        return "";
    }
}
