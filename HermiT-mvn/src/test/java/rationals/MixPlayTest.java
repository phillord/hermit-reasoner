package rationals;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import rationals.converters.Expression;
import rationals.transformations.Pruner;

/*
 * Created on Apr 9, 2004
 * 
 * $Log: MixPlayTest.java,v $
 * Revision 1.1  2004/06/25 09:14:29  bailly
 * creation repetoire de test
 *
 * Revision 1.5  2004/04/15 11:51:00  bailly
 * added randomization of MixPlay
 * TODO: check accessibility of synchronization letter
 *
 * Revision 1.4  2004/04/14 07:33:43  bailly
 * correct version of synchronization on the fly
 *
 * Revision 1.3  2004/04/13 07:08:38  bailly
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/12 16:37:59  bailly
 * worked on synchronization algorithm : begins to work but
 * there are still problems with proper implementation of backtracking
 *
 * Revision 1.1  2004/04/09 15:51:50  bailly
 * Added algorithm for computing a mixed word from several automata (to be verified)
 *
 */

/**
 * @author bailly
 * @version $Id: MixPlayTest.java 2 2006-08-24 14:41:48Z oqube $
 */
public class MixPlayTest extends TestCase {

    /**
     * 
     */
    public MixPlayTest(String name) {
        super(name);
    }

    public void test01Simple() throws Exception {
        Automaton a = new Pruner().transform(new Expression()
                .fromString("a(bbb)*e"));
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a(bb)^e"));
        //      Automaton c = new Pruner().transform(new Expression().fromString("bdde"));      
        MixPlay mp = new MixPlay();
        mp.addAutomaton(b);
        mp.addAutomaton(a);
        //      mp.addAutomaton(c);
        List check = Arrays.asList(new String[] { "a", "b", "b", "b", "b", "b",
                "b", "e" });
        mp.reset();
        List word = mp.play("e");
        assertEquals(check, word);
    }

    public void test02Simple() throws Exception {
        Automaton a = new Pruner().transform(new Expression()
                .fromString("a(bbb)*e"));
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a(bb)*e"));
        //      Automaton c = new Pruner().transform(new Expression().fromString("bdde"));      
        MixPlay mp = new MixPlay();
        mp.addAutomaton(b);
        mp.addAutomaton(a);
        //      mp.addAutomaton(c);
        List check = Arrays.asList(new String[] { "a", "e" });
        mp.reset();
        List word = mp.play("e");
        assertEquals(check, word);
    }

    public void test03Simple() throws Exception {
        Automaton a = new Pruner().transform(new Expression()
                .fromString("abbbe"));
        Automaton b = new Pruner().transform(new Expression()
                .fromString("abbe"));
        //      Automaton c = new Pruner().transform(new Expression().fromString("bdde"));      
        MixPlay mp = new MixPlay();
        mp.addAutomaton(b);
        mp.addAutomaton(a);
        //      mp.addAutomaton(c);
        mp.reset();
        List word = mp.play("e");
        assertTrue(word.isEmpty());
    }

    public void test04Replay() throws Exception {
        Automaton a = new Pruner().transform(new Expression()
                .fromString("a(bbb)*e"));
        Automaton b = new Pruner().transform(new Expression()
                .fromString("a(bb)*e"));
        //      Automaton c = new Pruner().transform(new Expression().fromString("bdde"));      
        MixPlay mp = new MixPlay();
        mp.addAutomaton(b);
        mp.addAutomaton(a);
        //      mp.addAutomaton(c);
        List check = Arrays.asList(new String[] { "a" });
        mp.reset();
        List word = mp.play("a");
        word = mp.play("e");
        check = Arrays.asList(new String[] { "e" });
        assertEquals(check, word);
    }
}