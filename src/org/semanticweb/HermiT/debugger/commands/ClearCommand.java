package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class ClearCommand extends AbstractCommand implements DebuggerCommand {
    
    /**
     * Clears the command screen.
     */
    public void execute() {
        debugger.getConsoleTextArea().clear();
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: clear");
        writer.println("Clear the command line screen. ");
        writer.flush();
        return buffer.toString();
    }

}
