package org.mmadsen.sim.transmission.models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.mmadsen.sim.transmission.agent.AgentSingleIntegerVariant;
import org.mmadsen.sim.transmission.analysis.TraitFrequencyAnalyzer;
import org.mmadsen.sim.transmission.analysis.top40DataFileRecorder;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmission.interfaces.IDataCollector;
import org.mmadsen.sim.transmission.interfaces.IPopulationFactory;
import org.mmadsen.sim.transmission.interfaces.ISharedDataManager;
import org.mmadsen.sim.transmission.population.PopulationFactory;
import org.mmadsen.sim.transmission.rules.NonOverlappingRandomSamplingTransmission;
import org.mmadsen.sim.transmission.rules.RandomAgentInfiniteAllelesMutation;
import org.mmadsen.sim.transmission.util.PopulationRuleset;
import org.mmadsen.sim.transmission.util.SharedRepository;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.engine.ActionGroup;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.ScheduleBase;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.util.Random;

public class TransmissionLabModel extends SimModelImpl implements ISharedDataManager {

	public static void main(String[] args) {
		SimInit init = new SimInit();
		TransmissionLabModel model = new TransmissionLabModel();
		init.loadModel(model, null, false);
	}
	
	private List<AgentSingleIntegerVariant> agentList = null;
	private ActionGroup allActionGroups = null;
	private ActionGroup analysisActionGroup = null;
	private List<IDataCollector> dataCollectorList = null;
	
	private Map<String, IDataCollector> dataCollectorMap = null;
	private Boolean dataCollectorsPresent = false;
	private String dataDumpDirectory = "/tmp";
	private double dataFileSnapshotPercentage = 0.10;
	private double earlyAdoptors = 0.05;
	private Boolean enableFileSnapshot = false;
	
	private Boolean enableNewTopN = true;
	private BasicAction initialAction;
	private String initialTraitStructure = null;
	private Log log = null;
	private int maxVariants = 4000;
	private ActionGroup modelActionGroup = null;
	private double mu = 0.01;
	private int numNodes = 500;
	private int numTicks = 500;
	private PopulationRuleset popRuleSet = null;
	private IAgentPopulation population = null;
	private Schedule schedule;
	private SharedRepository sharedDataRepository = null;
	private boolean showCopyHist = false;
	private int stillTrendy = 3;
	private int topNListSize = 40;
	private int Variant = 1;

	
	@SuppressWarnings("unchecked") // vector usage is type-unsafe but follows repast examples
	public TransmissionLabModel() {
			super();
			URL log4jresource = this.getClass().getResource("log4j.properties");
			PropertyConfigurator.configure(log4jresource);
			this.log = LogFactory.getLog(TransmissionLabModel.class);
			
			Vector popPropertyVec = new Vector();
			popPropertyVec.add("SequentialTrait");
			popPropertyVec.add("GaussianTrait");
			ListPropertyDescriptor pd = new ListPropertyDescriptor("InitialTraitStructure", popPropertyVec);
			descriptors.put("InitialTraitStructure", pd);
	}

	private void addDataCollector(IDataCollector collector) {
		this.dataCollectorList.add(collector);
		this.dataCollectorMap.put(collector.getDataCollectorTypeCode(), collector);
	}
	
	public void begin() {
		this.buildModel();
		
		
		// check to see if we've enabled or disabled any data collectors
		// would be nice if we could genericize this by having a standard parameter for each IDataCollector
		// but I haven't solved the problem of initializing the getInitParams() before we've actually run 
		// anything in the model yet...
		if ( this.getEnableNewTopN() == false) {
			IDataCollector d = this.dataCollectorMap.get("TraitFrequencyAnalyzer");
			this.removeDataCollector(d);
		}
		
		if ( this.getEnableFileSnapshot() == false) {
			IDataCollector d = this.dataCollectorMap.get("top40DataFileRecorder");
			this.removeDataCollector(d);
		}
		
		// now that the user has entered/modified parameters in the GUI,
		// let all of the IDataCollectors initialize themselves
		for( IDataCollector collector: this.dataCollectorList) {
			collector.initialize();
			this.analysisActionGroup.createActionFor(collector, "process");
		}

		// debug
		log.debug("dataCollectorMap: " + this.dataCollectorMap.toString());
		

		// now that the data collector list has been filtered by GUI params 
		// and built into an ActionGroup, we can set up the schedule
		this.buildSchedule();
	}
	
	
	private void buildModel() {
		IPopulationFactory factory = new PopulationFactory();
		this.population = factory.generatePopulation(this, log);
		this.agentList = this.population.getAgentList();
		this.maxVariants = this.population.getCurrentMaximumVariant();
	}


