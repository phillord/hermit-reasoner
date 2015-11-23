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
package org.semanticweb.HermiT.datatypes.datetime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
/**DateTimeValueSpaceSubset*/
public class DateTimeValueSpaceSubset implements ValueSpaceSubset {
    protected final List<DateTimeInterval> m_intervals;
    /**Empty interval.*/
    public DateTimeValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    /**
     * @param interval1 interval1
     * @param interval2 interval2
     */
    public DateTimeValueSpaceSubset(DateTimeInterval interval1,DateTimeInterval interval2) {
        m_intervals=new ArrayList<>(2);
        if (interval1!=null)
            m_intervals.add(interval1);
        if (interval2!=null)
            m_intervals.add(interval2);
    }
    /**
     * @param intervals intervals
     */
    public DateTimeValueSpaceSubset(List<DateTimeInterval> intervals) {
        m_intervals=intervals;
    }
    @Override
    public boolean hasCardinalityAtLeast(int number) {
        int left=number;
        for (int index=m_intervals.size()-1;left>0 && index>=0;--index)
            left=m_intervals.get(index).subtractSizeFrom(left);
        return left==0;
    }
    @Override
    public boolean containsDataValue(Object dataValue) {
        if (dataValue instanceof DateTime) {
            DateTime dateTime=(DateTime)dataValue;
            for (int index=m_intervals.size()-1;index>=0;--index)
                if (m_intervals.get(index).containsDateTime(dateTime))
                    return true;
        }
        return false;
    }
    @Override
    public void enumerateDataValues(Collection<Object> dataValues) {
        for (int index=m_intervals.size()-1;index>=0;--index)
            m_intervals.get(index).enumerateDateTimes(dataValues);
    }
    @Override
    public String toString() {
        StringBuilder buffer=new StringBuilder("xsd:dateTime{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index!=0)
                buffer.append(" + ");
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
