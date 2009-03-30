package org.semanticweb.HermiT.owlapi;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.semanticweb.HermiT.AbstractOWLOntologyTest;

public abstract class AbstractOWLAPITest extends AbstractOWLOntologyTest {
    protected static final String[] IGNORED_CLAUSES={
        " :- owl:BottomDataProperty*(X,Y)",
        " :- owl:BottomObjectProperty(X,Y)"
    };

    public AbstractOWLAPITest(String name) {
        super(name);
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
