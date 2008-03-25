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
import cern.jet.stat.Descriptive;
import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IStructuredPopulationWriter;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
import org.mmadsen.sim.transmissionlab.util.TraitCount;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.util.RepastException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 16, 2007
 * Time: 11:21:28 AM
 * 
 * OverallStatisticsRecorder is responsible for gathering any information about the simulation
 * run as a whole, and recording them to a data file in some format.  At the moment, this will be an
 * all-in one thing, but I can imagine a design whereby other IDataCollector modules store data in a
 * ISharedDataManager object tagged with an attribute which indicates that it should be stored at
 * the end of the run...and then this can be fully generic and just iterate over those items,
 * storing them in CSV or some other format.
 *
 */
public class OverallStatisticsRecorder extends AbstractDataCollector implements IDataCollector {

    private Log log = null;
	private double stepToStartRecording = 0.0;
    private double meanTurnover = 0.0;
    private double stdevTurnover = 0.0;
    private double meanTraitCount = 0.0;
    private double stdevTraitCount = 0.0;
    private double meanAgentCount = 0.0;
    private double stdevAgentCount = 0.0;
    private double meanResidenceTime = 0.0;
    private double stdevResidenceTime = 0.0;
    private double meanNumberClustersPerTrait = 0.0;
    private double stdevNumberClustersPerTrait = 0.0;
    private double clusteringCoefficient = 0.0;
    private double meanDistanceBetweenVertices = 0.0;
    private int numClusters = 0;
    private double mu = 0.0;
    private int numAgents = 0;
    private int topNListSize = 0;
    private static final String multipleRunOutput = "TL-multiple-run-statistics.txt";
    private static final String singleRunOutput = "TL-run-statistics.txt";
    private static final String topNTraitResidenceTimeMatrixOutput = "TL-topN-residence-time-matrix.csv";
    private static final String residenceTimeFrequenciesOutput = "TL-residence-time-frequencies.csv";
    private static final String pajekGraphOutputFile = "TL-population-structure-pajek.net";
    private static final String sharedTraitAcrossClusterFile = "TL-traits-shared-across-clusters.csv";
    private DoubleArrayList traitsAcrossClustersHistory = null;

    public OverallStatisticsRecorder(ISimulationModel m) {
		super(m);
        this.model = m;
        this.log = this.model.getLog();
        // TODO Auto-generated constructor stub
	}

    public void build() {
        this.log.debug("Entering OverallStatisticsRecorder.build()");
    }

    public void completion() {
       // no action needed...yet
    }


    
    @Override
	protected Schedule getSpecificSchedule(BasicAction actionToSchedule) {
		Schedule sched = new Schedule();
        sched.scheduleActionAt(this.stepToStartRecording, actionToSchedule);
		return sched;
	}

