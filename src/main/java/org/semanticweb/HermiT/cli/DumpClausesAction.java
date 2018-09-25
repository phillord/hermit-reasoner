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
    public void run(Reasoner hermit,StatusOutput status,PrintWriter output,boolean ignoreOntologyPrefixes) {
        if (file!=null) {
            if (file.equals("-")) {
                output=new PrintWriter(System.out);
            }
            else {
                java.io.FileOutputStream f;
                try {
                    f=new java.io.FileOutputStream(file);
                }
                catch (java.io.FileNotFoundException e) {
                    throw new IllegalArgumentException("unable to open "+file+" for writing");
                }
                catch (SecurityException e) {
                    throw new IllegalArgumentException("unable to write to "+file);
                }
                output=new PrintWriter(f);
            }
        }
        if (ignoreOntologyPrefixes)
            output.println(hermit.getDLOntology().toString(new Prefixes()));
        else
            output.println(hermit.getDLOntology().toString(hermit.getPrefixes()));
        output.flush();
    }
}