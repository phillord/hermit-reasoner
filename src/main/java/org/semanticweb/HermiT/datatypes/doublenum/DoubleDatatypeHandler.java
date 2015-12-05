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
package org.semanticweb.HermiT.datatypes.doublenum;

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

public class DoubleDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    protected static final String XSD_DOUBLE=XSD_NS+"double";
    protected static final ValueSpaceSubset DOUBLE_ENTIRE=new EntireDoubleSubset();
    protected static final ValueSpaceSubset EMPTY_SUBSET=new EmptyDoubleSubset();
    protected static final Set<String> s_managedDatatypeURIs=Collections.singleton(XSD_DOUBLE);
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
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert XSD_DOUBLE.equals(datatypeURI);
        try {
            return Double.parseDouble(lexicalForm);
        }
        catch (NumberFormatException error) {
            if (lexicalForm.equals("INF")) return Double.POSITIVE_INFINITY;
            if (lexicalForm.equals("-INF")) return Double.NEGATIVE_INFINITY;
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        }
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("A facet with URI '"+facetURI+"' is not supported on xsd:double. The xsd:double datatype supports only xsd:minInclusive, xsd:maxInclusive, xsd:minExclusive, and xsd:maxExclusive, but the ontology contains a datatype restriction "+this.toString());
            Object facetDataValue=datatypeRestriction.getFacetValue(index).getDataValue();
            if (!(facetDataValue instanceof Double))
                throw new UnsupportedFacetException("The '"+facetURI+"' facet takes only doubles as values when used on an xsd:double datatype, but the ontology contains a datatype restriction "+this.toString());
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return DOUBLE_ENTIRE;
        else {
            DoubleInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY_SUBSET;
            else
                return new NoNaNDoubleSubset(interval);
        }
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY_SUBSET)
            return valueSpaceSubset;
        else {
            DoubleInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY_SUBSET;
            else if (valueSpaceSubset==DOUBLE_ENTIRE)
                return new NoNaNDoubleSubset(interval);
            else {
                NoNaNDoubleSubset doubleSubset=(NoNaNDoubleSubset)valueSpaceSubset;
                List<DoubleInterval> oldIntervals=doubleSubset.m_intervals;
                List<DoubleInterval> newIntervals=new ArrayList<DoubleInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    DoubleInterval oldInterval=oldIntervals.get(index);
                    DoubleInterval intersection=oldInterval.intersectWith(interval);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNDoubleSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert XSD_DOUBLE.equals(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else {
            DoubleInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return valueSpaceSubset;
            else if (valueSpaceSubset==DOUBLE_ENTIRE) {
                List<DoubleInterval> newIntervals=new ArrayList<DoubleInterval>();
                if (!DoubleInterval.areIdentical(interval.m_lowerBoundInclusive,Double.NEGATIVE_INFINITY))
                    newIntervals.add(new DoubleInterval(Double.NEGATIVE_INFINITY,DoubleInterval.previousDouble(interval.m_lowerBoundInclusive)));
                if (!DoubleInterval.areIdentical(interval.m_upperBoundInclusive,Double.POSITIVE_INFINITY))
                    newIntervals.add(new DoubleInterval(DoubleInterval.nextDouble(interval.m_upperBoundInclusive),Double.POSITIVE_INFINITY));
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNDoubleSubset(newIntervals);
            }
            else {
                NoNaNDoubleSubset doubleSubset=(NoNaNDoubleSubset)valueSpaceSubset;
                DoubleInterval complementInterval1=null;
                if (!DoubleInterval.areIdentical(interval.m_lowerBoundInclusive,Double.NEGATIVE_INFINITY))
                    complementInterval1=new DoubleInterval(Double.NEGATIVE_INFINITY,DoubleInterval.previousDouble(interval.m_lowerBoundInclusive));
                DoubleInterval complementInterval2=null;
                if (!DoubleInterval.areIdentical(interval.m_upperBoundInclusive,Double.POSITIVE_INFINITY))
                    complementInterval2=new DoubleInterval(DoubleInterval.nextDouble(interval.m_upperBoundInclusive),Double.POSITIVE_INFINITY);
                List<DoubleInterval> oldIntervals=doubleSubset.m_intervals;
                List<DoubleInterval> newIntervals=new ArrayList<DoubleInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    DoubleInterval oldInterval=oldIntervals.get(index);
                    if (complementInterval1!=null) {
                        DoubleInterval intersection=oldInterval.intersectWith(complementInterval1);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (complementInterval2!=null) {
                        DoubleInterval intersection=oldInterval.intersectWith(complementInterval2);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new NoNaNDoubleSubset(newIntervals);
            }
        }
    }
    protected DoubleInterval getIntervalFor(DatatypeRestriction datatypeRestriction) {
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        double lowerBoundInclusive=Double.NEGATIVE_INFINITY;
        double upperBoundInclusive=Double.POSITIVE_INFINITY;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            double facetDataValue=(Double)datatypeRestriction.getFacetValue(index).getDataValue();
            if ((XSD_NS+"minInclusive").equals(facetURI)) {
                if (DoubleInterval.areIdentical(facetDataValue,+0.0))
                    facetDataValue=-0.0;
                if (DoubleInterval.isSmallerEqual(lowerBoundInclusive,facetDataValue))
                    lowerBoundInclusive=facetDataValue;
            }
            else if ((XSD_NS+"minExclusive").equals(facetURI)) {
                if (DoubleInterval.areIdentical(facetDataValue,-0.0))
                    facetDataValue=+0.0;
                facetDataValue=DoubleInterval.nextDouble(facetDataValue);
                if (DoubleInterval.isSmallerEqual(lowerBoundInclusive,facetDataValue))
                    lowerBoundInclusive=facetDataValue;
            }
            else if ((XSD_NS+"maxInclusive").equals(facetURI)) {
                if (DoubleInterval.areIdentical(facetDataValue,-0.0))
                    facetDataValue=+0.0;
                if (DoubleInterval.isSmallerEqual(facetDataValue,upperBoundInclusive))
                    upperBoundInclusive=facetDataValue;
            }
            else if ((XSD_NS+"maxExclusive").equals(facetURI)) {
                if (DoubleInterval.areIdentical(facetDataValue,+0.0))
                    facetDataValue=-0.0;
                facetDataValue=DoubleInterval.previousDouble(facetDataValue);
                if (DoubleInterval.isSmallerEqual(facetDataValue,upperBoundInclusive))
                    upperBoundInclusive=facetDataValue;
            }
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by xsd:double.");
        }
        if (DoubleInterval.isIntervalEmpty(lowerBoundInclusive,upperBoundInclusive))
            return null;
        else
            return new DoubleInterval(lowerBoundInclusive,upperBoundInclusive);
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
}
