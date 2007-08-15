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

package org.mmadsen.sim.transmissionlab.util;

import org.apache.commons.collections.Closure;
import org.apache.commons.logging.Log;
import org.mmadsen.sim.transmissionlab.agent.AgentSingleIntegerVariant;

import java.util.Map;


/**
 * FrequencyCounter implements a Closure from the Jakarta Commons Collections
 * library, thus allowing it to act like a functor (or "function object").  Its
 * purpose will be to track the frequency of each variant as the closure is
 * applied to a list of agents by CollectionUtils.forAlldo().
 * @author mark
 *
 */

public class TraitFrequencyCounterClosure implements Closure {
    private int agentCount = 0;
    private int variantCount = 0;
    private Map<Integer, TraitCount> frequencyMap = null;
    private Log log = null;

    public TraitFrequencyCounterClosure(Log log, Map<Integer, TraitCount> freqMap) {
        this.log = log;
        this.frequencyMap = freqMap;
    }

    public void execute(Object arg0) {
        // the following is the only place in this entire set of nested classes that we "know"
        // the concrete class of the agent objects....
        AgentSingleIntegerVariant agent = (AgentSingleIntegerVariant) arg0;
        Integer agentVariant = (Integer) agent.getAgentVariant();

        if ( this.frequencyMap.containsKey(agentVariant) == true ) {
            // we've seen the variant before; increment the count.
            TraitCount tc = this.frequencyMap.get(agentVariant);
            tc.increment();
            this.frequencyMap.put(agentVariant, tc);
            //log.debug("incrementing count for variant " + agentVariant + " to " + tc.getCount());
        } else {
            // this is first time we've seen this variant, initialize the count
            //log.debug("first encounter of varant " + agentVariant + ": initializing to 1");
            this.frequencyMap.put(agentVariant, new TraitCount(agentVariant));
            variantCount++;
        }
        agentCount++;
    }
    
    // next three methods are purely for debugging - DO NOT USE IN PRODUCTION CODE
    public void debugResetAgentCounter() {
        agentCount = 0;
        variantCount = 0;
    }

    public int debugGetVariantCounter() {
        return variantCount;
    }

    public int debugGetAgentCounter() {
        return agentCount;
    }
}