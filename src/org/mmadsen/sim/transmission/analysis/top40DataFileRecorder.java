package org.mmadsen.sim.transmission.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.analysis.TraitFrequencyAnalyzer.TraitCount;
import org.mmadsen.sim.transmission.interfaces.IDataCollector;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

/**
 * @author mark
 * 
 * top40DataFileRecorder is an IDataCollector which dumps top40 data to 
 * files during a specified percentage of model ticks.  For example, 
 * if the recording percentage is 10%, and the length of a model
 * run is 500 ticks, data recording will begin in tick 450 and 
 * continue through 500.  Thus, to get data recording throughout the 
 * model run, the recording percentage should be specified at 100%; 
 * similarly, a recording percentage of 0% will turn off all data recording.
 * 
 * The file format is slightly different, just because we might care about 
 * which traits exist, and tab-delimited is easy to import into Excel, so 
 * each snapshot file is:
 * 
 * traitID	<tab>	traitCount	<CRLF>
 * ...
 * 
 * These data are the curSortedTraitCounts from TraitFrequencyAnalyzer, so 
 * they also are written in reverse or descending frequency order - most 
 * frequent trait to least frequent.  
 */
public class top40DataFileRecorder implements IDataCollector {

	private TransmissionLabModel model = null;
	private Log log = null;
	private double stepToStartRecording = 0.0;
	private final String TYPE_CODE = this.getClass().getSimpleName();
	
	/* No implementation needed; each file snapshot cleans up after itself
	 * @see org.mmadsen.sim.transmission.IDataCollector#completion()
	 */
	public void completion() {
	}

	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmission.IDataCollector#build()
	 */
	public void build(Object m) {
		this.model = (TransmissionLabModel) m;
		this.log = model.getLog();
		this.log.debug("Entering top40DataFileRecorder.build()");
	}

	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmission.IDataCollector#initialize()
	 */
	public void initialize() {
		// calculate the tickCount for when we start recording
		double ticksToRecord = this.model.getNumTicks() * this.model.getDataFileSnapshotPercentage();
		this.stepToStartRecording = this.model.getNumTicks() - ticksToRecord;
		this.log.debug("initialize: will start recording after tick: " + this.stepToStartRecording);
		
	}

	/* (non-Javadoc)
	 * @see org.mmadsen.sim.transmission.IDataCollector#process()
	 */
	public void process() {
		if( this.model.getTickCount() < this.stepToStartRecording) {
			// nothing to do yet, so don't slow things down!
			return;
		}
		// ok, let's record some data
		log.debug("Recording file snapshot at " + this.model.getTickCount());
		this.recordAction();
	}
	
	public String getDataCollectorTypeCode() {
		return this.TYPE_CODE;
	}
	
	@SuppressWarnings("unchecked")
	private void recordAction() {
		String filePath = this.createDataDumpFilePath();
		List<TraitCount> curSortedTraitCounts = (List<TraitCount>) this.model.retrieveSharedObject(TraitFrequencyAnalyzer.TRAIT_COUNT_LIST_KEY);
		
		File neutralFile = new File(filePath);
		try {
			FileWriter writer = new FileWriter(neutralFile);
			StringBuffer sb = new StringBuffer();
			
			for(TraitCount trait: curSortedTraitCounts) {
				sb.append(trait.getTrait());
				sb.append("\t");
				sb.append(trait.getCount());
				sb.append("\r\n");
			}
			
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
		sb.append("rcm-data-numnodes-");
		sb.append(this.model.getNumNodes());
		sb.append("-step-");
		sb.append(this.model.getTickCount());
		sb.append("-mu-");
		sb.append(this.model.getMu());
		sb.append(".txt");
		return sb.toString();
	}

}
