// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.bool;

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

public class BooleanDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_BOOLEAN=Prefixes.s_semanticWebPrefixes.get("xsd")+"boolean";
    protected static final ValueSpaceSubset BOOLEAN_ALL=new BooleanAll();
    protected static final ValueSpaceSubset EMPTY=new BooleanNone();
    protected final static Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_BOOLEAN);
    protected final static Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(Boolean.class);
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        boolean value=((Boolean)dataValue).booleanValue();
        return '\"'+(value ? "true" : "false")+"\"^^"+prefixes.abbreviateURI(XSD_BOOLEAN);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_BOOLEAN.equals(datatypeURI);
        if ("false".equalsIgnoreCase(lexicalForm) || "0".equals(lexicalForm))
            return Boolean.FALSE;
        else if ("true".equalsIgnoreCase(lexicalForm) || "1".equals(lexicalForm))
            return Boolean.TRUE;
        else
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("xsd:boolean does not provide any facets.");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return BOOLEAN_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return BOOLEAN_ALL;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert XSD_BOOLEAN.equals(subsetDatatypeURI);
        assert XSD_BOOLEAN.equals(supersetDatatypeURI);
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert XSD_BOOLEAN.equals(datatypeURI1);
        assert XSD_BOOLEAN.equals(datatypeURI2);
        return false;
    }

    protected static class BooleanAll implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return number<=2;
        }
        public boolean containsDataValue(Object dataValue) {
            return Boolean.FALSE.equals(dataValue) || Boolean.TRUE.equals(dataValue);
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            dataValues.add(Boolean.FALSE);
            dataValues.add(Boolean.TRUE);
        }
    }
    
    protected static class BooleanNone implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return number<=0;
        }
        public boolean containsDataValue(Object dataValue) {
            return false;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
        }
    }
}
