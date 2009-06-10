package org.semanticweb.HermiT.structural;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.semanticweb.HermiT.AbstractOntologyTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;

public abstract class AbstractStructuralTest extends AbstractOntologyTest {

    public AbstractStructuralTest(String name) {
        super(name);
    }
    protected static void assertContainsAll(String testName,Collection<String> actual,String[] control) {
        assertContainsAll(testName,actual,control,null);
    }
    protected static void assertContainsAll(String testName,Collection<String> actual,String[] controlVariant1,String[] controlVariant2) {
        try {
            assertEquals(controlVariant1.length,actual.size());
            boolean isOKVariant1=false;
            if (controlVariant1!=null) {
                isOKVariant1=true;
                for (int i=0;isOKVariant1 && i<controlVariant1.length;i++)
                    isOKVariant1=actual.contains(controlVariant1[i]);
            }
            boolean isOKVariant2=false;
            if (!isOKVariant1 && controlVariant2!=null) {
                isOKVariant2=true;
                for (int i=0;isOKVariant2 && i<controlVariant2.length;i++)
                    isOKVariant2=actual.contains(controlVariant2[i]);
            }
            assertTrue(isOKVariant1 || isOKVariant2);
        }
        catch (AssertionFailedError e) {
            System.out.println("Test "+testName+" failed!");
            if (controlVariant1!=null) {
                System.out.println("Control set 1 ("+controlVariant1.length+" elements):");
                System.out.println("------------------------------------------");
                for (String object : controlVariant1)
                    System.out.println(object.toString());
                System.out.println("------------------------------------------");
            }
            if (controlVariant2!=null) {
                System.out.println("Control set 2 ("+controlVariant2.length+" elements):");
                System.out.println("------------------------------------------");
                for (String object : controlVariant2)
                    System.out.println(object.toString());
                System.out.println("------------------------------------------");
            }
            System.out.println("Actual set ("+actual.size()+" elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }

    /**
     * tests that the set have equal length and that the actual set contains all objects from the control set, otherwise the test fails and the contents of the control and the actual set are printed
     */
    protected static <T> void assertContainsAll(String testName,Collection<T> actual,Collection<T> control) {
        try {
            assertEquals(control.size(),actual.size());
            for (T contr : control)
                assertTrue(actual.contains(contr));
        }
        catch (AssertionFailedError e) {
            System.out.println("Test "+testName+" failed!");
            System.out.println("Control set ("+control.size()+" elements):");
            System.out.println("------------------------------------------");
            for (T object : control)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.println("Actual set ("+actual.size()+" elements):");
            System.out.println("------------------------------------------");
            for (Object object : actual)
                System.out.println(object.toString());
            System.out.println("------------------------------------------");
            System.out.flush();
            throw e;
        }
    }
    
    protected static String[] S(String... strings) {
        return strings;
    }

    protected Set<String> getDLClauses() throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausifier.clausify(m_ontologyManager,m_ontology,noDescriptionGraphs);
        String ontologyIRI = m_ontology.getOntologyID().getDefaultDocumentIRI() == null ? "urn:hermit:kb" : m_ontology.getOntologyID().getDefaultDocumentIRI().toString();
        Set<String> actualStrings=new HashSet<String>();
        Prefixes prefixes=new Prefixes();
        prefixes.declareSemanticWebPrefixes();
        prefixes.declareInternalPrefixes(Collections.singleton(ontologyIRI+"#"));
        prefixes.declareDefaultPrefix(ontologyIRI+"#");
        for (DLClause dlClause : dlOntology.getDLClauses())
            actualStrings.add(dlClause.toOrderedString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getPositiveFacts())
            actualStrings.add(atom.toString(prefixes));
        for (org.semanticweb.HermiT.model.Atom atom : dlOntology.getNegativeFacts())
            actualStrings.add("not "+atom.toString(prefixes));
        return actualStrings;
    }
}
