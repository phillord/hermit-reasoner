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
import org.semanticweb.HermiT.debugger.DerivationHistory;
import org.semanticweb.HermiT.debugger.DerivationViewer;
import org.semanticweb.HermiT.tableau.Node;

public class DerivationTreeCommand extends AbstractCommand {

    public DerivationTreeCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "dertree";
    }
    public String[] getDescription() {
        return new String[] {
            "clash","shows the derivation tree for the clash",
            "predicate [nodeID]+","shows the derivation tree for the given atom",
        };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: dertree clash");
        writer.println("    Shows the derivation tree for the clash.");
        writer.println("usage: dertree predicate [nodeID]+");
        writer.println("    Shows the derivation tree for the given atom.");
        writer.println("    yellow: DL clause application");
        writer.println("    cyan: disjunct application (choose and apply a disjunct)");
        writer.println("    blue: merged two nodes");
        writer.println("    dark grey: description graph checking");
        writer.println("    black: clash");
        writer.println("    red: existential expansion");
        writer.println("    magenta: base/given fact");
    }
    public void execute(String[] args) {
        if (args.length<2) {
            m_debugger.getOutput().println("The specification of the predicate is missing.");
            return;
        }
        Object[] tuple;
        String predicate=args[1];
        if ("clash".equals(predicate.toLowerCase()))
            tuple=new Object[0];
        else {
            tuple=new Object[args.length-1];
            try {
                tuple[0]=getDLPredicate(predicate);
            }
            catch (Exception e) {
                m_debugger.getOutput().println("Invalid predicate '"+predicate+"':"+e.getMessage());
            }
            if (tuple[0]==null) {
                m_debugger.getOutput().println("Invalid predicate '"+predicate+"'.");
                return;
            }
        }
        for (int index=1;index<tuple.length;index++) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(args[index+1]);
            }
            catch (NumberFormatException e) {
                m_debugger.getOutput().println("Invalid ID of the node at argument "+index+".");
                return;
            }
            Node node=m_debugger.getTableau().getNode(nodeID);
            if (node==null) {
                m_debugger.getOutput().println("Node with ID '"+nodeID+"' not found.");
                return;
            }
            tuple[index]=node;
        }
        DerivationHistory.Atom atom=m_debugger.getDerivationHistory().getAtom(tuple);
        if (atom!=null) {
            new DerivationViewer(m_debugger.getPrefixes(),atom);
            selectConsoleWindow();
        }
        else
            m_debugger.getOutput().println("Atom not found.");
    }
}
