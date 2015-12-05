package org.semanticweb.HermiT;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.protege.editor.owl.model.inference.ReasonerPreferences;
import org.semanticweb.HermiT.Configuration.PrepareReasonerInferences;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

public class ProtegeReasonerFactory extends AbstractProtegeOWLReasonerInfo {
    protected final Reasoner.ReasonerFactory factory=new Reasoner.ReasonerFactory();

    public BufferingMode getRecommendedBuffering() {
        return BufferingMode.BUFFERING;
    }
    public OWLReasonerFactory getReasonerFactory() {
        return factory;
    }

    public OWLReasonerConfiguration getConfiguration(ReasonerProgressMonitor monitor) {
        Configuration configuration=factory.getProtegeConfiguration(null);
        configuration.reasonerProgressMonitor=monitor;
        try {
            // see whether the Protege version of the user already has the reasoner preferences tab
            AbstractProtegeOWLReasonerInfo.class.getMethod("getOWLModelManager",(Class<?>[])null);
            // if we are not thrown into the catch block, we can initialise the reasoner preferences
            ReasonerPreferences preferences=this.getOWLModelManager().getReasonerPreferences();
            PrepareReasonerInferences prepareReasonerInferences=new PrepareReasonerInferences();

            // class classification
            prepareReasonerInferences.classClassificationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_CLASS_UNSATISFIABILITY) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_CLASSES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DISJOINT_CLASSES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_CLASSES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_DOMAINS) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_RANGES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATATYPE_PROPERTY_DOMAINS
            );

            // object property classification
            prepareReasonerInferences.objectPropertyClassificationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_OBJECT_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_INVERSE_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_OBJECT_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_OBJECT_PROPERTY_UNSATISFIABILITY
            );

            // data property classification
            prepareReasonerInferences.dataPropertyClassificationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_DATATYPE_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_DATATYPE_PROPERTIES) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATA_PROPERTY_ASSERTIONS
            );

            // realisation
            prepareReasonerInferences.realisationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERED_CLASS_MEMBERS) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_TYPES
            );

            // object property realisation
            prepareReasonerInferences.objectPropertyRealisationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_ASSERTIONS
            );

            // data property realisation
            prepareReasonerInferences.dataPropertyRealisationRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATA_PROPERTY_ASSERTIONS
            ); // cannot be switched off, but is usually fast since we only compute obvious assertions by syntactic analysis

            // object property domain & range
            prepareReasonerInferences.objectPropertyDomainsRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_DOMAINS
            );
            prepareReasonerInferences.objectPropertyRangesRequired=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_RANGES
            );

            // sameAs
            prepareReasonerInferences.sameAs=preferences.isEnabled(
                ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATA_PROPERTY_ASSERTIONS) ||
                preferences.isEnabled(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SAMEAS_INDIVIDUAL_ASSERTIONS
            ); // we also substitute same individuals now

            configuration.prepareReasonerInferences=prepareReasonerInferences;
        }
        catch (java.lang.NoSuchMethodException e) {
            // do nothing, prepareReasoner() will just execute all methods because the user's Protege
            // version does not yet have the reasoner preferences tab that we can use to customize
            // prepareReasoner()
        }
        return configuration;
    }
    public void initialise() throws Exception {
    }
    public void dispose() throws Exception {
    }
}