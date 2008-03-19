package org.mmadsen.sim.transmissionlab.population;

import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentSet;
import org.mmadsen.sim.transmissionlab.util.GraphEdgeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections15.Factory;

import java.util.*;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;

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

    // The Barabasi-Albert generator actually does the creation of the SparseGraph object,
    // so we give it an inner class factory to work with instead of doing it ourselves.
    class GraphFactory implements Factory<Graph<IAgent,Integer>> {
        public Graph<IAgent,Integer> create() {
            return new SparseGraph<IAgent,Integer>();
        }
    }



    public void saveGraphToFile(String filename, WriterType outputFormat) {

    }
}
