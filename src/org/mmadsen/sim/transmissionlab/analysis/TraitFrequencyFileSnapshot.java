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

import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
import org.mmadsen.sim.transmissionlab.util.TraitCount;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.util.RepastException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import cern.colt.list.DoubleArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Aug 7, 2007
 * Time: 1:21:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitFrequencyFileSnapshot extends AbstractDataCollector implements IDataCollector {
    private int recordingInterval = 1;
    private int numFileSnapshots = 1;
    private int intervalCount = 0;
    private String uniqueRunIdent = null;

    public TraitFrequencyFileSnapshot(ISimulationModel m) {
            super(m);
            this.model = m;
            this.log = this.model.getLog();
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
        this.log.debug("TraitFrequencyFileSnapshot.getSpecificSchedule() - recordingInterval: " + this.recordingInterval);
        Schedule sched = new Schedule();               
        sched.scheduleActionAtInterval(this.recordingInterval, actionToSchedule);
		return sched;
	}


    public void build() {
        this.log.debug("Entering TraitFrequencyFileSnapshot.build()");
    }

    public void completion() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO:  get a unique identifier from the model object and then create a specific directory for each run

    public void initialize() {
        this.log.debug("Entering TraitFrequencyFileSnapshot.initialize()");

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
        this.log.debug("Recording interval for file snapshots: " + this.recordingInterval);
    }

    public void process() {
        this.log.debug("Entering TraitFrequencyFileSnapshot.process()");
        // manual hack to ensure that we only record files every N ticks
        if (this.intervalCount != 1) {
            this.intervalCount--;
            return;
        }

        this.log.debug("recording file snapshot at " + this.model.getTickCount());

        // retrieve the current sorted trait counts
        ArrayList<TraitCount> curSortedTraitCounts = (ArrayList<TraitCount>) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_COUNT_LIST_KEY);

        Collections.sort(curSortedTraitCounts, TraitCount.SORT_BY_TRAIT_ID);

        this.recordStats(curSortedTraitCounts);

        this.intervalCount = this.recordingInterval;
        this.log.debug("Leaving TraitFrequencyFileSnapshot.process()");
    }

    @SuppressWarnings("unchecked")
	private void recordStats(ArrayList<TraitCount> traitCounts) {
        FileWriter writer = null;

        try {
            String outputFilename = this.createOutputFilename();
            writer = this.model.getFileWriterForPerRunOutput(outputFilename);

            StringBuffer header = new StringBuffer();
            header.append("Trait");
            header.append("\t");
            header.append("Count");
            header.append("\n");
            writer.write(header.toString());

            for( TraitCount trait: traitCounts ) {
			    StringBuffer sb = new StringBuffer();
                sb.append(trait.getTrait());
                sb.append("\t");
                sb.append(trait.getCount());
                sb.append("\n");
                writer.write(sb.toString());
            }
            writer.close();

        } catch (IOException ioe) {
			log.info("IOException on filepath: "+ this.model.getFileOutputDirectory() + ": " + ioe.getMessage());
		} 
	}


    private String createOutputFilename() {
        Double tick = this.model.getTickCount();
        StringBuffer sb = new StringBuffer();
        sb.append("TL-trait-counts-tick-");
        sb.append(tick.toString());
        sb.append(".txt");
		return sb.toString();
    }

}
