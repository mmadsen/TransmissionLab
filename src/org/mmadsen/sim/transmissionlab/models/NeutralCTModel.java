/*
 * Copyright (c) 2007, Mark E. Madsen, Alex Bentley, and Carl P. Lipo. All Rights Reserved.
 *
 * This code is offered for use under the terms of the Creative Commons-GNU General Public License
 * http://creativecommons.org/licenses/GPL/2.0/
 *
 * Our intent in licensing this software under the CC-GPL is to provide freedom for researchers, students,
 * and other interested parties to replicate our research results, pursue their own research, etc.  You are, however,
 * free to use the code contained in this package for whatever purposes you wish, provided you adhere to the
 * open license terms specified in LICENSE and GPL.txt
 *
 * See the files LICENSE and GPL.txt in the top-level directory of this source archive for the license
 * details and grant.
 */

package org.mmadsen.sim.transmissionlab.models;

import org.apache.commons.cli.*;
import org.mmadsen.sim.transmissionlab.analysis.OverallStatisticsRecorder;
import org.mmadsen.sim.transmissionlab.analysis.TraitFrequencyAnalyzer;
import org.mmadsen.sim.transmissionlab.analysis.TraitFrequencyFileSnapshot;
import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.population.SingleTraitPopulationFactory;
import org.mmadsen.sim.transmissionlab.population.StructuredPopulationFactory;
import org.mmadsen.sim.transmissionlab.rules.CTRuleFactory;
import org.mmadsen.sim.transmissionlab.rules.NullRule;
import org.mmadsen.sim.transmissionlab.util.SimParameterOptionsMap;
import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.util.Random;

import java.util.*;

/**
 * NeutralCTModel is the concrete model class which implements the neutral theory, "random copying" model.
 * This is a new implementation (given the refactored model abstraction) of the former "main model class"
 * TransmissionLabModel.  The purpose of this refactoring is to create a very simple process for creating
 * a new model out of TransmissionLab "parts."
 */
