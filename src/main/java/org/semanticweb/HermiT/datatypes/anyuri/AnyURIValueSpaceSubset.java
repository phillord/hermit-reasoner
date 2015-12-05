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
package org.semanticweb.HermiT.datatypes.anyuri;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.Datatypes;
import dk.brics.automaton.RegExp;

public class AnyURIValueSpaceSubset implements ValueSpaceSubset {
    protected static final Automaton s_anyChar;
    protected static final Automaton s_anyString;
    protected static final Automaton s_anyURI;
    protected static final Automaton s_empty;
    static {
        s_anyChar=BasicAutomata.makeAnyChar();
        s_anyString=BasicAutomata.makeAnyString();
        s_anyURI=Datatypes.get("URI");
        s_empty=BasicAutomata.makeEmpty();
    }

    protected final Automaton m_automaton;
    
    public AnyURIValueSpaceSubset(Automaton automaton) {
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
        if (dataValue instanceof URI)
            return m_automaton.run(dataValue.toString());
        else
            return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        Set<String> elements=m_automaton.getFiniteStrings();
        if (elements==null)
            throw new IllegalStateException("The value space range is infinite.");
        else {
            for (String element : elements)
                dataValues.add(URI.create(element));
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("xsd:anyURI{");
        buffer.append(m_automaton.toString());
        buffer.append('}');
        return buffer.toString();
    }
    public static Automaton toAutomaton(int minLength,int maxLength) {
        assert minLength<=maxLength;
        if (maxLength==Integer.MAX_VALUE) {
            if (minLength==0)
                return s_anyString;
            else
                return s_anyString.intersection(BasicOperations.repeat(s_anyChar,minLength));
        }
        else
            return s_anyString.intersection(BasicOperations.repeat(s_anyChar,minLength,maxLength));
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
        return new RegExp(pattern).toAutomaton();
    }
}
