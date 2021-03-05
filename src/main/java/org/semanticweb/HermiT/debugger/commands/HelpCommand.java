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
/**HelpCommand.*/
public class HelpCommand extends AbstractCommand {

    /**
     * @param debugger debugger
     */
    public HelpCommand(Debugger debugger) {
        super(debugger);
    }
    @Override
    public String getCommandName() {
        return "help";
    }
    @Override
    public String[] getDescription() {
        return new String[] {
            "","prints this list of command",
            "commandName","prints help for a command"
        };
    }
    @Override
    public void printHelp(PrintWriter writer) {
        writer.println("usage: help");
        writer.println("    Prints this message.");
        writer.println("usage: help commandName");
        writer.println("    Prints help for the command commandName.");
    }
    @Override
    public void execute(String[] args) {
        PrintWriter output = m_debugger.getOutput();
        if (args.length>1) {
            String commandName=args[1];
            DebuggerCommand command=m_debugger.getCommand(commandName);
            if (command==null)
                output.println("Unknown command '"+commandName+"'.");
            else
                command.printHelp(output);
        }
        else {
            output.println("Available commands are:");
            int maxFirstColumnWidth=0;
            for (DebuggerCommand command : m_debugger.getDebuggerCommands().values()) {
                String[] description=command.getDescription();
                for (int index=0;index<description.length;index+=2) {
                    int firstColumnWidth=command.getCommandName().length();
                    if (description[index].length() != 0)
                        firstColumnWidth+=1+description[index].length();
                    maxFirstColumnWidth=Math.max(maxFirstColumnWidth,firstColumnWidth);
                }
            }
            for (DebuggerCommand command : m_debugger.getDebuggerCommands().values()) {
                String[] description=command.getDescription();
                for (int index=0;index<description.length;index+=2) {
                    String commandLine=command.getCommandName();
                    if (description[index].length() != 0)
                        commandLine+=' '+description[index];
                    output.print("  ");
                    output.print(commandLine);
                    for (int i=commandLine.length();i<maxFirstColumnWidth;i++)
                        output.print(' ');
                    output.print("  :  ");
                    output.println(description[index+1]);
                }
            }
            output.println();
            output.println("Nodes in the current model are identified by node IDs.");
            output.println("Predicates are written as follows, where uri can be abbreviated or full:");
            output.println("    ==      equality");
            output.println("    !=      inequality");
            output.println("    +uri    atomic concept with the URI uri");
            output.println("    -uri    atomic role with the URI uri");
            output.println("    $uri    description graph with the URI uri");
        }
    }
}
