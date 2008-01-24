package org.semanticweb.HermiT.tableau;

import java.util.Collections;
import java.util.Set;
import java.util.HashMap;

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.owl.elements.*;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public abstract class AbstractOntologyTest extends AbstractHermiTTest {
    protected Ontology m_ontology;

    public AbstractOntologyTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        KAON2Connection connection=KAON2Manager.newConnection();
        m_ontology=connection.createOntology("file:/c:/test/ontology.owl",new HashMap<String,Object>());
    }
    protected void tearDown() {
        m_ontology=null;
    }
    protected void assertEquivalent(String string1,String string2,boolean result) throws Exception {
        Description description1=KAON2Manager.factory().description(string1,Namespaces.INSTANCE);
        Description description2=KAON2Manager.factory().description(string1,Namespaces.INSTANCE);
        Axiom axiom1=null;
        Axiom axiom2=null;
        if (!(description1 instanceof OWLClass)) {
            OWLClass temp=KAON2Manager.factory().owlClass("new$class1");
            axiom1=KAON2Manager.factory().equivalentClasses(description1,temp);
            m_ontology.addAxiom(axiom1);
            description1=temp;
        }
        if (!(description2 instanceof OWLClass)) {
            OWLClass temp=KAON2Manager.factory().owlClass("new$class2");
            axiom2=KAON2Manager.factory().equivalentClasses(description2,temp);
            m_ontology.addAxiom(axiom2);
            description2=temp;
        }
        try {
            Tableau tableau=getTableau();
            AtomicConcept concept1=AtomicConcept.create(description1.toString());
            AtomicConcept concept2=AtomicConcept.create(description2.toString());
            assertEquals(result,tableau.isSubsumedBy(concept1,concept2) && tableau.isSubsumedBy(concept2,concept1));
        }
        finally {
            if (axiom1!=null)
                m_ontology.removeAxiom(axiom1);
            if (axiom2!=null)
                m_ontology.removeAxiom(axiom2);
        }
    }
    protected void assertSubsumedBy(String subString,String superString,boolean result) throws Exception {
        Description subDescription=KAON2Manager.factory().description(subString,Namespaces.INSTANCE);
        Description superDescription=KAON2Manager.factory().description(superString,Namespaces.INSTANCE);
        if (subDescription instanceof OWLClass && superDescription instanceof OWLClass) {
            Tableau tableau=getTableau();
            AtomicConcept subconcept=AtomicConcept.create(subDescription.toString());
            AtomicConcept superconcept=AtomicConcept.create(superDescription.toString());
            assertEquals(result,tableau.isSubsumedBy(subconcept,superconcept));
        }
        else {
            OWLClass temp=KAON2Manager.factory().owlClass("new$class");
            Axiom axiom=KAON2Manager.factory().subClassOf(temp,KAON2Manager.factory().objectAnd(subDescription,superDescription.getComplementNNF()));
            m_ontology.addAxiom(axiom);
            try {
                Tableau tableau=getTableau();
                AtomicConcept tempConcept=AtomicConcept.create(temp.getURI());
                assertEquals(result,!tableau.isSatisfiable(tempConcept));
            }
            finally {
                m_ontology.removeAxiom(axiom);
            }
        }
    }
    protected void assertInstanceOf(String descriptionString,String individualString,boolean result) throws Exception {
        Description description=KAON2Manager.factory().description(descriptionString,Namespaces.INSTANCE);
        org.semanticweb.kaon2.api.owl.elements.Individual individual=KAON2Manager.factory().individual(individualString);
        Axiom axiom=KAON2Manager.factory().classMember(description.getComplementNNF(),individual);
        m_ontology.addAxiom(axiom);
        try {
            Tableau tableau=getTableau();
            assertEquals(result,!tableau.isABoxSatisfiable());
        }
        finally {
            m_ontology.removeAxiom(axiom);
        }
    }
    protected void assertABoxSatisfiable(boolean satisfiable) throws Exception {
        Tableau tableau=getTableau();
        assertEquals(satisfiable,tableau.isABoxSatisfiable());
    }
    protected void assertSatisfiable(String string,boolean satisfiable) throws Exception {
        Description description=KAON2Manager.factory().description(string,Namespaces.INSTANCE);
        Axiom axiom=null;
        if (!(description instanceof OWLClass)) {
            OWLClass temp=KAON2Manager.factory().owlClass("new$class1");
            axiom=KAON2Manager.factory().subClassOf(temp,description);
            m_ontology.addAxiom(axiom);
            description=temp;
        }
        try {
            AtomicConcept concept=AtomicConcept.create(((OWLClass)description).getURI());
            Tableau tableau=getTableau();
            assertEquals(satisfiable,tableau.isSatisfiable(concept));
        }
        finally {
            if (axiom!=null)
                m_ontology.removeAxiom(axiom);
        }
    }
    protected DLOntology getDLOntology() throws Exception {
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausification.clausify(shouldPrepareForNIRule(),m_ontology,true,noDescriptionGraphs);
    }
    protected boolean shouldPrepareForNIRule() {
        return false;
    }
    protected Tableau getTableau() throws Exception {
        DLOntology dlOntology=getDLOntology();
        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
        BlockingCache blockingCache=new BlockingCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        return new Tableau(getTableauMonitor(),existentialsExpansionStrategy,dlOntology);
    }
    protected TableauMonitor getTableauMonitor() {
        return null;
    }
    protected void addAxiom(String axiomString) throws Exception {
        Axiom axiom=KAON2Manager.factory().axiom(axiomString,Namespaces.INSTANCE);
        m_ontology.addAxiom(axiom);
    }
    protected void removeAxiom(String axiomString) throws Exception {
        Axiom axiom=KAON2Manager.factory().axiom(axiomString,Namespaces.INSTANCE);
        m_ontology.removeAxiom(axiom);
    }
}
