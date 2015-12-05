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
package org.semanticweb.HermiT.datatypes.binarydata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class BinaryDataDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    protected static final String XSD_HEX_BINARY=XSD_NS+"hexBinary";
    protected static final String XSD_BASE_64_BINARY=XSD_NS+"base64Binary";
    protected static final ValueSpaceSubset HEX_BINARY_ALL=new BinaryDataValueSpaceSubset(new BinaryDataLengthInterval(BinaryDataType.HEX_BINARY,0,Integer.MAX_VALUE));
    protected static final ValueSpaceSubset BASE_64_BINARY_ALL=new BinaryDataValueSpaceSubset(new BinaryDataLengthInterval(BinaryDataType.BASE_64_BINARY,0,Integer.MAX_VALUE));
    protected static final ValueSpaceSubset EMPTY=new BinaryDataValueSpaceSubset();
    protected static final Set<String> s_managedDatatypeURIs=new HashSet<String>();
    static {
        s_managedDatatypeURIs.add(XSD_HEX_BINARY);
        s_managedDatatypeURIs.add(XSD_BASE_64_BINARY);
    }
    protected static final Set<String> s_supportedFacetURIs=new HashSet<String>();
    static {
        s_supportedFacetURIs.add(XSD_NS+"minLength");
        s_supportedFacetURIs.add(XSD_NS+"maxLength");
        s_supportedFacetURIs.add(XSD_NS+"length");
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_managedDatatypeURIs.contains(datatypeURI);
        BinaryData binaryDataValue;
        if (XSD_HEX_BINARY.equals(datatypeURI))
            binaryDataValue=BinaryData.parseHexBinary(lexicalForm);
        else
            binaryDataValue=BinaryData.parseBase64Binary(lexicalForm);
        if (binaryDataValue==null)
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        else
            return binaryDataValue;
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_managedDatatypeURIs.contains(datatypeURI);
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on binary datatypes; only xsd:minLength, xsd:maxLength, and xsd:length are supported, but the ontology contains the restriction: "+this.toString());
            Object facetDataValue=datatypeRestriction.getFacetValue(index).getDataValue();
            if (!(facetDataValue instanceof Integer))
                throw new UnsupportedFacetException("The binary datatypes accept only integers as facet values, but for the facet with URI '"+facetURI+"' there is a non-integer value "+facetDataValue+" in the datatype restriction "+this.toString()+". ");
            int value=(Integer)facetDataValue;
            if (value<0 || value==Integer.MAX_VALUE)
                throw new UnsupportedFacetException("The datatype restriction "+this.toString()+" cannot be handled. The facet with URI '"+facetURI+"' does not support integer "+value+" as value. "+(value<0?"The value should not be negative. ":"The value is outside of the supported integer range, i.e., it is larger than "+Integer.MAX_VALUE));
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_managedDatatypeURIs.contains(datatypeURI);
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            if (XSD_HEX_BINARY.equals(datatypeURI))
                return HEX_BINARY_ALL;
            else
                return BASE_64_BINARY_ALL;
        BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
        if (interval==null)
            return EMPTY;
        else
            return new BinaryDataValueSpaceSubset(interval);
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY)
            return valueSpaceSubset;
        else {
            BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY;
            else {
                BinaryDataValueSpaceSubset doubleSubset=(BinaryDataValueSpaceSubset)valueSpaceSubset;
                List<BinaryDataLengthInterval> oldIntervals=doubleSubset.m_intervals;
                List<BinaryDataLengthInterval> newIntervals=new ArrayList<BinaryDataLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    BinaryDataLengthInterval oldInterval=oldIntervals.get(index);
                    BinaryDataLengthInterval intersection=oldInterval.intersectWith(interval);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (newIntervals.isEmpty())
                    return EMPTY;
                else
                    return new BinaryDataValueSpaceSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY)
            return EMPTY;
        else {
            BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return valueSpaceSubset;
            else {
                BinaryDataType binaryDataType=(XSD_HEX_BINARY.equals(datatypeURI) ? BinaryDataType.HEX_BINARY : BinaryDataType.BASE_64_BINARY);
                BinaryDataValueSpaceSubset doubleSubset=(BinaryDataValueSpaceSubset)valueSpaceSubset;
                BinaryDataLengthInterval complementInterval1=null;
                if (interval.m_minLength!=0)
                    complementInterval1=new BinaryDataLengthInterval(binaryDataType,0,interval.m_minLength-1);
                BinaryDataLengthInterval complementInterval2=null;
                if (interval.m_maxLength!=Integer.MAX_VALUE)
                    complementInterval2=new BinaryDataLengthInterval(binaryDataType,interval.m_maxLength+1,Integer.MAX_VALUE);
                List<BinaryDataLengthInterval> oldIntervals=doubleSubset.m_intervals;
                List<BinaryDataLengthInterval> newIntervals=new ArrayList<BinaryDataLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    BinaryDataLengthInterval oldInterval=oldIntervals.get(index);
                    if (complementInterval1!=null) {
                        BinaryDataLengthInterval intersection=oldInterval.intersectWith(complementInterval1);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (complementInterval2!=null) {
                        BinaryDataLengthInterval intersection=oldInterval.intersectWith(complementInterval2);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY;
                else
                    return new BinaryDataValueSpaceSubset(newIntervals);
            }
        }
    }
    protected BinaryDataLengthInterval getIntervalFor(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        int minLength=0;
        int maxLength=Integer.MAX_VALUE;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            int facetDataValue=(Integer)datatypeRestriction.getFacetValue(index).getDataValue();
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,facetDataValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,facetDataValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,facetDataValue);
                maxLength=Math.min(maxLength,facetDataValue);
            }
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by "+Prefixes.STANDARD_PREFIXES.abbreviateIRI(datatypeURI)+".");
        }
        BinaryDataType binaryDataType=(XSD_HEX_BINARY.equals(datatypeURI) ? BinaryDataType.HEX_BINARY : BinaryDataType.BASE_64_BINARY);
        if (BinaryDataLengthInterval.isIntervalEmpty(binaryDataType,minLength,maxLength))
            return null;
        else
            return new BinaryDataLengthInterval(binaryDataType,minLength,maxLength);
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        return subsetDatatypeURI.equals(supersetDatatypeURI);
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        return !datatypeURI1.equals(datatypeURI2);
    }
}
