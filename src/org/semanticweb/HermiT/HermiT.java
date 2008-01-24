package org.semanticweb.HermiT;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
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
import java.util.Set;
import java.util.HashMap;

import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.Ontology;

import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.existentials.*;
import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.hierarchy.*;
import org.semanticweb.HermiT.debugger.*;

public class HermiT implements Serializable {
    private static final long serialVersionUID=-8277117863937974032L;

    public static enum TableauMonitorType { NONE,TIMING,DEBUGGER_NO_HISTORY,DEBUGGER_HISTORY_ON };
    public static enum DirectBlockingType { PAIR_WISE,EQUALITY,OPTIMAL };
    public static enum BlockingType { ANYWHERE,ANCESTOR };
    public static enum BlockingCacheType { CACHED,NOT_CACHED };
    public static enum ExistentialsType { CREATION_ORDER,EL,INDIVIDUAL_REUSE };

    protected DLOntology m_dlOntology;
    protected Namespaces m_namespaces;
    protected TableauMonitorType m_tableauMonitorType;
    protected DirectBlockingType m_directBlockingType;
    protected BlockingType m_blockingType;
    protected BlockingCacheType m_blockingCacheType;
    protected ExistentialsType m_existentialsType;
    protected Tableau m_tableau;
    protected TableauSubsumptionChecker m_subsumptionChecker;

    
    public HermiT() {
        setTableauMonitorType(TableauMonitorType.NONE);
        setDirectBlockingType(DirectBlockingType.OPTIMAL);
        setBlockingType(BlockingType.ANYWHERE);
        setBlockingCacheType(BlockingCacheType.CACHED);
        setExistentialsType(ExistentialsType.CREATION_ORDER);
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
    public BlockingCacheType getBlockingCacheType() {
        return m_blockingCacheType;
    }
    public void setBlockingCacheType(BlockingCacheType blockingCacheType) {
        m_blockingCacheType=blockingCacheType;
    }
    public ExistentialsType getExistentialsType() {
        return m_existentialsType;
    }
    public void setExistentialsType(ExistentialsType existentialsType) {
        m_existentialsType=existentialsType;
    }
    public void loadOntology(String physicalURI) throws KAON2Exception,InterruptedException {
        DefaultOntologyResolver resolver=new DefaultOntologyResolver();
        String ontologyURI=resolver.registerOntology(physicalURI);
        KAON2Connection connection=KAON2Manager.newConnection();
        connection.setOntologyResolver(resolver);
        Ontology ontology=connection.openOntology(ontologyURI,new HashMap<String,Object>());
        loadKAON2Ontology(ontology);
    }
    public void loadKAON2Ontology(Ontology ontology) throws KAON2Exception {
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        DLOntology dlOntology=clausification.clausify(m_existentialsType==ExistentialsType.INDIVIDUAL_REUSE,ontology,true,noDescriptionGraphs);
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
        if (!dlOntology.canUseNIRule() && m_existentialsType==ExistentialsType.INDIVIDUAL_REUSE)
            throw new IllegalArgumentException("The supplied DL-onyology is not compatible with the individual reuse strategy.");
            
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
        
        TableauMonitor tableauMonitor=null;
        switch (m_tableauMonitorType) {
        case NONE:
            tableauMonitor=null;
            break;
        case TIMING:
            tableauMonitor=new Timer();
            break;
        case DEBUGGER_HISTORY_ON:
            tableauMonitor=new Debugger(m_namespaces,true);
            break;
        case DEBUGGER_NO_HISTORY:
            tableauMonitor=new Debugger(m_namespaces,false);
            break;
        }

        DirectBlockingChecker directBlockingChecker=null;
        switch (m_directBlockingType) {
        case OPTIMAL:
            directBlockingChecker=(m_dlOntology.hasAtMostRestrictions() && m_dlOntology.hasInverseRoles() ? PairWiseDirectBlockingChecker.INSTANCE : EqualityDirectBlockingChecker.INSTANCE);
            break;
        case EQUALITY:
            directBlockingChecker=EqualityDirectBlockingChecker.INSTANCE;
            break;
        case PAIR_WISE:
            directBlockingChecker=PairWiseDirectBlockingChecker.INSTANCE;
            break;
        }
        
        BlockingCache blockingCache=null;
        if (!dlOntology.hasNominals()) {
            switch (m_blockingCacheType) {
            case CACHED:
                blockingCache=new BlockingCache(directBlockingChecker);
                break;
            case NOT_CACHED:
                blockingCache=null;
                break;
            }
        }
        
        BlockingStrategy blockingStrategy=null;
        switch (m_blockingType) {
        case ANCESTOR:
            blockingStrategy=new AncestorBlocking(directBlockingChecker,blockingCache);
            break;
        case ANYWHERE:
            blockingStrategy=new AnywhereBlocking(directBlockingChecker,blockingCache);
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
        
        m_tableau=new Tableau(tableauMonitor,existentialsExpansionStrategy,m_dlOntology);
        m_subsumptionChecker=new TableauSubsumptionChecker(m_tableau);
    }
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    public Namespaces getNamespaces() {
        return m_namespaces;
    }
    public boolean isSubsumedBy(String subconceptName,String superconceptName) {
        return m_subsumptionChecker.isSubsumedBy(subconceptName,superconceptName);
    }
    public boolean isSatisfiable(String conceptName) {
        return m_subsumptionChecker.isSatisfiable(conceptName);
    }
    public SubsumptionHierarchy getSubsumptionHierarchy() {
        try {
            return new SubsumptionHierarchy(m_subsumptionChecker);
        }
        catch (SubsumptionHierarchy.SubusmptionCheckerException cantHappen) {
            throw new IllegalStateException("Internal error: subsumption checker threw an exception.");
        }
    }
    public boolean isABoxSatisfiable() {
        return m_tableau.isABoxSatisfiable();
    }
    public void printFlattenedHierarchy(PrintWriter output,SubsumptionHierarchy subsumptionHierarchy) {
        Map<String,Set<String>> flattenedHierarchy=subsumptionHierarchy.getFlattenedHierarchy();
        try {
            for (Map.Entry<String,Set<String>> entry : flattenedHierarchy.entrySet()) {
                output.println(m_namespaces.abbreviateAsNamespace(entry.getKey()));
                for (String owlClassURI : entry.getValue()) {
                    output.print("    ");
                    output.println(m_namespaces.abbreviateAsNamespace(owlClassURI));
                }
                output.println("-----------------------------------------------");
            }
            output.println("! THE END !");
        }
        finally {
            output.flush();
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
