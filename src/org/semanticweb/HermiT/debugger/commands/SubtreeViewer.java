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
package org.semanticweb.HermiT.debugger.commands;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.debugger.Debugger.NodeCreationInfo;
import org.semanticweb.HermiT.debugger.Printing;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.tableau.Node;

@SuppressWarnings("serial")
public class SubtreeViewer extends JFrame {
    protected final Debugger m_debugger;
    protected final SubtreeTreeModel m_subtreeTreeModel;
    protected final JTextArea m_nodeInfoTextArea;
    protected final JTree m_tableauTree;
    protected final JTextField m_nodeIDField;

    public SubtreeViewer(Debugger debugger,Node rootNode) {
        super("Subtree for node "+rootNode.getNodeID());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        m_debugger=debugger;
        m_subtreeTreeModel=new SubtreeTreeModel(debugger,rootNode);
        m_tableauTree=new JTree(m_subtreeTreeModel);
        m_tableauTree.setLargeModel(true);
        m_tableauTree.setShowsRootHandles(true);
        m_tableauTree.setCellRenderer(new NodeCellRenderer(debugger));
        m_tableauTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectionPath=m_tableauTree.getSelectionPath();
                if (selectionPath==null)
                    showNodeLabels(null);
                else
                    showNodeLabels((Node)selectionPath.getLastPathComponent());
            }
        });
        m_nodeInfoTextArea=new JTextArea();
        m_nodeInfoTextArea.setFont(Debugger.s_monospacedFont);
        JScrollPane modelScrollPane=new JScrollPane(m_tableauTree);
        modelScrollPane.setPreferredSize(new Dimension(600,400));
        JScrollPane nodeInfoScrollPane=new JScrollPane(m_nodeInfoTextArea);
        nodeInfoScrollPane.setPreferredSize(new Dimension(400,400));
        JSplitPane mainSplit=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,modelScrollPane,nodeInfoScrollPane);
        JPanel commandsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,3));
        commandsPanel.add(new JLabel("Node ID:"));
        m_nodeIDField=new JTextField();
        m_nodeIDField.setPreferredSize(new Dimension(200,m_nodeIDField.getPreferredSize().height));
        commandsPanel.add(m_nodeIDField);
        JButton button=new JButton("Search");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int nodeID;
                String nodeIDText=m_nodeIDField.getText();
                try {
                    nodeID=Integer.parseInt(nodeIDText);
                }
                catch (NumberFormatException error) {
                    JOptionPane.showMessageDialog(SubtreeViewer.this,"Invalid node ID '"+nodeIDText+"'.");
                    return;
                }
                Node node=m_debugger.getTableau().getNode(nodeID);
                if (node==null) {
                    JOptionPane.showMessageDialog(SubtreeViewer.this,"Node with ID "+nodeID+" cannot be found.");
                    return;
                }
                findNode(node);
            }
        });
        getRootPane().setDefaultButton(button);
        commandsPanel.add(button);
        button=new JButton("Refresh");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        commandsPanel.add(button);
        JPanel mainPanel=new JPanel(new BorderLayout());
        mainPanel.add(mainSplit,BorderLayout.CENTER);
        mainPanel.add(commandsPanel,BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
        setLocation(200,200);
        setVisible(true);
        m_nodeIDField.requestFocusInWindow();
    }
    public void refresh() {
        m_subtreeTreeModel.refresh();
    }
    public void findNode(Node node) {
        List<Node> pathToRoot=new ArrayList<Node>();
        Node currentNode=node;
        while (currentNode!=null && currentNode!=m_subtreeTreeModel.getRoot()) {
            pathToRoot.add(currentNode);
            currentNode=m_debugger.getNodeCreationInfo(currentNode).m_createdByNode;
        }
        if (currentNode==null) {
            JOptionPane.showMessageDialog(SubtreeViewer.this,"Node with ID "+node.getNodeID()+" is not present in the shown subtree.");
            return;
        }
        TreePath treePath=new MyTreePath(null,m_subtreeTreeModel.getRoot());
        for (int index=pathToRoot.size()-1;index>=0;--index)
            treePath=new MyTreePath(treePath,pathToRoot.get(index));
        m_tableauTree.expandPath(treePath);
        m_tableauTree.setSelectionPath(treePath);
        m_tableauTree.scrollPathToVisible(treePath);
    }
    public void showNodeLabels(Node node) {
        if (node==null)
            m_nodeInfoTextArea.setText("");
        else {
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            Printing.printNodeData(m_debugger,node,writer);
            writer.flush();
            m_nodeInfoTextArea.setText(buffer.toString());
            m_nodeInfoTextArea.select(0,0);
        }
    }

    protected static class SubtreeTreeModel implements TreeModel {
        protected final EventListenerList m_eventListeners;
        protected final Debugger m_debugger;
        protected final Node m_root;

        public SubtreeTreeModel(Debugger debugger,Node root) {
            m_eventListeners=new EventListenerList();
            m_debugger=debugger;
            m_root=root;
        }
        public void addTreeModelListener(TreeModelListener listener) {
            m_eventListeners.add(TreeModelListener.class,listener);
        }
        public void removeTreeModelListener(TreeModelListener listener) {
            m_eventListeners.remove(TreeModelListener.class,listener);
        }
        public Node getChild(Object parent,int index) {
            NodeCreationInfo nodeCreationInfo = null;
            if (parent instanceof Node) {
                nodeCreationInfo = m_debugger.getNodeCreationInfo((Node) parent);
            }
            if (nodeCreationInfo==null)
                return null;
            else
                return nodeCreationInfo.m_children.get(index);
        }
        public int getChildCount(Object parent) {
            NodeCreationInfo nodeCreationInfo = null;
            if (parent instanceof Node)
                nodeCreationInfo = m_debugger.getNodeCreationInfo((Node) parent);
            if (nodeCreationInfo==null)
                return 0;
            else
                return nodeCreationInfo.m_children.size();
        }
        public int getIndexOfChild(Object parent,Object child) {
            NodeCreationInfo nodeCreationInfo = null;
            if (parent instanceof Node)
                nodeCreationInfo = m_debugger.getNodeCreationInfo((Node) parent);
            if (nodeCreationInfo==null)
                return -1;
            else
                return nodeCreationInfo.m_children.indexOf(child);
        }
        public Object getRoot() {
            return m_root;
        }
        public boolean isLeaf(Object node) {
            return getChildCount(node)==0;
        }
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

    protected static class NodeCellRenderer extends DefaultTreeCellRenderer {
        protected static final Icon NOT_ACTIVE_ICON=new DotIcon(Color.LIGHT_GRAY);
        protected static final Icon BLOCKED_ICON=new DotIcon(Color.CYAN);
        protected static final Icon WITH_EXISTENTIALS_ICON=new DotIcon(Color.RED);
        protected static final Icon NI_NODE_ICON=new DotIcon(Color.BLACK);
        protected static final Icon NAMED_NODE_ICON=new DotIcon(Color.DARK_GRAY);
        protected static final Icon TREE_NODE_ICON=new DotIcon(Color.GREEN);
        protected static final Icon GRAPH_NODE_ICON=new DotIcon(Color.MAGENTA);
        protected static final Icon CONCRETE_NODE_ICON=new DotIcon(Color.BLUE);

        protected final Debugger m_debugger;

        public NodeCellRenderer(Debugger debugger) {
            m_debugger=debugger;
        }
        public Component getTreeCellRendererComponent(JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {
            Node node=(Node)value;
            StringBuffer buffer=new StringBuffer();
            ExistentialConcept existentialConcept=m_debugger.getNodeCreationInfo(node).m_createdByExistential;
            if (existentialConcept==null) {
                buffer.append(node.getNodeID());
                buffer.append(":(root)");
            }
            else if (existentialConcept instanceof AtLeastConcept) {
                AtLeastConcept atLeastConcept=(AtLeastConcept)existentialConcept;
                buffer.append(atLeastConcept.getOnRole().toString(m_debugger.getPrefixes()));
                buffer.append("  -->  ");
                buffer.append(node.getNodeID());
                buffer.append(":[");
                buffer.append(atLeastConcept.getToConcept().toString(m_debugger.getPrefixes()));
                buffer.append("]");
            }
            else {
                // Do nothing for now.
            }
            super.getTreeCellRendererComponent(tree,buffer.toString(),selected,expanded,leaf,row,hasFocus);
            if (!node.isActive())
                setIcon(NOT_ACTIVE_ICON);
            else if (node.isBlocked())
                setIcon(BLOCKED_ICON);
            else if (node.hasUnprocessedExistentials())
                setIcon(WITH_EXISTENTIALS_ICON);
            else {
                switch (node.getNodeType()) {
                case NAMED_NODE:
                    setIcon(NAMED_NODE_ICON);
                    break;
                case TREE_NODE:
                    setIcon(TREE_NODE_ICON);
                    break;
                case GRAPH_NODE:
                    setIcon(GRAPH_NODE_ICON);
                    break;
                case NI_NODE:
                    setIcon(NI_NODE_ICON);
                    break;
                case CONCRETE_NODE:
                case ROOT_CONSTANT_NODE:
                default:
                    setIcon(CONCRETE_NODE_ICON);
                    break;
                }
            }
            return this;
        }
    }

    protected static class DotIcon implements Icon {
        protected final Color m_color;

        public DotIcon(Color color) {
            m_color=color;
        }
        public int getIconHeight() {
            return 16;
        }
        public int getIconWidth() {
            return 16;
        }
        public void paintIcon(Component c,Graphics g,int x,int y) {
            Color oldColor=g.getColor();
            g.setColor(m_color);
            g.fillOval(x+2,y+2,x+12,y+12);
            g.setColor(oldColor);
        }
    }

    protected static class MyTreePath extends TreePath {
        public MyTreePath(Object object) {
            super(object);
        }
        public MyTreePath(TreePath treePath,Object object) {
            super(treePath,object);
        }
    }
}
