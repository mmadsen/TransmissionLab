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

package org.mmadsen.sim.transmissionlab.rules;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.interfaces.IAgent;
import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
import org.mmadsen.sim.transmissionlab.interfaces.IPopulationTransformationRule;
import org.mmadsen.sim.transmissionlab.interfaces.ISimulationModel;
import org.mmadsen.sim.transmissionlab.util.TraitCount;
import org.mmadsen.sim.transmissionlab.util.TraitFrequencyCounterClosure;
import uchicago.src.sim.util.RepastException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Apr 19, 2007
 * Time: 9:05:20 AM
 * To change this template use File | Settings | File Templates.
 */

public class ModalConformistTransmissionRule implements IPopulationTransformationRule {
    private Log log = null;
	private ISimulationModel model = null;
    private Double switchingProbability = 0.0;

    // needed for instantiation via reflection
    public ModalConformistTransmissionRule() {}

    public ModalConformistTransmissionRule(ISimulationModel model) {
        this.setSimulationModel(model);
    }

    public void setSimulationModel(ISimulationModel model) {
        this.model = model;
        this.log = this.model.getLog();
    }

    public Object transform(Object pop) {
		IAgentPopulation population = (IAgentPopulation) pop;
		log.debug("entering ModalConformistTransmissionRule.transform()");
		return this.transmit(population);
    }

    private IAgentPopulation transmit(IAgentPopulation population) {
        List<IAgent> agentList = population.getAgentList();

        try {
            this.switchingProbability = (Double) this.model.getSimpleModelPropertyByName("switchingProbability");
        } catch(RepastException ex) {
            System.out.println("FATAL EXCEPTION: " + ex.getMessage());
            System.exit(1);
        }

        Map<Integer, TraitCount> frequencyMap = new HashMap<Integer, TraitCount>();
        Closure freqCounter = new TraitFrequencyCounterClosure(this.log, frequencyMap);

        // fill up the frequency map
		CollectionUtils.forAllDo(agentList, freqCounter);

        // At this point, we've got all the counts, so let's prepare a sorted List
		// of TraitCounts for further processing
		List<TraitCount> curSortedTraitCounts = new ArrayList<TraitCount>();
		curSortedTraitCounts.addAll(frequencyMap.values());

        // sort the list - in this case the highest value will be last since we haven't reversed the list
        Collections.sort(curSortedTraitCounts);


        // MUST MODIFY IN PLACE!
        return population;
    }
}
