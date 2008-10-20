package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.DLPredicate;


public class DatatypeRestrictionLiteral extends DatatypeRestriction implements DLPredicate, DataRange {
    
    public DatatypeRestrictionLiteral(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
    }
    
    public CanonicalDataRange getNewInstance() {
        return new DatatypeRestrictionLiteral(this.datatypeURI);
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
        throw new RuntimeException("Cannot conjoin any facets to RDFS " +
        "literal datatype restrictions. ");
    }
    
    public boolean accepts(DataConstant constant) {
        return isNegated;
    }
    
    public boolean hasMinCardinality(int n) {
        return !isNegated;
    }
    
    public BigInteger getEnumerationSize() {
        if (isNegated) return BigInteger.ZERO;
        return null;
    }
    
    public DataConstant getSmallestAssignment() {
        return null;
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.RDFS + "literal"));
        return supportedDTs.contains(constant.getDatatypeURI());
    }
    
    public static boolean canHandleAll(Set<URI> datatypeURIs) {
        Set<URI> supportedDTs = new HashSet<URI>();
        supportedDTs.add(URI.create(org.semanticweb.owl.vocab.Namespaces.RDFS + "literal"));
        return supportedDTs.containsAll(datatypeURIs);
    }
}
