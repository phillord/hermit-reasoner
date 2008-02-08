package org.semanticweb.HermiT.run;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.hierarchy.*;

public class RunHermiT {
    public static final BufferedReader in=new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws Exception {
        System.setProperty("entityExpansionLimit",String.valueOf(Integer.MAX_VALUE));

        SubsumptionHierarchy subsumptionHierarchy=null;

//        System.setOut(new java.io.PrintStream(new java.io.FileOutputStream("c:\\temp\\transcript.txt"),true));
        
        HermiT hermit=new HermiT();
//        hermit.setBlockingSignatureCacheType(HermiT.BlockingSignatureCacheType.NOT_CACHED);
//        hermit.setExistentialsType(HermiT.ExistentialsType.INDIVIDUAL_REUSE);
        hermit.setTimingOn();
//        hermit.setTimingWithPauseOn();
//        hermit.setDebuggingOn(false);
//        hermit.setDebuggingOn(true);

        hermit.loadOntology("file:/C:/Work/TestOntologies/DOLCE_397/dolce_all.owl");
//        hermit.loadOntology("file:/C:/Work/ontologies/NCI/nciOncology.owl");
//        hermit.loadOntology("file:/C:/Temp/full-galen-no-functionality.owl");
//        hermit.loadOntology("file:/C:/Temp/galen-module1-no-functionality.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-module1.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-ians-full-undoctored.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-ians-full-doctored.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/wine/wine-no-data-properties.owl");

//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\GALEN-minus-graph.ser"));
//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\FMA-minus-graph.ser"));
//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\graphs\\galen-ians-full-undoctored-modified-concrete.ser"));
//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\FMA-Full-concrete.ser"));

//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\galen-module1.ser"));
//        hermit.getDLOntology().save(new java.io.File("C:\\Temp\\galen-module1.ser"));
//        hermit.loadDLOntology(new java.io.File("C:\\Temp\\galen-ians-full-undoctored.ser"));
//        hermit.getDLOntology().save(new java.io.File("C:\\Temp\\galen-ians-full-undoctored.ser"));
//        hermit=HermiT.load(new java.io.File("C:\\Temp\\galen-ians-full-undoctored-good-run.ser"));
//        hermit=HermiT.load(new java.io.File("C:\\Temp\\galen-ians-full-undoctored-bad-run.ser"));

        PrintWriter printWriter=new PrintWriter("c:\\temp\\rules.txt");
        printWriter.println(hermit.getDLOntology().toString(hermit.getNamespaces()));
        printWriter.close();

        if (hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_HISTORY_ON && hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_NO_HISTORY) {
            System.out.print("Press something to start...");
            pause();
        }
        long start=System.currentTimeMillis();

        /* These are the hard tests in 'galen-ians-full-undoctored.owl'. They require backtracking in the presence of individual reuse. */
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalCavity","http://www.co-ode.org/ontologies/galen#ActualCavity");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalAorta","http://www.co-ode.org/ontologies/galen#ArteryWhichHasLaterality");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AlcoholicGastritis","http://www.co-ode.org/ontologies/galen#Duodenitis");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#CapsuleOfKnee","http://www.co-ode.org/ontologies/galen#JointCapsule");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#RenalAbscess","http://www.co-ode.org/ontologies/galen#ActualCavity");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#RightBundleBranchBlock","http://www.co-ode.org/ontologies/galen#LeftBundleBranchBlock");

//        hermit.getTableau().isSatisfiable(org.semanticweb.HermiT.model.AtomicConcept.create("http://www.co-ode.org/ontologies/galen#Abdomen"));
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abdomen");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbdominalCavity");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbdominalAorta");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#CortexOfKidney");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AcuteErosionOfStomach");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AnteriorCruciateLigament");
      
        subsumptionHierarchy=hermit.getSubsumptionHierarchy();

        long duration=System.currentTimeMillis()-start;
        System.out.println("The reasoning task took "+duration+" ms");

        if (subsumptionHierarchy!=null)
            hermit.printFlattenedHierarchy(new PrintWriter("c:\\Temp\\out.txt"),subsumptionHierarchy);

        if (hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_HISTORY_ON && hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_NO_HISTORY) {
            System.out.println("Press something to end...");
            pause();
        }
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
