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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

public class Configuration implements Serializable,Cloneable,OWLReasonerConfiguration {
    private static final long serialVersionUID=7741510316249774519L;

    /**
     * Tableau monitors can be used to be informed about what HermiT does and they can be useful for debugging the reasoner.
     */
    public static enum TableauMonitorType {
        /**
         * The standard setting is no monitor, i.e., no information is recorded and printed about what the reasoner does.
         */
        NONE,
        /**
         * The TIMING tableau monitor print information about the tableau (number of nodes etc) in certain time intervals.
         */
        TIMING,
        /**
         * Waits at certain points (e.g., before building a tableau) for a keystroke by the user and is apart from that like
         * TIMING.
         */
        TIMING_WITH_PAUSE,
        /**
         * This opens a debugging application for HermiT. HermiT can be controlled with special commands from within the
         * debugging application. Without history HermiT does not record information about how assertions have been derived, so
         * one cannot see the derivation history for an assertion.
         */
        DEBUGGER_NO_HISTORY,
        /**
         * This opens a debugging application for HermiT. HermiT can be controlled with special commands from within the
         * debugging application. HermiT will keep, for each derived fact/assertion, how the assertion was derived. This is
         * obviously using a lot more memory than normal, but can be useful when debugging the reasoner.
         */
        DEBUGGER_HISTORY_ON
    }

    /**
     * Sets the blocking type used by HermiT. This can be combined with settings for the blocking strategy (anywhere or
     * ancestor or core blocking).
     */
    public static enum DirectBlockingType {
        /**
         * Forces HermiT to use single blocking even if the ontology is not suitable for single blocking (contains inverse roles).
         */
        SINGLE,
        /**
         * Forces HermiT to use pairwise blocking even if the ontology does not require that (contains no inverses).
         */
        PAIR_WISE,
        /**
         * Chooses the optimal blocking. If the ontology contains nominals HermiT will use single simple core blocking
         * (works even with inverses) and otherwise HermiT uses single blocking if the ontology contains no inverses and
         * pairwise blocking otherwise.
         */
        OPTIMAL
    }

    /**
     * The blocking strategy determines how which nodes HermiT considers for blockers.
     */
    public static enum BlockingStrategyType {
        /**
         * Forces HermiT to use anywhere blocking. Anywhere blocking usually creates smaller models than ancestor blocking.
         * It might be slower, but seems to work better in average cases.
         */
        ANYWHERE,
        /**
         * Forces HermiT to use ancestor blocking. Generates usually the biggest model, but can be faster in some cases than
         * the other strategies.
         */
        ANCESTOR,
        /**
         * An approximate blocking strategy and HermiT validates whether the block is ok before terminating. Concepts that
         * were added nondeterministically and concepts that are propagated from the parent node are considered for
         * blocking. Produces smaller models (less memory) than ANYWHERE and ANCESTOR, but can be slower in
         * particular since caching cannot be used with this blocking strategy. Caching is normally used if the
         * ontology does not contain nominals.
         */
        COMPLEX_CORE,
        /**
         * An approximate blocking strategy and HermiT validates whether the block is ok before terminating. Only
         * concepts that where added when the node was created are considered for blocking (e.g., for
         * ClassAssertion(ObjectSomeValuesFrom(r C) a) an r-successor say b is created with C in the label of b and
         * C counts for blocking; any concept added later to the label is not considered for blocking. ) This can generate
         * very small models, but might increase the time required in particular for classification of ontologies without
         * nominals since caching cannot yet be used with this strategy. This is the default for ontologies with nominals
         * where caching cannot be used anyway.
         */
        SIMPLE_CORE,
        /**
         * If the ontology contains nominals HermiT uses SIMPLE_CORE otherwise ANYWHERE.
         */
        OPTIMAL
    }

    /**
     * Switches caching on or off (caching can only be used with non-core blocking and if the ontology does not contain nominals).
     * With caching HermiT caches blockers. The first satisfiability test can be slow, but in subsequent tests blocking can occur
     * much earlier from cached blockers, which saves time and memory.
     */
    public static enum BlockingSignatureCacheType {
        /**
         * Forces HermiT to use caching (if compatible with the ontology).
         */
        CACHED,
        /**
         * Disables caching.
         */
        NOT_CACHED
    }

    /**
     * Sets a strategy type that determines how HermiT expands the model.
     */
    public static enum ExistentialStrategyType {
        /**
         * Strategy for expanding all existentials on the oldest node in the tableau with unexpanded existentials.
         * This usually closely approximates a breadth-first expansion. (Existentials introduced onto parent nodes
         * as a result of constraints on their children can produce newer nodes of lower depth than older nodes,
         * which could result in slight non-breadth-first behavior.)
         */
        CREATION_ORDER,
        /**
         * Individual reuse tries to reuse existing individuals first before creating a fresh successor. This can
         * introduce a lot of non-determinism, but the models can be much smaller (less memory required).
         */
        INDIVIDUAL_REUSE,
        /**
         * For EL ontologies this existential strategy can be set to use a deterministic version of individual
         * reuse that behaves similar to EL-style algorithms.
         */
        EL
    }

