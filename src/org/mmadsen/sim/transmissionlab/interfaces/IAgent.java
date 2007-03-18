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
	
}
