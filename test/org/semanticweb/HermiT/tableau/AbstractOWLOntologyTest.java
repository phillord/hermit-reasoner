package org.semanticweb.HermiT.tableau;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.HermiT.blocking.AnywhereBlocking;
import org.semanticweb.HermiT.blocking.BlockingSignatureCache;
import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.blocking.DirectBlockingChecker;
import org.semanticweb.HermiT.blocking.PairWiseDirectBlockingChecker;
import org.semanticweb.HermiT.existentials.CreationOrderStrategy;
import org.semanticweb.HermiT.existentials.ExistentialsExpansionStrategy;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.HermiT.owlapi.structural.OwlClausification;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Namespaces;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class AbstractOWLOntologyTest extends AbstractHermiTOWLTest {
    protected OWLOntology m_ontology;

    public AbstractOWLOntologyTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		m_ontology = manager.createOntology(URI.create("file:/c:/test/ontology.owl"));
    }
    protected void tearDown() {
        m_ontology=null;
    }
    protected void assertABoxSatisfiable(boolean satisfiable) throws Exception {
        Tableau tableau=getTableau();
        assertEquals(satisfiable,tableau.isABoxSatisfiable());
    }
    /**
     * @return a clausified version of the loaded ontology
     * @throws Exception
     */
    protected DLOntology getDLOntology() throws Exception {
        OwlClausification clausifier = new OwlClausification();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausifier.clausify(shouldPrepareForNIRule(),m_ontology,factory,noDescriptionGraphs);
    }
    protected boolean shouldPrepareForNIRule() {
        return false;
    }
    protected Tableau getTableau() throws Exception {
        DLOntology dlOntology=getDLOntology();
        DirectBlockingChecker directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
        BlockingSignatureCache blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
        BlockingStrategy blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
        ExistentialsExpansionStrategy existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
        return new Tableau(getTableauMonitor(),existentialsExpansionStrategy,dlOntology,new HashMap<String,Object>());
    }
    protected TableauMonitor getTableauMonitor() {
        return null;
    }
    /**
     * Loads an ontology via the OWL API so that it is available for 
     * the custom assert methods.
     * @param resource the resource to load 
     * @throws Exception if the resource cannot be found or an error occurred 
     * when loading the ontology 
     */
    protected void loadResource(String resource) throws Exception {
        m_ontology = getOWLOntologyFromResource(resource);
    }
    /**
     * creates an ontology that contains the given axioms
     * @param axioms in functional style syntax
     * @throws OWLOntologyCreationException
     */
    protected void createOntologyWithAxioms(String axioms) throws OWLOntologyCreationException {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("Namespace(=<file:/c/test.owl#>)");
    	buffer.append("Namespace(rdfs=<http://www.w3.org/2000/01/rdf-schema#>)");
    	buffer.append("Namespace(owl2xml=<http://www.w3.org/2006/12/owl2-xml#>)");
    	buffer.append("Namespace(test=<file:/c/test.owl#>)");
    	buffer.append("Namespace(owl=<http://www.w3.org/2002/07/owl#>)");
    	buffer.append("Namespace(xsd=<http://www.w3.org/2001/XMLSchema#>)");
    	buffer.append("Namespace(rdf=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)");
    	buffer.append("Ontology(<file:/c/test.owl>");
    	buffer.append(axioms);
    	buffer.append(")");
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	OWLOntologyInputSource input = new StringInputSource(buffer.toString());
		m_ontology = manager.loadOntology(input);
    }
    protected void assertSubsumedBy(String subAtomicConcept, String superAtomicConcept, boolean expectedResult) throws Exception {
        Tableau tableau=getTableau();
        AtomicConcept subconcept = AtomicConcept.create("file:/c/test.owl#" + subAtomicConcept);
        AtomicConcept superconcept = AtomicConcept.create("file:/c/test.owl#" + superAtomicConcept);
        boolean result = tableau.isSubsumedBy(subconcept,superconcept);
        assertEquals(expectedResult,result);
    }
    protected void assertSatisfiable(String atomicConcept, boolean satisfiable) throws Exception {
    	AtomicConcept concept = AtomicConcept.create("file:/c/test.owl#" + atomicConcept);
    	Tableau tableau=getTableau();
    	assertEquals(satisfiable,tableau.isSatisfiable(concept));
    }
}
