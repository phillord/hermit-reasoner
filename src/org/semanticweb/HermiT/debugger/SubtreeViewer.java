package org.semanticweb.HermiT.debugger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.ArrayList;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

@SuppressWarnings("serial")
public class SubtreeViewer extends JFrame {
    protected final Debugger m_debugger;
    protected final SubtreeTreeModel m_subtreeTreeModel;
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
        JScrollPane scrollPane=new JScrollPane(m_tableauTree);
        scrollPane.setPreferredSize(new Dimension(600,400));
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
                Node node=m_debugger.m_tableau.getNode(nodeID);
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
        mainPanel.add(scrollPane,BorderLayout.CENTER);
        mainPanel.add(commandsPanel,BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
        setLocation(200,200);
        setVisible(true);
    }
    public void refresh() {
        m_subtreeTreeModel.refresh();
    }
    public void findNode(Node node) {
        List<Node> pathToRoot=new ArrayList<Node>();
        Node currentNode=node;
        while (currentNode!=null && currentNode!=m_subtreeTreeModel.getRoot()) {
            pathToRoot.add(currentNode);
            currentNode=m_debugger.m_nodeCreationInfos.get(currentNode).m_createdByNode;
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
        public Object getChild(Object parent,int index) {
            return m_debugger.m_nodeCreationInfos.get(parent).m_children.get(index);
        }
        public int getChildCount(Object parent) {
            return m_debugger.m_nodeCreationInfos.get(parent).m_children.size();
        }
        public int getIndexOfChild(Object parent,Object child) {
            return m_debugger.m_nodeCreationInfos.get(parent).m_children.indexOf(child);
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
        protected static final Icon BLOCKED_ICON=new DotIcon(Color.CYAN);
        protected static final Icon WITH_EXISTENTIALS_ICON=new DotIcon(Color.RED);
        protected static final Icon NORMAL_ICON=new DotIcon(Color.BLACK);

        protected final Debugger m_debugger;
        
        public NodeCellRenderer(Debugger debugger) {
            m_debugger=debugger;
        }
        public Component getTreeCellRendererComponent(JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {
            Node node=(Node)value;
            StringBuffer buffer=new StringBuffer();
            ExistentialConcept existentialConcept=m_debugger.m_nodeCreationInfos.get(node).m_createdByExistential;
            if (existentialConcept==null) {
                buffer.append(node.getNodeID());
                buffer.append(":(root)");
            }
            else if (existentialConcept instanceof AtLeastAbstractRoleConcept) {
                AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)existentialConcept;
                buffer.append(atLeastAbstractRoleConcept.getOnAbstractRole().toString(m_debugger.m_namespaces));
                buffer.append("  -->  ");
                buffer.append(node.getNodeID());
                buffer.append(":[");
                buffer.append(atLeastAbstractRoleConcept.getToConcept().toString(m_debugger.m_namespaces));
                buffer.append("]");
            }
            else {
                
            }
            super.getTreeCellRendererComponent(tree,buffer.toString(),selected,expanded,leaf,row,hasFocus);
            if (node.isBlocked())
                setIcon(BLOCKED_ICON);
            else if (node.hasUnprocessedExistentials())
                setIcon(WITH_EXISTENTIALS_ICON);
            else
                setIcon(NORMAL_ICON);
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
