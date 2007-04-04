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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import uchicago.src.sim.util.Random;

public class NonOverlappingRandomSamplingTransmission implements
		IPopulationTransformationRule {

	private Log log = null;
	private ISimulationModel model = null;
	
	public NonOverlappingRandomSamplingTransmission(ISimulationModel model) {
		this.model = model;
        this.log = this.model.getLog();
    }
	
	
	public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering NonOverlappingRandomSamplingTransmission.transform()");
		return this.transmit(population);
	}

	private IAgentPopulation transmit(IAgentPopulation population) {
		List<IAgent> agentList = population.getAgentList();
		List<IAgent> newAgentList = new ArrayList<IAgent>();
		int numAgents = agentList.size();
		
		// since we're just sampling with replacement, we'll simply make a new
		// list of agents randomly from the old list, and replace the agent list
		// in the population object
		// NOTE:  this is the ONLY place in the class where we know that we're dealing
		// with a specific agent class, rather than a generic IAgent...
		for(int i = 0; i < numAgents; i++) {
			int index = Random.uniform.nextIntFromTo(0, numAgents - 1);
			//log.debug("transmit: choosing agent " + index + " to copy");
			AgentSingleIntegerVariant agent = (AgentSingleIntegerVariant) agentList.get(index);
			newAgentList.add(new AgentSingleIntegerVariant(agent.getAgentVariant()));
		}
		
		// test to make sure we have two populations of equal size!
		if ( newAgentList.size() != agentList.size()) {
			this.log.error("BUG: the new agent population isn't the same size as old and this is a constant population model");
		}
		
		// finally, store the new agent list
		population.replaceAgentList(newAgentList);
		
		return population;
	}
	

}