// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.datetime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class DateTimeValueSpaceSubset implements ValueSpaceSubset {
    protected final List<DateTimeInterval> m_intervals;
    
    public DateTimeValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    public DateTimeValueSpaceSubset(DateTimeInterval interval1,DateTimeInterval interval2) {
        m_intervals=new ArrayList<DateTimeInterval>(2);
        if (interval1!=null)
            m_intervals.add(interval1);
        if (interval2!=null)
            m_intervals.add(interval2);
    }
    public DateTimeValueSpaceSubset(List<DateTimeInterval> intervals) {
        m_intervals=intervals;
    }
    public String getDatatypeURI() {
        return DateTimeDatatypeHandler.XSD_DATE_TIME;
    }
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof DateTime) {
            DateTime dateTime=(DateTime)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).containsDateTime(dateTime))
                    return true;
        }
        return false;
    }
    public void enumerateDataValues(Collection<Object> dataValues) {
        for (int index=m_intervals.size()-1;index>=0;--index)
            m_intervals.get(index).enumerateDateTimes(dataValues);
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append("xsd:dateTime{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index!=0)
                buffer.append(" + ");
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
