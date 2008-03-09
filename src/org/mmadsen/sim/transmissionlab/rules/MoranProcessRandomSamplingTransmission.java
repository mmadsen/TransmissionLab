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

package org.mmadsen.sim.transmissionlab.rules;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import uchicago.src.sim.util.Random;

import java.util.*;
import java.util.Map.Entry;

public class MoranProcessRandomSamplingTransmission implements
		IPopulationTransformationRule {

	private Log log = null;
	private ISimulationModel model = null;

    // needed for instantiation via reflection
    public MoranProcessRandomSamplingTransmission() {}

    public MoranProcessRandomSamplingTransmission(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }
	
    public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering MoranProcessRandomSamplingTransmission.transform()");
		return this.transmit(population);
	}

	private IAgentPopulation transmit(IAgentPopulation population) {
		List<IAgent> agentList = population.getAgentList();
		Map<Integer, IAgent> selectedAgentMap = new HashMap<Integer,IAgent>();
		int numAgents = agentList.size();
		
		/*
		 * In the Moran process, we simulate overlapping generations by allowing most
		 * individuals to survive each "tick", and select 2  individuals.
		 * We then treat these as a pair, copying the first's variant to the second,
		 * without modifying the actual agent objects (i.e., reproduction happens "in place"
		 */

        int agentOneIdx = Random.uniform.nextIntFromTo(0, numAgents - 1);
        int agentTwoIdx = Random.uniform.nextIntFromTo(0, numAgents - 1);

        AgentSingleIntegerVariant agentOne = (AgentSingleIntegerVariant) agentList.get(agentOneIdx);
        AgentSingleIntegerVariant agentTwo = (AgentSingleIntegerVariant) agentList.get(agentTwoIdx);

        agentTwo.setAgentVariant(agentOne.getAgentVariant());
		
        return population;
	}
	

}