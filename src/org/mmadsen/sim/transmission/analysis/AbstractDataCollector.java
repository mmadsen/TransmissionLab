package org.mmadsen.sim.transmission.analysis;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.interfaces.IDataCollector;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

import uchicago.src.sim.engine.ActionUtilities;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;

public abstract class AbstractDataCollector implements IDataCollector {
	private TransmissionLabModel model = null;
	private Log log = null;
	
	public AbstractDataCollector(Object m) {
		this.model = (TransmissionLabModel) m;
		this.log = this.model.getLog();
	}

	public void build(Object model) {
		// nothing by default
	}

	public void completion() {
		// nothing by default
	}

	public String getDataCollectorName() {
		return null;
	}

	/**
	 * @return schedule - returns a 
	 * 
	 */
	public BasicAction getDataCollectorSchedule() {
		this.log.debug("Entering getDataCollectorSchedule for " + this.getClass().getCanonicalName());
		return this.getSpecificSchedule(this.getBasicActionForDataCollector());
	}
	
	protected BasicAction getBasicActionForDataCollector() {
		return ActionUtilities.createActionFor(this, "process");
	}
	
	/**
	 * Every data collector class must implement this method, returning a Schedule object
	 *@return Schedule - must return a Schedule object representing a schedule for this data collector
	 * 
	 */
	protected abstract Schedule getSpecificSchedule(BasicAction actionToSchedule);
	
	public void initialize() {
		// nothing by default

	}

	public void process() {
		// nothing by default

	}

}
