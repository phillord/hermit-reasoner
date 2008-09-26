package org.semanticweb.HermiT.debugger.commands;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.semanticweb.HermiT.debugger.Debugger;
import org.semanticweb.HermiT.tableau.Node;



public abstract class AbstractCommand implements ICommand {
    protected Debugger debugger;
    protected String[] args;
    
    public abstract void execute();

    public abstract String getHelpText();
    
    protected void showTextInWindow(String string,String title) {
        JTextArea textArea=new JTextArea(string);
        textArea.setFont(Debugger.s_monospacedFont);
        JScrollPane scrollPane=new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400,300));
        JFrame frame=new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        frame.setLocation(100,100);
        frame.setVisible(true);
    }
    
    protected void selectConsoleWindow() {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               if (debugger != null) debugger.getMainFrame().toFront();
           }
        });
    }
    
    /**
     * @param node a node in the tableau
     * @return "no" if node is not blocked; "directly by" plus "signature in 
     * cache" or the ID of the blocking node if the node is directly blocked; 
     * "indirectly by" plus "signature in cache" or the ID of the blocking node 
     * otherwise
     */
    protected static String formatBlockingStatus(Node node) {
        if (!node.isBlocked())
            return "no";
        else if (node.isDirectlyBlocked())
            return "directly by "+(node.getBlocker()==Node.CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
        else
            return "indirectly by "+(node.getBlocker()==Node.CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public void setDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}
