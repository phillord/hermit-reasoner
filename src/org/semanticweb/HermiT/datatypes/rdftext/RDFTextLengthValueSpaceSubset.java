// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.rdftext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class RDFTextLengthValueSpaceSubset implements ValueSpaceSubset {
    protected final List<RDFTextLengthInterval> m_intervals;
    
    public RDFTextLengthValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    public RDFTextLengthValueSpaceSubset(RDFTextLengthInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public RDFTextLengthValueSpaceSubset(RDFTextLengthInterval interval1,RDFTextLengthInterval interval2) {
        m_intervals=new ArrayList<RDFTextLengthInterval>(2);
        m_intervals.add(interval1);
        m_intervals.add(interval2);
    }
    public RDFTextLengthValueSpaceSubset(List<RDFTextLengthInterval> intervals) {
        m_intervals=intervals;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof String) {
            String value=(String)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).contains(value))
                    return true;
        }
        else if (dataValue instanceof RDFTextDataValue) {
            RDFTextDataValue value=(RDFTextDataValue)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).contains(value))
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
        buffer.append("rdf:text{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index==0)
                buffer.append('+');
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
