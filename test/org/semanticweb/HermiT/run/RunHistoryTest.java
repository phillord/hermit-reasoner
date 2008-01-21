package org.semanticweb.HermiT.run;

import org.semanticweb.HermiT.*;

public class RunHistoryTest {

    public static void main(String[] args) throws Exception {
        HermiT hermit=new HermiT();
        hermit.setDebuggingOn(true);
        String physicalURI=RunHistoryTest.class.getResource("res/history-test.xml").toString();
        hermit.loadOntology(physicalURI);
        hermit.isABoxSatisfiable();
    }
}
