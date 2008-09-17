// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.util.Set;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.DLOntology;

public interface Clausifier {
    @SuppressWarnings("serial")
    public class LoadingException extends Exception {
        
        public LoadingException(String message) {
            super(message);
        }
        
        public LoadingException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public LoadingException(Throwable cause) {
            super(cause);
        }
    }
    
    DLOntology loadFromURI(java.net.URI physicalURI,
                           Set<DescriptionGraph> graphs)
        throws LoadingException;
    DLOntology clausifyNativeOntology(Object ontology,
                                      Set<DescriptionGraph> graphs,
                                      Object... extraCrap)
        throws LoadingException;
}

