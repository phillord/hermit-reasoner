// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.binarydata;

/**
 * This class is adapted from the the Base64 encoder/decoder by Robert Harder, version 2.3.1.
 */
public class Base64 {
    /** No options specified. Value is zero. */
    public final static int NO_OPTIONS=0;

    /** Do break lines when encoding. Value is 8. */
    public final static int DO_BREAK_LINES=8;


    /** Maximum line length (76) of Base64 output. */
    private final static int MAX_LINE_LENGTH=76;

    /** The equals sign (=) as a byte. */
    private final static byte EQUALS_SIGN=(byte)'=';

    /** The new line character (\n) as a byte. */
    private final static byte NEW_LINE=(byte)'\n';

    private final static byte WHITE_SPACE_ENC=-5; // Indicates white space in encoding
    private final static byte EQUALS_SIGN_ENC=-1; // Indicates equals sign in encoding

    /** The 64 valid Base64 values. */
    private final static byte[] STANDARD_ALPHABET={ (byte)'A',(byte)'B',(byte)'C',(byte)'D',(byte)'E',(byte)'F',(byte)'G',(byte)'H',(byte)'I',(byte)'J',(byte)'K',(byte)'L',(byte)'M',(byte)'N',(byte)'O',(byte)'P',(byte)'Q',(byte)'R',(byte)'S',(byte)'T',(byte)'U',(byte)'V',(byte)'W',(byte)'X',(byte)'Y',(byte)'Z',(byte)'a',(byte)'b',(byte)'c',(byte)'d',(byte)'e',(byte)'f',(byte)'g',(byte)'h',(byte)'i',(byte)'j',(byte)'k',(byte)'l',(byte)'m',(byte)'n',(byte)'o',(byte)'p',(byte)'q',(byte)'r',(byte)'s',(byte)'t',(byte)'u',(byte)'v',(byte)'w',(byte)'x',(byte)'y',(byte)'z',(byte)'0',(byte)'1',(byte)'2',(byte)'3',(byte)'4',(byte)'5',(byte)'6',(byte)'7',(byte)'8',(byte)'9',(byte)'+',(byte)'/' };

    /**
     * Translates a Base64 value to either its 6-bit reconstruction value or a negative number indicating some other meaning.
     */
    private final static byte[] STANDARD_DECODABET={
        -9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 0 - 8
        -5,-5, // Whitespace: Tab and Linefeed
        -9,-9, // Decimal 11 - 12
        -5, // Whitespace: Carriage Return
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 14 - 26
        -9,-9,-9,-9,-9, // Decimal 27 - 31
        -5, // Whitespace: Space
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 33 - 42
        62, // Plus sign at decimal 43
        -9,-9,-9, // Decimal 44 - 46
        63, // Slash at decimal 47
        52,53,54,55,56,57,58,59,60,61, // Numbers zero through nine
        -9,-9,-9, // Decimal 58 - 60
        -1, // Equals sign at decimal 61
        -9,-9,-9, // Decimal 62 - 64
        0,1,2,3,4,5,6,7,8,9,10,11,12,13, // Letters 'A' through 'N'
        14,15,16,17,18,19,20,21,22,23,24,25, // Letters 'O' through 'Z'
        -9,-9,-9,-9,-9,-9, // Decimal 91 - 96
        26,27,28,29,30,31,32,33,34,35,36,37,38, // Letters 'a' through 'm'
        39,40,41,42,43,44,45,46,47,48,49,50,51, // Letters 'n' through 'z'
        -9,-9,-9,-9 // Decimal 123 - 126
    };


    // Encoding
    
    public static String encodeBytes(byte[] source) {
        return encodeBytes(source,0,source.length,NO_OPTIONS);
    }

    public static String encodeBytes(byte[] source,int options) {
        return encodeBytes(source,0,source.length,options);
    }

    public static String encodeBytes(byte[] source,int off,int len) {
        return encodeBytes(source,off,len,NO_OPTIONS);
    }

