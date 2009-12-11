/* Copyright 2008, 2009 by the Oxford University Computing Laboratory
   
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
package org.semanticweb.HermiT.datatypes.xmlliteral;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class XMLLiteralDatatypeHandler implements DatatypeHandler {
    protected static final String RDF_XML_LITERAL=Prefixes.s_semanticWebPrefixes.get("rdf")+"XMLLiteral";
    protected static final ValueSpaceSubset XML_LITERAL_ALL=new XMLLiteralAll();
    protected static final ValueSpaceSubset EMPTY=new XMLLiteralNone();
    protected final static Set<String> s_managedDatatypeURIs=Collections.singleton(RDF_XML_LITERAL);
    protected final static Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(XMLLiteral.class);
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        String value=((XMLLiteral)dataValue).getXML();
        for (int index=value.length()-1;index>=0;--index) {
            char c=value.charAt(index);
            if (c=='\\' || c=='\"')
                value=value.substring(0,index)+'\\'+value.substring(index);
        }
        return '\"'+value+"\"^^"+prefixes.abbreviateIRI(RDF_XML_LITERAL);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert RDF_XML_LITERAL.equals(datatypeURI);
        try {
            return XMLLiteral.parse(lexicalForm);
        }
        catch (Exception e) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI,e);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert RDF_XML_LITERAL.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("rdf:XMLLiteral does not provide any facets.");
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert RDF_XML_LITERAL.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return XML_LITERAL_ALL;
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert RDF_XML_LITERAL.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return XML_LITERAL_ALL;
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert RDF_XML_LITERAL.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert RDF_XML_LITERAL.equals(subsetDatatypeURI);
        assert RDF_XML_LITERAL.equals(supersetDatatypeURI);
        return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert RDF_XML_LITERAL.equals(datatypeURI1);
        assert RDF_XML_LITERAL.equals(datatypeURI2);
        return false;
    }

    protected static class XMLLiteralAll implements ValueSpaceSubset {

        public boolean hasCardinalityAtLeast(int number) {
            return true;
        }
        public boolean containsDataValue(Object dataValue) {
            return dataValue instanceof XMLLiteral;
        }
        public void enumerateDataValues(Collection<Object> dataValues) {
            throw new IllegalStateException("Internal errir: the value space is infinite!");
        }
    }
    
    protected static class XMLLiteralNone implements ValueSpaceSubset {

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
