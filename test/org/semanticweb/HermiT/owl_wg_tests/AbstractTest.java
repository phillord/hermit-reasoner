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
import java.io.PrintWriter;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;

public abstract class AbstractTest extends TestCase {
    public static int TIMEOUT=300000;
    protected static final File TEMPORARY_DIRECTORY=new File(new File(System.getProperty("java.io.tmpdir")),"WG-tests");

    protected WGTestDescriptor m_wgTestDescriptor;
    protected OWLOntologyManager m_ontologyManager;
    protected OWLOntology m_premiseOntology;
    protected Reasoner m_reasoner;
    protected final PrintWriter output;
    
    public AbstractTest(String name,WGTestDescriptor wgTestDescriptor,PrintWriter output) {
        super(name);
        this.output=output;
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
        if (output != null) {
            output.println("[ testResultOntology:runner <http://hermit-reasoner.com/> ;");
            output.println("  testResultOntology:test [ testOntology:identifier \""+m_wgTestDescriptor.identifier+"\"^^xsd:string ] ;");
            output.println("  rdf:type testResultOntology:"+this.reportTestType()+" ,");
            output.println("    testResultOntology:TestRun ,");
        }
        InterruptTimer timer=new InterruptTimer(TIMEOUT,m_reasoner);
        timer.start();
        try {
            m_reasoner.loadOntology(m_ontologyManager,m_premiseOntology,null);
            long t=System.currentTimeMillis();
            doTest();
            if (output != null) {
                output.println("    testResultOntology:PassingRun ;");
                output.println("  testResultOntology:runtimeMillisecs \""+(System.currentTimeMillis()-t)+"\"^^xsd:integer");
            }
        }
        catch (ReasonerInterruptedException e) {
            if (output != null) {
                output.println("    testResultOntology:IncompleteRun ;");
                output.print("  testResultOntology:details \"Timeout: "+TIMEOUT+" ms\"");
            }
            dumpFailureData();
            fail("Test timed out.");
        }
        catch (OutOfMemoryError e) {
            m_reasoner=null;
            Runtime.getRuntime().gc();
            if (output != null) {
                output.println("    testResultOntology:IncompleteRun ;");
                output.print("  testResultOntology:details \"Out of memory. \"");
            }
            dumpFailureData();
            fail("Test ran out of memory.");
        } catch (AssertionFailedError e) {
            if (output != null) {
                output.println("    testResultOntology:FailingRun ;");
                output.print("  testResultOntology:details \""+e.getMessage()+"\"");
            }
            fail("Test failed: " + e.getMessage());
        } catch (Throwable e) {
            if (output != null) {
                output.println("    testResultOntology:IncompleteRun ;");
                output.print("  testResultOntology:details \""+e.getMessage()+"\"");
            }
            dumpFailureData();
            throw e;
        }
        finally {
            timer.stopTiming();
            timer.join();
            if (output != null) {
                output.println("] .");
                output.println();
                output.flush();
            }
        }
    }
    protected void dumpFailureData() throws Exception {
        saveOntology(m_ontologyManager,m_premiseOntology,new File(getFailureRoot(),"premise.owl"));
    }
    protected Configuration getConfiguration() {
        Configuration c=new Configuration();
        c.tableauMonitorType=Configuration.TableauMonitorType.TIMING;
        return c;
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
        System.err.println(directory);
        return directory;
    }
    protected abstract void doTest() throws Exception;
    protected abstract String getTestType();
    protected abstract String reportTestType();
    
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
