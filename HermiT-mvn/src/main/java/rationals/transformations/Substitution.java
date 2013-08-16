/**
 * 
 */
package rationals.transformations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rationals.Automaton;
import rationals.NoSuchStateException;
import rationals.State;
import rationals.Transition;

/**
 * A (rational) substitution is a  morphism that maps
 * letters to languages.
 * A rational substitution is constructed like a {@see rationals.transformations.Morphism}
 * with an instance of {@see java.util.Map} that has Object as 
 * keys and either Object or Automaton instances as values.
 * If the value of a key is an object, then it is  considered a letter 
 * and this class acts like a morphism. If the value of the key is an
 * Automaton, then the complete transition is replaced by the language
 * denoted by the automaton.
 * 
 * @author nono
 * @see J.Berstel, "Transductions and context-free languages"
 */
public class Substitution implements UnaryTransformation {

	private Map morph;

	public Substitution(Map m) {
		this.morph = m;
	}

	/* (non-Javadoc)
	 * @see rationals.transformations.UnaryTransformation#transform(rationals.Automaton)
	 */
	public Automaton transform(Automaton a) {
		Automaton b = new Automaton();
		/* state map */
		Map stm = new HashMap();
		for (Iterator i = a.delta().iterator(); i.hasNext();) {
			Transition tr = (Transition) i.next();
			State ns = tr.start();
			State nss = (State) stm.get(ns);
			if (nss == null) {
				nss = b.addState(ns.isInitial(), ns.isTerminal());
				stm.put(ns, nss);
			}
			State ne = tr.end();
			State nse = (State) stm.get(ne);
			if (nse == null) {
				nse = b.addState(ne.isInitial(), ne.isTerminal());
				stm.put(ne, nse);
			}
			Object lbl = tr.label();
			if (!morph.containsKey(lbl))
				try {
					b.addTransition(new Transition(nss, lbl, nse));
				} catch (NoSuchStateException e) {
				}
			else
				try {
					/* is value an automaton ? */
					Object o = morph.get(lbl);
					if (o instanceof Automaton)
						insert(nss, nse, b, (Automaton) o);
					else
						b
								.addTransition(new Transition(nss, morph
										.get(lbl), nse));
				} catch (NoSuchStateException e1) {
				}
		}
		return b;
	}

	/**
	 * Insert <code>automaton</code> between states <code>nss</code> and
	 * <code>nse</code> in automaton <code>b</code>.
	 * This method add epsilon transitions from <code>nss</code> to each starting 
	 * state of automaton and from each ending state to <code>nse</code>.
	 * 
	 * @param nss
	 * @param nse
	 * @param b
	 * @param automaton
	 */

	private void insert(State nss, State nse, Automaton b, Automaton automaton) {
		/* map states */
		Map map = new HashMap();
		for (Iterator i = automaton.states().iterator(); i.hasNext();) {
			State e = (State) i.next();
			State n = b.addState(false, false);
			map.put(e, n);
			if (e.isInitial())
				try {
					b.addTransition(new Transition(nss, null, n));
				} catch (NoSuchStateException e1) {
				}
			if (e.isTerminal())
				try {
					b.addTransition(new Transition(n, null, nse));
				} catch (NoSuchStateException e1) {
				}

		}
		for (Iterator i = automaton.delta().iterator(); i.hasNext();) {
			Transition t = (Transition) i.next();
			try {
				b.addTransition(new Transition((State) map.get(t.start()), t
						.label(), (State) map.get(t.end())));
			} catch (NoSuchStateException x) {
			}
		}

	}

}
