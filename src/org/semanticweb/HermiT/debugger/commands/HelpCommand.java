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
            "commandName","prints help for a command"
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: help");
        writer.println("    Prints this message.");
        writer.println("usage: help commandName");
        writer.println("    Prints help for the command commandName.");
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
            m_debugger.getOutput().println("Available commands are:");
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
                    m_debugger.getOutput().print("  ");
                    m_debugger.getOutput().print(commandLine);
                    for (int i=commandLine.length();i<maxFirstColumnWidth;i++)
                        m_debugger.getOutput().print(' ');
                    m_debugger.getOutput().print("  :  ");
                    m_debugger.getOutput().println(description[index+1]);
                }
            }
            m_debugger.getOutput().println();
            m_debugger.getOutput().println("Nodes in the current model are identified by node IDs.");
            m_debugger.getOutput().println("Predicates are written as follows, where uri can be abbreviated or full:");
            m_debugger.getOutput().println("    ==      equality");
            m_debugger.getOutput().println("    !=      inequality");
            m_debugger.getOutput().println("    +uri    atomic concept with the URI uri");
            m_debugger.getOutput().println("    -uri    atomic role with the URI uri");
            m_debugger.getOutput().println("    $uri    description graph with the URI uri");
        }
    }
}
