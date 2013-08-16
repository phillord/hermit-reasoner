package rationals.transformations;

import java.util.Iterator;
import java.util.Set;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * Complete an Automaton by adding a sink state and needed transitions.
 * <p />
 * <ul>
 * <li>C = complete(A)</li>
 * <li>S(C) = S(A) U {sink}</li>
 * <li>S0(C) = S0(A)</li>
 * <li>T(C) = T(A)</li>
 * <li>D(C) = D(A) U { (s1,a,sink)) | not exists (s1,a,s2) in D(A) }</li>
 * </ul>
 * 
 * @author nono
 * @version $Id: SinkComplete.java 6 2006-08-30 08:56:44Z oqube $
 */
public class SinkComplete implements UnaryTransformation {

  private Set alphabet;

  public SinkComplete(Set alphabet) {
    this.alphabet = alphabet;
  }

  public SinkComplete() {
  }

    /*
     *  (non-Javadoc)
     * @see rationals.transformations.UnaryTransformation#transform(rationals.Automaton)
     */
    public Automaton transform(Automaton a) {
      Automaton b = (Automaton) a.clone();
      Set alph = (alphabet == null) ? b.alphabet() : alphabet;
      State hole = null;
      Set states = b.getStateFactory().stateSet();
      states.addAll(b.states());
      Iterator i = states.iterator();
      while (i.hasNext()) {
	State e = (State) i.next();
	Iterator j = alph.iterator();
	while (j.hasNext()) {
	  Object label = j.next();
	  if (b.delta(e, label).isEmpty()) {
	    if (hole == null)
	      hole = b.addState(false, false);
	    try {
	      b.addTransition(new Transition(e, label, hole));
	    } catch (NoSuchStateException x) {
	    }
	  }
	}
      }
      if (!(hole == null)) {
	Iterator j = alph.iterator();
	while (j.hasNext()) {
	  try {
	    b.addTransition(new Transition(hole, j.next(), hole));
	  } catch (NoSuchStateException x) {
	  }
	}
      }
      return b;
    }
}
