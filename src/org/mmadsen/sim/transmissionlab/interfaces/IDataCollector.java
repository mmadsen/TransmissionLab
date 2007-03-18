package org.mmadsen.sim.transmissionlab.interfaces;

import uchicago.src.sim.engine.BasicAction;

/**
 * @author mark
 * Interface IDataCollector represents any class that observe model state and 
 * make observations, calculate statistics, and update graphs and displays
 * The assumption is that the model holds a collection of IDataCollector objects,
 * and at each model step, simply iterates over the objects, calling each
 * object's process() method.  
 * 
 * Separation of observation from running the model itself provides a cleaner,
 * more extensible system whereby additional scientific observations can be
 * made of the same model without touching model or agent code.  
 * 
 * This separation does require that the modeler consider the access class of
 * fields and provide public (or protected) methods to access any data that
 * might be critical for observation.
 * 
 */
public interface IDataCollector {
	// will be called after instantiation to pass any parameters or configuration
	// at a minimum, we need a reference to the model object.  We pass that as a 
	// generic Object, so remember to cast it before use in implementing classes
	public void build(Object model);
	
	// will be called anytime the setup() method is called.  should both clean up
	// from any previous model run and set up for a new run.
	public void initialize();
	
	// will be called on the regular action schedule specified by the model (e.g., 
	// once per tick).  
	public void process();
	
	// can be called to finalize any data collection at the end of a run.  optional
	// but can be useful for closing data files, etc.  
	public void completion();
	
	// ID strings are used to identify the "type" of a data collector, so we can
	// find a reference to the collector object later, for example if we want to
	// do fancy scheduling and add or remove a data collector at a particular time
	public String getDataCollectorName();
	
	// We need to know when and how to schedule each data collector.  Ideally the 
	// model code should know nothing about this
	public BasicAction getDataCollectorSchedule();

    public int getSchedGroupType();

    public void setSchedGroupType(int schedGroupType);
	
}
