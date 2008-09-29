package org.semanticweb.HermiT.model;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeRestrictionString extends DataRange {
    
    public static enum SupportedFacets {
        LENGTH, MIN_LENGTH, MAX_LENGTH, PATTERN
    };
    
    protected Set<Integer> lengthValues = new HashSet<Integer>();
    protected Set<Integer> minLengthValues = new HashSet<Integer>();
    protected Set<Integer> maxLengthValues = new HashSet<Integer>();

    public DatatypeRestrictionString() {
        this.datatypeURI = XSDVocabulary.STRING.getURI();
    }
    
    public boolean isFinite() {
        return (!lengthValues.isEmpty() || !equalsValues.isEmpty());
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return false; 
    }
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case LENGTH: {
            Integer valueInt = new Integer(value);
            lengthValues.add(valueInt);
        } break;
        case MIN_LENGTH: {
            Integer valueInt = new Integer(value);
            minLengthValues.add(valueInt);
        } break;
        case MAX_LENGTH: {
            Integer valueInt = new Integer(value);
            maxLengthValues.add(valueInt);
        } break;
        default:
            throw new IllegalArgumentException("Unsupported facet.");
        }
    }
}
