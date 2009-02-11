package org.semanticweb.HermiT.owlapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.InternalNames;
import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.owlapi.structural.OWLHasKeyDummy;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.owl.model.OWLOntology;

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
                "../res/clausification-1-OWL-control.txt", null);
    }

    public void testNominalClausification1() throws Exception {
        assertClausification("../res/clausification-2-input.xml",
               "../res/clausification-2-OWL-control.txt", 
                "../res/clausification-2-OWL-control-variant.txt");
    }

    public void testNominalClausification2() throws Exception {
        assertClausification("../res/clausification-3-input.xml",
                "../res/clausification-3-OWL-control.txt", 
                "../res/clausification-3-OWL-control-variant.txt");
    }

    public void testNominalClausification3() throws Exception {
        assertClausification("../res/clausification-4-input.xml",
                "../res/clausification-4-OWL-control.txt", null);
    }

    public void testNominalClausification4() throws Exception {
        assertClausification("../res/clausification-5-input.xml",
                "../res/clausification-5-OWL-control.txt", null);
    }

    public void testAsymmetryClausification() throws Exception {
        assertClausification("../res/asymmetric-1-input.xml",
                "../res/asymmetric-1-OWL-control.txt", null);
    }

    public void testExistsSelfClausification() throws Exception {
        assertClausification("../res/self-1-input.owl",
                "../res/self-1-OWL-control.txt", "../res/self-1-OWL-control-variant.txt");
    }

    public void testExistsSelf2Clausification() throws Exception {
        assertClausification("../res/self-2-input.owl",
                "../res/self-2-OWL-control.txt", null);
    }
    
    public void testHasKeys() throws Exception {
        OwlClausification clausifier = new OwlClausification();
        DLClause clause = clausifier.clausifyKey(OWLHasKeyDummy.getDemoKey());
        Set<String> bAtoms = new HashSet<String>();
        bAtoms.add("<internal:Named>(X)");
        bAtoms.add("<internal:Named>(X2)");
        bAtoms.add("<int:C_test>(X)");
        bAtoms.add("<int:C_test>(X2)");
        bAtoms.add("<int:r_test>(X,Y0)");
        bAtoms.add("<int:r_test>(X2,Y0)");
        bAtoms.add("<internal:Named>(Y0)");
        bAtoms.add("<int:dp_test>*(X,Y1)");
        bAtoms.add("<int:dp_test>*(X2,Y2)");
        assertTrue(bAtoms.size() == clause.getBodyLength());
        for (int i = 0; i < clause.getBodyLength(); i++) {
           assertTrue(bAtoms.contains(clause.getBodyAtom(i).toString()));    
        }
        Set<String> hAtoms = new HashSet<String>();
        hAtoms.add("X == X2");
        hAtoms.add("Y1 != Y2");
        assertTrue(hAtoms.size() == clause.getHeadLength());
        for (int i = 0; i < clause.getHeadLength(); i++) {
            assertTrue(hAtoms.contains(clause.getHeadAtom(i).toString()));    
         }
    }
    
    protected String[] getControl(String resource) throws Exception {
        if (resource == null) return null;
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
            String controlResource, 
            String controlResourceVariant) throws Exception {
        assertDLClauses(getOWLOntologyFromResource(ontologyResource),
                getControl(controlResource), 
                getControl(controlResourceVariant));
    }

    protected void assertDLClauses(OWLOntology ontology, 
            String[] control, 
            String[] controlVariant) throws Exception {
        OwlClausification clausifier = new OwlClausification();
        Set<DescriptionGraph> noDescriptionGraphs = Collections.emptySet();
        DLOntology dlOntology
            = clausifier.clausify(new Reasoner.Configuration(),
                ontology, noDescriptionGraphs);
        Set<String> actualStrings = new HashSet<String>();
        Namespaces namespaces = InternalNames.withInternalNamespaces
            (new Namespaces(ontology.getURI() + "#",
                                Namespaces.semanticWebNamespaces));
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(namespaces));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(namespaces));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not " + atom.toString(namespaces));
        assertContainsAll(this.getName(), actualStrings, control, controlVariant);
    }
}
