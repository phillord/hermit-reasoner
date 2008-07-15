// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactoryAdapter;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ReasonerFactory extends ProtegeOWLReasonerFactoryAdapter {
    public OWLReasoner createReasoner(OWLOntologyManager m) {
        return new HermitReasoner(m);
    }
    public void initialise() throws Exception {}
    public void dispose() throws Exception {}
    public boolean requiresExplicitClassification() { return false; }
}
