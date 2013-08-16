package rationals;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

public class AutomatonBuilderTest extends TestCase {

  public void testStateLabellingYieldUniqueStates() {
    Automaton a = new Automaton();
    State s = a.state("init");
    State s2 = a.state("init");
    assertSame("should be same object", s, s2);
    assertEquals("objects should be equals", s, s2);
  }

  public void testUpdateStateAfterCreationAndAddTransition() {
    Automaton<TransitionBuilder> a = new Automaton<TransitionBuilder>();
    a.setBuilder(new TransitionBuilder());
    a.state("init").setInitial(true);
    a.from("init").on("a").go("next");
    a.state("next").setTerminal(true);
    System.err.println(a);
    assertTrue("automaton should accept word 'a'", a.accept(Collections
        .<Object>singletonList("a")));
  }

  public void testAddSeveralTransitions() {
    Automaton<TransitionBuilder> a = new Automaton<TransitionBuilder>();
    a.setBuilder(new TransitionBuilder());
    a.from("init").on("a").go("next");
    a.from("next").on("b").go("other");
    a.from("other").on("c").go("next");
    a.state("next").setTerminal(true);
    a.state("init").setInitial(true);
    assertTrue("automaton should accept word 'abcbc'", a.accept(Arrays
        .<Object>asList(new Object[] { "a", "b", "c", "b", "c" })));
    assertTrue("automaton should not accept word 'ab'", !a.accept(Arrays
        .<Object>asList(new Object[] { "a", "b" })));
  }
  
  public void testAddSeveralTransitionsFromSameStateAndLoops() {
    Automaton<TransitionBuilder> a = new Automaton<TransitionBuilder>();
    a.setBuilder(new TransitionBuilder());
    a.from("init").on("a").go("next").on("b").go("other");
    a.from("next").on("c").loop().on("b").go("other");
    a.state("init").setInitial(true);
    a.state("other").setTerminal(true);
    assertTrue("automaton should accept word 'accb'", a.accept(Arrays
        .<Object>asList(new Object[] { "a", "c", "c", "b" })));
    assertTrue("automaton should not accept word 'a'", !a.accept(Arrays
        .<Object>asList(new Object[] { "a"})));    
  }
  
  public void testRestartBuilderFromOtherState() {
    Automaton<TransitionBuilder> a = new Automaton<TransitionBuilder>();
    a.setBuilder(new TransitionBuilder());
    a.from("init").on("a").go("next").on("b").go("other")
    .from("next").on("c").loop().on("b").go("other");
    a.state("init").setInitial(true);
    a.state("other").setTerminal(true);
    assertTrue("automaton should accept word 'accb'", a.accept(Arrays
        .<Object>asList(new Object[] { "a", "c", "c", "b" })));
    assertTrue("automaton should not accept word 'a'", !a.accept(Arrays
        .<Object>asList(new Object[] { "a"})));    
  }

}
