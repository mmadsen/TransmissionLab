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

import org.mmadsen.sim.transmissionlab.interfaces.*;
import org.mmadsen.sim.transmissionlab.util.SimParameterOptionsMap;
import org.apache.commons.logging.Log;
import uchicago.src.sim.util.RepastException;

import java.util.Map;
import java.util.HashMap;

/**
 * StructuredPopulationFactory is a factory class which creates IAgentPopulation
 * instances with random sets of agents distributed according to some graph-theoretic
 * "constructor."  The underlying graphs are JUNG graph structures, with vertices holding
 * IAgent objects and edges representing abstract relatedness, presence in a social network, spatial
 * proximity, etc.
 *
 * The class 
 * 
 */
public class StructuredPopulationFactory implements IPopulationFactory, IParameterized {
    private SimParameterOptionsMap paramOptionsMap = null;
    private ISimulationModel model = null;
    private Log log = null;

    public static final String POP_STRUCTURE_PARAM = "PopulationStructure";
    public static final String POP_STRUCTURE_PROPERTY = "populationStructure";
    public static final PopulationStructureOptions DEFAULT_POP_STRUCTURE = PopulationStructureOptions.WellMixed;
    //public enum PopulationStructureOptions { WellMixed, RegularRandomGraph, BarabasiAlbertRG, EppsteinWangRG };
    public enum PopulationStructureOptions { WellMixed, BarabasiAlbertRG, EppsteinWangRG };
    private String wmClassName = "org.mmadsen.sim.transmissionlab.population.WellMixedPopulationStructure";
    //private String rrClassName = "org.mmadsen.sim.transmissionlab.population.RegularRandomGraphPopulationStructure";
    private String baClassName = "org.mmadsen.sim.transmissionlab.population.BARandomGraphPopulationStructure";
    private String ewClassName = "org.mmadsen.sim.transmissionlab.population.EWRandomGraphPopulationStructure";
    private Map<PopulationStructureOptions,String> structureClassMap = null;

    public StructuredPopulationFactory(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
        this.structureClassMap = new HashMap<PopulationStructureOptions,String>();
        this.paramOptionsMap = new SimParameterOptionsMap();
        this.paramOptionsMap.addParameter(POP_STRUCTURE_PARAM, PopulationStructureOptions.WellMixed.toString());
        //this.paramOptionsMap.addParameter(POP_STRUCTURE_PARAM, PopulationStructureOptions.RegularRandomGraph.toString());
        this.paramOptionsMap.addParameter(POP_STRUCTURE_PARAM, PopulationStructureOptions.BarabasiAlbertRG.toString());
        this.paramOptionsMap.addParameter(POP_STRUCTURE_PARAM, PopulationStructureOptions.EppsteinWangRG.toString());
        this.structureClassMap.put(PopulationStructureOptions.WellMixed, wmClassName);
        //this.structureClassMap.put(PopulationStructureOptions.RegularRandomGraph, rrClassName);
        this.structureClassMap.put(PopulationStructureOptions.BarabasiAlbertRG, baClassName);
        this.structureClassMap.put(PopulationStructureOptions.EppsteinWangRG, ewClassName);
    }

    public SimParameterOptionsMap getSimParameterOptionsMap() {
        return paramOptionsMap;
    }

    // Takes a raw IAgentPopulation, and returns an IAgentPopulation which has a selected
    // "structure" (including a default "well mixed" population for mean-field models).
    public IAgentPopulation generatePopulation(IAgentSet population) {
        String populationStructureType = null;
        try {
            populationStructureType = (String) this.model.getSimpleModelPropertyByName(POP_STRUCTURE_PROPERTY);
        } catch(RepastException ex) {
            this.log.error("Parameter does not exist, defaulting to " + DEFAULT_POP_STRUCTURE.toString());

        }

        if(populationStructureType == null) { populationStructureType = DEFAULT_POP_STRUCTURE.toString();}

        PopulationStructureOptions option = Enum.valueOf(PopulationStructureOptions.class,populationStructureType);
        IAgentPopulation structuredPop = this.createPopulationObject(option);
        return structuredPop.createStructuredPopulation(population);
    }

    private IAgentPopulation createPopulationObject(PopulationStructureOptions option) {
        String className = this.structureClassMap.get(option);
        Class classForPopulation = null;
        IAgentPopulation popObj = null;
        try {
            classForPopulation = Class.forName(className);
            popObj = (IAgentPopulation) classForPopulation.newInstance();
            popObj.setSimulationModel(this.model);
        } catch(Exception ex) {
            this.log.error("Class " + className + " fatal error: " + ex.getMessage());
            System.exit(1);
        }
        return popObj;
    }
}
