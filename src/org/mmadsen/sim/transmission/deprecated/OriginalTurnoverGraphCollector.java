package org.mmadsen.sim.transmission.deprecated;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.collections.CollectionUtils;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.interfaces.IDataCollector;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;

import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

public class OriginalTurnoverGraphCollector implements IDataCollector {
	private OpenSequenceGraph turnGraph = null;
	private TransmissionLabModel model = null;
	private Log log = null;
	private final String TYPE_CODE = this.getClass().getSimpleName();
	private int[] copy_summary = null;
	private int[] previous40 = null;
	private int[] top40 = null;
	private int[] unsortedTopAll = null;
	private int[] sortedTopAll = null;
	
	class Turnover implements Sequence {
		private TransmissionLabModel model = null;
		
		// basically we cache a ref to the enclosing class's model for cleaner
		// code later on.
		public Turnover() {
			this.model = OriginalTurnoverGraphCollector.this.model;
		}
		
		public double getSValue() {
			double z = 0.0;
			setTop40();
			z = oldGetSValue();
			
			log.debug("Turnover at t= " + this.model.getTickCount() + ": " + z);
			return z;
		}


		/*
		 * Turnover.getSValue seems to calculate the number of mismatches 
		 * between two sorted lists.  
		 * TODO:  Understand why this is a valid calculation - it does go negative...?
		 * @see uchicago.src.sim.analysis.Sequence#getSValue()
		 */
		private double oldGetSValue() {
			int [] previous40 = getPrevious40();
			int [] top40 = getTop40();
			
			List<int[]> prevColl = Arrays.asList(getPrevious40());
			List<int[]> newColl = Arrays.asList(getTop40());
			log.debug("previous40 as List: " + Arrays.deepToString(prevColl.toArray()));
			log.debug("top40 as List: " + Arrays.deepToString(newColl.toArray()));
			
			double z = 40;
			for (int i = 0; i < 40; i++) {
				for (int j = 0; j < 40; j++) {
					if (previous40[i] == top40[j]) {
						z--;
						//System.out.println("debug: previous40: " +previous40[i] + " new40: " +top40[j] + " z: " + z);
					}
				}
			}
			return z;
		}
	}
	
	public void build(Object m) {
		this.model = (TransmissionLabModel) m;
		this.log = model.getLog();
		this.log.debug("Entering OriginalTurnoverGraphCollector.build()");
		this.turnGraph = null;
		this.copy_summary = null;
		this.unsortedTopAll = null;
		this.sortedTopAll = null;
		this.top40 = null;
		this.previous40 = null;
	}

	public void completion() {
		this.log.debug("entering OriginalTurnoverGraphCollector.completion");
		if ( this.turnGraph != null) {
			this.turnGraph.dispose();
		}
	}

	public void initialize() {
		this.log.debug("entering OriginalTurnoverGraphCollector.initialize");
		turnGraph = new OpenSequenceGraph("Turnover Top40 Lists", this.model);
		turnGraph.setAxisTitles("time", "turnover");
		turnGraph.addSequence("Top 40", new Turnover());
		turnGraph.setAxisTitles("Time", "Turnover");
		turnGraph.setXRange(0, 50);
		turnGraph.setYRange(0, 30);
		turnGraph.setSize(800, 500);
		turnGraph.display();
		
		copy_summary = new int[this.model.getNumNodes() + 1];
		unsortedTopAll = new int[this.model.getNumNodes() + 1];
		sortedTopAll = new int[this.model.getNumNodes() + 1];
		previous40 = new int[40];
		top40 = new int[40];
	}

	public void process() {
		log.debug("Entering OriginalTurnoverGraphCollector.process at time " + this.model.getTickCount());
		this.resetCopySummary();
		this.turnGraph.step();
		previous40 = top40.clone();
	}

	public String getDataCollectorTypeCode() {
		return this.TYPE_CODE;
	}

	private void setTop40() {
		this.setSortedTopAll(getVariantSort(this.copy_summary));
		// MEM: simplified to remove the redundant array allocation and for loop to copy
		unsortedTopAll = copy_summary.clone();
		
		// TODO:  Introduce Logger for all this debugging stuff
//		System.out.println("sortedTopAll 0 and last 3 at t= " + getTickCount() + ": " + sortedTopAll[0] + ", " + sortedTopAll[sortedTopAll.length-1] + ", " + sortedTopAll[sortedTopAll.length-2]+ ", " +sortedTopAll[sortedTopAll.length-3]);
//		System.out.println("unsortedTopAll 0 and last 3 at t= " + getTickCount() + ": " +  unsortedTopAll[0] + ", " + unsortedTopAll[unsortedTopAll.length-1] + ", " + unsortedTopAll[unsortedTopAll.length-2]+ ", " +unsortedTopAll[unsortedTopAll.length-3]);
//		System.out.println("CopySummary 0 and last 3 at t= " + getTickCount() + ": " +  copy_summary[0] + ", " + copy_summary[copy_summary.length-1] + ", " + copy_summary[copy_summary.length - 2]+ ", " + copy_summary[copy_summary.length - 3]);

		log.debug("maxVariants = " + this.model.getMaxVariants());
		
		for (int i = 0; i < top40.length - 1; i++) {
			boolean found = false;
			for (int n = 0; n < this.model.getMaxVariants(); n++) {
				//log.debug("i = " + i + " n = " + n);
				if (!found
						&& sortedTopAll[this.model.getMaxVariants() - i] == unsortedTopAll[n]) {
					top40[i] = n;
					unsortedTopAll[n] = -1;
					found = true;
				}
			}

		}

		//System.out.println("Top 40 at t= " + getTickCount() + ": " + top40[0] + ", " + top40[1]+ ", " +top40[2]+ ", " +top40[3]);
	}
	
	// MEM:  removed redundant code, calls updateCopySummary since it did the same thing
	private int[] resetCopySummary() {
		copy_summary = null;
		copy_summary = new int[this.model.getMaxVariants() + 1];
		return updateCopySummary();
	}
	
	private int[] updateCopySummary() {
		for (int n = 0; n < this.model.getNumNodes(); n++) {
			AgentSingleIntegerVariant node = this.model.getAgentList().get(n);
			copy_summary[node.getAgentVariant()] += 1;
		}
		return copy_summary;
	}
	
	public void setSortedTopAll(int[] sta) {
		sortedTopAll = sta;
	}
	
	private int[] getVariantSort(int C[]) {
		int[] A = new int[C.length];
		for (int n = 0; n < C.length; n++) {
			A[n] = C[n];
		}
		
		
		int i, j, h = 1, v;
		
		do
			h = 3 * h + 1;
		while (h <= A.length - 1);
		
		do {
			h /= 3;
			for (i = h + 1; i <= A.length - 1; i++) {
				v = A[i];
				j = i;
				while ((j > h) && (A[j - h] > v)) {
					A[j] = A[j - h];
					j -= h;
				}
				A[j] = v;
			}
		} while (h > 1);
		
		return A;
	}
	
	private int[] getTop40() {
		return this.top40;
	}

	
	private int[] getPrevious40() {
		return this.previous40;
	}

}
