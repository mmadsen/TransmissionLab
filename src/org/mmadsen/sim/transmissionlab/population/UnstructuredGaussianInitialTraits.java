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

package org.mmadsen.sim.transmissionlab.population;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;

import uchicago.src.sim.util.Random;

/**
 * UnstructuredGaussianInitialTraits implements a population of simple
 * single-trait agents, where the initial traits assigned to each agent are 
 * chosen from a gaussian distribution rather than simply assigned 
 * uniquely and sequentially.  
 * 
 * TODO:  if we use this long-term, I need to make this configurable - no hard-coding the mean/stev
 * @author mark
 *
 */


public class UnstructuredGaussianInitialTraits implements IAgentPopulation {

	private ArrayList<IAgent> agentList = null;
	private int numAgents = 0;
	private double meanInitialVariant = 1000;
	private double stdevInitialVariant = 250;
	private Log log = null;
	private int currentMaxVariant = 0;
	
	public UnstructuredGaussianInitialTraits(int numAgents, Log log) {
		this.numAgents = numAgents;
		this.log = log;
		this.agentList = new ArrayList<IAgent>();
	
		Random.createNormal(meanInitialVariant, stdevInitialVariant);
		
		this.log.debug("Constructing UnstructuredGaussianInitialTrait population of " + this.numAgents + " agents");
		for( int i = 0; i < this.numAgents; i++) {
			double randomVariantDouble = Random.normal.nextDouble();
			int randomVariant = (int) randomVariantDouble;
			// simple tracking of max variant
			if ( randomVariant > this.currentMaxVariant) {
				this.currentMaxVariant = randomVariant;
			}
			// the following is the ONLY place where this class knows the concrete type of agents it's creating.
			this.agentList.add(new AgentSingleIntegerVariant(randomVariant, this.log));
		}
	}
	
	public List<IAgent> getAgentList() {
		return this.agentList;
	}

	public int getCurrentMaximumVariant() {
		return currentMaxVariant;
	}


	public void replaceAgentList(List<IAgent> newAgentList) {
		this.agentList = null;
		this.agentList = new ArrayList<IAgent>(newAgentList);
		
	}

    public int getPopulationSize() {
        return this.agentList.size();
    }

}
