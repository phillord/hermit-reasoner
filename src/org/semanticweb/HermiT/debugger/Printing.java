// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger;

import java.io.PrintWriter;
import java.util.Collection;

public class Printing {
    public static void printPadded(PrintWriter writer,int number,int size) {
        printPadded(writer,String.valueOf(number),size);
    }
    public static void printPadded(PrintWriter writer,String string,int size) {
        for (int i=size-string.length();i>=0;--i)
            writer.print(' ');
        writer.print(string);
    }
    public static <T> void printCollection(Collection<T> collection,PrintWriter writer) {
        for (T object : collection) {
            writer.print("    ");
            writer.print(object.toString());
            writer.println();
        }
    }
    public static <T> void diffCollections(String in1NotIn2,String in2NotIn1,PrintWriter writer,Collection<T> c1,Collection<T> c2) {
        boolean window1Message=false;
        for (Object object : c1) {
            if (!c2.contains(object)) {
                if (!window1Message) {
                    writer.println("<<<  "+in1NotIn2+":");
                    window1Message=true;
                }
                writer.print("    ");
                writer.print(object.toString());
                writer.println();
            }
        }
        if (window1Message)
            writer.println("--------------------------------------------");
        boolean window2Message=false;
        for (Object object : c2) {
            if (!c1.contains(object)) {
                if (!window2Message) {
                    writer.println(">>>  "+in2NotIn1+":");
                    window2Message=true;
                }
                writer.print("    ");
                writer.print(object.toString());
                writer.println();
            }
        }
        if (window1Message)
            writer.println("--------------------------------------------");
    }
}
