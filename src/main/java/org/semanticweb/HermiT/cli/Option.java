package org.semanticweb.HermiT.cli;

import static org.semanticweb.HermiT.cli.constants.kActions;
import static org.semanticweb.HermiT.cli.constants.kAlgorithm;
import static org.semanticweb.HermiT.cli.constants.kInternals;
import static org.semanticweb.HermiT.cli.constants.kMisc;
import static org.semanticweb.HermiT.cli.constants.kPrefixes;

import java.text.BreakIterator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.cli.Options;

class Option {
    static final String PREFIX = "p";
    static final String NO_PREFIXES = "N";
    static final String CHECK_ENTAILMENT = "E";
    static final String UNSATISFIABLE = "U";
    static final String EQUIVALENTS = "e";
    static final String SUPERS = "S";
    static final String SUBS = "s";
    static final String DIRECT = "d";
    static final String CONSISTENCY = "k";
    static final String PRETTY_PRINT = "P";
    static final String CLASSIFY_DPS = "D";
    static final String CLASSIFY_OPS = "O";
    static final String CLASSIFY = "c";
    static final String LOAD = "l";
    static final String OUTPUT = "o";
    static final String QUIET = "q";
    static final String VERBOSE = "v";
    static final String VERSION = "V";
    static final String HELP = "h";
    static final String DUMP_CLAUSES = "dC";
    static final String DEFAULT_PREFIXES = "dF";
    static final String DUMP_PREFIXES = "dP";
    static final String BASE = "kB";
    static final String BLOCK_CACHE = "kBC";
    static final String BLOCK_STRATEGY = "kBS";
    static final String CONCLUSION = "kC";
    static final String DIRECT_BLOCK = "kDB";
    static final String EXPANSION = "kE";
    static final String IGNORE_UNSUPPORTED_DATATYPES = "kI";
    static final String NO_INCONSISTENT_EXCEPTION = "kNE";
    static final String PREMISE = "kP";
    protected static final Option[] options=new Option[] {
            // meta:
            new Option(HELP,"help",kMisc,"display this help and exit"),
            new Option(VERSION,"version",kMisc,"display version information and exit"),
            new Option(VERBOSE,"verbose",kMisc,Arg.OPTIONAL,"AMOUNT","increase verbosity by AMOUNT levels (default 1)"),
            new Option(QUIET,"quiet",kMisc,Arg.OPTIONAL,"AMOUNT","decrease verbosity by AMOUNT levels (default 1)"),
            new Option(OUTPUT,"output",kMisc,Arg.REQUIRED,"FILE","write output to FILE"),
            new Option(PREMISE,"premise",kMisc,Arg.REQUIRED,"PREMISE","set the premise ontology to PREMISE"),
            new Option(CONCLUSION,"conclusion",kMisc,Arg.REQUIRED,"CONCLUSION","set the conclusion ontology to CONCLUSION"),

            // actions:
            new Option(LOAD,"load",kActions,"parse and preprocess ontologies (default action)"),
            new Option(CLASSIFY,"classify",kActions,"classify the classes of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option(CLASSIFY_OPS,"classifyOPs",kActions,"classify the object properties of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option(CLASSIFY_DPS,"classifyDPs",kActions,"classify the data properties of the ontology, optionally writing taxonomy to a file if -o (--output) is used"),
            new Option(PRETTY_PRINT,"prettyPrint",kActions,"when writing the classified hierarchy to a file, create a proper ontology and nicely indent the axioms according to their leven in the hierarchy"),
            new Option(CONSISTENCY,"consistency",kActions,Arg.OPTIONAL,"CLASS","check satisfiability of CLASS (default owl:Thing)"),
            new Option(DIRECT,"direct",kActions,"restrict next subs/supers call to only direct sub/superclasses"),
            new Option(SUBS,"subs",kActions,Arg.REQUIRED,"CLASS","output classes subsumed by CLASS (or only direct subs if following --direct)"),
            new Option(SUPERS,"supers",kActions,Arg.REQUIRED,"CLASS","output classes subsuming CLASS (or only direct supers if following --direct)"),
            new Option(EQUIVALENTS,"equivalents",kActions,Arg.REQUIRED,"CLASS","output classes equivalent to CLASS"),
            new Option(UNSATISFIABLE,"unsatisfiable",kActions,"output unsatisfiable classes (equivalent to --equivalents=owl:Nothing)"),
            new Option(DUMP_PREFIXES,"print-prefixes",kActions,"output prefix names available for use in identifiers"),
            new Option(CHECK_ENTAILMENT,"checkEntailment",kActions,"check whether the premise (option premise) ontology entails the conclusion ontology (option conclusion)"),

            new Option(NO_PREFIXES,"no-prefixes",kPrefixes,"do not abbreviate or expand identifiers using prefixes defined in input ontology"),
            new Option(PREFIX,"prefix",kPrefixes,Arg.REQUIRED,"PN=IRI","use PN as an abbreviation for IRI in identifiers"),
            new Option(DEFAULT_PREFIXES,"default-prefix",kPrefixes,Arg.REQUIRED,"IRI","use IRI as the default identifier prefix"),
            new Option(BASE,"base-prefix",kPrefixes,Arg.REQUIRED,"IRI","use IRI as the base to resolve relative ontology arguments"),

            // algorithm tweaks:
            new Option(DIRECT_BLOCK,"block-match",kAlgorithm,Arg.REQUIRED,"TYPE","identify blocked nodes with TYPE blocking; supported values are 'single', 'pairwise', and 'optimal' (default 'optimal')"),
            new Option(BLOCK_STRATEGY,"block-strategy",kAlgorithm,Arg.REQUIRED,"TYPE","use TYPE as blocking strategy; supported values are 'ancestor', 'anywhere', 'core', and 'optimal' (default 'optimal')"),
            new Option(BLOCK_CACHE,"blockersCache",kAlgorithm,"cache blocking nodes for use in later tests; not possible with nominals or core blocking"),
            new Option(IGNORE_UNSUPPORTED_DATATYPES,"ignoreUnsupportedDatatypes",kAlgorithm,"ignore unsupported datatypes"),
            new Option(EXPANSION,"expansion-strategy",kAlgorithm,Arg.REQUIRED,"TYPE","use TYPE as existential expansion strategy; supported values are 'el', 'creation', 'reuse', and 'optimal' (default 'optimal')"),
            new Option(NO_INCONSISTENT_EXCEPTION,"noInconsistentException",kAlgorithm,"do not throw an exception for an inconsistent ontology"),
            
            // internals:
            new Option(DUMP_CLAUSES,"dump-clauses",kInternals,Arg.OPTIONAL,"FILE","output DL-clauses to FILE (default stdout)")
        };

    enum Arg { 
        NONE(false,"", f->""),
        OPTIONAL(false,"::", f->"[="+f+"]"),
        REQUIRED(true,":",f->"="+f);
        String format;
        Function<String, String> example;
        boolean argRequired;

        private Arg(boolean required, String format, Function<String, String> example) {
            this.format=format;
            this.example=example;
            this.argRequired=required;
        }
    }
    protected String optChar;
    protected String longStr;
    protected String group;
    protected Arg arg;
    protected String metavar;
    protected String help;

    public Option(String inChar,String inLong,String inGroup,String inHelp) {
        this(inChar, inLong, inGroup, Arg.NONE, null, inHelp);
    }
    public Option(String inChar,String inLong,String inGroup,Arg argRequired,String inMetavar,String inHelp) {
        optChar=inChar;
        longStr=inLong;
        group=inGroup;
        arg=argRequired;
        metavar=inMetavar;
        help=inHelp;
    }
    public static Options createLongOpts(Option[] opts) {
        Options out=new Options();
        Map<String, String> optionTokens=new LinkedHashMap<>();
        for (Option o:opts) {
            org.apache.commons.cli.Option option = new org.apache.commons.cli.Option(o.optChar, o.longStr, o.arg!=Arg.NONE, o.help);
            option.setOptionalArg(o.arg.argRequired);
            if(optionTokens.containsKey(o.optChar)) {
                throw new RuntimeException("Bug in command line setup: some options are in conflict. "+o.optChar + " is used for two options.");
            }
            optionTokens.put(o.optChar, o.longStr);
            out.addOption(option);
        }
        if(optionTokens.values().size()!=new HashSet<>(optionTokens.values()).size()) {
            throw new RuntimeException("Bug in command line setup: some options are in conflict. Long option keys are used for more than one option. "+optionTokens.values());
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
            if (o.optChar.codePointAt(0)<256) {
                out.append("  -");
                out.append(o.optChar);
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
            if (o.optChar.codePointAt(0)<256) {
                out.append(o.optChar);
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
