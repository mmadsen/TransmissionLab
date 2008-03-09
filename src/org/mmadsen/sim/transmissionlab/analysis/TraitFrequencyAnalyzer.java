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
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.util.TraitCount;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.util.RepastException;

import java.util.*;


/**
 * TraitFrequencyAnalyzer is an IDataCollector module for analyzing the "top 40" properties
 * of a set of RCMAgents which store cultural variants.  Currently the assumption is that
 * each agent only possesses one variant but this will be generalized in future releases.
 * 
 * The general idea here is that we're going to make a single pass through the list of 
 * agents, either creating a new TraitCount value object if we see a new trait (with count = 1),
 * or incrementing an existing one.  This is facilitated by temporarily storing the TraitCounts
 * in a TreeMap indexed by trait ID.  The TraitCount value object not only stores the count
 * and trait ID together for easy access to either (an improvement on a raw Map) but allows
 * us to provide a custom sort order based on frequency, not trait number or object ID.  Thus
 * we implement Comparable and provide a compareTo() implementation that sorts by trait count.
 * 
 * The counting pass is facilitated by creating a Closure (from Commons Collections) that 
 * does the actual TraitCount manipulation.  This closure is then passed to 
 * CollectionUtils.forAlldo() over the agent list.  
 * 
 * Once the counting pass is completed, we initialize a List<TraitCount> from the TreeMap,
 * and since TraitCount implements Comparable, Collections.sort() provides us the 
 * list sorted by trait count in descending order (what we want if we're reading off "top N"
 * statistics).
 * 
 * Finally, "turnover" in "top N" statistics become relatively easy.   
 * If we assume that turnover is defined strictly in terms of the number of 
 * elements which are in either list BUT NOT BOTH, we can figure out "turnover" of 
 * that list by finding the cardinality of the complement of set intersection of an "old"
 * and "new" top n collection.  CollectionUtils.intersection() returns a collection which 
 * is the intersection (e.g., intersectionCollection), and thus turnover can be defined 
 * for a "top N" list as N - intersectionCollection.size();  
 * 
 * To calculate turnover, we store the sorted results of the previous TraitCount pass
 * in addition to the current results.  We do not store any older (previous previous, etc) 
 * versions of the lists.  
 * 
 * Uses the default implementation of getDataCollectorSchedule() from AbstractDataCollector
 * 
 * @author mark
 *
 */

public class TraitFrequencyAnalyzer extends AbstractDataCollector implements IDataCollector {
	public TraitFrequencyAnalyzer(ISimulationModel m) {
		super(m);
        this.model = m;
        this.log = this.model.getLog();
        // TODO Auto-generated constructor stub
	}

	public static final String TRAIT_COUNT_LIST_KEY = "TRAIT_COUNT_LIST_KEY";
    public static final String TURNOVER_HISTORY_KEY = "TURNOVER_HISTORY_KEY";
    public static final String TRAIT_COUNT_HISTORY_KEY = "TRAIT_COUNT_HISTORY_KEY";
    public static final String AGENT_TRAIT_TOPN_KEY = "AGENT_TRAIT_TOPN_KEY";
    public static final String TRAIT_RESIDENCE_TIME_KEY = "TRAIT_RESIDENCE_TIME_KEY";
    public static final String TRAIT_TOPN_RESIDENCE_MAP_KEY = "TRAIT_TOPN_RESIDENCE_MAP_KEY";
    private OpenSequenceGraph turnGraph = null;
	private OpenSequenceGraph totalVariabilityGraph = null;
    private OpenHistogram residenceTimeHistogram = null;
    private ISimulationModel model = null;
	private Log log = null;
	private Closure freqCounter = null;
    private Map<Integer, TraitCount> freqMap = null;
    private Map<Integer, TraitCount> residenceTimeMap = null;
    private ArrayList<TraitCount> prevSortedTraitCounts = null;
	private ArrayList<TraitCount> curSortedTraitCounts = null;
    private ArrayList<TraitCount> cumTraitResidenceTimeCounts = null;
    //private ArrayList<Map<Integer,TraitCount>> cumTraitTopNResidenceTimes = null;
    private Map<Integer,ArrayList<Integer>> cumTraitTopNResidenceTimes = null;
    private DoubleArrayList turnoverHistory = null;
    private DoubleArrayList traitCountHistory = null;
    private DoubleArrayList agentsInTopNHistory = null;
    private int topNListSize = 0;
	private double ewensVariationLevel = 0.0;
	private int ewensThetaMultipler = 0;
    private double curTurnover = 0.0;
    private Boolean isBatchRun = false;
    private double mu = 0.0;
    private int numAgents = 0;

    
	
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
		private TraitFrequencyAnalyzer analyzer = null;
		
