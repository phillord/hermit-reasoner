package org.semanticweb.HermiT.owlapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.HermiT;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ClausificationTest extends AbstractOWLOntologyTest {
    static {
        System.setProperty("entityExpansionLimit",
                String.valueOf(Integer.MAX_VALUE));
    }

    public ClausificationTest(String name) {
        super(name);
    }

    public void testBasicClausification() throws Exception {
        assertClausification("../res/clausification-1-input.xml",
                "../res/clausification-1-OWL-control.txt");
    }

    public void testNominalClausification1() throws Exception {
        assertClausification("../res/clausification-2-input.xml",
                "../res/clausification-2-OWL-control.txt");
    }

    public void testNominalClausification2() throws Exception {
        assertClausification("../res/clausification-3-input.xml",
                "../res/clausification-3-OWL-control.txt");
    }

    public void testNominalClausification3() throws Exception {
        assertClausification("../res/clausification-4-input.xml",
                "../res/clausification-4-OWL-control.txt");
    }

    public void testNominalClausification4() throws Exception {
        assertClausification("../res/clausification-5-input.xml",
                "../res/clausification-5-OWL-control.txt");
    }

    public void testAsymmetryClausification() throws Exception {
        assertClausification("../res/asymmetric-1-input.xml",
                "../res/asymmetric-1-OWL-control.txt");
    }

    public void testExistsSelfClausification() throws Exception {
        assertClausification("../res/self-1-input.owl",
                "../res/self-1-OWL-control.txt");
    }

    public void testExistsSelf2Clausification() throws Exception {
        assertClausification("../res/self-2-input.owl",
                "../res/self-2-OWL-control.txt");
    }

    protected String[] getControl(String resource) throws Exception {
        List<String> control = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(resource)));
        try {
            String line = reader.readLine();
            while (line != null) {
                control.add(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        String[] controlArray = new String[control.size()];
        control.toArray(controlArray);
        return controlArray;
    }

    protected void assertClausification(String ontologyResource,
            String controlResource) throws Exception {
        assertDLClauses(getOWLOntologyFromResource(ontologyResource),
                getControl(controlResource));
    }

    protected void assertDLClauses(OWLOntology ontology, String... control)
            throws Exception {
        OwlClausification clausifier = new OwlClausification();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        DLOntology dlOntology
            = clausifier.clausify(new HermiT.Configuration(),
                ontology, factory, noDescriptionGraphs);
        Set<String> actualStrings = new HashSet<String>();
        org.semanticweb.HermiT.Namespaces namespaces = new org.semanticweb.HermiT.Namespaces();
        namespaces.registerPrefix("a", ontology.getURI() + "#");
        namespaces.registerInternalPrefixes(ontology.getURI().toString());
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(namespaces));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(namespaces));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not " + atom.toString(namespaces));
        assertContainsAll(actualStrings, control);
    }
}
