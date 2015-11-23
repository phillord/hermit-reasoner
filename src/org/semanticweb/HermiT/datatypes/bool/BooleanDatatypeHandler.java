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
package org.semanticweb.HermiT.datatypes.bool;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;
/**BooleanDatatypeHandler.*/
public class BooleanDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_BOOLEAN=Prefixes.s_semanticWebPrefixes.get("xsd:")+"boolean";
    protected static final ValueSpaceSubset BOOLEAN_ALL=new BooleanAll();
    protected static final ValueSpaceSubset EMPTY=new BooleanNone();
    protected final static Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_BOOLEAN);

    @Override
    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    @Override
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_BOOLEAN.equals(datatypeURI);
        lexicalForm=lexicalForm.trim();
        if ("false".equalsIgnoreCase(lexicalForm) || "0".equals(lexicalForm))
            return Boolean.FALSE;
        else if ("true".equalsIgnoreCase(lexicalForm) || "1".equals(lexicalForm))
            return Boolean.TRUE;
        else
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
    }
    @Override
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()>0)
            throw new UnsupportedFacetException("The xsd:boolean datatype does not provide any facets, but the ontology contains a restriction on boolean with facets: "+this.toString());
    }
    @Override
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return BOOLEAN_ALL;
    }
    @Override
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return BOOLEAN_ALL;
    }
    @Override
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_BOOLEAN.equals(datatypeRestriction.getDatatypeURI());
        assert datatypeRestriction.getNumberOfFacetRestrictions()==0;
        return EMPTY;
    }
    @Override
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert XSD_BOOLEAN.equals(subsetDatatypeURI);
        assert XSD_BOOLEAN.equals(supersetDatatypeURI);
        return true;
    }
    @Override
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert XSD_BOOLEAN.equals(datatypeURI1);
        assert XSD_BOOLEAN.equals(datatypeURI2);
        return false;
    }

    protected static class BooleanAll implements ValueSpaceSubset {

        @Override
        public boolean hasCardinalityAtLeast(int number) {
            return number<=2;
        }
        @Override
        public boolean containsDataValue(Object dataValue) {
            return Boolean.FALSE.equals(dataValue) || Boolean.TRUE.equals(dataValue);
        }
        @Override
        public void enumerateDataValues(Collection<Object> dataValues) {
            dataValues.add(Boolean.FALSE);
            dataValues.add(Boolean.TRUE);
        }
    }

    protected static class BooleanNone implements ValueSpaceSubset {

        @Override
        public boolean hasCardinalityAtLeast(int number) {
            return number<=0;
        }
        @Override
        public boolean containsDataValue(Object dataValue) {
            return false;
        }
        @Override
        public void enumerateDataValues(Collection<Object> dataValues) {
        }
    }
}
