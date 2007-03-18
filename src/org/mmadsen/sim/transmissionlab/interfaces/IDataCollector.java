package org.mmadsen.sim.transmissionlab.interfaces;

import uchicago.src.sim.engine.BasicAction;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;

/**
 * Interface IDataCollector represents any class that observe model state and
 * make observations, calculate statistics, and update graphs and displays
 * <p>
 * Each IDataCollector instance is responsible for its own schedule, which can be one of
 * several types:
 * <ul>
 * <li>setup:  run this data collector once at the beginning of the simulation (not terribly useful in most cases)</li>
 * <li>simulation: run this data collector once during each tick of the simulation, after the "model" per-tick activities</li>
 * <li>final:  run this data collector once after the final tick of the simulation run (or when the user stops the simulation)</li>
 * </ul><p>
 * {@link org.mmadsen.sim.transmissionlab.analysis.AbstractDataCollector},
 * which all instantiations of the interface are encouraged to extend, provides static constants
 * for these scheduling types, as well as default implementations of scheduling and schedule type methods.
 * <p>
 * Separation of observation from running the model itself provides a cleaner,
 * more extensible system whereby flexible observations can be
 * made of the same model without touching model or agent code.
 * <p>
 * This separation does require that the modeler consider the access class of
 * fields and provide public methods to access any data that
 * might be critical for observation.
 * @author mark
 * 
 */
public interface IDataCollector {

    /**
     * build() is responsible for construction of any data collector resources and state that are
     * known at the time of model setup -- that is, when the Repast framework constructs the
     * model class and calls SimModelImpl.setup().  No information from the GUI parameter panel
     * is available to the data collector yet.  For construction activities which require user
     * parameter settings, see the initialize() method, which will/should be called by
     * SimModelImpl.begin() or a delegated method.
     * @param model - TransmissionLabModel instance representing the model instantiating the IDataCollector.  Passed
     * as a generic Object so cast this to TransmissionLabModel (or a subclass) before doing anything with it.
     * @see org.mmadsen.sim.transmissionlab.interfaces.IDataCollector#initialize
     */
    public void build(Object model);
	

    /**
     * initialize() is responsible for any construction or initialization steps that need to happen
     * before the model begins executing, but require configuration from the user.  That is,
     * initialize is called by SimModelImpl.begin() right at the moment the simulation starts, but
     * before tick #1.
     */
    public void initialize();

    /**
     * process() executes the data collection logic.  This method will be called by the model given
     * whatever schedule the IDataCollector instance specifies via
     * {@link org.mmadsen.sim.transmissionlab.interfaces.IDataCollector#getDataCollectorSchedule getDataCollectorSchedule}.  This could
     * be once per tick, at the beginning of the simulation, or after the "end" of the simulation.  Once
     * called, the process() method will have access to the model instance passed in build(), and via
     * that reference, to whatever data sources the simulation model provides.
     */
	public void process();

    /**
     * completion() defines any actions needed at the "end" of a simulation to clean up or otherwise
     * shut down the data collector in an orderly manner.  This might be closing disk files or
     * flushing cached data to a database instance, and so on.  In many cases this method might be
     * empty, if the data collector doesn't need to do anything to clean up.  In such cases, the
     * method signature needs to be implemented (since it's part of the interface), but it doesn't
     * need to do anything.
     *
     */ 
	public void completion();
	

    /**
     * Returns a constant String indicating the identity of the data collector, such that models
     * can find a given data collector in a map or other data structure if needed.  This is used
     * principally in a somewhat kludgy mechanism to disable a data collector given GUI settings
     * after the data collector has already been instantiated in model setup.
     * @return name - constant String indicating the "identity" of the data collector instance.
     *
     */
    public String getDataCollectorName();

    /**
     * Returns a BasicAction (or subclass thereof, like Schedule) which specifies the schedule for executing this
     * data collector's process() method.  That schedule can then be integrated into the model's master schedule.
     * @return schedule - Returns a subclass of BasicAction which specifies the schedule required by this data collector.
     */
	public BasicAction getDataCollectorSchedule();

    /**
     * Returns an instance of the enum {@link org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType DataCollectorScheduleType} which
     * specifies what "kind" of schedule object to expect from this data collector -- whether, for example, the schedule
     * is a per-tick schedule, one that runs at the end of the simulation, or before the start of the simulation.  This
     * is useful for sorting individual Schedule objects into ActionGroups which are run at different points in the overall
     * simulation.
     * @return scheduleType - returns an instance of the DataCollectorScheduleType enum indicating the scheduling type
     *
     */
    public DataCollectorScheduleType getSchedGroupType();
    
	
}
