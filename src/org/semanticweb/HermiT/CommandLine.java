package org.semanticweb.HermiT;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import java.net.URI;

public class CommandLine {	
	static protected final String m_usage = "Give some options and some arguments, retard.";
	static protected final String m_version = "HermiT version 0.5.0";
	
	protected static class StatusOutput {
		protected int m_level;
		public StatusOutput(int level) { m_level = level; }
		static public final int ALWAYS = 0;
		static public final int STATUS = 1;
		static public final int DETAIL = 2;
		static public final int DEBUG = 3;
		public void log(int level, String message) {
			if (level <= m_level) System.err.println(message);
		}
	}

	protected interface Action {
		void run(HermiT hermit, StatusOutput status, PrintWriter output);
	}
	static protected class ClassifyAction implements Action {
		public void run(HermiT hermit, StatusOutput status, PrintWriter output) {
			status.log(2, "classifying...");
			hermit.buildSubsumptionCache();
			if (output != null) {
				hermit.printFlattenedHierarchy(output, hermit.getSubsumptionHierarchy());
			}
		}
	}
	static protected class SatisfiabilityAction implements Action {
		final String conceptName;
		public SatisfiabilityAction(String c) { conceptName = c; }
		public void run(HermiT hermit, StatusOutput status, PrintWriter output) {
			boolean result = hermit.isSatisfiable(conceptName);
			if (result) {
				output.println(conceptName + (result ? " is satisfiable."
													 : " is not satisfiable."));
			}
		}
	}
	static protected class SupersAction implements Action {
		final String conceptName;
		boolean all;
		public SupersAction(String name, boolean getAll) {
			conceptName = name;
			all = getAll;
		}
		public void run(HermiT hermit, StatusOutput status, PrintWriter output) {
			throw new RuntimeException("public classification hierarchy interface not yet implemented");
		}
	}
	static protected class SubsAction implements Action {
		final String conceptName;
		boolean all;
		public SubsAction(String name, boolean getAll) {
			conceptName = name;
			all = getAll;
		}
		public void run(HermiT hermit, StatusOutput status, PrintWriter output) {
			throw new RuntimeException("public classification hierarchy interface not yet implemented");
		}
	}

	static protected class EquivalentsAction implements Action {
		final String conceptName;
		public EquivalentsAction(String name) {
			conceptName = name;
		}
		public void run(HermiT hermit, StatusOutput status, PrintWriter output) {
			throw new RuntimeException("public classification hierarchy interface not yet implemented");
		}
	}
	
	protected static class UsageException extends java.lang.IllegalArgumentException {
		UsageException(String inMessage) { super(inMessage); }
	}
	
	protected static final int
		kTime=1000, kDumpClauses=1001, kDumpRoleBox=1002, kOwlApi = 1003, kKaon2 = 1004,
		kDirectBlock = 1005, kBlockStrategy = 1006, kBlockCache = 1007, kExpansion = 1008, kBase = 1009,
		kParser = 1010;
	
	protected static final LongOpt[] m_options = new LongOpt[] {
		// meta:
		new LongOpt("help",				LongOpt.NO_ARGUMENT,		null,	'h'),
		new LongOpt("usage",			LongOpt.NO_ARGUMENT,		null,	'h'),
		new LongOpt("version",			LongOpt.NO_ARGUMENT,		null,	'V'),
		new LongOpt("verbose",			LongOpt.OPTIONAL_ARGUMENT,	null,	'v'),
		new LongOpt("quiet",			LongOpt.OPTIONAL_ARGUMENT,	null,	'q'),
		new LongOpt("output",			LongOpt.REQUIRED_ARGUMENT,	null,	'o'),
		// actions:
		new LongOpt("load", 			LongOpt.OPTIONAL_ARGUMENT,	null,	'l'),
		new LongOpt("classify", 		LongOpt.OPTIONAL_ARGUMENT,	null,	'c'),
		new LongOpt("consistency",		LongOpt.OPTIONAL_ARGUMENT,	null,	'k'),
		new LongOpt("all",				LongOpt.REQUIRED_ARGUMENT,	null,	'a'),
		new LongOpt("subs",				LongOpt.REQUIRED_ARGUMENT,	null,	's'),
		new LongOpt("supers",			LongOpt.REQUIRED_ARGUMENT,	null,	'S'),
		new LongOpt("equivalents",		LongOpt.REQUIRED_ARGUMENT,	null,	'e'),
		new LongOpt("unsatisfiable",	LongOpt.OPTIONAL_ARGUMENT,	null,	'U'),
		// debugging and benchmarking
		// new LongOpt("time",				LongOpt.OPTIONAL_ARGUMENT,	null,	kTime),
		// new LongOpt("dump-clauses",		LongOpt.OPTIONAL_ARGUMENT,	null,	kDumpClauses),
		// new LongOpt("dump-rbox",		LongOpt.OPTIONAL_ARGUMENT,	null,	kDumpRoleBox),
		// parsing and loading:
		new LongOpt("base",				LongOpt.REQUIRED_ARGUMENT,	null,	kBase),
		new LongOpt("parser",			LongOpt.REQUIRED_ARGUMENT,	null,	kParser),
		new LongOpt("owlapi",			LongOpt.NO_ARGUMENT,		null,	kOwlApi),
		new LongOpt("kaon2",			LongOpt.NO_ARGUMENT,		null,	kKaon2),
		// algorithm tweaks:
		new LongOpt("block-match",		LongOpt.REQUIRED_ARGUMENT,	null,	kDirectBlock),
		new LongOpt("block-strategy",	LongOpt.REQUIRED_ARGUMENT,	null,	kBlockStrategy),
		new LongOpt("block-cache",		LongOpt.REQUIRED_ARGUMENT,	null,	kBlockCache),
		new LongOpt("expansion",		LongOpt.REQUIRED_ARGUMENT,	null,	kExpansion),
	};
	
