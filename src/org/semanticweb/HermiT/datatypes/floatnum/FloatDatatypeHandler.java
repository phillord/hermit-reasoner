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
package org.semanticweb.HermiT.datatypes.floatnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class FloatDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");
    protected static final String XSD_FLOAT=XSD_NS+"float";
    protected static final ValueSpaceSubset FLOAT_ENTIRE=new EntireFloatSubset();
    protected static final ValueSpaceSubset EMPTY_SUBSET=new EmptyFloatSubset();
    protected static final Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_FLOAT);
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(Float.class);
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
        String lexicalForm=((Float)dataValue).toString();
        return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateIRI(XSD_FLOAT);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_FLOAT.equals(datatypeURI);
        try {
            if (lexicalForm.endsWith("INF")) {
                lexicalForm=lexicalForm.replaceAll("INF", "Infinity");
            } 
            return Float.parseFloat(lexicalForm);
        }
        catch (NumberFormatException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on xsd:float.");
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if (!(facetValue instanceof Float))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' takes only floats as values.");
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return FLOAT_ENTIRE;
        else {
            FloatInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY_SUBSET;
            else
                return new NoNaNFloatSubset(interval);
        }
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY_SUBSET)
            return valueSpaceSubset;
        else {
            FloatInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY_SUBSET;
            else if (valueSpaceSubset==FLOAT_ENTIRE)
                return new NoNaNFloatSubset(interval);
            else {
                NoNaNFloatSubset floatSubset=(NoNaNFloatSubset)valueSpaceSubset;
                List<FloatInterval> oldIntervals=floatSubset.m_intervals;
                List<FloatInterval> newIntervals=new ArrayList<FloatInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    FloatInterval oldInterval=oldIntervals.get(index);
                    FloatInterval intersection=oldInterval.intersectWith(interval);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNFloatSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_FLOAT.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else {
            FloatInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return valueSpaceSubset;
            else if (valueSpaceSubset==FLOAT_ENTIRE) {
                List<FloatInterval> newIntervals=new ArrayList<FloatInterval>();
                if (!FloatInterval.areIdentical(interval.m_lowerBoundInclusive,Float.NEGATIVE_INFINITY))
                    newIntervals.add(new FloatInterval(Float.NEGATIVE_INFINITY,FloatInterval.previousFloat(interval.m_lowerBoundInclusive)));
                if (!FloatInterval.areIdentical(interval.m_upperBoundInclusive,Float.POSITIVE_INFINITY))
                    newIntervals.add(new FloatInterval(FloatInterval.nextFloat(interval.m_upperBoundInclusive),Float.POSITIVE_INFINITY));
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNFloatSubset(newIntervals);
            }
            else {
                NoNaNFloatSubset floatSubset=(NoNaNFloatSubset)valueSpaceSubset;
                FloatInterval complementInterval1=null;
                if (!FloatInterval.areIdentical(interval.m_lowerBoundInclusive,Float.NEGATIVE_INFINITY))
                    complementInterval1=new FloatInterval(Float.NEGATIVE_INFINITY,FloatInterval.previousFloat(interval.m_lowerBoundInclusive));
                FloatInterval complementInterval2=null;
                if (!FloatInterval.areIdentical(interval.m_upperBoundInclusive,Float.POSITIVE_INFINITY))
                    complementInterval2=new FloatInterval(FloatInterval.nextFloat(interval.m_upperBoundInclusive),Float.POSITIVE_INFINITY);
                List<FloatInterval> oldIntervals=floatSubset.m_intervals;
                List<FloatInterval> newIntervals=new ArrayList<FloatInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    FloatInterval oldInterval=oldIntervals.get(index);
                    if (complementInterval1!=null) {
                        FloatInterval intersection=oldInterval.intersectWith(complementInterval1);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (complementInterval2!=null) {
                        FloatInterval intersection=oldInterval.intersectWith(complementInterval2);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNFloatSubset(newIntervals);
            }
        }
    }
    protected FloatInterval getIntervalFor(DatatypeRestriction datatypeRestriction) {
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        float lowerBoundInclusive=Float.NEGATIVE_INFINITY;
        float upperBoundInclusive=Float.POSITIVE_INFINITY;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            float facetValue=(Float)datatypeRestriction.getFacetValue(index);
            if ((XSD_NS+"minInclusive").equals(facetURI)) {
                if (FloatInterval.areIdentical(facetValue,+0.0F))
                    facetValue=-0.0F;
                if (FloatInterval.isSmallerEqual(lowerBoundInclusive,facetValue))
                    lowerBoundInclusive=facetValue;
            }
            else if ((XSD_NS+"minExclusive").equals(facetURI)) {
                if (FloatInterval.areIdentical(facetValue,-0.0F))
                    facetValue=+0.0F;
                facetValue=FloatInterval.nextFloat(facetValue);
                if (FloatInterval.isSmallerEqual(lowerBoundInclusive,facetValue))
                    lowerBoundInclusive=facetValue;
            } 
            else if ((XSD_NS+"maxInclusive").equals(facetURI)) {
                if (FloatInterval.areIdentical(facetValue,-0.0F))
                    facetValue=+0.0F;
                if (FloatInterval.isSmallerEqual(facetValue,upperBoundInclusive))
                    upperBoundInclusive=facetValue;
            } 
            else if ((XSD_NS+"maxExclusive").equals(facetURI)) {
                if (FloatInterval.areIdentical(facetValue,+0.0F))
                    facetValue=-0.0F;
                facetValue=FloatInterval.previousFloat(facetValue);
                if (FloatInterval.isSmallerEqual(facetValue,upperBoundInclusive))
                    upperBoundInclusive=facetValue;
            }
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by xsd:double.");
        }
        if (FloatInterval.isIntervalEmpty(lowerBoundInclusive,upperBoundInclusive))
            return null;
        else
            return new FloatInterval(lowerBoundInclusive,upperBoundInclusive);
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
}
