package org.mmadsen.sim.transmissionlab.analysis;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.models.TransmissionLabModel;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;

import uchicago.src.sim.engine.ActionUtilities;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;

/**
 * Abstract base class for IDataCollector implementations, providing useful default implementations of common methods.
 * <p>
 * Classes which implement IDataCollector may extend this base class in order to get useful default implementations
 * of the methods by which the model handles scheduling and other "mechanical" aspects of data collection.  The
 * rest of the interface methods which are specific to data collection are left unimplemented
 */

public abstract class AbstractDataCollector implements IDataCollector {
	private TransmissionLabModel model = null;
	private Log log = null;
    private final String TYPE_CODE = this.getClass().getSimpleName();

    /**
     * Returns an enumerated constant of type {@link org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType DataCollectorScheduleType}
     * which indicates the general scheduling "type" of this data collector.  This is used to sort data collectors into ActionGroups for
     * execution before, during, or after the simulation run.
     * @return scheduleType - DataCollectorScheduleType enumerated constant indicating the schedule "type" of the data collector.
     */
    public DataCollectorScheduleType getSchedGroupType() {
        return this.schedGroupType;
    }

    protected void setSchedGroupType(DataCollectorScheduleType schedGroupType) {
        this.schedGroupType = schedGroupType;
    }

    // default to data collection running on every model tick unless this is overriden by a subclass
    private DataCollectorScheduleType schedGroupType = DataCollectorScheduleType.EACH_TICK;


    public AbstractDataCollector(Object m) {
		this.model = (TransmissionLabModel) m;
		this.log = this.model.getLog();
	}

    /**
     * Default implementation of a unique name for the data collector, returns the string representation
     * of the class name, without package path.  Override this if you want per-object identifiers, or if
     * the same "simple class name" occurs in more than one package and you want a package.class identifier.
     * @return name - String representing the simple class name of the data collector object
     */
    public String getDataCollectorName() {
        return this.TYPE_CODE;
    }


    /**
     * Returns a BasicAction instance (likely Schedule) defined by a subclass, to run the process() method
     * of this instance.  This default implementation, along with {@link #getBasicActionForDataCollector},
     * wires up the process() method of the data collector to run on a given schedule.  The developer of a
     * subclass does not have to know anything except a handful of Schedule methods beginning with scheduleAction...
	 * @return schedule - returns a BasicAction instance
	 * 
	 */
	public BasicAction getDataCollectorSchedule() {
		this.log.debug("Entering getDataCollectorSchedule for " + this.getClass().getSimpleName());
		return this.getSpecificSchedule(this.getBasicActionForDataCollector());
	}
	
	protected BasicAction getBasicActionForDataCollector() {
		return ActionUtilities.createActionFor(this, "process");
	}

    // CONCRETE CLASSES MUST IMPLEMENT ALL METHODS BELOW THIS COMMENT

    /**
	 * Every data collector class must implement this method, returning a Schedule object.
     * <p>
     * Access is protected because model classes should not see this; it is intended to be implemented by
     * subclasses but called by the superclass template method {@link #getDataCollectorSchedule getDataCollectorSchedule}.
	 *@return Schedule - must return a Schedule object representing a schedule for this data collector
	 * 
	 */
	abstract protected Schedule getSpecificSchedule(BasicAction actionToSchedule);

    abstract public void build(Object model);

	abstract public void completion();

    abstract public void initialize();

    abstract public void process();

}
