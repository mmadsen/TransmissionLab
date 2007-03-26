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

import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.models.TransmissionLabModel;

import uchicago.src.sim.util.Random;
/**
 * @author mark
 * RandomAgentInfiniteAllelesMutation implements the original mutation rule
 * from the Bentley et al. 2007 random copying model.  
 */
public class RandomAgentInfiniteAllelesMutation implements
		IPopulationTransformationRule {
	
	private Log log = null;
	private TransmissionLabModel model = null;
	
	public RandomAgentInfiniteAllelesMutation(Log log, TransmissionLabModel model) {
		this.log = log;
		this.model = model;
	}
	
	public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering RandomAgentInfiniteAllelesMutation.transform()");
		return this.mutate(population);
	}

	private IAgentPopulation mutate( IAgentPopulation population ) {
		double mutationProbability = this.model.getMu();
		List<IAgent> agentList = population.getAgentList();
		for (IAgent agent: agentList) {
			agent = (AgentSingleIntegerVariant) agent;
			double chance = Random.uniform.nextDoubleFromTo(0, 1);
			if ( chance < mutationProbability ) {
				//this.log.debug("mutating an individual, chance was: " + chance + " mu was: " + mutationProbability);
				int curMaxVariant = this.model.getMaxVariants();
				curMaxVariant++;
				// NOTE:  This is the only place in the class we know that we're dealing
				// with a specific agent class, so we cast very close to the actual code that needs it.
				((AgentSingleIntegerVariant)agent).setAgentVariant(curMaxVariant);
				this.model.setMaxVariants(curMaxVariant);
			}
		}
		
		return population;
	}
	
}
