package org.semanticweb.HermiT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ProtegeReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
    public OWLReasoner createReasoner(OWLOntologyManager ontologyManager) {
        // ignore the given manager
        return this.createReasoner(OWLManager.createOWLOntologyManager(),new HashSet<OWLOntology>());
    }
    public void initialise() {
    }
    public void dispose() {
    }
    public boolean requiresExplicitClassification() {
        return false;
    }
    @SuppressWarnings("serial")
    public OWLReasoner createReasoner(OWLOntologyManager manager,Set<OWLOntology> ontologies) {
        // ignore the given manager
        Configuration configuration=new Configuration();
        configuration.ignoreUnsupportedDatatypes=true;
        Reasoner hermit=new Reasoner(configuration) {
            protected Set<OWLOntology> m_loadedOntologies;
            
            public void loadOntologies(Set<OWLOntology> ontologies) {
                if (!ontologies.isEmpty()) {
                    super.loadOntologies(ontologies);
                }
                m_loadedOntologies=ontologies;
            }
            public Set<OWLOntology> getLoadedOntologies() {
                return m_loadedOntologies;
            }
            // overwrite so that the methods don't throw errors
            public boolean isSymmetric(OWLObjectProperty property) {
                return false;
            }
            public boolean isTransitive(OWLObjectProperty property) {
                return false;
            }
            public Set<OWLDataRange> getRanges(OWLDataProperty property) {
                return new HashSet<OWLDataRange>();
            }
            public Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
                return new HashMap<OWLObjectProperty,Set<OWLIndividual>>();
            }
            public Map<OWLDataProperty,Set<OWLLiteral>> getDataPropertyRelationships(OWLIndividual individual) {
                return new HashMap<OWLDataProperty,Set<OWLLiteral>>();
            }
            public Set<OWLLiteral> getRelatedValues(OWLIndividual subject,OWLDataPropertyExpression property) {
                return new HashSet<OWLLiteral>();
            }
        };
        if (!ontologies.isEmpty()) hermit.loadOntologies(ontologies);
        return hermit;
    }
}