	private void buildSchedule() {
		// set up the model actions - this essentially runs the simulation
		this.modelActionGroup.createActionFor(this, "executePopulationRules");
		
		// we're stacking up two action groups, running them in sequence.  
		// first the model action group, then any analysis.  so *all* analyzer
		// modules always run *after* any modeling rule modules in any given
		// model step.  Perhaps this isn't flexible enough for all situations, 
		// but it's a good place to start.  
		this.allActionGroups.addAction(this.modelActionGroup);
		// analysis action group is already set up in begin(), which calls this method
		this.allActionGroups.addAction(this.analysisActionGroup);
		
		// set up the actual schedule.  It's pretty simple since all the complexity
		// is pushed down to the rules themselves.
		schedule.scheduleActionBeginning(1, this.allActionGroups);
		schedule.scheduleActionAt(numTicks, this, "stop", ScheduleBase.LAST);
	}
	
	// pretty much, this is all we have to do to run the stack of population rules...
	// and then we create a BasicAction for it in the modelActionGroup which 
	// schedules it to run.  that is done in buildSchedule() in the current model class
	public void executePopulationRules() {
		this.log.debug("executing population rules at tick: " + this.getNumTicks());
		this.population = (IAgentPopulation) this.popRuleSet.transform(this.population);
	}
	
	public List<AgentSingleIntegerVariant> getAgentList() {
		return this.population.getAgentList();
	}
	
	public boolean getCopyHist() {
		return showCopyHist;
	}

	public String getDataDumpDirectory() {
		return this.dataDumpDirectory;
	}
	
	public double getDataFileSnapshotPercentage() {
		return this.dataFileSnapshotPercentage;
	}

	public double getEarlyAdoptors() {
		return earlyAdoptors;
	}
	
	
	public Boolean getEnableFileSnapshot() {
		return enableFileSnapshot;
	}

	public Boolean getEnableNewTopN() {
		return this.enableNewTopN;
	}
	
	public String getInitialTraitStructure() {
		return initialTraitStructure;
	}
	
	public String[] getInitParam() {
		String[] params = { "NumNodes", "Mu", "NumTicks",
//				"StillTrendy", "EarlyAdoptors", 
			    "DataDumpDirectory", 
				"EnableNewTopN", "EnableFileSnapshot", 
				"TopNListSize", "DataFileSnapshotPercentage", "InitialTraitStructure" };

		return params;
	}
	
	public Log getLog() {
		return log;
	}
	
	public int getMaxVariants() {
		return maxVariants;
	}
	
	public double getMu() {
		return mu;
	}
	
	public String getName() {
		return "TransmissionLab";
	}
	
	
	public int getNumNodes() {
		return numNodes;
	}
	

	
	public int getNumTicks() {
		return numTicks;
	}


	public Schedule getSchedule() {
		return schedule;
	}



	public int getStillTrendy() {
		return stillTrendy;
	}


	public int getTopNListSize() {
		return topNListSize;
	}

	

	public int getVariant() {
		return Variant;
	}

	public void initialAction() {
		transmitVariants();
		
		// now we iterate over the IDataCollectors, allowing each to run
		for( IDataCollector collector: this.dataCollectorList) {
			collector.process();
		}
	}

