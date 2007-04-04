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

package org.mmadsen.sim.transmissionlab.interfaces;

import java.util.List;
/**
 * Interface IAgentPopulation represents an abstract view onto a population of IAgent objects.
 * In the original model, agents were held exclusively in an ArrayList and bear no spatial or other
 * relationship to each other.  In future models we will wish to have more complex inter-agent 
 * relationships, but it would be useful to allow this without having to modify other aspects of 
 * the model -- we should be able to move from a well-mixed population to a grid lattice to a 
 * graph-theoretic model without changing anything else about data collection or other model
 * rules.  
 * 
 * The List of IAgent objects returned here is fully generic...a really large percentage of the 
 * simulation model doesn't need to know jack about what actual "type" an agent is, so we keep 
 * things more generic by not letting any of that code know.  What this means is that places 
 * which *do* need to know the type of an agent, need to cast the actual agent object to its
 * "real" or concrete type.  Look at the "rule" classes for examples of how narrowly you can 
 * do this.
 * 
 * Classes implementing IAgentPopulation...
 * @author mark
 *
 */
public interface IAgentPopulation {
    // No matter the underlying representation, we need to know how many agents there are
    public int getPopulationSize();
    // No matter the underlying representation, we need to simply get a list of agents sometimes
	public List<IAgent> getAgentList();
	// For various reasons, we may want to know the "biggest" variant we've got in the population
	public int getCurrentMaximumVariant();
	// within a transformation rule, we might create a new set of agents (based on the old, for 
	// example, and simply replace the old list held by the population with a new one).  This 
	// is also why other parts of the model ought never to hold references directly to agents
	// or the agent-list from step to step, but always retrieve the agent list fresh each time.
	public void replaceAgentList( List<IAgent> newAgentList);
}
