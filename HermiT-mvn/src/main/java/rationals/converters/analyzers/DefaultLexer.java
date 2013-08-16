package rationals.converters.analyzers;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import rationals.converters.ConverterException;

/**
 * Default lexical analyser for regular expressions.
 * This lexer parse a regular expression and treats each letter 
 * as a label.
 * 
 * @author yroos
 * @version $Id: DefaultLexer.java 2 2006-08-24 14:41:48Z oqube $
 */
public class DefaultLexer implements Lexer {
    private StreamTokenizer tokenizer;

    private boolean end;

    private String image;

    private int value;

    private int current;

    /**
     * Construct a lexical analyzer to parse given string.
     * 
     * @param in the String to parse.
     */
    public DefaultLexer(String in) {
        this(new StreamTokenizer(new StringReader(in)));
    }

    /**
     * construct a lexical analyzer to parse the characters from given
     * Reader object.
     * 
     * @param rd the character stream to parse.
     */
    public DefaultLexer(Reader rd) {
      this(new StreamTokenizer(rd));  
    }
    
    /**
     * Construct  a lexical analyzer that uses given StreamTokenizer object
     * to get data from.
     * Note that the tokenizer is reset and given new attributes.
     * 
     * @param st
     */
    public DefaultLexer(StreamTokenizer st) {
        tokenizer = st;
        tokenizer.resetSyntax();
        tokenizer.eolIsSignificant(false);
        tokenizer.lowerCaseMode(false);
        tokenizer.slashSlashComments(true);
        tokenizer.quoteChar('\"');
        tokenizer.wordChars('0', '9');
        tokenizer.whitespaceChars(0, 32);        
        end = false;
    }
    
    public void read() throws ConverterException {
        if (end) {
            current = END;
            return;
        }
        ;
        int tk;
        try {
            tk = tokenizer.nextToken();
        } catch (IOException e) {
            throw new ConverterException("Unexpected character");
        }
        if (tk == StreamTokenizer.TT_EOF) {
            end = true;
            value = 0;
            image = "";
            current = END;
            return;
        }
        if (tk == StreamTokenizer.TT_WORD) {
            image = tokenizer.sval;
            if (image.charAt(0) >= '0' && image.charAt(0) <= '9') {
                try {
                    value = Integer.parseInt(tokenizer.sval);
                    image = "";
                    if (value == 0)
                        current = EMPTY;
                    else {
                        if (value == 1)
                            current = EPSILON;
                        else
                            current = INT;
                    }
                    return;
                } catch (Exception e) {
                    current = UNKNOWN;
                    return;
                }
            }
            value = 0;
            current = LABEL;
            return;
        }
        image = "";
        value = 0;
        switch (tk) {
        case '+':
            current = UNION;
            return;
        case '*':
            current = STAR;
            return;
        case '^':
            current = ITERATION;
            return;
        case '|':
            current = SHUFFLE;
            return;
        case '#':
            current = MIX;
            return;
        case '(':
            current = OPEN;
            return;
        case ')':
            current = CLOSE;
            return;
        case '{':
            current = OBRACE;
            return;
        case '}':
            current = CBRACE;
            return;
        default:
            current = LABEL;
            image = "" + new Character((char) tk);
        }
    }

    public int lineNumber() {
        return tokenizer.lineno();
    }

    public Object label() {
        return image;
    }

    public int value() {
        return value;
    }

    public int current() {
        return current;
    }
}

