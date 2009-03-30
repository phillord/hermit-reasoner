package org.semanticweb.HermiT.structural;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.HermiT.structural.OWLHasKeyDummy;

public class ClausificationTest extends AbstractStructuralTest {
    static {
        System.setProperty("entityExpansionLimit",String.valueOf(Integer.MAX_VALUE));
    }

    public ClausificationTest(String name) {
        super(name);
    }

    public void testBasic() throws Exception {
        assertClausification("res/basic-input.xml","res/basic-control.txt",null);
    }

    public void testNominals1() throws Exception {
        assertClausification("res/nominals-1-input.xml","res/nominals-1-control-1.txt","res/nominals-1-control-2.txt");
    }

   public void testNominals2() throws Exception {
        assertClausification("res/nominals-2-input.xml","res/nominals-2-control-1.txt","res/nominals-2-control-2.txt");
    }

    public void testNominals3() throws Exception {
        assertClausification("res/nominals-3-input.xml","res/nominals-3-control.txt",null);
    }

    public void testNominals4() throws Exception {
        assertClausification("res/nominals-4-input.xml","res/nominals-4-control.txt",null);
    }

    public void testAsymmetry() throws Exception {
        assertClausification("res/asymmetry-input.xml","res/asymmetry-control.txt",null);
    }

    public void testExistsSelf1() throws Exception {
        assertClausification("res/exists-self-1-input.owl","res/exists-self-1-control-1.txt","res/exists-self-1-control-2.txt");
    }

    public void testExistsSelf2() throws Exception {
        assertClausification("res/exists-self-2-input.owl","res/exists-self-2-control.txt",null);
    }

    public void testHasKeys() throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        DLClause clause=clausifier.clausifyKey(OWLHasKeyDummy.getDemoKey());
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

    protected void assertClausification(String ontologyResource,String controlResource,String controlResourceVariant) throws Exception {
        loadOWLOntologyFromResource(ontologyResource);
        assertDLClauses(getControl(controlResource),getControl(controlResourceVariant));
    }

    protected void assertDLClauses(String[] control,String[] controlVariant) throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausifier.clausify(m_ontologyManager,m_ontology,noDescriptionGraphs);
        Set<String> actualStrings=new HashSet<String>();
        Prefixes prefixes=new Prefixes();
        prefixes.declareSemanticWebPrefixes();
        prefixes.declareInternalPrefixes(Collections.singleton(m_ontology.getURI()+"#"));
        prefixes.declareDefaultPrefix(m_ontology.getURI()+"#");
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not "+atom.toString(prefixes));
        assertContainsAll(this.getName(),actualStrings,control,controlVariant);
    }
}
