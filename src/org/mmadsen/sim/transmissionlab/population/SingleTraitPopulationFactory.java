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

package org.mmadsen.sim.transmissionlab.population;

import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.util.SimParameterOptionsMap;
import uchicago.src.sim.util.RepastException;


/**
 *
 */

public class SingleTraitPopulationFactory implements IAgentSetFactory {
    private ISimulationModel model = null;
    private Log log = null;
    private SimParameterOptionsMap paramOptionsMap = null;
    public static final String INIT_TRAIT_PARAM = "InitialTraitStructure";
    public enum InitialTraitStructureOptions { SequentialTrait, GaussianTrait };
    public static final InitialTraitStructureOptions DEFAULT_INIT_TRAIT = InitialTraitStructureOptions.SequentialTrait;


    public SingleTraitPopulationFactory(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
        this.paramOptionsMap = new SimParameterOptionsMap();
        this.paramOptionsMap.addParameter(INIT_TRAIT_PARAM, InitialTraitStructureOptions.SequentialTrait.toString());
        this.paramOptionsMap.addParameter(INIT_TRAIT_PARAM, InitialTraitStructureOptions.GaussianTrait.toString());
    }

    public SimParameterOptionsMap getSimParameterOptionsMap() {
        return paramOptionsMap;
    }


    public IAgentSet generatePopulation() {
		Log log = model.getLog();
        
        String populationType = null;
        Integer numAgents = 0;
        try {
            populationType = (String) this.model.getSimpleModelPropertyByName("initialTraitStructure");
            numAgents = (Integer) this.model.getSimpleModelPropertyByName("numAgents");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

        if ( populationType == null ) {
			log.info("No initial trait structure chosen, defaulting to SequentialTrait");
			populationType = "SequentialTrait"; 
		}

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
