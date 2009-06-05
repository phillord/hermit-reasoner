/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model;

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
