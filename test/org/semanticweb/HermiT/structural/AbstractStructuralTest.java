package org.semanticweb.HermiT.structural;

import java.util.Collection;

import junit.framework.AssertionFailedError;

import org.semanticweb.HermiT.AbstractOWLOntologyTest;

public abstract class AbstractStructuralTest extends AbstractOWLOntologyTest {

    public AbstractStructuralTest(String name) {
        super(name);
    }
    /**
     * tests that the sets have equal length and that the actual set contains all objects from the control set, otherwise the test fails and the contents of the control and the actual set are printed
     */
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
            if (controlVariant2!=null) {
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
}
