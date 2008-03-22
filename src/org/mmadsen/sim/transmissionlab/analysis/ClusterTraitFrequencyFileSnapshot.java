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

package org.mmadsen.sim.transmissionlab.analysis;

import cern.colt.list.DoubleArrayList;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.util.TraitCount;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.population.ConnCavemanGraphPopulationStructure;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.util.RepastException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Aug 7, 2007
 * Time: 1:21:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterTraitFrequencyFileSnapshot extends AbstractDataCollector implements IDataCollector {
    private int recordingInterval = 1;
    private int numFileSnapshots = 1;
    private int intervalCount = 0;
    private String uniqueRunIdent = null;
    private Map<Integer, TraitCount> freqMap = null;
    private ArrayList<ArrayList<TraitCount>> curSortedTraitCounts = null;
    private Closure freqCounter = null;
    public static final String TRAITS_SHARED_ACROSS_CLUSTER_COUNTS = "TRAITS_SHARED_ACROSS_CLUSTER_COUNTS";
    private Map<Integer, Map<Integer, Integer>> sharedClusterTraitCountsByTick = null;

    public ClusterTraitFrequencyFileSnapshot(ISimulationModel m) {
            super(m);
            this.model = m;
            this.log = this.model.getLog();
    }


    /**
	 * FrequencyCounter implements a Closure from the Jakarta Commons Collections
	 * library, thus allowing it to act like a functor (or "function object").  Its
	 * purpose will be to track the frequency of each variant as the closure is
	 * applied to a list of agents by CollectionUtils.forAlldo().
	 * @author mark
	 *
	 */

	class FrequencyCounter implements Closure {
		private int agentCount = 0;
		private int variantCount = 0;
		private ClusterTraitFrequencyFileSnapshot analyzer = null;

		public FrequencyCounter() {
			analyzer = ClusterTraitFrequencyFileSnapshot.this;
		}

		public void execute(Object arg0) {
			// the following is the only place in this entire set of nested classes that we "know"
			// the concrete class of the agent objects....
			AgentSingleIntegerVariant agent = (AgentSingleIntegerVariant) arg0;
			Integer agentVariant = (Integer) agent.getAgentVariant();

			if ( analyzer.freqMap.containsKey(agentVariant) == true ) {
				// we've seen the variant before; increment the count.
				TraitCount tc = analyzer.freqMap.get(agentVariant);
				tc.increment();
				analyzer.freqMap.put(agentVariant, tc);
				//log.debug("incrementing count for variant " + agentVariant + " to " + tc.getCount());
			} else {
				// this is first time we've seen this variant, initialize the count
				//log.debug("first encounter of varant " + agentVariant + ": initializing to 1");
				analyzer.freqMap.put(agentVariant, new TraitCount(agentVariant));
				variantCount++;
			}
			agentCount++;
		}

		// next three methods are purely for debugging - DO NOT USE IN PRODUCTION CODE
		public void debugResetAgentCounter() {
			agentCount = 0;
			variantCount = 0;
		}

		public int debugGetVariantCounter() {
			return variantCount;
		}

		public int debugGetAgentCounter() {
			return agentCount;
		}
	}



    /*
    This isn't working to space out the intervals because *this* schedule is then being added to a higher-level schedule
    which executes each tick, which is added to another schedule which executes each tick, and thus the interval thing
    gets kind of lost.  So for the moment everything is executing on each tick, so I suspect that I ought to just
    hack this together in 1.5pre1 for now, and re-engineer the schedule system a bit, to let these interval things
    percolate a bit better.
     */
    @Override
	protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
        this.log.debug("ClusterTraitFrequencyFileSnapshot.getSpecificSchedule() - recordingInterval: " + this.recordingInterval);
        Schedule sched = new Schedule();
        sched.scheduleActionAtInterval(this.recordingInterval, actionToSchedule);
		return sched;
	}


    public void build() {
        this.log.debug("Entering ClusterTraitFrequencyFileSnapshot.build()");
        this.freqCounter = new FrequencyCounter();
        this.curSortedTraitCounts = new ArrayList<ArrayList<TraitCount>>();
        this.freqMap = new TreeMap<Integer, TraitCount>();
        this.sharedClusterTraitCountsByTick = new TreeMap<Integer,Map<Integer,Integer>>();

    }

    public void completion() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO:  get a unique identifier from the model object and then create a specific directory for each run

    public void initialize() {
        this.log.debug("Entering ClusterTraitFrequencyFileSnapshot.initialize()");

        this.uniqueRunIdent = this.model.getUniqueRunIdentifier();
        this.log.debug("Unique run identifier: " + this.uniqueRunIdent);

        // calculate recordingInterval from the model parameter getLengthSimulationRun / numberFileSnapshots
        try {
            this.numFileSnapshots = (Integer) this.model.getSimpleModelPropertyByName("numberFileSnapshots");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

        // Remember that we add 2 ticks to the user's requested simulation run because tick 1 and tick N are
        // special, "administrative" ticks.  And thus to get the right interval, we need to restrict our
        // collection to what the user originally asked for, which is the official length - 2.
        this.recordingInterval = (this.model.getLengthSimulationRun() - 2) / this.numFileSnapshots;
        this.intervalCount = this.recordingInterval;
        this.log.debug("Recording interval for per-cluster trait file snapshots: " + this.recordingInterval);
    }

    public void process() {
        this.log.debug("Entering ClusterTraitFrequencyFileSnapshot.process()");
        IAgentPopulation population = this.model.getPopulation();
        // return immediately if the population isn't clustered, because we can't do anything.
        if (population.isPopulationClustered() == false ) { return; }

        /** nothing below here is executed if the population isn't structured into discrete clusters **/

        // manual hack to ensure that we only record files every N ticks
        if (this.intervalCount != 1) {
            this.intervalCount--;
            return;
        }

        int modelTick = (int) Math.round(this.model.getTickCount());
        int numClusters = population.getNumClusters();
        this.curSortedTraitCounts.clear();
        Map<Integer,Integer> totalTraitsAcrossClusters = new TreeMap<Integer,Integer>();

        for(int c = 0; c < numClusters; c++) {
            this.log.debug("Processing cluster #" + c);

            List<IAgent> agentList = population.getAgentListForCluster(c);

            this.freqMap.clear();
            this.curSortedTraitCounts.add(new ArrayList<TraitCount>());

            // fill up the frequency map
            CollectionUtils.forAllDo(agentList, this.freqCounter);

            // At this point, we've got all the counts, so let's prepare a sorted List
            // of TraitCounts for further processing
            ArrayList<TraitCount> refClusterCount = this.curSortedTraitCounts.get(c);
            refClusterCount.addAll(this.freqMap.values());

            // now we count traits ACROSS all cluster - we'll be interested in whether some
            // traits spread to more than 1 cluster.
            for(TraitCount tc: refClusterCount) {
                this.log.debug("analyzing refClusterCount for: " + tc.getTrait());
                int trait = tc.getTrait();
                if (totalTraitsAcrossClusters.containsKey(trait)) {
                    int cnt = totalTraitsAcrossClusters.get(trait);
                    cnt++;
                    totalTraitsAcrossClusters.put(trait,cnt);
                } else {
                    totalTraitsAcrossClusters.put(trait,1);
                }
            }
            this.log.debug("totalTraitsAcrossClusters: " + totalTraitsAcrossClusters.toString());

        }

        this.sharedClusterTraitCountsByTick.put(modelTick,totalTraitsAcrossClusters);
        this.model.storeSharedObject(TRAITS_SHARED_ACROSS_CLUSTER_COUNTS, this.sharedClusterTraitCountsByTick);

        this.log.debug("recording file snapshot at " + this.model.getTickCount());

        this.recordStats(this.curSortedTraitCounts);

        this.intervalCount = this.recordingInterval;
        this.log.debug("Leaving TraitFrequencyFileSnapshot.process()");
    }

    @SuppressWarnings("unchecked")
	private void recordStats(ArrayList<ArrayList<TraitCount>> traitCounts) {
        FileWriter writer = null;

        try {
            String outputFilename = this.createOutputFilename();
            writer = this.model.getFileWriterForPerRunOutput(outputFilename);

            StringBuffer header = new StringBuffer();
            header.append("Cluster");
            header.append("\t");
            header.append("Trait");
            header.append("\t");
            header.append("Frequency");
            header.append("\n");
            writer.write(header.toString());

            for(int i = 0; i < traitCounts.size(); i++) {
                ArrayList<TraitCount> clusterCountList = traitCounts.get(i);
                for(TraitCount trait: clusterCountList) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(i);
                    sb.append("\t");
                    sb.append(trait.getTrait());
                    sb.append("\t");
                    sb.append(trait.getCount());
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }

            writer.close();

        } catch (IOException ioe) {
			log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
		}
	}


    private String createOutputFilename() {
        Double tick = this.model.getTickCount();
        StringBuffer sb = new StringBuffer();
        sb.append("TL-trait-counts-by-cluster-");
        sb.append(tick.toString());
        sb.append(".txt");
		return sb.toString();
    }

}