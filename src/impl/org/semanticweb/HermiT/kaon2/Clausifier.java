// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.kaon2;

import java.util.Set;
import java.util.HashMap;
import java.util.Collections;

import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.DLOntology;

import org.semanticweb.HermiT.kaon2.structural.Clausification;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;

public class Clausifier implements org.semanticweb.HermiT.Clausifier {
    
    public DLOntology loadFromURI(java.net.URI physicalURI,
                           Set<DescriptionGraph> graphs)
        throws LoadingException {
        try {
            DefaultOntologyResolver resolver =
                new DefaultOntologyResolver();
            String ontologyURI =
                resolver.registerOntology(physicalURI.toString());
            OntologyManager ontologyManager =
                KAON2Manager.newOntologyManager();
            ontologyManager.setOntologyResolver(resolver);
            Ontology ontology =
                ontologyManager.openOntology
                    (ontologyURI, new HashMap<String,Object>());
            return loadKAON2Ontology(ontology, graphs);
        } catch (KAON2Exception e) {
            throw new LoadingException(e);
        } catch (InterruptedException e) {
            throw new LoadingException(e);
        }
    }

    public DLOntology clausifyNativeOntology(Object ontology,
                                      Set<DescriptionGraph> graphs,
                                      Object... extraCrap)
        throws LoadingException {
        return loadKAON2Ontology((Ontology) ontology, graphs);
    }
    
    protected DLOntology loadKAON2Ontology(Ontology ontology,
                                           Set<DescriptionGraph> graphs)
        throws LoadingException {
        try {
            if (graphs == null) {
                graphs = Collections.emptySet();
            }
            Clausification clausification=new Clausification();
            return clausification.clausify(true, ontology, graphs);
        } catch (KAON2Exception e) {
            throw new LoadingException(e);
        }
    }

}