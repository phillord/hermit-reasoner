package rationals.converters.analyzers;

import rationals.Automaton;
import rationals.converters.ConverterException;
import rationals.transformations.Concatenation;
import rationals.transformations.Mix;
import rationals.transformations.Reducer;
import rationals.transformations.Shuffle;
import rationals.transformations.Star;
import rationals.transformations.Union;
// Grammar :
// E -> T E'
// E' -> + T E' | '/' '{' L '}' | eps
// T -> S T"
// T" -> '|' S T" | '#' S T" | eps
// S -> F T'
// T' -> F T' | eps
// F -> B B'
// B' -> * | int | ^ | eps
// B -> letter | 1 | 0 | ( E )
public class Parser {
    
  private Lexer lexico ;
  
  /**
   * Parse given string using standard grammar and lexical analyzer.
   * 
   * @param expression the expression to parse
   * @see Lexer
   * @see DefaultLexer
   */
  public Parser(String expression) {
    lexico = new DefaultLexer(expression) ;
  }

  /**
   * Parse using the given lexer. 
   * 
   * @param lexer the lexer to use for parsing.
   */
  public Parser(Lexer lexer) {
      this.lexico = lexer;
  }
  
  private Automaton error(String message) throws ConverterException {
    if (true) throw new ConverterException(
      "line " + lexico.lineNumber() + " , " + lexico.label() + " : " + message) ;
    return new Automaton() ;
  }
  
  public Automaton analyze() throws ConverterException {
    lexico.read() ;
    Automaton r = E() ;
    if (lexico.current() != Lexer.END) error("end of expression expected") ;
    return r ; 
  }
  
  private Automaton E() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL : {
        Automaton a = T() ;
        Automaton b = EP() ;
        return new Reducer().transform(new Union().transform(a , b)) ;
      }       
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.UNION :
      case Lexer.SHUFFLE :
	  case Lexer.MIX : 
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("expression expected") ;
    }
  }

  private Automaton EP() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL : return error("union expected") ; 
      case Lexer.CLOSE :
      case Lexer.END : return new Automaton() ;
      case Lexer.UNION : {
        lexico.read() ;
        Automaton a = T() ;
        Automaton b = EP() ;
        return new Reducer().transform(new Union().transform(a , b)) ;
      }
      case Lexer.SHUFFLE :
	  case Lexer.MIX : 
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("union expected") ; 
    }
  }

  private Automaton T() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL : {
        Automaton a = S() ;
        Automaton b = TS() ;
        return new Reducer().transform(
          new Shuffle().transform(a , b)) ;
      }       
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.UNION :
      case Lexer.SHUFFLE :
	  case Lexer.MIX : 
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("expression expected") ;
    }
  }

  private Automaton TS() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL :return error("concatenation expected") ;
      case Lexer.CLOSE :
      case Lexer.END : 
      case Lexer.UNION : return Automaton.epsilonAutomaton() ;
	  case Lexer.SHUFFLE : {
		lexico.read() ;
		Automaton a = S() ;
		Automaton b = TS() ;
		return new Reducer().transform(
		  new Shuffle().transform(a , b)) ;
	  }
	  case Lexer.MIX : 
	  	{
		lexico.read() ;
		Automaton a = S() ;
		Automaton b = TS() ;
		return new Reducer().transform(
		  new Mix().transform(a , b)) ;
	  }
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("concatenation expected") ; 
    }
  }

  private Automaton S() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL : {
        Automaton a = F() ;
        Automaton b = TP() ;
        return new Reducer().transform(
          new Concatenation().transform(a , b)) ;
      }       
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.UNION :
      case Lexer.SHUFFLE :
	  case Lexer.MIX : 
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("expression expected") ;
    }
  }

  private Automaton TP() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL :{
        Automaton a = F() ;
        Automaton b = TP() ;
        return new Reducer().transform(
          new Concatenation().transform(a , b)) ;
      }
      case Lexer.CLOSE :
      case Lexer.END : 
      case Lexer.UNION : 
	  case Lexer.MIX : 
      case Lexer.SHUFFLE :return Automaton.epsilonAutomaton() ;
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("concatenation expected") ; 
    }
  }

  private Automaton F() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.OPEN :
      case Lexer.LABEL : {
        Automaton a = BP(B()) ; 
        return a ;
      }       
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.UNION :
	  case Lexer.MIX : 
      case Lexer.SHUFFLE :
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("factor expected") ;
    }
  }

  private Automaton B() throws ConverterException {
    switch(lexico.current()) {
      case Lexer.EPSILON : {
        Automaton a = Automaton.epsilonAutomaton() ;
        lexico.read() ;
        return a ;
      }
      case Lexer.EMPTY : {
        Automaton a = new Automaton() ;
        lexico.read() ;
        return a ;
      }
      case Lexer.OPEN : {
        lexico.read() ;
        Automaton a = E() ;
        if (lexico.current() != Lexer.CLOSE) return error("( expected") ;
        lexico.read() ;
        return a ;
      }
      case Lexer.LABEL : {
        Automaton a = Automaton.labelAutomaton(lexico.label()) ;
        lexico.read() ;
        return a ;
      }      
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.SHUFFLE :
	  case Lexer.MIX : 
      case Lexer.UNION :
      case Lexer.STAR :
      case Lexer.ITERATION :
      case Lexer.INT :
      default : return error("factor expected") ;
    }
  }

  private Automaton BP(Automaton a) throws ConverterException {
    switch(lexico.current()) {
      case Lexer.OPEN :
      case Lexer.LABEL :
      case Lexer.CLOSE :
      case Lexer.END :
      case Lexer.UNION : 
	  case Lexer.MIX : 
      case Lexer.SHUFFLE :return a ;
      case Lexer.STAR : {
        lexico.read() ; 
        return new Reducer().transform(new Star().transform(a)) ;
      }
      case Lexer.ITERATION :
        lexico.read() ; 
        return new Reducer().transform(
          new Concatenation().transform(a , 
            new Star().transform(a))) ;
      case Lexer.EPSILON :
      case Lexer.EMPTY :
      case Lexer.INT : {
        int value = lexico.value() ;
        lexico.read() ;
        Automaton b = Automaton.epsilonAutomaton() ;
        for (int i = 0 ; i < value ; i++) {
          b = new Reducer().transform(
            new Concatenation().transform(b , a)) ;
        }
        return b ;
      }
      default : return error("Unexpected character") ;
    }
  }
}
