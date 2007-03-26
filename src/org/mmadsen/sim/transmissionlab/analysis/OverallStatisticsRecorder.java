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

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.BasicAction;
import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.models.TransmissionLabModel;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
import org.apache.commons.logging.Log;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    public OverallStatisticsRecorder(Object m) {
        super(m);
    }

    private TransmissionLabModel model = null;
	private Log log = null;
	private double stepToStartRecording = 0.0;
    private double meanTurnover = 0.0;
    private double stdevTurnover = 0.0;
    private double meanTraitCount = 0.0;
    private double stdevTraitCount = 0.0;


    public void build(Object model) {
        this.model = (TransmissionLabModel) model;
        this.log = this.model.getLog();
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
        this.stepToStartRecording = this.model.getNumTicks();
        this.setSchedGroupType(DataCollectorScheduleType.END);
        this.log.debug("OverallStatisticsRecorder: record data at tick: " + this.stepToStartRecording);
    }

    /*
     * TODO:  instead of plain arithmetic mean, consider winsorized or trimmed mean to deal with "early" run outliers
     */
    @Override
    public void process() {
        this.log.debug("OverallStatisticsRecorder running process()");
        DoubleArrayList turnoverHistory = (DoubleArrayList) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TURNOVER_HISTORY_KEY);
        DoubleArrayList traitCountHistory = (DoubleArrayList) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_COUNT_HISTORY_KEY);

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

        // record overall stats to a file
        this.recordStats();
    }

    @SuppressWarnings("unchecked")
	private void recordStats() {
		String filePath = this.createDataDumpFilePath();

        File neutralFile = new File(filePath);
        Boolean headerAlreadyExists = neutralFile.exists();

        try {
            // open the file for append = true since we want to gather multiple runs
            
            FileWriter writer = new FileWriter(neutralFile, true);

            if ( ! headerAlreadyExists ) {
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
                header.append("\n");
                writer.write(header.toString());
            }


            StringBuffer sb = new StringBuffer();

            sb.append(this.model.getNumAgents());
            sb.append("\t");
            sb.append(this.model.getMu());
            sb.append("\t");
            sb.append(this.model.getNumTicks());
            sb.append("\t");
            sb.append(this.model.getRngSeed());
            sb.append("\t");
            sb.append(this.model.getTopNListSize());
            sb.append("\t");
            sb.append(this.meanTurnover);
            sb.append("\t");
            sb.append(this.stdevTurnover);
            sb.append("\t");
            sb.append(this.meanTraitCount);
            sb.append("\t");
            sb.append(this.stdevTraitCount);
            sb.append("\n");

            writer.write(sb.toString());
			writer.close();
		} catch (IOException ioe) {
			log.info("IOException on filepath: "+ filePath + ": " + ioe.getMessage());
		}
	}

	/**
	 * Helper method to create a filepath usable for
	 * storing data snapshot files
	 * TODO:  Make this OS neutral for windows - works now on Mac/Linux
	 */
	private String createDataDumpFilePath() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.model.getDataDumpDirectory());
		sb.append("/");
		sb.append("transmissionlab-multiplerun-statistics");
        sb.append(".txt");
		return sb.toString();
	}
}
