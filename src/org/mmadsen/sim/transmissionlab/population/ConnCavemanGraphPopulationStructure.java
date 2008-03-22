package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.util.GraphEdgeFactory;
import org.mmadsen.sim.transmissionlab.util.TypedEdge;
import org.apache.commons.logging.Log;
import org.apache.commons.collections15.Factory;

import java.util.*;
import java.io.FileWriter;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.io.PajekNetWriter;
import uchicago.src.sim.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 3, 2008
 * Time: 3:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnCavemanGraphPopulationStructure extends AbstractStructuredPopulation {

    public enum CCEdgeType { Intracluster, Intercluster };
    private Graph<IAgent, TypedEdge<CCEdgeType>> socialGraph = null;
    private List<List<IAgent>> clusterList = null;
    private int numClusters = 1;
    private int rewiringsPerCluster = 0;

    // needed for instantiation via reflection
    public ConnCavemanGraphPopulationStructure() {}

    public ConnCavemanGraphPopulationStructure(ISimulationModel model) {
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
        int numAgents = agentSet.getPopulationSize();
        this.socialGraph = new SparseGraph<IAgent,TypedEdge<CCEdgeType>>();
        this.clusterList = new ArrayList<List<IAgent>>();

        // get the model parameters for clusters and rewirings
        this.getModelParameters();
        int agentsPerCluster = numAgents / this.numClusters;
        int agentsUnused = numAgents - (agentsPerCluster * this.numClusters);
        this.log.debug("CCGraph: agents per cluster: " + agentsPerCluster + " and unused agents: " + agentsUnused);
        // remove any unused agents from the agent Set, so they don't factor into transmission rules later
        this.pruneUnusedAgents(agentsUnused);

        // create cluster lists to track each cluster's agents
        // add the agents to each cluster list, so we can use the lists to construct the actual graph
        // this is accomplished by using the Factory<Agent> idiom that also gets used with the
        // JUNG graph generators. We iterate over the clusters, adding agentsPerCluster from
        // the agentSet to each cluster list.
        this.agentSet.resetAgentFactoryIterator();
        for(int i = 0; i < this.numClusters; i++) {
            this.clusterList.add(new ArrayList<IAgent>());
            for(int j = 0; j < agentsPerCluster; j++) {
                this.clusterList.get(i).add(this.agentSet.create());
            }
            // verify that we have the right number of agents in each cluster
            this.log.debug("cluster " + i + ": agent count: " + this.clusterList.get(i).size());

            // now create a completely connected subgraph with each cluster - the clusters will
            // still be disconnected.
            this.createCompleteSubgraphForAgentList(this.clusterList.get(i));
        }

        // rewire a minimal connected cycle through the graph
        // select a random agent from cluster i, and one from cluster i+1
        // remove an edge from the cluster i agent, and add it back from cluster i to cluster i+1
        for(int i = 0; i < this.numClusters; i++) {
            int nextCluster;
            // if we're at the last cluster, we "cycle" back to cluster 0 to complete the ring
            if ( i == (this.numClusters - 1)) {
                nextCluster = 0;
            } else {
                nextCluster = i + 1;
            }

            IAgent sourceAgent = this.selectRandomAgentFromCluster(i);
            IAgent targetAgent = this.selectRandomAgentFromCluster(nextCluster);
            TypedEdge<CCEdgeType> selectedEdge = this.selectRandomEdgeFromAgent(sourceAgent);
            this.socialGraph.removeEdge(selectedEdge);
            this.socialGraph.addEdge(new TypedEdge<CCEdgeType>(CCEdgeType.Intercluster),sourceAgent,targetAgent);
        }
        
        return this;
    }

    // Boolean which records whether this population supports distinct "clusters" of agents,
    // or whether the population is a single structure (e.g., well-mixed or an ER random graph
    // useful in selectively doing data analysis on a per-cluster whole population basis.
    public Boolean isPopulationClustered() {
        return true;
    }

    private IAgent selectRandomAgentFromCluster(int i) {
        IAgent selectedAgent = null;
        List<IAgent> agentClusterList = this.clusterList.get(i);
        int numAgentsInCluster = agentClusterList.size();
        int selection = uchicago.src.sim.util.Random.uniform.nextIntFromTo(0, numAgentsInCluster - 1);

        selectedAgent = agentClusterList.get(selection);
        return selectedAgent;
    }

    // select a randomly chosen Intracluster edge from an agent's edge set...we select only
    // intracluster edges so that we don't accidentally disconnect the graph later when we cycle back or
    // do multiple rewirings...
    private TypedEdge<CCEdgeType> selectRandomEdgeFromAgent(IAgent agent) {
        Boolean foundIntraclusterEdge = false;
        TypedEdge<CCEdgeType> chosenEdge = null;
        Collection<TypedEdge<CCEdgeType>> edgeCollection = this.socialGraph.getOutEdges(agent);
        ArrayList<TypedEdge<CCEdgeType>> edgesAsList = new ArrayList<TypedEdge<CCEdgeType>>(edgeCollection);
        int numEdges = edgeCollection.size();
        while(foundIntraclusterEdge == false) {
            int selection = uchicago.src.sim.util.Random.uniform.nextIntFromTo(0, numEdges - 1);
            TypedEdge<CCEdgeType> testEdge = edgesAsList.get(selection);
            if (testEdge.getEdgeType() == CCEdgeType.Intracluster) {
                foundIntraclusterEdge = true;
                chosenEdge = testEdge;
            }
        }
        return chosenEdge;
    }

    private void pruneUnusedAgents(int agentsUnused) {
        if (agentsUnused > 0 ) {
            List<IAgent> agentList = this.agentSet.getAgentList();
            for(int i = 0; i < agentsUnused; i++) {
                IAgent agent = agentList.get(i);
                this.agentSet.removeAgentFromSet(agent);
            }
        }
    }

    // now we iterate over all combinations of agents, adding an edge IF:
    // (a) agent1 != agent2 (i.e., no self-loops), and
    // (b) findEdge(agent1,agent2) == null (i.e., if we already added an edge
    // between agent1 and agent2, we don't add another edge for agent2 to agent1
    private void createCompleteSubgraphForAgentList(List<IAgent> agentList) {
        for(IAgent agent1: agentList) {
            for(IAgent agent2: agentList) {
                if(! agent1.equals(agent2)) {
                    if(this.socialGraph.findEdge(agent1,agent2) == null) {
                        this.socialGraph.addEdge(new TypedEdge<CCEdgeType>(CCEdgeType.Intracluster),agent1,agent2);
                    }
                }
            }
        }
    }

     public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }

    public void setRewiringsPerCluster(int rewirings) {
        this.rewiringsPerCluster = rewirings;
    }

    // This is a hack until I figure out how to do the SimParameterOptionsMap for all types of param
    private void getModelParameters() {
        try {
            this.setNumClusters((Integer) this.model.getSimpleModelPropertyByName("numClusters"));
            this.setRewiringsPerCluster((Integer) this.model.getSimpleModelPropertyByName("rewiringsPerCluster"));
        } catch(RepastException ex) {
            this.log.error("Parameter does not exist, fatal error: " + ex.getMessage());
        }
    }

    public void saveGraphToFile(FileWriter writer, IStructuredPopulationWriter.WriterType outputFormat) {
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

    public int getNumClusters() { return this.numClusters; }

    public List<IAgent> getAgentListForCluster(int cluster) {
        return this.clusterList.get(cluster);
    }

    // Returns List<List<IAgent>> for iteration in for() loops or with other iterators
    // If the population does not support clusters, returns null, so use this after a call to
    // isPopulationClustered()
    public List<List<IAgent>> getAgentListsByCluster() {
        return this.clusterList;
    }
}