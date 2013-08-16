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
 * Created 30 mai 07
 */
package rationals;

/**
 * An interface for easier creation of automata.
 * 
 * @author nono
 * 
 */
public interface Builder<T extends Builder<T>> {

  /**
   * Factory method.
   * 
   * @param label
   * @param auto
   * @return
   */
  T build(State label, Automaton<T> auto);
  
  /**
   * Sets the label of the transition.
   * 
   * @param label
   * @return this transition builder.
   */
  public T on(Object label);

  /**
   * Sets the end state and terminates transition construction. This method
   * effectively adds the transition to the automaton.
   * 
   * @param o
   *          the label of the end state.
   */
  public T go(Object o);

  /**
   * Adds a new transition in the automaton that loops on current label and from
   * state.
   * 
   * @return
   */
  public T loop();

  /**
   * Resets this builder to another starting state. Note that the state is
   * created if needed.
   * 
   * @param label
   *          the state to start from.
   * @return this builder.
   */
  public T from(Object label);

}
