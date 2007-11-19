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

import org.mmadsen.sim.transmissionlab.interfaces.ISharedDataManager;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationFactory;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.population.SingleTraitPopulationFactory;
import org.mmadsen.sim.transmissionlab.rules.NonOverlappingRandomSamplingTransmission;
import org.mmadsen.sim.transmissionlab.rules.MoranProcessRandomSamplingTransmission;
import org.mmadsen.sim.transmissionlab.rules.RandomAgentInfiniteAllelesMutation;
import org.mmadsen.sim.transmissionlab.analysis.TraitFrequencyAnalyzer;
import org.mmadsen.sim.transmissionlab.analysis.OverallStatisticsRecorder;
import org.apache.commons.cli.*;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.util.Random;
import uchicago.src.reflector.ListPropertyDescriptor;

import java.util.Vector;
import java.util.Date;

/**
 * ConformistAgentModel is really designed as a test model.  In designing structured populations,
 * I needed a model where the agents themselves have more complex "responsibilities" and structure
 * than the simple random copying model, to make sure more complex cases are covered by the design.
 * 
 */
public class ConformistAgentModel extends AbstractTLModel
        implements ISharedDataManager, ISimulationModel {
    private Boolean enableNewTopN = true;
    private Boolean enableOverallStats = true;
    private int ewensThetaMultipler = 2;
    private String initialTraitStructure = null;
    private String populationProcessType = null;
    private int maxVariants = 4000;
    private double mu = 0.01;
    private int numAgents = 500;
    private int topNListSize = 40;
    private double switchingProbability = 0.1;

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
        ConformistAgentModel model = new ConformistAgentModel();

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

    public String[] getInitParam() {
        String[] params = {"NumAgents", "Mu", "LengthSimulationRun",
                "EwensThetaMultipler",
                "PopulationProcessType",
                "FileOutputDirectory",
                "SwitchingProbability",
                "EnableNewTopN", "EnableOverallStats",
                "TopNListSize", "InitialTraitStructure"};
        return params;
    }

    public String getName() {
        return "ConformistAgentModel";
    }

    public void specificModelSetup() {
        // Create data collectors, in the order we'd like them to run
        IDataCollector f = new TraitFrequencyAnalyzer(this);
        this.addDataCollector(f);

        IDataCollector o = new OverallStatisticsRecorder(this);
        this.addDataCollector(o);

        // Initialize any RNG we'll need
        Random.createUniform();
    }

    public void resetSpecificModel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void buildPostParameterInitialization() {
        // check to see if we've enabled or disabled any data collectors
        // would be nice if we could genericize this by having a standard parameter for each IDataCollector
        // but I haven't solved the problem of initializing the getInitParams() before we've actually run
        // anything in the model yet...
        if (!this.getEnableNewTopN()) {
            this.log.debug("removing TraitFrequencyAnalyzer from active data collectors - not selected");
            IDataCollector d = this.getDataCollectorByName("TraitFrequencyAnalyzer");
            this.removeDataCollector(d);
        }
        
        if (!this.getEnableOverallStats()) {
            this.log.debug("removing OverallStatisticsRecorder from active data collectors - not selected");
            IDataCollector d = this.getDataCollectorByName("OverallStatisticsRecorder");
            this.removeDataCollector(d);
        }
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
    
    public void buildSpecificPopulation() {
        IPopulationFactory factory = new SingleTraitPopulationFactory();
        this.setPopulation(factory.generatePopulation(this));
        this.maxVariants = this.population.getCurrentMaximumVariant();
    }

    public void buildSpecificPopulationRules() {
        this.log.debug("Setting up population transformation rules");

        // DEBUG: testing only - remove
        //this.addPopulationRule(new NullRule(this));

        if ( this.getPopulationProcessType() == null ) {
            log.info("No PopulationProcess selection made - defaulting to WrightFisherProcess");
            this.setPopulationProcessType("WrightFisherProcess");
        }

        if (this.getPopulationProcessType().equals("WrightFisherProcess")) {
            this.addPopulationRule(new NonOverlappingRandomSamplingTransmission(this));
        } else if (this.getPopulationProcessType().equals("MoranProcess")) {
            this.addPopulationRule(new MoranProcessRandomSamplingTransmission(this));
        } else {
            this.log.error("Unknown PopulationProcessType: " + this.getPopulationProcessType());
        }

        this.log.debug("created Transmission rule: " + this.getPopulationProcessType());

        // now add a mutation rule
        this.addPopulationRule(new RandomAgentInfiniteAllelesMutation(this));
        this.log.debug("created Mutation rule");
    }

    @SuppressWarnings("unchecked")
    protected void addDynamicParameters() {
        this.log.debug("Adding dynamic parameters");
        Vector<String> popPropertyVec = new Vector<String>();
        popPropertyVec.add("SequentialTrait");
        popPropertyVec.add("GaussianTrait");
        ListPropertyDescriptor pd = new ListPropertyDescriptor("InitialTraitStructure", popPropertyVec);

        Vector<String> transmissionRulePropVec = new Vector<String>();
        transmissionRulePropVec.add("WrightFisherProcess");
        transmissionRulePropVec.add("MoranProcess");
        ListPropertyDescriptor pd2 = new ListPropertyDescriptor("PopulationProcessType", transmissionRulePropVec);
        this.descriptors.put("InitialTraitStructure", pd);
        this.descriptors.put("PopulationProcessType", pd2);
    }

    public Boolean getEnableNewTopN() {
        return enableNewTopN;
    }

    public void setEnableNewTopN(Boolean enableNewTopN) {
        this.enableNewTopN = enableNewTopN;
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

    public double getSwitchingProbability() {
        return switchingProbability;
    }

    public void setSwitchingProbability(double switchingProbability) {
        this.switchingProbability = switchingProbability;
    }

}
