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

/**
 * Implements a string with a language tag -- that is, a data value from the value space of rdf:PlainLiteral.
 */
public class RDFPlainLiteralDataValue {
    protected final String m_string;
    protected final String m_languageTag;
    
    public RDFPlainLiteralDataValue(String string,String languageTag) {
        m_string=string;
        m_languageTag=languageTag;
    }
    public String getString() {
        return m_string;
    }
    public String getLanguageTag() {
        return m_languageTag;
    }
    public int hashCode() {
        return m_string.hashCode()*3+m_languageTag.hashCode();
    }
    public boolean equals(Object that) {
        if (this==that)
            return true;
        if (!(that instanceof RDFPlainLiteralDataValue))
            return false;
        RDFPlainLiteralDataValue thatValue=(RDFPlainLiteralDataValue)that;
        return thatValue.m_string.equals(m_string) && thatValue.m_languageTag.equals(m_languageTag);
    }
    public String toString() {
        return '\"'+m_string+"\"@"+m_languageTag;
    }
}
