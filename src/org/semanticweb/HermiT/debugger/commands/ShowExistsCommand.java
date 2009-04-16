package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.debugger.Debugger.NodeCreationInfo;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class ShowExistsCommand extends AbstractCommand {

    public ShowExistsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "showExists";
    }
    public String[] getDescription() {
        return new String[] { "","prints nodes with their unprocessed existentials" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: showExists");
        writer.println("Prints nodes with their unprocessed existentials and where these came from. ");
    }
    public void execute(String[] args) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Nodes with existentials");
        writer.println("================================================================================");
        writer.println("      ID    # Existentials    Start Existential");
        writer.println("================================================================================");
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            if (node.isActive() && !node.isBlocked()&&node.hasUnprocessedExistentials()) {
                writer.print("  ");
                Printing.printPadded(writer,node.getNodeID(),6);
                writer.print("      ");
                Printing.printPadded(writer,node.getUnprocessedExistentials().size(),6);
                writer.print("        ");
                this.printStartExistential(node,writer);
                writer.println();
            }
            node=node.getNextTableauNode();
        }
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Nodes with existentials");
        selectConsoleWindow();
    }
    protected void printStartExistential(Node node,PrintWriter writer) {
        NodeCreationInfo nodeCreationInfo=m_debugger.getNodeCreationInfo(node);
        ExistentialConcept startExistential=nodeCreationInfo.m_createdByExistential;
        if (startExistential==null)
            writer.print("(root)");
        else
            writer.print(startExistential.toString(m_debugger.getPrefixes()));
    }
}
