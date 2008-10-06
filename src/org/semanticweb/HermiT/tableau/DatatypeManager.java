package org.semanticweb.HermiT.tableau;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.DataRange.Facets;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DatatypeManager {
    protected final TableauMonitor tableauMonitor;
    protected final ExtensionManager extensionManager;
    protected final ExtensionTable.Retrieval pairsDeltaOld;
    protected final ExtensionTable.Retrieval triplesFirstBoundRetr;
    protected final ExtensionTable.Retrieval triplesSecondBoundRetr;
    protected final ExtensionTable.Retrieval pairsFirstBoundRetr;
    protected final ExtensionTable.Retrieval triplesZeroBoundRetr;
    protected final UnionDependencySet dependencySetTwoCauses;
    
    public DatatypeManager(Tableau tableau) {
        tableauMonitor = tableau.m_tableauMonitor;
        extensionManager = tableau.m_extensionManager;
        // retrieval object for all the datatype assertions in the changed part of the tuple table
        pairsDeltaOld = extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[2],ExtensionTable.View.DELTA_OLD);
        // retrieval object to fetch the parent node of a data range node
        triplesFirstBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false }, ExtensionTable.View.TOTAL);
        // retrieval object to fetch the parent node of a data range node
        triplesSecondBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true }, ExtensionTable.View.TOTAL);
        // retrieval object for all datatype assertions that use the same variable as one from the above retrieval
        pairsFirstBoundRetr = extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        // retrieval object for inequalities
        triplesZeroBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,false },ExtensionTable.View.TOTAL);
        dependencySetTwoCauses = new UnionDependencySet(2);
    }
    public void checkDatatypeConstraints() {
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingStarted();
        }
        boolean datatypesSat = true;
        Set<DataRange> checkedRanges = new HashSet<DataRange>();
        Object[] pair = pairsDeltaOld.getTupleBuffer();
        pairsDeltaOld.open();
        while (datatypesSat && !pairsDeltaOld.afterLast()) {
            if (pair[0] instanceof DataRange && !checkedRanges.contains((DataRange)pair[0])) {
                // in the last saturation, we added a DataRange, so lets check 
                // whether this caused a clash
                
                // find the predecessor of this node and collect all datatype 
                // successors that the predecessor has
                Object[] triple = triplesSecondBoundRetr.getTupleBuffer();
                triplesSecondBoundRetr.getBindingsBuffer()[2]=pair[1];
                triplesSecondBoundRetr.open();
                Node parent = (Node) triple[1];
                
                // collect all the data range successors for the parent node
                Map<Node,Set<DataRange>> nodeToDRs = new HashMap<Node,Set<DataRange>>();
                Map<DataRange,DependencySet> rangeToDS = new HashMap<DataRange, DependencySet>();
                Set<DataRange> dataRanges;
                triple = triplesFirstBoundRetr.getTupleBuffer();
                triplesFirstBoundRetr.getBindingsBuffer()[1]=parent;
                triplesFirstBoundRetr.open();
                while (!triplesFirstBoundRetr.afterLast()) {
                    DLPredicate predicate = (DLPredicate) triple[0];
                    if (predicate instanceof AtomicRole 
                            && ((AtomicRole)predicate).isRestrictedToDatatypes()) {
                        Node child = (Node) triple[2];
                        Object[] pair2 = pairsFirstBoundRetr.getTupleBuffer();
                        pairsFirstBoundRetr.getBindingsBuffer()[1]=child;
                        pairsFirstBoundRetr.open();
                        while (!pairsFirstBoundRetr.afterLast()) {
                            Node node = (Node) pair2[1];
                            DependencySet ds = pairsFirstBoundRetr.getDependencySet();
                            checkedRanges.add((DataRange) pair2[0]);
                            rangeToDS.put((DataRange) pair2[0], ds);
                            dataRanges = new HashSet<DataRange>();
                            dataRanges.add((DataRange) pair2[0]);
                            if (nodeToDRs.containsKey(node)) {
                                dataRanges.addAll(nodeToDRs.get(node));
                            }
                            nodeToDRs.put(node, dataRanges);
                            pairsFirstBoundRetr.next();
                        }
                        if (!nodeToDRs.isEmpty()) {
                            datatypesSat = checkDatatypeAssertionFor(nodeToDRs, rangeToDS);
                        }
                    }
                    triplesFirstBoundRetr.next();
                }
            }
            pairsDeltaOld.next();
        }
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingFinished(datatypesSat);
        }
    }
    protected boolean checkDatatypeAssertionFor(
            Map<Node,Set<DataRange>> nodeToDRs, 
            Map<DataRange, DependencySet> rangeToDS) {
        // test for trivial unsatisfiability
        // is node inequal to itself?
        // also count how many nodes are inequal to this one
        Object[] triple = triplesZeroBoundRetr.getTupleBuffer();
        triplesZeroBoundRetr.getBindingsBuffer()[0]=Inequality.INSTANCE;
        triplesZeroBoundRetr.open();
        Map<Node,Set<Node>> nodeInequalities = new HashMap<Node,Set<Node>>();
        Map<Node[],DependencySet> inequalNodestoDS = new HashMap<Node[],DependencySet>();
        Set<Node> inequalNodes;
        while (!triplesZeroBoundRetr.afterLast()) {
            Node node1 = (Node) triple[1];
            Node node2 = (Node) triple[2];
            if (node1.equals(node2)) {
                setClash(node1, node2, triplesZeroBoundRetr.getDependencySet());
                return false;
            }
            if (nodeToDRs.containsKey(node1)) {
                // found an inequality between datatype nodes
                // save inequalities for node1
                inequalNodes = new HashSet<Node>();
                inequalNodes.add(node2);
                // add also all already known inequalities
                if (nodeInequalities.containsKey(node1)) {
                    inequalNodes.addAll(nodeInequalities.get(node1));
                }
                nodeInequalities.put(node1, inequalNodes);
                // save the pair with its dependency set
                inequalNodestoDS.put(new Node[] { node1, node2 }, triplesZeroBoundRetr.getDependencySet());
                
                // save inequalities for node2
                inequalNodes = new HashSet<Node>();
                inequalNodes.add(node1);
                if (nodeInequalities.containsKey(node2)) {
                    // add also all already known inequalities
                    inequalNodes.addAll(nodeInequalities.get(node2));
                }
                nodeInequalities.put(node2, inequalNodes);
                // save the pair with its dependency set
                inequalNodestoDS.put(new Node[] { node2, node1 }, triplesZeroBoundRetr.getDependencySet());
            }
            triplesZeroBoundRetr.next();
        }
        // Is there a dataRange assertion that is bottom?
        for (Node node : nodeToDRs.keySet()) {
            for (DataRange dataRange : nodeToDRs.get(node)) {
                if (dataRange.isBottom()) {
                    setClash(node, dataRange, rangeToDS.get(dataRange));
                    return false;
                }
            }
        }
        // conjoin all data ranges for each node
        Map<Node, DataRange> nodeToCanonicalDR = new HashMap<Node, DataRange>();
        for (Node node : nodeToDRs.keySet()) {
            DataRange canonicalRange = getCanonicalDataRange(nodeToDRs.get(node));
            if (canonicalRange == null || canonicalRange.isBottom()) {
                // conjoining the restrictions led to a clash
                int numberOfCauses = nodeToDRs.get(node).size();
                UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                Object[][] causes = new Object[numberOfCauses][2];
                int i = 0;
                for (DataRange range : nodeToDRs.get(node)) {
                    causes[i][0] = range;
                    causes[i][1] = node;
                    ds.m_dependencySets[i] = rangeToDS.get(range);
                    i++;
                }
                if (tableauMonitor != null) {
                    tableauMonitor.clashDetected(causes);
                    tableauMonitor.datatypeCheckingFinished(false);
                }
                extensionManager.setClash(ds);
                return false;
            }
            nodeToCanonicalDR.put(node, canonicalRange);
        }
        // a canonical data range can have two forms:
        // either it contains an enumeration of the possible values and no 
        // facets
        // or it consists possibly a list of not accepted values (notOneOf) and 
        // possibly facets 
        // take out those that have more values than needed for the inequalities
        boolean containedRemovable = true;
        while (containedRemovable) {
            containedRemovable = false;
            for (Node node : nodeToCanonicalDR.keySet()) {
                DataRange canonicalRange = nodeToCanonicalDR.get(node);
                int numInequalNodes = 0;
                if (nodeInequalities.get(node) != null) {
                    numInequalNodes = nodeInequalities.get(node).size();
                }
                if (canonicalRange.hasMinCardinality(numInequalNodes + 1)) {
                    nodeToCanonicalDR.remove(node);
                    containedRemovable = true;
                }
            }
        }
        // now we have a set of data ranges that have not enough trivial 
        // assignments, so we really have to check whether we can find a 
        // suitable assignment for the allowed values
        // first we partition the nodes and their data ranges
        List<Node> nodesToBeProcessed = new ArrayList<Node>();
        Set<SortedMap<DataRange, Node>> partitions = new HashSet<SortedMap<DataRange, Node>>();
        SortedMap<DataRange, Node> partition = new TreeMap<DataRange, Node>(SetLengthComparator.INSTANCE);
        Set<Node> remainingNodes = nodeToCanonicalDR.keySet();
        for (Node node : remainingNodes) {
            partition.put(nodeToCanonicalDR.get(node), node);
            remainingNodes.remove(node);
            nodesToBeProcessed.addAll(nodeInequalities.get(node));
            while (!nodesToBeProcessed.isEmpty()) {
                Node currentNode = nodesToBeProcessed.get(0);
                if (nodeToCanonicalDR.containsKey(currentNode)) {
                    partition.put(nodeToCanonicalDR.get(currentNode), currentNode);
                    remainingNodes.remove(currentNode);
                    nodesToBeProcessed.addAll(nodeInequalities.get(currentNode));
                }
                nodesToBeProcessed.remove(currentNode);
            }
            partitions.add(partition);
        }
        for (SortedMap<DataRange, Node> p : partitions) {
            Map<Node, String> nodeToValue = new HashMap<Node, String>();
            Node currentNode;
            DataRange currentRange;
            Iterator<Entry<DataRange, Node>> it = p.entrySet().iterator();
            while (it.hasNext()) {
                Entry<DataRange, Node> entry = it.next();
                currentRange = entry.getKey();
                currentNode = entry.getValue();
                String assignment = currentRange.getSmallestAssignment();
                if (assignment == null) {
                    // clash!
                    // collect the inequalities and the ranges that made up the 
                    // canonical data ranges in this partition and generate a 
                    // clash
                    int numberOfCauses = nodeInequalities.get(currentNode).size(); 
                    for (Node node : p.values()) {
                        numberOfCauses += nodeToDRs.get(node).size();
                    }
                    UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                    Object[][] causes = new Object[numberOfCauses][];
                    int i = 0;
                    for (Node node : p.values()) {
                        for (DataRange range : nodeToDRs.get(node)) {
                            causes[i] = new Object[] { range, node };
                            ds.m_dependencySets[i] = rangeToDS.get(range);
                            i++;
                        }
                        for (Node inequalNode : nodeInequalities.get(node)) {
                            causes[i] = new Object[] { Inequality.INSTANCE, node, inequalNode };
                            ds.m_dependencySets[i] = inequalNodestoDS.get(new Node[] { node, inequalNode });
                            i++;
                        }
                    }
                    if (tableauMonitor != null) {
                        tableauMonitor.clashDetected(causes);
                        tableauMonitor.datatypeCheckingFinished(false);
                    }
                    extensionManager.setClash(ds);
                    return false;
                }
                nodeToValue.put(currentNode, assignment);
                for (Node inequalNode : nodeInequalities.get(currentNode)) {
                    if (nodeToCanonicalDR.containsKey(inequalNode)) {
                        DataRange r = nodeToCanonicalDR.get(inequalNode);
                        r.addNotOneOf(assignment);
                    }
                }
            }
        }
        return true;
    }
    protected boolean hasAssignment(SortedMap<DataRange, Node> partition) {
        Map<Node, String> nodeToValue = new HashMap<Node, String>();
        for (DataRange range : partition.keySet()) {
            nodeToValue.put(partition.get(range), range.getSmallestAssignment());
        }
        return true;
    }
    protected static class SetLengthComparator implements Comparator<DataRange> { 
        public static Comparator<DataRange> INSTANCE = new SetLengthComparator();
        public int compare(DataRange dr1, DataRange dr2) {
            return dr1.getEnumeration().size() - dr2.getEnumeration().size(); 
        }
    }
    protected void setClash(Node node, DataRange dataRange, DependencySet ds) {
        // gets called when dataRange is bottom
        if (tableauMonitor != null) {
            tableauMonitor.clashDetected(new Object[][] {
                    new Object[] { dataRange, node } });
            tableauMonitor.datatypeCheckingFinished(false);
        }
        extensionManager.setClash(ds);
    }
    protected void setClash(Node node1, Node node2, DependencySet dependencySet) {
        // gets called when nodes that are equal are also required to be inequal
        if (tableauMonitor != null) {
            tableauMonitor.clashDetected(new Object[][] {
                    new Object[] { Inequality.INSTANCE, node1, node2 } });
            tableauMonitor.datatypeCheckingFinished(false);
        }
        extensionManager.setClash(dependencySet);
    }
    protected DataRange getCanonicalDataRange(Set<DataRange> ranges) {
        // create a new instance of the type that the first given data range has
        DataRange canonicalDR = ranges.iterator().next().getNewInstance();
        // set the URI to the one of the first data range, we then expect all 
        // ranges to have the same URI or it is a clash
        URI uri = ((ranges.iterator()).next()).getDatatypeURI();
        // check if all restrictions are for the same datatype
        // for negated ranges with oneOfs, change them to unnegated ranges with 
        // inequality values
        for (DataRange range : ranges) {
            // check URI compatibility, e.g., cannot be string and integer at the same time
            if (!range.getDatatypeURI().equals(uri)) {
                return null;
            }
            // turn oneOf values for negated data ranges into iequalities for constants
            if (range.isNegated() && range.getOneOf().size() > 0) {
                range.setNotOneOf(range.getOneOf());
                range.setOneOf(new HashSet<String>());
                range.negate();
            }
        }
        
        // now we compute one canonical data range that captures the 
        // restrictions of all the data ranges for the node 
        
        // check if they have oneOf restrictions and if so, whether there is a 
        // value that suits all restrictions
        Set<String> suitableConstants;
        for (DataRange range : ranges) {
            if (range.getOneOf().size() > 0) {
                suitableConstants = range.getOneOf();
                for (String constant : suitableConstants) {
                    for (DataRange range2 : ranges) {
                        if (!range2.accepts(constant)) {
                            // no matching value for constant in the other data 
                            // ranges, so we can remove it
                            suitableConstants.remove(constant);
                            // if we take out the last value from our oneOf it 
                            // becomes bottom
                        }
                    }
                }
                if (suitableConstants.isEmpty()) return null;
                canonicalDR.setOneOf(suitableConstants);
                return canonicalDR;
            }
        }
        // none of the ranges has a positive oneOf restriction
        // conjoin all the facets and negative oneOf values in one restriction
        for (DataRange range : ranges) {
            canonicalDR.conjoinFacetsFrom(range);
            canonicalDR.addAllToNotOneOf(range.getNotOneOf());
        }
        return canonicalDR;
    }
    protected boolean isCompatible(DataRange dataRange1, DataRange dataRange2) {
        System.out.println(dataRange1 + " compatible with \n" + dataRange2);
        if (!dataRange1.isNegated()) {
            if (!dataRange2.isNegated()) {
                // neither range is negated
                if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                    // both are of the same type, e.g., integer
                    if (!dataRange1.getOneOf().isEmpty() 
                            && !dataRange2.getOneOf().isEmpty()) {
                        // both have some value restrictions, e.g., equals 18
                        for (String value : dataRange1.getOneOf()) {
                            // check if one of the equals values has a matching 
                            // one in the other restriction
                            if (dataRange2.getOneOf().contains(value)) {
                                return true;
                            }
                        }
                    } else if (dataRange1.supports(Facets.MIN_INCLUSIVE)) {
                        
                    } else {
                        // at least one of the restrictions has no further 
                        // equals restriction, so we are fine
                        return true;
                    }
                } 
            } else {
                // the second range is negated
                // if the negated one if top, we are screwed
                if (!dataRange2.isTop()) {
                    if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                        // the negated and the non-negated range are of the same 
                        // type, e.g., integer
                        if (!dataRange1.getOneOf().isEmpty() 
                                && !dataRange2.getOneOf().isEmpty()) {
                            // both have some restrictions, e.g., equals 18
                            for (String value : dataRange1.getOneOf()) {
                                // check if one of the equals values has no matching 
                                // one in the other restriction
                                if (!dataRange2.getOneOf().contains(value)) {
                                    return true;
                                }
                            }
                        } else {
                            // at least one of the restrictions has no further 
                            // equals restriction, so we are fine
                            return true;
                        }
                    } else {
                        // the negated type is a different data range, e.g., we have 
                        // some integer and not some string, which is fine
                        return true;
                    }
                }
            }
        } else {
            // if the negated one if top, we are screwed
            if (!dataRange1.isTop()) {
                if (!dataRange2.isNegated()) {
                    // only the first range is negated
                    if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                        // both are of the same type, e.g., integer
                        if (!dataRange1.getOneOf().isEmpty() 
                                && !dataRange2.getOneOf().isEmpty()) {
                            // both have some value restrictions, e.g., equals 18
                            for (String value : dataRange1.getOneOf()) {
                                // check if one of the negated equals values has no 
                                // matching one in the non-negated restriction
                                if (!dataRange2.getOneOf().contains(value)) {
                                    return true;
                                }
                            }
                        } else {
                            // at least one of the restrictions has no further 
                            // equals restriction, so we are fine
                            return true;
                        }
                    } else {
                        // the negated range has a different type, so we are fine
                        return true;
                    }
                } else {
                    // both ranges are negated
                    // if the negated one is top, we are screwed
                    if (!dataRange2.isTop()) {
                        // this is fine since we have infinitely many values to 
                        // choose from and an unknown data range
                        return true;
                    }
                }
            } 
        }
        System.out.println("Check failed!");
        return false;
    }
}