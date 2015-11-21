package rationals.converters;
import rationals.Automaton;
import rationals.converters.analyzers.Parser;

public class Expression implements FromString {
  @Override
public Automaton fromString(String s) throws ConverterException {
    return new Parser(s).analyze() ;
  }
    
}

