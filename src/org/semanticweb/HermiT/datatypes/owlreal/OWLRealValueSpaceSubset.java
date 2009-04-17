// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.owlreal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class OWLRealValueSpaceSubset implements ValueSpaceSubset {
    protected final List<NumberInterval> m_intervals;
    
    public OWLRealValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    public OWLRealValueSpaceSubset(NumberInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public OWLRealValueSpaceSubset(List<NumberInterval> intervals) {
        m_intervals=intervals;
    }
    public String getDatatypeURI() {
        return OWLRealDatatypeHandler.OWL_NS+"real";
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof Number) {
            Number number=(Number)dataValue;
            if (Numbers.isValidNumber(number)) {
                for (int index=m_intervals.size()-1;index>=0;--index)
                    if (m_intervals.get(index).containsNumber(number))
                        return true;
            }
        }
        return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        for (int index=m_intervals.size()-1;index>=0;--index)
            m_intervals.get(index).enumerateNumbers(dataValues);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("owl:real{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index!=0)
                buffer.append(" + ");
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