public class NeutralCTModel extends AbstractTLModel
        implements ISharedDataManager, ISimulationModel {

    public static void main(String[] args) {
        // parse command line options to see if this is a batch run
        Options cliOptions = new Options();
        cliOptions.addOption("b", false, "enable batch mode");
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( cliOptions, args );
        }
        catch( ParseException ex ) {
            System.out.println("ERROR: Command line exception: " + ex.toString());
            System.exit(1);
        }

        SimInit init = new SimInit();
        NeutralCTModel model = new NeutralCTModel();

        model.preModelLoadSetup();

        model.isBatchExecution = cmd.hasOption("b");
        String paramFile = null;

        if ( model.isBatchExecution ) {
            if (!cmd.getArgList().isEmpty()) {
                System.out.println(cmd.getArgList().toString());
                paramFile = (String) cmd.getArgList().get(0);
                System.out.println("debug: " + paramFile);
            }
        }

        System.out.println("INFO: batch mode setting: " + model.isBatchExecution);

        init.loadModel(model, paramFile, model.isBatchExecution);
    }

    private Boolean enableNewTopN = true;
    private Boolean enableOverallStats = true;
    private List<String> parameterList = null;


    private Boolean enableTraitFrequencyFileSnapshots = false;
    private int ewensThetaMultipler = 2;
    private String initialTraitStructure = null;
    private String populationProcessType = null;
    private String populationStructure = null;
    private String mutationProcessType = null;
    private int numberFileSnapshots = 1;
    private int maxVariants = 4000;
    private double mu = 0.01;
    private int numAgents = 500;
    private int topNListSize = 40;

    public Boolean getEnableTraitFrequencyFileSnapshots() {
        return enableTraitFrequencyFileSnapshots;
    }

    public void setEnableTraitFrequencyFileSnapshots(Boolean enableTraitFrequencyFileSnapshots) {
        this.enableTraitFrequencyFileSnapshots = enableTraitFrequencyFileSnapshots;
    }

    public int getNumberFileSnapshots() {
        return numberFileSnapshots;
    }

    public void setNumberFileSnapshots(int numberFileSnapshots) {
        this.numberFileSnapshots = numberFileSnapshots;
    }


    public Boolean getEnableOverallStats() {
        return enableOverallStats;
    }

    public void setEnableOverallStats(Boolean enableOverallStats) {
        this.enableOverallStats = enableOverallStats;
    }

    public int getEwensThetaMultipler() {
        return ewensThetaMultipler;
    }

    public void setEwensThetaMultipler(int ewensThetaMultipler) {
        this.ewensThetaMultipler = ewensThetaMultipler;
    }

    public String getInitialTraitStructure() {
        return initialTraitStructure;
    }

    public void setInitialTraitStructure(String initialTraitStructure) {
        this.initialTraitStructure = initialTraitStructure;
    }

    public String getPopulationProcessType() {
        return populationProcessType;
    }

    public void setPopulationProcessType(String populationProcessType) {
        this.populationProcessType = populationProcessType;
    }

    public String getPopulationStructure() {
        return populationStructure;
    }

    public void setPopulationStructure(String populationStructure) {
        this.populationStructure = populationStructure;
    }

    public String getMutationProcessType() {
        return mutationProcessType;
    }

    public void setMutationProcessType(String mutationProcess) {
        this.mutationProcessType = mutationProcess;
    }

    public int getMaxVariants() {
        return maxVariants;
    }

    public void setMaxVariants(int maxVariants) {
        this.maxVariants = maxVariants;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public int getNumAgents() {
        return numAgents;
    }

    public void setNumAgents(int numAgents) {
        this.numAgents = numAgents;
    }

    public int getTopNListSize() {
        return topNListSize;
    }

    public void setTopNListSize(int topNListSize) {
        this.topNListSize = topNListSize;
    }

    public void preModelLoadSetup() {
        // Set up static parameters for our model
        this.parameterList = new ArrayList<String>();
        this.parameterList.add("NumAgents");
        this.parameterList.add("Mu");
        this.parameterList.add("LengthSimulationRun");
        this.parameterList.add("EwensThetaMultiplier");
        this.parameterList.add("FileOutputDirectory");
        this.parameterList.add("NumberFileSnapshots");
        this.parameterList.add("EnableOverallStats");
        this.parameterList.add("EnableTraitFrequencyFileSnapshots");
        this.parameterList.add("TopNListSize");

        // here we make List of SimParameterOptionsMap objects from various sources,
        // such as population factories, rules, etc.  This would be an ideal thing
        // to turn into an XML configuration thing, since it would just list classes
        // that needed to contribute configuration data to the overall model...for now
        // we do it statically in a concrete model class
        List<SimParameterOptionsMap> listParamOptMap = new ArrayList<SimParameterOptionsMap>();

        SingleTraitPopulationFactory stpFactory = new SingleTraitPopulationFactory(this);
        listParamOptMap.add(stpFactory.getSimParameterOptionsMap());
        StructuredPopulationFactory spFactory = new StructuredPopulationFactory(this);
        listParamOptMap.add(spFactory.getSimParameterOptionsMap());
        CTRuleFactory ruleFactory = new CTRuleFactory(this);
        listParamOptMap.add(ruleFactory.getSimParameterOptionsMap());

        log.debug("listParamOptMap: " + listParamOptMap.toString());

        this.addDynamicParameters(listParamOptMap);
    }

    @SuppressWarnings("unchecked")
    protected void addDynamicParameters(List<SimParameterOptionsMap> listParamOptMap ) {
        this.log.debug("Adding dynamic parameters");

        // add everything that comesin the listParamOptMap
        for(SimParameterOptionsMap poMap: listParamOptMap) {

            for(String parameter: poMap.getParameterNames())   {
                this.log.debug("Processing dynamic parameter " + parameter);
                Vector<String> paramVector = new Vector<String>();

                List<String> paramOptions = poMap.getOptionsForParam(parameter);
                this.log.debug("   Options: " + paramOptions.toString());
                paramVector.addAll(paramOptions);

                // create a property descriptor and register it in the descriptor panel
                ListPropertyDescriptor pd = new ListPropertyDescriptor(parameter, paramVector);
                this.parameterList.add(parameter);
                this.descriptors.put(parameter, pd);
            }
        }
    }

    public void specificModelSetup() {

        // Create data collectors, in the order we'd like them to run
        IDataCollector f = new TraitFrequencyAnalyzer(this);
        this.addDataCollector(f);

        IDataCollector o = new OverallStatisticsRecorder(this);
        this.addDataCollector(o);

        IDataCollector s = new TraitFrequencyFileSnapshot(this);
        this.addDataCollector(s);

        // Initialize any RNG we'll need
        Random.createUniform();
    }

    public void resetSpecificModel() {
        // nothing needed in this particular model beyond what the framework provides.
    }

    public void buildPostParameterInitialization() {
        // we no longer allow disabling the TraitFrequencyAnalyzer, since it is relied upon by all
        // the other data collectors for results, so turning it off is problematic.  I really
        // need to refactor its *graphs* out into another data collector, and maybe create a split
        // between Analyzers and Visualizers, running analyzers first in a step, then visualizers.
        // Analyzers would be fundamental to a model, specified in the model class, whereas visualizers
        // would be things you can turn off and on in the parameters....?

        if (!this.getEnableOverallStats()) {
            this.log.debug("removing OverallStatisticsRecorder from active data collectors - not selected");
            IDataCollector d = this.getDataCollectorByName("OverallStatisticsRecorder");
            this.removeDataCollector(d);
        }

        if (!this.getEnableTraitFrequencyFileSnapshots()) {
            this.log.debug("removing TraitFrequencyFileSnapshot from active data collectors - not selected");
            IDataCollector d = this.getDataCollectorByName("TraitFrequencyFileSnapshot");
            this.removeDataCollector(d);
        }
    }

    public void buildSpecificPopulation() {
        IAgentSetFactory agentFactory = new SingleTraitPopulationFactory(this);
        IPopulationFactory structureFactory = new StructuredPopulationFactory(this);
        IAgentSet modelAgentSet = agentFactory.generatePopulation();
        IAgentPopulation modelPopulation  = structureFactory.generatePopulation(modelAgentSet);
        this.setPopulation(modelPopulation);
        this.maxVariants = this.population.getCurrentMaximumVariant();
    }

    public void buildSpecificPopulationRules() {
        this.log.debug("Setting up population transformation rules");

        // DEBUG: testing only - remove
        //this.addPopulationRule(new NullRule(this));

        CTRuleFactory ruleFactory = new CTRuleFactory(this);
        // add rules in the order we want them to run
        this.log.info("Adding population process" );
        this.addPopulationRule(ruleFactory.getRuleForParameter(CTRuleFactory.POP_PROCESS_PROPERTY));
        this.log.info("Adding mutation rule" );
        this.addPopulationRule(ruleFactory.getRuleForParameter(CTRuleFactory.MUTATION_TYPE_PROPERTY));
    }

    public void buildSpecificPerRunIdentifier() {
        // Generate a unique run identifier
        Date now = new Date();
        StringBuffer ident = new StringBuffer();
        ident.append("TL-run-");
        ident.append(this.numAgents);
        ident.append("-");
        ident.append(this.mu);
        ident.append("-");
        ident.append(this.topNListSize);
        ident.append("-");
        ident.append(now.getTime());
        this.uniqueRunIdentifier = ident.toString();
    }

    public String[] getInitParam() {
        String[] stringArray = {};
        return this.parameterList.toArray(stringArray);
    }

    public String getName() {
        return "NeutralCTModel";
    }
}
