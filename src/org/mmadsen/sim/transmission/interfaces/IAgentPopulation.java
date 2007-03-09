package org.mmadsen.sim.transmission.interfaces;

import java.util.List;

import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
/**
 * Interface IAgentPopulation represents an abstract view onto a population of AgentSingleIntegerVariant objects.
 * In the original model, agents were held exclusively in an ArrayList and bear no spatial or other
 * relationship to each other.  In future models we will wish to have more complex inter-agent 
 * relationships, but it would be useful to allow this without having to modify other aspects of 
 * the model -- we should be able to move from a well-mixed population to a grid lattice to a 
 * graph-theoretic model without changing anything else about data collection or other model
 * rules.  
 * 
 * Classes implementing IAgentPopulation...
 * @author mark
 *
 */
public interface IAgentPopulation {
	// No matter the underlying representation, we need to simply get a list of agents sometimes
	public List<AgentSingleIntegerVariant> getAgentList();
	// For various reasons, we may want to know the "biggest" variant we've got in the population
	public int getCurrentMaximumVariant();
	// within a transformation rule, we might create a new set of agents (based on the old, for 
	// example, and simply replace the old list held by the population with a new one).  This 
	// is also why other parts of the model ought never to hold references directly to agents
	// or the agent-list from step to step, but always retrieve the agent list fresh each time.
	public void replaceAgentList( List<AgentSingleIntegerVariant> newAgentList);
}
