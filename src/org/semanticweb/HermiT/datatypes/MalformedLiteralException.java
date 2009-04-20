// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes;

@SuppressWarnings("serial")
public class MalformedLiteralException extends RuntimeException {

    public MalformedLiteralException(String lexicalForm,String datatypeURI) {
        this(lexicalForm,datatypeURI,null);
    }
    public MalformedLiteralException(String lexicalForm,String datatypeURI,Throwable cause) {
        super("Literal \""+lexicalForm+"\"^^<"+datatypeURI+"> is malformed",cause);
    }
}
