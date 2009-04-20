// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.binarydata;

import java.io.ByteArrayOutputStream;

/**
 * Represents a binary data value.
 */
public class BinaryData {
    protected static final char[] INT_TO_HEX=new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
    protected static final int[] HEX_TO_INT=new int[127];
    static {
        for (int i=0;i<HEX_TO_INT.length;i++)
            HEX_TO_INT[i]=-1;
        for (int i='0';i<='9';i++)
            HEX_TO_INT[i]=i-'0';
        for (int i='A';i<='F';i++)
            HEX_TO_INT[i]=i-'A'+10;
        for (int i='a';i<='f';i++)
            HEX_TO_INT[i]=i-'a'+10;
    }
    
    protected final BinaryDataType m_binaryDataType;
    protected final byte[] m_data;
    protected final int m_hashCode;
    
    public BinaryData(BinaryDataType binaryDataType,byte[] data) {
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
    public int hashCode() {
        return m_hashCode;
    }
    public String toString() {
        switch (m_binaryDataType) {
        case HEX_BINARY:
            return toHexBinary();
        case BASE_64_BINARY:
            return Base64.base64Encode(m_data);
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
            buffer.append(INT_TO_HEX[high]);
            buffer.append(INT_TO_HEX[low]);
        }
        return buffer.toString();
    }
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
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    public static BinaryData parseBase64Binary(String lexicalForm) {
        lexicalForm=removeWhitespace(lexicalForm);
        try {
            byte[] data=Base64.decodeBase64(lexicalForm);
            return new BinaryData(BinaryDataType.HEX_BINARY,data);
        }
        catch (IllegalArgumentException error) {
            return null;
        }
        catch (IndexOutOfBoundsException error) {
            return null;
        }
    }
    protected static String removeWhitespace(String lexicalForm) {
        lexicalForm=lexicalForm.trim();
        for (int index=lexicalForm.length()-1;index>=0;index--) {
            if (Character.isWhitespace(lexicalForm.charAt(index))) {
                int upperSpaceIndex=index;
                while (Character.isWhitespace(lexicalForm.charAt(index)))
                    index--;
                lexicalForm=lexicalForm.substring(0,index+1)+lexicalForm.substring(upperSpaceIndex+1);
            }
        }
        return lexicalForm;
    }
}