	protected static final String optString = "hVv::q::o:l::c::k::a:s:S:e:U::";

	public static void main(String[] argv) {
		int verbosity = 1;
		PrintWriter output = new PrintWriter(System.out);
		Collection<Action> actions = new LinkedList<Action>();
		URI base;
		HermiT.Configuration config = new HermiT.Configuration();
		boolean doAll = false;
		try {
			base = new URI("file:", "", "");
		} catch (java.net.URISyntaxException e) {
			throw new RuntimeException("unable to create default URI base");
		}
		Collection<URI> ontologies = new LinkedList<URI>();
		boolean didSomething = false;
		{
			Getopt g = new Getopt("hermit", argv, optString, m_options);
			int opt;
			while ((opt = g.getopt()) != -1) {
				switch (opt) {
				// meta:
				case 'h': {
					System.out.println(m_usage);
					didSomething = true;
				} break;
				case 'V': {
					System.out.println(m_version);
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
					if (arg == "-") output = new PrintWriter(System.out);
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
					actions.add(new ClassifyAction());
				} break;
				case 'k': {
					String arg = g.getOptarg();
					if (arg == null) {
						arg = "owl:Thing";
					}
					actions.add(new SatisfiabilityAction(arg));
				} break;
				case 'a': {
					doAll = true;
				} break;
				case 's': {
					String arg = g.getOptarg();
					actions.add(new SubsAction(arg, doAll));
				} break;
				case 'S': {
					String arg = g.getOptarg();
					actions.add(new SupersAction(arg, doAll));
				} break;
				case 'e': {
					String arg = g.getOptarg();
					actions.add(new EquivalentsAction(arg));
				} break;
				case 'U': {
					actions.add(new EquivalentsAction("owl:Nothing"));
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
					if (arg.toLowerCase() == "owlapi") {
						config.parserType = HermiT.ParserType.OWLAPI;
					} else if (arg.toLowerCase() == "kaon2") {
						config.parserType = HermiT.ParserType.KAON2;
					} else throw new UsageException("unknown parser type '" + arg + "'; supported values are 'owlapi' and 'kaon2'");
				} break;
				case kOwlApi : {
					config.parserType = HermiT.ParserType.OWLAPI;
				} break;
				case kKaon2 : {
					config.parserType = HermiT.ParserType.KAON2;
				} break;
				case kDirectBlock : {
					String arg = g.getOptarg();
					if (arg.toLowerCase() == "pairwise") {
						config.directBlockingType = HermiT.DirectBlockingType.PAIR_WISE;
					} else if (arg.toLowerCase() == "single") {
						config.directBlockingType = HermiT.DirectBlockingType.SINGLE;
					} else if (arg.toLowerCase() == "optimal") {
						config.directBlockingType = HermiT.DirectBlockingType.OPTIMAL;
					} else throw new UsageException("unknown direct blocking type '" + arg + "'; supported values are 'pairwise', 'single', and 'optimal'");
				} break;
				case kBlockStrategy : {
					String arg = g.getOptarg();
					if (arg.toLowerCase() == "anywhere") {
						config.blockingStrategyType = HermiT.BlockingStrategyType.ANYWHERE;
					} else if (arg.toLowerCase() == "ancestor") {
						config.blockingStrategyType = HermiT.BlockingStrategyType.ANCESTOR;
					} else throw new UsageException("unknown blocking strategy type '" + arg + "'; supported values are 'ancestor' and 'anywhere'");
				} break;
				case kBlockCache : {
					String arg = g.getOptarg();
					if (arg.toLowerCase() == "on") {
						config.blockingSignatureCacheType = HermiT.BlockingSignatureCacheType.CACHED;
					} else if (arg.toLowerCase() == "off") {
						config.blockingSignatureCacheType = HermiT.BlockingSignatureCacheType.NOT_CACHED;
					} else throw new UsageException("unknown blocking cache type '" + arg + "'; supported values are 'on' and 'off'");
				} break;
				case kExpansion : {
					String arg = g.getOptarg();
					if (arg.toLowerCase() == "creation") {
						config.existentialStrategyType = HermiT.ExistentialStrategyType.CREATION_ORDER;
					} else if (arg.toLowerCase() == "el") {
						config.existentialStrategyType = HermiT.ExistentialStrategyType.EL;
					} else if (arg.toLowerCase() == "reuse") {
						config.existentialStrategyType = HermiT.ExistentialStrategyType.INDIVIDUAL_REUSE;
					} else throw new UsageException("unknown existential strategy type '" + arg + "'; supported values are 'creation', 'el', and 'reuse'");
				} break;
				default : throw new UsageException("unrecognized option '" + opt + "'");
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
		for (URI ont : ontologies) {
			didSomething = true;
			status.log(2, "Processing " + ont.toString());
			try {
				HermiT hermit = new HermiT(ont, config);
				for (Action action : actions) {
					action.run(hermit, status, output);
				}
			} catch (Exception e) {
				// FIXME this whole thing needs real exception processing
				// System.err.println("It all went pear-shaped: " + e.message());
			}
		}
	}
}
