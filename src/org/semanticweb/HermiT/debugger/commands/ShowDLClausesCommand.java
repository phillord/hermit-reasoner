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
        for (DLClause dlClause : m_debugger.getTableau().getDLOntology().getDLClauses())
            writer.println(dlClause.toString(m_debugger.getPrefixes()));
        writer.flush();
        showTextInWindow(buffer.toString(),"DL-clauses");
        selectConsoleWindow();
    }
}
