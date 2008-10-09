// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.text.BreakIterator;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import org.semanticweb.HermiT.hierarchy.HierarchyPosition;
import org.semanticweb.HermiT.monitor.Timer;

public class CommandLine {
    
    static String breakLines(String str, int lineWidth, int indent) {
        StringBuffer out = new StringBuffer();
        BreakIterator i = BreakIterator.getLineInstance();
        i.setText(str);
        int curPos = 0;
        int curLinePos = indent;
        int next = i.first();
        while (next != BreakIterator.DONE) {
            String curSpan = str.substring(curPos, next);
            if (curLinePos + curSpan.length() > lineWidth) {
                out.append(System.getProperty("line.separator"));
                for (int j = 0; j < indent; ++j) out.append(" ");
                curLinePos = indent;
            }
            out.append(curSpan);
            curLinePos += curSpan.length();
            curPos = next;
            next = i.next();
        }
        return out.toString();
    }

    @SuppressWarnings("serial")
    protected static class UsageException extends java.lang.IllegalArgumentException {
        UsageException(String inMessage) { super(inMessage); }
    }
    
    static class Option {
        static enum Arg { NONE, OPTIONAL, REQUIRED };
        int optChar;
        String longStr;
        String group;
        Arg arg;
        String metavar;
        String help;
        Option(int inChar, String inLong, String inGroup, String inHelp) {
            optChar = inChar;
            longStr = inLong;
            group = inGroup;
            arg = Arg.NONE;
            help = inHelp;
        }
        Option(int inChar, String inLong, String inGroup, boolean argRequired, String inMetavar, String inHelp) {
            optChar = inChar;
            longStr = inLong;
            group = inGroup;
            arg = (argRequired ? Arg.REQUIRED : Arg.OPTIONAL);
            metavar = inMetavar;
            help = inHelp;
        }
        static LongOpt[] createLongOpts(Option[] opts) {
            LongOpt[] out = new LongOpt[opts.length];
            for (int i = 0; i < opts.length; ++i) {
                out[i] = new LongOpt(opts[i].longStr,
                    (opts[i].arg == Arg.NONE     ? LongOpt.NO_ARGUMENT :
                     opts[i].arg == Arg.OPTIONAL ? LongOpt.OPTIONAL_ARGUMENT
                                                 : LongOpt.REQUIRED_ARGUMENT),
                    null, opts[i].optChar);
            }
            return out;
        }
        
        String getLongOptExampleStr() {
            if (longStr == null || longStr.equals("")) return "";
            return new String("--" + longStr + 
                (arg == Arg.NONE        ? "" :
                 arg == Arg.OPTIONAL    ? "[=" + metavar + "]"
                                        : "=" + metavar));
        }
        
        static String formatOptionHelp(Option[] opts) {
            StringBuffer out = new StringBuffer();
            int fieldWidth = 0;
            for (Option o : opts) {
                int curWidth = o.getLongOptExampleStr().length();
                if (curWidth > fieldWidth) fieldWidth = curWidth;
            }
            String curGroup = null;
            for (Option o : opts) {
                if (o.group != curGroup) {
                    curGroup = o.group;
                    out.append(System.getProperty("line.separator"));
                    if (o.group != null) {
                        out.append(curGroup + ":");
                        out.append(System.getProperty("line.separator"));
                    }
                }
                if (o.optChar < 256) {
                    out.append("  -");
                    out.appendCodePoint(o.optChar);
                    if (o.longStr != null && o.longStr != "") {
                        out.append(", ");
                    } else {
                        out.append("  ");
                    }
                } else {
                    out.append("      ");
                }
                int fieldLeft = fieldWidth + 1;
                if (o.longStr != null && o.longStr != "") {
                    String s = o.getLongOptExampleStr();
                    out.append(s);
                    fieldLeft -= s.length();
                }
                for (; fieldLeft > 0; --fieldLeft) out.append(' ');
                out.append(breakLines(o.help, 80, 6 + fieldWidth + 1));
                out.append(System.getProperty("line.separator"));
            }
            return out.toString();
        }
        static String formatOptionsString(Option[] opts) {
            StringBuffer out = new StringBuffer();
            for (Option o : opts) {
                if (o.optChar < 256) {
                    out.appendCodePoint(o.optChar);
                    switch (o.arg) {
                        case REQUIRED:
                            out.append(":");
                            break;
                        case OPTIONAL:
                            out.append("::");
                            break;
                        case NONE:
                            break;
                    }
                }
            }
            return out.toString();
        }
    }
    
