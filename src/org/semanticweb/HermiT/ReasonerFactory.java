package org.semanticweb.HermiT;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
/**Reasoner factory.*/
public class ReasonerFactory implements OWLReasonerFactory {
    @Override
    public String getReasonerName() {
        return getClass().getPackage().getImplementationTitle();
    }
    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        return createReasoner(ontology,getProtegeConfiguration(null));
    }
    @Override
    public OWLReasoner createReasoner(OWLOntology ontology,OWLReasonerConfiguration config) {
        return createHermiTOWLReasoner(getProtegeConfiguration(config),ontology);
    }
    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        return createNonBufferingReasoner(ontology,getProtegeConfiguration(null));
    }
    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,OWLReasonerConfiguration owlAPIConfiguration) {
        Configuration configuration=getProtegeConfiguration(owlAPIConfiguration);
        configuration.bufferChanges=false;
        return createHermiTOWLReasoner(configuration,ontology);
    }
    protected Configuration getProtegeConfiguration(OWLReasonerConfiguration owlAPIConfiguration) {
        Configuration configuration;
        if (owlAPIConfiguration!=null) {
            if (owlAPIConfiguration instanceof Configuration)
                configuration=(Configuration)owlAPIConfiguration;
            else {
                configuration=new Configuration();
                configuration.freshEntityPolicy=owlAPIConfiguration.getFreshEntityPolicy();
                configuration.individualNodeSetPolicy=owlAPIConfiguration.getIndividualNodeSetPolicy();
                configuration.reasonerProgressMonitor=owlAPIConfiguration.getProgressMonitor();
                configuration.individualTaskTimeout=owlAPIConfiguration.getTimeOut();
            }
        }
        else {
            configuration=new Configuration();
            configuration.ignoreUnsupportedDatatypes=true;
        }
        return configuration;
    }
    protected OWLReasoner createHermiTOWLReasoner(Configuration configuration,OWLOntology ontology) {
        return new Reasoner(configuration,ontology);
    }
}
