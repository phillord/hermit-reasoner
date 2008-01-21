package org.semanticweb.HermiT.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.hierarchy.*;

public class RunGraphs {
    public static final BufferedReader in=new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {
        HermiT hermit=new HermiT();
        hermit.setTimingOn();

        // String dlOntology="galen-ians-full-undoctored-concrete.ser";
        String dlOntology="galen-ians-full-undoctored-modified-concrete.ser";
        hermit.loadDLOntology(new File("c:\\Temp\\"+dlOntology));
        SubsumptionHierarchy subsumptionHierarchy=null;

        System.out.print("Press something to start...");
        pause();
        long start=System.currentTimeMillis();

//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abacavir");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbductorPollicisBrevis");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abdomen");
        
//        subsumptionHierarchy=hermit.getSubsumptionHierarchy();

        long duration=System.currentTimeMillis()-start;
        System.out.println("The reasoning task took "+duration+" ms");

        if (subsumptionHierarchy!=null)
            hermit.printFlattenedHierarchy(new PrintWriter("c:\\Temp\\out.txt"),subsumptionHierarchy);

        System.out.println("Press something to end...");
        pause();
    }
    public static void pause() {
        try {
            in.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
