package org.semanticweb.HermiT.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class DatatypeRestrictionString extends DatatypeRestrictionLiteral implements DataRange {

    protected Integer minLength = null;
    protected Integer maxLength = null;
    protected String pattern = null;
    protected Automaton patternMatcher = null;
    protected boolean patternMatcherContainsAllFacets = false;
    protected boolean facetsChanged = false;
    
    public DatatypeRestrictionString() {
        this.datatypeURI = XSDVocabulary.STRING.getURI();
        supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.LENGTH, Facets.MIN_LENGTH, Facets.MAX_LENGTH, Facets.PATTERN
                })
        );
    }
    
    public DataRange getNewInstance() {
        return new DatatypeRestrictionString();
    }
    
    public boolean isFinite() {
        return (!isNegated && 
                (!oneOf.isEmpty() 
                        || !(maxLength == null))
                        || (!(patternMatcher == null) 
                                && patternMatcher.isFinite()));
    }
    protected void compileAllFacetsIntoPattern() {
        if (!patternMatcherContainsAllFacets || facetsChanged) {
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
                if (patternMatcher == null) {
                    patternMatcher = tmpAutomaton;
                } else {
                    patternMatcher = BasicOperations.intersection(patternMatcher, tmpAutomaton);
                }
                // we don't need this any longer since the automaton now 
                // captures this
                maxLength = null;
                minLength = null;
            } 
            if (patternMatcher == null && !notOneOf.isEmpty()) {
                patternMatcher = Automaton.makeAnyString();
            }
            for (String constant : notOneOf) {
                tmpAutomaton = Automaton.makeString(constant);
                patternMatcher = patternMatcher.minus(tmpAutomaton);
            }
            notOneOf = new HashSet<String>();
        }
        patternMatcherContainsAllFacets = true;
        facetsChanged = false;
    }
    public boolean hasMinCardinality(int n) {
        if (n == 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return (oneOf.size() >= n);
            }
            compileAllFacetsIntoPattern();
            return (null == patternMatcher.getFiniteStrings(n - 1));
        }
        return true;
    }
    public Set<String> getEnumeration() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return oneOf;
            }
            compileAllFacetsIntoPattern();
            return patternMatcher.getFiniteStrings();
        } 
        return null;
    }
    public String getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<String> sortedOneOfs = new TreeSet<String>(oneOf);
            return sortedOneOfs.first();
        }
        compileAllFacetsIntoPattern();
        return patternMatcher.getShortestExample(true);
    }
    public boolean accepts(String string) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(string);
        }
        compileAllFacetsIntoPattern();
        return patternMatcher.run(string);
    }
    public boolean isTop() {
        return false; 
    }
    public boolean isBottom() {
        return !hasMinCardinality(1);
    }
    public void setNotOneOf(Set<String> notOneOf) {
        super.setNotOneOf(notOneOf);
        facetsChanged = true;
    }
    public boolean addNotOneOf(String constant) {
        boolean result = super.addNotOneOf(constant);
        facetsChanged = result;
        return result;
    }
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        if (range instanceof DatatypeRestrictionString) {
            DatatypeRestrictionString restr = (DatatypeRestrictionString) range;
            if (!(isNegated ^ restr.isNegated())) {
                // both are negated or both are not negated
                if (restr.getMinLength() != null) {
                    addFacet(Facets.MIN_LENGTH, restr.getMinLength().toString()); 
                    facetsChanged = true;
                }
                if (restr.getMaxLength() != null) {
                    addFacet(Facets.MAX_LENGTH, restr.getMaxLength().toString());
                    facetsChanged = true;
                }
                if (restr.getPatternMatcher() != null) {
                    if (patternMatcher == null) {
                        this.patternMatcher = restr.getPatternMatcher();
                        this.pattern = restr.getPattern();
                    } else {
                        patternMatcher = BasicOperations.intersection(patternMatcher, 
                                restr.getPatternMatcher());
                        pattern = pattern + " and " + restr.getPattern();
                    }
                    facetsChanged = true;
                }
            } else {
                // only one is negated
                if (restr.getMinLength() != null) {
                    Integer newValue = new Integer(restr.getMinLength().intValue() - 1);
                    addFacet(Facets.MAX_LENGTH, newValue.toString()); 
                    facetsChanged = true;
                }
                if (restr.getMaxLength() != null) {
                    Integer newValue = new Integer(restr.getMaxLength().intValue() + 1);
                    addFacet(Facets.MIN_LENGTH, newValue.toString());
                    facetsChanged = true;
                }
                if (restr.getPatternMatcher() != null) {
                    if (patternMatcher == null) {
                        patternMatcher = restr.getPatternMatcher().complement();
                        pattern = "not " + restr.getPattern();
                    } else {
                        patternMatcher = BasicOperations.intersection(patternMatcher, 
                                restr.getPatternMatcher().complement());
                        pattern = pattern + " and not " + restr.getPattern();
                    }
                    facetsChanged = true;
                }
            }
        }
    }
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case LENGTH: {
            Integer valueInt = new Integer(value);
            if (minLength == null || valueInt.intValue() > minLength.intValue()) {
                minLength = valueInt;
                facetsChanged = true;
            }
            if (maxLength == null || valueInt.intValue() < maxLength.intValue()) {
                maxLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case MIN_LENGTH: {
            Integer valueInt = new Integer(value);
            if (minLength == null || valueInt.intValue() > minLength.intValue()) {
                minLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case MAX_LENGTH: {
            Integer valueInt = new Integer(value);
            if (maxLength == null || valueInt.intValue() < maxLength.intValue()) {
                maxLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case PATTERN: {
            RegExp regExp = new RegExp(value);
            this.pattern = value;
            Automaton tmpAutomaton = regExp.toAutomaton();
            if (patternMatcher != null) {
                pattern = pattern + " and " + value;
                patternMatcher = BasicOperations.intersection(patternMatcher, 
                        tmpAutomaton);
            } else {
                patternMatcher = tmpAutomaton; 
            }
            facetsChanged = true;
        } break;
        default:
            String message = "The given facet is not supported for this datatype.";
            if (supportedFacets.contains(facet)) {
                message = "The given facet is not yet supported for this datatype.";
            }
            throw new IllegalArgumentException(message);
        }
    }
    public Integer getMinLength() {
        return minLength;
    }
    public Integer getMaxLength() {
        return maxLength;
    }
    public Automaton getPatternMatcher() {
        return patternMatcher;
    }
    public String getPattern() {
        return pattern;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        if (isNegated) buffer.append("(not"); 
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        for (String value : oneOf) {
            buffer.append(" " + value);
        }
        for (String value : notOneOf) {
            buffer.append(" not " + value);
        }
        if (minLength != null) {
            buffer.append(" >= " + minLength);
        }
        if (maxLength != null) {
            buffer.append(" >= " + maxLength);
        }
        if (pattern != null) {
            buffer.append(" pattern=" + pattern);
        }
        buffer.append(")");
        if (isNegated) buffer.append(")");
        return buffer.toString();        
    }
}
