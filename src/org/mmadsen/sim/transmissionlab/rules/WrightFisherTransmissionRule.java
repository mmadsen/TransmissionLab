package org.mmadsen.sim.transmissionlab.rules;

import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.apache.commons.logging.Log;

import java.util.List;

import uchicago.src.sim.util.RepastException;
import uchicago.src.sim.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Jan 21, 2008
 * Time: 1:02:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrightFisherTransmissionRule implements IPopulationTransformationRule {
    private Log log = null;
    private ISimulationModel model = null;
    private Integer numAgents = 0;

    public WrightFisherTransmissionRule(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();

        try {
            this.numAgents = (Integer) this.model.getSimpleModelPropertyByName("numAgents");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }
    }

    public Object transform(Object pop) {

        IAgentPopulation population = (IAgentPopulation) pop;
        log.debug("entering WrightFisherTransmissionRule.transform()");
        return this.transmit(population);
    }

    // It is essential, in a world where the IAgentPopulation could hold structure
    // as well as an agent list, to return the SAME population object.  In other words,
    // the former method whereby I simply constructed a whole new agent list
    // and replaced the agent list in the IAgentPopulation object is deprecated, since
    // it would destroy all references in the JUNG2 Graph between vertices.

    // The new method is to generate a sequence of random integers between 0 and numAgents - 1
    // and copy just the variant of the individuals at those indices, storing them in a simple
    // Integer array.  Then we make pass 2 through the agentList and reassign their traits
    // from the integer array.  Thus, sampling is with replacement, and does not modify the
    // original agent object ID's.  And since we do not modify as we go, the population from which each
    // agent's new variant is sampled is "constant" for the operation of transmission - thus non-overlapping
    // generations are approximated.

    private IAgentPopulation transmit(IAgentPopulation population) {
        List<IAgent> agentList = population.getAgentList();
        Integer[] sampledTraitArray = new Integer[this.numAgents];

        // pass 1:  select new variants from the existing population with replacement
        for(int i = 0; i < numAgents; i++) {
            int index = Random.uniform.nextIntFromTo(0, this.numAgents - 1);
            sampledTraitArray[i] = ((AgentSingleIntegerVariant) agentList.get(index)).getAgentVariant();
        }

        for(int i = 0; i < numAgents; i++) {
            AgentSingleIntegerVariant agentRef = (AgentSingleIntegerVariant) agentList.get(i);
            agentRef.setAgentVariant(sampledTraitArray[i]);
        }
        
        return population;
    }
}
