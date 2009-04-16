// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.debugger.commands.ActiveNodesCommand;
import org.semanticweb.HermiT.debugger.commands.AgainCommand;
import org.semanticweb.HermiT.debugger.commands.BreakpointTimeCommand;
import org.semanticweb.HermiT.debugger.commands.ClearCommand;
import org.semanticweb.HermiT.debugger.commands.ContinueCommand;
import org.semanticweb.HermiT.debugger.commands.DerivationTreeCommand;
import org.semanticweb.HermiT.debugger.commands.DiffLabelsCommand;
import org.semanticweb.HermiT.debugger.commands.ExitCommand;
import org.semanticweb.HermiT.debugger.commands.ForeverCommand;
import org.semanticweb.HermiT.debugger.commands.HelpCommand;
import org.semanticweb.HermiT.debugger.commands.HistoryCommand;
import org.semanticweb.HermiT.debugger.commands.DebuggerCommand;
import org.semanticweb.HermiT.debugger.commands.IsAncestorOfCommand;
import org.semanticweb.HermiT.debugger.commands.NodesForCommand;
import org.semanticweb.HermiT.debugger.commands.OriginStatsCommand;
import org.semanticweb.HermiT.debugger.commands.ModelStatsCommand;
import org.semanticweb.HermiT.debugger.commands.QueryCommand;
import org.semanticweb.HermiT.debugger.commands.ReuseNodeForCommand;
import org.semanticweb.HermiT.debugger.commands.SearchLabelCommand;
import org.semanticweb.HermiT.debugger.commands.SearchPairwiseBlockingCommand;
import org.semanticweb.HermiT.debugger.commands.ShowDLClausesCommand;
import org.semanticweb.HermiT.debugger.commands.ShowDescriptionGraphCommand;
import org.semanticweb.HermiT.debugger.commands.ShowExistsCommand;
import org.semanticweb.HermiT.debugger.commands.ShowModelCommand;
import org.semanticweb.HermiT.debugger.commands.ShowNodeCommand;
import org.semanticweb.HermiT.debugger.commands.ShowSubtreeCommand;
import org.semanticweb.HermiT.debugger.commands.SingleStepCommand;
import org.semanticweb.HermiT.debugger.commands.UnprocessedDisjunctionsCommand;
import org.semanticweb.HermiT.debugger.commands.WaitForCommand;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtMostGuard;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.NegationDataRange;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.DataValueEnumeration;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.ExistsDescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.monitor.TableauMonitorForwarder;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

public class Debugger extends TableauMonitorForwarder {
    private static final long serialVersionUID=-1061073966460686069L;

    public static final Font s_monospacedFont=new Font("Monospaced",Font.PLAIN,12);

    public static enum WaitOption {
        GRAPH_EXPANSION,EXISTENTIAL_EXPANSION,CLASH,MERGE,DATATYPE_CHECKING
    };

    protected final Map<String,DebuggerCommand> m_commandsByName;
    protected final Prefixes m_prefixes;
    protected final DerivationHistory m_derivationHistory;
    protected final ConsoleTextArea m_consoleTextArea;
    protected final JFrame m_mainFrame;
    protected final PrintWriter m_output;
    protected final BufferedReader m_input;
    protected final Set<WaitOption> m_waitOptions;
    protected final Map<Node,NodeCreationInfo> m_nodeCreationInfos;
    protected Node m_lastExistentialNode;
    protected ExistentialConcept m_lastExistentialConcept;
    protected Tableau m_tableau;
    protected String m_lastCommand;
    protected boolean m_forever;
    protected long m_lastStatusMark;
    protected boolean m_singlestep;
    protected boolean m_inMainLoop;
    protected int m_breakpointTime;
    protected int m_currentIteration;

