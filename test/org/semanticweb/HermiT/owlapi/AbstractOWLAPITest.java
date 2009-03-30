package org.semanticweb.HermiT.owlapi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import junit.framework.AssertionFailedError;


import org.semanticweb.HermiT.AbstractOWLOntologyTest;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExpansionStrategy;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public abstract class AbstractOWLAPITest extends AbstractOWLOntologyTest {
    protected static final Node[][] NO_TUPLES=new Node[0][];
    protected static final String[] IGNORED_CLAUSES= { " :- owl:BottomDataProperty*(X,Y)"," :- owl:BottomObjectProperty(X,Y)" };

    public AbstractOWLAPITest(String name) {
        super(name);
    }

    /**
     * @return a clausified version of the loaded ontology
     * @throws Exception
     */
    protected DLOntology getDLOntology() throws Exception {
        OWLClausification clausifier=new OWLClausification(new Configuration());
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausifier.clausify(m_ontologyManager,m_ontology,noDescriptionGraphs);
    }

    protected Tableau getTableau() throws Exception {
        DLOntology dlOntology=getDLOntology();
        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
        ExpansionStrategy ExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        return new Tableau(null,ExpansionStrategy,dlOntology,new HashMap<String,Object>());
    }

    /**
     * tests that the sets have equal length and that the actual set contains all objects from the control set, otherwise the test fails and the contents of the control and the actual set are printed
     */
    protected static void assertContainsAll(String testName,Collection<String> actual,String[] control,String[] controlVariant) {
        for (String s : IGNORED_CLAUSES) {
            actual.remove(s);
        }
        for (Iterator<String> i=actual.iterator();i.hasNext();) {
            String s=i.next();
            if (s.startsWith("owl:TopObjectProperty")||s.startsWith("owl:TopDataProperty")) {
                i.remove();
            }
        }
        try {
            assertEquals(control.length,actual.size());
            boolean isOK=true;
            for (int i=0;i<control.length;i++) {
                if (isOK)
                    isOK=actual.contains(control[i]);
            }
            boolean isOKVariant=false;
            if (controlVariant!=null) {
                isOKVariant=true;
                for (int i=0;i<controlVariant.length;i++) {
                    if (isOK)
                        isOK=actual.contains(control[i]);
                }
            }
            assertTrue(isOK||isOKVariant);
        }
        catch (AssertionFailedError e) {
            System.out.println("Test "+testName+" failed!");
            System.out.println("Control set ("+control.length+" elements):");
            System.out.println("------------------------------------------");
            for (String object : control)
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

    /**
     * tests that the set have equal length and that the actual set contains all objects from the control set, otherwise the test fails and the contents of the control and the actual set are printed
     */
    protected static <T> void assertContainsAll(String testName,Collection<T> actual,Collection<T> control) {
        for (String s : IGNORED_CLAUSES) {
            actual.remove(s);
        }
        for (Iterator<T> i=actual.iterator();i.hasNext();) {
            T val=i.next();
            if (val instanceof String) {
                String s=(String)val;
                if (s.startsWith("owl:TopObjectProperty")||s.startsWith("owl:TopDataProperty")) {
                    i.remove();
                }
            }
        }
    
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
}
