package org.mmadsen.sim.transmissionlab.interfaces;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Feb 16, 2008
 * Time: 3:46:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IPopulationFactory {
    // Takes a raw IAgentPopulation, and returns an IAgentPopulation which has a selected
    // "structure" (including a default "well mixed" population for mean-field models).
    public IAgentPopulation generatePopulation(IAgentSet population);
}
