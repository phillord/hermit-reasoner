// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.Ontology;

import org.semanticweb.HermiT.owlapi.structural.*;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;
import java.net.URI;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.hierarchy.*;
import org.semanticweb.HermiT.debugger.*;

public class HermiT implements Serializable {
    private static final long serialVersionUID=-8277117863937974032L;

    public static enum TableauMonitorType { NONE,TIMING,TIMING_WITH_PAUSE,DEBUGGER_NO_HISTORY,DEBUGGER_HISTORY_ON };
    public static enum DirectBlockingType { PAIR_WISE,SINGLE,OPTIMAL };
    public static enum BlockingType { ANYWHERE,ANCESTOR };
    public static enum BlockingSignatureCacheType { CACHED,NOT_CACHED };
    public static enum ExistentialsType { CREATION_ORDER,EL,INDIVIDUAL_REUSE };

    protected final Map<String,Object> m_parameters;
    protected DLOntology m_dlOntology;
    protected Namespaces m_namespaces;
    protected TableauMonitor m_userTableauMonitor;
    protected TableauMonitorType m_tableauMonitorType;
    protected DirectBlockingType m_directBlockingType;
    protected BlockingType m_blockingType;
    protected BlockingSignatureCacheType m_blockingSignatureCacheType;
    protected ExistentialsType m_existentialsType;
    protected Tableau m_tableau;
    protected TableauSubsumptionChecker m_subsumptionChecker;
    protected boolean m_useKaon2;

