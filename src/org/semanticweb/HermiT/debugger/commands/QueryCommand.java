/* Copyright 2009 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;

public class QueryCommand extends AbstractCommand {

    public QueryCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "query";
    }
    public String[] getDescription() {
        return new String[] {
            "","prints whether there is a clash",
            "?|predicate [?|nodeID]+","prints all facts matching the query; ? is a joker",
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: query");
        writer.println("    Prints whether the model contains a clash.");
        writer.println("usage: ?|predicate [?|nodeID]+");
        writer.println("    Prints all facts matching the query, which is a partially specified atom.");
        writer.println("    Parts of the atom are either specified fully, or by using ? as a joker.");
    }
    public void execute(String[] args) {
        Object[] tuple=new Object[args.length-1];
        if (tuple.length==0) {
            // no further argument, so just check for a clash
            if (m_debugger.getTableau().getExtensionManager().containsClash())
                m_debugger.getOutput().println("The model currently contains a clash.");
            else
                m_debugger.getOutput().println("The modelcurrently does not contain a clash.");
        }
        else {
            // further query arguments
            if ("?".equals(args[1]))
                tuple[0]=null;
            else {
                try {
                    tuple[0]=getDLPredicate(args[1]);
                }
                catch (Exception e) {
                    m_debugger.getOutput().println("Invalid predicate '"+args[1]+"':"+e.getMessage());
                }
                if (tuple[0]==null) {
                    m_debugger.getOutput().println("Invalid predicate '"+args[1]+"'.");
                    return;
                }
            }
            for (int index=1;index<tuple.length;index++) {
                String nodeIDString=args[index+1];
                if ("?".equals(nodeIDString))
                    // no particular nodeID given
                    tuple[index]=null;
                else {
                    int nodeID;
                    try {
                        nodeID=Integer.parseInt(nodeIDString);
                    }
                    catch (NumberFormatException e) {
                        m_debugger.getOutput().println("Invalid node ID.");
                        return;
                    }
                    tuple[index]=m_debugger.getTableau().getNode(nodeID);
                    if (tuple[index]==null) {
                        m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                        return;
                    }
                }
            }
            boolean[] boundPositions=new boolean[tuple.length];
            for (int index=0;index<tuple.length;index++)
                if (tuple[index]!=null)
                    boundPositions[index]=true;
            ExtensionTable extensionTable=m_debugger.getTableau().getExtensionManager().getExtensionTable(tuple.length);
            ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(boundPositions,ExtensionTable.View.TOTAL);
            System.arraycopy(tuple,0,retrieval.getBindingsBuffer(),0,tuple.length);
            retrieval.open();
            Set<Object[]> facts=new TreeSet<Object[]>(Printing.FactComparator.INSTANCE);
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                facts.add(tupleBuffer.clone());
                retrieval.next();
            }
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("===========================================");
            StringBuffer queryName=new StringBuffer("Query:");
            writer.print("Query:");
            for (int index=1;index<args.length;index++) {
                writer.print(' ');
                writer.print(args[index]);
                queryName.append(' ');
                queryName.append(args[index]);
            }
            writer.println();
            writer.println("===========================================");
            for (Object[] fact : facts) {
                writer.print(' ');
                printFact(fact,writer);
                writer.println();
            }
            writer.println("===========================================");
            writer.flush();
            showTextInWindow(buffer.toString(),queryName.toString());
            selectConsoleWindow();
        }
    }
    protected void printFact(Object[] fact,PrintWriter writer) {
        Object dlPredicate=fact[0];
        if (dlPredicate instanceof Concept)
            writer.print(((Concept)dlPredicate).toString(m_debugger.getPrefixes()));
        else if (dlPredicate instanceof DLPredicate)
            writer.print(((DLPredicate)dlPredicate).toString(m_debugger.getPrefixes()));
        else
            throw new IllegalStateException("Internal error: invalid predicate.");
        writer.print('[');
        for (int position=1;position<fact.length;position++) {
            if (position!=1)
                writer.print(',');
            writer.print(((Node)fact[position]).getNodeID());
        }
        writer.print(']');
    }
}
