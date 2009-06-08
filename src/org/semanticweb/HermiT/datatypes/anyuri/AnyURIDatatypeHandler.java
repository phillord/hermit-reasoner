// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.anyuri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

import dk.brics.automaton.Automaton;

public class AnyURIDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");
    protected static final ValueSpaceSubset ANY_URI_ALL=new AnyURIValueSpaceSubset(AnyURIValueSpaceSubset.s_anyURI);
    protected static final ValueSpaceSubset EMPTY_SUBSET=new AnyURIValueSpaceSubset(AnyURIValueSpaceSubset.s_empty);
    protected static final Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_NS+"anyURI");
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(URI.class);
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        String lexicalForm=((URI)dataValue).toString();
        return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateIRI(XSD_NS+"anyURI");
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_managedDatatypeURIs.contains(datatypeURI);
        if (!AnyURIValueSpaceSubset.s_anyURI.run(lexicalForm))
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        try {
            return new URI(lexicalForm);
        }
        catch (URISyntaxException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if ((XSD_NS+"minLength").equals(facetURI) || (XSD_NS+"maxLength").equals(facetURI) || (XSD_NS+"length").equals(facetURI)) {
                if (facetValue instanceof Integer) {
                    int value=(Integer)facetValue;
                    if (value<0 || value==Integer.MAX_VALUE)
                        throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support integer "+value+" as value.");
                }
                else
                    throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support value of type "+facetValue.getClass()+" as value.");
            }
            else if ((XSD_NS+"pattern").equals(facetURI)) {
                if (facetValue instanceof String) {
                    String pattern=(String)facetValue;
                    if (!AnyURIValueSpaceSubset.isValidPattern(pattern))
                        throw new UnsupportedFacetException("String '"+pattern+"' is not a valid regular expression.");
                }
                else
                    throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support value of type "+facetValue.getClass()+" as value.");
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on xsd:anyURI.");
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return ANY_URI_ALL;
        else {
            Automaton automaton=getAutomatonFor(AnyURIValueSpaceSubset.s_anyURI,datatypeRestriction);
            if (automaton==null)
                return EMPTY_SUBSET;
            else
                return new AnyURIValueSpaceSubset(automaton);
        }
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (valueSpaceSubset==EMPTY_SUBSET || datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return valueSpaceSubset;
        else {
            Automaton restrictionAutomaton=getAutomatonFor(((AnyURIValueSpaceSubset)valueSpaceSubset).m_automaton,datatypeRestriction);
            if (restrictionAutomaton==null)
                return EMPTY_SUBSET;
            else
                return new AnyURIValueSpaceSubset(restrictionAutomaton);
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (valueSpaceSubset==EMPTY_SUBSET || datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return EMPTY_SUBSET;
        else {
            Automaton restrictionAutomaton=getAutomatonFor(AnyURIValueSpaceSubset.s_anyURI,datatypeRestriction);
            if (restrictionAutomaton==null)
                return valueSpaceSubset;
            Automaton difference=((AnyURIValueSpaceSubset)valueSpaceSubset).m_automaton.minus(restrictionAutomaton);
            if (difference.isEmpty())
                return EMPTY_SUBSET;
            else
                return new AnyURIValueSpaceSubset(difference);
        }
    }
    protected Automaton getAutomatonFor(Automaton automaton,DatatypeRestriction datatypeRestriction) {
        int minLength=0;
        int maxLength=Integer.MAX_VALUE;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,(Integer)facetValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,(Integer)facetValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,(Integer)facetValue);
                maxLength=Math.min(maxLength,(Integer)facetValue);
            }
            else if ((XSD_NS+"pattern").equals(facetURI)) {
                String pattern=(String)facetValue;
                Automaton facetAutomaton=AnyURIValueSpaceSubset.getPatternAutomaton(pattern);
                automaton=automaton.intersection(facetAutomaton);
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' not supported on xsd:anyURI.");
        }
        if (minLength>maxLength)
            return null;
        else if (minLength!=0 || maxLength!=Integer.MAX_VALUE)
            automaton=automaton.intersection(AnyURIValueSpaceSubset.toAutomaton(minLength,maxLength));
        if (automaton.isEmpty())
            return null;
        else
            return automaton;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert s_managedDatatypeURIs.contains(subsetDatatypeURI);
        assert s_managedDatatypeURIs.contains(supersetDatatypeURI);
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert s_managedDatatypeURIs.contains(datatypeURI1);
        assert s_managedDatatypeURIs.contains(datatypeURI2);
        return false;
    }
}
