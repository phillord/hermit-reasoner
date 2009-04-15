package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.model.DescriptionGraph;


public class ShowDescriptionGraphCommand extends AbstractCommand implements DebuggerCommand {
   
    /**
     * Prints a text representation for the description graph given as an argument. 
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("Graph name is missing.");
            return;
        }
        String graphName=args[1];
        for (DescriptionGraph descriptionGraph : debugger.getTableau().getDLOntology().getAllDescriptionGraphs())
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
        debugger.getOutput().println("Graph '"+graphName+"' not found.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: showDGraph graphName");
        writer.println("Prints a text representation for the description " +
        		"graph named graphName. ");
        writer.flush();
        return buffer.toString();
    }

}
