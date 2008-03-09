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

package org.mmadsen.sim.transmissionlab.rules;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;

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
	private ISimulationModel model = null;
	
    // needed for instantiation via reflection
    public NullRule() {}

    public NullRule(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }

	
	public Object transform(Object pop) {
		this.log.debug("Executing NullRule - doing nothing to the population");
		return pop;
	}

}
