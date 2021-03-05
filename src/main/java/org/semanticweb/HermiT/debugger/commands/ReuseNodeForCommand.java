/* Copyright 2009 by the Oxford University Computing Laboratory
   
   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.debugger.commands;

import java.io.PrintWriter;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.existentials.IndividualReuseStrategy;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.tableau.Node;
/**ReuseNodeForCommand.*/
public class ReuseNodeForCommand extends AbstractCommand {

    /**
     * @param debugger debugger
     */
    public ReuseNodeForCommand(Debugger debugger) {
        super(debugger);
    }
    @Override
    public String getCommandName() {
        return "reuseNodeFor";
    }
    @Override
    public String[] getDescription() {
        return new String[] { "nodeID","prints concepts for which the given node is a reuse node under individual reuse strategy" };
    }
    @Override
    public void printHelp(PrintWriter writer) {
        writer.println("usage: reuseNodeFor nodeID");
        writer.println("    If individual reuse strategy is used, prints the concepts for which the given node is a reuse node.");
    }
    @Override
    public void execute(String[] args) {
        PrintWriter output = m_debugger.getOutput();
        if (args.length<2) {
            output.println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            output.println("Invalid ID of the node. "+e.getMessage());
            return;
        }
        Node node=m_debugger.getTableau().getNode(nodeID);
        if (node==null) {
            output.println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        ExistentialExpansionStrategy strategy=m_debugger.getTableau().getExistentialsExpansionStrategy();
        if (strategy instanceof IndividualReuseStrategy) {
            IndividualReuseStrategy reuseStrategy=(IndividualReuseStrategy)strategy;
            AtomicConcept conceptForNode=reuseStrategy.getConceptForNode(node);
            output.print("Node '");
            output.print(node.getNodeID());
            output.print("' is ");
            if (conceptForNode==null)
                output.println("not a reuse node for any concept.");
            else {
                output.print("a reuse node for the '");
                output.print(conceptForNode.toString(m_debugger.getPrefixes()));
                output.println("' concept.");
            }
        }
        else
            output.println("Node reuse strategy is not currently in effect.");
    }
}
