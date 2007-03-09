package org.mmadsen.sim.transmission.population;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;


public class UnstructuredSequentialTraits implements IAgentPopulation {

	private ArrayList<AgentSingleIntegerVariant> agentList = null;
	private int numAgents = 0;
	private int initialVariant = 1;
	private Log log = null;
	private int currentMaxVariant = 0;
	
	public UnstructuredSequentialTraits(int numAgents, Log log) {
		this.numAgents = numAgents;
		this.log = log;
		this.agentList = new ArrayList<AgentSingleIntegerVariant>();
		int curVariant = this.initialVariant;
		
		this.log.debug("Constructing UnstructuredSequentialTrait population of " + this.numAgents + " agents");
		for( int i = 0; i < this.numAgents; i++) {
			this.agentList.add(new AgentSingleIntegerVariant(curVariant, this.log));
			curVariant++;
		}
		this.currentMaxVariant = curVariant;
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