    public HermiT() {
        m_parameters=new HashMap<String,Object>();
        setTableauMonitorType(TableauMonitorType.NONE);
        setDirectBlockingType(DirectBlockingType.OPTIMAL);
        setBlockingType(BlockingType.ANYWHERE);
        setBlockingSignatureCacheType(BlockingSignatureCacheType.CACHED);
        setExistentialsType(ExistentialsType.CREATION_ORDER);
        m_useKaon2 = true;
    }
    public void setParameter(String parameterName,Object value) {
        m_parameters.put(parameterName,value);
    }
    public Object getParameter(String parameterName) {
        return m_parameters.get(parameterName);
    }
    public void setUserTableauMonitor(TableauMonitor userTableauMonitor) {
        m_userTableauMonitor=userTableauMonitor;
    }
    public TableauMonitorType getTableauMonitorType() {
        return m_tableauMonitorType;
    }
    public void setTableauMonitorType(TableauMonitorType tableauMonitorType) {
        m_tableauMonitorType=tableauMonitorType;
    }
    public void setTimingOn() {
        m_tableauMonitorType=TableauMonitorType.TIMING;
    }
    public void setTimingWithPauseOn() {
        m_tableauMonitorType=TableauMonitorType.TIMING_WITH_PAUSE;
    }
    public void setDebuggingOn(boolean historyOn) {
        m_tableauMonitorType=(historyOn ? TableauMonitorType.DEBUGGER_HISTORY_ON : TableauMonitorType.DEBUGGER_NO_HISTORY);
    }
    public DirectBlockingType getDirectBlockingType() {
        return m_directBlockingType;
    }
    public void setDirectBlockingType(DirectBlockingType directBlockingType) {
        m_directBlockingType=directBlockingType;
    }
    public BlockingType getBlockingType() {
        return m_blockingType;
    }
    public void setBlockingType(BlockingType blockingType) {
        m_blockingType=blockingType;
    }
    public BlockingSignatureCacheType getBlockingSignatureCacheType() {
        return m_blockingSignatureCacheType;
    }
    public void setBlockingSignatureCacheType(BlockingSignatureCacheType blockingSignatureCacheType) {
        m_blockingSignatureCacheType=blockingSignatureCacheType;
    }
    public ExistentialsType getExistentialsType() {
        return m_existentialsType;
    }
    public void setExistentialsType(ExistentialsType existentialsType) {
        m_existentialsType=existentialsType;
    }
    public void loadOntology(String physicalURI) throws KAON2Exception,OWLException,InterruptedException {
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        loadOntology(physicalURI,noDescriptionGraphs);
    }
    public void loadOntology(String physicalURI,Set<DescriptionGraph> descriptionGraphs) throws KAON2Exception,OWLException,InterruptedException {
        if (m_useKaon2) {
            // DefaultOntologyResolver resolver=new DefaultOntologyResolver();
            // String ontologyURI=resolver.registerOntology(physicalURI);
            // OntologyManager ontologyManager=KAON2Manager.newOntologyManager();
            // ontologyManager.setOntologyResolver(resolver);
            // Ontology ontology=ontologyManager.openOntology(ontologyURI,new HashMap<String,Object>());
            // loadKAON2Ontology(ontology,descriptionGraphs);
        } else {
		    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    		OWLOntology o = manager.loadOntologyFromPhysicalURI(URI.create(physicalURI));
    		loadOwlOntology(o,manager.getOWLDataFactory(),descriptionGraphs);
	    }
    }
	public void loadOwlOntology(OWLOntology ontology,OWLDataFactory factory,Set<DescriptionGraph> descriptionGraphs) throws OWLException {
		OwlClausification c = new OwlClausification();
		DLOntology d = c.clausify(m_existentialsType==ExistentialsType.INDIVIDUAL_REUSE,ontology,factory,descriptionGraphs);
		loadDLOntology(d);
	}
    public void loadKAON2Ontology(Ontology ontology,Set<DescriptionGraph> descriptionGraphs) throws KAON2Exception {
        Clausification clausification=new Clausification();
        DLOntology dlOntology=clausification.clausify(m_existentialsType==ExistentialsType.INDIVIDUAL_REUSE,ontology,descriptionGraphs);
        loadDLOntology(dlOntology);
    }
    public void loadDLOntology(File file) throws Exception {
        BufferedInputStream input=new BufferedInputStream(new FileInputStream(file));
        try {
            loadDLOntology(DLOntology.load(input));
        }
        finally {
            input.close();
        }
    }
    public void loadDLOntology(DLOntology dlOntology) throws IllegalArgumentException {
        if (!dlOntology.canUseNIRule() &&
            dlOntology.hasAtMostRestrictions() &&
            dlOntology.hasInverseRoles() &&
            (m_existentialsType==ExistentialsType.INDIVIDUAL_REUSE)) {
            throw new IllegalArgumentException("The supplied DL-onyology is not compatible with the individual reuse strategy.");
        }
        Namespaces namespaces=new Namespaces();
        namespaces.registerStandardPrefixes();
        namespaces.registerPrefix("a",dlOntology.getOntologyURI()+"#");
        namespaces.registerInternalPrefixes(dlOntology.getOntologyURI());
        Collection<DLClause> nonAdmissibleDLClauses=dlOntology.getNonadmissibleDLClauses();
        if (!nonAdmissibleDLClauses.isEmpty()) {
            String CRLF=System.getProperty("line.separator");
            StringBuffer buffer=new StringBuffer();
            buffer.append("The following DL-clauses in the DL-ontology are not admissible:");
            buffer.append(CRLF);
            for (DLClause dlClause : nonAdmissibleDLClauses) {
                buffer.append(dlClause.toString(namespaces));
                buffer.append(CRLF);
            }
            throw new IllegalArgumentException(buffer.toString());
        }
        m_dlOntology=dlOntology;
        m_namespaces=namespaces;
        
        TableauMonitor wellKnownTableauMonitor=null;
        switch (m_tableauMonitorType) {
        case NONE:
            wellKnownTableauMonitor=null;
            break;
        case TIMING:
            wellKnownTableauMonitor=new Timer();
            break;
        case TIMING_WITH_PAUSE:
            wellKnownTableauMonitor=new TimerWithPause();
            break;
        case DEBUGGER_HISTORY_ON:
            wellKnownTableauMonitor=new Debugger(m_namespaces,true);
            break;
        case DEBUGGER_NO_HISTORY:
            wellKnownTableauMonitor=new Debugger(m_namespaces,false);
            break;
        }

        TableauMonitor tableauMonitor=null;
        if (m_userTableauMonitor==null)
            tableauMonitor=wellKnownTableauMonitor;
        else if (wellKnownTableauMonitor==null)
            tableauMonitor=m_userTableauMonitor;
        else
            tableauMonitor=new TableauMonitorFork(wellKnownTableauMonitor,m_userTableauMonitor);
        
        DirectBlockingChecker directBlockingChecker=null;
        switch (m_directBlockingType) {
        case OPTIMAL:
            directBlockingChecker=(m_dlOntology.hasAtMostRestrictions() && m_dlOntology.hasInverseRoles() ? new PairWiseDirectBlockingChecker() : new SingleDirectBlockingChecker());
            break;
        case SINGLE:
            directBlockingChecker=new SingleDirectBlockingChecker();
            break;
        case PAIR_WISE:
            directBlockingChecker=new PairWiseDirectBlockingChecker();
            break;
        }
        
        BlockingSignatureCache blockingSignatureCache=null;
        if (!dlOntology.hasNominals()) {
            switch (m_blockingSignatureCacheType) {
            case CACHED:
                blockingSignatureCache=new BlockingSignatureCache(directBlockingChecker);
                break;
            case NOT_CACHED:
                blockingSignatureCache=null;
                break;
            }
        }
        
        BlockingStrategy blockingStrategy=null;
        switch (m_blockingType) {
        case ANCESTOR:
            blockingStrategy=new AncestorBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        case ANYWHERE:
            blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingSignatureCache);
            break;
        }
        
