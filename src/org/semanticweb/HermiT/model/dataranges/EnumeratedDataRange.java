package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Namespaces;


public class EnumeratedDataRange extends DatatypeRestriction implements DataRange, CanonicalDataRange {
    
    public EnumeratedDataRange() {
        this.datatypeURI = null;
        this.supportedFacets = new HashSet<Facets>();
    }
    
    public CanonicalDataRange getNewInstance() {
        return new EnumeratedDataRange();
    }
    
    public void setOneOf(Set<DataConstant> oneOf) {
        this.oneOf = oneOf;
    }
    
    public boolean addOneOf(DataConstant constant) {
        return oneOf.add(constant);
    }
    
    public boolean removeOneOf(DataConstant constant) {
        boolean contained = oneOf.remove(constant);
        if (contained && oneOf.isEmpty()) {
            // it does not mean it can have arbitrary values now, but rather it 
            // is bottom if not negated and top if negated, so we have to swap 
            // negation values
            isNegated = !isNegated;
        }
        return contained;
    }
    
    public boolean isTop() {
        return false; 
    }
    
    public boolean isBottom() {
        return !isNegated && oneOf.isEmpty(); 
    }
    
    public boolean isFinite() {
        return !isNegated;
    }

    public boolean hasMinCardinality(int n) {
        return oneOf.size() >= n;
    }
    
    public BigInteger getEnumerationSize() {
        if (!isNegated) return new BigInteger("" + oneOf.size());
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            for (DataConstant constant : sortedOneOfs) {
                if (!notOneOf.contains(constant)) return constant;
            }
        }
        return null;
    }

    public boolean isNegated() {
        return isNegated;
    }
    
    public void negate() {
        isNegated = !isNegated;
    }
    
    public void conjoinFacetsFrom(CanonicalDataRange range) {
        throw new RuntimeException("Cannot conjoin any facets to enumerated " +
        "ranges. ");
    }
    
    public boolean supports(Facets facet) {
        return false;
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for RDFS Literal.");
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (!oneOf.isEmpty()) {
            if (isNegated) buffer.append("not ");
            buffer.append("oneOf(");
            boolean firstRun = true;
            for (DataConstant constant : oneOf) {
                if (!firstRun) {
                    buffer.append(isNegated ? " and " : " or ");
                    firstRun = false;
                }
                buffer.append(" " + constant.toString(namespaces));
            }
            buffer.append(")");
        } else if (!notOneOf.isEmpty()) {
            if (!notOneOf.isEmpty()) {
                buffer.append(" not OneOf(");
                boolean firstRun = true;
                for (DataConstant constant : notOneOf) {
                    if (!firstRun) {
                        buffer.append(isNegated ? " and " : " or ");
                        firstRun = false;
                    }
                    buffer.append(" not " + constant.toString(namespaces));
                }
                buffer.append(")");
            }
        } else {
            if (isNegated) {
                buffer.append("top"); 
            } else {
                buffer.append("bottom");
            }
        }
        buffer.append(")");
        return buffer.toString();        
    }

    public boolean accepts(DataConstant constant) {
        return (isNegated ^ oneOf.contains(constant));
    }
}
