package org.semanticweb.HermiT.cli;
import java.io.PrintWriter;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
class SatisfiabilityAction implements Action {
    final String conceptName;
    public SatisfiabilityAction(String c) {
        conceptName=c;
    }
    @Override
    public void run(Reasoner hermit,StatusOutput status,PrintWriter output,boolean ignoreOntologyPrefixes) {
        status.log(2,"Checking satisfiability of '"+conceptName+"'");
        Prefixes prefixes=hermit.getPrefixes();
        String conceptUri=prefixes.canBeExpanded(conceptName) ? prefixes.expandAbbreviatedIRI(conceptName) : conceptName;
        if (conceptUri.startsWith("<") && conceptUri.endsWith(">"))
            conceptUri=conceptUri.substring(1,conceptUri.length()-1);
        OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
        if (!hermit.isDefined(owlClass)) {
            status.log(0,"Warning: class '"+conceptUri+"' was not declared in the ontology.");
        }
        boolean result=hermit.isSatisfiable(owlClass);
        output.println(conceptName+(result ? " is satisfiable." : " is not satisfiable."));
        output.flush();
    }
}