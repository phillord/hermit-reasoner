// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes;

@SuppressWarnings("serial")
public class MalformedLiteralException extends RuntimeException {

    public MalformedLiteralException(String lexicalForm,String datatypeURI) {
        super("Literal \""+lexicalForm+"\"^^<"+datatypeURI+"> is malformed");
    }
}
