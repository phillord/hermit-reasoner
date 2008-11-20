/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

/**
 * A datatype restriction for Strings and String based datatypes. Automata are 
 * used to enfore further restrictions on String based datatypes (such as 
 * NMTOKEN) and for pattern facets.
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionString extends DatatypeRestriction {

    private static final long serialVersionUID = 4388569868665194529L;
    
    protected BigInteger minLength = null;
    protected BigInteger maxLength = null;
    protected String pattern = null;
    protected Automaton patternMatcher = null;
    protected boolean patternMatcherContainsAllFacets = false;
    protected boolean facetsChanged = false;
    
    /**
     * An implementation for String based datatypes. 
     * @param datatype a datatype (e.g., DT.STRING or DT.NMTOKEN etc.)
     */
    public DatatypeRestrictionString(DT datatype) {
        this.datatype = datatype;
        supportedFacets = new HashSet<Facets>(
                Arrays.asList(new Facets[] {
                        Facets.LENGTH, 
                        Facets.MIN_LENGTH, 
                        Facets.MAX_LENGTH, 
                        Facets.PATTERN
                })
        );
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionString(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        compileAllFacetsIntoPattern();
        return isBottom || (!isNegated && 
                ((patternMatcher != null && patternMatcher.isFinite()) 
                        || !oneOf.isEmpty()));
    }
    
    /**
     * Creates an automaton that captures all the facet restrictions and the 
     * assignments that are not allowed. The automaton can then be used to 
     * generate suitable assignments for this restriction. 
     */
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
            notOneOf.clear();
        }
        patternMatcherContainsAllFacets = true;
        facetsChanged = false;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case LENGTH: {
            addFacet(Facets.MIN_LENGTH, value);
            addFacet(Facets.MAX_LENGTH, value);
        } break;
        case MIN_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (minLength == null || valueInt.compareTo(minLength) > 0) {
                minLength = valueInt;
                facetsChanged = true;
            }
        } break;
        case MAX_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (maxLength == null || valueInt.compareTo(maxLength) < 0) {
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
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        if (constant.getImplementation() != Impl.IString) {
            return false;
        }
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        compileAllFacetsIntoPattern();
        return patternMatcher.run(constant.getValue());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.dataranges.DataRange)
     */
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
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#hasMinCardinality(java.math.BigInteger)
     */
    public boolean hasMinCardinality(BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) == 0) return true;
        if (isFinite()) {
            if (!oneOf.isEmpty()) {
                return n.compareTo(new BigInteger("" + oneOf.size())) >= 0;
            }
            compileAllFacetsIntoPattern();
            return (null == patternMatcher.getFiniteStrings(n.intValue() - 1));
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() {
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        }
        compileAllFacetsIntoPattern();
        if (!patternMatcher.isFinite()) return null;
        return new BigInteger("" + patternMatcher.getFiniteStrings().size());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getSmallestAssignment()
     */
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        compileAllFacetsIntoPattern();
        if (!patternMatcher.isFinite()) {
            return null;
        }
        String value = patternMatcher.getShortestExample(true);
        return value != null ? new DataConstant(Impl.IString, datatype, value) : null;
    }
   
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#printExtraInfo(org.semanticweb.HermiT.Namespaces)
     */
    protected String printExtraInfo(Namespaces namespaces) {
        compileAllFacetsIntoPattern();
        return (pattern != null) ?  pattern : "";
    }
    
    /**
     * @return an automaton that captures all the possible assignments for this 
     * data range
     */
    public Automaton getPatternMatcher() {
        compileAllFacetsIntoPattern();
        return patternMatcher;
    }
    
    /**
     * @return a String that captures the given pattern facet restrictions 
     */
    public String getPattern() {
        return pattern;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.RDFTEXT).contains(constant.getDatatype());
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.RDFTEXT).contains(datatype);
    }
}
