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

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
/**AgainCommand.*/
public class AgainCommand extends AbstractCommand {
    
    /**
     * @param debugger debugger
     */
    public AgainCommand(Debugger debugger) {
        super(debugger);
    }
    @Override
    public String getCommandName() {
        return "a";
    }
    @Override
    public String[] getDescription() {
        return new String[] { "","executes the last command again" };
    }
    @Override
    public void printHelp(PrintWriter writer) {
        writer.println("usage: a");
        writer.println("    Executes the last command again.");
    }
    @Override
    public void execute(String[] args) {
        String commandLine=m_debugger.getLastCommand();
        if (commandLine!=null) {
            m_debugger.getOutput().println("# "+commandLine);
            m_debugger.processCommandLine(commandLine);
        }
    }
}