    /**
     * One can implement an instance of this class and pass it to HermiT. HermiT will then print warning with the
     * warning() method of the interface, e.g., if it ignores an unsupported datatype. HermiT does not provide an
     * implementation for the interface itself though.
     */
    public WarningMonitor warningMonitor;
    /**
     * If a progress monitor is set, HermiT will report the progress of a classification task. This is used for
     * example by Protege.
     */
    public ReasonerProgressMonitor reasonerProgressMonitor;
    public TableauMonitorType tableauMonitorType;
    public DirectBlockingType directBlockingType;
    public BlockingStrategyType blockingStrategyType;
    public BlockingSignatureCacheType blockingSignatureCacheType;
    public ExistentialStrategyType existentialStrategyType;
    /**
     * If HermiT encounters a non-OWL2 datatype, it normally throws an error. If set to true, axioms containing unsupported
     * datatypes will be ignored.
     */
    public boolean ignoreUnsupportedDatatypes;
    /**
     * Can be used to set a custom Tableau monitor.
     */
    public TableauMonitor monitor;
    /**
     * The parameters are passed to the Tableau class instance, but currently no parameters are used.
     */
    public Map<String,Object> parameters;
    /**
     * If set to some value, reasoning in HermiT is interrupted as soon as any individual reasoning task takes any longer than
     * individualTaskTimeout ms.
     */
    public long individualTaskTimeout;
    public IndividualNodeSetPolicy individualNodeSetPolicy;
    public FreshEntityPolicy freshEntityPolicy;
    /**
     * If set to true, then each disjunct of a disjunction is associated with a punish factor and whenever a disjunct causes
     * a clash, the punish factor is increased. Whenever HermiT has to pick a disjunction, it picks the disjunction with
     * the least punish factor that has not yet been tried for that node and disjunction.
     */
    public boolean useDisjunctionLearning;
    /**
     * If set to true, then axioms that are to be added or removed are buffered and the addition and removal is only performed
     * when the flush() method of the reasoner is called.
     */
    public boolean bufferChanges;
    /**
     * The default value is true and HermiT will throw an exception if it finds the ontology to be inconsistent.
     *
     * If set to false, HermiT will not throw an exception for inconsistent ontologies. The only exception is when asked for data property values for an
     * individual and a data property because any of the infinitely many data values would be an answer. Restricting answers to just the data values in the
     * signature does not make much sense. If the parameter is set to false and the ontology is inconsistent, all classes occurring in the ontology are, for
     * example, returned as subclasses of owl:nothing. Some answers might be unexpected or unintuitive, e.g., a property will be both reflexive and irreflexive
     * etc. Use with care, e.g., only when trying to get explanations of inconsistencies, where throwing an error might not be helpful.
     */
    public boolean throwInconsistentOntologyException;

    public PrepareReasonerInferences prepareReasonerInferences;

    /**
     * The default value is false and HermiT will use a specialiased classification strategy for deterministic ontologies, which often is faster, but not always.
     * If the value is set to true, then HermiT will use the Quasi Ordering Classification method even for deterministic ontologies.
     */
    public boolean forceQuasiOrderClassification;

    public Configuration() {
        warningMonitor=null;
        reasonerProgressMonitor=null;
        tableauMonitorType=Configuration.TableauMonitorType.NONE;
        directBlockingType=Configuration.DirectBlockingType.OPTIMAL;
        blockingStrategyType=Configuration.BlockingStrategyType.OPTIMAL;
        blockingSignatureCacheType=Configuration.BlockingSignatureCacheType.CACHED;
        existentialStrategyType=Configuration.ExistentialStrategyType.CREATION_ORDER;
        ignoreUnsupportedDatatypes=false;
        monitor=null;
        parameters=new HashMap<String,Object>();
        individualTaskTimeout=-1;
        bufferChanges=true;
        individualNodeSetPolicy=IndividualNodeSetPolicy.BY_NAME;
        freshEntityPolicy=FreshEntityPolicy.ALLOW;
        useDisjunctionLearning=true;
        throwInconsistentOntologyException=true;
        prepareReasonerInferences=null;
        forceQuasiOrderClassification=false;
    }
    protected void setIndividualReuseStrategyReuseAlways(Set<? extends AtomicConcept> concepts) {
        parameters.put("IndividualReuseStrategy.reuseAlways",concepts);
    }
    public void loadIndividualReuseStrategyReuseAlways(File file) throws IOException {
        Set<AtomicConcept> concepts=loadConceptsFromFile(file);
        setIndividualReuseStrategyReuseAlways(concepts);
    }
    protected void setIndividualReuseStrategyReuseNever(Set<? extends AtomicConcept> concepts) {
        parameters.put("IndividualReuseStrategy.reuseNever",concepts);
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
    public Configuration clone() {
        try {
            Configuration result=(Configuration)super.clone();
            result.parameters=new HashMap<String,Object>(parameters);
            return result;
        }
        catch (CloneNotSupportedException cantHappen) {
            return null;
        }
    }
    public static interface WarningMonitor {
        void warning(String warning);
    }
    public long getTimeOut() {
        return individualTaskTimeout;
    }
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return individualNodeSetPolicy;
	}
	public ReasonerProgressMonitor getProgressMonitor() {
		return reasonerProgressMonitor;
	}
	public FreshEntityPolicy getFreshEntityPolicy() {
		return freshEntityPolicy;
	}
	public static class PrepareReasonerInferences {
	    public boolean classClassificationRequired=true;
	    public boolean objectPropertyClassificationRequired=true;
	    public boolean dataPropertyClassificationRequired=true;
	    public boolean objectPropertyDomainsRequired=true;
	    public boolean objectPropertyRangesRequired=true;
	    public boolean realisationRequired=true;
	    public boolean objectPropertyRealisationRequired=true;
	    public boolean dataPropertyRealisationRequired=true;
	    public boolean sameAs=true;
	}
}
