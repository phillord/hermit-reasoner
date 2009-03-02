/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

/**
 * An implementation for the top data range. Everything is contained in this 
 * range or nothing if negated. 
 * 
 * @author BGlimm
 */
public class DatatypeRestrictionLiteral extends DatatypeRestriction {

    private static final long serialVersionUID = 1220315599384980776L;

    /**
     * Create an instance of the top data range. 
     * @param datatype a datatype (should use DT.LITERAL)
     */
    public DatatypeRestrictionLiteral(DT datatype) {
        this.datatype = datatype;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#getNewInstance()
     */
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionLiteral(this.datatype);
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestriction#isBottom()
     */
    public boolean isBottom() {
        return isNegated; 
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#isFinite()
     */
    public boolean isFinite() {
        return isNegated;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DataRange#addFacet(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets, java.lang.String)
     */
    public void addFacet(Facet facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
                        "RDFS Literal.");
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#conjoinFacetsFrom(org.semanticweb.HermiT.model.dataranges.DataRange)
     */
    public void conjoinFacetsFrom(DataRange range) {
        return;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#accepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean accepts(DataConstant constant) {
        return !isNegated;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#hasMinCardinality(java.math.BigInteger)
     */
    public boolean hasMinCardinality(BigInteger n) {
        if (!oneOf.isEmpty()) {
            return n.compareTo(new BigInteger("" + oneOf.size())) <= 0; 
        }
        return !isNegated;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getEnumerationSize()
     */
    public BigInteger getEnumerationSize() {
        return BigInteger.ZERO;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#getSmallestAssignment()
     */
    public DataConstant getSmallestAssignment() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return true; // accepts all datatypes
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.CanonicalDataRange#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.LITERAL).contains(datatype);
    }
}
