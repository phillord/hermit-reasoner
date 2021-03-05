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

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Represents a binary data value.
 */
public class BinaryData {
    protected static final char[] INT_TO_HEX=new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
    protected static final int[] HEX_TO_INT=new int[] {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
            // '0'-'9'
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
            -1, -1, -1, -1, -1, -1, -1,
            // 'A'-'F'
            10, 11, 12, 13, 14, 15, 
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            //'a'-'f'
            10, 11, 12, 13, 14, 15, 
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    
    protected final BinaryDataType m_binaryDataType;
    protected final byte[] m_data;
    protected final int m_hashCode;
    
    /**
     * @param binaryDataType data type
     * @param data data
     */
    public BinaryData(BinaryDataType binaryDataType,byte[] data) {
        m_binaryDataType=binaryDataType;
        m_data=data;
        int hashCode=binaryDataType.hashCode();
        for (int index=0;index<m_data.length;index++)
            hashCode=hashCode*3+m_data[index];
        m_hashCode=hashCode;
    }
    /**
     * @return data type
     */
    public BinaryDataType getBinaryDataType() {
        return m_binaryDataType;
    }
    /**
     * @return number of bytes
     */
    public int getNumberOfBytes() {
        return m_data.length;
    }
    /**
     * @param index index
     * @return value at index
     */
    public byte getByte(int index) {
        return m_data[index];
    }
    @Override
    public boolean equals(Object that) {
        if (this==that)
            return true;
        if (!(that instanceof BinaryData))
            return false;
        BinaryData thatData=(BinaryData)that;
        if (m_hashCode!=thatData.m_hashCode || m_data.length!=thatData.m_data.length || m_binaryDataType!=thatData.m_binaryDataType)
            return false;
        for (int index=m_data.length-1;index>=0;--index)
            if (m_data[index]!=thatData.m_data[index])
                return false;
        return true;
    }
    @Override
    public int hashCode() {
        return m_hashCode;
    }
    @Override
    public String toString() {
        switch (m_binaryDataType) {
        case HEX_BINARY:
            return toHexBinary();
        case BASE_64_BINARY:
            return Base64.getEncoder().encodeToString(m_data);
        default:
            throw new IllegalStateException("Internal error: invalid binary data type.");
        }
    }
    protected String toHexBinary() {
        StringBuilder buffer=new StringBuilder();
        for (int index=0;index<m_data.length;index++) {
            int octet=(m_data[index] & 0xFF);
            int high=octet/16;
            int low=octet % 16;
            buffer.append(INT_TO_HEX[high]);
            buffer.append(INT_TO_HEX[low]);
        }
        return buffer.toString();
    }
    /**
     * @param lexicalForm form to parse
     * @return parsed data
     */
    public static BinaryData parseHexBinary(String lexicalForm) {
        try {
            if ((lexicalForm.length() % 2)!=0)
                return null;
            ByteArrayOutputStream result=new ByteArrayOutputStream();
            for (int index=0;index<lexicalForm.length();) {
                char digit1=lexicalForm.charAt(index++);
                int high=HEX_TO_INT[digit1];
                if (high<0)
                    return null;
                char digit2=lexicalForm.charAt(index++);
                int low=HEX_TO_INT[digit2];
                if (low<0)
                    return null;
                int octet=(high*16+low);
                result.write(octet);
            }
            return new BinaryData(BinaryDataType.HEX_BINARY,result.toByteArray());
        }
        catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
            return null;
        }
    }
    /**
     * @param lexicalForm form to parse
     * @return parsed data
     */
    public static BinaryData parseBase64Binary(String lexicalForm) {
        try {
            byte[] data=Base64.getDecoder().decode(removeWhitespace(lexicalForm));
            return new BinaryData(BinaryDataType.HEX_BINARY,data);
        }
        catch (@SuppressWarnings("unused") IllegalArgumentException|IndexOutOfBoundsException error) {
            return null;
        }
    }
    protected static String removeWhitespace(String lexicalForm) {
        StringBuilder b=new StringBuilder(lexicalForm);
        for(int i=b.length()-1;i>-1;i--) {
            if(Character.isWhitespace(b.charAt(i))) {
                b.deleteCharAt(i);
            }
        }
        return b.toString();
    }
}
