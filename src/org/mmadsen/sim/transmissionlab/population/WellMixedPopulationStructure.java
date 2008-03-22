package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.util.GraphEdgeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections15.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.io.FileWriter;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.PajekNetWriter;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 3, 2008
 * Time: 2:48:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class WellMixedPopulationStructure extends AbstractStructuredPopulation {
    private Graph<IAgent,Integer> socialGraph = null;
    private GraphEdgeFactory edgeFactory = null;

    // needed for instantiation via reflection
    public WellMixedPopulationStructure() {}

    public WellMixedPopulationStructure(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public List<IAgent> getNeighboringAgents(IAgent agent) {
        // temporary well-mixed population is just all agents....
        List<IAgent> neighborList = new ArrayList<IAgent>();
        Collection<IAgent> neighborColl = this.socialGraph.getNeighbors(agent);
        neighborList.addAll(neighborColl);
        return neighborList;
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

    // Boolean which records whether this population supports distinct "clusters" of agents,
    // or whether the population is a single structure (e.g., well-mixed or an ER random graph
    // useful in selectively doing data analysis on a per-cluster whole population basis.
    public Boolean isPopulationClustered() {
        return false;
    }
    // Returns List<IAgent> for a specific cluster number, if the population supports clusters
    // if not, returns null, so use this after a call to isPopulationClustered()
    public List<IAgent> getAgentListForCluster(int cluster) {
        return null; 
    }
    // Returns List<List<IAgent>> for iteration in for() loops or with other iterators
    // If the population does not support clusters, returns null, so use this after a call to
    // isPopulationClustered()
    public List<List<IAgent>> getAgentListsByCluster() {
        return null;
    }

    // Returns number of population "clusters" if the population is clustered.  Returns 0
    // otherwise.  Should be used after a call to isPopulationClustered().
    public int getNumClusters() {
        return 0;
    }

    public void saveGraphToFile(FileWriter writer, WriterType outputFormat) {
        this.log.debug("Entering saveGraphToFile");
        if(outputFormat == IStructuredPopulationWriter.WriterType.Pajek) {
            PajekNetWriter graphWriter = new PajekNetWriter();
            try {
                graphWriter.save(this.socialGraph, writer);
            } catch( Exception ex ) {
                this.log.error("Error writing socialGraph to Pajek file: " + ex.getMessage());
            }
        }
    }
}
