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

/**
 * 
 */
package org.mmadsen.sim.transmissionlab.agent;

import org.mmadsen.sim.transmissionlab.interfaces.IAgent;

/**
 * @author mark
 *
 */
public abstract class AbstractAgent implements IAgent {

    private int agentID = 0;
    private static int nextAgentId = 0;
    private int numTraitsHeld = 1;

    public AbstractAgent() {
        this.setAgentID(nextAgentId);
        nextAgentId++;
	}
	
	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmissionlab.interfaces.IAgent#getAgentID()
	 */
	public int getAgentID() {
		return this.agentID;
	}

	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmissionlab.interfaces.IAgent#setAgentID(int)
	 */
	public void setAgentID(int agentID) {
		this.agentID = agentID;
	}

    public int getNumTraitsHeld() {
        return this.numTraitsHeld;
    }

    public void setNumTraitsToHold(int numTraits) {
        this.numTraitsHeld = numTraits;
    }

}
