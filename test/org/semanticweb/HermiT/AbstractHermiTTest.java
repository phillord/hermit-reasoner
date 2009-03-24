package org.semanticweb.HermiT;

import java.util.Collection;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractHermiTTest extends TestCase {

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

    public AbstractHermiTTest() {
        super();
    }

    public AbstractHermiTTest(String name) {
        super(name);
    }

}
