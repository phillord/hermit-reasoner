package rationals.converters;

import rationals.Automaton;


public interface FromString {
    Automaton fromString(String s) throws ConverterException ;
}
