/*
 * Copyright (c) 2007 - OQube / Arnaud Bailly This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Created 29 mai 07
 */
package rationals;

/**
 * A class for step-by-step creation of transitions. A TransitionBuilder can be
 * used to add expesiveness to transition creation withing automaton.
 * 
 * @author nono
 * 
 */
public class TransitionBuilder implements Builder<TransitionBuilder> {

  private State start;

  private Automaton automaton;

  protected Object label;

  /**
   * Creates a transition builder for given automaton.
   * 
   * @param state
   *          the starting state of transition.
   * @param automaton
   *          the automaton where transition will be added.
   */
  public TransitionBuilder(State state, Automaton automaton) {
    this.start = state;
    this.automaton = automaton;
  }

  public TransitionBuilder() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Sets the label of the transition.
   * 
   * @param label
   * @return this transition builder.
   */
  public TransitionBuilder on(Object label) {
    this.label = label;
    return this;
  }

  /**
   * Sets the end state and terminates transition construction. This method
   * effectively adds the transition to the automaton.
   * 
   * @param o
   *          the label of the end state.
   */
  public TransitionBuilder go(Object o) {
    State s = automaton.state(o);
    try {
      automaton.addTransition(new Transition(start, label, s));
    } catch (NoSuchStateException e) {
      assert false;
    }
    return this;
  }

  /**
   * Adds a new transition in the automaton that loops on current label and from
   * state.
   * 
   * @return
   */
  public TransitionBuilder loop() {
    try {
      automaton.addTransition(new Transition(start, label, start));
    } catch (NoSuchStateException e) {
      assert false;
    }
    return this;
  }

  /**
   * Resets this builder to another starting state. Note that the state is
   * created if needed.
   * 
   * @param label
   *          the state to start from.
   * @return this builder.
   */
  public TransitionBuilder from(Object label) {
    this.start = automaton.state(label);
    this.label = null;
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see rationals.Builder#build(java.lang.Object, rationals.Automaton)
   */
  public TransitionBuilder build(State state, Automaton<TransitionBuilder> auto) {
    this.start = state;
    this.label = null;
    this.automaton = auto;
    return this;
  }

}
