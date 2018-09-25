package org.semanticweb.HermiT.cli;

import java.io.PrintWriter;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

class SubsAction implements Action {
    final String conceptName;
    final boolean all;

    public SubsAction(String name, boolean getAll) {
        conceptName = name;
        all = getAll;
    }

    @Override
    public void run(Reasoner hermit, StatusOutput status, PrintWriter output, boolean ignoreOntologyPrefixes) {
        status.log(2, "Finding subs of '" + conceptName + "'");
        Prefixes prefixes = hermit.getPrefixes();
        String conceptUri = prefixes.canBeExpanded(conceptName) ? prefixes.expandAbbreviatedIRI(conceptName) : conceptName;
        if (conceptUri.startsWith("<") && conceptUri.endsWith(">"))
            conceptUri = conceptUri.substring(1, conceptUri.length() - 1);
        OWLClass owlClass = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(conceptUri));
        if (!hermit.isDefined(owlClass)) {
            status.log(0, "Warning: class '" + conceptUri + "' was not declared in the ontology.");
        }
        NodeSet<OWLClass> classes;
        if (all) {
            classes = hermit.getSubClasses(owlClass, false);
            output.println("All sub-classes of '" + conceptName + "':");
        } else {
            classes = hermit.getSubClasses(owlClass, true);
            output.println("Direct sub-classes of '" + conceptName + "':");
        }
        for (Node<OWLClass> set : classes)
            for (OWLClass classInSet : set)
                if (ignoreOntologyPrefixes) {
                    String iri = classInSet.getIRI().toString();
                    if (prefixes.canBeExpanded(iri))
                        output.println("\t" + prefixes.expandAbbreviatedIRI(iri));
                    else
                        output.println("\t" + iri);
                } else
                    output.println("\t" + prefixes.abbreviateIRI(classInSet.getIRI().toString()));
        output.flush();
    }
}