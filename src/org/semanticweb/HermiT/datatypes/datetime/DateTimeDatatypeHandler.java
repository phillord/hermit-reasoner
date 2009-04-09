package org.semanticweb.HermiT.datatypes.datetime;

import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

/**
 * Implements a handler for xsd:dateTime.
 */
public class DateTimeDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");
    protected static final String XSD_DATE_TIME=XSD_NS+"dateTime";
    protected static final String XSD_DATE_TIME_STAMP=XSD_NS+"dateTimeStamp";

    protected static final Set<String> s_managedDatatypeURIs=new HashSet<String>();
    static {
        s_managedDatatypeURIs.add(XSD_DATE_TIME);
        s_managedDatatypeURIs.add(XSD_DATE_TIME_STAMP);
    }
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(DateTime.class);
    }
    protected static final Set<String> s_supportedFacetURIs=new HashSet<String>();
    static {
        s_supportedFacetURIs.add(XSD_NS+"minInclusive");
        s_supportedFacetURIs.add(XSD_NS+"minExclusive");
        s_supportedFacetURIs.add(XSD_NS+"maxInclusive");
        s_supportedFacetURIs.add(XSD_NS+"maxExclusive");
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        assert dataValue instanceof DateTime;
        return '\"'+dataValue.toString()+"\"^^"+prefixes.abbreviateURI(XSD_DATE_TIME);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_managedDatatypeURIs.contains(datatypeURI);
        DateTime dateTime=DateTime.parse(lexicalForm);
        if (dateTime==null)
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        return dateTime;
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on datatypes derived from owl:real.");
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if (!(facetValue instanceof Number))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' takes only numbers as values.");
            if (facetValue instanceof DateTime)
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' supports only date/times as a value.");
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        return null;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return null;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return null;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        return false;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        return false;
    }
}
