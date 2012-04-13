/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.datatypes.anyuri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DatatypeRestriction;

import dk.brics.automaton.Automaton;

public class AnyURIDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    protected static final ValueSpaceSubset ANY_URI_ALL=new AnyURIValueSpaceSubset(AnyURIValueSpaceSubset.s_anyURI);
    protected static final ValueSpaceSubset EMPTY_SUBSET=new AnyURIValueSpaceSubset(AnyURIValueSpaceSubset.s_empty);
    protected static final Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_NS+"anyURI");

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
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
            Constant facetValue=datatypeRestriction.getFacetValue(index);
            Object facetDataValue=facetValue.getDataValue();
            if ((XSD_NS+"minLength").equals(facetURI) || (XSD_NS+"maxLength").equals(facetURI) || (XSD_NS+"length").equals(facetURI)) {
                if (facetDataValue instanceof Integer) {
                    int value=(Integer)facetDataValue;
                    if (value<0 || value==Integer.MAX_VALUE)
                        throw new UnsupportedFacetException("The datatype restriction "+this.toString()+" cannot be handled. The facet with URI '"+facetURI+"' does not support integer "+value+" as value. "+(value<0?"The value should not be negative. ":"The value is outside of the supported integer range, i.e., it is larger than "+Integer.MAX_VALUE));
                }
                else
                    throw new UnsupportedFacetException("The datatype xsd:anyURI accepts only integers as facet values for the facet with URI '"+facetURI+"', but in the ontology we have a datatype restriction "+this.toString()+". The value '"+facetValue.toString()+"' does not seem to be an integer.");
            }
            else if ((XSD_NS+"pattern").equals(facetURI)) {
                if (facetDataValue instanceof String) {
                    String pattern=(String)facetDataValue;
                    if (!AnyURIValueSpaceSubset.isValidPattern(pattern))
                        throw new UnsupportedFacetException("String '"+pattern+"' in the datatype restriction "+this.toString()+" is not a valid regular expression.");
                }
                else
                    throw new UnsupportedFacetException("The facet with URI '"+facetURI+"' supports only strings as values, but '"+facetValue.toString()+"' in the restriction "+this.toString()+" does not seem to be a string. It is an instance of the class "+facetValue.getClass()+". ");
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on xsd:anyURI; only xsd:minLength, xsd:maxLength, xsd:length, and xsd:pattern are supported, but the ontology contains the restriction: "+this.toString());
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
            Object facetDataValue=datatypeRestriction.getFacetValue(index).getDataValue();
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,(Integer)facetDataValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,(Integer)facetDataValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,(Integer)facetDataValue);
                maxLength=Math.min(maxLength,(Integer)facetDataValue);
            }
            else if ((XSD_NS+"pattern").equals(facetURI)) {
                String pattern=(String)facetDataValue;
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
