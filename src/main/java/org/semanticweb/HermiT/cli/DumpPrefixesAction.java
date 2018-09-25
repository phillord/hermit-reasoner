package org.semanticweb.HermiT.cli;

import java.io.PrintWriter;
import java.util.Map;

import org.semanticweb.HermiT.Reasoner;

class DumpPrefixesAction implements Action {
    @Override
    public void run(Reasoner hermit, StatusOutput status, PrintWriter output, boolean ignoreOntologyPrefixes) {
        output.println("Prefixes:");
        for (Map.Entry<String, String> e : hermit.getPrefixes().getPrefixIRIsByPrefixName().entrySet()) {
            output.println("\t" + e.getKey() + "\t" + e.getValue());
        }
        output.flush();
    }
}