package rationals;

import static org.junit.Assert.*;

import org.junit.Test;
@SuppressWarnings("javadoc")
public class AutomatonBuilderTest {
    @Test
    public void testStateLabellingYieldUniqueStates() {
        Automaton a = new Automaton();
        State s = a.state("init");
        State s2 = a.state("init");
        assertSame("should be same object", s, s2);
        assertEquals("objects should be equals", s, s2);
    }
}
