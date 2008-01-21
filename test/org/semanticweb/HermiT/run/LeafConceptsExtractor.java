package org.semanticweb.HermiT.run;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LeafConceptsExtractor {

    public static void main(String[] args) throws Exception {
        Map<String,Set<String>> hierarchy=DiffHierarchies.loadHierarchy("C:\\Temp\\FMA-FULL-syntactic-classification.txt");
        Map<String,Set<String>> subconceptsByConcept=new HashMap<String,Set<String>>();
        for (Map.Entry<String,Set<String>> entry : hierarchy.entrySet()) {
            String subconcept=entry.getKey();
            for (String superconcept : entry.getValue()) {
                Set<String> subconcepts=subconceptsByConcept.get(superconcept);
                if (subconcepts==null) {
                    subconcepts=new HashSet<String>();
                    subconceptsByConcept.put(superconcept,subconcepts);
                }
                subconcepts.add(subconcept);
            }
        }
        Set<String> leafConcepts=new TreeSet<String>();
        for (Map.Entry<String,Set<String>> entry : subconceptsByConcept.entrySet())
            if (entry.getValue().size()==1)
                leafConcepts.add(entry.getKey());
        PrintWriter output=new PrintWriter(new FileWriter("c:\\Temp\\leafConcepts.txt"));
        try {
            for (String concept : leafConcepts)
                output.println(concept);
        }
        finally {
            output.close();
        }
    }
}
