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
package org.semanticweb.HermiT.datatypes;

/**
 * Malformed literal exception.
 */
@SuppressWarnings("serial")
public class MalformedLiteralException extends RuntimeException {

    /**
     * @param lexicalForm lexicalForm
     * @param datatypeURI datatypeURI
     */
    public MalformedLiteralException(String lexicalForm,String datatypeURI) {
        this(lexicalForm,datatypeURI,null);
    }
    /**
     * @param lexicalForm lexicalForm
     * @param datatypeURI datatypeURI
     * @param cause cause
     */
    public MalformedLiteralException(String lexicalForm,String datatypeURI,Throwable cause) {
        super("Literal \""+lexicalForm+"\"^^<"+datatypeURI+"> is malformed",cause);
    }
}
