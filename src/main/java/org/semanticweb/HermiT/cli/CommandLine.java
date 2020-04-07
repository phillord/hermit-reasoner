/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.cli;

import static org.semanticweb.HermiT.cli.constants.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.monitor.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

/**
 * Command line interface to HermiT.
 */
public class CommandLine {

    /**
     * @param argv
     *            args
     * @throws ParseException
     *             if arguments are invalid
     * @throws OWLException
     *             if a general error happens
     */
    public static void main(String[] argv) throws ParseException, OWLException {
        try {
            int verbosity = 1;
            boolean ignoreOntologyPrefixes = false;
            PrintWriter output = new PrintWriter(System.out);
            String defaultPrefix = null;
            Map<String, String> prefixMappings = new HashMap<>();
            String resultsFileLocation = null;
            boolean classifyClasses = false;
            boolean classifyOPs = false;
            boolean classifyDPs = false;
            boolean prettyPrint = false;
            Collection<Action> actions = new LinkedList<>();
            URI base;
            IRI conclusionIRI = null;
            Configuration config = new Configuration();
            boolean doAll = true;
            try {
                base = new URI("file", System.getProperty("user.dir") + "/", null);
            } catch (java.net.URISyntaxException e) {
                throw new RuntimeException("unable to create default IRI base", e);
            }
            Collection<IRI> ontologies = new LinkedList<>();
            boolean didSomething = false;
            Options g = Option.createLongOpts(Option.options);
            CommandLineParser parser = new DefaultParser();
            org.apache.commons.cli.CommandLine cmd = parser.parse(g, argv);
            // meta:
            if (cmd.hasOption(Option.HELP)) {
                System.out.println(usageString);
                for (String s : helpHeader)
                    System.out.println(s);
                System.out.println(Option.formatOptionHelp(Option.options));
                for (String s : footer)
                    System.out.println(s);
                System.exit(0);
                didSomething = true;
            }
            if (cmd.hasOption(Option.VERSION)) {
                System.out.println(versionString);
                for (String s : footer)
                    System.out.println(s);
                System.exit(0);
                didSomething = true;
            }
            if (cmd.hasOption(Option.VERBOSE)) {
                String arg = cmd.getOptionValue(Option.VERBOSE);
                if (arg == null) {
                    verbosity += 1;
                } else
                    try {
                        verbosity += Integer.parseInt(arg, 10);
                    } catch (NumberFormatException e) {
                        throw new UsageException("argument to --verbose must be a number", e);
                    }
            }
            if (cmd.hasOption(Option.QUIET)) {
                String arg = cmd.getOptionValue(Option.QUIET);
                if (arg == null) {
                    verbosity -= 1;
                } else
                    try {
                        verbosity -= Integer.parseInt(arg, 10);
                    } catch (NumberFormatException e) {
                        throw new UsageException("argument to --quiet must be a number", e);
                    }
            }
            if (cmd.hasOption(Option.OUTPUT)) {
                String arg = cmd.getOptionValue(Option.OUTPUT);
                if (arg == null)
                    throw new UsageException("--output requires an argument");
                if (arg.equals("-"))
                    output = new PrintWriter(System.out);
                else {
                    try {
                        File file = new File(arg);
                        if (!file.exists())
                            file.createNewFile();
                        file = file.getAbsoluteFile();
                        output = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)), true);
                        resultsFileLocation = file.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        throw new IllegalArgumentException("unable to open " + arg + " for writing");
                    } catch (SecurityException e) {
                        throw new IllegalArgumentException("unable to write to " + arg);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("unable to write to " + arg + ": " + e.getMessage());
                    }
                }
            }
            if (cmd.hasOption(Option.PREMISE)) {
                String arg = cmd.getOptionValue(Option.PREMISE);
                if (arg == null)
                    throw new UsageException("--premise requires a IRI as argument");
                else {
                    ontologies.add(IRI.create(arg));
                }
            }
            if (cmd.hasOption(Option.CONCLUSION)) {
                String arg = cmd.getOptionValue(Option.CONCLUSION);
                if (arg == null)
                    throw new UsageException("--conclusion requires a IRI as argument");
                else {
                    conclusionIRI = IRI.create(arg);
                }
            }
            // actions:
            if (cmd.hasOption(Option.LOAD)) {
                // load is a no-op; loading happens no matter what the user asks
            }
            if (cmd.hasOption(Option.CLASSIFY)) {
                classifyClasses = true;
            }
            if (cmd.hasOption(Option.CLASSIFY_OPS)) {
                classifyOPs = true;
            }
            if (cmd.hasOption(Option.CLASSIFY_DPS)) {
                classifyDPs = true;
            }
            if (cmd.hasOption(Option.PRETTY_PRINT)) {
                prettyPrint = true;
            }
            if (cmd.hasOption(Option.CONSISTENCY)) {
                String arg = cmd.getOptionValue(Option.CONSISTENCY);
                if (arg == null) {
                    arg = "http://www.w3.org/2002/07/owl#Thing";
                }
                actions.add(new SatisfiabilityAction(arg));
            }
            if (cmd.hasOption(Option.DIRECT)) {
                doAll = false;
            }
            if (cmd.hasOption(Option.SUBS)) {
                String arg = cmd.getOptionValue(Option.SUBS);
                actions.add(new SubsAction(arg, doAll));
                doAll = true;
            }
            if (cmd.hasOption(Option.SUPERS)) {
                String arg = cmd.getOptionValue(Option.SUPERS);
                actions.add(new SupersAction(arg, doAll));
                doAll = true;
            }
            if (cmd.hasOption(Option.EQUIVALENTS)) {
                String arg = cmd.getOptionValue(Option.EQUIVALENTS);
                actions.add(new EquivalentsAction(arg));
            }
            if (cmd.hasOption(Option.UNSATISFIABLE)) {
                actions.add(new EquivalentsAction("http://www.w3.org/2002/07/owl#Nothing"));
            }
            if (cmd.hasOption(Option.CHECK_ENTAILMENT)) {
                if (conclusionIRI != null)
                    actions.add(new EntailsAction(config, conclusionIRI));
            }
            if (cmd.hasOption(Option.DUMP_PREFIXES)) {
                actions.add(new DumpPrefixesAction());
            }
            if (cmd.hasOption(Option.NO_PREFIXES)) {
                ignoreOntologyPrefixes = true;
            }
            if (cmd.hasOption(Option.PREFIX)) {
                String arg = cmd.getOptionValue(Option.PREFIX);
                int eqIndex = arg.indexOf('=');
                if (eqIndex == -1) {
                    throw new IllegalArgumentException(
                            "the prefix declaration '" + arg + "' is not of the form PN=IRI.");
                }
                prefixMappings.put(arg.substring(0, eqIndex), arg.substring(eqIndex + 1));
            }
            if (cmd.hasOption(Option.DEFAULT_PREFIXES)) {
                String arg = cmd.getOptionValue(Option.DEFAULT_PREFIXES);
                defaultPrefix = arg;
            }
            if (cmd.hasOption(Option.BASE)) {
                String arg = cmd.getOptionValue(Option.BASE);
                try {
                    base = new URI(arg);
                } catch (java.net.URISyntaxException e) {
                    throw new IllegalArgumentException("'" + arg + "' is not a valid base URI.", e);
                }
            }

            if (cmd.hasOption(Option.DIRECT_BLOCK)) {
                String arg = cmd.getOptionValue(Option.DIRECT_BLOCK);
                if (arg.toLowerCase().equals("pairwise")) {
                    config.directBlockingType = Configuration.DirectBlockingType.PAIR_WISE;
                } else if (arg.toLowerCase().equals("single")) {
                    config.directBlockingType = Configuration.DirectBlockingType.SINGLE;
                } else if (arg.toLowerCase().equals("optimal")) {
                    config.directBlockingType = Configuration.DirectBlockingType.OPTIMAL;
                } else
                    throw new UsageException("unknown direct blocking type '" + arg
                            + "'; supported values are 'pairwise', 'single', and 'optimal'");
            }
            if (cmd.hasOption(Option.BLOCK_STRATEGY)) {
                String arg = cmd.getOptionValue(Option.BLOCK_STRATEGY);
                if (arg.toLowerCase().equals("anywhere")) {
                    config.blockingStrategyType = Configuration.BlockingStrategyType.ANYWHERE;
                } else if (arg.toLowerCase().equals("ancestor")) {
                    config.blockingStrategyType = Configuration.BlockingStrategyType.ANCESTOR;
                } else if (arg.toLowerCase().equals("core")) {
                    config.blockingStrategyType = Configuration.BlockingStrategyType.SIMPLE_CORE;
                } else if (arg.toLowerCase().equals("optimal")) {
                    config.blockingStrategyType = Configuration.BlockingStrategyType.OPTIMAL;
                } else
                    throw new UsageException("unknown blocking strategy type '" + arg
                            + "'; supported values are 'ancestor' and 'anywhere'");
            }
            if (cmd.hasOption(Option.BLOCK_CACHE)) {
                config.blockingSignatureCacheType = Configuration.BlockingSignatureCacheType.CACHED;
            }
            if (cmd.hasOption(Option.EXPANSION)) {
                String arg = cmd.getOptionValue(Option.EXPANSION);
                if (arg.toLowerCase().equals("creation")) {
                    config.existentialStrategyType = Configuration.ExistentialStrategyType.CREATION_ORDER;
                } else if (arg.toLowerCase().equals("el")) {
                    config.existentialStrategyType = Configuration.ExistentialStrategyType.EL;
                } else if (arg.toLowerCase().equals("reuse")) {
                    config.existentialStrategyType = Configuration.ExistentialStrategyType.INDIVIDUAL_REUSE;
                } else
                    throw new UsageException("unknown existential strategy type '" + arg
                            + "'; supported values are 'creation', 'el', and 'reuse'");
            }
            if (cmd.hasOption(Option.IGNORE_UNSUPPORTED_DATATYPES)) {
                config.ignoreUnsupportedDatatypes = true;
            }
            if (cmd.hasOption(Option.NO_INCONSISTENT_EXCEPTION)) {
                config.throwInconsistentOntologyException = false;
            }
            if (cmd.hasOption(Option.DUMP_CLAUSES)) {
                actions.add(new DumpClausesAction(cmd.getOptionValue(Option.DUMP_CLAUSES)));
            }
            for (String i : cmd.getArgs()) {
                try {
                    ontologies.add(IRI.create(base.resolve(i)));
                } catch (IllegalArgumentException e) {
                    throw new UsageException(i + " is not a valid ontology name", e);
                }
            }
            // done processing arguments
            StatusOutput status = new StatusOutput(verbosity);
            if (verbosity > 3)
                config.monitor = new Timer(System.err);
            if (classifyClasses || classifyOPs || classifyDPs)
                actions.add(new ClassifyAction(classifyClasses, classifyOPs, classifyDPs, prettyPrint,
                        resultsFileLocation));
            for (IRI ont : ontologies) {
                didSomething = true;
                status.log(2, "Processing " + ont.toString());
                status.log(2, String.valueOf(actions.size()) + " actions");
                try {
                    long startTime = System.currentTimeMillis();
                    OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
                    if (ont.isAbsolute()) {
                        URI uri = URI.create(ont.getNamespace());
                        String scheme = uri.getScheme();
                        if (scheme != null && scheme.equalsIgnoreCase("file")) {
                            File file = new File(URI.create(ont.getNamespace()));
                            if (file.isDirectory()) {
                                OWLOntologyIRIMapper mapper = new AutoIRIMapper(file, false);
                                ontologyManager.getIRIMappers().add(mapper);
                            }
                        }
                    }
                    OWLOntology ontology = ontologyManager.loadOntology(ont);
                    // if (!ignoreOntologyPrefixes) {
                    // SimpleRenderer renderer=new SimpleRenderer();
                    // renderer.setPrefixesFromOntologyFormat(ontology,
                    // ontologyManager, true);
                    // ToStringRenderer.getInstance().setRenderer(renderer);
                    // }
                    long parseTime = System.currentTimeMillis() - startTime;
                    status.log(2, "Ontology parsed in " + String.valueOf(parseTime) + " msec.");
                    startTime = System.currentTimeMillis();
                    Reasoner hermit = new Reasoner(config, ontology);
                    Prefixes prefixes = hermit.getPrefixes();
                    if (defaultPrefix != null) {
                        try {
                            prefixes.declareDefaultPrefix(defaultPrefix);
                        } catch (IllegalArgumentException e) {
                            status.log(2, "Default prefix " + defaultPrefix
                                    + " could not be registered because there is already a registered default prefix. ");
                        }
                    }
                    for (String prefixName : prefixMappings.keySet()) {
                        try {
                            prefixes.declarePrefix(prefixName, prefixMappings.get(prefixName));
                        } catch (IllegalArgumentException e) {
                            status.log(2,
                                    "Prefixname " + prefixName + " could not be set to "
                                            + prefixMappings.get(prefixName)
                                            + " because there is already a registered prefix name for the IRI. ");
                        }
                    }
                    long loadTime = System.currentTimeMillis() - startTime;
                    status.log(2, "Reasoner created in " + String.valueOf(loadTime) + " msec.");
                    for (Action action : actions) {
                        status.log(2, "Doing action...");
                        startTime = System.currentTimeMillis();
                        action.run(hermit, status, output, ignoreOntologyPrefixes);
                        long actionTime = System.currentTimeMillis() - startTime;
                        status.log(2, "...action completed in " + String.valueOf(actionTime) + " msec.");
                    }
                } catch (org.semanticweb.owlapi.model.OWLException e) {
                    System.err.println("It all went pear-shaped: " + e.getMessage());
                    throw e;
                }
            }
            if (!didSomething)
                throw new UsageException("No ontologies given.");
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println(usageString);
            System.err.println("Try 'hermit --help' for more information.");
        }
    }
}
