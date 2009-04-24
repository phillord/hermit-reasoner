// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.rdftext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class RDFTextDatatypeHandler implements DatatypeHandler {
    protected static final String RDF_TEXT=Prefixes.s_semanticWebPrefixes.get("rdf")+"text";
    protected static final String XSD_STRING=Prefixes.s_semanticWebPrefixes.get("xsd")+"string";
    protected static final ValueSpaceSubset RDF_TEXT_ALL=new RDFTextAll();
    protected static final ValueSpaceSubset XSD_STRING_ALL=new XSDStringAll();
    protected static final ValueSpaceSubset RDF_TEXT_MINUS_XSD_STRING=new RDFTextMinusXSDString();
    protected static final ValueSpaceSubset EMPTY=new RDFTextNone();
    protected static final Set<String> s_managedDatatypeURIs=new HashSet<String>();
    static {
        s_managedDatatypeURIs.add(RDF_TEXT);
        s_managedDatatypeURIs.add(XSD_STRING);
    }
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(RDFTextDataValue.class);
        s_managedDataValueClasses.add(String.class);
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        if (dataValue instanceof String)
            return '\"'+dataValue.toString()+'\"';
        else {
            RDFTextDataValue rdfTextDataValue=(RDFTextDataValue)dataValue;
            return '\"'+rdfTextDataValue.getString()+"\"@"+rdfTextDataValue.getLanguageTag();
        }
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert RDF_TEXT.equals(datatypeURI) || XSD_STRING.equals(datatypeURI);
        if (RDF_TEXT.equals(datatypeURI)) {
            int lastAt=lexicalForm.lastIndexOf('@');
            if (lastAt==-1)
                throw new MalformedLiteralException(lexicalForm,datatypeURI);
            String string=lexicalForm.substring(0,lastAt);
            String languageTag=lexicalForm.substring(lastAt+1);
            if (languageTag.length()==0)
                return string;
            else
                return new RDFTextDataValue(string,languageTag);
        }
        else
            return lexicalForm;
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert RDF_TEXT.equals(datatypeRestriction.getDatatypeURI()) || XSD_STRING.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("'"+datatypeRestriction.getDatatypeURI()+"' does not provide any facets (yet).");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert RDF_TEXT.equals(datatypeURI) || XSD_STRING.equals(datatypeURI);
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        if (RDF_TEXT.equals(datatypeURI))
            return RDF_TEXT_ALL;
        else
            return XSD_STRING_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert RDF_TEXT_ALL==valueSpaceSubset || RDF_TEXT_MINUS_XSD_STRING==valueSpaceSubset || XSD_STRING_ALL==valueSpaceSubset || EMPTY==valueSpaceSubset;
        assert RDF_TEXT.equals(datatypeURI) || XSD_STRING.equals(datatypeURI);
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        if (RDF_TEXT.equals(datatypeURI))
            return valueSpaceSubset;
        else {
            if (RDF_TEXT_ALL==valueSpaceSubset || XSD_STRING_ALL==valueSpaceSubset)
                return XSD_STRING_ALL;
            else
                return EMPTY;
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert RDF_TEXT_ALL==valueSpaceSubset || RDF_TEXT_MINUS_XSD_STRING==valueSpaceSubset || XSD_STRING_ALL==valueSpaceSubset || EMPTY==valueSpaceSubset;
        assert RDF_TEXT.equals(datatypeURI) || XSD_STRING.equals(datatypeURI);
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        if (RDF_TEXT.equals(datatypeURI))
            return EMPTY;
        else {
            if (RDF_TEXT_ALL==valueSpaceSubset || RDF_TEXT_MINUS_XSD_STRING==valueSpaceSubset)
                return RDF_TEXT_MINUS_XSD_STRING;
            else
                return EMPTY;
        }
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert RDF_TEXT.equals(subsetDatatypeURI) || XSD_STRING.equals(subsetDatatypeURI);
        assert RDF_TEXT.equals(supersetDatatypeURI) || XSD_STRING.equals(supersetDatatypeURI);
        if (RDF_TEXT.equals(subsetDatatypeURI))
            return RDF_TEXT.equals(supersetDatatypeURI);
        else
            return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert RDF_TEXT.equals(datatypeURI1) || XSD_STRING.equals(datatypeURI1);
        assert RDF_TEXT.equals(datatypeURI2) || XSD_STRING.equals(datatypeURI2);
        return false;
    }

    protected static class RDFTextAll implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            assert dataValue instanceof RDFTextDataValue || dataValue instanceof String;
            return true;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("The value space is infinite.");
        }
    }

    protected static class RDFTextMinusXSDString implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            return dataValue instanceof RDFTextDataValue;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("The value space is infinite.");
        }
    }

    protected static class XSDStringAll implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            return dataValue instanceof String;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new UnsupportedOperationException("The value space is infinite.");
        }
    }

    protected static class RDFTextNone implements ValueSpaceSubset {

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
