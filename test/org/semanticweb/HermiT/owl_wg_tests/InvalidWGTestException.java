package org.semanticweb.HermiT.owl_wg_tests;

@SuppressWarnings("serial")
public class InvalidWGTestException extends Exception {
    
    public InvalidWGTestException(String message) {
        super(message);
    }
    public InvalidWGTestException(String message,Throwable cause) {
        super(message,cause);
    }
}
