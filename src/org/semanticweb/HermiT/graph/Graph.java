// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<T> {
    protected final Set<T> m_elements;
    protected final Map<T,Set<T>> m_successorsByNodes;

    public Graph() {
        m_elements=new HashSet<T>();
        m_successorsByNodes=new HashMap<T,Set<T>>();
    }
    public void addEdge(T from,T to) {
        Set<T> successors=m_successorsByNodes.get(from);
        if (successors==null) {
            successors=new HashSet<T>();
            m_successorsByNodes.put(from,successors);
        }
        successors.add(to);
        m_elements.add(from);
        m_elements.add(to);
    }
    public Set<T> getElements() {
        return m_elements;
    }
    public Set<T> getSuccessors(T node) {
        Set<T> result=m_successorsByNodes.get(node);
        if (result==null)
            result=Collections.emptySet();
        return result;
    }
    public void transitivelyClose() {
        List<T> toProcess=new ArrayList<T>();
        for (Set<T> reachable : m_successorsByNodes.values()) {
            toProcess.clear();
            toProcess.addAll(reachable);
            while (!toProcess.isEmpty()) {
                T elementOnPath=toProcess.remove(toProcess.size()-1);
                Set<T> elementOnPathSuccessors=m_successorsByNodes.get(elementOnPath);
                if (elementOnPathSuccessors!=null)
                    for (T elementOnPathSuccessor : elementOnPathSuccessors)
                        if (reachable.add(elementOnPathSuccessor))
                            toProcess.add(elementOnPathSuccessor);
            }
        }
    }
    public Graph<T> getInverse() {
        Graph<T> result=new Graph<T>();
        for (Map.Entry<T,Set<T>> entry : m_successorsByNodes.entrySet()) {
            T from=entry.getKey();
            for (T successor : entry.getValue())
                result.addEdge(successor,from);
        }
        return result;
    }
    /**
     * gstoil addition
     */
    public Graph<T> clone() {
        Graph<T> result=new Graph<T>();
        for (Map.Entry<T,Set<T>> entry : m_successorsByNodes.entrySet()) {
            T from=entry.getKey();
            for (T successor : entry.getValue())
                result.addEdge(from,successor);
        }
        return result;
    }
    /**
     * gstoil addition
     */
	public void removeElements(Set<T> elements) {
		for(T element : elements){
			m_elements.remove( element );
			m_successorsByNodes.remove( element );
		}
			
	}
}
