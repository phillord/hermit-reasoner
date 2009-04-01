package org.semanticweb.HermiT.datatypes.common;

import java.util.Collection;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class EmptyValueSpaceSubset implements ValueSpaceSubset {
    protected final String m_datatypeURI;

    public EmptyValueSpaceSubset(String datatypeURI) {
        m_datatypeURI=datatypeURI;
    }
    public String getDatatypeURI() {
        return m_datatypeURI;
    }
    public boolean hasCardinalityAtLeast(int number) {
        return number<=0;
    }
    public boolean containsDataValue(Object dataValue) {
        return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
    }
}
