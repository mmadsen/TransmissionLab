package org.mmadsen.sim.transmission.interfaces;

import org.apache.commons.logging.Log;

import uchicago.src.sim.engine.SimModelImpl;

/**
 * Interface IPopulationFactory represents any class that constructs an initial population
 * of RCMAgents.  Such classes are plug-in replacements to allow model scenarios to be held
 * constant while varying the initial construction of population.  
 * 
 * For example, the initial scenario from Bentley et al. 2007 is a population of N 
 * agents, each of which possesses a different initial variant, chosen as sequential 
 * integer tags beginning at 1 (in the revised model) and ending at N.  One might, however, 
 * wish to begin with a population initialized with a Gaussian distribution of variants,
 * a small number of pools of the same variant, etc.  
 * 
 * We also may wish to introduce spatial structure to the population of agents.  Given the 
 * complexity of each type of spatial representation and the possibly different APIs for 
 * accessing them, it will be a good thing to abstract much of the population representation
 * out of the central model class, to ensure that the model "mechanics" are as clean and 
 * accurate as possible.  
 * 
 * The interface is structured as a series of Factory methods.  One method that took XML
 * configuration or an array of param objects might be better and more generic - after I get
 * the basic structure working I'll try to further genericize it.
 * 
 * @author mark
 *
 */
public interface IPopulationFactory {
	// construct a random population of numAgent RCMAgents, with no other structure
	public IAgentPopulation generatePopulation(SimModelImpl model, Log log);
}
