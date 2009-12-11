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

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.tableau.GroundDisjunction;

public class UnprocessedDisjunctionsCommand extends AbstractCommand {

    public UnprocessedDisjunctionsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "uDisjunctions";
    }
    public String[] getDescription() {
        return new String[] { "","shows unprocessed ground disjunctions" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: uDisjunctions");
        writer.println("    Prints a list of unprocessed ground disjunctions.");
    }
    public void execute(String[] args) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Unprocessed ground disjunctions");
        writer.println("===========================================");
        GroundDisjunction groundDisjunction=m_debugger.getTableau().getFirstUnprocessedGroundDisjunction();
        while (groundDisjunction!=null) {
            for (int disjunctIndex=0;disjunctIndex<groundDisjunction.getNumberOfDisjuncts();disjunctIndex++) {
                if (disjunctIndex!=0)
                    writer.print(" v ");
                DLPredicate dlPredicate=groundDisjunction.getDLPredicate(disjunctIndex);
                if (Equality.INSTANCE.equals(dlPredicate)) {
                    writer.print(groundDisjunction.getArgument(disjunctIndex,0).getNodeID());
                    writer.print(" == ");
                    writer.print(groundDisjunction.getArgument(disjunctIndex,1).getNodeID());
                }
                else {
                    writer.print(dlPredicate.toString(m_debugger.getPrefixes()));
                    writer.print('(');
                    for (int argumentIndex=0;argumentIndex<dlPredicate.getArity();argumentIndex++) {
                        if (argumentIndex!=0)
                            buffer.append(',');
                        writer.print(groundDisjunction.getArgument(disjunctIndex,argumentIndex).getNodeID());
                    }
                    writer.print(')');
                }
            }
            writer.println();
            groundDisjunction=groundDisjunction.getPreviousGroundDisjunction();
        }
        writer.flush();
        showTextInWindow(buffer.toString(),"Unprocessed ground disjunctions");
        selectConsoleWindow();
    }
}
