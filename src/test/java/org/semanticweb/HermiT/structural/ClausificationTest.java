package org.semanticweb.HermiT.structural;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;

public class ClausificationTest extends AbstractStructuralTest {
    static {
        System.setProperty("entityExpansionLimit",String.valueOf(Integer.MAX_VALUE));
    }

    public ClausificationTest(String name) {
        super(name);
    }
    
    public void testBasic() throws Exception {
        assertClausification("res/basic-input.xml","res/basic-control.txt");
    }

    public void testNominals1() throws Exception {
        assertClausification("res/nominals-1-input.xml","res/nominals-1-control.txt");
    }

   public void testNominals2() throws Exception {
        assertClausification("res/nominals-2-input.xml","res/nominals-2-control.txt");
    }

    public void testNominals3() throws Exception {
        assertClausification("res/nominals-3-input.xml","res/nominals-3-control.txt");
    }

    public void testNominals4() throws Exception {
        assertClausification("res/nominals-4-input.xml","res/nominals-4-control.txt");
    }

    public void testAsymmetry() throws Exception {
        String axioms = "Declaration(ObjectProperty(:as))";
        axioms += "Declaration(ObjectProperty(:as))";
        axioms += "Declaration(NamedIndividual(:a))";
        axioms += "Declaration(NamedIndividual(:b))";
        axioms += "AsymmetricObjectProperty(:as)";
        axioms += "SubObjectPropertyOf(:r :as)";
        axioms += "ObjectPropertyAssertion(:r :a :b)";
        axioms += "ObjectPropertyAssertion(:as :b :a)";
        loadOntologyWithAxioms(axioms);
        assertDLClauses(getControl("res/asymmetry-control.txt"));
    }

    public void testExistsSelf1() throws Exception {
        assertClausification("res/has-self-1-input.owl","res/has-self-1-control.txt");
    }

    public void testExistsSelf2() throws Exception {
        assertClausification("res/has-self-2-input.owl","res/has-self-2-control.txt");
    }
    
    public void testHasKeys() throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        OWLHasKeyAxiom key = m_dataFactory.getOWLHasKeyAxiom(m_dataFactory.getOWLClass(IRI.create("int:C_test")), m_dataFactory.getOWLObjectProperty(IRI.create("int:r_test")), m_dataFactory.getOWLDataProperty(IRI.create("int:dp_test")));
        DLClause clause=clausifier.clausifyKey(key);
        Set<String> bAtoms=new HashSet<String>();
        bAtoms.add("<internal:nam#Named>(X)");
        bAtoms.add("<internal:nam#Named>(X2)");
        bAtoms.add("<int:C_test>(X)");
        bAtoms.add("<int:C_test>(X2)");
        bAtoms.add("<int:r_test>(X,Y0)");
        bAtoms.add("<int:r_test>(X2,Y0)");
        bAtoms.add("<internal:nam#Named>(Y0)");
        bAtoms.add("<int:dp_test>(X,Y1)");
        bAtoms.add("<int:dp_test>(X2,Y2)");
        assertTrue(bAtoms.size()==clause.getBodyLength());
        for (int i=0;i<clause.getBodyLength();i++) {
            assertTrue(bAtoms.contains(clause.getBodyAtom(i).toString()));
        }
        Set<String> hAtoms=new HashSet<String>();
        hAtoms.add("X == X2");
        hAtoms.add("Y1 != Y2");
        assertTrue(hAtoms.size()==clause.getHeadLength());
        for (int i=0;i<clause.getHeadLength();i++) {
            assertTrue(hAtoms.contains(clause.getHeadAtom(i).toString()));
        }
    }

    protected String[] getControl(String resource) throws Exception {
        if (resource==null)
            return null;
        List<String> control=new ArrayList<String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resource)));
        try {
            String line=reader.readLine();
            while (line!=null) {
                control.add(line);
                line=reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        String[] controlArray=new String[control.size()];
        control.toArray(controlArray);
        return controlArray;
    }

    protected void assertClausification(String ontologyResource,String controlResource) throws Exception {
        loadOntologyFromResource(ontologyResource);
        assertDLClauses(getControl(controlResource));
    }

    protected void assertDLClauses(String[] control) throws Exception {
        List<String> actualStrings = getDLClauses();
        assertContainsAll(this.getName(),actualStrings,control);
    }
}
