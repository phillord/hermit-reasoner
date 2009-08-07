// Copyright 2009 by Oxford University; see license.txt for details
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
        for (DescriptionGraph descriptionGraph : m_debugger.getTableau().getDLOntology().getAllDescriptionGraphs())
            if (descriptionGraph.getName().equals(graphName)) {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("===========================================");
                writer.println("    Contents of the graph '"+graphName+"'");
                writer.println("===========================================");
                writer.println(descriptionGraph.getTextRepresentation());
                writer.flush();
                showTextInWindow(buffer.toString(),"Contents of the graph '"+graphName+"'");
                selectConsoleWindow();
                return;
            }
        m_debugger.getOutput().println("Graph '"+graphName+"' not found.");
    }
}
