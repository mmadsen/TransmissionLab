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

import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;

import java.util.List;

/**
 * GraphStructuredPopulationFactory is a factory class which creates IAgentPopulation
 * instances with random sets of agents distributed according to some graph-theoretic
 * "constructor."  The underlying graphs are JUNG graph structures, with vertices holding
 * IAgent objects and edges representing abstract relatedness, presence in a social network, spatial
 * proximity, etc.
 * 
 */
public class GraphStructuredPopulationFactory implements IAgentPopulation {

    /*
    TEMP COMMENT:

    The notion here is that the user is going to select a type of graph, and will have told us
    the number of agents to use.  Depending upon the type of graph selected, we'll need a couple of
    other parameters, and then we'll construct a randomly-generated graph of the appropriate type,
    using JUNG graph generators.

    Then, we'll initialize a set of agents as normal, except that agent classes have to subclass
    JUNG's SparseVertex class (which has implementations for all the methods we care about, so this
    doesn't involve crufting up our agent class at all).

    Then, we'll retrieve the vertex set of the randomly-generated graph, and assign each agent
    to a vertex.  This can be done randomly since initial agents and their traits are arbitrary
    in this type of population anyhow.

    The latter operation is likely in GraphStructuredPopulation's constructor, perhaps being handed
    a nested IAgentPopulation which does Gaussian or Sequential initial traits.  Hmm...perhaps that's
    the right approach here -- GraphStructured populations are "facades" around composed UnstructuredPopulation
    classes for reusability.  This kind of approach gives us layers of population structure if desired.

    Then, because the transmission rules shouldn't know anything about a population, we need an abstraction
    for the transmission that returns, for any specific agent, it's "neighbor set." This set is used by
    the transmission rules to select agents to copy, etc.  In a well-mixed population this set just happens
    to be the whole n-1 agent pool; in graph structured populations this set will be related to the degree of
    the vertex/agent.

     */

    



    // No matter the underlying representation, we need to know how many agents there are
    public int getPopulationSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    // No matter the underlying representation, we need to simply get a list of agents sometimes
    public List<IAgent> getAgentList() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // For various reasons, we may want to know the "biggest" variant we've got in the population
    public int getCurrentMaximumVariant() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // within a transformation rule, we might create a new set of agents (based on the old, for
    // example, and simply replace the old list held by the population with a new one).  This
    // is also why other parts of the model ought never to hold references directly to agents
    // or the agent-list from step to step, but always retrieve the agent list fresh each time.

    public void replaceAgentList(List<IAgent> newAgentList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
