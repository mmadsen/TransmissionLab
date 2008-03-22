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

package org.mmadsen.sim.transmissionlab.agent;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;

/**
 * Generic agent class for simulations that require nothing but a single 
 * "index" trait with no structure.
 * 
 */

public class AgentSingleIntegerVariant extends AbstractAgent  {
    private int agentVariant = 0;
    @SuppressWarnings("unused")
	private Log log = null;

	public AgentSingleIntegerVariant() {
        super();
    }

	public AgentSingleIntegerVariant(int variant) {
		super();
        this.agentVariant = variant;
    }

	public AgentSingleIntegerVariant(int variant, Log l) {
		super();
        this.log = l;
		this.agentVariant = variant;
    }

	public int getAgentVariant() {
		return this.agentVariant;
	}

	public void setAgentVariant(int agvar) {
        agentVariant = agvar;
	}

	public AgentSingleIntegerVariant copyOf() {
		return new AgentSingleIntegerVariant(this.getAgentVariant(), this.log);
	}


}
