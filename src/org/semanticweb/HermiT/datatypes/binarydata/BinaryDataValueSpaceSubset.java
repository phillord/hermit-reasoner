// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.binarydata;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class BinaryDataValueSpaceSubset implements ValueSpaceSubset {
    protected final List<BinaryDataLenghInterval> m_intervals;
    
    public BinaryDataValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    public BinaryDataValueSpaceSubset(BinaryDataLenghInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public BinaryDataValueSpaceSubset(List<BinaryDataLenghInterval> intervals) {
        m_intervals=intervals;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof BinaryData) {
            BinaryData binaryData=(BinaryData)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).contains(binaryData))
                    return true;
        }
        return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        for (int index=m_intervals.size()-1;index>=0;--index)
            m_intervals.get(index).enumerateValues(dataValues);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("binaryData{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index==0)
                buffer.append('+');
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
