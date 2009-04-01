package org.semanticweb.HermiT.datatypes.owlreal;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.datatypes.common.EmptyValueSpaceSubset;

public class OWLRealDatatypeHandler implements DatatypeHandler {
    protected static final String OWL_REAL=Prefixes.s_semanticWebPrefixes.get("owl")+"real";
    protected static final String XSD_INTEGER=Prefixes.s_semanticWebPrefixes.get("xsd")+"integer";
    protected static final ValueSpaceSubset INTEGER_ALL=new IntegerAll();
    protected static final ValueSpaceSubset EMPTY=new EmptyValueSpaceSubset(OWL_REAL);
    
    protected final Set<String> m_managedDatatypeURIs;
    protected final Set<Class<?>> m_managedDataValueClasses;

    public OWLRealDatatypeHandler() {
        m_managedDatatypeURIs=new HashSet<String>();
        m_managedDatatypeURIs.add(XSD_INTEGER);
        m_managedDataValueClasses=new HashSet<Class<?>>();
        m_managedDataValueClasses.add(Integer.class);
    }
    public Set<String> getManagedDatatypeURIs() {
        return m_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return m_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        assert dataValue instanceof Integer;
        return '\"'+dataValue.toString()+"\"^^"+prefixes.abbreviateURI(XSD_INTEGER);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_INTEGER.equals(datatypeURI);
        try {
            return Integer.parseInt(lexicalForm);
        }
        catch (NumberFormatException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_INTEGER.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("xsd:integer does not provide any facets (yet).");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_INTEGER.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return INTEGER_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_INTEGER.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return INTEGER_ALL;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_INTEGER.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        return false;
    }

    protected static class IntegerAll implements ValueSpaceSubset {

        public String getDatatypeURI() {
            return OWL_REAL;
        }
        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            return dataValue instanceof Integer;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("The value space is infinite.");
        }
    }
}
