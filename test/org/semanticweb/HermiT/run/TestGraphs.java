package org.semanticweb.HermiT.run;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.semanticweb.HermiT.model.*;

public class TestGraphs {
    public static final BufferedReader in=new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {
        DLOntology dlOntology=DLOntology.load(new File("c:\\Temp\\galen-ians-full-undoctored.ser"));
        DescriptionGraph graph=dlOntology.getAllDescriptionGraphs().iterator().next();
        String string=graph.toString();
        System.out.println(string);
        System.out.println(dlOntology.isHorn());
    }
}
