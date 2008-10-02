package org.semanticweb.HermiT.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.vocab.XSDVocabulary;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class DatatypeRestrictionString extends DataRange {
    Set<Facets> supportedFacets = new HashSet<Facets>(
            Arrays.asList(new Facets[] {
                    Facets.LENGTH, Facets.MIN_LENGTH, Facets.MAX_LENGTH, Facets.PATTERN
            })
    );
    
    protected Set<Integer> lengthValues = new HashSet<Integer>();
    protected Integer minLength = null;
    protected Integer maxLength = null;
    protected Automaton patternMatcher = null;
    protected boolean automatonContainsAllFacets = false;
    protected boolean facetsChanged = false;
    
    public DatatypeRestrictionString() {
        this.datatypeURI = XSDVocabulary.STRING.getURI();
    }
    
    public boolean isFinite() {
        return (!isNegated && 
                (!lengthValues.isEmpty() 
                        || !equalsValues.isEmpty() 
                        || !(maxLength == null))
                        || (!(patternMatcher == null) 
                                && patternMatcher.isFinite()));
    }
    protected void compileFacetsIntoAutomaton() {
        
    }
    public boolean hasMinCardinality(int n) {
        if (n == 0) return true;
        if (isFinite()) {
            if (!equalsValues.isEmpty()) {
                return (equalsValues.size() >= n);
            }
            RegExp regExp = null;
            Automaton tmpAutomaton = null;
            if (maxLength != null || minLength != null) {
                String tmpPattern;
                if (minLength != null) {
                    tmpPattern = "{" + minLength.intValue() + ",";
                } else {
                    tmpPattern = "{0,";
                }
                if (maxLength != null) {
                    tmpPattern = tmpPattern + maxLength.intValue();
                } 
                tmpPattern = tmpPattern + "}";
                regExp = new RegExp("." + tmpPattern);
                tmpAutomaton = regExp.toAutomaton();
                if (patternMatcher != null) {
                    // we are now producing an automaton that captures both the 
                    // required pattern and the required maxLength
                    patternMatcher = BasicOperations.intersection(patternMatcher, tmpAutomaton);
                    // we don't need this any longer since the automaton now 
                    // captures this
                    maxLength = null;
                    minLength = null;
                    return (null == patternMatcher.getFiniteStrings(n - 1));
                } else {
                    return (null == tmpAutomaton.getFiniteStrings(n - 1));
                }
            } 
            if (patternMatcher != null) {
                return (null == patternMatcher.getFiniteStrings(n - 1));
            }
        }
        return true;
    }
    public Set<String> getEnumeration() {
        if (isFinite()) {
            RegExp regExp = null;
            Automaton tmpAutomaton = null;
            if (maxLength != null || minLength != null) {
                String tmpPattern;
                if (minLength != null) {
                    tmpPattern = "{" + minLength.intValue() + ",";
                } else {
                    tmpPattern = "{0,";
                }
                if (maxLength != null) {
                    tmpPattern = tmpPattern + maxLength.intValue();
                } 
                tmpPattern = tmpPattern + "}";
                regExp = new RegExp("." + tmpPattern);
                tmpAutomaton = regExp.toAutomaton();
                if (patternMatcher != null) {
                    // we are now producing an automaton that captures both the 
                    // required pattern and the required maxLength
                    patternMatcher = BasicOperations.intersection(patternMatcher, tmpAutomaton);
                    // we don't need this any longer since the automaton now 
                    // captures this
                    maxLength = null;
                    minLength = null;
                    return patternMatcher.getFiniteStrings();
                } else {
                    return tmpAutomaton.getFiniteStrings();
                }
            } 
            if (patternMatcher != null) {
                return patternMatcher.getFiniteStrings();
            }
        } 
        return null;
    }
    public boolean contains(String string) {
        return true;
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
            if (minLength == null || valueInt.intValue() < minLength.intValue()) {
                minLength = valueInt;
            }
        } break;
        case MAX_LENGTH: {
            Integer valueInt = new Integer(value);
            if (maxLength == null || valueInt.intValue() > maxLength.intValue()) {
                maxLength = valueInt;
            }
        } break;
        case PATTERN: {
            RegExp regExp = new RegExp(value);
            Automaton tmpAutomaton = regExp.toAutomaton();
            if (patternMatcher != null) {
                Collection<Automaton> c = new HashSet<Automaton>();
                c.add(tmpAutomaton);
                c.add(patternMatcher);
                patternMatcher = Automaton.union(c);
            } else {
                patternMatcher = tmpAutomaton; 
            }
        } break;
        default:
            String message = "The given facet is not supported for this datatype.";
            if (supportedFacets.contains(facet)) {
                message = "The given facet is not yet supported for this datatype.";
            }
            throw new IllegalArgumentException(message);
        }
    }
}
