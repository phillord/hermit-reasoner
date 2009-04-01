package org.semanticweb.HermiT.datatypes;

import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * Implements the functions needed for a particular datatype.
 */
public interface DatatypeHandler {
    Set<String> getManagedDatatypeURIs();
    Set<Class<?>> getManagedDataValueClasses();
    String toString(Prefixes prefixes,Object dataValue);
    Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException;
    void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException;
    ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction);
    ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction);
    ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction);
    boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI);
    boolean isDisjointWith(String datatypeURI1,String datatypeURI2);
}
