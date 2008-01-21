package org.semanticweb.HermiT.run;

import java.util.*;
import java.io.*;

public class DiffHierarchies {

    public static void main(String[] args) throws Exception {
        Map<String,Set<String>> newClassification=loadHierarchy("c:\\Temp\\stupid-classification.txt");
        Map<String,Set<String>> oldClassification=loadHierarchy("c:\\Temp\\GALEN-original.txt");
        
        System.out.println("The following are the newly derived subsumption relationships:");
        System.out.println("==============================================================");
        printSuperclassesIn1ButNotIn2(newClassification,oldClassification);
        System.out.println();
        System.out.println();
        System.out.println("The following subsumption relationships got \"lost in the translation\":");
        System.out.println("==============================================================");
        printSuperclassesIn1ButNotIn2(oldClassification,newClassification);
    }
    protected static void printSuperclassesIn1ButNotIn2(Map<String,Set<String>> hierarchy1,Map<String,Set<String>> hierarchy2) {
        int numberOfSubsumptions=0;
        for (Map.Entry<String,Set<String>> entry1 : hierarchy1.entrySet()) {
            Set<String> superclasses1=entry1.getValue();
            Set<String> superclasses2=hierarchy2.get(entry1.getKey());
            if (superclasses2==null)
                superclasses2=Collections.emptySet();
            Set<String> temp=new TreeSet<String>(superclasses1);
            temp.removeAll(superclasses2);
            if (!temp.isEmpty()) {
                System.out.println(entry1.getKey());
                for (String string : temp) {
                    System.out.println("    "+string);
                    numberOfSubsumptions++;
                }
            }
        }
        System.out.println("==============================================================");
        System.out.println("  Total relationships: "+numberOfSubsumptions);
        System.out.println("==============================================================");
    }
    public static Map<String,Set<String>> loadHierarchy(String fileName) throws Exception {
        System.setOut(new PrintStream(new FileOutputStream("c:\\Temp\\diff.txt"),true));
        Map<String,Set<String>> hierarchy=new TreeMap<String,Set<String>>();
        BufferedReader reader=new BufferedReader(new FileReader(fileName));
        try {
            Set<String> currentSet=null;
            String line=reader.readLine();
            while (line!=null) {
                if ("-----------------------------------------------".equals(line))
                    currentSet=null;
                else if (line.startsWith("    ")) {
                    String classURI=line.substring("    ".length());
                    currentSet.add(classURI);
                }
                else if (!"! THE END !".equals(line)) {
                    currentSet=hierarchy.get(line);
                    if (currentSet==null) {
                        currentSet=new TreeSet<String>();
                        hierarchy.put(line,currentSet);
                    }
                }
                line=reader.readLine();
            }
            return hierarchy;
        }
        finally {
            reader.close();
        }
    }
}
