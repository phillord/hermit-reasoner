package org.semanticweb.HermiT.cli;
import java.io.PrintWriter;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;
class EquivalentsAction implements Action {
    final String conceptName;

    public EquivalentsAction(String name) {
        conceptName=name;
    }
    @Override
    public void run(Reasoner hermit,StatusOutput status,PrintWriter output,boolean ignoreOntologyPrefixes) {
        status.log(2,"Finding equivalents of '"+conceptName+"'");
        Prefixes prefixes=hermit.getPrefixes();
        String conceptUri=prefixes.canBeExpanded(conceptName) ? prefixes.expandAbbreviatedIRI(conceptName) : conceptName;
        if (conceptUri.startsWith("<") && conceptUri.endsWith(">"))
            conceptUri=conceptUri.substring(1,conceptUri.length()-1);
        OWLClass owlClass=OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
        if (!hermit.isDefined(owlClass)) {
            status.log(0,"Warning: class '"+conceptName+"' was not declared in the ontology.");
        }
        Node<OWLClass> classes=hermit.getEquivalentClasses(owlClass);
        if (ignoreOntologyPrefixes)
            output.println("Classes equivalent to '"+conceptName+"':");
        else
            output.println("Classes equivalent to '"+prefixes.abbreviateIRI(conceptName)+"':");
        for (OWLClass classInSet : classes)
            if (ignoreOntologyPrefixes) {
                String iri=classInSet.getIRI().toString();
                if (prefixes.canBeExpanded(iri))
                    output.println("\t"+prefixes.expandAbbreviatedIRI(iri));
                else
                    output.println("\t"+iri);
            }
            else
                output.println("\t"+prefixes.abbreviateIRI(classInSet.getIRI().toString()));
        output.flush();
    }
}