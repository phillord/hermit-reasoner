package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class SingleStepCommand extends AbstractCommand implements ICommand {
    
    /**
     * Set stepwise expansion to on or off.
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("The status is missing.");
            return;
        }
        String status=args[1].toLowerCase();
        if ("on".equals(status)) {
            debugger.setSinglestep(true);
            debugger.getOutput().println("Single step mode on.");
        }
        else if ("off".equals(status)) {
            debugger.setSinglestep(false);
            debugger.getOutput().println("Single step mode off.");
        }
        else
            debugger.getOutput().println("Incorrect single step mode '"+status+"'.");
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: singleStep on|off");
        writer.println("Expands the tableau step by step if on and " +
        		"continuously if off. ");
        writer.flush();
        return buffer.toString();
    }

}
