package org.semanticweb.HermiT.model;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionBoolean extends DataRange {
    
    public static enum SupportedFacets {};
    
    public DatatypeRestrictionBoolean() {
        this.datatypeURI = XSDVocabulary.BOOLEAN.getURI();
    }

    public DataRange getNewInstance() {
        return new DatatypeRestrictionBoolean();
    }
    
    public boolean isFinite() {
        return true;
    }
    public void setOneOf(Set<String> oneOf) {
        for (String constant : oneOf) {
            if (!constant.equalsIgnoreCase("true") && !constant.equalsIgnoreCase("false")) {
                throw new RuntimeException("Tried to add a non-boolean value to a Boolean datatype restriction.");
            }
        }
        this.oneOf = oneOf;
    }
    public boolean addOneOf(String constant) {
        boolean result = false;
        if (constant.equals("true") || constant.equals("false")) {
            result = oneOf.add(constant);
        }
        if (oneOf.size() == 2) {
            // this means it is top, so oneOfs is unnecessary
            oneOf.clear();
        }
        return result;
    }
    public boolean hasMinCardinality(int n) {
        if (n > 2) return false;
        if (!oneOf.isEmpty()) {
          return (oneOf.size() >= n);
        }
        return true;
    }
    public Set<String> getEnumeration() {
        if (!oneOf.isEmpty()) {
            return oneOf;
        } else {
            Set<String> result = new HashSet<String>();
            result.add("true");
            result.add("false");
            return result;
        }
    }
    public String getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            return oneOf.iterator().next();
        }
        if (hasMinCardinality(1)) {
            if (notOneOf.contains("true")) {
                return "false";
            } else {
                return "true";
            }
        }
        return null;
    }
    public boolean accepts(String constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return (constant.equals("true") || constant.equals("false")); 
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return !hasMinCardinality(1); 
    }
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        // nothing to do for booleans
    }
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for Boolean values.");
    }
}
