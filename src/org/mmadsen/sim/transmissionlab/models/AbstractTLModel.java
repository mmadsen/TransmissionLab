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

import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.ActionGroup;
import uchicago.src.sim.engine.ScheduleBase;
import uchicago.src.sim.util.RepastException;
import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.util.SharedRepository;
import org.mmadsen.sim.transmissionlab.util.PopulationRuleset;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.PropertyConfigurator;

import java.net.URL;
import java.util.*;

/**
 * AbstractTLModel represents a simulation model class which follows not just the
 * Repast conventions, but the structures set up in the TransmissionLab project to
 * simplify computational modeling of transmission & evolution processes.  Extending
 * this class will provide partial or "boilerplate" implementations of methods needed
 * in your build() and setup() methods.
 *
 */
public abstract class AbstractTLModel extends SimModelImpl implements ISimulationModel {
    protected Log log = null;
    protected List<IDataCollector> dataCollectorList = null;
    protected Map<String, IDataCollector> dataCollectorMap = null;
    protected SharedRepository sharedDataRepository = null;
    protected Boolean dataCollectorsPresent = false;
    protected Schedule schedule;
    protected Boolean isBatchExecution = false;
    protected ActionGroup simulationActionGroups = null;
    protected ActionGroup analysisActionGroup = null;
    protected ActionGroup finalActionGroup = null;
    protected ActionGroup modelActionGroup = null;
    protected ActionGroup setupActionGroup = null;
    protected PopulationRuleset popRuleSet = null;
    protected IAgentPopulation population = null;
    protected String fileOutputDirectory = "/tmp";