    protected static class StatusOutput {
        protected int level;
        public StatusOutput(int inLevel) { level = inLevel; }
        static public final int ALWAYS = 0;
        static public final int STATUS = 1;
        static public final int DETAIL = 2;
        static public final int DEBUG = 3;
        public void log(int inLevel, String message) {
            if (inLevel <= level) System.err.println(message);
        }
    }

    protected interface Action {
        void run(Reasoner hermit, Namespaces namespaces,
                 StatusOutput status, PrintWriter output);
    }
    
    static protected class DumpNamespacesAction implements Action {
        public void run(Reasoner hermit, Namespaces namespaces,
                        StatusOutput status, PrintWriter output) {
            output.println("Namespaces:");
            for (Map.Entry<String, String> e
                    : namespaces.getDeclarations().entrySet()) {
                output.println("\t" + e.getKey() + "\t" + e.getValue());
            }
        }
    }
    
    static protected class DumpClausesAction implements Action {
        final String file;
        public DumpClausesAction(String fileName) {
            file = fileName;
        }
        public void run(Reasoner hermit, Namespaces namespaces,
                        StatusOutput status, PrintWriter output) {
            if (file != null) {
                if (file.equals("-")) {
                    output = new PrintWriter(System.out);
                } else {
                    java.io.FileOutputStream f;
                    try {
                        f = new java.io.FileOutputStream(file);
                    } catch (java.io.FileNotFoundException e) {
                        throw new IllegalArgumentException("unable to open " + file + " for writing");
                    } catch (SecurityException e) {
                        throw new IllegalArgumentException("unable to write to " + file);
                    }
                    output = new PrintWriter(f);
                }
            }
            hermit.outputClauses(output, namespaces);
        }
    }
    static protected class ClassifyAction implements Action {
        final String file;
        public ClassifyAction(String fileName) {
            file = fileName;
        }
        public void run(Reasoner hermit, Namespaces namespaces,
                        StatusOutput status, PrintWriter output) {
            status.log(2, "classifying...");
            hermit.seedSubsumptionCache();
            if (file != null) {
                if (file.equals("-")) {
                    output = new PrintWriter(System.out);
                } else {
                    java.io.FileOutputStream f;
                    try {
                        f = new java.io.FileOutputStream(file);
                    } catch (java.io.FileNotFoundException e) {
                        throw new IllegalArgumentException("unable to open " + file + " for writing");
                    } catch (SecurityException e) {
                        throw new IllegalArgumentException("unable to write to " + file);
                    }
                    output = new PrintWriter(f);
                }
                hermit.printSortedAncestorLists(output);
            }
        }
    }
    
    static protected class SatisfiabilityAction implements Action {
        final String conceptName;
        public SatisfiabilityAction(String c) { conceptName = c; }
        public void run(Reasoner hermit,  Namespaces namespaces,
                        StatusOutput status, PrintWriter output) {
            status.log(2, "Checking satisfiability of '" + conceptName + "'");
            String conceptUri = namespaces.uriFromId(conceptName);
            if (!hermit.isClassNameDefined(conceptUri)) {
                status.log(0, "Warning: class '" + conceptUri +
                                "' was not declared in the ontology.");
            }
            boolean result = hermit.isClassSatisfiable(conceptUri);
            output.println(conceptName + (result ? " is satisfiable."
                                                 : " is not satisfiable."));
        }
    }
    
