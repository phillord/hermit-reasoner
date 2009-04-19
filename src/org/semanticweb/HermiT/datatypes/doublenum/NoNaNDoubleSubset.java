// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.doublenum;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class NoNaNDoubleSubset implements ValueSpaceSubset {
    protected final List<DoubleInterval> m_intervals;
    
    public NoNaNDoubleSubset() {
        m_intervals=Collections.emptyList();
    }
    public NoNaNDoubleSubset(DoubleInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public NoNaNDoubleSubset(List<DoubleInterval> intervals) {
        m_intervals=intervals;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof Double) {
            double number=(Double)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).contains(number))
                    return true;
        }
        return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        for (int index=m_intervals.size()-1;index>=0;--index)
            m_intervals.get(index).enumerateNumbers(dataValues);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("xsd:double{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index==0)
                buffer.append('+');
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
