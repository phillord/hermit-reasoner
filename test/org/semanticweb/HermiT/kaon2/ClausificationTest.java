package org.semanticweb.HermiT.kaon2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.kaon2.structural.Clausification;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.kaon2.api.Ontology;

public class ClausificationTest extends AbstractHermiTTest {
    static {
        System.setProperty("entityExpansionLimit",
                String.valueOf(Integer.MAX_VALUE));

    }

    public ClausificationTest(String name) {
        super(name);
    }

    public void testBasicClausification() throws Exception {
        assertClausification("../res/clausification-1-input.xml",
                "../res/clausification-1-control.txt");
    }

    public void testNominalClausification1() throws Exception {
        assertClausification("../res/clausification-2-input.xml",
                "../res/clausification-2-control.txt");
    }

    public void testNominalClausification2() throws Exception {
        assertClausification("../res/clausification-3-input.xml",
                "../res/clausification-3-control.txt");
    }

    public void testNominalClausification3() throws Exception {
        assertClausification("../res/clausification-4-input.xml",
                "../res/clausification-4-control.txt");
    }

    public void testNominalClausification4() throws Exception {
        assertClausification("../res/clausification-5-input.xml",
                "../res/clausification-5-control.txt");
    }

    protected void assertClausification(String ontologyResource,
            String controlResource) throws Exception {
        assertDLClauses(getOntologyFromResource(ontologyResource),
                getControl(controlResource));
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

    protected void assertDLClauses(Ontology ontology, String... control)
            throws Exception {
        Clausification clausification = new Clausification();
        DLOntology dlOntology = clausification.clausify(false, ontology,
                new HashSet<DescriptionGraph>());
        Set<String> actualStrings = new HashSet<String>();
        org.semanticweb.HermiT.Namespaces namespaces = new org.semanticweb.HermiT.Namespaces();
        namespaces.registerPrefix("a", ontology.getOntologyURI() + "#");
        namespaces.registerInternalPrefixes(ontology.getOntologyURI());
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(namespaces));
        assertContainsAll(actualStrings, control);
    }
}
