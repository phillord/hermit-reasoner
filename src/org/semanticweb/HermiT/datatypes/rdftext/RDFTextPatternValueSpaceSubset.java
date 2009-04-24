// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.rdftext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.Datatypes;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class RDFTextPatternValueSpaceSubset implements ValueSpaceSubset {
    public static final char SEPARATOR='\u0001';
    public static final char TRAILER='-';
    protected static final Automaton s_separator;
    protected static final Automaton s_anyChar;
    protected static final Automaton s_anyString;
    protected static final Automaton s_nonemptyString;
    protected static final Automaton s_trailer;
    protected static final Automaton s_anyLangTag;
    protected static final Automaton s_nonemptyLangTag;
    protected static final Automaton s_noLangTag;
    protected static final Automaton s_anyRDFText;
    protected static final Automaton s_anyXSDString;
    static {
        s_separator=BasicAutomata.makeChar(SEPARATOR);
        s_anyChar=BasicAutomata.makeAnyChar();
        s_anyString=BasicAutomata.makeAnyString();
        s_nonemptyString=s_anyString.minus(BasicAutomata.makeEmptyString());
        s_trailer=BasicAutomata.makeChar(TRAILER);
        s_anyLangTag=s_separator.concatenate(s_anyString).concatenate(s_trailer);
        s_nonemptyLangTag=s_separator.concatenate(s_nonemptyString).concatenate(s_trailer);
        s_noLangTag=s_separator.concatenate(s_trailer);
        s_anyRDFText=s_anyString.concatenate(s_anyLangTag);
        s_anyXSDString=s_anyString.concatenate(s_noLangTag);
    }
    
    protected final Automaton m_automaton;
    
    public RDFTextPatternValueSpaceSubset(Automaton automaton) {
        m_automaton=automaton;
    }
    public boolean hasCardinalityAtLeast(int number) {
        Set<String> elements=m_automaton.getFiniteStrings(number);
        if (elements==null)
            return true;
        else
            return elements.size()>=number;
    }
    public boolean containsDataValue(Object dataValue) {
        String lexicalForm;
        String languageTag;
        if (dataValue instanceof String) {
            lexicalForm=(String)dataValue;
            languageTag="";
        }
        else {
            RDFTextDataValue value=(RDFTextDataValue)dataValue;
            lexicalForm=value.getString();
            languageTag=value.getLanguageTag().toLowerCase();
        }
        return m_automaton.run(lexicalForm+SEPARATOR+languageTag+TRAILER);
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        Set<String> elements=m_automaton.getFiniteStrings();
        if (elements==null)
            throw new IllegalStateException("The value space range is infinite.");
        else
            dataValues.addAll(elements);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("rdf:text{");
        buffer.append(m_automaton.toString());
        buffer.append('}');
        return buffer.toString();
    }
    public static Automaton toAutomaton(List<RDFTextLengthInterval> intervals) {
        Automaton result=null;
        for (int intervalIndex=intervals.size()-1;intervalIndex>=0;--intervalIndex) {
            RDFTextLengthInterval interval=intervals.get(intervalIndex);
            Automaton stringPart;
            if (interval.m_maxLength==Integer.MAX_VALUE) {
                if (interval.m_minLength==0)
                    stringPart=BasicAutomata.makeAnyString();
                else
                    stringPart=BasicOperations.repeat(s_anyChar,interval.m_minLength);
            }
            else
                stringPart=BasicOperations.repeat(s_anyChar,interval.m_minLength,interval.m_maxLength);
            Automaton intervalAutomaton;
            if (interval.m_languageTagMode==RDFTextLengthInterval.LanguageTagMode.ABSENT)
                intervalAutomaton=stringPart.concatenate(s_noLangTag);
            else
                intervalAutomaton=stringPart.concatenate(s_nonemptyLangTag);
            if (result==null)
                result=intervalAutomaton;
            else
                result=result.intersection(intervalAutomaton);
        }
        return result;
    }
    public static Automaton toAutomaton(int minLength,int maxLength) {
        Automaton result;
        if (maxLength==Integer.MAX_VALUE) {
            if (minLength==0)
                result=BasicAutomata.makeAnyString();
            else
                result=BasicOperations.repeat(s_anyChar,minLength);
        }
        else
            result=BasicOperations.repeat(s_anyChar,minLength,maxLength);
        return result.concatenate(s_anyLangTag);
    }
    public static boolean isValidPattern(String pattern) {
        try {
            new RegExp(pattern);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }
    public static Automaton getPatternAutomaton(String pattern) {
        Automaton patternAutomaton=new RegExp(pattern).toAutomaton();
        return patternAutomaton.concatenate(s_separator).concatenate(s_anyLangTag).concatenate(s_trailer);
    }
    public static Automaton getLanguageRangeAutomaton(String languageRange) {
        Automaton result=s_nonemptyString.concatenate(s_separator);
        if ("*".equals(languageRange))
            result=result.concatenate(s_nonemptyString);
        else {
            Automaton languageRangeAutomaton=BasicAutomata.makeString(languageRange.toLowerCase());
            result=result.concatenate(languageRangeAutomaton);
        }
        return result.concatenate(s_trailer);
    }
    public static Automaton getDatatypeAutomaton(String datatypeURI) {
        int hashPosition=datatypeURI.lastIndexOf('#');
        String datatypeName=datatypeURI.substring(hashPosition+1);
        Automaton automaton=Datatypes.get(datatypeName);
        return automaton.concatenate(s_anyLangTag);
    }
}
