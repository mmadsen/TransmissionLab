package org.mmadsen.sim.transmission.population;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmission.models.TransmissionLabModel;
import org.mmadsen.sim.transmission.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmission.interfaces.IPopulationFactory;

import uchicago.src.sim.engine.SimModelImpl;


public class PopulationFactory implements IPopulationFactory {
	
	public IAgentPopulation generatePopulation(SimModelImpl model, Log log) {
		
		String populationType = ((TransmissionLabModel) model).getInitialTraitStructure();
		if ( populationType == null ) { 
			log.info("No initial trait structure chosen, defaulting to SequentialTrait");
			populationType = "SequentialTrait"; 
		}
		
		int numAgents = ((TransmissionLabModel) model).getNumNodes();
		
		if ( populationType.equalsIgnoreCase("SequentialTrait")) {
			log.debug("Constructing UnstructuredSequentialTrait population");
			return new UnstructuredSequentialTraits(numAgents, log);
		} else if (populationType.equalsIgnoreCase("GaussianTrait")) {
			log.debug("Constructing UnstructuredGaussianTrait population");
			return new UnstructuredGaussianInitialTraits(numAgents, log);
		} 
		
		// this should never happen but the method has to return SOMETHING in the code path
		return null;
	}

}
