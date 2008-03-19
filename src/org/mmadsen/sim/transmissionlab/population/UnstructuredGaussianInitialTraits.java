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

package org.mmadsen.sim.transmissionlab.population;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentSet;
import uchicago.src.sim.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * UnstructuredGaussianInitialTraits implements a population of simple
 * single-trait agents, where the initial traits assigned to each agent are 
 * chosen from a gaussian distribution rather than simply assigned 
 * uniquely and sequentially.  
 * 
 * TODO:  if we use this long-term, I need to make this configurable - no hard-coding the mean/stev
 * @author mark
 *
 */


public class UnstructuredGaussianInitialTraits implements IAgentSet {

	private ArrayList<IAgent> agentList = null;
	private int numAgents = 0;
	private double meanInitialVariant = 1000;
	private double stdevInitialVariant = 250;
	private Log log = null;
	private int currentMaxVariant = 0;
    private Iterator<IAgent> iterAgentFactory = null;

    public UnstructuredGaussianInitialTraits(int numAgents, Log log) {
		this.numAgents = numAgents;
		this.log = log;
		this.agentList = new ArrayList<IAgent>();
	
		Random.createNormal(meanInitialVariant, stdevInitialVariant);
		
		this.log.debug("Constructing UnstructuredGaussianInitialTrait population of " + this.numAgents + " agents");
		for( int i = 0; i < this.numAgents; i++) {
			double randomVariantDouble = Random.normal.nextDouble();
			int randomVariant = (int) randomVariantDouble;
			// simple tracking of max variant
			if ( randomVariant > this.currentMaxVariant) {
				this.currentMaxVariant = randomVariant;
			}
			// the following is the ONLY place where this class knows the concrete type of agents it's creating.
			this.agentList.add(new AgentSingleIntegerVariant(randomVariant, this.log));
		}
        this.iterAgentFactory = this.agentList.iterator();
    }
	
	public List<IAgent> getAgentList() {
		return this.agentList;
	}

    // All rules should use agent "neighbors" to perform transmission and copying, even if
    // the "neighbors" are the whole population in a well-mixed model
    // In this particular type of population, it is well-mixed, so we just return the agent list.
    public List<IAgent> getNeighboringAgents(IAgent agent) {
        return this.getAgentList();
    }

    public int getCurrentMaximumVariant() {
		return currentMaxVariant;
	}

    // needed if we construct rules from textual classnames, since newInstance()
    // doesn't take constructor arguments.
    public void setSimulationModel(ISimulationModel model) {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getPopulationSize() {
        return this.agentList.size();
    }

    // The Factory<Agent> interface defines a create() interface which is
    // used by JUNG2 graph generators.  When an AgentSet is treated as an
    // agent factory for JUNG2 graph generators, we simply iterate over
    // the existing agent population, rather than creating new agents.
    // Thus, we might want to reset the internal state and "do it again"
    // but I don't expect this method to see use except in specialized cases.
    public void resetAgentFactoryIterator() {
        // oddly, there's no "reset" method on an iterator, so we just
        // abandon the current one and ask for a new one.
        this.iterAgentFactory = null;
        this.iterAgentFactory = this.agentList.iterator();
    }

    public void addAgentToSet(IAgent agent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAgentFromSet(IAgent agent) {
        this.log.debug("removing agent from agentSet");
        this.agentList.remove(agent);
        this.numAgents--;
        this.resetAgentFactoryIterator();
    }

    /**
     * The create() method is NOT for actually creating agents.  It is the Factory<Agent> method
     * for passing to JUNG2 graph generator classes; it actually *iterates* over the existing
     * agents...
     * @return
     */

    public IAgent create() {
        return this.iterAgentFactory.next();
    }
}
