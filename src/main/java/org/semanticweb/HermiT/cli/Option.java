package org.semanticweb.HermiT.cli;

import static org.semanticweb.HermiT.cli.constants.kActions;
import static org.semanticweb.HermiT.cli.constants.kAlgorithm;
import static org.semanticweb.HermiT.cli.constants.kBlockCache;
import static org.semanticweb.HermiT.cli.constants.kBlockStrategy;
import static org.semanticweb.HermiT.cli.constants.kConclusion;
import static org.semanticweb.HermiT.cli.constants.kDefaultPrefix;
import static org.semanticweb.HermiT.cli.constants.kDirectBlock;
import static org.semanticweb.HermiT.cli.constants.kDumpClauses;
import static org.semanticweb.HermiT.cli.constants.kDumpPrefixes;
import static org.semanticweb.HermiT.cli.constants.kExpansion;
import static org.semanticweb.HermiT.cli.constants.kIgnoreUnsupportedDatatypes;
import static org.semanticweb.HermiT.cli.constants.kInternals;
import static org.semanticweb.HermiT.cli.constants.kMisc;
import static org.semanticweb.HermiT.cli.constants.kNoInconsistentException;
import static org.semanticweb.HermiT.cli.constants.kPrefixes;
import static org.semanticweb.HermiT.cli.constants.kPremise;

import java.text.BreakIterator;
import java.util.function.Function;

import gnu.getopt.LongOpt;

