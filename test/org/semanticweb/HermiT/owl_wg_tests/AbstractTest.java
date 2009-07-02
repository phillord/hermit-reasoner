package org.semanticweb.HermiT.owl_wg_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.tableau.InterruptException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractTest extends TestCase {
    public static int TIMEOUT=300000;
    protected static final File TEMPORARY_DIRECTORY=new File(new File(System.getProperty("java.io.tmpdir")),"WG-tests");

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
        m_reasoner=new Reasoner(getConfiguration());
    }
    protected void tearDown() {
        m_wgTestDescriptor=null;
        m_ontologyManager=null;
        m_premiseOntology=null;
        m_reasoner=null;
    }
    public void runTest() throws Throwable {
        InterruptTimer timer=new InterruptTimer(TIMEOUT,m_reasoner);
        timer.start();
        try {
            m_reasoner.loadOntology(m_ontologyManager,m_premiseOntology,null);
            doTest();
        }
        catch (InterruptException e) {
            dumpFailureData();
            fail("Test timed out.");
        }
        catch (OutOfMemoryError e) {
            m_reasoner=null;
            Runtime.getRuntime().gc();
            dumpFailureData();
            fail("Test ran out of memory.");
        }
        catch (Throwable e) {
            dumpFailureData();
            throw e;
        }
        finally {
            timer.stopTiming();
            timer.join();
        }
    }
    protected void dumpFailureData() throws Exception {
        saveOntology(m_ontologyManager,m_premiseOntology,new File(getFailureRoot(),"premise.owl"));
    }
    protected Configuration getConfiguration() {
        return new Configuration();
    }
    protected void saveOntology(OWLOntologyManager manager,OWLOntology ontology,File file) throws Exception {
        BufferedWriter writer=new BufferedWriter(new FileWriter(file));
        RDFXMLRenderer renderer=new RDFXMLRenderer(manager,ontology,writer);
        renderer.render();
        writer.close();
    }    
    protected File getFailureRoot() { 
        File directory=new File(new File(TEMPORARY_DIRECTORY,m_wgTestDescriptor.testID),getTestType());
        directory.mkdirs();
        return directory;
    }
    protected abstract void doTest() throws Exception;
    protected abstract String getTestType();
    
    protected static class InterruptTimer extends Thread {
        protected final int m_timeout;
        protected final Reasoner m_reasoner;
        protected boolean m_timingStopped;
        
        public InterruptTimer(int timeout,Reasoner reasoner) {
            super("HermiT Interrupt Thread");
            setDaemon(true);
            m_timeout=timeout;
            m_reasoner=reasoner;
            m_timingStopped=false;
        }
        public synchronized void run() {
            try {
                if (!m_timingStopped) {
                    wait(m_timeout);
                    if (!m_timingStopped)
                        m_reasoner.interrupt();
                }
            }
            catch (InterruptedException stopped) {
            }
        }
        public synchronized void stopTiming() {
            m_timingStopped=true;
            notifyAll();
        }
    }
}
