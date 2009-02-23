package org.semanticweb.HermiT.tableau;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;

public abstract class AbstractHermiTTest extends TestCase {
    protected static final Node[][] NO_TUPLES=new Node[0][];
    protected static final DLOntology EMPTY_DL_ONTOLOGY;
    static {
        Set<DLClause> dlClauses = Collections.emptySet();
        Set<Atom> atoms = Collections.emptySet();
        EMPTY_DL_ONTOLOGY = new DLOntology(
                "opaque:test", // ontology_URI
                dlClauses, // clauses
                atoms, // positive facts
                atoms, // negative facts 
                null, // atomic concepts
                null, // object roles
                null, // data roles
                null, // individuals
                false, // hasInverseRoles
                false, // hasAtMostRestrictions
                false, // hasNominals
                false, // canUseNIRule
                false); // hasDatatypes
    }

    public AbstractHermiTTest(String name) {
        super(name);
    }
    protected Ontology getOntology(String physicalURI) throws Exception {
        DefaultOntologyResolver resolver=new DefaultOntologyResolver();
        String ontologyURI=resolver.registerOntology(physicalURI);
        OntologyManager ontologyManager=KAON2Manager.newOntologyManager();
        ontologyManager.setOntologyResolver(resolver);
        return ontologyManager.openOntology(ontologyURI,new HashMap<String,Object>()); 
    }
    protected Ontology getOntologyFromResource(String resourceName) throws Exception {
        return getOntology(getClass().getResource(resourceName).toString());
    }
    protected Set<Axiom> getAxioms(String resourceName) throws Exception {
        return getOntologyFromResource(resourceName).createAxiomRequest().get();
    }
    protected Set<String> getStrings(String resourceName) throws Exception {
        Set<String> strings=new HashSet<String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResource(resourceName).openStream()));
        try {
            String line=reader.readLine();
            while (line!=null) {
                strings.add(line);
                line=reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        return strings;
    }
    protected String getResourceText(String resourceName) throws Exception {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter output=new PrintWriter(buffer);
        BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getResource(resourceName).openStream()));
        try {
            String line=reader.readLine();
            while (line!=null) {
                output.println(line);
                line=reader.readLine();
            }
        }
        finally {
            reader.close();
        }
        output.flush();
        return buffer.toString();
    }
    protected void assertEquals(Set<Axiom> axioms,String controlResourceName) throws Exception {
        Set<Axiom> controlAxioms=getAxioms(controlResourceName);
        assertEquals(axioms,controlAxioms);
    }
    protected <T> void assertEquals(Set<T> actual,Set<T> control) throws Exception {
        if (!actual.equals(control)) {
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
            assertTrue(false);
        }
    }
    protected static <T> void assertContainsAll(Collection<T> actual,T... control) {
        try {
            assertEquals(control.length,actual.size());
            for (int i=0;i<control.length;i++)
                assertTrue(actual.contains(control[i]));
        }
        catch (AssertionFailedError e) {
            System.out.println("Control set ("+control.length+" elements):");
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
    protected static void assertRetrieval(ExtensionTable extensionTable,Object[] searchTuple,ExtensionTable.View extensionView,Object[][] expectedTuples) {
        boolean[] bindingPattern=new boolean[searchTuple.length];
        for (int i=0;i<searchTuple.length;i++)
            if (searchTuple[i]!=null)
                bindingPattern[i]=true;
        ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(bindingPattern,extensionView);
        System.arraycopy(searchTuple,0,retrieval.getBindingsBuffer(),0,searchTuple.length);
        assertRetrieval(retrieval,expectedTuples);
    }

    protected static void assertRetrieval(ExtensionTable.Retrieval retrieval,Object[][] expectedTuples) {
        retrieval.open();
        boolean[] consumed=new boolean[expectedTuples.length];
        while (!retrieval.afterLast()) {
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            boolean tupleFound=false;
            for (int i=0;!tupleFound && i<expectedTuples.length;i++) {
                if (!consumed[i] && tuplesEqual(tupleBuffer,expectedTuples[i])) {
                    consumed[i]=true;
                    tupleFound=true;
                }
            }
            if (!tupleFound)
                fail("Tuple from the retrieval not found in the expected tuples.");
            retrieval.next();
        }
        for (int i=0;i<consumed.length;i++)
            if (!consumed[i])
                fail("Tuple from the expected list has not been seen in the retrieval.");
    }
    protected static void assertEquals(Object[] tuple1,Object[] tuple2) {
        assertEquals(tuple1.length,tuple2.length);
        for (int index=0;index<tuple1.length;index++)
            assertEquals(tuple1[index],tuple2[index]);
    }
    protected static boolean tuplesEqual(Object[] tuple1,Object[] tuple2) {
        if (tuple1.length!=tuple2.length)
            return false;
        for (int i=0;i<tuple1.length;i++)
            if (!tuple1[i].equals(tuple2[i]))
                return false;
        return true;
    }

    protected static Object[] T(Object... nodes) {
        return nodes;
    }
}
