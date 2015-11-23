package org.semanticweb.HermiT;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

/**
 * Protege reasoner factory.
 */
public class ProtegeReasonerFactory extends AbstractProtegeOWLReasonerInfo {
    protected final ReasonerFactory factory=new ReasonerFactory();

    @Override
    public BufferingMode getRecommendedBuffering() {
        return BufferingMode.BUFFERING;
    }
    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return factory;
    }

    @Override
    public OWLReasonerConfiguration getConfiguration(ReasonerProgressMonitor monitor) {
        Configuration configuration = new Configuration();
        configuration.ignoreUnsupportedDatatypes = true;
        configuration.reasonerProgressMonitor = monitor;
        return configuration;
    }
}