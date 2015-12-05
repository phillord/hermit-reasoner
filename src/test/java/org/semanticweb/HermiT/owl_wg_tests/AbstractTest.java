/* Copyright 2009 by the Oxford University Computing Laboratory

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
// An update for the tests (all.rdf) should regularly be downloaded to the
// ontologies folder from http://wiki.webont.org/exports/
package org.semanticweb.HermiT.owl_wg_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxRenderer;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public abstract class AbstractTest extends TestCase {
    public static int TIMEOUT=300000;

    protected File m_dumpTestDataDirectory;
    protected WGTestDescriptor m_wgTestDescriptor;
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_premiseOntology;
    protected Reasoner m_reasoner;
    protected boolean m_useDisjunctionLearning;
    
    public AbstractTest(String name,WGTestDescriptor wgTestDescriptor,File dumpTestDataDirectory) {
        this(name,wgTestDescriptor,dumpTestDataDirectory,true);
    }
    public AbstractTest(String name,WGTestDescriptor wgTestDescriptor,File dumpTestDataDirectory,boolean useDisjunctionLearning) {
        super(name);
        m_wgTestDescriptor=wgTestDescriptor;
        m_dumpTestDataDirectory=dumpTestDataDirectory;
        m_useDisjunctionLearning=useDisjunctionLearning;
    }
    protected void setUp() throws Exception {
        m_ontologyManager=OWLManager.createOWLOntologyManager();
        registerImportedReosurces();
        m_premiseOntology=m_wgTestDescriptor.getPremiseOntology(m_ontologyManager);
    }
    protected void registerMappingToResource(String ontologyIRI,String physicalResource) throws Exception {
        IRI physicalIRI=IRI.create(getClass().getResource(physicalResource).toURI());
        IRI logicalIRI=IRI.create(ontologyIRI);
        m_ontologyManager.addIRIMapper(new SimpleIRIMapper(logicalIRI,physicalIRI));
    }
    protected void registerImportedReosurces() throws Exception {
        registerMappingToResource("http://www.w3.org/2002/03owlt/miscellaneous/consistent001","ontologies/consistent001.rdf");
        registerMappingToResource("http://www.w3.org/2002/03owlt/miscellaneous/consistent002","ontologies/consistent002.rdf");
        registerMappingToResource("http://www.w3.org/2002/03owlt/imports/support011-A","ontologies/support011-A.rdf");
    }
    protected void tearDown() {
        m_wgTestDescriptor=null;
        m_ontologyManager=null;
        m_premiseOntology=null;
        m_reasoner=null;
        m_dumpTestDataDirectory=null;
    }
    public void runTest() throws Throwable {
        dumpTestData();
        m_reasoner=new Reasoner(getConfiguration(),m_premiseOntology,null);
        InterruptTimer timer=new InterruptTimer(TIMEOUT,m_reasoner);
        timer.start();
        try {
            doTest();
        }
        catch (ReasonerInterruptedException e) {
            fail("Test timed out.");
        }
        catch (OutOfMemoryError e) {
            m_reasoner=null;
            Runtime.getRuntime().gc();
            fail("Test ran out of memory.");
        }
        catch (AssertionFailedError e) {
            fail("Test failed: "+e.getMessage());
        }
        catch (Throwable e) {
            throw e;
        }
        finally {
            timer.stopTiming();
            timer.join();
        }
    }
    protected void dumpTestData() throws Exception {
        if (m_dumpTestDataDirectory!=null)
            saveOntology(m_ontologyManager,m_premiseOntology,new File(m_dumpTestDataDirectory,"premise.owl"));
    }
    protected Configuration getConfiguration() {
        Configuration c=new Configuration();
        c.throwInconsistentOntologyException=false;
        c.useDisjunctionLearning=m_useDisjunctionLearning;
        return c;
    }
    protected void saveOntology(OWLOntologyManager manager,OWLOntology ontology,File file) throws Exception {
        BufferedWriter writer=new BufferedWriter(new FileWriter(file));
        OWLFunctionalSyntaxRenderer renderer=new OWLFunctionalSyntaxRenderer();
        renderer.render(ontology,writer);
        writer.close();
    }
    protected abstract void doTest() throws Exception;

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
