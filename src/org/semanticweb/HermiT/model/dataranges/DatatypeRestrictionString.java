package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
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
                        Facets.LENGTH, Facets.MIN_LENGTH, Facets.MAX_LENGTH, Facets.PATTERN
                })
        );
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionString();
    }
    
    public boolean addOneOf(DataConstant constant) {
        return oneOf.add(constant);
    }
    
    public void setOneOf(Set<DataConstant> oneOf) {
        this.oneOf = oneOf;
    }
    
    public boolean removeOneOf(DataConstant constant) {
        boolean contained = oneOf.remove(constant);
        if (contained && oneOf.isEmpty()) {
            // it does not mean it can have arbitrary values now, but rather it 
            // is bottom if not negated and top if negated, so we have to swap 
            // negation values
            isBottom = true;
        }
        return contained;
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
            for (DataConstant constant : notOneOf) {
                tmpAutomaton = Automaton.makeString(constant.getValue());
                patternMatcher = patternMatcher.minus(tmpAutomaton);
            }
            notOneOf = new HashSet<DataConstant>();
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
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DatatypeRestrictionLiteral#getSmallestAssignment()
     */
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        compileAllFacetsIntoPattern();
        String value = patternMatcher.getShortestExample(true);
        return value != null ? new DataConstant(datatypeURI, value) : null;
    }
    
    public boolean facetsAccept(DataConstant constant) {
        compileAllFacetsIntoPattern();
        return patternMatcher.run(constant.getValue());
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        compileAllFacetsIntoPattern();
        return patternMatcher.run(constant.getValue());
    }
    
    public boolean isTop() {
        return false; 
    }
    
    public boolean isBottom() {
        return !hasMinCardinality(1);
    }
    
    public void setNotOneOf(Set<DataConstant> notOneOf) {
        super.setNotOneOf(notOneOf);
        facetsChanged = true;
    }
    
    public boolean addNotOneOf(DataConstant constant) {
        boolean result = super.addNotOneOf(constant);
        facetsChanged = result;
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        if (!(range instanceof DatatypeRestrictionString)) {
            throw new IllegalArgumentException("The given parameter is not " +
                    "an instance of DatatypeRestrictionString. It is " +
                    "only allowed to add facets from other String " +
                    "datatype restrictions. ");
        }
        DatatypeRestrictionString restr = (DatatypeRestrictionString) range;
        if (isNegated) {
            throw new RuntimeException("Cannot add facets to negated " +
            		"data ranges!");
        }
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
    }
    
    public void addFacet(Facets facet, String value) {
        switch (facet) {
        case LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (isNegated) {
                addFacet(Facets.MIN_LENGTH, (valueInt.add(BigInteger.ONE)).toString());
                addFacet(Facets.MAX_LENGTH, (valueInt.subtract(BigInteger.ONE)).toString());
            } else {
                if (minLength == null || valueInt.compareTo(minLength) > 0) {
                    minLength = valueInt;
                    facetsChanged = true;
                }
                if (maxLength == null || valueInt.compareTo(maxLength) < 0) {
                    maxLength = valueInt;
                    facetsChanged = true;
                }
            }
        } break;
        case MIN_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (isNegated) {
                valueInt = valueInt.subtract(BigInteger.ONE);
                if (maxLength != null) {
                    if (valueInt.compareTo(maxLength) > 0) {
                        maxLength = valueInt;
                        facetsChanged = true;
                    }
                } else {
                    maxLength = valueInt;
                    facetsChanged = true;
                }
            } else {
                if (minLength == null || valueInt.compareTo(valueInt) > minLength.intValue()) {
                    minLength = valueInt;
                    facetsChanged = true;
                }
            }
        } break;
        case MAX_LENGTH: {
            BigInteger valueInt = new BigInteger(value);
            if (isNegated) {
                valueInt = valueInt.add(BigInteger.ONE);
                if (minLength != null) {
                    if (valueInt.compareTo(minLength) < 0) {
                        minLength = valueInt;
                        facetsChanged = true;
                    }
                } else {
                    minLength = valueInt;
                    facetsChanged = true;
                }
            } else {
                if (maxLength == null || valueInt.compareTo(valueInt)< maxLength.intValue()) {
                    maxLength = valueInt;
                    facetsChanged = true;
                }
            }
        } break;
        case PATTERN: {
            if (isNegated) {
                RegExp regExp = new RegExp("~" + value);
                Automaton tmpAutomaton = regExp.toAutomaton();
                if (patternMatcher != null) {
                    pattern = pattern + " or " + "~" + value;
                    patternMatcher = BasicOperations.union(patternMatcher, 
                            tmpAutomaton);
                } else {
                    pattern = "~" + value;
                    patternMatcher = tmpAutomaton; 
                }
                facetsChanged = true;
            } else {
                RegExp regExp = new RegExp(value);
                Automaton tmpAutomaton = regExp.toAutomaton();
                if (patternMatcher != null) {
                    pattern = pattern + " and " + value;
                    patternMatcher = BasicOperations.intersection(patternMatcher, 
                            tmpAutomaton);
                } else {
                    pattern = value;
                    patternMatcher = tmpAutomaton; 
                }
                facetsChanged = true;
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
    public BigInteger getMinLength() {
        return minLength;
    }
    public BigInteger getMaxLength() {
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
        buffer.append("(");
        if (isNegated) buffer.append("not ");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        if (minLength != null) {
            buffer.append(isNegated ? " or " : " and ");
            buffer.append(" >= " + minLength);
        }
        if (maxLength != null) {
            buffer.append(isNegated ? " or " : " and ");
            buffer.append(" >= " + maxLength);
        }
        if (pattern != null) {
            buffer.append(isNegated ? " or " : " and ");
            buffer.append(" pattern=" + pattern);
        }
        buffer.append(")");
        return buffer.toString();        
    }
}
