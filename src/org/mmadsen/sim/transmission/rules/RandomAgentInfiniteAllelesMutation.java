package org.mmadsen.sim.transmission.rules;

import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IAgent;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmission.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

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
