package org.mmadsen.sim.transmission.agent;

import org.apache.commons.logging.Log;

/**
 * Generic agent class for simulations.  Currently just holds one variant; clearly this 
 * can be extended in any number of ways.
 * 
 */

public class AgentSingleIntegerVariant {

	int agentVariant = 1;
	@SuppressWarnings("unused")
	private Log log = null;

	public AgentSingleIntegerVariant() {
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
		/*if ( agvar < 0) {
			StringBuffer sb = new StringBuffer("setAgentVariant: ");
			sb.append("agent: ");
			sb.append(this.toString());
			sb.append("  variant: ");
			sb.append(agvar);
			log.debug(sb);
		}*/
		agentVariant = agvar;
	}

}
