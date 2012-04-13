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
package org.semanticweb.HermiT.datatypes.datetime;

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

/**
 * Implements a handler for xsd:dateTime.
 */
public class DateTimeDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    protected static final String XSD_DATE_TIME=XSD_NS+"dateTime";
    protected static final String XSD_DATE_TIME_STAMP=XSD_NS+"dateTimeStamp";
    protected static final DateTimeInterval INTERVAL_ALL_WITH_TIMEZONE=new DateTimeInterval(IntervalType.WITH_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,Long.MAX_VALUE,BoundType.EXCLUSIVE);
    protected static final DateTimeInterval INTERVAL_ALL_WITHOUT_TIMEZONE=new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,Long.MAX_VALUE,BoundType.EXCLUSIVE);
    protected static final DateTimeValueSpaceSubset ENTIRE_SUBSET=new DateTimeValueSpaceSubset(INTERVAL_ALL_WITH_TIMEZONE,INTERVAL_ALL_WITHOUT_TIMEZONE);
    protected static final DateTimeValueSpaceSubset WITH_TIMEZONE_SUBSET=new DateTimeValueSpaceSubset(INTERVAL_ALL_WITH_TIMEZONE,null);
    protected static final DateTimeValueSpaceSubset EMPTY_SUBSET=new DateTimeValueSpaceSubset();
    protected static final Set<String> s_managedDatatypeURIs=new HashSet<String>();
    static {
        s_managedDatatypeURIs.add(XSD_DATE_TIME);
        s_managedDatatypeURIs.add(XSD_DATE_TIME_STAMP);
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
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_managedDatatypeURIs.contains(datatypeURI);
        DateTime dateTime=DateTime.parse(lexicalForm);
        if (dateTime==null || (XSD_DATE_TIME_STAMP.equals(datatypeURI) && !dateTime.hasTimeZoneOffset()))
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        return dateTime;
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on datatypes derived from xsd:dateTime; only xsd:minInclusive, xsd:maxInclusive, xsd:minExclusive, and xsd:maxExclusive are supported, but the ontology contains the datatype restriction "+this.toString());
            Object facetDataValue=datatypeRestriction.getFacetValue(index).getDataValue();
            if (!(facetDataValue instanceof DateTime))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' supports only date/time values, but "+facetDataValue+" is not a date/time instance in the restriction "+this.toString()+".");
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0) {
            if (XSD_DATE_TIME.equals(datatypeRestriction.getDatatypeURI()))
                return ENTIRE_SUBSET;
            else
                return WITH_TIMEZONE_SUBSET;
        }
        DateTimeInterval[] intervals=getIntervalsFor(datatypeRestriction);
        if (intervals[0]==null && intervals[1]==null)
            return EMPTY_SUBSET;
        else
            return new DateTimeValueSpaceSubset(intervals[0],intervals[1]);
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        DateTimeInterval[] intervals=getIntervalsFor(datatypeRestriction);
        if (intervals[0]==null && intervals[1]==null)
            return EMPTY_SUBSET;
        else {
            DateTimeValueSpaceSubset dateTimeSubset=(DateTimeValueSpaceSubset)valueSpaceSubset;
            List<DateTimeInterval> oldIntervals=dateTimeSubset.m_intervals;
            List<DateTimeInterval> newIntervals=new ArrayList<DateTimeInterval>();
            for (int index=0;index<oldIntervals.size();index++) {
                DateTimeInterval oldInterval=oldIntervals.get(index);
                if (intervals[0]!=null) {
                    DateTimeInterval intersection=oldInterval.intersectWith(intervals[0]);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (intervals[1]!=null) {
                    DateTimeInterval intersection=oldInterval.intersectWith(intervals[1]);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
            }
            if (newIntervals.isEmpty())
                return EMPTY_SUBSET;
            else
                return new DateTimeValueSpaceSubset(newIntervals);
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        DateTimeInterval[] intervals=getIntervalsFor(datatypeRestriction);
        if (intervals[0]==null && intervals[1]==null)
            return valueSpaceSubset;
        else {
            List<DateTimeInterval> complementedIntervals=new ArrayList<DateTimeInterval>(4);
            if (intervals[0]==null)
                complementedIntervals.add(INTERVAL_ALL_WITH_TIMEZONE);
            else {
                if (intervals[0].m_lowerBound!=Long.MIN_VALUE)
                    complementedIntervals.add(new DateTimeInterval(IntervalType.WITH_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,intervals[0].m_lowerBound,intervals[0].m_lowerBoundType.getComplement()));
                if (intervals[0].m_upperBound!=Long.MAX_VALUE)
                    complementedIntervals.add(new DateTimeInterval(IntervalType.WITH_TIMEZONE,intervals[0].m_upperBound,intervals[0].m_upperBoundType.getComplement(),Long.MAX_VALUE,BoundType.EXCLUSIVE));
            }
            if (intervals[1]==null)
                complementedIntervals.add(INTERVAL_ALL_WITHOUT_TIMEZONE);
            else {
                if (intervals[1].m_lowerBound!=Long.MIN_VALUE)
                    complementedIntervals.add(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,intervals[1].m_lowerBound,intervals[1].m_lowerBoundType.getComplement()));
                if (intervals[1].m_upperBound!=Long.MAX_VALUE)
                    complementedIntervals.add(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,intervals[1].m_upperBound,intervals[1].m_upperBoundType.getComplement(),Long.MAX_VALUE,BoundType.EXCLUSIVE));
            }
            DateTimeValueSpaceSubset dateTimeSubset=(DateTimeValueSpaceSubset)valueSpaceSubset;
            List<DateTimeInterval> oldIntervals=dateTimeSubset.m_intervals;
            List<DateTimeInterval> newIntervals=new ArrayList<DateTimeInterval>();
            for (int index=0;index<oldIntervals.size();index++) {
                DateTimeInterval oldInterval=oldIntervals.get(index);
                for (int complementedIndex=0;complementedIndex<complementedIntervals.size();complementedIndex++) {
                    DateTimeInterval complementedInterval=complementedIntervals.get(complementedIndex);
                    DateTimeInterval intersection=oldInterval.intersectWith(complementedInterval);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
            }
            if (newIntervals.isEmpty())
                return EMPTY_SUBSET;
            else
                return new DateTimeValueSpaceSubset(newIntervals);
        }
    }
    protected DateTimeInterval[] getIntervalsFor(DatatypeRestriction datatypeRestriction) {
        DateTimeInterval[] intervals=new DateTimeInterval[2];
        intervals[0]=INTERVAL_ALL_WITH_TIMEZONE;
        if (XSD_DATE_TIME.equals(datatypeRestriction.getDatatypeURI()))
            intervals[1]=INTERVAL_ALL_WITHOUT_TIMEZONE;
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return intervals;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            DateTime facetDataValue=(DateTime)datatypeRestriction.getFacetValue(index).getDataValue();
            if ((XSD_NS+"minInclusive").equals(facetURI) || (XSD_NS+"minExclusive").equals(facetURI)) {
                BoundType boundType=(XSD_NS+"minInclusive").equals(facetURI) ? BoundType.INCLUSIVE : BoundType.EXCLUSIVE;
                if (facetDataValue.hasTimeZoneOffset()) {
                    if (intervals[0]!=null)
                        intervals[0]=intervals[0].intersectWith(new DateTimeInterval(IntervalType.WITH_TIMEZONE,facetDataValue.getTimeOnTimeline(),boundType,Long.MAX_VALUE,BoundType.EXCLUSIVE));
                    if (intervals[1]!=null)
                        intervals[1]=intervals[1].intersectWith(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,facetDataValue.getTimeOnTimeline()+DateTime.MAX_TIME_ZONE_CORRECTION,BoundType.EXCLUSIVE,Long.MAX_VALUE,BoundType.EXCLUSIVE));
                }
                else {
                    if (intervals[0]!=null)
                        intervals[0]=intervals[0].intersectWith(new DateTimeInterval(IntervalType.WITH_TIMEZONE,facetDataValue.getTimeOnTimeline()+DateTime.MAX_TIME_ZONE_CORRECTION,BoundType.EXCLUSIVE,Long.MAX_VALUE,BoundType.EXCLUSIVE));
                    if (intervals[1]!=null)
                        intervals[1]=intervals[1].intersectWith(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,facetDataValue.getTimeOnTimeline(),boundType,Long.MAX_VALUE,BoundType.EXCLUSIVE));
                }
            }
            else if ((XSD_NS+"maxInclusive").equals(facetURI) || (XSD_NS+"maxExclusive").equals(facetURI)) {
                BoundType boundType=(XSD_NS+"maxInclusive").equals(facetURI) ? BoundType.INCLUSIVE : BoundType.EXCLUSIVE;
                if (facetDataValue.hasTimeZoneOffset()) {
                    if (intervals[0]!=null)
                        intervals[0]=intervals[0].intersectWith(new DateTimeInterval(IntervalType.WITH_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,facetDataValue.getTimeOnTimeline(),boundType));
                    if (intervals[1]!=null)
                        intervals[1]=intervals[1].intersectWith(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,facetDataValue.getTimeOnTimeline()-DateTime.MAX_TIME_ZONE_CORRECTION,BoundType.EXCLUSIVE));
                }
                else {
                    if (intervals[0]!=null)
                        intervals[0]=intervals[0].intersectWith(new DateTimeInterval(IntervalType.WITH_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,facetDataValue.getTimeOnTimeline()-DateTime.MAX_TIME_ZONE_CORRECTION,BoundType.EXCLUSIVE));
                    if (intervals[1]!=null)
                        intervals[1]=intervals[1].intersectWith(new DateTimeInterval(IntervalType.WITHOUT_TIMEZONE,Long.MIN_VALUE,BoundType.EXCLUSIVE,facetDataValue.getTimeOnTimeline(),boundType));
                }
            }
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by xsd:dateTime.");
        }
        return intervals;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert XSD_DATE_TIME_STAMP.equals(subsetDatatypeURI) || XSD_DATE_TIME.equals(subsetDatatypeURI);
        assert XSD_DATE_TIME_STAMP.equals(supersetDatatypeURI) || XSD_DATE_TIME.equals(supersetDatatypeURI);
        if (XSD_DATE_TIME.equals(subsetDatatypeURI))
            return XSD_DATE_TIME.equals(supersetDatatypeURI);
        else
            return true;
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert XSD_DATE_TIME_STAMP.equals(datatypeURI1) || XSD_DATE_TIME.equals(datatypeURI1);
        assert XSD_DATE_TIME_STAMP.equals(datatypeURI2) || XSD_DATE_TIME.equals(datatypeURI2);
        return false;
    }
}
