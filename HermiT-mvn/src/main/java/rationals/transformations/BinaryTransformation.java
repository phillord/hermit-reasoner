package rationals.transformations;

import rationals.Automaton;

/**
 * A generic interface for binary operations between two automata.
 * 
 * @author nono
 * @version $Id: BinaryTransformation.java 2 2006-08-24 14:41:48Z oqube $
 */
public interface BinaryTransformation {
  public Automaton transform(Automaton a , Automaton b) ;
}
