package org.semanticweb.HermiT.run;

import java.io.File;
import java.io.PrintWriter;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.hierarchy.*;

public class RunGraphsDebugger {

    public static void main(String[] args) throws Exception {
        HermiT hermit=new HermiT();
        hermit.setDebuggingOn(true);

        // String dlOntology="galen-ians-full-undoctored-concrete.ser";
        String dlOntology="galen-ians-full-undoctored-modified-concrete.ser";
        hermit.loadDLOntology(new File("c:\\Temp\\"+dlOntology));
        SubsumptionHierarchy subsumptionHierarchy=null;


//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abacavir");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#AbductorPollicisBrevis");
//        hermit.isSatisfiable("http://www.co-ode.org/ontologies/galen#Abdomen");
        hermit.isSubsumedBy("http://www.co-ode.org/ontologies/galen#GramNegativeCellWall","http://www.co-ode.org/ontologies/galen#BacterialCellWall");
        
//        subsumptionHierarchy=hermit.getSubsumptionHierarchy();

        if (subsumptionHierarchy!=null)
            hermit.printFlattenedHierarchy(new PrintWriter("c:\\Temp\\out.txt"),subsumptionHierarchy);
    }
}
