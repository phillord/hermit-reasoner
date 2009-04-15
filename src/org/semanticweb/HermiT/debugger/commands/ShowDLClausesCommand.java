package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.model.DLClause;


public class ShowDLClausesCommand extends AbstractCommand implements DebuggerCommand {
    
    /**
     * Prints the clauses from the current DLOntology. 
     */
    public void execute() {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        for (DLClause dlClause : debugger.getTableau().getDLOntology().getDLClauses())
            writer.println(dlClause.toString(debugger.getPrefixes()));
        writer.flush();
        super.showTextInWindow(buffer.toString(),"DL-clauses");
        super.selectConsoleWindow();
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: showDLClauses");
        writer.println("Prints the clauses from the current DLOntology. ");
        writer.flush();
        return buffer.toString();
    }

}