    public Debugger(Prefixes prefixes,boolean historyOn) {
        super(new DerivationHistory());
        m_commandsByName=new TreeMap<String,DebuggerCommand>();
        registerCommands();
        m_prefixes=prefixes;
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
        m_nodeCreationInfos=new HashMap<Node,NodeCreationInfo>();
        m_forever=false;
        m_singlestep=false;
        m_breakpointTime=30000;
        m_mainFrame.setVisible(true);
        m_output.println("Good morning Dr. Chandra. This is HAL. I'm ready for my first lesson.");
        m_output.println("Derivation history is "+(m_forwardingOn ? "on" : "off")+".");
    }
    protected void registerCommands() {
        registerCommand(new HelpCommand(this));
        registerCommand(new AgainCommand(this));
        registerCommand(new ExitCommand(this));
        registerCommand(new ContinueCommand(this));
        registerCommand(new ClearCommand(this));
        registerCommand(new ForeverCommand(this));
        registerCommand(new HistoryCommand(this));
        registerCommand(new DerivationTreeCommand(this));
        registerCommand(new ActiveNodesCommand(this));
        registerCommand(new IsAncestorOfCommand(this));
        registerCommand(new UnprocessedDisjunctionsCommand(this));
        registerCommand(new ShowExistsCommand(this));
        registerCommand(new ShowModelCommand(this));
        registerCommand(new ShowDLClausesCommand(this));
        registerCommand(new ShowNodeCommand(this));
        registerCommand(new ShowDescriptionGraphCommand(this));
        registerCommand(new ShowSubtreeCommand(this));
        registerCommand(new QueryCommand(this));
        registerCommand(new SearchLabelCommand(this));
        registerCommand(new DiffLabelsCommand(this));
        registerCommand(new SearchPairwiseBlockingCommand(this));
        registerCommand(new ReuseNodeForCommand(this));
        registerCommand(new NodesForCommand(this));
        registerCommand(new OriginStatsCommand(this));
        registerCommand(new ModelStatsCommand(this));
        registerCommand(new SingleStepCommand(this));
        registerCommand(new BreakpointTimeCommand(this));
        registerCommand(new WaitForCommand(this));
    }
    protected void registerCommand(DebuggerCommand command) {
        m_commandsByName.put(command.getCommandName().toLowerCase(),command);
    }
    public Map<String,DebuggerCommand> getDebuggerCommands() {
        return Collections.unmodifiableMap(m_commandsByName);
    }
    public void dispose() {
        m_mainFrame.dispose();
        m_tableau=null;
    }
    public Tableau getTableau() {
        return m_tableau;
    }
    public PrintWriter getOutput() {
        return m_output;
    }
    public JFrame getMainFrame() {
        return m_mainFrame;
    }
    public String getLastCommand() {
        return m_lastCommand;
    }
    public ConsoleTextArea getConsoleTextArea() {
        return m_consoleTextArea;
    }
    public Prefixes getPrefixes() {
        return m_prefixes;
    }
    public DerivationHistory getDerivationHistory() {
        return m_derivationHistory;
    }
    public NodeCreationInfo getNodeCreationInfo(Node node) {
        NodeCreationInfo nodeCreationInfo=m_nodeCreationInfos.get(node);
        return nodeCreationInfo;
    }
    public void setBreakpointTime(int time) {
        m_breakpointTime=time;
    }
    public void setInMainLoop(boolean inMainLoop) {
        m_inMainLoop=inMainLoop;
    }
    public void setForever(boolean forever) {
        m_forever=forever;
    }
    public void setSinglestep(boolean singlestep) {
        m_singlestep=singlestep;
    }
    public boolean addWaitOption(WaitOption option) {
        return m_waitOptions.add(option);
    }
    public boolean removeWaitOption(WaitOption option) {
        return m_waitOptions.remove(option);
    }
    public DebuggerCommand getCommand(String commandName) {
        return m_commandsByName.get(commandName.toLowerCase());
    }
    public void mainLoop() {
        try {
            m_inMainLoop=true;
            while (m_inMainLoop) {
                m_output.print("> ");
                String commandLine=m_input.readLine();
                if (commandLine!=null) {
                    commandLine=commandLine.trim();
                    processCommandLine(commandLine);
                }
            }
            m_output.flush();
        }
        catch (IOException e) {
        }
        m_lastStatusMark=System.currentTimeMillis();
    }
    public void processCommandLine(String commandLine) {
        String[] parsedCommand=parse(commandLine);
        String commandName=parsedCommand[0];
        DebuggerCommand command=getCommand(commandName);
        if (command==null)
            m_output.println("Unknown command '"+commandName+"'.");
        else {
            command.execute(parsedCommand);
            if (!(command instanceof AgainCommand))
                m_lastCommand=commandLine;
        }
    }
    public DLPredicate getDLPredicate(String predicate) {
        if ("==".equals(predicate))
            return Equality.INSTANCE;
        else if ("!=".equals(predicate))
            return Inequality.INSTANCE;
        else if (predicate.startsWith("+"))
            return AtomicConcept.create(m_prefixes.expandAbbreviatedURI(predicate.substring(1)));
        else if (predicate.startsWith("-"))
            return AtomicRole.create(m_prefixes.expandAbbreviatedURI(predicate.substring(1)));
        else if (predicate.startsWith("$")) {
            String graphName=m_prefixes.expandAbbreviatedURI(predicate.substring(1));
            for (DescriptionGraph descriptionGraph : m_tableau.getDLOntology().getAllDescriptionGraphs())
                if (graphName.equals(descriptionGraph.getName()))
                    return descriptionGraph;
            return null;
        }
        else
            return null;
    }
    public void printNodeData(Node node,PrintWriter writer) {
        writer.print("Node ID:    ");
        writer.println(node.getNodeID());
        writer.print("Node Type:  ");
        writer.println(node.getNodeType());
        writer.print("Parent ID:  ");
        writer.println(node.getParent()==null ? "(root node)" : node.getParent().getNodeID());
        writer.print("Depth:      ");
        writer.println(node.getTreeDepth());
        writer.print("Status:     ");
        if (node.isActive())
            writer.println("active");
        else if (node.isMerged()) {
            Node mergeTarget=node.getMergedInto();
            while (mergeTarget!=null) {
                writer.print(" --> ");
                writer.print(mergeTarget.getNodeID());
                mergeTarget=mergeTarget.getMergedInto();
            }
            writer.println();
        }
        else
            writer.println("pruned");
        writer.print("Blocked:    ");
        writer.println(formatBlockingStatus(node));
        writer.print("Created as: ");
        NodeCreationInfo nodeCreationInfo=getNodeCreationInfo(node);
        ExistentialConcept startExistential=nodeCreationInfo.m_createdByExistential;
        if (!(startExistential instanceof AtLeastConcept))
            writer.println("(root)");
        else
            writer.println(((AtLeastConcept)startExistential).getToConcept().toString(m_prefixes));
        printConceptLabel(node,writer);
        printEdges(node,writer);
    }
    protected static String formatBlockingStatus(Node node) {
        if (!node.isBlocked())
            return "no";
        else if (node.isDirectlyBlocked())
            return "directly by "+(node.getBlocker()==Node.CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
        else
            return "indirectly by "+(node.getBlocker()==Node.CACHE_BLOCKER ? "signature in cache" : node.getBlocker().getNodeID());
    }
    public void printConceptLabel(Node node,PrintWriter writer) {
        TreeSet<AtomicConcept> atomicConcepts=new TreeSet<AtomicConcept>(ConceptComparator.INSTANCE);
        TreeSet<ExistentialConcept> existentialConcepts=new TreeSet<ExistentialConcept>(ConceptComparator.INSTANCE);
        TreeSet<AtomicNegationConcept> negativeConcepts=new TreeSet<AtomicNegationConcept>(ConceptComparator.INSTANCE);
        TreeSet<DataRange> dataRanges=new TreeSet<DataRange>(ConceptComparator.INSTANCE);
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object potentialConcept=retrieval.getTupleBuffer()[0];
            if (potentialConcept instanceof AtomicNegationConcept)
                negativeConcepts.add((AtomicNegationConcept)potentialConcept);
            else if (potentialConcept instanceof AtomicConcept)
                atomicConcepts.add((AtomicConcept)potentialConcept);
            else if (potentialConcept instanceof ExistentialConcept)
                existentialConcepts.add((ExistentialConcept)potentialConcept);
            else if (potentialConcept instanceof DataRange)
                dataRanges.add((DataRange)potentialConcept);
            else if (potentialConcept instanceof DescriptionGraph) {
                // ignore description graphs here
            }
            else
                throw new IllegalStateException("Found something in the label that is not a known type!");
            retrieval.next();
        }
        if (!atomicConcepts.isEmpty() || !existentialConcepts.isEmpty()) {
            writer.print("-- Positive concept label ------------------------");
            printConcepts(atomicConcepts,writer,3);
            printConcepts(existentialConcepts,writer,1);
        }
        if (!negativeConcepts.isEmpty()) {
            writer.print("-- Negative concept label ------------------------");
            printConcepts(negativeConcepts,writer,3);
        }
        if (!dataRanges.isEmpty()) {
            writer.print("-- Data ranges label ------------------------");
            printDataRanges(dataRanges,writer,1);
        }
    }
    public void printEdges(Node node,PrintWriter writer) {
        Map<Node,Set<AtomicRole>> outgoingEdges=new TreeMap<Node,Set<AtomicRole>>(NodeComparator.INSTANCE);
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=retrieval.getTupleBuffer()[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)retrieval.getTupleBuffer()[0];
                Node toNode=(Node)retrieval.getTupleBuffer()[2];
                Set<AtomicRole> set=outgoingEdges.get(toNode);
                if (set==null) {
                    set=new TreeSet<AtomicRole>(RoleComparator.INSTANCE);
                    outgoingEdges.put(toNode,set);
                }
                set.add(atomicRole);
            }
            retrieval.next();
        }
        if (!outgoingEdges.isEmpty()) {
            writer.println("-- Outgoing edges --------------------------------");
            printEdgeMap(outgoingEdges,writer);
        }
        Map<Node,Set<AtomicRole>> incomingEdges=new TreeMap<Node,Set<AtomicRole>>(NodeComparator.INSTANCE);
        retrieval=m_tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[2]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object atomicRoleObject=retrieval.getTupleBuffer()[0];
            if (atomicRoleObject instanceof AtomicRole) {
                AtomicRole atomicRole=(AtomicRole)retrieval.getTupleBuffer()[0];
                Node fromNode=(Node)retrieval.getTupleBuffer()[1];
                Set<AtomicRole> set=incomingEdges.get(fromNode);
                if (set==null) {
                    set=new TreeSet<AtomicRole>(RoleComparator.INSTANCE);
                    incomingEdges.put(fromNode,set);
                }
                set.add(atomicRole);
            }
            retrieval.next();
        }
        if (!incomingEdges.isEmpty()) {
            writer.println("-- Incoming edges --------------------------------");
            printEdgeMap(incomingEdges,writer);
        }
    }
    protected void printConcepts(Set<? extends Concept> set,PrintWriter writer,int numberInRow) {
        int number=0;
        for (Concept concept : set) {
            if (number!=0)
                writer.print(", ");
            if ((number%numberInRow)==0) {
                writer.println();
                writer.print("    ");
            }
            writer.print(concept.toString(m_prefixes));
            number++;
        }
        writer.println();
    }
    protected void printDataRanges(Set<? extends DataRange> set,PrintWriter writer,int numberInRow) {
        int number=0;
        for (DataRange range : set) {
            if (number!=0)
                writer.print(", ");
            if ((number%numberInRow)==0) {
                writer.println();
                writer.print("    ");
            }
            writer.print(range.toString(m_prefixes));
            number++;
        }
        writer.println();
    }
    protected void printEdgeMap(Map<Node,Set<AtomicRole>> map,PrintWriter writer) {
        for (Map.Entry<Node,Set<AtomicRole>> entry : map.entrySet()) {
            writer.print("    ");
            writer.print(entry.getKey().getNodeID());
            writer.print(" -->");
            int number=0;
            for (AtomicRole atomicRole : entry.getValue()) {
                if (number!=0)
                    writer.print(", ");
                if ((number%3)==0) {
                    writer.println();
                    writer.print("        ");
                }
                writer.print(atomicRole.toString(m_prefixes));
                number++;
            }
            writer.println();
        }
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
    protected void printState() {
        int numberOfNodes=0;
        int inactiveNodes=0;
        int blockedNodes=0;
        int nodesWithExistentials=0;
        int pendingExistentials=0;
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            numberOfNodes++;
            if (!node.isActive())
                inactiveNodes++;
            else if (node.isBlocked())
                blockedNodes++;
            else {
                if (node.hasUnprocessedExistentials())
                    nodesWithExistentials++;
                pendingExistentials+=node.getUnprocessedExistentials().size();
            }
            node=node.getNextTableauNode();
        }
        m_output.println("Nodes: "+numberOfNodes+"  Inactive nodes: "+inactiveNodes+"  Blocked nodes: "+blockedNodes+"  Nodes with exists: "+nodesWithExistentials+"  Pending existentials: "+pendingExistentials);
    }

    public void setTableau(Tableau tableau) {
        super.setTableau(tableau);
        m_tableau=tableau;
    }
    public void isSatisfiableStarted(AtomicConcept atomicConcept) {
        super.isSatisfiableStarted(atomicConcept);
        m_output.println("Will check whether '"+m_prefixes.abbreviateURI(atomicConcept.getURI())+"' is satisfiable.");
        mainLoop();
    }
    public void isSatisfiableFinished(AtomicConcept atomicConcept,boolean result) {
        super.isSatisfiableFinished(atomicConcept,result);
        m_output.println("'"+m_prefixes.abbreviateURI(atomicConcept.getURI())+"' is "+(result ? "" : "not ")+"satisfiable.");
        mainLoop();
        dispose();
    }
    public void isSubsumedByStarted(AtomicConcept subconcept,AtomicConcept superconcept) {
        super.isSubsumedByStarted(subconcept,superconcept);
        m_output.println("Will check whether '"+m_prefixes.abbreviateURI(subconcept.getURI())+"' is subsumed by '"+m_prefixes.abbreviateURI(superconcept.getURI())+"'.");
        mainLoop();
    }
    public void isSubsumedByFinished(AtomicConcept subconcept,AtomicConcept superconcept,boolean result) {
        super.isSubsumedByFinished(subconcept,superconcept,result);
        m_output.println("'"+m_prefixes.abbreviateURI(subconcept.getURI())+"' is "+(result ? "" : "not ")+"subsumed by '"+m_prefixes.abbreviateURI(superconcept.getURI())+"'.");
        mainLoop();
        dispose();
    }
    public void isABoxSatisfiableStarted() {
        super.isABoxSatisfiableStarted();
        m_output.println("Will check whether ABox is satisfiable.");
        mainLoop();
    }
    public void isABoxSatisfiableFinished(boolean result) {
        super.isABoxSatisfiableFinished(result);
        m_output.println("ABox is "+(result ? "" : "not ")+"satisfiable.");
        mainLoop();
        dispose();
    }
    public void isInstanceOfStarted(AtomicConcept concept,Individual individual) {
        super.isInstanceOfStarted(concept,individual);
        m_output.println("Will check whether '"+m_prefixes.abbreviateURI(concept.getURI())+"' is an instance of '"+m_prefixes.abbreviateURI(individual.getURI())+"'.");
        mainLoop();
    }
    public void isInstanceOfFinished(AtomicConcept concept,Individual individual,boolean result) {
        super.isInstanceOfFinished(concept,individual,result);
        m_output.println("'"+m_prefixes.abbreviateURI(concept.getURI())+"' is "+(result ? "" : "not ")+"an instance of '"+m_prefixes.abbreviateURI(individual.getURI())+"'.");
        mainLoop();
        dispose();
    }
    public void tableauCleared() {
        super.tableauCleared();
        m_nodeCreationInfos.clear();
        m_lastExistentialNode=null;
        m_lastExistentialConcept=null;
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
        if (m_singlestep) {
            m_output.println("Iteration "+m_currentIteration+" finished...");
        }
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
    public void mergeStarted(Node mergeFrom,Node mergeInto) {
        super.mergeStarted(mergeFrom,mergeInto);
        if (m_waitOptions.contains(WaitOption.MERGE)) {
            m_forever=false;
            m_output.println("Node '"+mergeFrom.getNodeID()+"' will be merged into node '"+mergeInto.getNodeID()+"'.");
            mainLoop();
        }
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        super.existentialExpansionStarted(existentialConcept,forNode);
        m_lastExistentialNode=forNode;
        m_lastExistentialConcept=existentialConcept;
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        super.existentialExpansionFinished(existentialConcept,forNode);
        m_lastExistentialNode=null;
        m_lastExistentialConcept=null;
        if ((existentialConcept instanceof ExistsDescriptionGraph && m_waitOptions.contains(WaitOption.GRAPH_EXPANSION)) || (existentialConcept instanceof AtLeastConcept && m_waitOptions.contains(WaitOption.EXISTENTIAL_EXPANSION))) {
            m_forever=false;
            m_output.println(existentialConcept.toString(m_prefixes)+" expanded for node "+forNode.getNodeID());
            mainLoop();
        }
    }
    public void nodeCreated(Node node) {
        super.nodeCreated(node);
        m_nodeCreationInfos.put(node,new NodeCreationInfo(node,m_lastExistentialNode,m_lastExistentialConcept));
        if (m_lastExistentialNode!=null)
            m_nodeCreationInfos.get(m_lastExistentialNode).m_children.add(node);
    }
    public void nodeDestroyed(Node node) {
        super.nodeDestroyed(node);
        NodeCreationInfo nodeCreationInfo=m_nodeCreationInfos.remove(node);
        if (nodeCreationInfo.m_createdByNode!=null)
            m_nodeCreationInfos.get(nodeCreationInfo.m_createdByNode).m_children.remove(node);
    }
    public void datatypeCheckingStarted() {
        super.datatypeCheckingStarted();
        if (m_waitOptions.contains(WaitOption.DATATYPE_CHECKING)) {
            m_forever=false;
            m_output.println("Will check whether the datatype constraints are satisfiable.");
            mainLoop();
        }
    }

    public static class FactComparator implements Comparator<Object[]> {
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

    protected static class RoleComparator implements Comparator<Role> {
        public static final RoleComparator INSTANCE=new RoleComparator();

        public int compare(Role ar1,Role ar2) {
            int type1=getRoleType(ar1);
            int type2=getRoleType(ar2);
            if (type1!=type2)
                return type1-type2;
            if (type1==0)
                return ((AtomicRole)ar1).getURI().compareTo(((AtomicRole)ar2).getURI());
            else
                return ((InverseRole)ar1).getInverseOf().getURI().compareTo(((InverseRole)ar2).getInverseOf().getURI());
        }
        protected int getRoleType(Role ar) {
            if (ar instanceof AtomicRole)
                return 0;
            else
                return 1;
        }
    }

    public static class ConceptComparator implements Comparator<Concept> {
        public static final ConceptComparator INSTANCE=new ConceptComparator();

        public int compare(Concept c1,Concept c2) {
            int type1=getConceptType(c1);
            int type2=getConceptType(c2);
            if (type1!=type2)
                return type1-type2;
            switch (type1) {
            case 0:
                return ((AtomicConcept)c1).getURI().compareTo(((AtomicConcept)c2).getURI());
            case 1:
                {
                    AtMostGuard g1=(AtMostGuard)c1;
                    AtMostGuard g2=(AtMostGuard)c2;
                    int comparison=RoleComparator.INSTANCE.compare(g1.getOnRole(),g2.getOnRole());
                    if (comparison!=0)
                        return comparison;
                    return compare(g1.getToAtomicConcept(),g2.getToAtomicConcept());
                }
            case 2:
                {
                    AtLeastConcept l1=(AtLeastConcept)c1;
                    AtLeastConcept l2=(AtLeastConcept)c2;
                    int comparison=RoleComparator.INSTANCE.compare(l1.getOnRole(),l2.getOnRole());
                    if (comparison!=0)
                        return comparison;
                    return compare(l1.getToConcept(),l2.getToConcept());
                }
            case 3:
                {
                    ExistsDescriptionGraph g1=(ExistsDescriptionGraph)c1;
                    ExistsDescriptionGraph g2=(ExistsDescriptionGraph)c2;
                    return g1.getDescriptionGraph().getName().compareTo(g2.getDescriptionGraph().getName());
                }
            case 4:
                return ((AtomicNegationConcept)c1).getNegatedAtomicConcept().getURI().compareTo(((AtomicNegationConcept)c2).getNegatedAtomicConcept().getURI());
            case 5:
                return compareDatatypeRestrictions((DatatypeRestriction)c1,(DatatypeRestriction)c2);
            case 6:
                return compareDataValueEnumerations((DataValueEnumeration)c1,(DataValueEnumeration)c2);
            case 7:
                {
                    NegationDataRange ndr1=(NegationDataRange)c1;
                    NegationDataRange ndr2=(NegationDataRange)c2;
                    return compare(ndr1.getNegatedDataRange(),ndr2.getNegatedDataRange());
                }
            default:
                throw new IllegalArgumentException();
            }
        }
        protected int getConceptType(Concept c) {
            if (c instanceof AtMostGuard)
                return 1;
            else if (c instanceof AtomicConcept)
                return 0;
            else if (c instanceof AtLeastConcept)
                return 2;
            else if (c instanceof ExistsDescriptionGraph)
                return 3;
            else if (c instanceof AtomicNegationConcept)
                return 4;
            else if (c instanceof DatatypeRestriction)
                return 5;
            else if (c instanceof DataValueEnumeration)
                return 6;
            else if (c instanceof NegationDataRange)
                return 7;
            else
                throw new IllegalArgumentException();
        }
        protected int compareDatatypeRestrictions(DatatypeRestriction dr1,DatatypeRestriction dr2) {
            int comparison=dr1.getDatatypeURI().compareTo(dr2.getDatatypeURI());
            if (comparison!=0)
                return comparison;
            comparison=dr1.getNumberOfFacetRestrictions()-dr2.getNumberOfFacetRestrictions();
            if (comparison!=0)
                return comparison;
            for (int index=0;index<dr1.getNumberOfFacetRestrictions();index++) {
                comparison=dr1.getFacetURI(index).compareTo(dr2.getFacetURI(index));
                if (comparison!=0)
                    return comparison;
                comparison=compareDataValues(dr1.getFacetValue(index),dr2.getFacetValue(index));
                if (comparison!=0)
                    return comparison;
            }
            return 0;
        }
        protected int compareDataValueEnumerations(DataValueEnumeration dve1,DataValueEnumeration dve2) {
            int comparison=dve1.getNumberOfDataValues()-dve2.getNumberOfDataValues();
            if (comparison!=0)
                return comparison;
            for (int index=0;index<dve1.getNumberOfDataValues();index++) {
                comparison=compareDataValues(dve1.getDataValue(index),dve2.getDataValue(index));
                if (comparison!=0)
                    return comparison;
            }
            return 0;
            
        }
        protected int compareDataValues(Object dv1,Object dv2) {
            return dv1.toString().compareTo(dv2.toString());
        }
    }

    public static class NodeComparator implements Comparator<Node> {
        public static final NodeComparator INSTANCE=new NodeComparator();

        public int compare(Node o1,Node o2) {
            return o1.getNodeID()-o2.getNodeID();
        }
    }

    public static class NodeCreationInfo {
        public final Node m_node;
        public final Node m_createdByNode;
        public final ExistentialConcept m_createdByExistential;
        public final List<Node> m_children;

        public NodeCreationInfo(Node node,Node createdByNode,ExistentialConcept createdByExistential) {
            m_node=node;
            m_createdByNode=createdByNode;
            m_createdByExistential=createdByExistential;
            m_children=new ArrayList<Node>(4);
        }
    }
}