    static protected class SupersAction implements Action {
        final String conceptName;
        boolean all;
        public SupersAction(String name, boolean getAll) {
            conceptName = name;
            all = getAll;
        }
        public void run(Reasoner hermit,
                        Namespaces namespaces,
                        StatusOutput status,
                        PrintWriter output) {
            status.log(2, "Finding supers of '" + conceptName + "'");
            String conceptUri = namespaces.uriFromId(conceptName);
            if (!hermit.isClassNameDefined(conceptUri)) {
                status.log(0, "Warning: class '" + conceptUri +
                                "' was not declared in the ontology.");
            }
            HierarchyPosition<String> pos =
                hermit.getClassTaxonomyPosition(conceptUri);
            if (all) {
                output.println(
                    "All super-classes of '" + conceptName + "':");
                for (String sup : pos.getAncestors()) {
                    output.println("\t" + namespaces.idFromUri(sup));
                }
            } else {
                output.println(
                    "Direct super-classes of '" + conceptName + "':");
                for (HierarchyPosition<String> sup
                        : pos.getParentPositions()) {
                    for (String name : sup.getEquivalents()) {
                        output.println("\t" + namespaces.idFromUri(name));
                    }
                }
            }
        }
    }
    
    static protected class SubsAction implements Action {
        final String conceptName;
        boolean all;
        public SubsAction(String name, boolean getAll) {
            conceptName = name;
            all = getAll;
        }
        public void run(Reasoner hermit,
                        Namespaces namespaces,
                        StatusOutput status,
                        PrintWriter output) {
            status.log(2, "Finding subs of '" + conceptName + "'");
            String conceptUri = namespaces.uriFromId(conceptName);
            if (!hermit.isClassNameDefined(conceptUri)) {
                status.log(0, "Warning: class '" + conceptUri +
                                "' was not declared in the ontology.");
            }
            HierarchyPosition<String> pos =
                hermit.getClassTaxonomyPosition(conceptUri);
            if (all) {
                output.println(
                    "All sub-classes of '" + conceptName + "':");
                for (String sub : pos.getDescendants()) {
                    output.println("\t" + namespaces.idFromUri(sub));
                }
            } else {
                output.println(
                    "Direct sub-classes of '" + conceptName + "':");
                for (HierarchyPosition<String> sub
                        : pos.getChildPositions()) {
                    for (String name : sub.getEquivalents()) {
                        output.println("\t" + namespaces.idFromUri(name));
                    }
                }
            }
        }
    }

    static protected class EquivalentsAction implements Action {
        final String conceptName;
        public EquivalentsAction(String name) {
            conceptName = name;
        }
        public void run(Reasoner hermit,
                        Namespaces namespaces,
                        StatusOutput status,
                        PrintWriter output) {
            status.log(2, "Finding equivalents of '" + conceptName + "'");
            String conceptUri = namespaces.uriFromId(conceptName);
            if (!hermit.isClassNameDefined(conceptUri)) {
                status.log(0, "Warning: class '" + conceptUri +
                                "' was not declared in the ontology.");
            }
            HierarchyPosition<String> pos =
                hermit.getClassTaxonomyPosition(conceptUri);
            output.println(
                    "Classes equivalent to '" + conceptName + "':");
            for (String equiv : pos.getEquivalents()) {
                output.println("\t" + namespaces.idFromUri(equiv));
            }
        }
    }
    
    
    protected static final int
        kTime=1000, kDumpClauses=1001, kDumpRoleBox=1002, kOwlApi = 1003, kKaon2 = 1004,
        kDirectBlock = 1005, kBlockStrategy = 1006, kBlockCache = 1007, kExpansion = 1008, kBase = 1009,
        kParser = 1010, kClausifyRoleBox = 1011, kDefaultNamespace = 1012, kDumpNamespaces = 1013;
    
