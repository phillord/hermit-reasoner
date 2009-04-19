// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.floatnum;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class NoNaNFloatSubset implements ValueSpaceSubset {
    protected final List<FloatInterval> m_intervals;
    
    public NoNaNFloatSubset() {
        m_intervals=Collections.emptyList();
    }
    public NoNaNFloatSubset(FloatInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public NoNaNFloatSubset(List<FloatInterval> intervals) {
        m_intervals=intervals;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof Float) {
            float number=(Float)dataValue;
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
        buffer.append("xsd:float{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index==0)
                buffer.append('+');
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
