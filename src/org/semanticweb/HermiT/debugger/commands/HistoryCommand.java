package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class HistoryCommand extends AbstractCommand implements ICommand {
   
    /**
     * Switches the derivation history on or off. 
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("The status is missing.");
            return;
        }
        String status=args[1].toLowerCase();
        if ("on".equals(status)) {
            debugger.setForwardingOn(true);
            debugger.getOutput().println("Derivation history on.");
        }
        else if ("off".equals(status)) {
            debugger.setForwardingOn(false);
            debugger.getOutput().println("Derivation history off.");
        }
        else
            debugger.getOutput().println("Incorrect history status '"+status+"'.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: history on/off");
        writer.println("Switches the derivation history on or off.");
        writer.flush();
        return buffer.toString();
    }

}
