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
        hermit.setExistentialsType(HermiT.ExistentialsType.INDIVIDUAL_REUSE);
        hermit.setTimingOn();
//        hermit.setTimingWithPauseOn();
//        hermit.setDebuggingOn(false);
//        hermit.setDebuggingOn(true);

//        hermit.loadOntology("file:/C:/Work/TestOntologies/FMA/fma_no_data.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/pizza/pizza.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/propreo/propreo.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/NCI/nciOncology.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/DOLCE_397/dolce_all_no_datatype.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/bams-from-swanson/bams-from-swanson-98-4-5-07-no-data.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-module1-no-functionality.owl");
        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-module1-no-inverse.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-module1.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-ians-full-undoctored.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/GALEN/galen-ians-full-doctored.owl");
//        hermit.loadOntology("file:/C:/Work/TestOntologies/wine/wine-no-data-properties.owl");

        PrintWriter printWriter=new PrintWriter("c:\\temp\\rules.txt");
        printWriter.println(hermit.getDLOntology().toString(hermit.getNamespaces()));
        printWriter.close();

        if (hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_HISTORY_ON && hermit.getTableauMonitorType()!=HermiT.TableauMonitorType.DEBUGGER_NO_HISTORY) {
            System.out.print("Press something to start...");
            pause();
        }
        long start=System.currentTimeMillis();

//        hermit.isABoxSatisfiable();
          subsumptionHierarchy=hermit.getSubsumptionHierarchy();
        
//        hermit.isSatisfiable("http://nlm.nih.gov/ontology/FMAInOWL#Diaphragmatic_surface_of_heart");
//        hermit.isSatisfiable("http://nlm.nih.gov/ontology/FMAInOWL#A1_pulley_of_fibrous_tendon_sheath_of_finger");
//        hermit.isSubsumedBy("http://nlm.nih.gov/ontology/FMAInOWL#A1_pulley_of_fibrous_tendon_sheath_of_finger","http://nlm.nih.gov/ontology/FMAInOWL#A-tubule_of_triplet_microtubule_of_kinetosome_of_flagellu");
        
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-agent","http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#physical-endurant");
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-agent","http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#agentive-social-object");
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-physical-object","http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-agent");      
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-agent","http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#non-physical-endurant");
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#rational-agent","http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#agentive-social-object");
//        hermit.isSubsumedBy("http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#non-physical-endurant","http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#endurant");
        
        /* These are the hard tests in 'galen-ians-full-undoctored.owl'. They require backtracking in the presence of individual reuse. */
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalCavity","http://www.co-ode.org/ontologies/galen#ActualCavity");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AbdominalAorta","http://www.co-ode.org/ontologies/galen#ArteryWhichHasLaterality");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#AlcoholicGastritis","http://www.co-ode.org/ontologies/galen#Duodenitis");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#CapsuleOfKnee","http://www.co-ode.org/ontologies/galen#JointCapsule");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#RenalAbscess","http://www.co-ode.org/ontologies/galen#ActualCavity");
//        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#RightBundleBranchBlock","http://www.co-ode.org/ontologies/galen#LeftBundleBranchBlock");

        
//        hermit.getTableau().isSatisfiable(org.semanticweb.HermiT.model.AtomicConcept.create("http://nlm.nih.gov/ontology/FMAInOWL#A-peripheral_microtubule_of_axoneme_of_cilium"));
//        hermit.getTableau().isSatisfiable(org.semanticweb.HermiT.model.AtomicConcept.create("http://www.co-ode.org/ontologies/galen#Abdomen"));
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abdomen");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbdominalCavity");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbdominalAorta");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#CortexOfKidney");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AcuteErosionOfStomach");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AnteriorCruciateLigament");
      
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
