package org.semanticweb.HermiT.owl_wg_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.coode.owl.rdf.rdfxml.RDFXMLRenderer;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.monitor.TableauMonitorAdapter;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.Node;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntology;

import junit.framework.TestCase;

public abstract class AbstractTest extends TestCase {
    public static long TIMEOUT=6000L;
    protected static final File TEMPORARY_DIRECTORY=new File(System.getProperty("java.io.tmpdir"));

    protected WGTestDescriptor m_wgTestDescriptor;
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_premiseOntology;
    protected Reasoner m_reasoner;
    
    public AbstractTest(String name,WGTestDescriptor wgTestDescriptor) {
        super(name);
        m_wgTestDescriptor=wgTestDescriptor;
    }
    protected void setUp() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        m_premiseOntology=m_wgTestDescriptor.getPremiseOntology(m_ontologyManager);
        Configuration configuration=getConfiguration();
        configuration.monitor=new TimeoutMonitor();
        m_reasoner=new Reasoner(configuration,m_ontologyManager,m_premiseOntology);
    }
    protected void tearDown() {
        m_wgTestDescriptor=null;
        m_ontologyManager=null;
        m_premiseOntology=null;
        m_reasoner=null;
    }
    public void runTest() throws Exception {
        try {
            doTest();
        }
        catch (TimeoutException e) {
            fail("Test timed out.");
            dumpFailureData();
        }
        catch (OutOfMemoryError e) {
            m_reasoner=null;
            Runtime.getRuntime().gc();
            fail("Test ran out of memory.");
            dumpFailureData();
        }
        catch (AssertionError e) {
            dumpFailureData();
            throw e;
        }
    }
    protected void dumpFailureData() throws Exception {
        saveOntology(m_ontologyManager,m_premiseOntology,new File(getFailureRoot(),"premise.owl"));
    }
    protected File getFailureRoot() { 
        return new File(TEMPORARY_DIRECTORY,m_wgTestDescriptor.identifier);
    }
    protected Configuration getConfiguration() {
        return new Configuration();
    }
    protected void saveOntology(OWLOntologyManager manager,OWLOntology ontology,File file) throws Exception {
        file.mkdirs();
        BufferedWriter writer=new BufferedWriter(new FileWriter(file));
        RDFXMLRenderer renderer=new RDFXMLRenderer(manager,ontology,writer);
        renderer.render();
        writer.close();
    }    
    protected abstract void doTest() throws Exception;
    
    @SuppressWarnings("serial")
    protected static class TimeoutMonitor extends TableauMonitorAdapter {
        protected long m_timeoutTime;
        
        public void saturateStarted() {
            m_timeoutTime=System.currentTimeMillis()+TIMEOUT;
        }
        public void iterationStarted() {
            checkTimeout();
        }
        public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
            checkTimeout();
        }
        public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
            checkTimeout();
        }
        protected void checkTimeout() {
            if (System.currentTimeMillis()>m_timeoutTime)
                throw new TimeoutException();
        }
    }
    
    @SuppressWarnings("serial")
    protected static class TimeoutException extends RuntimeException {
    }
}
