package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger.WaitOption;

public class WaitForCommand extends AbstractCommand implements DebuggerCommand {
    
    /**
     * Adds or removes wait options for the debugger. 
     */
    public void execute() {
        boolean add;
        for (int index=1;index<args.length;index++) {
            String argument=args[index];
            WaitOption waitOption=null;
            add = true;
            if (argument.startsWith("-")) {
                add=false;
                argument = argument.substring(1);
            } else if (argument.startsWith("+")) {
                argument = argument.substring(1);
            }
            if ("gexists".equals(argument))
                waitOption=WaitOption.GRAPH_EXPANSION;
            else if ("exists".equals(argument))
                waitOption=WaitOption.EXISTENTIAL_EXPANSION;
            else if ("clash".equals(argument))
                waitOption=WaitOption.CLASH;
            else if ("merge".equals(argument))
                waitOption=WaitOption.MERGE;
            else if ("dtcheck".equals(argument))
                waitOption=WaitOption.DATATYPE_CHECKING;
            else {
                debugger.getOutput().println("Invalid wait option '"+argument+"'.");
                return;
            }
            if (waitOption!=null) {
                modifyWaitOptions(waitOption,add);
                debugger.getOutput().println("Will "+(add ? "" : "not ")+"wait for "+waitOption+".");
            }
        }
    }
    protected void modifyWaitOptions(WaitOption waitOption, boolean add) {
        if (add) {
            debugger.addWaitOption(waitOption);
        } else {
            debugger.removeWaitOption(waitOption);
        }
    }
    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("usage: waitFor ([+|-]gexists|exists|clash|merge)+");
        writer.println("usage example: waitFor -clash +gexists");
        writer.println("Sets (+ default) or removes (-) wait options for the " +
        		"tableau expansion. Possible wait options are: ");
        writer.println("gexists - stop at the next description graph expansion");
        writer.println("exists - stop at the next existential expansion");
        writer.println("clash - stop at the next clash");
        writer.println("merge - stop at the next merging of nodes");
        writer.flush();
        return buffer.toString();
    }

}
