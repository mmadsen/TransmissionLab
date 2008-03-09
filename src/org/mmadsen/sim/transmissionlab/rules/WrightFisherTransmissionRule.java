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

     // needed for instantiation via reflection
    public WrightFisherTransmissionRule() {}

    public WrightFisherTransmissionRule(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
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

    /*private IAgentPopulation nonSpatialTransmit(IAgentPopulation population) {
        // For the moment, we don't really care about the neighbors, so we pass in null.
        List<IAgent> agentList = population.getNeighboringAgents(null);
        this.numAgents = agentList.size();
        Integer[] sampledTraitArray = new Integer[this.numAgents];

        // pass 1:  select new variants from the existing population with replacement
        for(int i = 0; i < this.numAgents; i++) {
            int index = Random.uniform.nextIntFromTo(0, this.numAgents - 1);
            sampledTraitArray[i] = ((AgentSingleIntegerVariant) agentList.get(index)).getAgentVariant();
        }

        for(int i = 0; i < this.numAgents; i++) {
            AgentSingleIntegerVariant agentRef = (AgentSingleIntegerVariant) agentList.get(i);
            agentRef.setAgentVariant(sampledTraitArray[i]);
        }
        
        return population;
    }*/

    // New version of a fully neighborhood-based WrightFisher model, suitable
    // for use when the population is actually on a graph
    private IAgentPopulation transmit(IAgentPopulation population) {
        List<IAgent> flatAgentList = population.getAgentList();
        this.log.debug("WF: agent list size: "+ flatAgentList.size());
        this.numAgents = flatAgentList.size();
        Integer[] sampledTraitArray = new Integer[this.numAgents];

        // pass 1:  for each agent in the population, find the list of neighbors,
        // and select a random neighbor (including self) for a trait to take
        // copy that trait into the sampledTraitArray
        for(int i = 0; i < this.numAgents; i++) {
            List<IAgent> neighborList = population.getNeighboringAgents(flatAgentList.get(i));
            int numNeighbors = neighborList.size();
            //this.log.debug("neighbor list size: " + numNeighbors);
            if(numNeighbors == 0) {
                // If a given individual has no neighbors, they keep their trait...
                sampledTraitArray[i] = ((AgentSingleIntegerVariant)flatAgentList.get(i)).getAgentVariant();
            } else {
                int index = Random.uniform.nextIntFromTo(0, numNeighbors - 1);
                sampledTraitArray[i] = ((AgentSingleIntegerVariant) neighborList.get(index)).getAgentVariant();
            }
        }

        // pass 2:  go back through the full agent list, assigning each agent the trait
        // we selected in pass 1
        for(int i = 0; i < this.numAgents; i++) {
            AgentSingleIntegerVariant agentRef = (AgentSingleIntegerVariant) flatAgentList.get(i);
            agentRef.setAgentVariant(sampledTraitArray[i]);
        }

        return population;
    }
}