class Option {
    protected static final Option[] options=new Option[] {
            // meta:
            new Option('h',"help",kMisc,"display this help and exit"),
            new Option('V',"version",kMisc,"display version information and exit"),
            new Option('v',"verbose",kMisc,Boolean.FALSE,"AMOUNT","increase verbosity by AMOUNT levels (default 1)"),
            new Option('q',"quiet",kMisc,Boolean.FALSE,"AMOUNT","decrease verbosity by AMOUNT levels (default 1)"),
            new Option('o',"output",kMisc,Boolean.TRUE,"FILE","write output to FILE"),
            new Option(kPremise,"premise",kMisc,Boolean.TRUE,"PREMISE","set the premise ontology to PREMISE"),
            new Option(kConclusion,"conclusion",kMisc,Boolean.TRUE,"CONCLUSION","set the conclusion ontology to CONCLUSION"),

            // actions:
            new Option('l',"load",kActions,"parse and preprocess ontologies (default action)"),
            new Option('c',"classify",kActions,"classify the classes of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option('O',"classifyOPs",kActions,"classify the object properties of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option('D',"classifyDPs",kActions,"classify the data properties of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option('P',"prettyPrint",kActions,"when writing the classified hierarchy to a file, create a proper ontology and nicely indent the axioms according to their leven in the hierarchy"),
            new Option('k',"consistency",kActions,Boolean.FALSE,"CLASS","check satisfiability of CLASS (default owl:Thing)"),
            new Option('d',"direct",kActions,"restrict next subs/supers call to only direct sub/superclasses"),
            new Option('s',"subs",kActions,Boolean.TRUE,"CLASS","output classes subsumed by CLASS (or only direct subs if following --direct)"),
            new Option('S',"supers",kActions,Boolean.TRUE,"CLASS","output classes subsuming CLASS (or only direct supers if following --direct)"),
            new Option('e',"equivalents",kActions,Boolean.TRUE,"CLASS","output classes equivalent to CLASS"),
            new Option('U',"unsatisfiable",kActions,"output unsatisfiable classes (equivalent to --equivalents=owl:Nothing)"),
            new Option(kDumpPrefixes,"print-prefixes",kActions,"output prefix names available for use in identifiers"),
            new Option('E',"checkEntailment",kActions,"check whether the premise (option premise) ontology entails the conclusion ontology (option conclusion)"),

            new Option('N',"no-prefixes",kPrefixes,"do not abbreviate or expand identifiers using prefixes defined in input ontology"),
            new Option('p',"prefix",kPrefixes,Boolean.TRUE,"PN=IRI","use PN as an abbreviation for IRI in identifiers"),
            new Option(kDefaultPrefix,"prefix",kPrefixes,Boolean.TRUE,"IRI","use IRI as the default identifier prefix"),

            // algorithm tweaks:
            new Option(kDirectBlock,"block-match",kAlgorithm,Boolean.TRUE,"TYPE","identify blocked nodes with TYPE blocking; supported values are 'single', 'pairwise', and 'optimal' (default 'optimal')"),
            new Option(kBlockStrategy,"block-strategy",kAlgorithm,Boolean.TRUE,"TYPE","use TYPE as blocking strategy; supported values are 'ancestor', 'anywhere', 'core', and 'optimal' (default 'optimal')"),
            new Option(kBlockCache,"blockersCache",kAlgorithm,"cache blocking nodes for use in later tests; not possible with nominals or core blocking"),
            new Option(kIgnoreUnsupportedDatatypes,"ignoreUnsupportedDatatypes",kAlgorithm,"ignore unsupported datatypes"),
            new Option(kExpansion,"expansion-strategy",kAlgorithm,Boolean.TRUE,"TYPE","use TYPE as existential expansion strategy; supported values are 'el', 'creation', 'reuse', and 'optimal' (default 'optimal')"),
            new Option(kNoInconsistentException,"noInconsistentException",kAlgorithm,"do not throw an exception for an inconsistent ontology"),
            
            // internals:
            new Option(kDumpClauses,"dump-clauses",kInternals,Boolean.FALSE,"FILE","output DL-clauses to FILE (default stdout)")
        };

    enum Arg { 
        NONE(LongOpt.NO_ARGUMENT, "", f->""),
        OPTIONAL(LongOpt.OPTIONAL_ARGUMENT,"::", f->"[="+f+"]"),
        REQUIRED(LongOpt.REQUIRED_ARGUMENT,":",f->"="+f);
        int longOpt;
        String format;
        Function<String, String> example;

        private Arg(int l, String format, Function<String, String> example) {
            this.longOpt=l;
            this.format=format;
            this.example=example;
        }
        public static Arg fromBoolean(Boolean b) {
            if(b==null) {return NONE;}
            if(b.booleanValue()) {
                return REQUIRED;
            }
            return OPTIONAL;
        }
    }
    protected int optChar;
    protected String longStr;
    protected String group;
    protected Arg arg;
    protected String metavar;
    protected String help;

    public Option(int inChar,String inLong,String inGroup,String inHelp) {
        this(inChar, inLong, inGroup, null, null, inHelp);
    }
    public Option(int inChar,String inLong,String inGroup,Boolean argRequired,String inMetavar,String inHelp) {
        optChar=inChar;
        longStr=inLong;
        group=inGroup;
        arg=Arg.fromBoolean(argRequired);
        metavar=inMetavar;
        help=inHelp;
    }
    public static LongOpt[] createLongOpts(Option[] opts) {
        LongOpt[] out=new LongOpt[opts.length];
        for (int i=0;i<opts.length;++i) {
            out[i]=new LongOpt(opts[i].longStr,opts[i].arg.longOpt,null,opts[i].optChar);
        }
        return out;
    }

    public String getLongOptExampleStr() {
        if (longStr==null||longStr.equals(""))
            return "";
        return "--"+longStr+arg.example.apply(metavar);
    }

    public static String formatOptionHelp(Option[] opts) {
        StringBuilder out=new StringBuilder();
        int fieldWidth=0;
        for (Option o : opts) {
            int curWidth=o.getLongOptExampleStr().length();
            if (curWidth>fieldWidth)
                fieldWidth=curWidth;
        }
        String curGroup=null;
        for (Option o : opts) {
            if (o.group!=curGroup) {
                curGroup=o.group;
                out.append(System.getProperty("line.separator"));
                if (o.group!=null) {
                    out.append(curGroup+":");
                    out.append(System.getProperty("line.separator"));
                }
            }
            if (o.optChar<256) {
                out.append("  -");
                out.appendCodePoint(o.optChar);
                if (o.longStr!=null&&o.longStr!="") {
                    out.append(", ");
                }
                else {
                    out.append("  ");
                }
            }
            else {
                out.append("      ");
            }
            int fieldLeft=fieldWidth+1;
            if (o.longStr!=null&&o.longStr!="") {
                String s=o.getLongOptExampleStr();
                out.append(s);
                fieldLeft-=s.length();
            }
            for (;fieldLeft>0;--fieldLeft)
                out.append(' ');
            out.append(breakLines(o.help,80,6+fieldWidth+1));
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }

    public static String formatOptionsString(Option[] opts) {
        StringBuilder out=new StringBuilder();
        for (Option o : opts) {
            if (o.optChar<256) {
                out.appendCodePoint(o.optChar);
                out.append(o.arg.format);
            }
        }
        return out.toString();
    }

    protected static String breakLines(String str,int lineWidth,int indent) {
        StringBuilder out=new StringBuilder();
        BreakIterator i=BreakIterator.getLineInstance();
        i.setText(str);
        int curPos=0;
        int curLinePos=indent;
        int next=i.first();
        while (next!=BreakIterator.DONE) {
            String curSpan=str.substring(curPos,next);
            if (curLinePos+curSpan.length()>lineWidth) {
                out.append(System.getProperty("line.separator"));
                for (int j=0;j<indent;++j)
                    out.append(" ");
                curLinePos=indent;
            }
            out.append(curSpan);
            curLinePos+=curSpan.length();
            curPos=next;
            next=i.next();
        }
        return out.toString();
    }
}
