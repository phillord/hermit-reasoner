package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class ExitCommand extends AbstractCommand implements ICommand {
   
    public ExitCommand() {
    }
    /**
     * Exists the current program execution. 
     */
    public void execute() {
        System.exit(0);
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: exit");
        writer.println("or");
        writer.println("usage: quit");
        writer.println("Exits the current program. ");
        writer.flush();
        return buffer.toString();
    }

}
