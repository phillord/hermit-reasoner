// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
    public OWLReasoner createReasoner(OWLOntologyManager m) {
        // When created by Protege, tolerate datatypes by default, since
        // there is no (easy) way to pass configuration settings:
        return new HermitReasoner(m, true);
    }
    public void initialise() throws Exception {}
    public void dispose() throws Exception {}
    public boolean requiresExplicitClassification() { return false; }
}
