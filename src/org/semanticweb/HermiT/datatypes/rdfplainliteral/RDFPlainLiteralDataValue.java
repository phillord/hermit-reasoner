// Copyright 2008 by Oxford University; see license.txt for details
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
