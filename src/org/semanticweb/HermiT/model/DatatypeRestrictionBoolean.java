package org.semanticweb.HermiT.model;

import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionBoolean extends DataRange {
    
    public DatatypeRestrictionBoolean() {
        this.datatypeURI = XSDVocabulary.BOOLEAN.getURI();
    }
    public static enum SupportedFacets {};
    
    public boolean isFinite() {
        return true;
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return false; 
    }
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for Boolean values.");
    }
}