    static protected final String versionString = "HermiT version 0.5.0";
    protected static final String usageString = "Usage: hermit [OPTION]... URI...";
    protected static final String[] helpHeader = {
        "Perform reasoning on each OWL ontology URI.",
        "Example: hermit -k http://hermit-reasoner.org/2008/test.owl",
        "    (retrieves and checks consistency of test ontology)",
        "",
        "Both relative and absolute URIs can be used. Relative URIs are resolved with",
        "respect to the current directory (i.e. local file names are valid URIs); this",
        "behavior can be changed with the '--base' option.",
        "",
        "By default, ontologies are simply retrieved and parsed. For more interesting",
        "reasoning, set one of the -c/-k/-s/-S/-e/-U options."
    };
    protected static final String[] helpFooter = {
        "HermiT is a product of Oxford University.",
        "Visit <http://hermit-reasoner.org> for details."
    };
    protected static final String
        kMisc = "Miscellaneous", kActions = "Actions", kParsing = "Parsing and loading",
        kNamespaces = "Namespace expansion and abbreviation",
        kAlgorithm = "Algorithm settings (expert users only!)",
        kInternals = "Internals and debugging (unstable)";
    
    protected static final Option[] options = new Option[] {
        // meta:
        new Option('h', "help",     kMisc,
                    "display this help and exit"),
        new Option('V', "version",  kMisc,
                    "display version information and exit"),
        new Option('v', "verbose",  kMisc, false, "AMOUNT",
                    "increase verbosity by AMOUNT levels (default 1)"),
        new Option('q', "quiet",    kMisc, false, "AMOUNT",
                    "decrease verbosity by AMOUNT levels (default 1)"),
        new Option('o', "output",   kMisc, true, "FILE",
                    "write output to FILE"),
                    
        // actions:
        new Option('l', "load", kActions,
                    "parse and preprocess ontologies (default action)"),
        new Option('c', "classify", kActions, false, "FILE",
                    "classify ontology, optionally writing taxonomy to FILE (use - for standard out)"),
        new Option('k', "consistency", kActions, false, "CLASS",
                    "check satisfiability of CLASS (default owl:Thing)"),
        new Option('d', "direct", kActions,
                    "restrict next subs/supers call to only direct sub/superclasses"),
        new Option('s', "subs", kActions, true, "CLASS",
                    "output classes subsumed by CLASS (or only direct subs if following --direct)"),
        new Option('S', "supers", kActions, true, "CLASS",
                    "output classes subsuming CLASS (or only direct supers if following --direct)"),
        new Option('e', "equivalents", kActions, true, "CLASS",
                    "output classes equivalent to CLASS"),
        new Option('U', "unsatisfiable", kActions,
                    "output unsatisfiable classes (equivalent to --equivalents=owl:Nothing)"),
        new Option(kDumpNamespaces, "print-namespaces", kActions,
                    "output namespace prefixes available for use in identifiers"),

        new Option('N', "no-namespaces", kNamespaces,
                    "do not abbreviate or expand identifiers using namespaces defined in input ontology"),
        new Option('n', "namespace", kNamespaces, true, "NS=URI",
                    "use NS as an abbreviation for URI in identifiers"),
        new Option(kDefaultNamespace, "namespace", kNamespaces, true, "URI",
                    "use URI as the default identifier namespace"),
        
        new Option(kBase, "base", kParsing, true, "BASE",
                    "use BASE as base for ontology URI arguments"),
        new Option(kParser, "parser", kParsing, true, "PARSER",
                    "use PARSER for parsing; supported values are 'owlapi' and 'kaon2'"),
        new Option(kOwlApi, "owlapi", kParsing,
                    "use OWL API parser (default)"),
        new Option(kKaon2, "kaon2", kParsing,
                    "use KAON2 parser"),
                    
        // algorithm tweaks:
        new Option(kDirectBlock, "block-match", kAlgorithm, true, "TYPE",
                    "identify blocked nodes with TYPE blocking; supported values are 'single', 'pairwise', 'pairwise-reflexive', and 'optimal' (default 'optimal')"),
        new Option(kBlockStrategy, "block-strategy", kAlgorithm, true, "TYPE",
                    "search for blockers with TYPE searching; supported values are 'ancestor' and 'anywhere' (default 'anywhere')"),
        new Option(kBlockCache, "block-cache", kAlgorithm, true, "VALUE",
                    "set use of blocking cahce to VALUE; supported values are 'on' and 'off' (default 'on')"),
        new Option(kExpansion, "expansion", kAlgorithm, true, "TYPE",
                    "use TYPE strategy for existential expansion; supported values are 'creation', 'el', and 'reuse' (default 'creation')"),
        new Option(kClausifyRoleBox, "clausify-rolebox", kAlgorithm,
                    "add clauses to realize transitive edges (experimental)"),
                    
        // internals:
        new Option(kDumpClauses, "dump-clauses", kInternals, false, "FILE",
                    "output DL-clauses to FILE (default stdout)"),
    };
    
