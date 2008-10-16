package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.owl.vocab.XSDVocabulary;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class DatatypeRestrictionString extends DatatypeRestriction {

    protected BigInteger minLength = null;
    protected BigInteger maxLength = null;
    protected String pattern = null;
    protected Automaton patternMatcher = null;
    protected boolean patternMatcherContainsAllFacets = false;
    protected boolean facetsChanged = false;
    
    public DatatypeRestrictionString() {
        this.datatypeURI = XSDVocabulary.STRING.getURI();
        supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.LENGTH, 
                        Facets.MIN_LENGTH, 
                        Facets.MAX_LENGTH, 
                        Facets.PATTERN
                })
        );
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionString();
    }
    
    public boolean isFinite() {
        compileAllFacetsIntoPattern();
        return isBottom || (!isNegated && 
                ((patternMatcher != null && patternMatcher.isFinite()) 
                        || !oneOf.isEmpty()));
    }
    
    protected void compileAllFacetsIntoPattern() {
        if (!patternMatcherContainsAllFacets || facetsChanged || !notOneOf.isEmpty()) {
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
            for (DataConstant constant : notOneOf) {
                tmpAutomaton = Automaton.makeString(constant.getValue());
                patternMatcher = patternMatcher.minus(tmpAutomaton);
            }
            notOneOf = new HashSet<DataConstant>();
        }
        patternMatcherContainsAllFacets = true;
        facetsChanged = false;
    }
    
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case LENGTH: {
            addFacet(Facets.MIN_LENGTH, value);
            addFacet(Facets.MAX_LENGTH, value);
        } break;
        case MIN_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (minLength == null || valueInt.compareTo(valueInt) > minLength.intValue()) {
                minLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case MAX_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (maxLength == null || valueInt.compareTo(valueInt)< maxLength.intValue()) {
                maxLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case PATTERN: {
            RegExp regExp = new RegExp(value);
            Automaton tmpAutomaton = regExp.toAutomaton();
            if (patternMatcher != null) {
                patternMatcher = BasicOperations.intersection(patternMatcher, 
                        tmpAutomaton);
            } else {
                pattern = value;
                patternMatcher = tmpAutomaton; 
            }
            facetsChanged = true;
        } break;
        default:
            throw new IllegalArgumentException("The given facet is not " +
            		"supported for this datatype.");
        }
    }
    
    public boolean facetsAccept(DataConstant constant) {
        compileAllFacetsIntoPattern();
        return patternMatcher.run(constant.getValue());
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
                        "data ranges!");
        }
        if (!(range instanceof DatatypeRestrictionString)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionString. It is " +
                    "only allowed to add facets from other String " +
                    "datatype restrictions. ");
        }
        DatatypeRestrictionString restr = (DatatypeRestrictionString) range;
        if (!isBottom()) {
            Automaton restrMatcher = restr.getPatternMatcher(); 
            if (restrMatcher != null) {
                if (restr.isNegated()) {
                    restrMatcher.complement();
                }
                if (patternMatcher == null) {
                    this.patternMatcher = restrMatcher;
                } else {
                    patternMatcher = BasicOperations.intersection(patternMatcher, 
                            restr.getPatternMatcher());
                }
            }
        }
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        compileAllFacetsIntoPattern();
        return patternMatcher.run(constant.getValue());
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
    
    public BigInteger getEnumerationSize() {
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return new BigInteger("" + oneOf.size());
            }
            compileAllFacetsIntoPattern();
            return new BigInteger("" + patternMatcher.getFiniteStrings().size());
        } 
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        compileAllFacetsIntoPattern();
        String value = patternMatcher.getShortestExample(true);
        return value != null ? new DataConstant(datatypeURI, value) : null;
    }
   
    protected String printExtraInfo(Namespaces namespaces) {
        compileAllFacetsIntoPattern();
        return (pattern != null) ?  pattern : "";
    }
    
    public Automaton getPatternMatcher() {
        compileAllFacetsIntoPattern();
        return patternMatcher;
    }
    
    public String getPattern() {
        return pattern;
    }
}
