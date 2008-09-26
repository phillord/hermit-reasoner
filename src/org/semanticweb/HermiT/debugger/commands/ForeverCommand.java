package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class ForeverCommand extends AbstractCommand implements ICommand {
    
    /**
     * Starts expanding the tableau and does not wait for input. 
     */
    public void execute() {
        debugger.setInMainLoop(false);
        debugger.setForever(true);
        debugger.setSinglestep(false);
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: forever");
        writer.println("Starts expanding the tableau and does not wait for input. ");
        writer.flush();
        return buffer.toString();
    }

}
