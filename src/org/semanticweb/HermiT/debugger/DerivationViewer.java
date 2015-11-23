/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.semanticweb.HermiT.Prefixes;
/**DerivationViewer.*/
@SuppressWarnings("serial")
public class DerivationViewer extends JFrame {
    protected final Prefixes m_prefixes;
    protected final DerivationTreeTreeModel m_derivationTreeTreeModel;
    protected final JTree m_derivationTree;

    /**
     * @param prefixes prefixes
     * @param root root
     */
    public DerivationViewer(Prefixes prefixes,DerivationHistory.Fact root) {
        super("Derivation tree for "+root.toString(prefixes));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        m_prefixes=prefixes;
        m_derivationTreeTreeModel=new DerivationTreeTreeModel(root);
        m_derivationTree=new JTree(m_derivationTreeTreeModel);
        m_derivationTree.setLargeModel(true);
        m_derivationTree.setShowsRootHandles(true);
        m_derivationTree.setCellRenderer(new DerivationTreeCellRenderer());
        JScrollPane scrollPane=new JScrollPane(m_derivationTree);
        scrollPane.setPreferredSize(new Dimension(600,400));
        JButton button=new JButton("Refresh");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        JPanel panel=new JPanel(new BorderLayout());
        panel.add(scrollPane,BorderLayout.CENTER);
        panel.add(button,BorderLayout.SOUTH);
        setContentPane(panel);
        getRootPane().setDefaultButton(button);
        pack();
        setLocation(150,150);
        setVisible(true);
    }
    /**Refresh.*/
    public void refresh() {
        m_derivationTreeTreeModel.refresh();
    }

    protected static class DerivationTreeTreeModel implements TreeModel,Serializable {
        private static final long serialVersionUID=9210217812084186766L;

        protected final EventListenerList m_eventListeners;
        protected final DerivationHistory.Fact m_root;

        public DerivationTreeTreeModel(DerivationHistory.Fact root) {
            m_eventListeners=new EventListenerList();
            m_root=root;
        }
        @Override
        public void addTreeModelListener(TreeModelListener listener) {
            m_eventListeners.add(TreeModelListener.class,listener);
        }
        @Override
        public void removeTreeModelListener(TreeModelListener listener) {
            m_eventListeners.remove(TreeModelListener.class,listener);
        }
        @Override
        public Object getChild(Object parent,int index) {
            DerivationHistory.Fact parentFact=(DerivationHistory.Fact)parent;
            DerivationHistory.Derivation derivation=parentFact.getDerivation();
            return derivation.getPremise(index);
        }
        @Override
        public int getChildCount(Object parent) {
            DerivationHistory.Fact parentFact=(DerivationHistory.Fact)parent;
            DerivationHistory.Derivation derivation=parentFact.getDerivation();
            return derivation.getNumberOfPremises();
        }
        @Override
        public int getIndexOfChild(Object parent,Object child) {
            DerivationHistory.Fact parentFact=(DerivationHistory.Fact)parent;
            DerivationHistory.Derivation derivation=parentFact.getDerivation();
            for (int index=0;index<derivation.getNumberOfPremises();index++)
                if (child.equals(derivation.getPremise(index)))
                    return index;
            return -1;
        }
        @Override
        public Object getRoot() {
            return m_root;
        }
        @Override
        public boolean isLeaf(Object node) {
            DerivationHistory.Fact nodeFact=(DerivationHistory.Fact)node;
            DerivationHistory.Derivation derivation=nodeFact.getDerivation();
            return derivation.getNumberOfPremises()==0;
        }
        @Override
        public void valueForPathChanged(TreePath path,Object newValue) {
        }
        public void refresh() {
            Object[] listeners=m_eventListeners.getListenerList();
            TreeModelEvent e=new TreeModelEvent(this,new Object[] { getRoot() });
            for (Object listener : listeners)
                if (listener instanceof TreeModelListener)
                    ((TreeModelListener)listener).treeStructureChanged(e);
        }
    }