	// TODO:  Deprecate and remove after Ruleset thoroughly tested, before 1.3 release!!
	public void mutateVariants() {
		log.debug("Entering mutateVariants at time: " + this.getTickCount());
		for (int n = 0; n < numNodes; n++) {
			double chance = Random.uniform.nextDoubleFromTo(0, 1);
			AgentSingleIntegerVariant iNode = (AgentSingleIntegerVariant) agentList.get(n);
			if (chance < getMu()) {
				this.maxVariants++;
				iNode.setAgentVariant(this.maxVariants);
			}
		}
	}

	private void removeDataCollector(IDataCollector collector) {
		this.dataCollectorList.remove(collector);
	}

	public void removeInitialAction() {
		schedule.removeAction(initialAction);
	}

	public void removeSharedObject(String key) {
		this.sharedDataRepository.removeEntry(key);
	}
	
	public Collection<Object> retrieveAllAsCollection() {
		return this.sharedDataRepository.getAllEntries();
	}

	public Object retrieveSharedObject(String key) {
		return this.sharedDataRepository.getEntry(key);
	}
	
	public void setCopyHist(boolean val) {
		showCopyHist = val;
	}

	public void setDataDumpDirectory(String dir) {
		this.dataDumpDirectory = dir;
		this.log.debug("Directory for file output: " + this.dataDumpDirectory);
	}

	public void setDataFileSnapshotPercentage(double d) {
		this.dataFileSnapshotPercentage = d;
	}


	public void setEarlyAdoptors(double eadopt) {
		earlyAdoptors = eadopt;
	}

	public void setEnableFileSnapshot(Boolean enableFileSnapshot) {
		this.enableFileSnapshot = enableFileSnapshot;
	}

	public void setEnableNewTopN( Boolean e ) {
		this.enableNewTopN = e;
	}

	public void setInitialTraitStructure(String initialTraitStructure) {
		this.initialTraitStructure = initialTraitStructure;
	}

	public void setMaxVariants( int newMaxVariant ) {
		this.maxVariants = newMaxVariant;
	}
	
	public void setMu(double mew) {
		mu = mew;
	}
	
	public void setNumNodes(int n) {
		numNodes = n;
	}


	public void setnumTicks(int timest) {
		numTicks = timest;
	}

	public void setStillTrendy(int stdy) {
		stillTrendy = stdy;
	}

	public void setTopNListSize(int topNListSize) {
		this.topNListSize = topNListSize;
	}

	public void setup() {
		this.log.info("Entering setup() for a new simulation run");
		
		/*
		 * CLEANUP SECTION - Ensure that if this is just the reset button in the GUI, 
		 * we're ready for another run, not leaking memory, and not working with stale
		 * data.
		 */
		this.schedule = null;
		this.agentList = null;
		this.Variant = 1;
		
		// if this is the first time through (i.e., just started the simulation),
		// we don't have data collectors yet, but if this is called by hitting
		// the rest for a second or later run, we need to clean out the graphs
		// and other objects held by data collectors.
		if ( this.dataCollectorsPresent == true ) {
			for( IDataCollector collector: this.dataCollectorList) {
				collector.completion();
			}
		}

		this.dataCollectorList = null;
		this.dataCollectorMap = null;
		this.dataCollectorsPresent = false;
		this.sharedDataRepository = null;
		this.popRuleSet = null;
		
		/*
		 * INSTANTIATION AND SETUP SECTION - now that we're clean and safe, construct
		 * things that need to be present for the "configuration" portion of the run --
		 * i.e., after starting the simulation and displaying the parameter panel, but before
		 * hitting "start"
		 */
		
		Random.createUniform();
	
		this.agentList = new ArrayList<AgentSingleIntegerVariant>();
		this.dataCollectorList = new ArrayList<IDataCollector>();
		this.dataCollectorMap = new HashMap<String, IDataCollector>();
		this.sharedDataRepository = new SharedRepository();
		this.schedule = new Schedule();  
		this.modelActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
		this.analysisActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
		this.allActionGroups = new ActionGroup(ActionGroup.SEQUENTIAL);
		
		// TODO: roughing out the measurement refactoring - seems clunky but it works
		IDataCollector f = new TraitFrequencyAnalyzer();
		this.addDataCollector(f);
		
		IDataCollector t = new top40DataFileRecorder();
		this.addDataCollector(t);
		
		for( IDataCollector collector: this.dataCollectorList) {
			collector.build(this);
		}
		
		this.dataCollectorsPresent = true;
		
		// now let's setup the rules that'll govern the population transformations at each step
		this.setupPopulationRules();
	}

