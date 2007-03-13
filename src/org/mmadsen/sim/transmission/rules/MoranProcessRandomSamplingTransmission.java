package org.mmadsen.sim.transmission.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IAgent;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmission.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

import uchicago.src.sim.util.Random;

public class MoranProcessRandomSamplingTransmission implements
		IPopulationTransformationRule {

	private Log log = null;
	private TransmissionLabModel model = null;
	private int numReproductivePairsPerTick = 1;
	
	public MoranProcessRandomSamplingTransmission(Log log, TransmissionLabModel model) {
		this.log = log;
		this.model = model;
	}
	
	public void setReproductivePairsPerTick( int pairs ) {
		this.numReproductivePairsPerTick = pairs;
	}
	
	public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering NonOverlappingRandomSamplingTransmission.transform()");
		return this.transmit(population);
	}

	private IAgentPopulation transmit(IAgentPopulation population) {
		List<IAgent> agentList = population.getAgentList();
		Map<Integer, IAgent> selectedAgentMap = new HashMap<Integer,IAgent>();
		int numAgents = agentList.size();
		
		/*
		 * In the Moran process, we simulate overlapping generations by allowing most
		 * individuals to survive each "tick", and select 2 * f individuals.  
		 * We then treat these as f pairs, where one individual in the pair is removed
		 * from the population and replaced by a clone of the other individual.  In the
		 * classic Moran process, "f" is typically 1.  That should really slow down the 
		 * evolution of the population, so we'll make "f" configurable, and test how things
		 * scale with increasing f.  
		 * 
		 * The original Moran process selected two unique individuals, one to die, one to be
		 * copied to occupy the dead individual's "slot" -- to generalize this to N pairs of 
		 * individuals, we need all of the numReproductivePairsPerTick agents to be *unique*
		 * otherwise we have a problem, and NoSuchElementExceptions, and general havoc.  Plus,
		 * it wouldn't be faithful to the underlying process we're modeling.   
		 * 
		 */
		int numUniqueAgentsNeeded = this.numReproductivePairsPerTick * 2;
		while(numUniqueAgentsNeeded != 0) {
			int index = Random.uniform.nextIntFromTo(0, numAgents - 1);
			if (!selectedAgentMap.containsKey(index)) {
				selectedAgentMap.put(index, agentList.get(index));
				numUniqueAgentsNeeded--;
			}
		}
		
		// Now iterate over the selected pairs of agents, removing the first agent
		// and replacing it in the agentList with a copy of the second agent of the pair.  
		// This will yield one more copy of whatever variants all the "agent 2's" represent
		// at the end of the step.
		Set<Entry<Integer, IAgent>> entries = selectedAgentMap.entrySet();
		Iterator entryIter = entries.iterator();
		while(entryIter.hasNext()) {
			Entry entry1 = (Entry) entryIter.next();
			Entry entry2 = (Entry) entryIter.next();
			Integer indexAgent1 = (Integer) entry1.getKey();
			IAgent agent2 = (IAgent) entry2.getValue();
			
			agentList.set(indexAgent1, ((AgentSingleIntegerVariant)agent2).copyOf());
			
		}
		
		// finally, store the agent list as just modified.
		population.replaceAgentList(agentList);
		
		return population;
	}
	

}