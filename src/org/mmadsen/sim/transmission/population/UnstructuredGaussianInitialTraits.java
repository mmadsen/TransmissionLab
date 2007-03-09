package org.mmadsen.sim.transmission.population;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;

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

	private ArrayList<AgentSingleIntegerVariant> agentList = null;
	private int numAgents = 0;
	private double meanInitialVariant = 1000;
	private double stdevInitialVariant = 250;
	private Log log = null;
	private int currentMaxVariant = 0;
	
	public UnstructuredGaussianInitialTraits(int numAgents, Log log) {
		this.numAgents = numAgents;
		this.log = log;
		this.agentList = new ArrayList<AgentSingleIntegerVariant>();
	
		Random.createNormal(meanInitialVariant, stdevInitialVariant);
		
		this.log.debug("Constructing UnstructuredGaussianInitialTrait population of " + this.numAgents + " agents");
		for( int i = 0; i < this.numAgents; i++) {
			double randomVariantDouble = Random.normal.nextDouble();
			int randomVariant = (int) randomVariantDouble;
			// simple tracking of max variant
			if ( randomVariant > this.currentMaxVariant) {
				this.currentMaxVariant = randomVariant;
			}
			this.agentList.add(new AgentSingleIntegerVariant(randomVariant, this.log));
		}
	}
	
	public List<AgentSingleIntegerVariant> getAgentList() {
		return this.agentList;
	}

	public int getCurrentMaximumVariant() {
		return currentMaxVariant;
	}

	public void replaceAgentList(List<AgentSingleIntegerVariant> newAgentList) {
		this.agentList = null;
		this.agentList = new ArrayList<AgentSingleIntegerVariant>(newAgentList);
	}

}
