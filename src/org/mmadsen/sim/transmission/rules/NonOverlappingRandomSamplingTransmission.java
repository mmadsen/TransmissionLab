package org.mmadsen.sim.transmission.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmission.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

import uchicago.src.sim.util.Random;

public class NonOverlappingRandomSamplingTransmission implements
		IPopulationTransformationRule {

	private Log log = null;
	private TransmissionLabModel model = null;
	
	public NonOverlappingRandomSamplingTransmission(Log log, TransmissionLabModel model) {
		this.log = log;
		this.model = model;
	}
	
	
	public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering NonOverlappingRandomSamplingTransmission.transform()");
		return this.transmit(population);
	}

	private IAgentPopulation transmit(IAgentPopulation population) {
		List<AgentSingleIntegerVariant> agentList = population.getAgentList();
		List<AgentSingleIntegerVariant> newAgentList = new ArrayList<AgentSingleIntegerVariant>();
		int numAgents = agentList.size();
		
		// since we're just sampling with replacement, we'll simply make a new
		// list of agents randomly from the old list, and replace the agent list
		// in the population object
		for(int i = 0; i < numAgents; i++) {
			int index = Random.uniform.nextIntFromTo(0, numAgents - 1);
			//log.debug("transmit: choosing agent " + index + " to copy");
			AgentSingleIntegerVariant agent = agentList.get(index);
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