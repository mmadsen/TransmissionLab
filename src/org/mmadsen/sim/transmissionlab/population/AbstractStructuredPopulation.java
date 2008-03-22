package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.apache.commons.logging.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 17, 2008
 * Time: 9:02:41 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractStructuredPopulation implements IAgentPopulation {
    protected ISimulationModel model = null;
    protected Log log = null;
    protected IAgentSet agentSet = null;

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }

    public int getPopulationSize() {
        return this.agentSet.getPopulationSize();
    }

    public List<IAgent> getAgentList() {
        return this.agentSet.getAgentList();
    }

    public abstract List<IAgent> getNeighboringAgents(IAgent agent);

    public int getCurrentMaximumVariant() {
        return this.agentSet.getCurrentMaximumVariant();
    }

    // An agent population is constructed by creating an "agent set" as a primitive unstructured
    // population, and then "decorated" (in Gang of Four parlance) with a structure, which then
    // is handed back to the model
    public abstract IAgentPopulation createStructuredPopulation(IAgentSet agentSet);
}
