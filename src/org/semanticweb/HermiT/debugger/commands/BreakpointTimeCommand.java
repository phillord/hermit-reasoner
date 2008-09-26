package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class BreakpointTimeCommand extends AbstractCommand implements ICommand {
   
    /**
     * Tries to fetch an integer that represents the time to the next breakpoint 
     * in seconds from the arguments given to the constructor and 
     * prints a list of nodes and their blocking status for nodes with a label 
     * equal to the label of the node with the given ID
     */
    public void execute() {
        if (args.length<2) {
            debugger.getOutput().println("Time is missing.");
            return;
        }
        int breakpointTimeSeconds;
        try {
            breakpointTimeSeconds=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            debugger.getOutput().println("Invalid time.");
            return;
        }
        debugger.getOutput().println("Breakpoint time is "+breakpointTimeSeconds+" seconds.");
        debugger.setBreakpointTime(breakpointTimeSeconds*1000);
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: BreakpointTime timeInSeconds");
        writer.println("Sets the next breakpoint time to the given time in " +
        	       "seconds.");
        writer.flush();
        return buffer.toString();
    }

}
