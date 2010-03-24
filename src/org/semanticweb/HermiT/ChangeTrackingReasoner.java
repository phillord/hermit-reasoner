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

package org.semanticweb.HermiT;

import java.util.List;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

public class ChangeTrackingReasoner extends Reasoner {
    private static final long serialVersionUID=-838517681658238457L;

    protected final OntologyChangeListener m_ontologyChangeListener;
    protected OWLOntology m_rootOntology;
    protected boolean m_changed;

    public ChangeTrackingReasoner(Configuration configuration,OWLOntology rootOntology) {
        super(configuration,rootOntology);
        m_ontologyChangeListener=new OntologyChangeListener();
        m_rootOntology=rootOntology;
        m_rootOntology.getOWLOntologyManager().addOntologyChangeListener(m_ontologyChangeListener);
    }
    public void dispose() {
        if (m_rootOntology!=null) {
            m_rootOntology.getOWLOntologyManager().removeOntologyChangeListener(m_ontologyChangeListener);
            m_rootOntology=null;
        }
        super.dispose();
    }
    public OWLOntology getRootOntology() {
        return m_rootOntology;
    }
    public void flush() {
        if (m_changed) {
            loadOntology(m_rootOntology,null);
            m_changed=false;
        }
    }

    protected class OntologyChangeListener implements OWLOntologyChangeListener {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            m_changed=true;
            if (!m_configuration.bufferChanges)
                flush();
        }
    }
}
