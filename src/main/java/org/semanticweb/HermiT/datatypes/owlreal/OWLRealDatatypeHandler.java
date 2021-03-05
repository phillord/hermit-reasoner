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
package org.semanticweb.HermiT.datatypes.owlreal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * Implements a handler for the numeric datatypes derived from owl:real. This class
 * makes am important assumption that all data values are represented with objects
 * whose .equals() method behaves as expected. This means that, for numbers represented
 * using Java's classes, one needs to use the most specific class. For example, a decimal
 * 1.0 *must* be represented as Integer 1, and a rational 7/2 *must* be represented
 * as BigDecimal 3.5.
 */
public class OWLRealDatatypeHandler implements DatatypeHandler {
    protected static final String OWL_NS=Prefixes.s_semanticWebPrefixes.get("owl:");
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    private static final String MAXEXCLUSIVE = XSD_NS+"maxExclusive";
    private static final String MAXINCLUSIVE = XSD_NS+"maxInclusive";
    private static final String MINEXCLUSIVE = XSD_NS+"minExclusive";
    private static final String MININCLUSIVE = XSD_NS+"minInclusive";
    private static final String OWLRATIONAL = OWL_NS+"rational";
    private static final String XSDDECIMAL = XSD_NS+"decimal";
    private static final String OWLREAL = OWL_NS+"real";

