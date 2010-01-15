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
package org.semanticweb.HermiT.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<T> implements Serializable {
    private static final long serialVersionUID = 5372948202031042380L;
    
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
