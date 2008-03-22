package org.mmadsen.sim.transmissionlab.agent;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Mar 20, 2008
 * Time: 11:07:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class AgentMultipleIntegerVariants extends AbstractAgent {
    private List<Integer> agentVariantList = null;
    private Log log = null;

    public AgentMultipleIntegerVariants(int numTraitsToHold, Log log) {
        super();
        this.log = log;
        this.setNumTraitsToHold(numTraitsToHold);
    }

    public int getAgentVariant(int variantNum) {
        return this.agentVariantList.get(variantNum);
    }

    public void setAgentVariant(int variantNum, int value) {
        this.agentVariantList.set(variantNum, value);
    }

    public AgentMultipleIntegerVariants copyOf() {
        AgentMultipleIntegerVariants newAgent = new AgentMultipleIntegerVariants(this.getNumTraitsHeld(),this.log);
        for(int i = 0; i < this.getNumTraitsHeld(); i++) {
            newAgent.setAgentVariant(i, this.getAgentVariant(i));
        }
        
        return newAgent;
	}
    
}
