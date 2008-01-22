package org.semanticweb.HermiT.debugger;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Comparator;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.existentials.*;

public class Debugger extends TableauMonitorForwarder {
    private static final long serialVersionUID=-1061073966460686069L;

    public static final Font s_monospacedFont=new Font("Monospaced",Font.PLAIN,12);

    protected static enum WaitOption { GRAPH_EXPANSION,EXISTENTIAL_EXPANSION,CLASH };
    
    protected final Namespaces m_namespaces;
    protected final DerivationHistory m_derivationHistory;
    protected final ConsoleTextArea m_consoleTextArea;
    protected final JFrame m_mainFrame;
    protected final PrintWriter m_output;
    protected final BufferedReader m_input;
    protected final Set<WaitOption> m_waitOptions;
    protected Tableau m_tableau;
    protected String m_lastCommand;
    protected boolean m_forever;
    protected long m_lastStatusMark;
    protected boolean m_singlestep;
    protected boolean m_inMainLoop;
    protected int m_breakpointTime;
    protected int m_currentIteration;

    public Debugger(Namespaces namespaces,boolean historyOn) {
        super(new DerivationHistory());
        m_namespaces=namespaces;
        m_derivationHistory=(DerivationHistory)m_forwardingTargetMonitor;
        m_consoleTextArea=new ConsoleTextArea();
        m_consoleTextArea.setFont(s_monospacedFont);
        m_output=new PrintWriter(m_consoleTextArea.getWriter());
        m_input=new BufferedReader(m_consoleTextArea.getReader());
        JScrollPane scrollPane=new JScrollPane(m_consoleTextArea);
        scrollPane.setPreferredSize(new Dimension(800,300));
        m_mainFrame=new JFrame("HermiT Debugger");
        m_mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_mainFrame.setContentPane(scrollPane);
        m_mainFrame.pack();
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        Dimension preferredSize=m_mainFrame.getPreferredSize();
        m_mainFrame.setLocation((screenSize.width-preferredSize.width)/2,screenSize.height-100-preferredSize.height);
        m_forwardingOn=historyOn;
        m_waitOptions=new HashSet<WaitOption>();
        m_forever=false;
        m_singlestep=true;
        m_breakpointTime=30000;
        m_mainFrame.setVisible(true);
        m_output.println("Good morning Dr. Chandra. This is HAL. I'm ready for my first lesson.");
        m_output.println("Derivation history is "+(m_forwardingOn ? "on" : "off")+".");
    }
    public void dispose() {
        m_mainFrame.dispose();
        m_tableau=null;
    }
    public void mainLoop() {
        try {
            m_inMainLoop=true;
            while (m_inMainLoop) {
                m_output.print("> ");
                String commandLine=m_input.readLine();
                if (commandLine!=null) {
                    commandLine=commandLine.trim();
                    if ("a".equals(commandLine)) {
                        commandLine=m_lastCommand;
                        m_output.print("# ");
                        m_output.println(commandLine);
                    }
                    else
                        m_lastCommand=commandLine;
                    String[] parsedCommand=parse(commandLine);
                    String command=parsedCommand[0].toLowerCase();
                    if ("exit".equals(command) || "quit".equals(command))
                        doExit(parsedCommand);
                    else if ("c".equals(command) || "cont".equals(command))
                        doContinue(parsedCommand);
                    else if ("clear".equals(command))
                        doClear(parsedCommand);
                    else if ("forever".equals(command))
                        doForever(parsedCommand);
                    else if ("history".equals(command))
                        doHistory(parsedCommand);
                    else if ("dertree".equals(command))
                        doDerivationTree(parsedCommand);
                    else if ("exists".equals(command))
                        doShowExists(parsedCommand);
                    else if ("activenodes".equals(command))
                        doActiveNodes(parsedCommand);
                    else if ("isancof".equals(command))
                        doIsAncestorOf(parsedCommand);
                    else if ("showmodel".equals(command))
                        doShowModel(parsedCommand);
                    else if ("showdlclauses".equals(command))
                        doShowDLClauses(parsedCommand);
                    else if ("showdgraph".equals(command))
                        doShowDescriptionGraph(parsedCommand);
                    else if ("q".equals(command))
                        doQuery(parsedCommand);
                    else if ("searchlabel".equals(command))
                        doSearchLabel(parsedCommand);
                    else if ("difflabels".equals(command))
                        doDiffLabels(parsedCommand);
                    else if ("searchpwblock".equals(command))
                        doSearchPairWiseBlocking(parsedCommand);
                    else if ("searchpwblock".equals(command))
                        doSearchPairWiseBlocking(parsedCommand);
                    else if ("rnodefor".equals(command))
                        doReuseNodeFor(parsedCommand);
                    else if ("singlestep".equals(command))
                        doSingleStep(parsedCommand);
                    else if ("bptime".equals(command))
                        doBreakpointTime(parsedCommand);
                    else if ("wait".equals(command))
                        doWait(parsedCommand);
                    else
                        m_output.println("Unknown command '"+command+"'.");
                }
            }
            m_output.flush();
        }
        catch (IOException e) {
        }
        m_lastStatusMark=System.currentTimeMillis();
    }
    protected void doExit(String[] commandLine) {
        System.exit(0);
    }
    protected void doContinue(String[] commandLine) {
        m_inMainLoop=false;
    }
    protected void doForever(String[] commandLine) {
        m_inMainLoop=false;
        m_forever=true;
        m_singlestep=false;
    }
    protected void doClear(String[] commandLine) {
        m_consoleTextArea.clear();
    }
    protected void doHistory(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("The status is missing.");
            return;
        }
        String status=commandLine[1].toLowerCase();
        if ("on".equals(status)) {
            m_forwardingOn=true;
            m_output.println("Derivation history on.");
        }
        else if ("off".equals(status)) {
            m_forwardingOn=false;
            m_output.println("Derivation history off.");
        }
        else
            m_output.println("Incorrect history status '"+status+"'.");
    }
    protected void doDerivationTree(String[] commandLine) {
        Object[] tuple;
        if (commandLine.length<2) {
            m_output.println("The specification of the predicate is missing.");
            return;
        }
        String predicate=commandLine[1];
        if ("clash".equals(predicate))
            tuple=new Object[0];
        else {
            tuple=new Object[commandLine.length-1];
            tuple[0]=getDLPredicate(predicate);
            if (tuple[0]==null){
                m_output.println("Invalid predicate '"+predicate+"'.");
                return;
            }
        }
        for (int index=1;index<tuple.length;index++) {
            int nodeID;
            try {
                nodeID=Integer.parseInt(commandLine[index+1]);
            }
            catch (NumberFormatException e) {
                m_output.println("Invalid ID of the node at argument "+index+".");
                return;
            }
            Node node=m_tableau.getNode(nodeID);
            if (node==null) {
                m_output.println("Node with ID '"+nodeID+"' not found.");
                return;
            }
            tuple[index]=node;
        }
        DerivationHistory.Atom atom=m_derivationHistory.getAtom(tuple);
        if (atom!=null) {
            new DerivationViewer(m_namespaces,atom);
            selectConsoleWindow();
        }
        else
            m_output.println("Atom not found.");
    }
    protected DLPredicate getDLPredicate(String predicate) {
        if ("==".equals(predicate))
            return Equality.INSTANCE;
        else if ("!=".equals(predicate))
            return Inequality.INSTANCE;
        else if (predicate.startsWith("+"))
            return AtomicConcept.create(m_namespaces.expandString(predicate.substring(1)));
        else if (predicate.startsWith("-"))
            return AtomicAbstractRole.create(m_namespaces.expandString(predicate.substring(1)));
        else if (predicate.startsWith("$")) {
            String graphName=m_namespaces.expandString(predicate.substring(1));
            for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs())
                if (graphName.equals(descriptionGraph.getName()))
                    return descriptionGraph;
            return null;
        }
        else
            return null;
    }
    protected void doSingleStep(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("The status is missing.");
            return;
        }
        String status=commandLine[1].toLowerCase();
        if ("on".equals(status)) {
            m_singlestep=true;
            m_output.println("Single step mode on.");
        }
        else if ("off".equals(status)) {
            m_singlestep=false;
            m_output.println("Single step mode off.");
        }
        else
            m_output.println("Incorrect single step mode '"+status+"'.");
    }
    protected void doShowModel(String[] commandLine) {
        Set<Object[]> facts=new TreeSet<Object[]>(FactComparator.INSTANCE);
        String title;
        if (commandLine.length<2) {
            for (ExtensionTable extensionTable : m_tableau.getExtensionManager().getExtensionTables()) {
                ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.TOTAL);
                loadFacts(facts,retrieval);
            }
            title="Current model";
        }
        else {
            DLPredicate dlPredicate=getDLPredicate(commandLine[1]);
            if (dlPredicate!=null) {
                ExtensionTable extensionTable=m_tableau.getExtensionManager().getExtensionTable(dlPredicate.getArity()+1);
                boolean[] bindings=new boolean[extensionTable.getArity()];
                bindings[0]=true;
                ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(bindings,ExtensionTable.View.TOTAL);
                retrieval.getBindingsBuffer()[0]=dlPredicate;
                loadFacts(facts,retrieval);
                title="Assertions containing the predicate '"+m_namespaces.abbreviateAsNamespace(dlPredicate.toString())+"'.";
            }
            else {
                int nodeID;
                try {
                    nodeID=Integer.parseInt(commandLine[1]);
                }
                catch (NumberFormatException e) {
                    m_output.println("Invalid ID of the node.");
                    return;
                }
                Node node=m_tableau.getNode(nodeID);
                if (node==null) {
                    m_output.println("Node with ID '"+nodeID+"' not found.");
                    return;
                }
                for (ExtensionTable extensionTable : m_tableau.getExtensionManager().getExtensionTables())
                    for (int position=0;position<extensionTable.getArity();position++) {
                        boolean[] bindings=new boolean[extensionTable.getArity()];
                        bindings[position]=true;
                        ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(bindings,ExtensionTable.View.TOTAL);
                        retrieval.getBindingsBuffer()[position]=node;
                        loadFacts(facts,retrieval);
                    }
                title="Assertions containing node '"+node.getNodeID()+"'.";
            }
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        Object lastPredicate=null;
        for (Object[] fact : facts) {
            if (lastPredicate!=fact[0]) {
                lastPredicate=fact[0];
                writer.println();
            }
            writer.print(' ');
            printFact(fact,writer);
            writer.println();
        }
        writer.flush();
        showTextInWindow(buffer.toString(),title);
        selectConsoleWindow();
    }
    protected void printFact(Object[] fact,PrintWriter writer) {
        Object dlPredicate=fact[0];
        if (dlPredicate instanceof Concept)
            writer.print(((Concept)dlPredicate).toString(m_namespaces));
        else if (dlPredicate instanceof DLPredicate)
            writer.print(((DLPredicate)dlPredicate).toString(m_namespaces));
        else
            throw new IllegalStateException("Internal error: invalid predicate.");
        writer.print('[');
        for (int position=1;position<fact.length;position++) {
            if (position!=1)
                writer.print(',');
            writer.print(((Node)fact[position]).getNodeID());
        }
        writer.print(']');
    }
    protected void loadFacts(Set<Object[]> facts,ExtensionTable.Retrieval retrieval) {
        retrieval.open();
        while (!retrieval.afterLast()) {
            facts.add(retrieval.getTupleBuffer().clone());
            retrieval.next();
        }
    }
    protected void doShowDLClauses(String[] commandLine) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        for (DLClause dlClause : m_tableau.getDLOntology().getDLClauses())
            writer.println(dlClause.toString(m_namespaces));
        writer.flush();
        showTextInWindow(buffer.toString(),"DL-clauses");
        selectConsoleWindow();
    }
    protected void doShowDescriptionGraph(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("Graph name is missing.");
            return;
        }
        String graphName=commandLine[1];
        for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs())
            if (descriptionGraph.getName().equals(graphName)) {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("===========================================");
                writer.println("    Contents of the graph '"+graphName+"'");
                writer.println("===========================================");
                writer.println(descriptionGraph.getTextRepresentation());
                writer.flush();
                showTextInWindow(buffer.toString(),"Contents of the graph '"+graphName+"'");
                selectConsoleWindow();
                return;
            }
        m_output.println("Graph '"+graphName+"' not found.");
    }
    protected void doQuery(String[] commandLine) {
        Object[] tuple=new Object[commandLine.length-1];
        if (tuple.length==0) {
            if (m_tableau.getExtensionManager().containsClash())
                m_output.println("Tableau currently contains a clash.");
            else
                m_output.println("Tableau currently does not contain a clash.");
        }
        else {
            if ("?".equals(commandLine[1]))
                tuple[0]=null;
            else {
                tuple[0]=getDLPredicate(commandLine[1]);
                if (tuple[0]==null) {
                    m_output.println("Invalid predicate '"+commandLine[1]+"'.");
                    return;
                }
            }
            for (int index=1;index<tuple.length;index++) {
                String nodeIDString=commandLine[index+1];
                if ("?".equals(nodeIDString))
                    tuple[index]=null;
                else {
                    int nodeID;
                    try {
                        nodeID=Integer.parseInt(nodeIDString);
                    }
                    catch (NumberFormatException e) {
                        m_output.println("Invalid node ID.");
                        return;
                    }
                    tuple[index]=m_tableau.getNode(nodeID);
                    if (tuple[index]==null) {
                        m_output.println("Node with ID '"+nodeID+"' not found.");
                        return;
                    }
                }
            }
            boolean[] boundPositions=new boolean[tuple.length];
            for (int index=0;index<tuple.length;index++)
                if (tuple[index]!=null)
                    boundPositions[index]=true;
            ExtensionTable extensionTable=m_tableau.getExtensionManager().getExtensionTable(tuple.length);
            ExtensionTable.Retrieval retrieval=extensionTable.createRetrieval(boundPositions,ExtensionTable.View.TOTAL);
            System.arraycopy(tuple,0,retrieval.getBindingsBuffer(),0,tuple.length);
            retrieval.open();
            Set<Object[]> facts=new TreeSet<Object[]>(FactComparator.INSTANCE);
            Object[] tupleBuffer=retrieval.getTupleBuffer();
            while (!retrieval.afterLast()) {
                facts.add(tupleBuffer.clone());
                retrieval.next();
            }
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("===========================================");
            StringBuffer queryName=new StringBuffer("Query: ");
            writer.print("Query:");
            for (int index=1;index<commandLine.length;index++) {
                writer.print(' ');
                writer.print(commandLine[index]);
                queryName.append(' ');
                queryName.append(commandLine[index]);
            }
            writer.println();
            writer.println("===========================================");
            for (Object[] fact : facts) {
                writer.print(' ');
                printFact(fact,writer);
                writer.println();
            }
            writer.println("===========================================");
            writer.flush();
            showTextInWindow(buffer.toString(),queryName.toString());
            selectConsoleWindow();
        }
    }
    protected void doIsAncestorOf(String[] commandLine) {
        if (commandLine.length<3) {
            m_output.println("Node IDs are missing.");
            return;
        }
        int nodeID1;
        try {
            nodeID1=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid ID of the first node.");
            return;
        }
        int nodeID2;
        try {
            nodeID2=Integer.parseInt(commandLine[2]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid ID of the second node.");
            return;
        }
        Node node1=m_tableau.getNode(nodeID1);
        Node node2=m_tableau.getNode(nodeID2);
        if (node1==null) {
            m_output.println("Node with ID '"+nodeID1+"' not found.");
            return;
        }
        if (node2==null) {
            m_output.println("Node with ID '"+nodeID2+"' not found.");
            return;
        }
        boolean result=node1.isAncestorOf(node2);
        m_output.print("Node ");
        m_output.print(node1.getNodeID());
        m_output.print(" is ");
        if (!result)
            m_output.print("not ");
        m_output.print("an ancestor of node ");
        m_output.print(node2.getNodeID());
        m_output.println(".");
    }
    protected void doActiveNodes(String[] commandLine) {
        Node node=m_tableau.getFirstTableauNode();
        List<Node> nodes=new ArrayList<Node>();
        while (node!=null) {
            if (!node.isBlocked()) {
                nodes.add(node);
            }
            node=node.getNextTableauNode();
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Active nodes ("+nodes.size()+"):");
        writer.println("===========================================");
        writer.println("      ID");
        writer.println("===========================================");
        for (Node activeNode : nodes) {
            writer.print("  ");
            writer.println(activeNode.getNodeID());
        }
        writer.flush();
        showTextInWindow(buffer.toString(),"Active nodes");
        selectConsoleWindow();
    }
    protected void doSearchLabel(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid node ID.");
            return;
        }
        Node node=m_tableau.getNode(nodeID);
        if (node!=null) {
            CharArrayWriter buffer=new CharArrayWriter();
            PrintWriter writer=new PrintWriter(buffer);
            writer.println("Nodes with label equal to the one of "+node.getNodeID());
            writer.println("===========================================");
            writer.println("      ID    Blocked");
            writer.println("===========================================");
            Node searchNode=m_tableau.getFirstTableauNode();
            while (searchNode!=null) {
                // WARNING: the == comparison is possible only because we use the concept factory!
                if (node.getPositiveLabel()==searchNode.getPositiveLabel()) {
                    writer.print("  ");
                    Printing.printPadded(writer,searchNode.getNodeID(),6);
                    writer.print("    ");
                    writer.print(formatBlockingStatus(searchNode));
                    writer.println();
                }
                searchNode=searchNode.getNextTableauNode();
            }
            writer.flush();
            showTextInWindow(buffer.toString(),"Nodes with label equal to the one of "+node.getNodeID());
            selectConsoleWindow();
        }
        else
            m_output.println("Node with ID '"+nodeID+"' not found.");
    }
    protected static String  formatBlockingStatus(Node node) {
        if (!node.isBlocked())
            return "no";
        else if (node.isDirectlyBlocked())
            return "directly by "+node.getBlocker().getNodeID();
        else
            return "indirectly by "+node.getBlocker().getNodeID();
    }
    protected void doSearchPairWiseBlocking(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid node ID.");
            return;
        }
        Node node=m_tableau.getNode(nodeID);
        if (node!=null) {
            if (node.getNodeType()!=NodeType.TREE_NODE)
                m_output.println("Node "+node.getNodeID()+" is not a tree node and does not have a pair-wise blocking signature.");
            else {
                CharArrayWriter buffer=new CharArrayWriter();
                PrintWriter writer=new PrintWriter(buffer);
                writer.println("Nodes with the pair-wise blocking signature of "+node.getNodeID());
                writer.println("===========================================");
                writer.println("      ID    Blocked");
                writer.println("===========================================");
                Node searchNode=m_tableau.getFirstTableauNode();
                while (searchNode!=null) {
                    // WARNING: the == comparison works only because we use the concept set factory.
                    if (searchNode.getNodeType()==NodeType.TREE_NODE && node.getPositiveLabel()==searchNode.getPositiveLabel() && node.getParent().getPositiveLabel()==searchNode.getParent().getPositiveLabel() && node.getFromParentLabel()==searchNode.getFromParentLabel() && node.getToParentLabel()==searchNode.getToParentLabel()) {
                        writer.print("  ");
                        Printing.printPadded(writer,searchNode.getNodeID(),6);
                        writer.print("    ");
                        writer.print(formatBlockingStatus(searchNode));
                        writer.println();
                    }
                    searchNode=searchNode.getNextTableauNode();
                }
                writer.flush();
                showTextInWindow(buffer.toString(),"Nodes with the pair-wise blocking signature of "+node.getNodeID());
                selectConsoleWindow();
            }
        }
        else
            m_output.println("Node with ID '"+nodeID+"' not found.");
    }
    protected void doDiffLabels(String[] commandLine) {
        if (commandLine.length<3) {
            m_output.println("Node IDs are missing.");
            return;
        }
        int nodeID1;
        try {
            nodeID1=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid ID of the first node.");
            return;
        }
        int nodeID2;
        try {
            nodeID2=Integer.parseInt(commandLine[2]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid ID of the second node.");
            return;
        }
        Node node1=m_tableau.getNode(nodeID1);
        Node node2=m_tableau.getNode(nodeID2);
        if (node1==null) {
            m_output.println("Node with ID '"+nodeID1+"' not found.");
            return;
        }
        if (node2==null) {
            m_output.println("Node with ID '"+nodeID2+"' not found.");
            return;
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Differences in labels of node "+node1.getNodeID()+" and "+node2.getNodeID());
        writer.println("===========================================");
        Printing.diffCollections(
            "Concepts in the label of "+node1.getNodeID()+" but not in the label of "+node2.getNodeID(),
            "Concepts in the label of "+node2.getNodeID()+" but not in the label of "+node1.getNodeID(),
            writer,node1.getPositiveLabel(),node2.getPositiveLabel());
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Differences in labels of node "+node1.getNodeID()+" and "+node2.getNodeID());
        selectConsoleWindow();
    }

    protected void doShowExists(String[] commandLine) {
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("Nodes with existentials");
        writer.println("===========================================");
        writer.println("      ID    Existentials");
        writer.println("===========================================");
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked() && node.hasUnprocessedExistentials()) {
                writer.print("  ");
                Printing.printPadded(writer,node.getNodeID(),6);
                writer.print("      ");
                Printing.printPadded(writer,node.getUnprocessedExistentials().size(),6);
                writer.println();
            }
            node=node.getNextTableauNode();
        }
        writer.println("===========================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Nodes with existentials");
        selectConsoleWindow();
    }
    protected void doReuseNodeFor(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("Node ID is missing.");
            return;
        }
        int nodeID;
        try {
            nodeID=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid ID of the node.");
            return;
        }
        Node node=m_tableau.getNode(nodeID);
        if (node==null) {
            m_output.println("Node with ID '"+nodeID+"' not found.");
            return;
        }
        ExistentialsExpansionStrategy strategy=m_tableau.getExistentialsExpansionStrategy();
        if (strategy instanceof IndividualReuseStrategy) {
            IndividualReuseStrategy reuseStrategy=(IndividualReuseStrategy)strategy;
            AtomicConcept conceptForNode=reuseStrategy.getConceptForNode(node);
            m_output.print("Node '");
            m_output.print(node.getNodeID());
            m_output.print("' is ");
            if (conceptForNode==null)
                m_output.println("not a reuse node for any concept.");
            else {
                m_output.print("a reuse node for the '");
                m_output.print(m_namespaces.abbreviateAsNamespace(conceptForNode.getURI()));
                m_output.println("' concept.");
            }
        }
        else
            m_output.println("Node reuse strategy is not currently in effect.");
    }
    protected void doBreakpointTime(String[] commandLine) {
        if (commandLine.length<2) {
            m_output.println("Time is missing.");
            return;
        }
        int breakpointTimeSeconds;
        try {
            breakpointTimeSeconds=Integer.parseInt(commandLine[1]);
        }
        catch (NumberFormatException e) {
            m_output.println("Invalid time.");
            return;
        }
        m_output.println("Breakpoint time is "+breakpointTimeSeconds+" seconds.");
        m_breakpointTime=breakpointTimeSeconds*1000;
    }
    protected void doWait(String[] commandLine) {
        boolean add=true;
        for (int index=1;index<commandLine.length;index++) {
            String argument=commandLine[index];
            if ("+".equals(argument))
                add=true;
            else if ("-".equals(argument))
                add=false;
            else if ("gexists".equals(argument))
                modifyWaitOptions(WaitOption.GRAPH_EXPANSION,add);
            else if ("exists".equals(argument))
                modifyWaitOptions(WaitOption.EXISTENTIAL_EXPANSION,add);
            else if ("clash".equals(argument))
                modifyWaitOptions(WaitOption.CLASH,add);
            else {
                m_output.println("Invalid wait option '"+argument+"'.");
                return;
            }
        }
    }
    protected void modifyWaitOptions(WaitOption waitOption,boolean add) {
        if (add)
            m_waitOptions.add(waitOption);
        else
            m_waitOptions.remove(waitOption);
    }
    protected String[] parse(String command) {
        command=command.trim();
        List<String> arguments=new ArrayList<String>();
        int firstChar=0;
        int nextSpace=command.indexOf(' ');
        while (nextSpace!=-1) {
            arguments.add(command.substring(firstChar,nextSpace));
            firstChar=nextSpace;
            while (firstChar<command.length() && command.charAt(firstChar)==' ')
                firstChar++;
            nextSpace=command.indexOf(' ',firstChar);
        }
        arguments.add(command.substring(firstChar));
        String[] result=new String[arguments.size()];
        arguments.toArray(result);
        return result;
    }
    protected void showTextInWindow(String string,String title) {
        JTextArea textArea=new JTextArea(string);
        textArea.setFont(s_monospacedFont);
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
               m_mainFrame.toFront();
           }
        });
    }

    public void setTableau(Tableau tableau) {
        super.setTableau(tableau);
        m_tableau=tableau;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        super.isSatisfiableStarted(atomicConcept);
        if (m_singlestep) {
            m_output.println("Will check whether '"+atomicConcept.getURI()+"' is satisfiable.");
            mainLoop();
        }
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        super.isSatisfiableFinished(atomicConcept,result);
        m_output.println("'"+atomicConcept.getURI()+"' is "+(result ? " " : "not ")+"satisfiable.");
        mainLoop();
        dispose();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        super.isSubsumedByStarted(subconcept,superconcept);
        if (m_singlestep) {
            m_output.println("Will check whether '"+subconcept.getURI()+"' is subsumed by '"+superconcept.getURI()+"'.");
            mainLoop();
        }
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        super.isSubsumedByFinished(subconcept,superconcept,result);
        m_output.println("'"+subconcept.getURI()+"' is "+(result ? " " : "not ")+"subsumed by '"+superconcept.getURI()+"'.");
        mainLoop();
        dispose();
    }
    public void isABoxSatisfiableStarted() {
        super.isABoxSatisfiableStarted();
        if (m_singlestep) {
            m_output.println("Will check whether ABox is satisfiable.");
            mainLoop();
        }
    }
    public void isABoxSatisfiableFinished(boolean result) {
        super.isABoxSatisfiableFinished(result);
        m_output.println("ABox is "+(result ? " " : "not ")+"satisfiable...");
        mainLoop();
        dispose();
    }
    public void saturateStarted() {
        super.saturateStarted();
        m_currentIteration=0;
        if (m_singlestep) {
            m_output.println("Saturation starting...");
            mainLoop();
        }
    }
    public void iterationStarted() {
        super.iterationStarted();
        m_currentIteration++;
        if (m_singlestep) {
            m_output.println("Iteration "+m_currentIteration+" starts...");
            mainLoop();
        }
    }
    public void iterationFinished() {
        super.iterationFinished();
        if (System.currentTimeMillis()-m_lastStatusMark>m_breakpointTime) {
            printState();
            if (!m_forever)
                mainLoop();
            m_lastStatusMark=System.currentTimeMillis();
        }
    }
    public void clashDetected(Object[]... causes) {
        super.clashDetected(causes);
        if (m_waitOptions.contains(WaitOption.CLASH)) {
            m_forever=false;
            m_output.println("Clash detected.");
            mainLoop();
        }
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        super.existentialExpansionFinished(existentialConcept,forNode);
        if ((existentialConcept instanceof ExistsDescriptionGraph && m_waitOptions.contains(WaitOption.GRAPH_EXPANSION)) ||
            (existentialConcept instanceof AtLeastAbstractRoleConcept && m_waitOptions.contains(WaitOption.EXISTENTIAL_EXPANSION))) {
            m_forever=false;
            m_output.println(existentialConcept.toString(m_namespaces)+" expanded for node "+forNode.getNodeID());
            mainLoop();
        }
    }
    protected void printState() {
        int numberOfNodes=0;
        int blockedNodes=0;
        int nodesWithExistentials=0;
        int pendingExistentials=0;
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            numberOfNodes++;
            if (node.isBlocked())
                blockedNodes++;
            else {
                if (node.hasUnprocessedExistentials())
                    nodesWithExistentials++;
                pendingExistentials+=node.getUnprocessedExistentials().size();
            }
            node=node.getNextTableauNode();
        }
        m_output.println("Nodes: "+numberOfNodes+"  Blocked nodes: "+blockedNodes+"  Nodes with exists: "+nodesWithExistentials+"  Pending existentials: "+pendingExistentials);
    }
    
    protected static class FactComparator implements Comparator<Object[]> {
        public static final FactComparator INSTANCE=new FactComparator();

        public int compare(Object[] o1,Object[] o2) {
            int compare=o1.length-o2.length;
            if (compare!=0)
                return compare;
            compare=o1[0].toString().compareTo(o2[0].toString());
            if (compare!=0)
                return compare;
            for (int index=1;index<o1.length;index++) {
                compare=((Node)o1[index]).getNodeID()-((Node)o2[index]).getNodeID();
                if (compare!=0)
                    return compare;
            }
            return 0;
        }
    }
}