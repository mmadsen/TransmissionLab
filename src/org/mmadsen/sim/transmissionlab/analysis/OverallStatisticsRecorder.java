package org.mmadsen.sim.transmissionlab.analysis;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.BasicAction;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.models.TransmissionLabModel;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
import org.apache.commons.logging.Log;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 16, 2007
 * Time: 11:21:28 AM
 * 
 * OverallStatisticsRecorder is responsible for gathering any information about the simulation
 * run as a whole, and recording them to a data file in some format.  At the moment, this will be an
 * all-in one thing, but I can imagine a design whereby other IDataCollector modules store data in a
 * ISharedDataManager object tagged with an attribute which indicates that it should be stored at
 * the end of the run...and then this can be fully generic and just iterate over those items,
 * storing them in CSV or some other format.
 *
 */
public class OverallStatisticsRecorder extends AbstractDataCollector implements IDataCollector {
    public OverallStatisticsRecorder(Object m) {
        super(m);
    }

    private TransmissionLabModel model = null;
	private Log log = null;
	private double stepToStartRecording = 0.0;


    public void build(Object model) {
        this.model = (TransmissionLabModel) model;
        this.log = this.model.getLog();
        this.log.debug("Entering OverallStatisticsRecorder.build()");
    }

    public void completion() {
       // no action needed...yet
    }


    
    @Override
	protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
		Schedule sched = new Schedule();
        sched.scheduleActionAt(this.stepToStartRecording, actionToSchedule);
		return sched;
	}

    public void initialize() {
        this.log.debug("Entering OverallStatisticsRecorder.initialize()");
        this.stepToStartRecording = this.model.getNumTicks();
        this.setSchedGroupType(DataCollectorScheduleType.END);
        this.log.debug("OverallStatisticsRecorder: record data at tick: " + this.stepToStartRecording);
    }

    /*
     * TODO:  record some kind of moving-window average that we're going to have TraitFrequencyAnalyzer calculate and store.
     */
    @Override
    public void process() {
        this.log.debug("OverallStatisticsRecorder running process()");
    }
}
