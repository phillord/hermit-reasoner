/*______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on 25 mars 2005
 *
 */
package rationals.converters.analyzers;

import rationals.converters.ConverterException;

/**
 * Interface lifting lexical analysis.
 * This interface allows customization of parsing of RE, in particular to
 * override the definition of what is a labeL. 
 * Instances of Lexer are used by instances of Parser to retrieve tokens.
 * @author nono
 * @version $Id: Lexer.java 2 2006-08-24 14:41:48Z oqube $
 * @see DefaultLexer
 * @see Parser
 */
public interface Lexer {
    public static final int LABEL = 0;

    public static final int INT = 1;

    public static final int EPSILON = 2;

    public static final int EMPTY = 3;

    public static final int ITERATION = 4;

    public static final int UNION = 5;

    public static final int STAR = 6;

    public static final int OPEN = 7;

    public static final int CLOSE = 8;

    public static final int END = 9;

    public static final int UNKNOWN = 10;

    // AB
    public static final int SHUFFLE = 11;

    public static final int MIX = 12;

    public static final int OBRACE = 13;

    public static final int CBRACE = 14;

    /**
     * Read more data from the underying input.
     * 
     * @throws ConverterException if some characters cannot be converted
     */
    public abstract void read() throws ConverterException;

    /**
     * Return the current line number in the underlying character
     * stream.
     * Line separation is platform dependent.
     * 
     * @return number of current line, starting from 1
     */
    public abstract int lineNumber();

    /**
     * Return the image of current token.
     * This method is used by Parser to create atomic Automaton objects
     * so any Object can be used.
     * 
     * @return an Object which is a label for a transition.
     */
    public abstract Object label();

    /**
     * Return the value of a number.
     * 
     * @return value of a number.
     */
    public abstract int value();

    /**
     * Returns the current token value.
     * This value must be one of the constants defined in interface Lexer.
     * 
     * @return a constant denoting the kind of token.
     */
    public abstract int current();
}