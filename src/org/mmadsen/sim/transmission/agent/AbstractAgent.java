/**
 * 
 */
package org.mmadsen.sim.transmission.agent;

import org.mmadsen.sim.transmission.interfaces.IAgent;

/**
 * @author mark
 *
 */
public abstract class AbstractAgent implements IAgent {
	private int agentID = 0;
	
	public AbstractAgent() {
		this.agentID = this.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmission.interfaces.IAgent#getAgentID()
	 */
	public int getAgentID() {
		return this.agentID;
	}

	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmission.interfaces.IAgent#setAgentID(int)
	 */
	public void setAgentID(int agentID) {
		this.agentID = agentID;
	}

	
}
