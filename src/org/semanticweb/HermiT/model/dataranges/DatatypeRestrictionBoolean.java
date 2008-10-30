package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class DatatypeRestrictionBoolean extends DatatypeRestriction {

    public static DataConstant TRUE = new DataConstant(Impl.IBoolean, DT.BOOLEAN, "true");
    public static DataConstant FALSE = new DataConstant(Impl.IBoolean, DT.BOOLEAN, "false");
    
    public DatatypeRestrictionBoolean(DT datatype) {
        this.datatype = datatype;
        this.supportedFacets = new HashSet<Facets>();
    }

    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionBoolean(this.datatype);
    }
    
    public boolean isFinite() {
        return true;
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
                        "Boolean values.");
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        return;
    }
    
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return (constant.equals(DatatypeRestrictionBoolean.TRUE) 
                || constant.equals(DatatypeRestrictionBoolean.FALSE)); 
    }
    
    public boolean hasMinCardinality(BigInteger n) {
        if (isNegated || n.compareTo(BigInteger.ZERO) <= 0) return true;
        if (!oneOf.isEmpty()) {
            return (new BigInteger("" + oneOf.size()).compareTo(n) >= 0);
        } else {
            return (new BigInteger("" + 2).subtract(new BigInteger("" + notOneOf.size())).compareTo(n) >= 0);
        }
    }
    
    public BigInteger getEnumerationSize() { 
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        } else {
            return (new BigInteger("" + 2).subtract(new BigInteger("" + notOneOf.size())));
        }
    }
    
    public DataConstant getSmallestAssignment() {
        if (isFinite()) { 
            if (!oneOf.isEmpty()) {
                SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
                return sortedOneOfs.first();
            }
            if (!isBottom()) {
                if (notOneOf.contains(DatatypeRestrictionBoolean.TRUE)) {
                    return DatatypeRestrictionBoolean.FALSE;
                } else {
                    return DatatypeRestrictionBoolean.TRUE;
                }
            }
        } 
        return null;
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.BOOLEAN).contains(constant.getDatatype());
    }
    
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.BOOLEAN).contains(datatype);
    }
}