    public void initialize() {
        this.log.debug("Entering OverallStatisticsRecorder.initialize()");
        this.stepToStartRecording = this.model.getLengthSimulationRun();
        this.setSchedGroupType(DataCollectorScheduleType.END);
        this.log.debug("OverallStatisticsRecorder: record data at tick: " + this.stepToStartRecording);
        try {
            this.topNListSize = (Integer) this.model.getSimpleModelPropertyByName("topNListSize");
            this.mu = (Double) this.model.getSimpleModelPropertyByName("mu");
            this.numAgents = (Integer) this.model.getSimpleModelPropertyByName("numAgents");
            this.numClusters = (Integer) this.model.getSimpleModelPropertyByName("numClusters");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

    }

    /*
     * TODO:  instead of plain arithmetic mean, consider winsorized or trimmed mean to deal with "early" run outliers
     */
    @Override
    public void process() {
        this.log.debug("OverallStatisticsRecorder running process()");
        DoubleArrayList turnoverHistory = (DoubleArrayList) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TURNOVER_HISTORY_KEY);
        DoubleArrayList traitCountHistory = (DoubleArrayList) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_COUNT_HISTORY_KEY);
        DoubleArrayList agentsTopNHistory = (DoubleArrayList) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.AGENT_TRAIT_TOPN_KEY);
        Map<Integer,TraitCount> traitResidenceMap = (Map<Integer,TraitCount>) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_RESIDENCE_TIME_KEY);
        Map<Integer,ArrayList<Integer>> cumTraitTopNResidenceTimes = (Map<Integer,ArrayList<Integer>>)this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_TOPN_RESIDENCE_MAP_KEY);
        Map<Integer, Map<Integer, Integer>> sharedClusterTraitCountsByTick = (Map<Integer, Map<Integer,Integer>>) this.model.retrieveSharedObject(ClusterTraitFrequencyFileSnapshot.TRAITS_SHARED_ACROSS_CLUSTER_COUNTS);

        // calculate turnover statistics
        this.meanTurnover = Descriptive.mean(turnoverHistory);
        double varianceTurnover = Descriptive.sampleVariance(turnoverHistory, this.meanTurnover);
        this.stdevTurnover = Descriptive.standardDeviation(varianceTurnover);
        this.log.info("Mean turnover: " + this.meanTurnover + "  stdev: " + this.stdevTurnover);

        // calculate total variation statistics
        this.meanTraitCount = Descriptive.mean(traitCountHistory);
        double varianceTraitCount = Descriptive.sampleVariance(traitCountHistory, this.meanTraitCount);
        this.stdevTraitCount = Descriptive.standardDeviation(varianceTraitCount);
        this.log.info("Mean num traits in population: " + this.meanTraitCount + "  stdev: " + this.stdevTraitCount);

        // calculate stats for the number of agents with traits in the top N
        this.meanAgentCount = Descriptive.mean(agentsTopNHistory);
        double varianceAgentCount = Descriptive.sampleVariance(agentsTopNHistory, this.meanAgentCount);
        this.stdevAgentCount = Descriptive.standardDeviation(varianceAgentCount);
        this.log.info("Mean num agents with traits in top N: " + this.meanAgentCount + "  stdev: " + this.stdevAgentCount);

        // calculate stats for the "residence" time of traits - basically this is just the values from the
        // residenceTimeMap
        // 11/10/2007 - calculate ln(residenceTime) since it's a highly skewed distribution
        DoubleArrayList residenceTimeList = new DoubleArrayList();
        Map<Integer, Integer> residenceTimesFreq = new HashMap<Integer, Integer>();

        for(TraitCount tc: traitResidenceMap.values()) {
            // First we track the frequency of traits that last N ticks.
            // This data comes from the TraitCount objects contained in the traitResidenceMap.
            // We're not interested in the trait ID here, just the count of ticks the trait lasted
            // before becoming extinct.  Thus, we grab the count from each TraitCount object,
            // and hash into residenceTimesFreq and increment that "time slot" -- i.e., if trait
            // 1001 had lasted 5 ticks, we'd look at key "5" and increment it.  If key "5" hadn't
            // existed before, we'd establish it.  Pretty typical frequency counting stuff, other than
            // the fact that we're ignoring the exact trait ID.
            if (residenceTimesFreq.containsKey(tc.getCount())) {
                Integer numTraitsWithCount = residenceTimesFreq.get(tc.getCount());
                numTraitsWithCount++;
                residenceTimesFreq.put(tc.getCount(),numTraitsWithCount);
            }
            else {
                residenceTimesFreq.put(tc.getCount(),(Integer) 1);
            }

            // Now, let's add the ln(tc.getCount) to the list of residence times we'll use to calc the
            // log-mean of residence times for the final stats summary.
            residenceTimeList.add(StrictMath.log((double) tc.getCount()));
        }

        this.meanResidenceTime = Descriptive.mean(residenceTimeList);
        double varianceResidenceTime = Descriptive.sampleVariance(residenceTimeList, this.meanResidenceTime);
        this.stdevResidenceTime = Descriptive.standardDeviation(varianceResidenceTime);
        this.log.info("Mean log trait sojourn time: " + this.meanResidenceTime + "  stdev: " + this.stdevResidenceTime);

        this.traitsAcrossClustersHistory = this.extractCountTraitsAcrossClusters(sharedClusterTraitCountsByTick);
        this.meanNumberClustersPerTrait = Descriptive.mean(this.traitsAcrossClustersHistory);
        double varianceNumClustersPerTrait = Descriptive.sampleVariance(this.traitsAcrossClustersHistory,this.meanNumberClustersPerTrait );
        this.stdevNumberClustersPerTrait = Descriptive.standardDeviation(varianceNumClustersPerTrait);
        this.log.info("Mean number of clusters per trait: " + this.meanNumberClustersPerTrait + " stdev: " + this.stdevNumberClustersPerTrait);

        // record the population structure graph to a Pajek file for display and external analysis
        IAgentPopulation population = this.model.getPopulation();
        FileWriter socialGraphWriter = this.model.getFileWriterForPerRunOutput(pajekGraphOutputFile);
        population.saveGraphToFile(socialGraphWriter, IStructuredPopulationWriter.WriterType.Pajek);

        // HACK
        this.calculateGraphStatistics();
        this.log.info("Characteristic length of graph: " + this.meanDistanceBetweenVertices);
        this.log.info("Clustering coefficient of graph: " + this.clusteringCoefficient);

        // record overall stats to a file
        this.recordStats();
        this.recordResidenceMatrix(cumTraitTopNResidenceTimes);
        this.recordResidenceTimeFrequencies(residenceTimesFreq);
        if(population.isPopulationClustered()) {
            this.recordTraitsSharedAcrossClusters(sharedClusterTraitCountsByTick);
        }
    }

    @SuppressWarnings("unchecked")
	private void recordStats() {
        FileWriter runWriter = null;
        FileWriter multRunWriter = null;
        Boolean headerAlreadyExists = false;

        StringBuffer header = new StringBuffer();
        header.append("NumAgents");
        header.append("\t");
        header.append("MutationRate");
        header.append("\t");
        header.append("LengthSimRun");
        header.append("\t");
        header.append("RngSeed");
        header.append("\t");
        header.append("TopNListSize");
        header.append("\t");
        header.append("MeanTurnover");
        header.append("\t");
        header.append("StdevTurnover");
        header.append("\t");
        header.append("MeanTraitCount");
        header.append("\t");
        header.append("StdevTraitCount");
        header.append("\t");
        header.append("MeanAgentCount");
        header.append("\t");
        header.append("StdevAgentCount");
        header.append("\t");
        header.append("MeanSojournTime");
        header.append("\t");
        header.append("StdevSojournTime");
        header.append("\t");
        header.append("MeanNumClustersPerTrait");
        header.append("\t");
        header.append("StdevNumClustersPerTrait");
        header.append("\t");
        header.append("CharacteristicLength");
        header.append("\t");
        header.append("ClusteringCoefficient");
        header.append("\t");
        header.append("NumClusters");
        header.append("\n");


        try {
            headerAlreadyExists = this.model.testFileExistsInDataDirectory(multipleRunOutput);
            runWriter = this.model.getFileWriterForPerRunOutput(singleRunOutput);
            multRunWriter = this.model.getFileWriterForMultipleRunOutput(multipleRunOutput);

            
            runWriter.write(header.toString());

            if ( ! headerAlreadyExists ) {
                multRunWriter.write(header.toString());
            }


            StringBuffer sb = new StringBuffer();

            sb.append(this.numAgents);
            sb.append("\t");
            sb.append(this.mu);
            sb.append("\t");
            sb.append((this.model.getLengthSimulationRun() - 2));
            sb.append("\t");
            sb.append(this.model.getRngSeed());
            sb.append("\t");
            sb.append(this.topNListSize);
            sb.append("\t");
            sb.append(this.meanTurnover);
            sb.append("\t");
            sb.append(this.stdevTurnover);
            sb.append("\t");
            sb.append(this.meanTraitCount);
            sb.append("\t");
            sb.append(this.stdevTraitCount);
            sb.append("\t");
            sb.append(this.meanAgentCount);
            sb.append("\t");
            sb.append(this.stdevAgentCount);
            sb.append("\t");
            sb.append(this.meanResidenceTime);
            sb.append("\t");
            sb.append(this.stdevResidenceTime);
            sb.append("\t");
            sb.append(this.meanNumberClustersPerTrait);
            sb.append("\t");
            sb.append(this.stdevNumberClustersPerTrait);
            sb.append("\t");
            sb.append(this.meanDistanceBetweenVertices);
            sb.append("\t");
            sb.append(this.clusteringCoefficient);
            sb.append("\t");
            sb.append(this.numClusters);
            sb.append("\n");

            runWriter.write(sb.toString());
            multRunWriter.write(sb.toString());
            runWriter.close();
            multRunWriter.close();
        } catch (IOException ioe) {
			log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
		}
	}

    /*
        TODO: Hmm...problem here is that the matrix needs rotation to fit the output form....
        I'll get a list of fixed list positions, and then all the traits and their residence time in THAT list position
        What I want to output is a list of traits, and then a sequential list of list positions with residence time...
        Need to think about how to transpose/transform this list...
     */

    private void recordResidenceMatrix(Map<Integer,ArrayList<Integer>> cumTraitTopNResidenceTimes) {
        FileWriter residenceMatrixWriter = null;

        StringBuffer header = new StringBuffer();
        header.append("Trait");
        header.append(",");

        for(int i = 0; i < this.topNListSize; i++ ) {
            header.append(i);
            header.append(",");
        }

        header.append("\n");

        try {
            residenceMatrixWriter = this.model.getFileWriterForPerRunOutput(topNTraitResidenceTimeMatrixOutput);
            residenceMatrixWriter.write(header.toString());

            for(Map.Entry<Integer,ArrayList<Integer>> entrySet : cumTraitTopNResidenceTimes.entrySet()) {
                Integer trait = entrySet.getKey();
                ArrayList<Integer> traitPosList = entrySet.getValue();
                StringBuffer line = new StringBuffer();
                line.append(trait);
                line.append(",");
                for(Integer posCount: traitPosList) {
                    line.append(posCount);
                    line.append(",");
                }
                line.append("\n");
                residenceMatrixWriter.write(line.toString());
            }

            residenceMatrixWriter.close();

        } catch (IOException ioe ) {
             log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
        }
        
    }

    private void recordResidenceTimeFrequencies(Map<Integer,Integer> residenceTimesFreq ) {
        FileWriter residenceFreqWriter = null;


        StringBuffer header = new StringBuffer();
        header.append("ResidenceTimeTicks,");
        header.append("NumTraits");
        header.append("\n");

        try {
            residenceFreqWriter = this.model.getFileWriterForPerRunOutput(residenceTimeFrequenciesOutput);
            residenceFreqWriter.write(header.toString());

            for(Map.Entry<Integer,Integer> entrySet : residenceTimesFreq.entrySet()) {
                Integer residenceTime = entrySet.getKey();
                Integer numTraits = entrySet.getValue();
                StringBuffer line = new StringBuffer();
                line.append(residenceTime);
                line.append(",");
                line.append(numTraits);
                line.append("\n");
                residenceFreqWriter.write(line.toString());
            }
            residenceFreqWriter.close();

        } catch (IOException ioe ) {
            log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
        }
    }

    private void recordTraitsSharedAcrossClusters(Map<Integer,Map<Integer, Integer>> traitsSharedAcrossClusters) {
        FileWriter sharedTraitWriter = null;


        StringBuffer header = new StringBuffer();
        header.append("Time,");
        header.append("Trait,");
        header.append("NumClusters");
        header.append("\n");

        try {
            sharedTraitWriter = this.model.getFileWriterForPerRunOutput(sharedTraitAcrossClusterFile);
            sharedTraitWriter.write(header.toString());

            for(Map.Entry<Integer,Map<Integer,Integer>> entrySet : traitsSharedAcrossClusters.entrySet()) {
                Integer time = entrySet.getKey();
                Map<Integer,Integer> traitCountMap = entrySet.getValue();
                StringBuffer line = null;
                for(Map.Entry<Integer,Integer> countSet: traitCountMap.entrySet()) {
                    int trait = countSet.getKey();
                    int count = countSet.getValue();
                    //this.log.debug("recording traits for time: " + time + " trait: " + trait + " count: " + count);
                    line = new StringBuffer();
                    line.append(time);
                    line.append(",");
                    line.append(trait);
                    line.append(",");
                    line.append(count);
                    line.append("\n");
                    sharedTraitWriter.write(line.toString());
                }
            }
            sharedTraitWriter.close();

        } catch (IOException ioe ) {
            log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
        }
    }

    private DoubleArrayList extractCountTraitsAcrossClusters(Map<Integer,Map<Integer, Integer>> traitsSharedAcrossClusters)  {
        DoubleArrayList listCountTraitsAcrossClusters = new DoubleArrayList();
        for(Map.Entry<Integer,Map<Integer,Integer>> entrySet : traitsSharedAcrossClusters.entrySet()) {
            Map<Integer,Integer> traitCountMap = entrySet.getValue();
            for(Map.Entry<Integer,Integer> countSet: traitCountMap.entrySet()) {
                int count = countSet.getValue();
                listCountTraitsAcrossClusters.add((double) count);
            }
        }
        return listCountTraitsAcrossClusters;
    }

    // Extreme HACK - this will only work for the Connected Caveman graph!!!
    private void calculateGraphStatistics() {
        double n = (double) this.numAgents;
        double k = (double) (this.numAgents / this.numClusters);

        // characteristic (i.e., avg) length between any two vertices
        double term1 = (k / (n - 1));
        double term2numerator = n * ((n - k) - 1);
        double term2denom = 2 * (k + 1) * (n - 1);
        this.meanDistanceBetweenVertices = term1 + (term2numerator / term2denom);

        // clusting coefficient
        this.clusteringCoefficient = 1 - (6 / ((k * k) / 1));
    }
}
