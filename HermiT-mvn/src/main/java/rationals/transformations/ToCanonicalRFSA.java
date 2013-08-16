package rationals.transformations;


import rationals.Automaton;

public class ToCanonicalRFSA implements UnaryTransformation {

  public Automaton transform(Automaton a) {
    Reverser r = new Reverser() ;
    ToC c = new ToC() ;
    return c.transform(r.transform(c.transform(r.transform(a)))) ;
  }
}
