package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.util.GraphEdgeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections15.Factory;

import java.util.*;
import java.io.FileWriter;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.io.PajekNetWriter;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 3, 2008
 * Time: 3:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BARandomGraphPopulationStructure extends AbstractStructuredPopulation {
    private Graph<IAgent,Integer> socialGraph = null;
    private GraphEdgeFactory edgeFactory = null;


        // needed for instantiation via reflection
    public BARandomGraphPopulationStructure() {}

    public BARandomGraphPopulationStructure(ISimulationModel model) {
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
        this.edgeFactory = GraphEdgeFactory.getInstance();
        Factory<Graph<IAgent,Integer>> graphFactory = new GraphFactory();
        Set<IAgent> seedAgentSet = new HashSet<IAgent>();

        BarabasiAlbertGenerator<IAgent,Integer> baGenerator =
                new BarabasiAlbertGenerator<IAgent,Integer>(graphFactory, this.agentSet, this.edgeFactory,
                        50, 4, seedAgentSet);

        baGenerator.evolveGraph(this.getPopulationSize() - 50);
        this.socialGraph = baGenerator.generateGraph();

        //this.log.debug("socialGraph after evolution: " + this.socialGraph.toString());

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

    // The Barabasi-Albert generator actually does the creation of the SparseGraph object,
    // so we give it an inner class factory to work with instead of doing it ourselves.
    class GraphFactory implements Factory<Graph<IAgent,Integer>> {
        public Graph<IAgent,Integer> create() {
            return new SparseGraph<IAgent,Integer>();
        }
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
