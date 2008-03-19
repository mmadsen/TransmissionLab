package org.mmadsen.sim.transmissionlab.interfaces;

import org.apache.commons.collections15.Factory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 4, 2008
 * Time: 8:23:23 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IAgentSet extends Factory<IAgent> {
    // The extension of Factor<IAgent> is to allow an IAgentSet to
    // serve as a "vertex factory" for JUNG2 graph generators (e.g.,
    // Barabasi-Albert). 

    // we always need to be able to get the population size; this is the total number of
    // vertices/agents
    public int getPopulationSize();

	public List<IAgent> getAgentList();

    public int getCurrentMaximumVariant();

    // The Factory<Agent> interface defines a create() interface which is
    // used by JUNG2 graph generators.  When an AgentSet is treated as an
    // agent factory for JUNG2 graph generators, we simply iterate over
    // the existing agent population, rather than creating new agents.
    // Thus, we might want to reset the internal state and "do it again"
    // but I don't expect this method to see use except in specialized cases.
    public void resetAgentFactoryIterator();

    public void addAgentToSet(IAgent agent);

    public void removeAgentFromSet(IAgent agent);

}
