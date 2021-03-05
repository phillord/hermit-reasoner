package org.semanticweb.HermiT.cli;

import java.io.PrintWriter;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.Reasoner;

class DumpClausesAction implements Action {
    final String file;

    public DumpClausesAction(String fileName) {
        file=fileName;
    }
    @Override
    public void run(Reasoner hermit,StatusOutput status,PrintWriter out,boolean ignoreOntologyPrefixes) {
        PrintWriter output = selectFile(out);
        if (ignoreOntologyPrefixes)
            output.println(hermit.getDLOntology().toString(new Prefixes()));
        else
            output.println(hermit.getDLOntology().toString(hermit.getPrefixes()));
        output.flush();
    }

    protected PrintWriter selectFile(PrintWriter output) {
        if (file != null) {
            if (file.equals("-")) {
                return new PrintWriter(System.out);
            } 
            try {
                return new PrintWriter(new java.io.FileOutputStream(file));
            } catch (java.io.FileNotFoundException e) {
                throw new IllegalArgumentException("unable to open " + file + " for writing.", e);
            } catch (SecurityException e) {
                throw new IllegalArgumentException("unable to write to " + file, e);
            }
        }
        return output;
    }
}