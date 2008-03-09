package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentSet;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.util.GraphEdgeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections15.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 3, 2008
 * Time: 2:48:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class WellMixedPopulationStructure implements IAgentPopulation {
    private ISimulationModel model = null;
    private Log log = null;
    private Graph<IAgent,Integer> socialGraph = null;
    private GraphEdgeFactory edgeFactory = null;
    private IAgentSet agentSet = null;

        // needed for instantiation via reflection
    public WellMixedPopulationStructure() {}

    public WellMixedPopulationStructure(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }

    // No matter the underlying representation, we need to know how many agents there are
    public int getPopulationSize() {
        return this.agentSet.getPopulationSize();
    }

    public List<IAgent> getAgentList() {
        return this.agentSet.getAgentList();
    }

    public List<IAgent> getNeighboringAgents(IAgent agent) {
        // temporary well-mixed population is just all agents....
        List<IAgent> neighborList = new ArrayList<IAgent>();
        Collection<IAgent> neighborColl = this.socialGraph.getNeighbors(agent);
        neighborList.addAll(neighborColl);
        return neighborList;
    }

    public int getCurrentMaximumVariant() {
        return this.agentSet.getCurrentMaximumVariant();
    }

    // An agent population is constructed by creating an "agent set" as a primitive unstructured
    // population, and then "decorated" (in Gang of Four parlance) with a structure, which then
    // is handed back to the model
    public IAgentPopulation createStructuredPopulation(IAgentSet agentSet) {
        this.agentSet = agentSet;
        this.socialGraph = new SparseGraph<IAgent,Integer>();
        this.edgeFactory = GraphEdgeFactory.getInstance();

        for(IAgent agent: this.getAgentList()) {
            this.socialGraph.addVertex(agent);
        }
        // now we iterate over all combinations of agents, adding an edge IF:
        // (a) agent1 != agent2 (i.e., no self-loops), and
        // (b) findEdge(agent1,agent2) == null (i.e., if we already added an edge
        // between agent1 and agent2, we don't add another edge for agent2 to agent1
        for(IAgent agent1: this.getAgentList()) {
            for(IAgent agent2: this.getAgentList()) {
                if(! agent1.equals(agent2)) {
                    if(socialGraph.findEdge(agent1,agent2) == null) {
                        socialGraph.addEdge(this.edgeFactory.create(),agent1,agent2);
                    }
                }
            }
        }
        return this;
    }
}
