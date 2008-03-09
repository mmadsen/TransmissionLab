package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentSet;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 3, 2008
 * Time: 2:55:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegularRandomGraphPopulationStructure implements IAgentPopulation {
    // needed if we construct rules from textual classnames, since newInstance()
    // doesn't take constructor arguments.
    public void setSimulationModel(ISimulationModel model) {
    //To change body of implemented methods use File | Settings | File Templates.
    }// No matter the underlying representation, we need to know how many agents there are
    public int getPopulationSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }// No matter the underlying representation, we need to simply get a list of agents sometimes

    public List<IAgent> getAgentList() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }// All rules should use agent "neighbors" to perform transmission and copying, even if
    // the "neighbors" are the whole population in a well-mixed model
    public List<IAgent> getNeighboringAgents(IAgent agent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }// For various reasons, we may want to know the "biggest" variant we've got in the population

    public int getCurrentMaximumVariant() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // An agent population is constructed by creating an "agent set" as a primitive unstructured
    // population, and then "decorated" (in Gang of Four parlance) with a structure, which then
    // is handed back to the model
    public IAgentPopulation createStructuredPopulation(IAgentSet population) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
