package org.semanticweb.HermiT.existentials;

import java.io.*;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;

public class Test {
    public static void main(String[] args) throws Exception {
        DLOntology dlOntology=DLOntology.load(new File("c:\\Temp\\galen-module1.ser"));
        PrintWriter writer=new PrintWriter(new FileWriter("c:\\Temp\\galen-module1-rules.txt"));
        Namespaces ns=new Namespaces();
        ns.registerPrefix("a",dlOntology.getOntologyURI()+"#");
        writer.println(dlOntology.toString(ns));
        writer.close();
    }
}
