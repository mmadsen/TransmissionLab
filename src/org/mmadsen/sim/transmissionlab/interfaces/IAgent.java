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

package org.mmadsen.sim.transmissionlab.interfaces;
/**
 * Interface IAgent specifies the contract that all Agent classes need to implement
 * within TransmissionLab.  
 * 
 * 3/11/07:  currently this contract is limited, but I expect when we expand from a 
 * single trait per agent to multiple traits, structured traits, etc, we will need to add
 * methods here.
 * @author mark
 *
 */
public interface IAgent {
	/**
	 * Returns a 32-bit integer representing the ID of this agent object.  This should be 
	 * unique, and the easiest way to do that is to return the hashCode of the object itself, 
	 * though any other scheme that gives agents a unique ID is also fine.
	 * @return int - ID of an individual agent. 
	 */
	public int getAgentID();
	
	/**
	 * Sets the current ID field of the agent, overriding any existing ID.  This should be 
	 * unique, though the method itself has no post-condition contract to guarantee that
	 * because it has no automatic access to all other agent objects to determine this.  
	 * 
	 * Thus, if you override default agent ID construction (e.g., from the AbstractAgent
	 * base class, it is YOUR RESPONSIBILITY to ensure that agent ID's are unique.  Why 
	 * would you override the default behavior?  A simple default ID might be the hashCode
	 * of the object, for example, and you might want a simple small integer (e.g., agent 1, 2...n) 
	 * rather than a 32bit number representing the JVM heap address of the object.  
	 * 
	 * @param agentID - int representing the desired ID for the agent object.  
	 */
	public void setAgentID(int agentID);

    /**
     * Returns the number of traits held by an agent.
     */
    public int getNumTraitsHeld();

    /**
     * Sets the number of trait slots this agent holds, or is expected to hold at maximum.  In some
     * models these will be the same thing, but when modeling situations where we "grow" the
     * trait list through innovation, one should not rely on a pre-existing notion of this value
     * but instead use the getNumTraitsHeld() value to index the traits actually held, or use
     * an iterator-style construction instead.
     */
    public void setNumTraitsToHold(int numTraits);
}
