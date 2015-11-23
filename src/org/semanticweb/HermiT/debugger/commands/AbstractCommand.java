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

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
/**AbstractCommand.*/
public abstract class AbstractCommand implements DebuggerCommand {
    protected final Debugger m_debugger;

    /**
     * @param debugger debugger
     */
    public AbstractCommand(Debugger debugger) {
        m_debugger=debugger;
    }
    protected void showTextInWindow(String string,String title) {
        JTextArea textArea=new JTextArea(string);
        textArea.setFont(Debugger.s_monospacedFont);
        JScrollPane scrollPane=new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400,300));
        JFrame frame=new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        frame.setLocation(100,100);
        frame.setVisible(true);
    }
    protected void selectConsoleWindow() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (m_debugger!=null)
                    m_debugger.getMainFrame().toFront();
            }
        });
    }
    protected DLPredicate getDLPredicate(String predicate) {
        if ("==".equals(predicate))
            return Equality.INSTANCE;
        else if ("!=".equals(predicate))
            return Inequality.INSTANCE;
        else if (predicate.startsWith("+")) 
            return AtomicConcept.create(m_debugger.getPrefixes().expandAbbreviatedIRI(predicate.substring(1)));
        else if (predicate.startsWith("-"))
            return AtomicRole.create(m_debugger.getPrefixes().expandAbbreviatedIRI(predicate.substring(1)));
        else if (predicate.startsWith("$")) {
            String graphName=m_debugger.getPrefixes().expandAbbreviatedIRI(predicate.substring(1));
            for (DescriptionGraph descriptionGraph : m_debugger.getTableau().getPermanentDLOntology().getAllDescriptionGraphs())
                if (graphName.equals(descriptionGraph.getName()))
                    return descriptionGraph;
            return null;
        }
        else
            return null;
    }
}
