package rationals.converters;

import rationals.Automaton;


public interface FromString {
  public Automaton fromString(String s) throws ConverterException ;
}
