// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.rdftext;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import dk.brics.automaton.Automaton;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class RDFTextDatatypeHandler implements DatatypeHandler {
    protected static final Pattern s_languageTagPattern=Pattern.compile(
        "("+
            "([a-z]{2,3}"+
                "("+
                    "(-[a-z]{3}){0,3}"+             // extlang
                ")?"+
            ")|"+
            "[a-z]{4}|"+                            // 4ALPHA
            "[a-z]{5,8}"+                           // 5*8ALPHA
        ")"+                                        // language
        "(-[a-z]{4})?"+                             // script
        "(-([a-z]{2}|[0-9]{3}))?"+                  // region
        "(-([a-z0-9]{5,8}|([0-9][a-z0-9]{3})))*"+   // variant
        "(-([a-wy-z0-9](-[a-z0-9]{2,8})+))*"+       // extension
        "(-x(-[a-z0-9]{1,8})+)?"                    // privateuse
    );
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");
    protected static final String RDF_NS=Prefixes.s_semanticWebPrefixes.get("rdf");
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(RDFTextDataValue.class);
        s_managedDataValueClasses.add(String.class);
    }
    protected static final Map<String,ValueSpaceSubset> s_subsetsByDatatype=new HashMap<String,ValueSpaceSubset>();
    static {
        s_subsetsByDatatype.put(RDF_NS+"text",new RDFTextLengthValueSpaceSubset(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,0,Integer.MAX_VALUE),new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.PRESENT,0,Integer.MAX_VALUE)));
        s_subsetsByDatatype.put(XSD_NS+"string",new RDFTextLengthValueSpaceSubset(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,0,Integer.MAX_VALUE)));
        s_subsetsByDatatype.put(XSD_NS+"normalizedString",null); 
        s_subsetsByDatatype.put(XSD_NS+"token",null); 
        s_subsetsByDatatype.put(XSD_NS+"Name",null); 
        s_subsetsByDatatype.put(XSD_NS+"NCName",null); 
        s_subsetsByDatatype.put(XSD_NS+"NMTOKEN",null);
        s_subsetsByDatatype.put(XSD_NS+"language",null); 
    }
    protected static final ValueSpaceSubset EMPTY_SUBSET=new RDFTextLengthValueSpaceSubset();
    protected static final Map<String,Set<String>> s_datatypeSupersets=new HashMap<String,Set<String>>();
    static {
        String[][] initializer=new String[][] {
            { RDF_NS+"text",             RDF_NS+"text" },
            { XSD_NS+"string",           RDF_NS+"text", XSD_NS+"string" },
            { XSD_NS+"normalizedString", RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString" },
            { XSD_NS+"token",            RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token" },
            { XSD_NS+"Name",             RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"Name" },
            { XSD_NS+"NCName",           RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"Name",   XSD_NS+"NCName"},
            { XSD_NS+"MKTOKEN",          RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"NMTOKEN"},
            { XSD_NS+"language",         RDF_NS+"text", XSD_NS+"string", XSD_NS+"normalizedString", XSD_NS+"token", XSD_NS+"language" },
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
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        if (dataValue instanceof String)
            return '\"'+dataValue.toString()+'\"';
        else {
            RDFTextDataValue rdfTextDataValue=(RDFTextDataValue)dataValue;
            return '\"'+rdfTextDataValue.getString()+"\"@"+rdfTextDataValue.getLanguageTag();
        }
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if ((RDF_NS+"text").equals(datatypeURI)) {
            int lastAt=lexicalForm.lastIndexOf('@');
            if (lastAt==-1)
                throw new MalformedLiteralException(lexicalForm,datatypeURI);
            String string=lexicalForm.substring(0,lastAt);
            for (int index=string.length()-1;index>=0;--index)
                if (!isRDFTextCharacter(string.charAt(index)))
                    throw new MalformedLiteralException(lexicalForm,datatypeURI);
            String languageTag=lexicalForm.substring(lastAt+1);
            if (languageTag.length()==0)
                return string;
            else {
                if (s_languageTagPattern.matcher(languageTag).matches())
                    return new RDFTextDataValue(string,languageTag);
                else
                    throw new MalformedLiteralException(lexicalForm,datatypeURI);
            }
        }
        else if ((XSD_NS+"string").equals(datatypeURI)) {
            for (int index=lexicalForm.length()-1;index>=0;--index)
                if (!isRDFTextCharacter(lexicalForm.charAt(index)))
                    throw new MalformedLiteralException(lexicalForm,datatypeURI);
            return lexicalForm;
        }
        else
            throw new IllegalArgumentException("Temporary exception.");
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
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
                    if (!RDFTextPatternValueSpaceSubset.isValidPattern(pattern))
                        throw new UnsupportedFacetException("String '"+pattern+"' is not a valid regular expression.");
                }
                else
                    throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support value of type "+facetValue.getClass()+" as value.");
            }
            else if ((RDF_NS+"langRange").equals(facetURI)) {
                if (!(facetValue instanceof String))
                    throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support value of type "+facetValue.getClass()+" as value.");
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on rdf:text.");
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
                return new RDFTextPatternValueSpaceSubset(automaton);
        }
        else {
            RDFTextLengthInterval[]intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return EMPTY_SUBSET;
            else if (intervals[0]!=null && intervals[1]!=null)
                return new RDFTextLengthValueSpaceSubset(intervals[0],intervals[1]);
            else if (intervals[0]==null && intervals[1]!=null)
                return new RDFTextLengthValueSpaceSubset(intervals[1]);
            else
                throw new IllegalStateException("Internal error: invalid intervals for strings.");
        }
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if (valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else if ((valueSpaceSubset instanceof RDFTextPatternValueSpaceSubset) || needsAutomatons(datatypeRestriction)) {
            Automaton restrictionAutomaton=getAutomatonFor(datatypeRestriction);
            if (restrictionAutomaton==null)
                return EMPTY_SUBSET;
            Automaton valueSpaceAutomaton;
            if (valueSpaceSubset instanceof RDFTextPatternValueSpaceSubset)
                valueSpaceAutomaton=((RDFTextPatternValueSpaceSubset)valueSpaceSubset).m_automaton;
            else
                valueSpaceAutomaton=RDFTextPatternValueSpaceSubset.toAutomaton(((RDFTextLengthValueSpaceSubset)valueSpaceSubset).m_intervals);
            Automaton intersection=valueSpaceAutomaton.intersection(restrictionAutomaton);
            if (intersection.isEmpty())
                return EMPTY_SUBSET;
            else
                return new RDFTextPatternValueSpaceSubset(intersection);
        }
        else {
            RDFTextLengthInterval[] intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return EMPTY_SUBSET;
            else {
                List<RDFTextLengthInterval> oldIntervals=((RDFTextLengthValueSpaceSubset)valueSpaceSubset).m_intervals;
                List<RDFTextLengthInterval> newIntervals=new ArrayList<RDFTextLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    RDFTextLengthInterval oldInterval=oldIntervals.get(index);
                    if (intervals[0]!=null) {
                        RDFTextLengthInterval intersection=oldInterval.intersectWith(intervals[0]);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (intervals[1]!=null) {
                        RDFTextLengthInterval intersection=oldInterval.intersectWith(intervals[1]);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new RDFTextLengthValueSpaceSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.containsKey(datatypeURI);
        if (valueSpaceSubset==EMPTY_SUBSET)
            return EMPTY_SUBSET;
        else if ((valueSpaceSubset instanceof RDFTextPatternValueSpaceSubset) || needsAutomatons(datatypeRestriction)) {
            Automaton restrictionAutomaton=getAutomatonFor(datatypeRestriction);
            if (restrictionAutomaton==null)
                return valueSpaceSubset;
            Automaton valueSpaceAutomaton;
            if (valueSpaceSubset instanceof RDFTextPatternValueSpaceSubset)
                valueSpaceAutomaton=((RDFTextPatternValueSpaceSubset)valueSpaceSubset).m_automaton;
            else
                valueSpaceAutomaton=RDFTextPatternValueSpaceSubset.toAutomaton(((RDFTextLengthValueSpaceSubset)valueSpaceSubset).m_intervals);
            Automaton intersection=valueSpaceAutomaton.intersection(restrictionAutomaton.complement());
            if (intersection.isEmpty())
                return EMPTY_SUBSET;
            else
                return new RDFTextPatternValueSpaceSubset(intersection);
        }
        else {
            RDFTextLengthInterval[] intervals=getIntervalsFor(datatypeRestriction);
            if (intervals[0]==null && intervals[1]==null)
                return valueSpaceSubset;
            else {
                List<RDFTextLengthInterval> complementedIntervals=new ArrayList<RDFTextLengthInterval>(4);
                if (intervals[0]!=null) {
                    if (intervals[0].m_minLength>0)
                        complementedIntervals.add(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.PRESENT,0,intervals[0].m_minLength-1));
                    if (intervals[0].m_maxLength<Integer.MAX_VALUE)
                        complementedIntervals.add(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.PRESENT,intervals[0].m_maxLength+1,Integer.MAX_VALUE));
                }
                if (intervals[1]!=null) {
                    if (intervals[1].m_minLength>0)
                        complementedIntervals.add(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,0,intervals[1].m_minLength-1));
                    if (intervals[1].m_maxLength<Integer.MAX_VALUE)
                        complementedIntervals.add(new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,intervals[1].m_maxLength+1,Integer.MAX_VALUE));
                }
                List<RDFTextLengthInterval> oldIntervals=((RDFTextLengthValueSpaceSubset)valueSpaceSubset).m_intervals;
                List<RDFTextLengthInterval> newIntervals=new ArrayList<RDFTextLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    RDFTextLengthInterval oldInterval=oldIntervals.get(index);
                    for (int complementedIndex=complementedIntervals.size()-1;complementedIndex>=0;--complementedIndex) {
                        RDFTextLengthInterval complementedInterval=complementedIntervals.get(complementedIndex);
                        RDFTextLengthInterval intersection=oldInterval.intersectWith(complementedInterval);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY_SUBSET;
                else
                    return new RDFTextLengthValueSpaceSubset(newIntervals);
            }
        }
    }
    protected boolean needsAutomatons(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        if (s_subsetsByDatatype.get(datatypeURI) instanceof RDFTextLengthValueSpaceSubset) {
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
    protected RDFTextLengthInterval[] getIntervalsFor(DatatypeRestriction datatypeRestriction) {
        RDFTextLengthInterval[] intervals=new RDFTextLengthInterval[2];
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_subsetsByDatatype.get(datatypeURI) instanceof RDFTextLengthValueSpaceSubset;
        int minLength=0;
        int maxLength=Integer.MAX_VALUE;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            assert !(XSD_NS+"pattern").equals(facetURI) && !(RDF_NS+"langRange").equals(facetURI);
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,(Integer)facetValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,(Integer)facetValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,(Integer)facetValue);
                maxLength=Math.min(maxLength,(Integer)facetValue);
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' not supported on '"+datatypeURI+"'.");
        }
        if (minLength<=maxLength) {
            if ((RDF_NS+"text").equals(datatypeURI)) {
                intervals[0]=new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.PRESENT,minLength,maxLength);
                intervals[1]=new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,minLength,maxLength);
            }
            else if ((XSD_NS+"string").equals(datatypeURI))
                intervals[1]=new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode.ABSENT,minLength,maxLength);
            else
                throw new IllegalStateException("Internal error: invalid datatype and facets.");
        }
        return intervals;
    }
    protected Automaton getAutomatonFor(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        Automaton automaton=null;
        if ((XSD_NS+"string").equals(datatypeURI))
            automaton=RDFTextPatternValueSpaceSubset.s_anyXSDString;
        else if ((RDF_NS+"text").equals(datatypeURI))
            automaton=RDFTextPatternValueSpaceSubset.s_anyRDFText;
        else
            automaton=((RDFTextPatternValueSpaceSubset)s_subsetsByDatatype.get(datatypeURI)).m_automaton;
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
                Automaton facetAutomaton=RDFTextPatternValueSpaceSubset.getPatternAutomaton(pattern);
                automaton=automaton.intersection(facetAutomaton);
            }
            else if ((RDF_NS+"langRange").equals(facetURI)) {
                String languageRange=(String)facetValue;
                Automaton languageRangeAutomaton=RDFTextPatternValueSpaceSubset.getLanguageRangeAutomaton(languageRange);
                automaton=automaton.intersection(languageRangeAutomaton);
            }
            else
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' not supported on '"+datatypeURI+"'.");
        }
        if (minLength>maxLength)
            return null;
        else if (minLength!=0 || maxLength!=Integer.MAX_VALUE)
            automaton=automaton.intersection(RDFTextPatternValueSpaceSubset.toAutomaton(minLength,maxLength));
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
    public static boolean isRDFTextCharacter(char c) {
        return c==0x9 || c==0xA || c==0xD || (0x20<=c && c<=0xD7FF) || (0xE000<=c && c<=0xFFFD);
    }
}