        ExistentialsExpansionStrategy existentialsExpansionStrategy=null;
        switch (m_existentialsType) {
        case CREATION_ORDER:
            existentialsExpansionStrategy=new CreationOrderStrategy(blockingStrategy);
            break;
        case EL:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,true);
            break;
        case INDIVIDUAL_REUSE:
            existentialsExpansionStrategy=new IndividualReuseStrategy(blockingStrategy,false);
            break;
        }
        
        m_tableau=new Tableau(tableauMonitor,existentialsExpansionStrategy,m_dlOntology,m_parameters);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
    }
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    public Namespaces getNamespaces() {
        return m_namespaces;
    }
    public Tableau getTableau() {
        return m_tableau;
    }
    public boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) {
        return m_subsumptionChecker.isSubsumedBy(subconcept,superconcept);
    }
    public boolean isSubsumedBy(String subconceptName,String superconceptName) {
        return isSubsumedBy(AtomicConcept.create(subconceptName),AtomicConcept.create(superconceptName));
    }
    public boolean isSatisfiable(AtomicConcept concept) {
        return m_subsumptionChecker.isSatisfiable(concept);
    }
    public boolean isSatisfiable(String conceptName) {
        return isSatisfiable(AtomicConcept.create(conceptName));
    }
    public SubsumptionHierarchy getSubsumptionHierarchy() {
        try {
            return new SubsumptionHierarchy(m_subsumptionChecker);
        }
        catch (SubsumptionHierarchy.SubusmptionCheckerException cantHappen) {
            throw new IllegalStateException("Internal error: subsumption checker threw an exception.");
        }
    }
    public void clearSubsumptionCache() {
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
    }
    public boolean isABoxSatisfiable() {
        return m_tableau.isABoxSatisfiable();
    }
    public void printFlattenedHierarchy(PrintWriter output,SubsumptionHierarchy subsumptionHierarchy) {
        Map<AtomicConcept,Set<AtomicConcept>> flattenedHierarchy=subsumptionHierarchy.getFlattenedHierarchy();
        try {
            for (Map.Entry<AtomicConcept,Set<AtomicConcept>> entry : flattenedHierarchy.entrySet()) {
                output.println(m_namespaces.abbreviateAsNamespace(entry.getKey().getURI()));
                for (AtomicConcept atomicConcept : entry.getValue()) {
                    output.print("    ");
                    output.println(m_namespaces.abbreviateAsNamespace(atomicConcept.getURI()));
                }
                output.println("-----------------------------------------------");
            }
            output.println("! THE END !");
        }
        finally {
            output.flush();
        }
    }
    public void setIndividualReuseStrategyReuseAlways(Set<? extends LiteralConcept> concepts) {
        m_parameters.put("IndividualReuseStrategy.reuseAlways",concepts);
    }
    public void loadIndividualReuseStrategyReuseAlways(File file) throws IOException {
        Set<AtomicConcept> concepts=loadConceptsFromFile(file);
        setIndividualReuseStrategyReuseAlways(concepts);
    }
    public void setIndividualReuseStrategyReuseNever(Set<? extends LiteralConcept> concepts) {
        m_parameters.put("IndividualReuseStrategy.reuseNever",concepts);
    }
    public void loadIndividualReuseStrategyReuseNever(File file) throws IOException {
        Set<AtomicConcept> concepts=loadConceptsFromFile(file);
        setIndividualReuseStrategyReuseNever(concepts);
    }
    protected Set<AtomicConcept> loadConceptsFromFile(File file) throws IOException {
        Set<AtomicConcept> result=new HashSet<AtomicConcept>();
        BufferedReader reader=new BufferedReader(new FileReader(file));
        try {
            String line=reader.readLine();
            while (line!=null) {
                result.add(AtomicConcept.create(line));
                line=reader.readLine();
            }
            return result;
        }
        finally {
            reader.close();
        }
    }
    public void save(File file) throws IOException {
        OutputStream outputStream=new BufferedOutputStream(new FileOutputStream(file));
        try {
            save(outputStream);
        }
        finally {
            outputStream.close();
        }
    }
    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }
    public static HermiT load(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            return (HermiT)objectInputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            IOException error=new IOException();
            error.initCause(e);
            throw error;
        }
    }
    public static HermiT load(File file) throws IOException {
        InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
        try {
            return load(inputStream);
        }
        finally {
            inputStream.close();
        }
    }
}
