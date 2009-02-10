package org.semanticweb.HermiT.owlapi.structural;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AxiomType;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLAxiomVisitorEx;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLObjectVisitorEx;
import org.semanticweb.owl.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.OWLLogicalAxiomImpl;

public class OWLHasKeyDummy extends OWLLogicalAxiomImpl implements OWLLogicalAxiom {

    public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    
    public OWLDescription classExpression;
    public Set<OWLObjectPropertyExpression> objectProperties = new HashSet<OWLObjectPropertyExpression>();
    public Set<OWLDataPropertyExpression> dataProperties = new HashSet<OWLDataPropertyExpression>();
    
    public static OWLHasKeyDummy getDemoKey() throws URISyntaxException {
        OWLHasKeyDummy key = new OWLHasKeyDummy();
        OWLDataFactory f = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        OWLClass ce = f.getOWLClass(new URI("int:C_test"));
        key.setClassExpression(ce);
        
        Set<OWLObjectPropertyExpression> oprops = new HashSet<OWLObjectPropertyExpression>();
        oprops.add(f.getOWLObjectProperty(new URI("int:r_test")));
        key.setObjectProperties(oprops);
        
        Set<OWLDataPropertyExpression> dprops = new HashSet<OWLDataPropertyExpression>();
        dprops.add(manager.getOWLDataFactory().getOWLDataProperty(new URI("int:dp_test")));
        key.setDataProperties(dprops);
        
        return key;
    }
    
    public OWLHasKeyDummy() {
        super(manager.getOWLDataFactory());
    }
    
    public OWLHasKeyDummy(OWLDataFactory dataFactory) {
        super(dataFactory);
    }

    protected int compareObjectOfSameType(OWLObject object) {
        return 0;
    }

    public void accept(OWLAxiomVisitor visitor) {
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return null;
    }

    public AxiomType<OWLHasKeyDummy> getAxiomType() {
        return null;
    }

    public void accept(OWLObjectVisitor visitor) {
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return null;
    }
    
    public void setClassExpression(OWLDescription classExpression) {
        this.classExpression = classExpression;
    }

    public void setObjectProperties(
            Set<OWLObjectPropertyExpression> objectProperties) {
        this.objectProperties = objectProperties;
    }

    public void setDataProperties(Set<OWLDataPropertyExpression> dataProperties) {
        this.dataProperties = dataProperties;
    }

    public OWLDescription getClassExpression() {
        return classExpression;
    }
    
    public Set<OWLObjectPropertyExpression> getObjectProperties() {
        return objectProperties;
    }
    
    public Set<OWLDataPropertyExpression> getDataProperties() {
        return dataProperties;
    }
    
    public String toString() {
        String s = "HasKey(" + classExpression;
        for (OWLObjectPropertyExpression p : this.getObjectProperties()) {
            s += " " + p.toString();
        }
        for (OWLDataPropertyExpression p : this.getDataProperties()) {
            s += " " + p.toString();
        }
        s += ")";
        return s;
    }
}
