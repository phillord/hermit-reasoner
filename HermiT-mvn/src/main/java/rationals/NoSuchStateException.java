package rationals;

/** Instances of this class are thrown by the method
 * <tt>addTransition</tt> in class <tt>Automaton</tt>
 * when an attempt is made to use a state which does
 * not belong to te right automaton.
 * @author yroos@lifl.fr
 * @version 1.0
 * @see Automaton
*/
public class NoSuchStateException extends Exception {
    /**
   * 
   */
  public NoSuchStateException() {
    super();
    // TODO Auto-generated constructor stub
  }
  /**
   * @param message
   */
  public NoSuchStateException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }
  /**
   * @param message
   * @param cause
   */
  public NoSuchStateException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }
  /**
   * @param cause
   */
  public NoSuchStateException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }
}
