/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes.old;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.datatypes.old.DataConstant.Impl;

/**
 * An implementation for Booleans. Supports true and false. 0 and 1 are assumed 
 * to be normalised during parsing. 
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionBoolean extends InternalDatatypeRestriction {

    private static final long serialVersionUID = 6310069828833769998L;
    
    public static DataConstant TRUE = new DataConstant(Impl.IBoolean, DT.BOOLEAN, "true");
    public static DataConstant FALSE = new DataConstant(Impl.IBoolean, DT.BOOLEAN, "false");
    
    /**
     * Create a restriction for Booleans (true/false). 
     * @param datatype Should be DT.BOOLEAN
     */
    public DatatypeRestrictionBoolean(DT datatype) {
        this.datatype = datatype;
        this.supportedFacets = new HashSet<Facet>();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionBoolean(this.datatype);
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facet facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
                        "Boolean values.");
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.dataranges.DataRange)
     */
    public void conjoinFacetsFrom(InternalDataRange range) {
        return;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        if (!oneOf.isEmpty()) {
            return oneOf.contains(constant);
        }
        return (constant.equals(DatatypeRestrictionBoolean.TRUE) 
                || constant.equals(DatatypeRestrictionBoolean.FALSE)); 
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#hasMinCardinality(java.math.BigInteger)
     */
    public boolean hasMinCardinality(BigInteger n) {
        if (isNegated || n.compareTo(BigInteger.ZERO) <= 0) return true;
        if (!oneOf.isEmpty()) {
            return (new BigInteger("" + oneOf.size()).compareTo(n) >= 0);
        } else {
            return (new BigInteger("" + 2).subtract(new BigInteger("" + notOneOf.size())).compareTo(n) >= 0);
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() { 
        if (!oneOf.isEmpty()) {
            return new BigInteger("" + oneOf.size());
        } else {
            return (new BigInteger("" + 2).subtract(new BigInteger("" + notOneOf.size())));
        }
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getSmallestAssignment()
     */
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
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return constant.getImplementation() == Impl.IBoolean;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.BOOLEAN).contains(datatype);
    }
}
