package org.semanticweb.HermiT.cli;

import java.io.PrintWriter;

import org.semanticweb.HermiT.Reasoner;

interface Action {
    void run(Reasoner hermit,StatusOutput status,PrintWriter output,boolean ignoreOntologyPrefixes);
}
