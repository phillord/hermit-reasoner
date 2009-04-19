// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.binarydata;

import java.io.ByteArrayOutputStream;

/**
 * Represents a binary data value.
 */
public class BinaryDataValue {
    protected static final char[] HEX_DIGITS=new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
    
    protected final BinaryDataType m_binaryDataType;
    protected final byte[] m_data;
    protected final int m_hashCode;
    
    public BinaryDataValue(BinaryDataType binaryDataType,byte[] data) {
        m_binaryDataType=binaryDataType;
        m_data=data;
        int hashCode=binaryDataType.hashCode();
        for (int index=0;index<m_data.length;index++)
            hashCode=hashCode*3+m_data[index];
        m_hashCode=hashCode;
    }
    public BinaryDataType getBinaryDataType() {
        return m_binaryDataType;
    }
    public int getNumberOfBytes() {
        return m_data.length;
    }
    public byte getByte(int index) {
        return m_data[index];
    }
    public boolean equals(Object that) {
        if (this==that)
            return true;
        if (!(that instanceof BinaryDataValue))
            return false;
        BinaryDataValue thatData=(BinaryDataValue)that;
        if (m_hashCode!=thatData.m_hashCode || m_data.length!=thatData.m_data.length || m_binaryDataType!=thatData.m_binaryDataType)
            return false;
        for (int index=m_data.length-1;index>=0;--index)
            if (m_data[index]!=thatData.m_data[index])
                return false;
        return true;
    }
    public int hashCode() {
        return m_hashCode;
    }
    public String toString() {
        switch (m_binaryDataType) {
        case HEX_BINARY:
            return toHexBinary();
        case BASE_64_BINARY:
            return Base64.encodeBytes(m_data);
        default:
            throw new IllegalStateException("Internal error: invalid binary data type.");
        }
    }
    protected String toHexBinary() {
        StringBuffer buffer=new StringBuffer();
        for (int index=0;index<m_data.length;index++) {
            int octet=(m_data[index] & 0xFF);
            int high=octet/16;
            int low=octet % 16;
            buffer.append(HEX_DIGITS[high]);
            buffer.append(HEX_DIGITS[low]);
        }
        return buffer.toString();
    }
    public static BinaryDataValue parseHexBinary(String lexicalForm) {
        if ((lexicalForm.length() % 2)!=0)
            return null;
        ByteArrayOutputStream result=new ByteArrayOutputStream();
        for (int index=0;index<lexicalForm.length();) {
            int high;
            char digit1=Character.toLowerCase(lexicalForm.charAt(index++));
            if ('0'<=digit1 && digit1<='9')
                high=digit1-'0';
            else if ('a'<=digit1 && digit1<='f')
                high=digit1-'a'+10;
            else
                return null;
            int low;
            char digit2=Character.toLowerCase(lexicalForm.charAt(index++));
            if ('0'<=digit2 && digit2<='9')
                low=digit2-'0';
            else if ('a'<=digit2 && digit2<='f')
                low=digit2-'a'+10;
            else
                return null;
            int octet=(high*16+low);
            result.write(octet);
        }
        return new BinaryDataValue(BinaryDataType.HEX_BINARY,result.toByteArray());
    }
    public static BinaryDataValue parseBase64Binary(String lexicalForm) {
        try {
            byte[] data=Base64.decode(lexicalForm);
            return new BinaryDataValue(BinaryDataType.HEX_BINARY,data);
        }
        catch (Base64.DecodeException error) {
            return null;
        }
    }
}
