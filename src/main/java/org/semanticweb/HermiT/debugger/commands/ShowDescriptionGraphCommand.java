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
import org.semanticweb.HermiT.model.DescriptionGraph;

public class ShowDescriptionGraphCommand extends AbstractCommand {

    public ShowDescriptionGraphCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showDGraph";
    }
    public String[] getDescription() {
        return new String[] { "graphName","prints a text representation of the description graph graphName" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showDGraph graphName");
        writer.println("    Prints information about the description graph with the given name.");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("Graph name is missing.");
            return;
        }
        String graphName=args[1];
        for (DescriptionGraph descriptionGraph : m_debugger.getTableau().getPermanentDLOntology().getAllDescriptionGraphs())
            if (descriptionGraph.getName().equals(graphName)) {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("===========================================");
                writer.println("    Contents of the graph '"+descriptionGraph.getName()+"'");
                writer.println("===========================================");
                writer.println(descriptionGraph.getTextRepresentation());
                writer.flush();
                showTextInWindow(buffer.toString(),"Contents of the graph '"+descriptionGraph.getName()+"'");
                selectConsoleWindow();
                return;
            }
        m_debugger.getOutput().println("Graph '"+graphName+"' not found.");
    }
}