	private void setupPopulationRules() {
		this.log.debug("Setting up population transformation rules");
		this.popRuleSet = new PopulationRuleset();
		// DEBUG: testing only - remove 
		//this.popRuleSet.addRule(new NullRule(log, this));
		
		this.popRuleSet.addRule(new RandomAgentInfiniteAllelesMutation(log, this));
		this.popRuleSet.addRule(new NonOverlappingRandomSamplingTransmission(log, this));
	}
	
	public void storeSharedObject(String key, Object value) {
		this.sharedDataRepository.putEntry(key, value);
	}

	public void transmitVariants() {
		log.debug("Entering transmitVariants at time: " + this.getTickCount());
		int[] copiedVariants = new int[numNodes];
		
		/*
		 * MEM:
		 * Create a list of variants, equal in size to the 
		 * number of agents, but chosen randomly from the 
		 * agent population, with replacement. This list will
		 * be used to then reassign all of the agents with a 
		 * variant for the next time step, from the current population
		 * of variants.  This could be the same variant they 
		 * already have, but the chance of that is proportional
		 * to the frequency of their old variant in the population.
		 * This implements a pure random sampling of traits, and
		 * implies that low-frequency traits could go "extinct" purely
		 * by drift (i.e., not being part of the sampled variation 
		 * in a given time step).
		 */
		for (int n = 0; n < numNodes; n++) {
			int index = Random.uniform.nextIntFromTo(0, numNodes - 1);
			AgentSingleIntegerVariant iNode = agentList.get(index);
			copiedVariants[n] = iNode.getAgentVariant();
		}

		/*
		 * MEM:  
		 * Right now, even though parts of this next loop 
		 * weren't commented out in the model I received, 
		 * it's not doing anything.  The 
		 * intent appears to be that we'll single out a proportion
		 * of the population as "early adoptors" and we'll give them
		 * a "trendier" trait -- however defined.  I've commented it 
		 * out completely for now, since we're not even relying upon 
		 * it for beneficial side effects, and part of it was causing
		 * our negative variant index bug.
		 */
		
		// Now the early Adopters look for "cooler copies"
		// with frequencies < theNewThing
		/*for (int i = 0; i < (numNodes * earlyAdoptors); i++) {
			//Find an early adoptor at index n
			int eAindex = Random.uniform.nextIntFromTo(0, numNodes - 1);
			AgentSingleIntegerVariant eANode = (AgentSingleIntegerVariant) agentList.get(eAindex);

			// early adopter then looks until finds another with trendy variant
			int trendyVariant = eANode.getAgentVariant();
			//while (trendyVariant < getVariant() - (int)getNMu()*getStillTrendy()) {
			//int index = Random.uniform.nextIntFromTo(0, numNodes - 1);
			//AgentSingleIntegerVariant iNode = (AgentSingleIntegerVariant)agentList.get(index);
			// trendyVariant = iNode.getAgentVariant();
			//}
			
			// MEM - this is what's causing the issue with negative array
			// indices in updateCopySummary(). Not sure what it does, actually. 
			//copiedVariants[eAindex] = getVariant() - i;//trendyVariant;

		}*/

		/*
		 * MEM:  
		 * Now we just run through the array of "new" variants and 
		 * copy those to the relevant individuals, setting up the 
		 * population for the next time step.  
		 */
		
		for (int n = 0; n < numNodes; n++) {
			AgentSingleIntegerVariant node = (AgentSingleIntegerVariant) agentList.get(n);
			node.setAgentVariant(copiedVariants[n]);
		}
	}

}
