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
import org.semanticweb.HermiT.model.DLClause;

public class ShowDLClausesCommand extends AbstractCommand {

    public ShowDLClausesCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showDLClauses";
    }
    public String[] getDescription() {
        return new String[] { "","prints the currently used set of DL-clauses" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showDLClauses");
        writer.println("    Prints the currently used set of DL-clauses.");
    }
    public void execute(String[] args) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        if (!m_debugger.getTableau().getPermanentDLOntology().getDLClauses().isEmpty()) {
            writer.println("-----------------------------------------------");
            writer.println("Permanent DL-clauses:");
            writer.println("-----------------------------------------------");
            for (DLClause dlClause : m_debugger.getTableau().getPermanentDLOntology().getDLClauses())
                writer.println(dlClause.toString(m_debugger.getPrefixes()));
        }
        if (m_debugger.getTableau().getAdditionalDLOntology()!=null && !m_debugger.getTableau().getAdditionalDLOntology().getDLClauses().isEmpty()) {
            writer.println("-----------------------------------------------");
            writer.println("Additional DL-clauses:");
            writer.println("-----------------------------------------------");
            for (DLClause dlClause : m_debugger.getTableau().getAdditionalDLOntology().getDLClauses())
                writer.println(dlClause.toString(m_debugger.getPrefixes()));
        }
        writer.flush();
        showTextInWindow(buffer.toString(),"DL-clauses");
        selectConsoleWindow();
    }
}
