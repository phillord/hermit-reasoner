package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;

public class HelpCommand extends AbstractCommand {

    public HelpCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "help";
    }
    public String[] getDescription() {
        return new String[] {
            "","prints this list of command",
            "<command name>","prints help for a command"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: help [commandName]");
        writer.println("If commandName is not present, prints the list of all commands.");
        writer.println("If commandName is present, prints the help for a given command.");
    }
    public void execute(String[] args) {
        if (args.length>1) {
            String commandName=args[1];
            DebuggerCommand command=m_debugger.getCommand(commandName);
            if (command==null)
                m_debugger.getOutput().println("Unknown command '"+commandName+"'.");
            else
                command.printHelp(m_debugger.getOutput());
        }
        else {
            int maxFirstColumnWidth=0;
            for (DebuggerCommand command : m_debugger.getDebuggerCommands().values()) {
                String[] description=command.getDescription();
                for (int index=0;index<description.length;index+=2) {
                    int firstColumnWidth=command.getCommandName().length();
                    if (!description[index].isEmpty())
                        firstColumnWidth+=1+description[index].length();
                    maxFirstColumnWidth=Math.max(maxFirstColumnWidth,firstColumnWidth);
                }
            }
            for (DebuggerCommand command : m_debugger.getDebuggerCommands().values()) {
                String[] description=command.getDescription();
                for (int index=0;index<description.length;index+=2) {
                    String commandLine=command.getCommandName();
                    if (!description[index].isEmpty())
                        commandLine+=' '+description[index];
                    m_debugger.getOutput().print(commandLine);
                    for (int i=commandLine.length();i<maxFirstColumnWidth;i++)
                        m_debugger.getOutput().print(' ');
                    m_debugger.getOutput().print("  :  ");
                    m_debugger.getOutput().println(description[index+1]);
                }
            }
        }
    }
}
