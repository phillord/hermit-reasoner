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
package org.semanticweb.HermiT.datatypes.rdfplainliteral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DatatypeRestriction;

import dk.brics.automaton.Automaton;

public class RDFPlainLiteralDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd:");
    protected static final String RDF_NS=Prefixes.s_semanticWebPrefixes.get("rdf:");
    protected static final Map<String,ValueSpaceSubset> s_subsetsByDatatype=new HashMap<String,ValueSpaceSubset>();
    static {
        s_subsetsByDatatype.put(RDF_NS+"PlainLiteral",new RDFPlainLiteralLengthValueSpaceSubset(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,0,Integer.MAX_VALUE),new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.PRESENT,0,Integer.MAX_VALUE)));
        s_subsetsByDatatype.put(XSD_NS+"string",new RDFPlainLiteralLengthValueSpaceSubset(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,0,Integer.MAX_VALUE)));
        registerPatternDatatype(XSD_NS+"normalizedString");
        registerPatternDatatype(XSD_NS+"token");
        registerPatternDatatype(XSD_NS+"Name");
        registerPatternDatatype(XSD_NS+"NCName");
        registerPatternDatatype(XSD_NS+"NMTOKEN");
        registerPatternDatatype(XSD_NS+"language");
    }
    protected static void registerPatternDatatype(String datatypeURI) {
        Automaton automaton=RDFPlainLiteralPatternValueSpaceSubset.getDatatypeAutomaton(datatypeURI);
        s_subsetsByDatatype.put(datatypeURI,new RDFPlainLiteralPatternValueSpaceSubset(automaton));
    }
    protected static final ValueSpaceSubset EMPTY_SUBSET=new RDFPlainLiteralLengthValueSpaceSubset();
    protected static final Map<String,Set<String>> s_datatypeSupersets=new HashMap<String,Set<String>>();
    static {
        String[][] initializer=new String[][] {
            { RDF_NS+"PlainLiteral",     RDF_NS+"PlainLiteral" },
            { XSD_NS+"string",           RDF_NS+"PlainLiteral", XSD_NS+"string" },
            { XSD_NS+"normalizedString", RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString" },
            { XSD_NS+"token",            RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token" },
            { XSD_NS+"Name",             RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"Name" },
            { XSD_NS+"NCName",           RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"Name",   XSD_NS+"NCName"},
            { XSD_NS+"NMTOKEN",          RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"NMTOKEN"},
            { XSD_NS+"language",         RDF_NS+"PlainLiteral", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"language" },
        };
        for (int datatype1Index=0;datatype1Index<initializer.length;datatype1Index++) {
            String datatype1URI=initializer[datatype1Index][0];
            Set<String> set=new HashSet<String>();
            for (int datatype2Index=1;datatype2Index<initializer[datatype1Index].length;datatype2Index++)
                set.add(initializer[datatype1Index][datatype2Index]);
            s_datatypeSupersets.put(datatype1URI,set);
        }
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_subsetsByDatatype.keySet();
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        Object dataValue;
        if ((RDF_NS+"PlainLiteral").equals(datatypeURI)) {
            int lastAt=lexicalForm.lastIndexOf('@');
            if (lastAt==-1)
                throw new MalformedLiteralException(lexicalForm,datatypeURI);
            String string=lexicalForm.substring(0,lastAt);
            String languageTag=lexicalForm.substring(lastAt+1);
            if (languageTag.length()==0)
                dataValue=string;
            else
                dataValue=new RDFPlainLiteralDataValue(string,languageTag);
        }
        else
            dataValue=lexicalForm;
        if (s_subsetsByDatatype.get(datatypeURI).containsDataValue(dataValue))
            return dataValue;
        else
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
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
                    throw new UnsupportedFacetException("The datatype rdf:PlainLiteral accepts only integers as facet values for the facet with URI '"+facetURI+"', but in the ontology we have a datatype restriction "+this.toString()+". The value '"+facetValue.toString()+"' does not seem to be an integer.");
            }
            else if ((XSD_NS+"pattern").equals(facetURI)) {
                if (facetDataValue instanceof String) {
                    String pattern=(String)facetDataValue;
                    if (!RDFPlainLiteralPatternValueSpaceSubset.isValidPattern(pattern))
                        throw new UnsupportedFacetException("String '"+pattern+"' in the datatype restriction "+this.toString()+" is not a valid regular expression.");
                }
                else
                    throw new UnsupportedFacetException("The facet with URI '"+facetURI+"' supports only strings as values, but '"+facetValue.toString()+"' in the restriction "+this.toString()+" does not seem to be a string. It is an instance of the class "+facetValue.getClass()+". ");
            }
            else if ((RDF_NS+"langRange").equals(facetURI)) {
                if (!(facetDataValue instanceof String))
                    throw new UnsupportedFacetException("The facet with URI '"+facetURI+"' supports only strings as values, but '"+facetValue.toString()+"' in the restriction "+this.toString()+" does not seem to be a string. It is an instance of the class "+facetValue.getClass()+". ");
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on rdf:PlainLiteral; only xsd:minLength, xsd:maxLength, xsd:length, xsd:pattern, and rdf:langRange are supported, but the ontology contains the restriction: "+this.toString());
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            return s_subsetsByDatatype.get(datatypeURI);
        else if (needsAutomatons(datatypeRestriction)) {
            Automaton automaton=getAutomatonFor(datatypeRestriction);
            if (automaton==null)
                return EMPTY_SUBSET;
            else
                return new RDFPlainLiteralPatternValueSpaceSubset(automaton);
        }
        else {
            RDFPlainLiteralLengthInterval[]intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return EMPTY_SUBSET;
            else if (intervals[0]!=null && intervals[1]!=null)
                return new RDFPlainLiteralLengthValueSpaceSubset(intervals[0],intervals[1]);
            else if (intervals[0]==null && intervals[1]!=null)
                return new RDFPlainLiteralLengthValueSpaceSubset(intervals[1]);
            else
                return new RDFPlainLiteralLengthValueSpaceSubset(intervals[0]);
        }
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if (valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else if ((valueSpaceSubset instanceof RDFPlainLiteralPatternValueSpaceSubset) || needsAutomatons(datatypeRestriction)) {
            Automaton restrictionAutomaton=getAutomatonFor(datatypeRestriction);
            if (restrictionAutomaton==null)
                return EMPTY_SUBSET;
            Automaton valueSpaceSubsetAutomaton=getAutomatonFor(valueSpaceSubset);
            if (valueSpaceSubsetAutomaton==null)
                return EMPTY_SUBSET;
            Automaton intersection=valueSpaceSubsetAutomaton.intersection(restrictionAutomaton);
            if (intersection.isEmpty())
                return EMPTY_SUBSET;
            else
                return new RDFPlainLiteralPatternValueSpaceSubset(intersection);
        }
        else {
            RDFPlainLiteralLengthInterval[] intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return EMPTY_SUBSET;
            else {
                List<RDFPlainLiteralLengthInterval> oldIntervals=((RDFPlainLiteralLengthValueSpaceSubset)valueSpaceSubset).m_intervals;
                List<RDFPlainLiteralLengthInterval> newIntervals=new ArrayList<RDFPlainLiteralLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    RDFPlainLiteralLengthInterval oldInterval=oldIntervals.get(index);
                    if (intervals[0]!=null) {
                        RDFPlainLiteralLengthInterval intersection=oldInterval.intersectWith(intervals[0]);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (intervals[1]!=null) {
                        RDFPlainLiteralLengthInterval intersection=oldInterval.intersectWith(intervals[1]);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new RDFPlainLiteralLengthValueSpaceSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if (valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else if ((valueSpaceSubset instanceof RDFPlainLiteralPatternValueSpaceSubset) || needsAutomatons(datatypeRestriction)) {
            Automaton restrictionAutomaton=getAutomatonFor(datatypeRestriction);
            if (restrictionAutomaton==null)
                return valueSpaceSubset;
            Automaton valueSpaceSubsetAutomaton=getAutomatonFor(valueSpaceSubset);
            if (valueSpaceSubsetAutomaton==null)
                return EMPTY_SUBSET;
            Automaton difference=valueSpaceSubsetAutomaton.minus(restrictionAutomaton);
            if (difference.isEmpty())
                return EMPTY_SUBSET;
            else
                return new RDFPlainLiteralPatternValueSpaceSubset(difference);
        }
        else {
            RDFPlainLiteralLengthInterval[] intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return valueSpaceSubset;
            else {
                List<RDFPlainLiteralLengthInterval> complementedIntervals=new ArrayList<RDFPlainLiteralLengthInterval>(4);
                if (intervals[0]!=null) {
                    if (intervals[0].m_minLength>0)
                        complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.PRESENT,0,intervals[0].m_minLength-1));
                    if (intervals[0].m_maxLength<Integer.MAX_VALUE)
                        complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.PRESENT,intervals[0].m_maxLength+1,Integer.MAX_VALUE));
                }
                else
                    complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.PRESENT,0,Integer.MAX_VALUE));
                if (intervals[1]!=null) {
                    if (intervals[1].m_minLength>0)
                        complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,0,intervals[1].m_minLength-1));
                    if (intervals[1].m_maxLength<Integer.MAX_VALUE)
                        complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,intervals[1].m_maxLength+1,Integer.MAX_VALUE));
                }
                else
                    complementedIntervals.add(new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,0,Integer.MAX_VALUE));
                List<RDFPlainLiteralLengthInterval> oldIntervals=((RDFPlainLiteralLengthValueSpaceSubset)valueSpaceSubset).m_intervals;
                List<RDFPlainLiteralLengthInterval> newIntervals=new ArrayList<RDFPlainLiteralLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    RDFPlainLiteralLengthInterval oldInterval=oldIntervals.get(index);
                    for (int complementedIndex=complementedIntervals.size()-1;complementedIndex>=0;--complementedIndex) {
                        RDFPlainLiteralLengthInterval complementedInterval=complementedIntervals.get(complementedIndex);
                        RDFPlainLiteralLengthInterval intersection=oldInterval.intersectWith(complementedInterval);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new RDFPlainLiteralLengthValueSpaceSubset(newIntervals);
            }
        }
    }
    protected boolean needsAutomatons(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        if (s_subsetsByDatatype.get(datatypeURI) instanceof RDFPlainLiteralLengthValueSpaceSubset) {
            for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
                String facetURI=datatypeRestriction.getFacetURI(index);
                if ((XSD_NS+"pattern").equals(facetURI) || (RDF_NS+"langRange").equals(facetURI))
                    return true;
            }
            return false;
        }
        else
            return true;
    }
    protected RDFPlainLiteralLengthInterval[] getIntervalsFor(DatatypeRestriction datatypeRestriction) {
        RDFPlainLiteralLengthInterval[] intervals=new RDFPlainLiteralLengthInterval[2];
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.get(datatypeURI) instanceof RDFPlainLiteralLengthValueSpaceSubset;
        int minLength=0;
        int maxLength=Integer.MAX_VALUE;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            assert (XSD_NS+"minLength").equals(facetURI) || (XSD_NS+"maxLength").equals(facetURI) || (XSD_NS+"length").equals(facetURI);
            int facetDataValue=(Integer)datatypeRestriction.getFacetValue(index).getDataValue();
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,facetDataValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,facetDataValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,facetDataValue);
                maxLength=Math.min(maxLength,facetDataValue);
            }
        }
        if (minLength<=maxLength) {
            if ((RDF_NS+"PlainLiteral").equals(datatypeURI)) {
                intervals[0]=new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.PRESENT,minLength,maxLength);
                intervals[1]=new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,minLength,maxLength);
            }
            else if ((XSD_NS+"string").equals(datatypeURI))
                intervals[1]=new RDFPlainLiteralLengthInterval(RDFPlainLiteralLengthInterval.LanguageTagMode.ABSENT,minLength,maxLength);
        }
        return intervals;
    }
    protected Automaton getAutomatonFor(ValueSpaceSubset valueSpaceSubset) {
        if (valueSpaceSubset instanceof RDFPlainLiteralPatternValueSpaceSubset)
            return ((RDFPlainLiteralPatternValueSpaceSubset)valueSpaceSubset).m_automaton;
        else
            return RDFPlainLiteralPatternValueSpaceSubset.toAutomaton((RDFPlainLiteralLengthValueSpaceSubset)valueSpaceSubset);
    }
    protected Automaton getAutomatonFor(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        Automaton automaton=RDFPlainLiteralPatternValueSpaceSubset.getDatatypeAutomaton(datatypeURI);
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
                Automaton facetAutomaton=RDFPlainLiteralPatternValueSpaceSubset.getPatternAutomaton(pattern);
                automaton=automaton.intersection(facetAutomaton);
            }
            else if ((RDF_NS+"langRange").equals(facetURI)) {
                String languageRange=(String)facetDataValue;
                Automaton languageRangeAutomaton=RDFPlainLiteralPatternValueSpaceSubset.getLanguageRangeAutomaton(languageRange);
                automaton=automaton.intersection(languageRangeAutomaton);
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' not supported on '"+datatypeURI+"'.");
        }
        if (minLength>maxLength)
            return null;
        else if (minLength!=0 || maxLength!=Integer.MAX_VALUE)
            automaton=automaton.intersection(RDFPlainLiteralPatternValueSpaceSubset.toAutomaton(minLength,maxLength));
        if (automaton.isEmpty())
            return null;
        else
            return automaton;
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        assert s_subsetsByDatatype.containsKey(subsetDatatypeURI);
        assert s_subsetsByDatatype.containsKey(supersetDatatypeURI);
        return s_datatypeSupersets.get(subsetDatatypeURI).contains(supersetDatatypeURI);
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        assert s_subsetsByDatatype.containsKey(datatypeURI1);
        assert s_subsetsByDatatype.containsKey(datatypeURI2);
        return false;
    }
}