		public FrequencyCounter() {
			analyzer = TraitFrequencyAnalyzer.this;
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
    

    /**
	 * TurnoverSequence is a data Sequence from the Repast libraries, 
	 * designed to provide a stream of double values to an OpenSequenceGraph.
	 * 
	 * What this does is actually calculate a turnover value, given the 
	 * sorted list of TraitCounts from the previous time step and this time step.
	 * 
	 * The sequence method getSValue() will be called by OpenSequenceGraph.step(), 
	 * so the precondition contract here is that the graph's step() method must be 
	 * called from within IDataCollector.process(), at a time when prevSortedTraitCounts
	 * holds the counts from tickCount - 1, and curSortedTraitCounts holds the counts
	 * from the current model tick.  This is kinda hard to guarantee programmatically, 
	 * but if you have bugs, beware -- call step() at the right time!
	 * @author mark
	 *
	 */
	class TurnoverSequence implements Sequence {
		
		/**
		 * precondition contract:  prevSortedTraitCounts != null, curSortedTraitCounts != null
		 * if prevSortedTraitCounts == null, it's the first tick on the model and we return 0
		 * otherwise, calculate "set intersection turnover"
		 * 
		 * we define "set intersection turnover" as the number of traits which are NOT part 
		 * of the intersection of the prev and cur TraitCount lists.  This, in turn, means 
		 * that the turnover is:  (prev.size + cur.size) - ( 2 * intersection.size )	
		 * 
		 */
		public double getSValue() {
            return curTurnover;
		}




    }
	
	class TotalVariabilitySequence implements Sequence {

		public double getSValue() {
			return (double) curSortedTraitCounts.size();
		}
		
	}
	
	class EwensSequence implements Sequence {

		public double getSValue() {
			// We return a constant value here since we're aiming at a "reference" line on the 
			// total variation graph
			return ewensVariationLevel;
		}
		
	}

	
	public void build() {
        this.log.debug("Entering TraitFrequencyAnalyzer.build()");
		this.freqCounter = new FrequencyCounter();
        this.freqMap = new TreeMap<Integer, TraitCount>();
        this.residenceTimeMap = new TreeMap<Integer, TraitCount>();
        this.isBatchRun = this.model.getBatchExecution();
        //this.cumTraitTopNResidenceTimes = new ArrayList<Map<Integer,TraitCount>>();
        this.cumTraitTopNResidenceTimes = new HashMap<Integer,ArrayList<Integer>>();
    }

	public void completion() {
		this.log.debug("entering TraitFrequencyAnalyzer.completion");
		if ( this.turnGraph != null ) {
			this.turnGraph.dispose();
		}
		if ( this.totalVariabilityGraph != null) {
			this.totalVariabilityGraph.dispose();
		}
        if ( this.residenceTimeHistogram != null ) {
            this.residenceTimeHistogram.dispose();
        }

        this.curSortedTraitCounts = null;
		this.prevSortedTraitCounts = null;
        this.residenceTimeMap = null;
        this.cumTraitResidenceTimeCounts = null;
        this.cumTraitTopNResidenceTimes = null;
        this.model.removeSharedObject(TRAIT_COUNT_LIST_KEY);
	}

	public void initialize() {
		this.log.debug("entering TraitFrequencyAnalyzer.initialize()");

        try {
            this.topNListSize = (Integer) this.model.getSimpleModelPropertyByName("topNListSize");
            this.ewensThetaMultipler = (Integer) this.model.getSimpleModelPropertyByName("ewensThetaMultipler");
            this.mu = (Double) this.model.getSimpleModelPropertyByName("mu");
            this.numAgents = (Integer) this.model.getSimpleModelPropertyByName("numAgents");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

        this.turnoverHistory = new DoubleArrayList();
        this.traitCountHistory = new DoubleArrayList();
        this.agentsInTopNHistory = new DoubleArrayList();
        // there needs to be an actual List object at initialization of the histogram, even if there's no data yet.
        this.cumTraitResidenceTimeCounts = new ArrayList<TraitCount>();
        this.cumTraitResidenceTimeCounts.add(new TraitCount(0));

        // we need to track the number of ticks each trait that ever makes the TopN list spends in each
        // position of the list.  E.G., we care that trait 101 spends 5 ticks in 1st place, 3 ticks in 2nd place, etc.
        /*for( int i = 0; i < this.topNListSize; i++ ) {
            this.cumTraitTopNResidenceTimes.add(new HashMap<Integer, TraitCount>());
        }*/

        this.ewensVariationLevel = this.ewensThetaMultipler * this.mu * this.numAgents;
		this.log.info("Ewens " + this.ewensThetaMultipler + "Nmu variation level is: " + this.ewensVariationLevel);
		if ( ! this.isBatchRun ) {
            this.turnGraph = new OpenSequenceGraph("Trait Turnover per Step", this.model);
            this.turnGraph.setAxisTitles("time", "turnover");
            StringBuffer sb = new StringBuffer();
            sb.append("Top ");
            sb.append(this.topNListSize);
            this.turnGraph.addSequence(sb.toString(), new TurnoverSequence());
            this.turnGraph.setXRange(0, 50);
            this.turnGraph.setYRange(0, 30);
            this.turnGraph.setSize(400, 250);
            this.turnGraph.display();

            this.totalVariabilityGraph = new OpenSequenceGraph("Total Number of Traits in Population", this.model);
            this.totalVariabilityGraph.setAxisTitles("time", "# of Traits");
            this.totalVariabilityGraph.addSequence("num traits", new TotalVariabilitySequence());
            this.totalVariabilityGraph.addSequence("Ewens " + this.ewensThetaMultipler + "Nmu", new EwensSequence());
            //this.totalVariabilityGraph.addSequence("Avg. Traits", new AverageTraitCountSequence());
            this.totalVariabilityGraph.setXRange(0, 50);
            this.totalVariabilityGraph.setYRange(0, 100);
            this.totalVariabilityGraph.setSize(400, 250);
            this.totalVariabilityGraph.display();

            this.residenceTimeHistogram = new OpenHistogram("Trait Residence Time Distribution", 25, 0);

            // TODO: The sojourn time graph would be better as a log-normal plot.  Explore how to do that....
            this.residenceTimeHistogram.createHistogramItem("Residence Time", this.cumTraitResidenceTimeCounts, "getCountAsPrimitiveInt", -1, 0);
            this.residenceTimeHistogram.display();
        }
    }

	@SuppressWarnings("unchecked")
	public void process() {
		this.log.debug("Entering TraitFrequencyAnalyzer.process at time " + this.model.getTickCount());
		// cache a fresh copy of the agent list since it may have changed due to other module's actions
		IAgentPopulation population = this.model.getPopulation();
		List<IAgent> agentList = population.getAgentList();

		// clear out the frequency map, and current list of sorted TraitCounts and recount
        // DO NOT clear out residenceTimeMap, however, since it's cumulative over the course of the run
        this.freqMap.clear();
		this.curSortedTraitCounts = null;
        this.curTurnover = 0.0;

        // fill up the frequency map
		CollectionUtils.forAllDo(agentList, this.freqCounter);

        // At this point, we've got all the counts, so let's prepare a sorted List
		// of TraitCounts for further processing
		this.curSortedTraitCounts = new ArrayList<TraitCount>();
		this.curSortedTraitCounts.addAll(this.freqMap.values());

        // update the residence time map for traits seen in this tick
        for(TraitCount trait: this.curSortedTraitCounts) {
            int agentVariant = trait.getTrait();

            if ( this.residenceTimeMap.containsKey(agentVariant) ) {
				// we've seen the variant before; increment the count.
				TraitCount tc = this.residenceTimeMap.get(agentVariant);
				tc.increment();
				this.residenceTimeMap.put(agentVariant, tc);

			} else {
				// this is first time we've seen this variant, initialize the count
                this.residenceTimeMap.put(agentVariant, new TraitCount(agentVariant));
            }
        }

        // now update the list of trait residence times for the OpenHistogram, since it doesn't use Maps
        this.cumTraitResidenceTimeCounts.clear();
        this.cumTraitResidenceTimeCounts.addAll(this.residenceTimeMap.values());


        // capture the number of traits present in the population currently into the historical list
        this.traitCountHistory.add(this.curSortedTraitCounts.size());
        Collections.sort(curSortedTraitCounts);
		Collections.reverse(curSortedTraitCounts);

        // MEM: refactored out of the Sequence class to allow the simulation to run in batch mode
        this.curTurnover = this.calculateTurnover();
        
        // this is the right time to call the graph step() -- prevSortedTraitCounts still
		// represents tickCount - 1, and curSortedTraitCounts represents this tick.
        if ( ! this.isBatchRun ) {
            this.turnGraph.step();
		    this.totalVariabilityGraph.step();
            this.residenceTimeHistogram.step();
        }

        // store the current version of the turnoverHistory list in the shared repository in case
        // another module wants the current snapshot, for moving averages or something similiar
        this.model.storeSharedObject(TURNOVER_HISTORY_KEY, this.turnoverHistory);
        this.model.storeSharedObject(TRAIT_COUNT_HISTORY_KEY, this.traitCountHistory);
        this.model.storeSharedObject(AGENT_TRAIT_TOPN_KEY, this.agentsInTopNHistory);
        this.model.storeSharedObject(TRAIT_RESIDENCE_TIME_KEY, this.residenceTimeMap);
        this.model.storeSharedObject(TRAIT_TOPN_RESIDENCE_MAP_KEY, this.cumTraitTopNResidenceTimes);

        // housekeeping - store cur in prev for comparison next time around
		// and cache the current trait counts in the model shared repository for 
		// other modules to use - note that inter-step comparisons *within* this class don't use
        // the shared repository - we're writing the object to the repository for other classes
        this.model.storeSharedObject(TRAIT_COUNT_LIST_KEY, this.curSortedTraitCounts);
		this.prevSortedTraitCounts = this.curSortedTraitCounts;
        this.log.debug("Leaving TraitFrequencyAnalyzer.process at time " + this.model.getTickCount());
    }
	
	//	 helper method to reduce duplication - held in the outer class so it
    // // can be used by all inner classes.
	private List<Integer> getTopNTraits( List<TraitCount> traitCounts ) {
		ArrayList<Integer> listOfTraits = new ArrayList<Integer>();
		for( TraitCount trait: traitCounts ) {
			listOfTraits.add(trait.getTrait());
		}
		if (listOfTraits.size() > topNListSize ) {
			return listOfTraits.subList(0, topNListSize);
		}
		// otherwise return the whole list if it's smaller than "top N"
		return listOfTraits;
	}

    /**
     * getNumAgentsInTopN is a helper method which calculates the number of agents which make up that "top N" set
     * of traits -- in other words, it gives us a measure of evenness of agent
     * distribution in the sense that 10% of the agents could be in the top 20 traits,
     * or 90% of the agents could be in the top 20 traits.
     *
     * @param traitCounts - List<TraitCount> of all TraitCount objects, which combine the trait and its frequency.
     * @return counts - List<Integer> of the ID numbers of the top N traits, in reverse sorted order
     */
    private int getNumAgentsInTopN( List<TraitCount> traitCounts ) {
        int listSize = topNListSize;
        int numAgentsInTopN = 0;

        if ( traitCounts.size() < listSize ) {
            listSize = traitCounts.size();
        }

        for( int i = 0; i < listSize; i++ ) {
            TraitCount tc = traitCounts.get(i);
            numAgentsInTopN += tc.getCount();
        }

        return numAgentsInTopN;
    }

    /**
     * calculateTurnover() runs through the prev and current sorted lists of TraitCount
     * objects, and calculates turnover (additions and removals) of traits from "top N"
     * lists (i.e., truncating prev and cur sorted lists if list.size > topNlistsize).
     * The topN cur and prev lists are then intersected, and we return turnover as:
     * (prevsize + cursize) - (2 * intersection.size)
     * @return turnover - double representing turnover, calculated from current data
     */
    private double calculateTurnover() {
        double turnover = 0.0;
        //log.debug("lists should be trimmed to " + topNListSize);

        if ( prevSortedTraitCounts == null ) {
            // this will happen on the first tick, after that we should be fine
            return 0;
        }

        // given the sorted trait frequencies tallied in the IDataCollector process()
        // method, extract the top N traits and the number of agents that have traits in that top N list
        List prevList = this.getTopNTraits(prevSortedTraitCounts);
        List curList = this.getTopNTraits(curSortedTraitCounts);
        int numAgentsInTopN = this.getNumAgentsInTopN(curSortedTraitCounts);

        log.debug("TFA:  num agents with traits in top N: " + numAgentsInTopN);

        // update the tracking information for how long traits in the TopN spend in each position
        this.updateCumTopNResidenceByTrait(curList);

        // now find the intersection of these two sorted trait ID lists
        Collection intersection = CollectionUtils.intersection(prevList, curList);
        log.debug("TFA:  previous: " + Arrays.deepToString(prevList.toArray()));
        log.debug("TFA:  current: " + Arrays.deepToString(curList.toArray()));
        log.debug("TFA:  intersection: " + Arrays.deepToString(intersection.toArray()));

        // now use the list sizes and the cardinality of the intersection set to calculate turnover
        int prevSize = prevList.size();
        int curSize = curList.size();
        int intersectionSize = intersection.size();
        turnover = (prevSize + curSize) - ( 2 * intersection.size());
        log.debug("prev size: " + prevSize + " cursize: " + curSize + " intersection size: " + intersectionSize + " turnover: " + turnover);

        // add the calculated to the turnover history
        this.turnoverHistory.add(turnover);
        this.agentsInTopNHistory.add((double) numAgentsInTopN);

        return turnover;
    }



    /*
     * New implementation of tracking how long traits in the TopN spend in each list position
     *
     * It turns out that we don't want an ArrayList of Map<Integer, TraitCount>, because we want
     * to easily create the output data matrix with traits as rows, and columns as top N list positions,
     * with counts (0...maxTicks) in the cells.  So the new implementation makes that easier out
     * the back end, by using a different data structure up front.  Instead, we use:
     * Map<Integer, ArrayList>.
     *
     * TODO:  OK, the approach to use ArrayList isn't working, because as I work my way down
     * the top N list, I'm not hitting the *same* ArrayList each time, so add doesn't do the trick.  ahhh....
     * need *two* tests, not one, since this is a *sparse* matrix...
     */

    private void updateCumTopNResidenceByTrait(List<Integer> curTraitsTopN) {
        int listPos = 0;
        for(Integer trait: curTraitsTopN) {
            if (this.cumTraitTopNResidenceTimes.containsKey(trait)) {
                // Because the ArrayList has been initialized to hold zeros for each trait in each
                // top N list position, if we've seen the trait before in the map we can get on
                // with the business of incrementing a count, whether that count is zero or not.
                Integer count = this.cumTraitTopNResidenceTimes.get(trait).get(listPos);
                count++;
                this.cumTraitTopNResidenceTimes.get(trait).set(listPos, count);
            } else {
                // First create a new ArrayList and initialize 0..topNListSize to 0 counts,
                // so all array positions are defined for the upper branch of the "if"
                ArrayList<Integer> traitCountList = new ArrayList<Integer>();
                for(int i = 0; i < this.topNListSize; i++ ) {
                    traitCountList.add(0);
                }
                
                // now increment the counter for the one trait/list position combo we're dealing with
                traitCountList.set(listPos, 1);
                this.cumTraitTopNResidenceTimes.put(trait, traitCountList);
            }
            listPos++;
        }
    }

    @Override
	protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
		Schedule sched = new Schedule();
		sched.scheduleActionBeginning(2, actionToSchedule);
		return sched;
	}



}
