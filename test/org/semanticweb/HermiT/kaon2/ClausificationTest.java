package org.semanticweb.HermiT.kaon2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.InternalNames;
import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.deprecated.AbstractHermiTTest;
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
        // we really want the following clauses in the control set, but stupid 
        // KAON2 cannot parse this properly, so till KAON2 is fixed, we have to 
        // live with the artificial genid2 role that KAON2 introduces:
        // r(Y,X) v r(Y1,X) :- c(X), nom:i2(Y), nom:i1(Y1)
        // e(X) v (atMost 2 r d)(X) :- d(X), r(X,Y), d(Y)
        // Y1 == Y2 v Y1 == Y3 v Y2 == Y3 :- (atMost 2 r d)(X), r(X,Y1), d(Y1), r(X,Y2), d(Y2), r(X,Y3), d(Y3), Y1 < Y2, Y2 < Y3
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
        Namespaces namespaces = InternalNames.withInternalNamespaces
            (new Namespaces(ontology.getOntologyURI() + "#",
                                Namespaces.semanticWebNamespaces));
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(namespaces));
        assertContainsAll(this.getName(), actualStrings, control);
    }
}
