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
import uchicago.src.sim.util.RepastException;

import java.util.List;

/**
 * @author mark
 * RandomAgentInfiniteAllelesMutation implements the original mutation rule
 * from the Bentley et al. 2007 random copying model.  
 */
public class RandomAgentInfiniteAllelesMutation implements
		IPopulationTransformationRule {
	
	private Log log = null;
	private ISimulationModel model = null;
	
    // needed for instantiation via reflection
    public RandomAgentInfiniteAllelesMutation() {}

    public RandomAgentInfiniteAllelesMutation(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }
	
	public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering RandomAgentInfiniteAllelesMutation.transform()");
		return this.mutate(population);
	}

	private IAgentPopulation mutate( IAgentPopulation population ) {
        Double mutationProbability = 0.0;
        Integer maxVariants = 0;
        try {
            mutationProbability = (Double) model.getSimpleModelPropertyByName("mu");
            maxVariants = (Integer) model.getSimpleModelPropertyByName("maxVariants");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

        List<IAgent> agentList = population.getAgentList();
        int curMaxVariant = maxVariants;
        for (IAgent agent: agentList) {
			agent = (AgentSingleIntegerVariant) agent;
			double chance = Random.uniform.nextDoubleFromTo(0, 1);
			if ( chance < mutationProbability ) {
				//this.log.debug("mutating an individual, chance was: " + chance + " mu was: " + mutationProbability);
                curMaxVariant++;
				// NOTE:  This is the only place in the class we know that we're dealing
				// with a specific agent class, so we cast very close to the actual code that needs it.
				((AgentSingleIntegerVariant)agent).setAgentVariant(curMaxVariant);
                try {
                    model.setModelPropertyByName("maxVariants", curMaxVariant);
                } catch(RepastException ex) {
                    System.out.println("FATAL EXCEPTION: " + ex.getMessage());
                    System.exit(1);
                }
			}
		}
		
		return population;
	}
	
}
