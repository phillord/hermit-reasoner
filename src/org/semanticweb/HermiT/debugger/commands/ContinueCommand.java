package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class ContinueCommand extends AbstractCommand implements DebuggerCommand {
    
    /**
     * Continues the execution of the current program. 
     */
    public void execute() {
          debugger.setInMainLoop(false);
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: continue");
        writer.println("or");
        writer.println("usage: c");
        writer.println("Continues the execution of the current program. ");
        writer.flush();
        return buffer.toString();
    }

}
