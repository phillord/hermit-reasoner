package org.semanticweb.HermiT.tableau;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionLiteral;
import org.semanticweb.HermiT.model.dataranges.EnumeratedDataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DatatypeManager {
    protected final TableauMonitor tableauMonitor;
    protected final ExtensionManager extensionManager;
    protected final ExtensionTable.Retrieval pairsDeltaOld;
    protected final ExtensionTable.Retrieval triplesDeltaOld;
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
        // retrieval object for all the inequality assertions in the changed part of the tuple table
        triplesDeltaOld = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,false },ExtensionTable.View.DELTA_OLD);
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
     * range siblings are satisfiable given the asserted inequalities. If not, a 
     * clash is set in the extensionManager an the tableauMonitor (if not null) 
     * is notified accordingly. 
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
            if (pair[0] instanceof DataRange) { //&& !checkedDRs.contains((DataRange) pair[0])) {
                // in the last saturation, we added a DataRange, so lets check 
                // whether this caused a clash
                Map<Node, Set<Node>> inequalities = new HashMap<Node, Set<Node>>();
                inequalities.put((Node) pair[1], new HashSet<Node>());
                Map<Node, Set<Node>> inequalitiesSym = new HashMap<Node, Set<Node>>();
                inequalitiesSym.put((Node) pair[1], new HashSet<Node>());
                boolean foundSelfInequality = fetchRelevantNodes(inequalities, inequalitiesSym);
                if (foundSelfInequality) return false;
                
                Map<Node,Set<DataRange>> nodeToDRs = fetchRelevantDataRanges(inequalities.keySet());
                datatypesSat = checkDatatypeAssertionFor(nodeToDRs, inequalities, inequalitiesSym);
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
        Object[] triple = triplesDeltaOld.getTupleBuffer();
        triplesDeltaOld.getBindingsBuffer()[0]=Inequality.INSTANCE;
        triplesDeltaOld.open();
        while (datatypesSat && !triplesDeltaOld.afterLast()) {
            if (((Node) triple[1]).m_nodeType == NodeType.CONCRETE_NODE 
                    && ((Node) triple[1]).m_nodeType == NodeType.CONCRETE_NODE) { 
                // in the last saturation, we added an inequality between 
                // concrete nodes, so lets check whether this caused a clash
                Map<Node, Set<Node>> inequalities = new HashMap<Node, Set<Node>>();
                inequalities.put((Node) triple[1], new HashSet<Node>());
                inequalities.put((Node) triple[2], new HashSet<Node>());
                Map<Node, Set<Node>> inequalitiesSym = new HashMap<Node, Set<Node>>();
                inequalitiesSym.put((Node) triple[1], new HashSet<Node>());
                inequalitiesSym.put((Node) triple[2], new HashSet<Node>());
                boolean foundSelfInequality = fetchRelevantNodes(inequalities, inequalitiesSym);
                if (foundSelfInequality) return false;
                
                Map<Node,Set<DataRange>> nodeToDRs = fetchRelevantDataRanges(inequalities.keySet());
                datatypesSat = checkDatatypeAssertionFor(nodeToDRs, inequalities, inequalitiesSym);
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
            triplesDeltaOld.next();
        }
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingFinished(datatypesSat);
        }
        return datatypesSat;
    }
    
    /**
     * Input are 2 maps with the nodes as keys and empty sets as values for 
     * which we want to collect the inequalities. Afterwards the sets have keys 
     * for all nodes that are linked to the input nodes via an inequality 
     * (considering it is symmetric). The values in inequalities contain the 
     * inequalities (not treating inequality as symmetric) and the values in 
     * inequalitiesSym contain the inverse inequalities. this is needed because 
     * when a clash is found later on, we need to reconstruct whether the 
     * extension table contains neq n1 n2 or neq n2 n1. 
     * If a self inequality is found (neq node node) a clash is raised and the 
     * method return true. 
     * @param inequalities is a map that is initially assumed to have all nodes 
     *        we are interested in as keys but with empty sets as values. 
     *        Whenever an inequality is found, the inequal nodes are added to 
     *        the set and inequalities are collected for them as well. 
     * @param inequalitiesSym is a map that is initially assumed to have all 
     *        nodes we are interested in as keys but with empty sets as values. 
     *        Whenever an inequality is found with one of the keys in the third 
     *        position, the inequalities is collected. 
     * @return true if a tuple neq n1 n1 is found while fetching the 
     *         inequalities from the extension table and false otherwise
     */
    protected boolean fetchRelevantNodes(
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesInv) {
        Object[] triple = triplesZeroBoundRetr.getTupleBuffer();
        triplesZeroBoundRetr.getBindingsBuffer()[0]=Inequality.INSTANCE;
        boolean fixedPointReached = false;
        
        while(!fixedPointReached) {
            triplesZeroBoundRetr.open();
            fixedPointReached = true;
            while (!triplesZeroBoundRetr.afterLast()) {
                Node node1 = (Node) triple[1];
                Node node2 = (Node) triple[2];
                // see if it is relevant
                if (node1.m_nodeType == NodeType.CONCRETE_NODE 
                        && ((inequalities.containsKey(node1) 
                                && !inequalities.get(node1).contains(node2)) 
                                || (inequalities.containsKey(node2) 
                                        && !inequalities.get(node2).contains(node1)))) {
                    
                    // self inequality found
                    if (node1.equals(node2)) {
                        if (tableauMonitor != null) {
                            tableauMonitor.clashDetected(new Object[][] {
                                    new Object[] { Inequality.INSTANCE, node1, node2 } });
                            tableauMonitor.datatypeCheckingFinished(false);
                        }
                        extensionManager.setClash(triplesZeroBoundRetr.getDependencySet());
                        return true;
                    }
                    if (!inequalities.containsKey(node1)) {
                        inequalities.put(node1, new HashSet<Node>());
                        inequalitiesInv.put(node1, new HashSet<Node>());
                    }
                    if (!inequalities.containsKey(node2)) {
                        inequalities.put(node2, new HashSet<Node>());
                        inequalitiesInv.put(node2, new HashSet<Node>());
                    }
                    // record the inequality
                    if (!inequalities.get(node1).contains(node2)) {
                        fixedPointReached = false;
                        Set<Node> inequalNodes = inequalities.get(node1);
                        inequalNodes.add(node2);
                        inequalities.put(node1, inequalNodes);
                    }
                    // record the inverse
                    if (!inequalitiesInv.get(node2).contains(node1)) {
                        fixedPointReached = false;
                        Set<Node> inequalNodes = inequalitiesInv.get(node2);
                        inequalNodes.add(node1);
                        inequalitiesInv.put(node2, inequalNodes);
                    }
                }
                triplesZeroBoundRetr.next();
            }
        }
        return false; // no self inequality
    }
    
    /**
     * Given a set of nodes, collect all data ranges from the extension table. 
     * @param nodes a set of nodes for which data range assertions are to be 
     *              collected from the extension table
     * @return A map that contains as keys the given nodes and, for each key n1, 
     *         the map contains all data range assertion for n1 from the 
     *         extension table. 
     */
    protected Map<Node,Set<DataRange>> fetchRelevantDataRanges(Set<Node> nodes) {
        Map<Node,Set<DataRange>> nodeToDRs = new HashMap<Node,Set<DataRange>>();
        Set<DataRange> dataRanges;

        for (Node node : nodes) {
            Object[] DRsForNode = pairsFirstBoundRetr.getTupleBuffer();
            pairsFirstBoundRetr.getBindingsBuffer()[1] = node;
            pairsFirstBoundRetr.open();
            dataRanges = new HashSet<DataRange>();
            while (!pairsFirstBoundRetr.afterLast()) {
                dataRanges.add((DataRange) DRsForNode[0]);
                pairsFirstBoundRetr.next();
            }
            nodeToDRs.put(node, dataRanges);
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
    protected boolean checkDatatypeAssertionFor(
            Map<Node,Set<DataRange>> nodeToDRs, 
            Map<Node,Set<Node>> inequalities, 
            Map<Node,Set<Node>> inequalitiesSym) {
        // test for trivial unsatisfiability:
        // Is there a dataRange assertion that is bottom?
        for (Node node : nodeToDRs.keySet()) {
            for (DataRange dataRange : nodeToDRs.get(node)) {
                if (dataRange.isBottom()) {
                    if (tableauMonitor != null) {
                        tableauMonitor.clashDetected(new Object[][] {
                                new Object[] { dataRange, node } });
                        tableauMonitor.datatypeCheckingFinished(false);
                    }
                    extensionManager.setClash(
                            extensionManager.getAssertionDependencySet(dataRange, node));
                    return false; 
                }
            }
        }

        // conjoin all data ranges for each node into a canonical range 
        // leave the original ranges unchanged for backtracking
        List<Pair> CDRsToNodes = buildCanonicalRanges(nodeToDRs);
        if (CDRsToNodes == null) {
            // found a clash while joining the ranges, clash has been raised
            return false; 
        }
        
        // take out those that have more values than needed for the inequalities
        removeUnderRestrictedRanges(CDRsToNodes, inequalities, inequalitiesSym);

        boolean hasAssignment = hasAssignment(CDRsToNodes, 
                    inequalities,  
                    inequalitiesSym, 
                    nodeToDRs); 
        if (!hasAssignment) {
            return false;
        }
        return true;
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
    protected List<Pair> buildCanonicalRanges(Map<Node, Set<DataRange>> nodeToDRs) {
        List<Pair> CDRsToNodes = new ArrayList<Pair>();
        for (Node node : nodeToDRs.keySet()) {
            CanonicalDataRange canonicalRange;
            if (nodeToDRs.get(node).size() > 1) {
                canonicalRange = getCanonicalDataRange(nodeToDRs.get(node));
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
            } else {
                canonicalRange = (CanonicalDataRange) nodeToDRs.get(node).iterator().next();
            }
            CDRsToNodes.add(new Pair(canonicalRange, node));
        }
        return CDRsToNodes;
    }
    
    /**
     * Given a set of data ranges, construct a data range that captures all the 
     * restrictions of the given ranges. 
     * @param ranges a set of data ranges
     * @return a data range that captures all restrictions from the data ranges 
     *         in the given set ranges or null this is not possible due to 
     *         different data types or contradiction oneOf values in the given 
     *         ranges
     */
    protected CanonicalDataRange getCanonicalDataRange(Set<DataRange> ranges) {
        // create a new instance of the type that the first positive data range has
        CanonicalDataRange canonicalDR = null;
        // set the URI to the one of the first positive data range, we then 
        // expect all ranges to have the same URI or it is a clash
        boolean onlyNegated = true;
        Set<DataConstant> constants = null;
        Set<DataConstant> forbiddenConstants = new HashSet<DataConstant>();
        for (DataRange range : ranges) {
            if (!range.isNegated()) {
                onlyNegated = false;
                if (range.getDatatype() != null) {
                    // found a datatype restriction, which cannot have oneOfs
                    if (canonicalDR == null) {
                        canonicalDR = range.getNewInstance();
                    } else {
                        if (DT.isSubOf(range.getDatatype(), canonicalDR.getDatatype())) {
                            // found a more specific implementation, 
                            // E.g., the canonical range is implemented with 
                            // decimals, but the new range is only for integers. 
                            // Hence we should use the more restrictive integer 
                            // range as the canonical range.
                            canonicalDR = range.getNewInstance();
                        } else if (range.getDatatype() != DT.LITERAL && !canonicalDR.canHandle(range.getDatatype())) {
                            // found an incompatibility
                            return null;
                        }
                    }
                    forbiddenConstants.addAll(range.getNotOneOf());
                } else {
                    // not a datatype restriction, so it is an enumerated range
                    if (constants == null) {
                        constants = range.getOneOf();
                    } else {
                        constants.retainAll(range.getOneOf());
                    }
                }
            } else {
                if (!range.getOneOf().isEmpty()) {
                    // range has notOneOfs
                    forbiddenConstants.addAll(range.getOneOf());
                }
            }
        }
        if (onlyNegated) {
            // only negated ranges -> trivially satisfiable
            return new DatatypeRestrictionLiteral(DT.LITERAL);
        }
        if (constants != null) {
            constants.removeAll(forbiddenConstants);
            if (constants.isEmpty()) {
                // we had oneOfs, but they were not compatible
                return null;
            }
        }
        if (canonicalDR == null) {
            // all ranges are enumerated ones
            if (constants == null) return null;
            canonicalDR = new EnumeratedDataRange();
            canonicalDR.setOneOf(constants);
            return canonicalDR;
        } else {
            // if there are oneOf restrictions, check whether there are values 
            // that suit the datatype restriction
            if (constants != null) {
                Set<DataConstant> unsuitable = new HashSet<DataConstant>();
                for (DataConstant constant : constants) {
                    if (!canonicalDR.datatypeAccepts(constant)) {
                        unsuitable.add(constant);
                    }
                }
                constants.removeAll(unsuitable);
                if (constants.isEmpty()) return null;
            }
            if (forbiddenConstants != null) {
                Set<DataConstant> irrelevant = new HashSet<DataConstant>();
                for (DataConstant constant : forbiddenConstants) {
                    if (!canonicalDR.datatypeAccepts(constant)) {
                        irrelevant.add(constant);
                    }
                }
                forbiddenConstants.removeAll(irrelevant);
            }
        }
            
        // now we compute one canonical data range that captures the 
        // restrictions of all the data ranges for the node 

        // lets look at the facets
        for (DataRange range : ranges) {
            if (range.getDatatypeURI() != null 
                    && range.getDatatype() != DT.LITERAL) {
                    // literal ranges have no facets
                canonicalDR.conjoinFacetsFrom(range);
            }
        }
        
        // if we have oneOfs, we make sure they conform to the facets
        if (constants != null) {
            Set<DataConstant> unsuitable = new HashSet<DataConstant>();
            for (DataConstant constant : constants) {
                if (!canonicalDR.accepts(constant)) {
                    unsuitable.add(constant);
                }
            }
            constants.removeAll(unsuitable);
            if (constants.isEmpty()) {
                // no more suitable values left
                return null;
            }
            canonicalDR.setOneOf(constants);
            return canonicalDR;
        } else {
            canonicalDR.setNotOneOf(forbiddenConstants);
            return canonicalDR;
        }
    }
    
    /**
     * Remove those nodes and data ranges that have more than enough values to 
     * cover the inequalities. 
     * @param nodeToCanonicalDR a map from nodes to canonical data ranges
     * @param inequalities inequalities between nodes
     * @param inequalitiesSym symmetric counterpart for the above relation
     */
    protected void removeUnderRestrictedRanges(List<Pair> CDRsToNodes, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        boolean containedRemovable = true;
        Set<Pair> removablePairs = new HashSet<Pair>();
        while (containedRemovable) {
            containedRemovable = false;
            for (Pair pair : CDRsToNodes) {
                CanonicalDataRange cdr = pair.getCanonicalDataRange();
                Node node = pair.getNode();
                int numInequalNodes = 0;
                if (inequalities.get(node) != null) {
                    numInequalNodes = inequalities.get(node).size();
                }
                if (inequalitiesSym.get(node) != null) {
                    numInequalNodes += inequalitiesSym.get(node).size();
                }
                if (cdr.hasMinCardinality(new BigInteger("" + (numInequalNodes + 1)))) {
                    removablePairs.add(pair);
                    containedRemovable = true;
                }
            }
            CDRsToNodes.removeAll(removablePairs);
            removablePairs.clear();
        }
    }
    
//    /**
//     * Partition the set of nodes and their canonical data ranges together with 
//     * their inequalities into mutally disjoint non-empty subsets P1, ..., Pn 
//     * such that no Pi and Pj with i neq j have variables in common. 
//     * @param nodeToCanonicalDR a map from nodes to canonical data ranges
//     * @param inequalities a map from nodes to the nodes for which an inequality 
//     *        is known
//     * @param inequalitiesSym the symmetric counterpart toinequalitiesSym 
//     * @return a set of partitions such that the nodes and their data ranges in 
//     *         each partition have inequalities between them 
//     */
//    protected Set<Map<CanonicalDataRange, Node>> buildPartitions(
//            Map<Node, CanonicalDataRange> nodeToCanonicalDR, 
//            Map<Node, Set<Node>> inequalities, 
//            Map<Node, Set<Node>> inequalitiesSym) {
//        Set<Map<CanonicalDataRange, Node>> partitions = new HashSet<Map<CanonicalDataRange, Node>>();
//        Map<CanonicalDataRange, Node> partition = new HashMap<CanonicalDataRange, Node>();
//        List<Node> remainingNodes = new ArrayList<Node>(nodeToCanonicalDR.keySet());
//        List<Node> nodesForThisPartition = new ArrayList<Node>();
//        while (!remainingNodes.isEmpty()) {
//            Node node = remainingNodes.get(0);
//            partition.put(nodeToCanonicalDR.get(node), node);
//            remainingNodes.remove(node);
//            // nodes that are inequal to this one should go into the same 
//            // partition
//            if (inequalities.containsKey(node)) {
//                nodesForThisPartition.addAll(inequalities.get(node));
//            }
//            if (inequalitiesSym.containsKey(node)) {
//                nodesForThisPartition.addAll(inequalitiesSym.get(node));
//            }
//            while (!nodesForThisPartition.isEmpty()) {
//                Node currentNode = nodesForThisPartition.get(0);
//                if (nodeToCanonicalDR.containsKey(currentNode)) {
//                    partition.put(nodeToCanonicalDR.get(currentNode), currentNode);
//                    remainingNodes.remove(currentNode);
//                    // put all remaining nodes that are inequal to this one into 
//                    // the list of nodes to be processed, so that they go into 
//                    // the same partition
//                    for (Node n : remainingNodes) {
//                        if (inequalities.get(currentNode) != null 
//                                && inequalities.get(currentNode).contains(n)) {
//                            nodesForThisPartition.add(n);
//                        }
//                        if (inequalitiesSym.get(currentNode) != null 
//                                && inequalitiesSym.get(currentNode).contains(n)) {
//                            nodesForThisPartition.add(n);
//                        }
//                    }
//                }
//                nodesForThisPartition.remove(currentNode);
//            }
//            partitions.add(partition);
//        }
//        return partitions;
//    }
    
    /**
     * Given a map from data ranges to nodes and inequalities between the nodes, 
     * check whether there is a suitable assignment of values for the nodes that 
     * complies with the given datatype restrictions and inequalities.  
     * @param partition A map from data ranges to nodes such that the 
     *        inequalities given in the next two parameters link the variables 
     *        in the partition. 
     * @param inequalities A map from nodes to sets of nodes for which there are 
     *        inequalities in the extension table. 
     * @param inequalitiesSym The symmetric counter part to the above map. 
     * @param originalNodeToDRs A map from nodes to data ranges such that for 
     *        each node and data range in the map, there is a corresponding 
     *        entry in the extension table.  
     * @return true if an assignment from values to nodes exists such that the 
     *         datatype restrictions and the inequalities are satisfied and 
     *         false otherwise.  
     */
    protected boolean hasAssignment(List<Pair> pairs, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym, 
            Map<Node, Set<DataRange>> originalNodeToDRs) {
        Collections.sort(pairs);
        
        Map<Node, DataConstant> nodeToValue = new HashMap<Node, DataConstant>();
        Node currentNode;
        CanonicalDataRange currentRange;
        for (Pair pair : pairs) {
            currentNode = pair.getNode();
            currentRange = pair.getCanonicalDataRange();
            DataConstant assignment = currentRange.getSmallestAssignment();
            //System.out.println("DataRange " + currentRange + " of node " + currentNode + " got assigned " + assignment);
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
                numberOfCauses += originalNodeToDRs.get(currentNode).size();
                
                UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                Object[][] causes = new Object[numberOfCauses][];
                int i = 0;
                for (DataRange range : originalNodeToDRs.get(currentNode)) {
                    causes[i] = new Object[] { range, currentNode };
                    ds.m_dependencySets[i] = 
                            extensionManager.getAssertionDependencySet(range, currentNode);
                    i++;
                }
                if (inequalities.get(currentNode) != null) {
                    for (Node inequalNode : inequalities.get(currentNode)) {
                        causes[i] = new Object[] { 
                                Inequality.INSTANCE, currentNode, inequalNode };
                        ds.m_dependencySets[i] = 
                                extensionManager.getAssertionDependencySet(
                                        Inequality.INSTANCE, 
                                        currentNode, 
                                        inequalNode);
                        i++;
                    }
                }
                if (inequalitiesSym.get(currentNode) != null) {
                    for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                        causes[i] = new Object[] { 
                                Inequality.INSTANCE, inequalNode, currentNode };
                        ds.m_dependencySets[i] = 
                                extensionManager.getAssertionDependencySet(
                                        Inequality.INSTANCE, 
                                        inequalNode, 
                                        currentNode);
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
            for (Node inequalNode : inequalities.get(currentNode)) {
                for (Pair p : pairs) {
                    if (p.getNode() == inequalNode) {
                        p.getCanonicalDataRange().notOneOf(assignment);
                    }
                }
            }
            for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                for (Pair p : pairs) {
                    if (p.getNode() == inequalNode) {
                        p.getCanonicalDataRange().notOneOf(assignment);
                    }
                }
            }
        }
        //System.out.println("-----------------------");
        return true;
    }
    
    /**
     * used to sort data ranges based on the number of possible assignments
     */
    protected static class SetLengthComparator implements Comparator<CanonicalDataRange> { 
        public static Comparator<CanonicalDataRange> INSTANCE = new SetLengthComparator();
        public int compare(CanonicalDataRange dr1, CanonicalDataRange dr2) {
            if (dr1.isFinite() && dr2.isFinite()) {
                BigInteger size1 = dr1.getEnumerationSize();
                BigInteger size2 = dr2.getEnumerationSize();
                if (size1.equals(size2)) return 0;
                return (size1.compareTo(size2) > 0) ? 1 : -1; 
            } else {
                return 0;
            }
        }
    }
    
    protected class Pair implements Comparable<Pair> {
        private final CanonicalDataRange cdr;
        private final Node node;
        private transient final int hash;

        public Pair(CanonicalDataRange cdr, Node node) {
            this.cdr = cdr;
            this.node = node;
            hash = (cdr == null ? 0 : cdr.hashCode() * 31)
                    + (node == null ? 0 : node.hashCode());
        }

        public CanonicalDataRange getCanonicalDataRange() {
            return cdr;
        }

        public Node getNode() {
            return node;
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof Pair)) {
                return false;
            }
            Pair other = (Pair) o;
            return (cdr == null ? other.getCanonicalDataRange() == null : cdr.equals(other.getCanonicalDataRange()))
                    && (node == null ? other.getNode() == null : node.equals(other.getNode()));
        }
        
        public int compareTo(Pair pair) {
            CanonicalDataRange cdr2 = pair.getCanonicalDataRange();
            Node node2 = pair.getNode();
            if (cdr.isFinite() && cdr2.isFinite()) {
                BigInteger size1 = cdr.getEnumerationSize();
                BigInteger size2 = cdr2.getEnumerationSize();
                if (size1.equals(size2)) {
                    Integer i = node.m_nodeID;
                    return i.compareTo(node2.m_nodeID);
                }
                return (size1.compareTo(size2) > 0) ? 1 : -1; 
            } else {
                return 0;
            }
        }
    } 
}