    public static void main(String[] argv) {
        try {
            int verbosity = 1;
            boolean ignoreOntologyNamespaces = false;
            Map<String, String> newNamespaces = new HashMap<String, String>();
            PrintWriter output = new PrintWriter(System.out);
            Collection<Action> actions = new LinkedList<Action>();
            URI base;
            Reasoner.Configuration config = new Reasoner.Configuration();
            config.subsumptionCacheStrategyType =
                Reasoner.SubsumptionCacheStrategyType.ON_REQUEST;
            boolean doAll = true;
            try {
                base = new URI("file", System.getProperty("user.dir") + "/", null);
            } catch (java.net.URISyntaxException e) {
                throw new RuntimeException("unable to create default URI base");
            }
            Collection<URI> ontologies = new LinkedList<URI>();
            boolean didSomething = false;
            {
                Getopt g = new Getopt("hermit", argv,
                                      Option.formatOptionsString(options),
                                      Option.createLongOpts(options));
                g.setOpterr(false);
                int opt;
                while ((opt = g.getopt()) != -1) {
                    switch (opt) {
                    // meta:
                    case 'h': {
                        System.out.println(usageString);
                        for (String s : helpHeader) System.out.println(s);
                        System.out.println(Option.formatOptionHelp(options));
                        for (String s : helpFooter) System.out.println(s);
                        System.exit(0);
                        didSomething = true;
                    } break;
                    case 'V': {
                        System.out.println(versionString);
                        System.exit(0);
                        didSomething = true;
                    } break;
                    case 'v': {
                        String arg = g.getOptarg();
                        if (arg == null) {
                            verbosity += 1;
                        } else try {
                            verbosity += Integer.parseInt(arg, 10);
                        } catch (NumberFormatException e) {
                            throw new UsageException("argument to --verbose must be a number");
                        }
                    } break;
                    case 'q': {
                        String arg = g.getOptarg();
                        if (arg == null) {
                            verbosity -= 1;
                        } else try {
                            verbosity -= Integer.parseInt(arg, 10);
                        } catch (NumberFormatException e) {
                            throw new UsageException("argument to --verbose must be a number");
                        }
                    } break;
                    case 'o': {
                        String arg = g.getOptarg();
                        if (arg == null) throw new UsageException("--output requires an argument");
                        if (arg.equals("-")) output = new PrintWriter(System.out);
                        else {
                            java.io.FileOutputStream f;
                            try {
                                f = new java.io.FileOutputStream(arg);
                            } catch (java.io.FileNotFoundException e) {
                                throw new IllegalArgumentException("unable to open " + arg + " for writing");
                            } catch (SecurityException e) {
                                throw new IllegalArgumentException("unable to write to " + arg);
                            }
                            output = new PrintWriter(f);
                        }
                    } break;
                    // actions:
                    case 'l': {
                        // load is a no-op; loading happens no matter what the user asks
                    } break;
                    case 'c': {
                        actions.add(new ClassifyAction(g.getOptarg()));
                    } break;
                    case 'k': {
                        String arg = g.getOptarg();
                        if (arg == null) {
                            arg = "<http://www.w3.org/2002/07/owl#Thing>";
                        }
                        actions.add(new SatisfiabilityAction(arg));
                    } break;
                    case 'd': {
                        doAll = false;
                    } break;
                    case 's': {
                        String arg = g.getOptarg();
                        actions.add(new SubsAction(arg, doAll));
                        doAll = true;
                    } break;
                    case 'S': {
                        String arg = g.getOptarg();
                        actions.add(new SupersAction(arg, doAll));
                        doAll = true;
                    } break;
                    case 'e': {
                        String arg = g.getOptarg();
                        actions.add(new EquivalentsAction(arg));
                    } break;
                    case 'U': {
                        actions.add(new EquivalentsAction("<http://www.w3.org/2002/07/owl#Nothing>"));
                    } break;
                    case kDumpNamespaces: {
                        actions.add(new DumpNamespacesAction());
                    } break;
                    case 'N': {
                        ignoreOntologyNamespaces = true;
                    } break;
                    case 'n': {
                        String arg = g.getOptarg();
                        int eqIndex = arg.indexOf('=');
                        if (eqIndex == -1) {
                            throw new IllegalArgumentException("the namespace definition '" + arg + "' is not of the form NS=URI.");
                        }
                        newNamespaces.put(arg.substring(0, eqIndex), arg.substring(eqIndex + 1));
                    } break;
                    case kDefaultNamespace: {
                        String arg = g.getOptarg();
                        newNamespaces.put("", arg);
                    } break;
                    case kBase : {
                        String arg = g.getOptarg();
                        try {
                            base = new URI(arg);
                        } catch (java.net.URISyntaxException e) {
                            throw new IllegalArgumentException("'" + arg + "' is not a valid base URI.");
                        }
                    } break;
                    case kParser : {
                        String arg = g.getOptarg();
                        if (arg.toLowerCase().equals("owlapi")) {
                            config.parserType = Reasoner.ParserType.OWLAPI;
                        } else if (arg.toLowerCase().equals("kaon2")) {
                            config.parserType = Reasoner.ParserType.KAON2;
                        } else throw new UsageException("unknown parser type '" + arg + "'; supported values are 'owlapi' and 'kaon2'");
                    } break;
                    case kOwlApi : {
                        config.parserType = Reasoner.ParserType.OWLAPI;
                    } break;
                    case kKaon2 : {
                        config.parserType = Reasoner.ParserType.KAON2;
                    } break;
                    case kDirectBlock : {
                        String arg = g.getOptarg();
                        if (arg.toLowerCase().equals("pairwise")) {
                            config.directBlockingType = Reasoner.DirectBlockingType.PAIR_WISE;
                        } else if (arg.toLowerCase().equals("single")) {
                            config.directBlockingType = Reasoner.DirectBlockingType.SINGLE;
                        } else if (arg.toLowerCase().equals("optimal")) {
                            config.directBlockingType = Reasoner.DirectBlockingType.OPTIMAL;
                        } else throw new UsageException("unknown direct blocking type '" + arg + "'; supported values are 'pairwise', 'single', and 'optimal'");
                    } break;
                    case kBlockStrategy : {
                        String arg = g.getOptarg();
                        if (arg.toLowerCase().equals("anywhere")) {
                            config.blockingStrategyType = Reasoner.BlockingStrategyType.ANYWHERE;
                        } else if (arg.toLowerCase().equals("ancestor")) {
                            config.blockingStrategyType = Reasoner.BlockingStrategyType.ANCESTOR;
                        } else throw new UsageException("unknown blocking strategy type '" + arg + "'; supported values are 'ancestor' and 'anywhere'");
                    } break;
                    case kBlockCache : {
                        String arg = g.getOptarg();
                        if (arg.toLowerCase().equals("on")) {
                            config.blockingSignatureCacheType = Reasoner.BlockingSignatureCacheType.CACHED;
                        } else if (arg.toLowerCase().equals("off")) {
                            config.blockingSignatureCacheType = Reasoner.BlockingSignatureCacheType.NOT_CACHED;
                        } else throw new UsageException("unknown blocking cache type '" + arg + "'; supported values are 'on' and 'off'");
                    } break;
                    case kExpansion : {
                        String arg = g.getOptarg();
                        if (arg.toLowerCase().equals("creation")) {
                            config.existentialStrategyType = Reasoner.ExistentialStrategyType.CREATION_ORDER;
                        } else if (arg.toLowerCase().equals("el")) {
                            config.existentialStrategyType = Reasoner.ExistentialStrategyType.EL;
                        } else if (arg.toLowerCase().equals("reuse")) {
                            config.existentialStrategyType = Reasoner.ExistentialStrategyType.INDIVIDUAL_REUSE;
                        } else throw new UsageException("unknown existential strategy type '" + arg + "'; supported values are 'creation', 'el', and 'reuse'");
                    } break;
                    case kClausifyRoleBox : {
                        config.clausifyTransitivity = true;
                        config.checkClauses = false;
                        config.existentialStrategyType = Reasoner.ExistentialStrategyType.DEPTH_FIRST;
                    } break;
                    case kDumpClauses : {
                        actions.add(new DumpClausesAction(g.getOptarg()));
                    } break;
                    default : {
                        if (g.getOptopt() != 0) {
                            throw new UsageException("invalid option -- " + (char) g.getOptopt());
                        }
                        throw new UsageException("invalid option");
                    }
                } // end option switch
                } // end loop over options
                for (int i = g.getOptind(); i < argv.length; ++i) {
                    try {
                        ontologies.add(base.resolve(argv[i]));
                    } catch (IllegalArgumentException e) {
                        throw new UsageException(argv[i] + " is not a valid ontology name");
                    }
                }
            } // done processing arguments
            StatusOutput status = new StatusOutput(verbosity);
            if (verbosity > 3) config.monitor = new Timer(new PrintWriter(System.err));
            for (URI ont : ontologies) {
                didSomething = true;
                status.log(2, "Processing " + ont.toString());
                status.log(2, String.valueOf(actions.size()) + " actions");
                try {
                    long startTime = System.currentTimeMillis();
                    Reasoner hermit = new Reasoner(ont, config);
                    long loadTime = System.currentTimeMillis() - startTime;
                    status.log(2, "Loaded in " + String.valueOf(loadTime) + " msec.");
                    Namespaces namespaces = 
                        (ignoreOntologyNamespaces ? new Namespaces(newNamespaces)
                                                  : new Namespaces(newNamespaces,
                                                            hermit.getNamespaces()));
                    for (Action action : actions) {
                        status.log(2, "Doing action...");
                        startTime = System.currentTimeMillis();
                        action.run(hermit, hermit.getNamespaces(), status, output);
                        long actionTime = System.currentTimeMillis() - startTime;
                        status.log(2, "...action completed in " + String.valueOf(actionTime) + " msec.");
                        output.flush(); 
                    }
                } catch (Clausifier.LoadingException e) {
                    // FIXME this whole thing needs real exception processing
                    System.err.println("Loading failed: " + e.getMessage());
                } catch (org.semanticweb.owl.model.OWLException e) {
                    // FIXME this whole thing needs real exception processing
                    System.err.println("It all went pear-shaped: " + e.getMessage());
                }
            }
            if (!didSomething) throw new UsageException ("No ontologies given.");
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println(usageString);
            System.err.println("Try 'hermit --help' for more information.");
        }
    }
}