    protected static final Map<String,NumberInterval> s_intervalsByDatatype;
    protected static final Map<String,ValueSpaceSubset> s_subsetsByDatatype;
    static {
        Object[][] initializer=new Object[][] {
            { OWLREAL,                    NumberRange.REAL,    MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { OWLRATIONAL,                NumberRange.RATIONAL,MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { XSDDECIMAL,                 NumberRange.DECIMAL, MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { XSD_NS+"integer",           NumberRange.INTEGER, MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { XSD_NS+"nonNegativeInteger",NumberRange.INTEGER, Integer.valueOf(0),                BoundType.INCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { XSD_NS+"positiveInteger",   NumberRange.INTEGER, Integer.valueOf(0),                BoundType.EXCLUSIVE,PlusInfinity.INSTANCE,                  BoundType.EXCLUSIVE },
            { XSD_NS+"nonPositiveInteger",NumberRange.INTEGER, MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,Integer.valueOf(0),                     BoundType.INCLUSIVE },
            { XSD_NS+"negativeInteger",   NumberRange.INTEGER, MinusInfinity.INSTANCE,            BoundType.EXCLUSIVE,Integer.valueOf(0),                     BoundType.EXCLUSIVE },
            { XSD_NS+"long",              NumberRange.INTEGER, Long.valueOf(Long.MIN_VALUE),      BoundType.INCLUSIVE,Long.valueOf(Long.MAX_VALUE),           BoundType.INCLUSIVE },
            { XSD_NS+"int",               NumberRange.INTEGER, Integer.valueOf(Integer.MIN_VALUE),BoundType.INCLUSIVE,Integer.valueOf(Integer.MAX_VALUE),     BoundType.INCLUSIVE },
            { XSD_NS+"short",             NumberRange.INTEGER, Integer.valueOf(Short.MIN_VALUE),  BoundType.INCLUSIVE,Integer.valueOf(Short.MAX_VALUE),       BoundType.INCLUSIVE },
            { XSD_NS+"byte",              NumberRange.INTEGER, Integer.valueOf(Byte.MIN_VALUE),   BoundType.INCLUSIVE,Integer.valueOf(Byte.MAX_VALUE),        BoundType.INCLUSIVE },
            { XSD_NS+"unsignedLong",      NumberRange.INTEGER, Integer.valueOf(0),                BoundType.INCLUSIVE,new BigInteger("18446744073709551615"), BoundType.INCLUSIVE },
            { XSD_NS+"unsignedInt",       NumberRange.INTEGER, Integer.valueOf(0),                BoundType.INCLUSIVE,Long.valueOf(4294967295L),              BoundType.INCLUSIVE },
            { XSD_NS+"unsignedShort",     NumberRange.INTEGER, Integer.valueOf(0),                BoundType.INCLUSIVE,Integer.valueOf(65535),                 BoundType.INCLUSIVE },
            { XSD_NS+"unsignedByte",      NumberRange.INTEGER, Integer.valueOf(0),                BoundType.INCLUSIVE,Integer.valueOf(255),                   BoundType.INCLUSIVE },
        };
        Map<String,NumberInterval> intervalsByDatatype=new ConcurrentHashMap<>(20, 0.75F, 1);
        Map<String,ValueSpaceSubset> subsetsByDatatype=new ConcurrentHashMap<>(20, 0.75F, 1);
        for (Object[] row : initializer) {
            String datatypeURI=(String)row[0];
            NumberInterval interval=new NumberInterval((NumberRange)row[1],NumberRange.NOTHING,(Number)row[2],(BoundType)row[3],(Number)row[4],(BoundType)row[5]);
            intervalsByDatatype.put(datatypeURI,interval);
            subsetsByDatatype.put(datatypeURI,new OWLRealValueSpaceSubset(interval));
        }
        s_intervalsByDatatype=intervalsByDatatype;
        s_subsetsByDatatype=subsetsByDatatype;
    }
    protected static final ValueSpaceSubset EMPTY_SUBSET=new OWLRealValueSpaceSubset();
    protected static final Set<String> s_supportedFacetURIs=new HashSet<>(Arrays.asList(MININCLUSIVE,MINEXCLUSIVE,MAXINCLUSIVE,MAXEXCLUSIVE));
    protected static final Map<String,Set<String>> s_datatypeSupersets;
    protected static final Map<String,Set<String>> s_datatypeDisjoints;
    static {
        Map<String,Set<String>> datatypeSupersets=new ConcurrentHashMap<>(20, 0.75F, 1);
        Map<String,Set<String>> datatypeDisjoints=new ConcurrentHashMap<>(20, 0.75F, 1);
        for (String datatypeURI : s_intervalsByDatatype.keySet()) {
            datatypeSupersets.put(datatypeURI,new HashSet<>());
            datatypeDisjoints.put(datatypeURI,new HashSet<>());
        }
        for (Map.Entry<String,NumberInterval> entry1 : s_intervalsByDatatype.entrySet()) {
            String datatypeURI1=entry1.getKey();
            NumberInterval interval1=entry1.getValue();
            for (Map.Entry<String,NumberInterval> entry2 : s_intervalsByDatatype.entrySet()) {
                String datatypeURI2=entry2.getKey();
                NumberInterval interval2=entry2.getValue();
                NumberInterval intersection=interval1.intersectWith(interval2);
                if (intersection==null)
                    datatypeDisjoints.get(datatypeURI1).add(datatypeURI2);
                else if (intersection==interval1) {
                    // The above test depends on the fact that NumberInterval.intersectWith() will not
                    // create a new interval object if interval1 is contained in interval2.
                    datatypeSupersets.get(datatypeURI1).add(datatypeURI2);
                }
            }
        }
        s_datatypeDisjoints=datatypeDisjoints;
        s_datatypeSupersets=datatypeSupersets;
    }

    @Override
    public Set<String> getManagedDatatypeURIs() {
        return s_intervalsByDatatype.keySet();
    }
    @Override
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_intervalsByDatatype.containsKey(datatypeURI);
        try {
            if (OWLREAL.equals(datatypeURI))
                throw new MalformedLiteralException(lexicalForm,datatypeURI);
            else if (OWLRATIONAL.equals(datatypeURI))
                return Numbers.parseRational(lexicalForm);
            else if (XSDDECIMAL.equals(datatypeURI))
                return Numbers.parseDecimal(lexicalForm);
            else
                return Numbers.parseInteger(lexicalForm);
        }
        catch (NumberFormatException error) {
            throw new MalformedLiteralException(lexicalForm,datatypeURI,error);
        }
    }
    @Override
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        assert s_intervalsByDatatype.containsKey(datatypeRestriction.getDatatypeURI());
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("A facet with URI '"+facetURI+"' is not supported on datatypes derived from owl:real. The owl:real derived datatypes support only xsd:minInclusive, xsd:maxInclusive, xsd:minExclusive, and xsd:maxExclusive, but the ontology contains a datatype restriction "+this.toString());
            Constant facetValue=datatypeRestriction.getFacetValue(index);
            Object facetDataValue=facetValue.getDataValue();
            if (!(facetDataValue instanceof Number))
                throw new UnsupportedFacetException("The '"+facetURI+"' facet takes only numbers as values when used on a datatype derived from owl:real, but the ontology contains a datatype restriction "+this.toString()+" where "+facetDataValue+" is not a number. ");
            if (!Numbers.isValidNumber((Number)facetDataValue))
                throw new UnsupportedFacetException("The facet with URI '"+facetURI+"' does not support '"+facetValue.toString()+"' as value. The value should be an integer, a decimal, or a rational, but this seems not to be the case in the datatype restriction "+this.toString());
        }
    }
    @Override
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        assert s_intervalsByDatatype.containsKey(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return s_subsetsByDatatype.get(datatypeRestriction.getDatatypeURI());
        NumberInterval interval=getIntervalFor(datatypeRestriction);
        if (interval==null)
            return EMPTY_SUBSET;
        else
            return new OWLRealValueSpaceSubset(interval);
    }
    @Override
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_intervalsByDatatype.containsKey(datatypeRestriction.getDatatypeURI());
        NumberInterval interval=getIntervalFor(datatypeRestriction);
        if (interval==null)
            return EMPTY_SUBSET;
        else {
            OWLRealValueSpaceSubset realSubset=(OWLRealValueSpaceSubset)valueSpaceSubset;
            List<NumberInterval> oldIntervals=realSubset.m_intervals;
            List<NumberInterval> newIntervals=new ArrayList<>();
            for (int index=0;index<oldIntervals.size();index++) {
                NumberInterval oldInterval=oldIntervals.get(index);
                NumberInterval intersection=oldInterval.intersectWith(interval);
                if (intersection!=null)
                    newIntervals.add(intersection);
            }
            if (newIntervals.isEmpty())
                return EMPTY_SUBSET;
            else
                return new OWLRealValueSpaceSubset(newIntervals);
        }
    }
    @Override
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_intervalsByDatatype.containsKey(datatypeRestriction.getDatatypeURI());
        NumberInterval interval=getIntervalFor(datatypeRestriction);
        if (interval==null)
            return valueSpaceSubset;
        else {
            NumberInterval complementInterval1=null;
            if (!interval.m_lowerBound.equals(MinusInfinity.INSTANCE))
                complementInterval1=new NumberInterval(NumberRange.REAL,NumberRange.NOTHING,MinusInfinity.INSTANCE,BoundType.EXCLUSIVE,interval.m_lowerBound,interval.m_lowerBoundType.getComplement());
            NumberInterval complementInterval2=null;
            if (!interval.m_baseRange.equals(NumberRange.REAL))
                complementInterval2=new NumberInterval(NumberRange.REAL,interval.m_baseRange,interval.m_lowerBound,interval.m_lowerBoundType,interval.m_upperBound,interval.m_upperBoundType);
            NumberInterval complementInterval3=null;
            if (!interval.m_upperBound.equals(PlusInfinity.INSTANCE))
                complementInterval3=new NumberInterval(NumberRange.REAL,NumberRange.NOTHING,interval.m_upperBound,interval.m_upperBoundType.getComplement(),PlusInfinity.INSTANCE,BoundType.EXCLUSIVE);
            OWLRealValueSpaceSubset realSubset=(OWLRealValueSpaceSubset)valueSpaceSubset;
            List<NumberInterval> oldIntervals=realSubset.m_intervals;
            List<NumberInterval> newIntervals=new ArrayList<>();
            for (int index=0;index<oldIntervals.size();index++) {
                NumberInterval oldInterval=oldIntervals.get(index);
                if (complementInterval1!=null) {
                    NumberInterval intersection=oldInterval.intersectWith(complementInterval1);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (complementInterval2!=null) {
                    NumberInterval intersection=oldInterval.intersectWith(complementInterval2);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (complementInterval3!=null) {
                    NumberInterval intersection=oldInterval.intersectWith(complementInterval3);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
            }
            if (newIntervals.isEmpty())
                return EMPTY_SUBSET;
            else
                return new OWLRealValueSpaceSubset(newIntervals);
        }
    }
    protected NumberInterval getIntervalFor(DatatypeRestriction datatypeRestriction) {
        NumberInterval baseInterval=s_intervalsByDatatype.get(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return baseInterval;
        NumberRange baseRange=baseInterval.m_baseRange;
        NumberRange excludedRange=baseInterval.m_excludedRange;
        Number lowerBound=baseInterval.m_lowerBound;
        BoundType lowerBoundType=baseInterval.m_lowerBoundType;
        Number upperBound=baseInterval.m_upperBound;
        BoundType upperBoundType=baseInterval.m_upperBoundType;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            Number facetDataValue=(Number)datatypeRestriction.getFacetValue(index).getDataValue();
            if (MININCLUSIVE.equals(facetURI)) {
                int comparison=Numbers.compare(facetDataValue,lowerBound);
                if (comparison>0) {
                    lowerBound=facetDataValue;
                    lowerBoundType=BoundType.INCLUSIVE;
                }
                // If the numbers are equal, nothing needs to be done to the bound type because
                // the existing one is at least as restrictive as INCLUSIVE.
            }
            else if (MINEXCLUSIVE.equals(facetURI)) {
                int comparison=Numbers.compare(facetDataValue,lowerBound);
                if (comparison>0) {
                    lowerBound=facetDataValue;
                    lowerBoundType=BoundType.EXCLUSIVE;
                }
                else if (comparison==0) {
                    // EXCLUSIVE is guaranteed to be the more restrictive bound.
                    lowerBoundType=BoundType.EXCLUSIVE;
                }
            }
            else if (MAXINCLUSIVE.equals(facetURI)) {
                int comparison=Numbers.compare(facetDataValue,upperBound);
                if (comparison<0) {
                    upperBound=facetDataValue;
                    upperBoundType=BoundType.INCLUSIVE;
                }
                // If the numbers are equal, nothing needs to be done to the bound type because
                // the existing one is at least as restrictive as INCLUSIVE.
            }
            else if (MAXEXCLUSIVE.equals(facetURI)) {
                int comparison=Numbers.compare(facetDataValue,upperBound);
                if (comparison<0) {
                    upperBound=facetDataValue;
                    upperBoundType=BoundType.EXCLUSIVE;
                }
                else if (comparison==0) {
                    // EXCLUSIVE is guaranteed to be the more restrictive bound.
                    upperBoundType=BoundType.EXCLUSIVE;
                }
            }
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by owl:real.");
        }
        if (NumberInterval.isIntervalEmpty(baseRange,excludedRange,lowerBound,lowerBoundType,upperBound,upperBoundType))
            return null;
        else
            return new NumberInterval(baseRange,excludedRange,lowerBound,lowerBoundType,upperBound,upperBoundType);
    }
    @Override
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert s_intervalsByDatatype.containsKey(subsetDatatypeURI);
        assert s_intervalsByDatatype.containsKey(supersetDatatypeURI);
        return s_datatypeSupersets.get(subsetDatatypeURI).contains(supersetDatatypeURI);
    }
    @Override
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert s_intervalsByDatatype.containsKey(datatypeURI1);
        assert s_intervalsByDatatype.containsKey(datatypeURI2);
        return s_datatypeDisjoints.get(datatypeURI1).contains(datatypeURI2);
    }
}