    protected static class TextIcon implements Icon,Serializable {
        private static final long serialVersionUID=2955881594360729470L;

        protected static final int WIDTH16=16;
        protected static final int HEIGHT16=16;

        protected final Color m_background;
        protected final Color m_foreground;
        protected final String m_text;
        protected final Font m_font;

        public TextIcon(Color background,Color foreground,String text,Font font) {
            m_background=background;
            m_foreground=foreground;
            m_text=text;
            m_font=font;
        }
        @Override
        public int getIconHeight() {
            return WIDTH16;
        }
        @Override
        public int getIconWidth() {
            return HEIGHT16;
        }
        @Override
        public void paintIcon(Component c,Graphics g,int x,int y) {
            Color oldColor=g.getColor();
            g.setColor(m_background);
            g.fillOval(x+2,y+2,x+WIDTH16-2,y+HEIGHT16-2);
            g.setColor(m_foreground);
            Font oldFont=g.getFont();
            g.setFont(m_font);
            FontMetrics fontMetrics=g.getFontMetrics();
            int textX=x+(WIDTH16-fontMetrics.stringWidth(m_text))/2+2;
            int textY=y+(HEIGHT16+fontMetrics.getAscent()-fontMetrics.getDescent())/2;
            g.drawString(m_text,textX,textY);
            g.setFont(oldFont);
            g.setColor(oldColor);
        }
    }

    protected static final Font s_font=new Font("Serif",Font.BOLD,11);

    protected static final Icon DLCLAUSE_APPLICATION_ICON=new TextIcon(Color.YELLOW,Color.BLACK,"R",s_font);
    protected static final Icon DISJUNCT_APPLICATION_ICON=new TextIcon(Color.CYAN,Color.BLACK,"D",s_font);
    protected static final Icon MERGING_ICON=new TextIcon(Color.BLUE,Color.WHITE,"M",s_font);
    protected static final Icon GRAPH_CHECKING_ICON=new TextIcon(Color.DARK_GRAY,Color.WHITE,"G",s_font);
    protected static final Icon CLASH_DETECTION_ICON=new TextIcon(Color.BLACK,Color.WHITE,"C",s_font);
    protected static final Icon EXISTENTIAL_EXPANSION_ICON=new TextIcon(Color.RED,Color.WHITE,"E",s_font);
    protected static final Icon BASE_FACT_ICON=new TextIcon(Color.MAGENTA,Color.WHITE,"B",s_font);

    protected class DerivationTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree,Object value,boolean s,boolean expanded,boolean leaf,int row,boolean focus) {
            DerivationHistory.Fact fact=(DerivationHistory.Fact)value;
            DerivationHistory.Derivation derivation=fact.getDerivation();
            StringBuffer text=new StringBuffer();
            text.append(fact.toString(m_prefixes));
            text.append(derivation.toString(m_prefixes));
            super.getTreeCellRendererComponent(tree,text.toString(),s,expanded,leaf,row,focus);
            if (derivation instanceof DerivationHistory.DLClauseApplication)
                setIcon(DLCLAUSE_APPLICATION_ICON);
            else if (derivation instanceof DerivationHistory.DisjunctApplication)
                setIcon(DISJUNCT_APPLICATION_ICON);
            else if (derivation instanceof DerivationHistory.Merging)
                setIcon(MERGING_ICON);
            else if (derivation instanceof DerivationHistory.GraphChecking)
                setIcon(GRAPH_CHECKING_ICON);
            else if (derivation instanceof DerivationHistory.ClashDetection)
                setIcon(CLASH_DETECTION_ICON);
            else if (derivation instanceof DerivationHistory.ExistentialExpansion)
                setIcon(EXISTENTIAL_EXPANSION_ICON);
            else if (derivation instanceof DerivationHistory.BaseFact)
                setIcon(BASE_FACT_ICON);
            else
                setIcon(null);
            return this;
        }
    }
}