    public static String encodeBytes(byte[] source,int off,int len,int options) {
        char[] encoded=encodeBytesToChars(source,off,len,options);
        return new String(encoded);
    }

    public static char[] encodeBytesToChars(byte[] source,int off,int len,int options) {
        if (off<0)
            throw new IllegalArgumentException("Cannot have negative offset: "+off);
        if (len<0)
            throw new IllegalArgumentException("Cannot have length offset: "+len);
        if (off+len>source.length)
            throw new IllegalArgumentException(String.format("Cannot have offset of %d and length of %d with array of length %d",off,len,source.length));

        boolean breakLines=(options & DO_BREAK_LINES)>0;

        int len43=len*4/3;
        char[] outBuff=new char[(len43) // Main 4:3
                +((len%3)>0 ? 4 : 0) // Account for padding
                +(breakLines ? (len43/MAX_LINE_LENGTH) : 0)]; // New lines
        int d=0;
        int e=0;
        int len2=len-2;
        int lineLength=0;
        for (;d<len2;d+=3,e+=4) {
            encode3to4(source,d+off,3,outBuff,e);

            lineLength+=4;
            if (breakLines && lineLength==MAX_LINE_LENGTH) {
                outBuff[e+4]=NEW_LINE;
                e++;
                lineLength=0;
            }
        }

        if (d<len) {
            encode3to4(source,d+off,len-d,outBuff,e);
            e+=4;
        }

        char[] finalOut=new char[e];
        System.arraycopy(outBuff,0,finalOut,0,e);
        return finalOut;

    }

    private static void encode3to4(byte[] source,int srcOffset,int numSigBytes,char[] destination,int destOffset) {

        // 1 2 3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------| || || || | Six bit groups to index ALPHABET
        // >>18 >>12 >> 6 >> 0 Right shift necessary
        // 0x3f 0x3f 0x3f Additional AND

        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff=(numSigBytes>0 ? ((source[srcOffset]<<24)>>>8) : 0)|(numSigBytes>1 ? ((source[srcOffset+1]<<24)>>>16) : 0)|(numSigBytes>2 ? ((source[srcOffset+2]<<24)>>>24) : 0);

        switch (numSigBytes) {
        case 3:
            destination[destOffset]=(char)STANDARD_ALPHABET[(inBuff >>> 18)];
            destination[destOffset+1]=(char)STANDARD_ALPHABET[(inBuff >>> 12) & 0x3f];
            destination[destOffset+2]=(char)STANDARD_ALPHABET[(inBuff >>> 6) & 0x3f];
            destination[destOffset+3]=(char)STANDARD_ALPHABET[(inBuff) & 0x3f];
            break;
        case 2:
            destination[destOffset]=(char)STANDARD_ALPHABET[(inBuff >>> 18)];
            destination[destOffset+1]=(char)STANDARD_ALPHABET[(inBuff >>> 12) & 0x3f];
            destination[destOffset+2]=(char)STANDARD_ALPHABET[(inBuff >>> 6) & 0x3f];
            destination[destOffset+3]=(char)EQUALS_SIGN;
            break;
        case 1:
            destination[destOffset]=(char)STANDARD_ALPHABET[(inBuff >>> 18)];
            destination[destOffset+1]=(char)STANDARD_ALPHABET[(inBuff >>> 12) & 0x3f];
            destination[destOffset+2]=(char)EQUALS_SIGN;
            destination[destOffset+3]=(char)EQUALS_SIGN;
            break;
        }
    }

    // Decoding

    public static byte[] decode(char[] source) throws DecodeException {
        return decode(source,0,source.length,Base64.NO_OPTIONS);
    }

