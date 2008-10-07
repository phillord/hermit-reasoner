package org.semanticweb.HermiT.tableau;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.DatatypeRestrictionLiteral.Facets;
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
    
    /**
     * Checks if in the last iteration a new data range assertion has been added 
     * and if so, whether the restrictions on this newly added node and its data 
     * range siblings are satisfiable given the asserted inequalities.  
     * @return true if the data range assertions are satisfiable and false 
     *         otherwise
     */
    public boolean checkDatatypeConstraints() {
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingStarted();
        }
        boolean datatypesSat = true;
        Set<DataRange> checkedDRs = new HashSet<DataRange>();
        Object[] pair = pairsDeltaOld.getTupleBuffer();
        pairsDeltaOld.open();
        while (datatypesSat && !pairsDeltaOld.afterLast()) {
            if (pair[0] instanceof DataRange && !checkedDRs.contains((DataRange) pair[0])) {
                // in the last saturation, we added a DataRange, so lets check 
                // whether this caused a clash
                Map<Node,Set<DataRange>> nodeToDRs = fetchRelevantAssertions((Node) pair[1]);
                datatypesSat = checkDatatypeAssertionFor(nodeToDRs);
                if (datatypesSat) {
                    // remember, which ranges we have already checked because 
                    // pairsDeltaOld can contain more than one data range 
                    // assertion and we check not just a found assertion, but 
                    // the all data range assertions for the node and its data 
                    // range siblings
                    for (Node node : nodeToDRs.keySet()) {
                        checkedDRs.addAll(nodeToDRs.get(node));
                    }
                }
            }
            pairsDeltaOld.next();
        }
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingFinished(datatypesSat);
        }
        return datatypesSat;
    }
    /**
     * Given a node that has a data range assertion in the extension table, 
     * look up its predecessor node in the tableau (unique for data range nodes) 
     * and collect all successors of the fond predecessor that have data range 
     * assertions. 
     * @param initialNode a node that has a data range assertion in the 
     *        extension table
     * @return A map that contains as keys the given node and its siblings from 
     *         the tableau that also have data range assertions. For each key n1 
     *         that mapping contains all data range assertion for n1 from the 
     *         extension table. 
     */
    protected Map<Node,Set<DataRange>> fetchRelevantAssertions(Node initialNode) {
        Map<Node,Set<DataRange>> nodeToDRs = new HashMap<Node,Set<DataRange>>();
        Set<DataRange> dataRanges;
        // find the predecessor of this node and collect all datatype 
        // successors that the predecessor has
        Object[] triple = triplesSecondBoundRetr.getTupleBuffer();
        triplesSecondBoundRetr.getBindingsBuffer()[2] = initialNode;
        triplesSecondBoundRetr.open();
        Node predecessor = (Node) triple[1];
        
        // collect all the data range successors for the parent node
        triple = triplesFirstBoundRetr.getTupleBuffer();
        triplesFirstBoundRetr.getBindingsBuffer()[1]=predecessor;
        triplesFirstBoundRetr.open();
        while (!triplesFirstBoundRetr.afterLast()) {
            DLPredicate predicate = (DLPredicate) triple[0];
            if (predicate instanceof AtomicRole 
                    && ((AtomicRole)predicate).isRestrictedToDatatypes()) {
                Node successor = (Node) triple[2];
                Object[] assertionsForSuccessor = pairsFirstBoundRetr.getTupleBuffer();
                pairsFirstBoundRetr.getBindingsBuffer()[1] = successor;
                pairsFirstBoundRetr.open();
                while (!pairsFirstBoundRetr.afterLast()) {
                    dataRanges = new HashSet<DataRange>();
                    dataRanges.add((DataRange) assertionsForSuccessor[0]);
                    if (nodeToDRs.containsKey(successor)) {
                        dataRanges.addAll(nodeToDRs.get(successor));
                    }
                    nodeToDRs.put(successor, dataRanges);
                    pairsFirstBoundRetr.next();
                }
            }
            triplesFirstBoundRetr.next();
        }
        return nodeToDRs;
    }
    /**
     * Given a map with nodes and data range assertions for the nodes, check 
     * whether there is an assignment for all the data ranges that is consistent 
     * with the restrictions on the data ranges and the inequalities
     * @param nodeToDRs a mapping from nodes to their asserted data ranges
     * @return true if a consistent assignment exists, false otherwise
     */
    protected boolean checkDatatypeAssertionFor(Map<Node,Set<DataRange>> nodeToDRs) {
        // test for trivial unsatisfiability:
        // Is there a dataRange assertion that is bottom?
        for (Node node : nodeToDRs.keySet()) {
            for (DataRange dataRange : nodeToDRs.get(node)) {
                if (dataRange.isBottom()) {
                    setClash(node, dataRange);
                    return false;
                }
            }
        }
        
        // collect the inequalities and aise clash when self inequality is found 
        Map<Node,Set<Node>> inequalities = new HashMap<Node,Set<Node>>();
        Map<Node,Set<Node>> inequalitiesSym = new HashMap<Node,Set<Node>>();
        boolean foundSelfInequality = fetchInequalities(nodeToDRs.keySet(), 
                        inequalities, inequalitiesSym);
        if (foundSelfInequality) return false; // clash has been raised
        
        // conjoin all data ranges for each node into a canonical range 
        // leave the original ranges unchanged for backtracking
        Map<Node, DataRange> nodeToCanonicalDR = buildCanonicalRanges(nodeToDRs);
        if (nodeToCanonicalDR == null) {
            // found a clash while joining the ranges, clash has been raised
            return false; 
        }
        
        // take out those that have more values than needed for the inequalities
        removeUnderRestrictedRanges(nodeToCanonicalDR, inequalities, inequalitiesSym);

        // now we have a set of data ranges that have not enough trivial 
        // assignments, so we really have to check whether we can find a 
        // suitable assignment for the allowed values
        // first we partition the nodes and their data ranges
        List<Node> nodesToBeProcessed = new ArrayList<Node>();
        Set<Map<DataRange, Node>> partitions = new HashSet<Map<DataRange, Node>>();
        Map<DataRange, Node> partition = new HashMap<DataRange, Node>();
        List<Node> remainingNodes = new ArrayList<Node>(nodeToCanonicalDR.keySet());
        while (!remainingNodes.isEmpty()) {
            Node node = remainingNodes.get(0);
            partition.put(nodeToCanonicalDR.get(node), node);
            remainingNodes.remove(node);
            if (inequalities.containsKey(node)) {
                nodesToBeProcessed.addAll(inequalities.get(node));
            }
            if (inequalitiesSym.containsKey(node)) {
                nodesToBeProcessed.addAll(inequalitiesSym.get(node));
            }
            while (!nodesToBeProcessed.isEmpty()) {
                Node currentNode = nodesToBeProcessed.get(0);
                if (nodeToCanonicalDR.containsKey(currentNode)) {
                    partition.put(nodeToCanonicalDR.get(currentNode), currentNode);
                    remainingNodes.remove(currentNode);
                    // put all remaining nodes that are inequal to this one into 
                    // the list of nodes to be processed, so that they go into 
                    // the same partition
                    for (Node n : remainingNodes) {
                        if (inequalities.get(currentNode) != null 
                                && inequalities.get(currentNode).contains(n)) {
                            nodesToBeProcessed.add(n);
                        }
                        if (inequalitiesSym.get(currentNode) != null 
                                && inequalitiesSym.get(currentNode).contains(n)) {
                            nodesToBeProcessed.add(n);
                        }
                    }
                }
                nodesToBeProcessed.remove(currentNode);
            }
            partitions.add(partition);
        }
        for (Map<DataRange, Node> p : partitions) {
            List<DataRange> ranges = new ArrayList<DataRange>(p.keySet());
            Collections.sort(ranges, SetLengthComparator.INSTANCE);
            Map<Node, String> nodeToValue = new HashMap<Node, String>();
            Node currentNode;
            for (DataRange currentRange : ranges) {
                currentNode = p.get(currentRange);
                String assignment = currentRange.getSmallestAssignment();
                if (assignment == null) {
                    // clash!
                    // collect the inequalities and the ranges that made up the 
                    // canonical data ranges in this partition and generate a 
                    // clash
                    int numberOfCauses = 0; 
                    if (inequalities.containsKey(currentNode)) {
                        numberOfCauses += inequalities.get(currentNode).size(); 
                    }
                    if (inequalitiesSym.containsKey(currentNode)) {
                        numberOfCauses += inequalitiesSym.get(currentNode).size(); 
                    }
                    // although we are checking the canonical ranges, all the 
                    // ranges that made up each canonical range contributes to 
                    // the clash and we need their dependency sets
                    for (Node node : p.values()) {
                        numberOfCauses += nodeToDRs.get(node).size();
                    }
                    UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                    Object[][] causes = new Object[numberOfCauses][];
                    int i = 0;
                    for (Node node : p.values()) {
                        for (DataRange range : nodeToDRs.get(node)) {
                            causes[i] = new Object[] { range, node };
                            ds.m_dependencySets[i] = extensionManager.getAssertionDependencySet(range, node);
                            i++;
                        }
                    }
                    if (inequalities.get(currentNode) != null) {
                        for (Node inequalNode : inequalities.get(currentNode)) {
                            causes[i] = new Object[] { Inequality.INSTANCE, currentNode, inequalNode };
                            ds.m_dependencySets[i] = extensionManager.getAssertionDependencySet(Inequality.INSTANCE, currentNode, inequalNode);
                            i++;
                        }
                    }
                    if (inequalitiesSym.get(currentNode) != null) {
                        for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                            causes[i] = new Object[] { Inequality.INSTANCE, inequalNode, currentNode };
                            ds.m_dependencySets[i] = extensionManager.getAssertionDependencySet(Inequality.INSTANCE, inequalNode, currentNode);
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
                if (inequalities.get(currentNode) != null) {
                    for (Node inequalNode : inequalities.get(currentNode)) {
                        if (nodeToCanonicalDR.containsKey(inequalNode)) {
                            DataRange r = nodeToCanonicalDR.get(inequalNode);
                            r.addNotOneOf(assignment);
                        }
                    }
                }
                if (inequalitiesSym.get(currentNode) != null) {
                    for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                        if (nodeToCanonicalDR.containsKey(inequalNode)) {
                            DataRange r = nodeToCanonicalDR.get(inequalNode);
                            r.addNotOneOf(assignment);
                        }
                    }
                }
            }
        }
        return true;
    }
    /**
     * Given a set of nodes for which there are data range assertions in the 
     * tableau, collect all inequalities for the nodes. If a self inequality is 
     * found (neq node node) a clash is raised and the method return true. 
     * @param nodes the set of nodes for which inequalities are collected
     * @param inequalities is a map that is initially assumed to be empty. 
     *        After running the method, it contains as keys all nodes n1 from 
     *        the given set nodes for which there is an inequality assertion of 
     *        the form neq n1 n2 in the extension table; the set of values 
     *        for each key n1 consist of all those nodes n2 for which neq n1 n2 
     *        is in the extension table. 
     * @param inequalitiesSym is a map that is initially assumed to be 
     *        empty. After running the method, it contains the symmetric 
     *        counterpart of inequalities. I.e., it contains as keys all 
     *        nodes n1 from the given set nodes for which there is an inequality 
     *        assertion of the form neq n2 n1 in the extension table; the set of 
     *        values for each key n1 consist of all those nodes n2 for which 
     *        neq n2 n1 is in the extension table. 
     * @return true if a tuple neq n1 n1 is found while fetching the 
     *         inequalities from the extension table and false otherwise
     */
    protected boolean fetchInequalities(Set<Node> nodes, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        Object[] triple = triplesZeroBoundRetr.getTupleBuffer();
        triplesZeroBoundRetr.getBindingsBuffer()[0]=Inequality.INSTANCE;
        triplesZeroBoundRetr.open();
        while (!triplesZeroBoundRetr.afterLast()) {
            Node node1 = (Node) triple[1];
            Node node2 = (Node) triple[2];
            if (node1.equals(node2)) {
                if (tableauMonitor != null) {
                    tableauMonitor.clashDetected(new Object[][] {
                            new Object[] { Inequality.INSTANCE, node1, node2 } });
                    tableauMonitor.datatypeCheckingFinished(false);
                }
                extensionManager.setClash(triplesZeroBoundRetr.getDependencySet());
                return true;
            }
            if (nodes.contains(node1)) {
                // found an inequality between datatype nodes
                // save inequalities for node1
                Set<Node> inequalNodes = new HashSet<Node>();
                inequalNodes.add(node2);
                // add also all already known inequalities
                if (inequalities.containsKey(node1)) {
                    inequalNodes.addAll(inequalities.get(node1));
                }
                inequalities.put(node1, inequalNodes);
                
                // save inequalities for node2
                inequalNodes = new HashSet<Node>();
                inequalNodes.add(node1);
                if (inequalitiesSym.containsKey(node2)) {
                    // add also all already known inequalities
                    inequalNodes.addAll(inequalitiesSym.get(node2));
                }
                inequalitiesSym.put(node2, inequalNodes);
            }
            triplesZeroBoundRetr.next();
        }
        return false;
    }
    
    /**
     * Given a map from nodes to a set of data ranges, construct a map from 
     * nodes to data ranges such that each constructed data range captures all 
     * restrictions of the data ranges in the set for the node in the given map. 
     * If any constructed canonical data range is equal to bottom, a clash is 
     * raised and null is returned. The canonical ranges have either oneOf 
     * values or facets (possibly empty) and notOneOfs, which are constants 
     * unsuitable for assignment (possibly empty). 
     * @param nodeToDRs a map from nodes to sets of data ranges 
     *        n -> {dr_1, ..., dr_n}
     * @return a map from nodes to data ranges n -> dr such that dr captures the 
     *         restrictions of {dr_1, ..., dr_n} or null if conjoining the 
     *         ranges lead to a clash (constructed range is bottom)
     */
    protected Map<Node, DataRange> buildCanonicalRanges(Map<Node, Set<DataRange>> nodeToDRs) {
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
                    ds.m_dependencySets[i] = extensionManager.getAssertionDependencySet(range, node);
                    i++;
                }
                if (tableauMonitor != null) {
                    tableauMonitor.clashDetected(causes);
                    tableauMonitor.datatypeCheckingFinished(false);
                }
                extensionManager.setClash(ds);
                return null;
            }
            nodeToCanonicalDR.put(node, canonicalRange);
        }
        return nodeToCanonicalDR;
    }
    
    /**
     * Given a set of data ranges, construct a data range that captures all the 
     * restrictions of the given ranges. 
     * @param ranges a set of data ranges
     * @return a data range that captures all restrictions from the data ranges 
     * in the given set ranges or null this is not possible due to different 
     * data types or contradiction oneOf values in the given ranges
     */
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
    
    /**
     * Remove those nodes and data ranges that have more than enough values to 
     * cover the inequalities. 
     * @param nodeToCanonicalDR a map from nodes to canonical data ranges
     * @param inequalities inequalities between nodes
     * @param inequalitiesSym symmetric counterpart for the above relation
     */
    protected void removeUnderRestrictedRanges(
            Map<Node, DataRange> nodeToCanonicalDR, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        boolean containedRemovable = true;
        Set<Node> removableNodes = new HashSet<Node>();
        while (containedRemovable) {
            containedRemovable = false;
            for (Node node : nodeToCanonicalDR.keySet()) {
                DataRange canonicalRange = nodeToCanonicalDR.get(node);
                int numInequalNodes = 0;
                if (inequalities.get(node) != null) {
                    numInequalNodes = inequalities.get(node).size();
                }
                if (inequalitiesSym.get(node) != null) {
                    numInequalNodes += inequalitiesSym.get(node).size();
                }
                if (canonicalRange.hasMinCardinality(numInequalNodes + 1)) {
                    removableNodes.add(node);
                    containedRemovable = true;
                }
            }
            for (Node node  : removableNodes) {
                nodeToCanonicalDR.remove(node);
            }
            removableNodes.clear();
        }
    }
    
    protected static class SetLengthComparator implements Comparator<DataRange> { 
        public static Comparator<DataRange> INSTANCE = new SetLengthComparator();
        public int compare(DataRange dr1, DataRange dr2) {
            return dr1.getEnumeration().size() - dr2.getEnumeration().size(); 
        }
    }
    protected void setClash(Node node, DataRange dataRange) {
        // gets called when dataRange is bottom
        if (tableauMonitor != null) {
            tableauMonitor.clashDetected(new Object[][] {
                    new Object[] { dataRange, node } });
            tableauMonitor.datatypeCheckingFinished(false);
        }
        extensionManager.setClash(extensionManager.getAssertionDependencySet(dataRange, node));
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