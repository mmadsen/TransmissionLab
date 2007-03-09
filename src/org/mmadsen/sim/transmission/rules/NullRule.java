package org.mmadsen.sim.transmission.rules;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

/**
 * NullRule does absolutely nothing to the population.  The whole point is to use it
 * for testing how the framework and infrastructure works -- is the ruleset system
 * executing on the correct schedule, in the right order, etc?  Normally we wouldn't
 * use this class for anything in production.
 * @author mark
 *
 */
public class NullRule implements IPopulationTransformationRule{
	private Log log = null;
	@SuppressWarnings("unused")
	private TransmissionLabModel model = null;
	
	public NullRule(Log log, TransmissionLabModel model) {
		this.log = log;
		this.model = model;
	}
	
	public Object transform(Object pop) {
		this.log.debug("Executing NullRule - doing nothing to the population");
		return pop;
	}

}
