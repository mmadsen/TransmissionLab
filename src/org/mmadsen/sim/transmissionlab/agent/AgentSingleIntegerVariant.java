package org.mmadsen.sim.transmissionlab.agent;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;

/**
 * Generic agent class for simulations that require nothing but a single 
 * "index" trait with no structure.
 * 
 */

public class AgentSingleIntegerVariant extends AbstractAgent implements IAgent {

	int agentVariant = 1;

	@SuppressWarnings("unused")
	private Log log = null;

	public AgentSingleIntegerVariant() {
		super();
	}

	public AgentSingleIntegerVariant(int variant) {
		agentVariant = variant;
	}

	public AgentSingleIntegerVariant(int variant, Log l) {
		log = l;
		agentVariant = variant;
	}

	public int getAgentVariant() {
		return agentVariant;
	}

	public void setAgentVariant(int agvar) {
		/*
		 * if ( agvar < 0) { StringBuffer sb = new
		 * StringBuffer("setAgentVariant: "); sb.append("agent: ");
		 * sb.append(this.toString()); sb.append(" variant: ");
		 * sb.append(agvar); log.debug(sb); }
		 */
		agentVariant = agvar;
	}

	public AgentSingleIntegerVariant copyOf() {
		return new AgentSingleIntegerVariant(this.getAgentVariant(), this.log);
	}
	
}