    public static byte[] decode(char[] source,int off,int len,int options) throws DecodeException {
        if (off<0 || off+len>source.length)
            throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and process %d bytes.",source.length,off,len));

        if (len==0)
            return new byte[0];
        else if (len<4)
            throw new IllegalArgumentException("Base64-encoded string must have at least four characters, but length specified was "+len);

        int len34=len*3/4; // Estimate on array size
        byte[] outBuff=new byte[len34]; // Upper limit on size of output
        int outBuffPosn=0; // Keep track of where we're writing

        byte[] b4=new byte[4]; // Four byte buffer from source, eliminating white space
        int b4Posn=0; // Keep track of four byte input buffer
        int i=0; // Source array counter
        byte sbiCrop=0; // Low seven bits (ASCII) of input
        byte sbiDecode=0; // Special value from DECODABET

        for (i=off;i<off+len;i++) {
            
            sbiCrop=(byte)(source[i] & 0x7f); // Only the low seven bits
            sbiDecode=STANDARD_DECODABET[sbiCrop]; // Special value

            // White space, Equals sign, or legit Base64 character
            // Note the values such as -5 and -9 in the
            // DECODABETs at the top of the file.
            if (sbiDecode>=WHITE_SPACE_ENC) {
                if (sbiDecode>=EQUALS_SIGN_ENC) {
                    b4[b4Posn++]=sbiCrop; // Save non-whitespace
                    if (b4Posn>3) { // Time to decode?
                        outBuffPosn+=decode4to3(b4,0,outBuff,outBuffPosn,options);
                        b4Posn=0;
                        if (sbiCrop==EQUALS_SIGN)
                            break;
                    }
                }
            }
            else
                throw new DecodeException(String.format("Bad Base64 input character '%c' in array position %d",source[i],i));
        }

        byte[] out=new byte[outBuffPosn];
        System.arraycopy(outBuff,0,out,0,outBuffPosn);
        return out;
    }

    private static int decode4to3(byte[] source,int srcOffset,byte[] destination,int destOffset,int options) {

        if (srcOffset<0 || srcOffset+3>=source.length)
            throw new IllegalArgumentException(String.format("Source array with length %d cannot have offset of %d and still process four bytes.",source.length,srcOffset));
        if (destOffset<0 || destOffset+2>=destination.length)
            throw new IllegalArgumentException(String.format("Destination array with length %d cannot have offset of %d and still store three bytes.",destination.length,destOffset));

        if (source[srcOffset+2]==EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
            // | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            int outBuff=((STANDARD_DECODABET[source[srcOffset]] & 0xFF) << 18) | ((STANDARD_DECODABET[source[srcOffset+1]] & 0xFF) << 12);

            destination[destOffset]=(byte)(outBuff >>> 16);
            return 1;
        }
        else if (source[srcOffset+3]==EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            int outBuff=((STANDARD_DECODABET[source[srcOffset]] & 0xFF) << 18) | ((STANDARD_DECODABET[source[srcOffset+1]] & 0xFF) << 12) | ((STANDARD_DECODABET[source[srcOffset+2]] & 0xFF) << 6);

            destination[destOffset]=(byte)(outBuff>>>16);
            destination[destOffset+1]=(byte)(outBuff>>>8);
            return 2;
        }
        else {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6 )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
            // | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
            int outBuff=((STANDARD_DECODABET[source[srcOffset]] & 0xFF) << 18) | ((STANDARD_DECODABET[source[srcOffset+1]] & 0xFF) << 12) | ((STANDARD_DECODABET[source[srcOffset+2]] & 0xFF) << 6) | ((STANDARD_DECODABET[source[srcOffset+3]] & 0xFF));

            destination[destOffset]=(byte)(outBuff >> 16);
            destination[destOffset+1]=(byte)(outBuff >> 8);
            destination[destOffset+2]=(byte)(outBuff);

            return 3;
        }
    }

    public static byte[] decode(String s) throws DecodeException {
        return decode(s,NO_OPTIONS);
    }

    public static byte[] decode(String s,int options) throws DecodeException {
        char[] chars=s.toCharArray();
        return decode(chars,0,chars.length,options);
    }
    
    @SuppressWarnings("serial")
    public static class DecodeException extends Exception {
        public DecodeException(String message) {
            super(message);
        }
    }
} 
