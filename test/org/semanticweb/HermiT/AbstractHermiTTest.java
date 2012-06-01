package org.semanticweb.HermiT;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractHermiTTest extends TestCase {

    public AbstractHermiTTest() {
        super();
    }
    public AbstractHermiTTest(String name) {
        super(name);
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
        if (resourceName==null)
            return null;
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
    @SuppressWarnings("all")
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
}
