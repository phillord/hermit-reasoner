package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class HelpCommand extends AbstractCommand implements ICommand {
    public void execute() {
        // do nothing
    }

    public String getHelpText() {
        CharArrayWriter buffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("Possible commands are: ");
        writer.println("activenodes: show all active nodes");
        writer.println("BPTime timeInSeconds: set break point after timeInSeconds");
        writer.println("exit: exit the debugger");
        writer.println("c: continue with the execution");
        writer.println("clear: clear the screen");
        writer.println("cont: continue with the execution");
        writer.println("derTree clash|unaryPredicate nodeID|binaryPredicate nodeID nodeID: show the derivation tree for the given predicate");
        writer.println("diffLabels nodeID1 nodeID2: compares the node labels");
        writer.println("forever: run and don't wait for input");
        writer.println("history on|off: switch derivation history on/off");
        writer.println("isAncOf nodeID1 nodeID2: tests whether nodeID1 is an ancestor of nodeID2");
        writer.println("nodesFor conceptName: prints nodes that have been created by (atleast n r.conceptName)");
        writer.println("originStats: prints origin information for nodes in the tableau");
        writer.println("q: see query");
        writer.println("query: prints whether there is a clash");
        writer.println("query +C|$DG|? nodeID|?: prints facts for C - concept, DG - decr. graph, ? any unary predicate");
        writer.println("query ==|!=|-R|? nodeID|? nodeID|?: prints facts for R-role, ? any binary predicate");
        writer.println("quit: see exit");
        writer.println("showExists: prints nodes with their unprocessed existentials");
        writer.println("showModel [Predicate]: print all assertions (for the predicate if given)");
        writer.println("showDLClauses: prints the clauses from the current DLOntology");
        writer.println("showNode nodeID: prints information about the node nodeID");
        writer.println("showdGraph graphName: prints a text representation for the description graph graphName");
        writer.println("showSubtree [nodeID]: prints the subtree for the last checked node or nodeID if given");
        writer.println("singleStep on|off: step by step mode on or off");
        writer.println("rNodeFor nodeID: prints concepts for which the given node is a reuse node under individual reuse strategy");
        writer.println("searchLabel nodeID: prints all nodes with label equal to nodeID");
        writer.println("searchPWBlock nodeID1: prints nodes with the same blocking signature");
        writer.println("UDisjunctions: prints unprocessed ground disjunctions");
        writer.println("waitFor ([+|-]gexists|cexists|exists|clash|merge)+: sets (+ default) or removes (-) wait options");
        writer.println("");
        writer.println("Use \"help commandName\" to get help for a specific " +
        		"command. ");
        writer.flush();
        return buffer.toString();
    }

}
