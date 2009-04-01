package org.semanticweb.HermiT.datatypes.floatnum;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.datatypes.common.EmptyValueSpaceSubset;

public class FloatDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_FLOAT=Prefixes.s_semanticWebPrefixes.get("xsd")+"float";
    protected static final ValueSpaceSubset FLOAT_ALL=new FloatAll();
    protected static final ValueSpaceSubset EMPTY=new EmptyValueSpaceSubset(XSD_FLOAT);
    
    protected final Set<String> m_managedDatatypeURIs;
    protected final Set<Class<?>> m_managedDataValueClasses;

    public FloatDatatypeHandler() {
        m_managedDatatypeURIs=Collections.singleton(XSD_FLOAT);
        m_managedDataValueClasses=new HashSet<Class<?>>();
        m_managedDataValueClasses.add(Float.class);
    }
    public Set<String> getManagedDatatypeURIs() {
        return m_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return m_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        String lexicalForm=((Float)dataValue).toString();
        return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateURI(XSD_FLOAT);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_FLOAT.equals(datatypeURI);
        try {
            return Float.parseFloat(lexicalForm);
        }
        catch (NumberFormatException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("xsd:float does not provide any facets.");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return FLOAT_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return FLOAT_ALL;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert XSD_FLOAT.equals(subsetDatatypeURI);
        assert XSD_FLOAT.equals(supersetDatatypeURI);
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert XSD_FLOAT.equals(datatypeURI1);
        assert XSD_FLOAT.equals(datatypeURI2);
        return false;
    }

    protected static class FloatAll implements ValueSpaceSubset {

        public String getDatatypeURI() {
            return XSD_FLOAT;
        }
        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            assert dataValue instanceof Float;
            return true;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("You should not do this with floats.");
        }
    }
}
