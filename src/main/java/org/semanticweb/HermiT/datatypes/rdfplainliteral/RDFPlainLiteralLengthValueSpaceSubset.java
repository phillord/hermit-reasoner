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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;

public class RDFPlainLiteralLengthValueSpaceSubset implements ValueSpaceSubset {
    protected final List<RDFPlainLiteralLengthInterval> m_intervals;
    
    public RDFPlainLiteralLengthValueSpaceSubset() {
        m_intervals=Collections.emptyList();
    }
    public RDFPlainLiteralLengthValueSpaceSubset(RDFPlainLiteralLengthInterval interval) {
        m_intervals=Collections.singletonList(interval);
    }
    public RDFPlainLiteralLengthValueSpaceSubset(RDFPlainLiteralLengthInterval interval1,RDFPlainLiteralLengthInterval interval2) {
        m_intervals=new ArrayList<RDFPlainLiteralLengthInterval>(2);
        m_intervals.add(interval1);
        m_intervals.add(interval2);
    }
    public RDFPlainLiteralLengthValueSpaceSubset(List<RDFPlainLiteralLengthInterval> intervals) {
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
        else if (dataValue instanceof RDFPlainLiteralDataValue) {
            RDFPlainLiteralDataValue value=(RDFPlainLiteralDataValue)dataValue;
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
        buffer.append("rdf:PlainLiteral{");
        for (int index=0;index<m_intervals.size();index++) {
            if (index!=0)
                buffer.append(" + ");
            buffer.append(m_intervals.get(index).toString());
        }
        buffer.append('}');
        return buffer.toString();
    }
}
