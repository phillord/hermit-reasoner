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
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.debugger.Debugger.NodeCreationInfo;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;

public class ShowExistsCommand extends AbstractCommand {

    public ShowExistsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showExists";
    }
    public String[] getDescription() {
        return new String[] { "","prints nodes with unprocessed existentials" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showExists");
        writer.println("    Prints a list of nodes that have unprocessed existentials, together with information that generated these nodes.");
    }
    public void execute(String[] args) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Nodes with existentials");
        writer.println("================================================================================");
        writer.println("      ID    # Existentials    Start Existential");
        writer.println("================================================================================");
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            if (node.isActive() && !node.isBlocked()&&node.hasUnprocessedExistentials()) {
                writer.print("  ");
                Printing.printPadded(writer,node.getNodeID(),6);
                writer.print("      ");
                Printing.printPadded(writer,node.getUnprocessedExistentials().size(),6);
                writer.print("        ");
                this.printStartExistential(node,writer);
                writer.println();
            }
            node=node.getNextTableauNode();
        }
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Nodes with existentials");
        selectConsoleWindow();
    }
    protected void printStartExistential(Node node,PrintWriter writer) {
        NodeCreationInfo nodeCreationInfo=m_debugger.getNodeCreationInfo(node);
        ExistentialConcept startExistential=nodeCreationInfo.m_createdByExistential;
        if (startExistential==null)
            writer.print("(root)");
        else
            writer.print(startExistential.toString(m_debugger.getPrefixes()));
    }
}