    public String getFileOutputDirectory() {
        return fileOutputDirectory;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public int getLengthSimulationRun() {
        return lengthSimulationRun;
    }

    public void setLengthSimulationRun(int lengthSimulationRun) {
        this.lengthSimulationRun = lengthSimulationRun;
    }

    protected int lengthSimulationRun = 1000;

    public AbstractTLModel() {
        super();
        URL log4jresource = this.getClass().getResource("log4j.properties");
        PropertyConfigurator.configure(log4jresource);
        this.log = LogFactory.getLog(AbstractTLModel.class);
        this.addDynamicParameters();
    }

    protected abstract void addDynamicParameters();

    protected void addDataCollector(IDataCollector collector) {
        this.dataCollectorList.add(collector);
        this.dataCollectorMap.put(collector.getDataCollectorName(), collector);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    protected void removeDataCollector(IDataCollector collector) {
        this.dataCollectorList.remove(collector);
        this.dataCollectorMap.remove(collector);
    }

    public Log getLog() {
        return log;
    }

    public void removeSharedObject(String key) {
        this.sharedDataRepository.removeEntry(key);
    }

    public Collection<Object> retrieveAllAsCollection() {
        return this.sharedDataRepository.getAllEntries();
    }

    public Object retrieveSharedObject(String key) {
        return this.sharedDataRepository.getEntry(key);
    }

    public void storeSharedObject(String key, Object value) {
        this.sharedDataRepository.putEntry(key, value);
    }

    public Boolean getBatchExecution() {
        return isBatchExecution;
    }

    public void setBatchExecution(Boolean batchExecution) {
        isBatchExecution = batchExecution;
    }

    // pretty much, this is all we have to do to run the stack of population rules...
    // and then we create a BasicAction for it in the modelActionGroup which
    // schedules it to run.
    @SuppressWarnings({"UnusedDeclaration"})
    public void executePopulationRules() {
        this.log.debug("executing population rules at tick: " + this.getTickCount());
        this.population = (IAgentPopulation) this.popRuleSet.transform(this.population);
    }

    public IAgentPopulation getPopulation() {
        return this.population;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setPopulation(IAgentPopulation population) {
        this.population = population;
    }

    protected void addPopulationRule(IPopulationTransformationRule rule) {
        this.popRuleSet.addRule(rule);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    protected IDataCollector getDataCollectorByName(String name) {
        return this.dataCollectorMap.get(name);
    }

    /*
     * In order to ensure that this is called, we use
     * the same template method pattern as does the AbstractDataCollector -> concrete
     * class setup:  we define the concrete setup method here, and define an
     * abstract specificModelSetup() method which each concrete model needs to
     * define.
     */

    protected void resetModel() {
        /*
         * CLEANUP SECTION - Ensure that if this is just the reset button in the GUI,
         * we're ready for another run, not leaking memory, and not working with stale
         * data.
         */
        this.schedule = null;

        // if this is the first time through (i.e., just started the simulation),
        // we don't have data collectors yet, but if this is called by hitting
        // the rest for a second or later run, we need to clean out the graphs
        // and other objects held by data collectors.
        if (this.dataCollectorsPresent) {
            for (IDataCollector collector : this.dataCollectorList) {
                collector.completion();
            }
        }

        this.dataCollectorList = null;
        this.dataCollectorMap = null;
        this.dataCollectorsPresent = false;
        this.sharedDataRepository = null;
        this.popRuleSet = null;
        this.population = null;
    }

    public void setup() {
        this.log.info("Entering setup() for a new simulation run");

        this.resetModel();
        this.resetSpecificModel();

        /*
           * INSTANTIATION AND SETUP SECTION - now that we're clean and safe, construct
           * things that need to be present for the "configuration" portion of the run --
           * i.e., after starting the simulation and displaying the parameter panel, but before
           * hitting "start"
           */


        // HACK:  SimModelImpl doesn't retain any info on whether this model was constructed as a
        // batch-mode simulation or not, so if we were started via the main() method in the model
        // class, great.  If we were started via SimInit.main(), we need a way to tell whether
        // we're in batch mode and this is the "cleanest" way that doesn't involve me hacking the
        // repast source.
        if (!this.getBatchExecution() && this.getController().isBatch()) {
            log.info("Probably started from SimInit.main as batch, will disable GUI elements");
            this.setBatchExecution(true);
        }


        this.dataCollectorList = new ArrayList<IDataCollector>();
        this.dataCollectorMap = new HashMap<String, IDataCollector>();
        this.sharedDataRepository = new SharedRepository();
        this.schedule = new Schedule();
        this.popRuleSet = new PopulationRuleset();
        this.modelActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
        this.analysisActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
        this.simulationActionGroups = new ActionGroup(ActionGroup.SEQUENTIAL);
        this.setupActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
        // at the final tick, when executing things at the finish, we don't have an inherent notion
        // of "ordering" for stuff that happens after the model run.
        this.finalActionGroup = new ActionGroup(ActionGroup.RANDOM);

        // CALL THE CONCRETE SUBCLASS AT THIS POINT TO DO ITS OWN SETUP
        this.specificModelSetup();


        for (IDataCollector collector : this.dataCollectorList) {
            collector.build();
        }

        this.dataCollectorsPresent = true;

        this.log.debug("completed model setup() successfullly");
    }

    public void begin() {
        // Now that we have parameters from a possible GUI run, do any specialized initialization
        // and then construct the initial population of agents
        this.buildPostParameterInitialization();
        this.buildSpecificPopulation();

        // let all of the IDataCollectors initialize themselves.  Then we
        // let the IDataCollector tell us how to schedule itself.
        for (IDataCollector collector : this.dataCollectorList) {
            collector.initialize();
            DataCollectorScheduleType schedGroupType = collector.getSchedGroupType();
            if (schedGroupType == DataCollectorScheduleType.EACH_TICK) {
                this.analysisActionGroup.addAction(collector.getDataCollectorSchedule());
            } else if (schedGroupType == DataCollectorScheduleType.END) {
                this.finalActionGroup.addAction(collector.getDataCollectorSchedule());
            } else if (schedGroupType == DataCollectorScheduleType.BEGIN) {
                this.setupActionGroup.addAction(collector.getDataCollectorSchedule());
            } else {
                this.log.error("ERROR: unknown schedule group type: " + schedGroupType.toString());
            }

        }


        // now let's setup the rules that'll govern the population transformations at each step
        // we do it here rather than in setup() because we want access to GUI parameter selections
        this.buildSpecificPopulationRules();

        // now that the data collector list has been filtered by GUI params
        // and built into an ActionGroup, we can set up the schedule
        this.buildSchedule();

        // and we're off to the races...
    }

    private void buildSchedule() {
        // set up the model actions - this essentially runs the simulation
        this.modelActionGroup.createActionFor(this, "executePopulationRules");

        // we're stacking up two action groups, running them in sequence.
        // first the model action group, then any analysis.  so *all* analyzer
        // modules always run *after* any modeling rule modules in any given
        // model step.  Perhaps this isn't flexible enough for all situations,
        // but it's a good place to start.
        this.simulationActionGroups.addAction(this.modelActionGroup);
        // analysis action group is already set up in begin(), which calls this method
        this.simulationActionGroups.addAction(this.analysisActionGroup);

        // set up the actual schedule.  It's pretty simple since all the complexity
        // is pushed down to the rules themselves.
        // we do use tick #1 as the "setup" tick - where any BasicActions that should run
        // prior to the simulation really starting go -- not necessary in the current
        // model but technically, we could set up all sorts of stuff there.
        // we then start the real simulation on tick #2, running the combined
        // modelActionGroup and analysisActionGroup actions in sequence at each tick.
        // we then run any "final" actions at the end of the simulation.
        // we then *define* the end of the simulation to be the last configured tick,
        // unless the user hits stop first.

        schedule.scheduleActionAt(1, this.setupActionGroup);
        schedule.scheduleActionBeginning(2, this.simulationActionGroups);
        schedule.scheduleActionAtEnd(this.finalActionGroup);
        schedule.scheduleActionAt(lengthSimulationRun, this, "stop", ScheduleBase.LAST);
    }

    public Object getSimpleModelPropertyByName(String property) throws RepastException {
        Object parameter = null;
        try {
            parameter = PropertyUtils.getSimpleProperty(this, property);
        } catch( Exception ex ) {
            throw new RepastException(ex, ex.getMessage());
        }
        return parameter;
    }

    public void setModelPropertyByName(String property, Object value) throws RepastException {
        try {
            PropertyUtils.setSimpleProperty(this, property, value);
        } catch( Exception ex ) {
            throw new RepastException(ex, ex.getMessage());
        }
    }
}
