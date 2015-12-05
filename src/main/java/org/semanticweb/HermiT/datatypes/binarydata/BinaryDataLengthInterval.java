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
package org.semanticweb.HermiT.datatypes.binarydata;

import java.util.Collection;

public class BinaryDataLengthInterval {
    protected final BinaryDataType m_binaryDataType;
    protected final int m_minLength;
    protected final int m_maxLength;

    public BinaryDataLengthInterval(BinaryDataType binaryDataType,int minLength,int maxLength) {
        assert !isIntervalEmpty(binaryDataType,minLength,maxLength);
        m_binaryDataType=binaryDataType;
        m_minLength=minLength;
        m_maxLength=maxLength;
    }
    /**
     * Computes the intersection of this interval with the supplied one. If the two intervals do not intersect, the result is null.
     */
    public BinaryDataLengthInterval intersectWith(BinaryDataLengthInterval that) {
        if (m_binaryDataType!=that.m_binaryDataType)
            return null;
        int newMinLength=Math.max(m_minLength,that.m_minLength);
        int newMaxLength=Math.min(m_maxLength,that.m_maxLength);
        if (isIntervalEmpty(m_binaryDataType,newMinLength,newMaxLength))
            return null;
        else if (isEqual(m_binaryDataType,newMinLength,newMaxLength))
            return this;
        else if (that.isEqual(m_binaryDataType,newMinLength,newMaxLength))
            return that;
        else
            return new BinaryDataLengthInterval(m_binaryDataType,newMinLength,newMaxLength);
    }
    protected boolean isEqual(BinaryDataType binaryDataType,int minLength,int maxLength) {
        return m_binaryDataType==binaryDataType && m_minLength==minLength && m_maxLength==maxLength;
    }
    public int subtractSizeFrom(int argument) {
        if (argument<=0 || m_maxLength==Integer.MAX_VALUE)
            return 0;
        // If m_minLength or m_maxLength is more than 7, then the number of
        // values exceeds the range of long. 
        if (m_minLength>=7 || m_maxLength>=7)
            return 0;
        // We now compute the actual number of values.
        long size=getNumberOfValuesOfLength(m_maxLength)-getNumberOfValuesOfLength(m_minLength-1);
        return (int)Math.max(argument-size,0L);
    }
    protected long getNumberOfValuesOfLength(int length) {
        if (length<0)
            return 0L;
        else {
            long valuesOfLength=1L;
            long total=1L;
            for (int i=1;i<=length;i++) {
                valuesOfLength*=256L;
                total+=valuesOfLength;
            }
            return total;
        }
    }
    public boolean contains(BinaryData value) {
        return m_binaryDataType==value.getBinaryDataType() && m_minLength<=value.getNumberOfBytes() && value.getNumberOfBytes()<=m_maxLength;
    }
    public void enumerateValues(Collection<Object> values) {
        if (m_maxLength==Integer.MAX_VALUE)
            throw new IllegalStateException("Internal error: the data range is infinite!");
        if (m_minLength==0)
            values.add(new BinaryData(m_binaryDataType,new byte[0]));
        byte[] temp=new byte[m_maxLength];
        processPosition(temp,values,0);
    }
    protected void processPosition(byte[] temp,Collection<Object> values,int position) {
        if (position<m_maxLength) {
            for (int b=0;b<=255;b++) {
                temp[position]=(byte)b;
                if (m_minLength<=position+1) {
                    byte[] copy=new byte[position+1];
                    System.arraycopy(temp,0,copy,0,copy.length);
                    values.add(new BinaryData(m_binaryDataType,copy));
                }
                processPosition(temp,values,position+1);
            }
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        buffer.append(m_binaryDataType.toString());
        buffer.append('[');
        buffer.append(m_minLength);
        buffer.append("..");
        if (m_maxLength==Integer.MAX_VALUE)
            buffer.append("+INF");
        else
            buffer.append(m_maxLength);
        buffer.append(']');
        return buffer.toString();
    }
    protected static boolean isIntervalEmpty(BinaryDataType binaryDataType,int minLength,int maxLength) {
        return minLength>maxLength;
    }
}
