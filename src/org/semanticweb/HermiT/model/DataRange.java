package org.semanticweb.HermiT.model;

import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.DatatypeRestrictionLiteral.Facets;


public interface DataRange extends DLPredicate {
    
    public DataRange getNewInstance();
    public int getArity();
    public URI getDatatypeURI();
    public Set<String> getOneOf();
    public void setOneOf(Set<String> oneOf);
    public boolean addOneOf(String constant);
    public void removeOneOf(String constant);
    public boolean hasNonNegatedOneOf();
    public Set<String> getNotOneOf();
    public void setNotOneOf(Set<String> notOneOf);
    public boolean addNotOneOf(String constant);
    public boolean addAllToNotOneOf(Set<String> constants);
    public boolean isTop();
    public boolean isBottom();
    public boolean isFinite();
    public boolean hasMinCardinality(int n);
    public Set<String> getEnumeration();
    public String getSmallestAssignment();
    public boolean accepts(String string);
    public boolean isNegated();
    public void negate();
    public void conjoinFacetsFrom(DataRange range);
    public void addFacet(Facets facet, String value);
    public boolean supports(Facets facet);
    public String toString(Namespaces namespaces); 
}
