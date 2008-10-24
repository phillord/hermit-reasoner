package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.Set;

import org.semanticweb.HermiT.model.DLPredicate;


public class DatatypeRestrictionLiteral extends DatatypeRestriction implements DLPredicate, DataRange {
    
    public DatatypeRestrictionLiteral(DT datatype) {
        this.datatype = datatype;
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionLiteral(this.datatype);
    }

    public boolean isBottom() {
        return isNegated; 
    }
    
    public boolean isFinite() {
        return isNegated;
    }

    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for " +
                        "RDFS Literal.");
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        return;
    }
    
    public boolean accepts(DataConstant constant) {
        return isNegated;
    }
    
    public boolean hasMinCardinality(BigInteger n) {
        return !isNegated;
    }
    
    public BigInteger getEnumerationSize() {
        return BigInteger.ZERO;
    }
    
    public DataConstant getSmallestAssignment() {
        return null;
    }

    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.LITERAL).contains(constant.getDatatype());
    }
    
    public boolean canHandleAll(Set<DT> datatypes) {
        return DT.getSubTreeFor(DT.LITERAL).containsAll(datatypes);
    }
}
