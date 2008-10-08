package org.semanticweb.HermiT.model;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionBoolean extends DatatypeRestrictionLiteral {

    public DatatypeRestrictionBoolean() {
        this.datatypeURI = XSDVocabulary.BOOLEAN.getURI();
        this.supportedFacets = new HashSet<Facets>();
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
        if (constant.toLowerCase().equals("true") || constant.toLowerCase().equals("false")) {
            result = oneOf.add(constant);
        } else {
            // it is bottom
            notOneOf.add("true");
            notOneOf.add("false");
        }
        if (oneOf.size() == 2) {
            // this means it is top, so oneOfs is unnecessary
            oneOf.clear();
        }
        return result;
    }
    public boolean hasMinCardinality(int n) {
        if (!isNegated) { 
            if (n > 2) return false;
            if (!oneOf.isEmpty()) {
              return (oneOf.size() >= n);
            }
            return true;
        } else {
            throw new RuntimeException("Can only compute minimal cardinalities for non-negated data ranges!");
        }
    }
    public Set<String> getEnumeration() {
        if (!isNegated) { 
            if (!oneOf.isEmpty()) {
                return oneOf;
            } else {
                Set<String> result = new HashSet<String>();
                result.add("true");
                result.add("false");
                return result;
            }
        } else {
            throw new RuntimeException("Can only enumerate the range if it is finite and not negated!");
        }
    }
    public String getSmallestAssignment() {
        if (!isNegated) { 
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
        } else {
            throw new RuntimeException("Can only get the smallest assignment if the range is finite and not negated!");
        }
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
        throw new RuntimeException("Cannot conjoin any facets to boolean " +
        		"datatype restrictions. ");
    }
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
        		"Boolean values.");
    }
    public String toString(Namespaces namespaces) {
        return super.toString(namespaces);        
    }
}
