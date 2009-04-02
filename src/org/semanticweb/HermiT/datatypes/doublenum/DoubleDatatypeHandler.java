package org.semanticweb.HermiT.datatypes.doublenum;

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

public class DoubleDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_DOUBLE=Prefixes.s_semanticWebPrefixes.get("xsd")+"double";
    protected static final ValueSpaceSubset DOUBLE_ALL=new DoubleAll();
    protected static final ValueSpaceSubset EMPTY=new EmptyValueSpaceSubset(XSD_DOUBLE);
    protected static final Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_DOUBLE);
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(Double.class);
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        String lexicalForm=((Double)dataValue).toString();
        return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateURI(XSD_DOUBLE);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_DOUBLE.equals(datatypeURI);
        try {
            return Double.parseDouble(lexicalForm);
        }
        catch (NumberFormatException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("xsd:double does not provide any facets.");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return DOUBLE_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return DOUBLE_ALL;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert XSD_DOUBLE.equals(subsetDatatypeURI);
        assert XSD_DOUBLE.equals(supersetDatatypeURI);
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert XSD_DOUBLE.equals(datatypeURI1);
        assert XSD_DOUBLE.equals(datatypeURI2);
        return false;
    }

    protected static class DoubleAll implements ValueSpaceSubset {

        public String getDatatypeURI() {
            return XSD_DOUBLE;
        }
        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            assert dataValue instanceof Double;
            return true;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("You should not do this with doubles.");
        }
    }
}
