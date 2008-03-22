package org.mmadsen.sim.transmissionlab.analysis;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.util.RepastException;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.apache.commons.logging.Log;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 20, 2008
 * Time: 10:04:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitFrequencyAnalysisManager extends AbstractDataCollector {
    private ISimulationModel model = null;
    private Log log = null;
    private int numTraitsPerAgent = 0;
    
    public TraitFrequencyAnalysisManager(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
        // TODO Auto-generated constructor stub
    }

    protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
        Schedule sched = new Schedule();
		sched.scheduleActionBeginning(2, actionToSchedule);
		return sched;
    }

    public void build() {
        this.log.debug("Entering TraitFrequencyAnalysisManager.build()");

        this.log.debug("Exiting TraitFrequencyAnalysisManager.build()");
    }

    public void completion() {
        this.log.debug("Entering TraitFrequencyAnalysisManager.completion()");

        this.log.debug("Exiting TraitFrequencyAnalysisManager.completion()");
    }

    public void initialize() {
        this.log.debug("Entering TraitFrequencyAnalysisManager.initialize()");

        this.log.debug("Exiting TraitFrequencyAnalysisManager.initialize()");
    }

    public void process() {
        this.log.debug("Entering TraitFrequencyAnalysisManager.process()");

        this.log.debug("Exiting TraitFrequencyAnalysisManager.process()");
    }

        // This is a hack until I figure out how to do the SimParameterOptionsMap for all types of param
    private void getModelParameters() {
        try {
            this.numTraitsPerAgent = ((Integer) this.model.getSimpleModelPropertyByName("numTraitsPerAgent"));
        } catch(RepastException ex) {
            this.log.error("Parameter does not exist, fatal error: " + ex.getMessage());
        }
    }
}
