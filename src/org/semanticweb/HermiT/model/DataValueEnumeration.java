/* Copyright 2009 by the Oxford University Computing Laboratory
   
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
package org.semanticweb.HermiT.model;

import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;

/**
 * A data range that consists of a given set of constants.
 */
public class DataValueEnumeration extends DataRange {
    private static final long serialVersionUID=4663162424764302912L;

    protected final Object[] m_dataValues;
    
    protected DataValueEnumeration(Object[] dataValues) {
        m_dataValues=dataValues;
    }
    public int getNumberOfDataValues() {
        return m_dataValues.length;
    }
    public Object getDataValue(int index) {
        return m_dataValues[index];
    }
    public LiteralConcept getNegation() {
        return NegationDataRange.create(this);
    }
    public boolean isAlwaysTrue() {
        return false;
    }
    public boolean isAlwaysFalse() {
        return m_dataValues.length==0;
    }
    public boolean containsDataValue(Object dataValue) {
        for (int i=m_dataValues.length-1;i>=0;--i)
            if (m_dataValues[i].equals(dataValue))
                return true;
        return false;
    }
    public String toOrderedString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        buffer.append("{ ");
        SortedSet<String> dataValues = new TreeSet<String>();
        for (Object dataValue : m_dataValues) {
            dataValues.add(DatatypeRegistry.toString(prefixes,dataValue));
        }
        boolean isFirst = true;
        for (String s : dataValues) {
            if (!isFirst)
                buffer.append(' ');
            isFirst=false;
            buffer.append(s);
        }
        buffer.append(" }");
        return buffer.toString();
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        buffer.append("{ ");
        for (int index=0;index<m_dataValues.length;index++) {
            if (index>0)
                buffer.append(' ');
            Object dataValue=m_dataValues[index];
            buffer.append(DatatypeRegistry.toString(prefixes,dataValue));
        }
        buffer.append(" }");
        return buffer.toString();
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<DataValueEnumeration> s_interningManager=new InterningManager<DataValueEnumeration>() {
        protected boolean equal(DataValueEnumeration object1,DataValueEnumeration object2) {
            if (object1.m_dataValues.length!=object2.m_dataValues.length)
                return false;
            for (int index=object1.m_dataValues.length-1;index>=0;--index)
                if (!contains(object1.m_dataValues[index],object2.m_dataValues))
                    return false;
            return true;
        }
        protected boolean contains(Object object,Object[] dataValues) {
            for (int i=dataValues.length-1;i>=0;--i)
                if (dataValues[i].equals(object))
                    return true;
            return false;
        }
        protected int getHashCode(DataValueEnumeration object) {
            int hashCode=0;
            for (int index=object.m_dataValues.length-1;index>=0;--index)
                hashCode+=object.m_dataValues[index].hashCode();
            return hashCode;
        }
    };
    
    public static DataValueEnumeration create(Object[] dataValues) {
        return s_interningManager.intern(new DataValueEnumeration(dataValues));
    }
}
