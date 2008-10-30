package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;


public class EnumeratedDataRange extends DatatypeRestriction implements DataRange, CanonicalDataRange {
    
    public EnumeratedDataRange() {
        this.datatype = null;
        this.supportedFacets = new HashSet<Facets>();
    }
    
    public CanonicalDataRange getNewInstance() {
        return new EnumeratedDataRange();
    }
    
    public boolean isFinite() {
        return !isNegated;
    }
    
    public void addFacet(Facets facet, String value) {
        throw new IllegalArgumentException("Facets are not supported for RDFS Literal.");
    }
    
    public void conjoinFacetsFrom(DataRange range) {
        throw new RuntimeException("Cannot conjoin any facets to enumerated " +
        "ranges. ");
    }
    
    public boolean accepts(DataConstant constant) {
        return oneOf.contains(constant);
    }
    
    public boolean hasMinCardinality(BigInteger n) {
        return (n.compareTo(new BigInteger("" + oneOf.size())) <= 0);
    }
    
    public BigInteger getEnumerationSize() {
        return new BigInteger("" + oneOf.size());
    }
    
    public DataConstant getSmallestAssignment() {
        if (!oneOf.isEmpty()) {
            SortedSet<DataConstant> sortedOneOfs = new TreeSet<DataConstant>(oneOf);
            return sortedOneOfs.first();
        }
        return null;
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        return true;
    }
    
    public boolean canHandle(DT datatype) {
        return false;
    }
}
